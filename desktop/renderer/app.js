const state = {
  view: 'dashboard',
  fornecedores: [],
  solicitacoes: [],
  selectedRequestId: null,
  lastEvaluation: new Map()
};

const titles = {
  dashboard: ['Visão geral', 'Acompanhe o fluxo de compras e cotações.'],
  solicitacoes: ['Solicitações', 'Consulte e opere as solicitações de compra.'],
  nova: ['Nova solicitação', 'Crie uma solicitação e defina a estratégia de avaliação.'],
  fornecedores: ['Fornecedores', 'Cadastre e consulte fornecedores participantes.'],
  sobre: ['Sobre o projeto', 'Veja onde cada Design Pattern foi aplicado.'],
  detalhe: ['Detalhes da solicitação', 'Cotações, avaliação e decisão de compra.']
};

const content = document.getElementById('content');
const pageTitle = document.getElementById('pageTitle');
const pageSubtitle = document.getElementById('pageSubtitle');
const supplierDialog = document.getElementById('supplierDialog');
const quoteDialog = document.getElementById('quoteDialog');
const approvalDialog = document.getElementById('approvalDialog');
const rejectDialog = document.getElementById('rejectDialog');

function esc(value) {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');
}

function money(value, currency = 'BRL') {
  if (value === null || value === undefined) return '—';
  try {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency }).format(Number(value));
  } catch (_) {
    return `${value} ${currency}`;
  }
}

function date(value) {
  if (!value) return '—';
  return new Intl.DateTimeFormat('pt-BR').format(new Date(`${value}T12:00:00`));
}

function dateTime(value) {
  if (!value) return '—';
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
}

function statusLabel(status) {
  return {
    RASCUNHO: 'Rascunho',
    EM_COTACAO: 'Em cotação',
    AGUARDANDO_APROVACAO: 'Aguardando aprovação',
    APROVADA: 'Aprovada',
    REJEITADA: 'Rejeitada'
  }[status] || status;
}

function criterioLabel(value) {
  return {
    MENOR_PRECO: 'Menor preço',
    MENOR_PRAZO: 'Menor prazo',
    CUSTO_BENEFICIO: 'Custo-benefício'
  }[value] || value;
}

function toast(title, message = '', type = '') {
  const node = document.createElement('div');
  node.className = `toast ${type}`;
  node.innerHTML = `<strong>${esc(title)}</strong>${message ? `<span>${esc(message)}</span>` : ''}`;
  document.getElementById('toastContainer').appendChild(node);
  setTimeout(() => node.remove(), 4200);
}

function err(response) {
  if (!response?.data) return `Erro HTTP ${response?.status || ''}`.trim();
  const detail = response.data.detail || response.data.message || response.data.title;
  const validation = response.data.errors ? Object.values(response.data.errors).join(' · ') : '';
  return [detail, validation].filter(Boolean).join(' — ') || `Erro HTTP ${response.status}`;
}

async function api(method, endpoint, body) {
  const response = await window.compraflow.api.request(method, endpoint, body);
  if (!response.ok) throw new Error(err(response));
  return response.data;
}

function loading() {
  content.innerHTML = '<div class="loading">Carregando...</div>';
}

function setHeader(view) {
  const [title, subtitle] = titles[view] || titles.dashboard;
  pageTitle.textContent = title;
  pageSubtitle.textContent = subtitle;
}

function setActiveNav(view) {
  document.querySelectorAll('.nav-item').forEach((button) => {
    button.classList.toggle('active', button.dataset.view === view);
  });
}

async function refreshData() {
  const [fornecedores, solicitacoes] = await Promise.all([
    api('GET', '/api/fornecedores'),
    api('GET', '/api/solicitacoes')
  ]);
  state.fornecedores = fornecedores || [];
  state.solicitacoes = solicitacoes || [];
}

async function go(view, options = {}) {
  state.view = view;
  if (options.id) state.selectedRequestId = Number(options.id);
  setHeader(view);
  setActiveNav(view === 'detalhe' ? 'solicitacoes' : view);
  loading();
  try {
    if (view !== 'sobre') await refreshData();
    if (view === 'dashboard') renderDashboard();
    if (view === 'solicitacoes') renderSolicitacoes();
    if (view === 'nova') renderNova();
    if (view === 'fornecedores') renderFornecedores();
    if (view === 'sobre') renderSobre();
    if (view === 'detalhe') await renderDetalhe(state.selectedRequestId);
  } catch (error) {
    content.innerHTML = `<div class="empty"><strong>Não foi possível carregar</strong>${esc(error.message)}</div>`;
    toast('Erro ao carregar dados', error.message, 'error');
  }
}

function requestTable(rows) {
  if (!rows.length) {
    return '<div class="empty"><strong>Nenhuma solicitação cadastrada</strong>Crie a primeira solicitação para iniciar o fluxo.</div>';
  }
  return `<div class="table-wrap"><table><thead><tr><th>Código</th><th>Solicitante</th><th>Departamento</th><th>Critério</th><th>Status</th><th>Criada em</th></tr></thead><tbody>${rows.map((item) => `
    <tr>
      <td><button class="link-button" data-request-id="${item.id}">${esc(item.codigo)}</button></td>
      <td>${esc(item.solicitante)}</td>
      <td>${esc(item.departamento)}</td>
      <td>${esc(criterioLabel(item.criterioAvaliacao))}</td>
      <td><span class="badge ${item.status}">${esc(statusLabel(item.status))}</span></td>
      <td>${esc(dateTime(item.criadaEm))}</td>
    </tr>`).join('')}</tbody></table></div>`;
}

function renderDashboard() {
  const counts = state.solicitacoes.reduce((acc, item) => {
    acc[item.status] = (acc[item.status] || 0) + 1;
    return acc;
  }, {});
  const recent = [...state.solicitacoes].sort((a, b) => b.id - a.id).slice(0, 6);
  content.innerHTML = `<div class="cards">
    <div class="metric"><div class="label">Solicitações</div><div class="value">${state.solicitacoes.length}</div><div class="hint">Total cadastrado</div></div>
    <div class="metric"><div class="label">Em cotação</div><div class="value">${counts.EM_COTACAO || 0}</div><div class="hint">Recebendo propostas</div></div>
    <div class="metric"><div class="label">Aguardando aprovação</div><div class="value">${counts.AGUARDANDO_APROVACAO || 0}</div><div class="hint">Com fornecedor selecionado</div></div>
    <div class="metric"><div class="label">Fornecedores</div><div class="value">${state.fornecedores.length}</div><div class="hint">Ativos para cotação</div></div>
  </div>
  <div class="section"><div class="section-head"><div><h2>Solicitações recentes</h2><p>Últimos processos de compra cadastrados.</p></div><button class="btn ghost small" data-go="solicitacoes">Ver todas</button></div>${requestTable(recent)}</div>`;
}

function renderSolicitacoes() {
  content.innerHTML = `<div class="section" style="margin-top:0"><div class="section-head"><div><h2>Todos os processos</h2><p>${state.solicitacoes.length} solicitação(ões) cadastrada(s).</p></div><button class="btn primary small" data-go="nova">＋ Nova solicitação</button></div>${requestTable(state.solicitacoes)}</div>`;
}

function renderNova() {
  content.innerHTML = `<form id="requestForm" class="form-card">
    <div class="form-grid two">
      <label>Solicitante<input name="solicitante" required maxlength="120" placeholder="Nome do solicitante"></label>
      <label>Departamento<input name="departamento" required maxlength="100" placeholder="Ex.: TI, Financeiro, Compras"></label>
    </div>
    <label style="margin-top:16px">Justificativa<textarea name="justificativa" required maxlength="1000" rows="4" placeholder="Explique a necessidade da compra"></textarea></label>
    <label style="margin-top:16px">Estratégia de avaliação<select name="criterioAvaliacao"><option value="MENOR_PRECO">Menor preço</option><option value="MENOR_PRAZO">Menor prazo</option><option value="CUSTO_BENEFICIO" selected>Custo-benefício</option></select></label>
    <div class="section-head" style="padding-left:0;padding-right:0;margin-top:20px"><div><h2>Itens da solicitação</h2><p>Adicione um ou mais itens.</p></div><button type="button" class="btn ghost small" id="addItemButton">＋ Adicionar item</button></div>
    <div id="itemsBox" class="items-box"></div>
    <div class="form-actions"><button type="button" class="btn ghost" data-go="solicitacoes">Cancelar</button><button class="btn primary" type="submit">Criar solicitação</button></div>
  </form>`;
  addItem();
}

function addItem(item = {}) {
  const box = document.getElementById('itemsBox');
  if (!box) return;
  const row = document.createElement('div');
  row.className = 'item-row';
  row.innerHTML = `<input data-field="descricao" required maxlength="180" placeholder="Descrição" value="${esc(item.descricao || '')}">
    <input data-field="quantidade" required type="number" min="1" value="${Number(item.quantidade || 1)}">
    <input data-field="unidade" required maxlength="20" value="${esc(item.unidade || 'UN')}">
    <input data-field="especificacao" maxlength="1000" placeholder="Especificação (opcional)" value="${esc(item.especificacao || '')}">
    <button type="button" class="icon-btn remove-item">×</button>`;
  box.appendChild(row);
}

function renderFornecedores() {
  const rows = state.fornecedores.map((fornecedor) => `<tr><td><strong>${esc(fornecedor.razaoSocial)}</strong></td><td>${esc(fornecedor.cnpj)}</td><td>${esc(fornecedor.email)}</td><td><span class="badge">${fornecedor.ativo ? 'Ativo' : 'Inativo'}</span></td></tr>`).join('');
  content.innerHTML = `<div class="section" style="margin-top:0"><div class="section-head"><div><h2>Fornecedores ativos</h2><p>Empresas disponíveis para registrar propostas.</p></div><button class="btn primary small" id="newSupplierButton">＋ Novo fornecedor</button></div><div class="table-wrap"><table><thead><tr><th>Razão social</th><th>CNPJ</th><th>E-mail</th><th>Status</th></tr></thead><tbody>${rows}</tbody></table></div></div>`;
}

function actions(request) {
  const buttons = [];
  if (request.status === 'RASCUNHO') {
    buttons.push('<button class="btn ghost" id="editRequestButton">✎ Editar</button>');
    buttons.push('<button class="btn danger" id="deleteRequestButton">Excluir</button>');
  }
  buttons.push('<button class="btn ghost" id="printRequestButton">Imprimir</button>');
  buttons.push('<button class="btn ghost" id="savePdfButton">Salvar PDF</button>');
  if (request.status === 'RASCUNHO') buttons.push('<button class="btn primary" id="openQuotePhaseButton">Abrir para cotação</button>');
  if (request.status === 'EM_COTACAO') buttons.push('<button class="btn primary" id="evaluateButton">Avaliar cotações</button>');
  if (request.status === 'AGUARDANDO_APROVACAO') {
    buttons.push('<button class="btn success" id="approveButton">Aprovar</button>');
    buttons.push('<button class="btn danger" id="rejectButton">Rejeitar</button>');
  }
  return buttons.join('');
}

function quoteTable(quotes, selectedId) {
  if (!quotes.length) return '<div class="empty"><strong>Nenhuma cotação recebida</strong>Abra o processo e registre ao menos três propostas.</div>';
  return `<div class="table-wrap"><table><thead><tr><th>Fornecedor</th><th>Total</th><th>Entrega</th><th>Pagamento</th><th>Validade</th><th></th></tr></thead><tbody>${quotes.map((quote) => `<tr><td>${esc(quote.fornecedor)}</td><td>${money(quote.totalOriginal, quote.moeda)} <small>${quote.moeda}</small></td><td>${quote.prazoEntregaDias} dias</td><td>${quote.prazoPagamentoDias} dias</td><td>${date(quote.validade)}</td><td>${selectedId === quote.id ? '<span class="badge APROVADA">Selecionada</span>' : ''}</td></tr>`).join('')}</tbody></table></div>`;
}

async function renderDetalhe(id) {
  const request = await api('GET', `/api/solicitacoes/${id}`);
  const quotes = await api('GET', `/api/solicitacoes/${id}/cotacoes`);
  const evaluation = state.lastEvaluation.get(id);
  content.innerHTML = `<div class="detail-grid"><div>
    <div class="detail-card">
      <div style="display:flex;justify-content:space-between"><div><h2 style="margin:0 0 5px">${esc(request.codigo)}</h2><span class="badge ${request.status}">${esc(statusLabel(request.status))}</span></div><button class="btn ghost small" data-go="solicitacoes">← Voltar</button></div>
      <p style="color:var(--muted);margin:17px 0 0;line-height:1.6">${esc(request.justificativa)}</p>
      <div class="action-row">${actions(request)}</div>
      ${request.status === 'RASCUNHO' ? '<div class="subtle-note">Registros antigos em rascunho também podem ser editados e excluídos normalmente.</div>' : ''}
    </div>
    <div class="section"><div class="section-head"><div><h2>Itens solicitados</h2><p>${request.itens.length} item(ns)</p></div></div><div class="table-wrap"><table><thead><tr><th>Descrição</th><th>Qtd.</th><th>Unidade</th><th>Especificação</th></tr></thead><tbody>${request.itens.map((item) => `<tr><td>${esc(item.descricao)}</td><td>${item.quantidade}</td><td>${esc(item.unidade)}</td><td>${esc(item.especificacao || '—')}</td></tr>`).join('')}</tbody></table></div></div>
    <div class="section"><div class="section-head"><div><h2>Cotações recebidas</h2><p>${quotes.length} proposta(s) registrada(s).</p></div>${request.status === 'EM_COTACAO' ? '<button class="btn primary small" id="addQuoteButton">＋ Registrar cotação</button>' : ''}</div>${quoteTable(quotes, request.cotacaoSelecionadaId)}</div>
    ${evaluation ? `<div class="winner"><strong>Resultado da avaliação</strong><div style="margin-top:7px">${esc(evaluation.fornecedorVencedor)} · ${money(evaluation.valorTotalBrl)} · ${evaluation.prazoEntregaDias} dia(s)</div><div style="color:var(--muted);font-size:12px;margin-top:5px">${esc(evaluation.justificativa)}</div></div>` : ''}
  </div>
  <aside class="detail-card"><h3>Dados do processo</h3><div class="meta-list">
    <div class="meta"><span>Solicitante</span><b>${esc(request.solicitante)}</b></div>
    <div class="meta"><span>Departamento</span><b>${esc(request.departamento)}</b></div>
    <div class="meta"><span>Estratégia</span><b>${esc(criterioLabel(request.criterioAvaliacao))}</b></div>
    <div class="meta"><span>Valor selecionado</span><b>${money(request.valorSelecionadoBrl)}</b></div>
    <div class="meta"><span>Nível exigido</span><b>${esc(request.nivelAprovacaoExigido || '—')}</b></div>
    <div class="meta"><span>Aprovador</span><b>${esc(request.nivelAprovador || '—')}</b></div>
    <div class="meta"><span>Criada em</span><b>${esc(dateTime(request.criadaEm))}</b></div>
  </div></aside></div>`;
}

function renderSobre() {
  content.innerHTML = '<div class="about-grid"><div class="pattern-card"><b>Singleton</b><p><code>MoneyRoundingPolicy</code> demonstra o Singleton clássico. Beans do Spring também usam escopo singleton por padrão.</p></div><div class="pattern-card"><b>Strategy</b><p>A seleção troca entre menor preço, menor prazo e custo-benefício sem alterar o fluxo principal.</p></div><div class="pattern-card"><b>Facade</b><p><code>CompraFacade</code> esconde services, repositories, câmbio, strategies e aprovação dos controllers.</p></div><div class="pattern-card"><b>Chain of Responsibility</b><p>A aprovação percorre Coordenador → Gerente → Diretor → Diretoria.</p></div></div><div class="section"><div class="section-head"><div><h2>Arquitetura desktop</h2><p>Electron gerencia o Spring Boot local.</p></div></div><div style="padding:18px"><div class="code">Electron Renderer\n  │ IPC seguro\n  ▼\nElectron Main\n  │ HTTP 127.0.0.1\n  ▼\nSpring Boot + JPA + H2 persistente</div><div class="action-row"><button class="btn ghost" id="openSwaggerButton">Abrir Swagger</button><button class="btn ghost" id="openLogsButton">Abrir log</button><button class="btn ghost" id="openDataButton">Pasta de dados</button></div></div></div>';
}

function quoteDialogOpen(id) {
  quoteDialog.querySelector('[name=solicitacaoId]').value = id;
  quoteDialog.querySelector('[name=fornecedorId]').innerHTML = state.fornecedores.map((fornecedor) => `<option value="${fornecedor.id}">${esc(fornecedor.razaoSocial)}</option>`).join('');
  const validity = new Date();
  validity.setDate(validity.getDate() + 15);
  quoteDialog.querySelector('[name=validade]').value = validity.toISOString().slice(0, 10);
  quoteDialog.showModal();
}

async function runAction(fn, message) {
  try {
    await fn();
    toast(message, '', 'success');
    await go('detalhe', { id: state.selectedRequestId });
  } catch (error) {
    toast('Operação não concluída', error.message, 'error');
  }
}

function ensureEditDialog() {
  if (document.getElementById('editRequestDialog')) return;
  const dialog = document.createElement('dialog');
  dialog.id = 'editRequestDialog';
  dialog.className = 'dialog wide edit-dialog';
  dialog.innerHTML = `<form method="dialog" id="editRequestForm">
    <div class="dialog-head"><div><h3>Editar solicitação</h3><p>Disponível enquanto o pedido estiver em rascunho, inclusive para registros criados em versões anteriores.</p></div><button class="icon-btn" value="cancel">×</button></div>
    <input type="hidden" name="solicitacaoId">
    <div class="form-grid two"><label>Solicitante<input name="solicitante" required maxlength="120"></label><label>Departamento<input name="departamento" required maxlength="100"></label></div>
    <label>Justificativa<textarea name="justificativa" rows="4" required maxlength="1000"></textarea></label>
    <label>Estratégia<select name="criterioAvaliacao"><option value="MENOR_PRECO">Menor preço</option><option value="MENOR_PRAZO">Menor prazo</option><option value="CUSTO_BENEFICIO">Custo-benefício</option></select></label>
    <div class="section-head"><div><h3>Itens</h3><p>Altere, remova ou adicione itens antes da cotação.</p></div><button type="button" class="btn ghost small" id="editAddItemButton">＋ Adicionar item</button></div>
    <div id="editItemsBox" class="edit-items"></div>
    <div class="dialog-actions"><button class="btn ghost" value="cancel">Cancelar</button><button class="btn primary" value="default">Salvar alterações</button></div>
  </form>`;
  document.body.appendChild(dialog);

  document.getElementById('editAddItemButton').addEventListener('click', () => addEditItem());
  document.getElementById('editItemsBox').addEventListener('click', (event) => {
    const button = event.target.closest('.remove-edit-item');
    if (!button) return;
    const box = document.getElementById('editItemsBox');
    if (box.children.length <= 1) {
      toast('A solicitação precisa ter pelo menos um item.', '', 'error');
      return;
    }
    button.closest('.edit-item').remove();
  });
  document.getElementById('editRequestForm').addEventListener('submit', submitEditRequest);
}

function addEditItem(item = {}) {
  const box = document.getElementById('editItemsBox');
  const row = document.createElement('div');
  row.className = 'edit-item';
  row.innerHTML = `<label>Descrição<input data-field="descricao" required maxlength="180" value="${esc(item.descricao || '')}"></label>
    <label>Qtd.<input data-field="quantidade" type="number" min="1" required value="${Number(item.quantidade || 1)}"></label>
    <label>Unidade<input data-field="unidade" required maxlength="20" value="${esc(item.unidade || 'UN')}"></label>
    <label>Especificação<input data-field="especificacao" maxlength="1000" value="${esc(item.especificacao || '')}"></label>
    <button type="button" class="icon-btn remove-edit-item">×</button>`;
  box.appendChild(row);
}

async function openEditRequest(id) {
  ensureEditDialog();
  const request = await api('GET', `/api/solicitacoes/${id}`);
  if (request.status !== 'RASCUNHO') throw new Error('Somente solicitações em rascunho podem ser editadas.');
  const form = document.getElementById('editRequestForm');
  form.elements.solicitacaoId.value = request.id;
  form.elements.solicitante.value = request.solicitante || '';
  form.elements.departamento.value = request.departamento || '';
  form.elements.justificativa.value = request.justificativa || '';
  form.elements.criterioAvaliacao.value = request.criterioAvaliacao || 'CUSTO_BENEFICIO';
  const box = document.getElementById('editItemsBox');
  box.innerHTML = '';
  (request.itens || []).forEach(addEditItem);
  if (!request.itens?.length) addEditItem();
  document.getElementById('editRequestDialog').showModal();
}

async function submitEditRequest(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const itens = [...document.querySelectorAll('#editItemsBox .edit-item')].map((row) => ({
    descricao: row.querySelector('[data-field=descricao]').value.trim(),
    quantidade: Number(row.querySelector('[data-field=quantidade]').value),
    unidade: row.querySelector('[data-field=unidade]').value.trim(),
    especificacao: row.querySelector('[data-field=especificacao]').value.trim() || null
  }));
  const id = Number(form.elements.solicitacaoId.value);
  try {
    await api('PUT', `/api/solicitacoes/${id}`, {
      solicitante: form.elements.solicitante.value.trim(),
      departamento: form.elements.departamento.value.trim(),
      justificativa: form.elements.justificativa.value.trim(),
      criterioAvaliacao: form.elements.criterioAvaliacao.value,
      itens
    });
    document.getElementById('editRequestDialog').close();
    toast('Solicitação atualizada', 'Alterações salvas com sucesso.', 'success');
    await go('detalhe', { id });
  } catch (error) {
    toast('Não foi possível editar', error.message, 'error');
  }
}

async function deleteRequest(id, code) {
  if (!window.confirm(`Excluir definitivamente a solicitação ${code}?\n\nEsta ação não pode ser desfeita.`)) return;
  try {
    await api('DELETE', `/api/solicitacoes/${id}`);
    state.lastEvaluation.delete(id);
    toast('Solicitação excluída', code, 'success');
    await go('solicitacoes');
  } catch (error) {
    toast('Não foi possível excluir', error.message, 'error');
  }
}

function reportHtml(request, quotes) {
  const selected = quotes.find((quote) => quote.id === request.cotacaoSelecionadaId);
  const itemRows = (request.itens || []).map((item) => `<tr><td>${esc(item.descricao)}</td><td>${item.quantidade}</td><td>${esc(item.unidade)}</td><td>${esc(item.especificacao || '—')}</td></tr>`).join('');
  const quoteRows = quotes.length
    ? quotes.map((quote) => `<tr class="${quote.id === request.cotacaoSelecionadaId ? 'selected' : ''}"><td>${esc(quote.fornecedor)}</td><td>${esc(quote.moeda)}</td><td>${esc(money(quote.totalOriginal, quote.moeda))}</td><td>${quote.prazoEntregaDias} dias</td><td>${quote.prazoPagamentoDias} dias</td></tr>`).join('')
    : '<tr><td colspan="5">Nenhuma cotação registrada.</td></tr>';
  return `<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><title>${esc(request.codigo)}</title><style>
    @page{size:A4;margin:14mm}*{box-sizing:border-box}body{font-family:Arial,sans-serif;color:#172033;font-size:12px;margin:0}h1{font-size:24px;margin:0}.brand{font-size:13px;color:#5b6475}.head{display:flex;justify-content:space-between;border-bottom:2px solid #172033;padding-bottom:14px;margin-bottom:18px}.badge{display:inline-block;border:1px solid #cfd5df;border-radius:999px;padding:4px 9px;font-weight:700}.grid{display:grid;grid-template-columns:1fr 1fr;gap:10px 28px;margin:14px 0}.field span{display:block;color:#6b7280;font-size:10px;text-transform:uppercase;margin-bottom:3px}.field b{font-size:12px}.box{border:1px solid #d9dee8;border-radius:8px;padding:12px;margin:14px 0}.box h2{font-size:14px;margin:0 0 9px}table{width:100%;border-collapse:collapse;margin-top:8px}th,td{text-align:left;border-bottom:1px solid #e5e7eb;padding:7px 6px;vertical-align:top}th{font-size:10px;text-transform:uppercase;color:#6b7280}.selected td{font-weight:700;background:#f3f6fb}.footer{margin-top:24px;padding-top:10px;border-top:1px solid #d9dee8;color:#6b7280;font-size:10px}.decision{border-left:4px solid #172033;padding-left:10px}.justification{white-space:pre-wrap;line-height:1.5}
  </style></head><body>
    <div class="head"><div><div class="brand">CompraFlow · Solicitação de Compra</div><h1>${esc(request.codigo)}</h1></div><div class="badge">${esc(statusLabel(request.status))}</div></div>
    <div class="grid"><div class="field"><span>Solicitante</span><b>${esc(request.solicitante)}</b></div><div class="field"><span>Departamento</span><b>${esc(request.departamento)}</b></div><div class="field"><span>Critério</span><b>${esc(criterioLabel(request.criterioAvaliacao))}</b></div><div class="field"><span>Criada em</span><b>${esc(dateTime(request.criadaEm))}</b></div></div>
    <div class="box"><h2>Justificativa</h2><div class="justification">${esc(request.justificativa)}</div></div>
    <div class="box"><h2>Itens solicitados</h2><table><thead><tr><th>Descrição</th><th>Qtd.</th><th>Unidade</th><th>Especificação</th></tr></thead><tbody>${itemRows}</tbody></table></div>
    <div class="box"><h2>Cotações</h2><table><thead><tr><th>Fornecedor</th><th>Moeda</th><th>Total</th><th>Entrega</th><th>Pagamento</th></tr></thead><tbody>${quoteRows}</tbody></table></div>
    <div class="box decision"><h2>Resultado / aprovação</h2><div class="grid"><div class="field"><span>Cotação selecionada</span><b>${esc(selected?.fornecedor || '—')}</b></div><div class="field"><span>Valor selecionado</span><b>${esc(money(request.valorSelecionadoBrl))}</b></div><div class="field"><span>Nível exigido</span><b>${esc(request.nivelAprovacaoExigido || '—')}</b></div><div class="field"><span>Nível aprovador</span><b>${esc(request.nivelAprovador || '—')}</b></div></div><div class="field"><span>Observação / decisão</span><b>${esc(request.observacaoDecisao || '—')}</b></div></div>
    <div class="footer">Documento gerado pelo CompraFlow em ${esc(new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date()))}.</div>
  </body></html>`;
}

async function documentData(id) {
  const [request, quotes] = await Promise.all([
    api('GET', `/api/solicitacoes/${id}`),
    api('GET', `/api/solicitacoes/${id}/cotacoes`)
  ]);
  return { request, html: reportHtml(request, quotes) };
}

async function printRequest(id) {
  try {
    const { html } = await documentData(id);
    const result = await window.compraflow.document.print(html);
    if (result.success) toast('Impressão enviada', 'Use a janela do Windows para escolher a impressora.', 'success');
    else if (result.failureReason && !/canceled/i.test(result.failureReason)) toast('Falha ao imprimir', result.failureReason, 'error');
  } catch (error) {
    toast('Falha ao imprimir', error.message, 'error');
  }
}

async function savePdf(id) {
  try {
    const { request, html } = await documentData(id);
    const result = await window.compraflow.document.savePdf(html, `CompraFlow-${request.codigo}`);
    if (result.saved) toast('PDF salvo', result.path, 'success');
  } catch (error) {
    toast('Falha ao gerar PDF', error.message, 'error');
  }
}

content.addEventListener('click', (event) => {
  const requestButton = event.target.closest('[data-request-id]');
  if (requestButton) return go('detalhe', { id: requestButton.dataset.requestId });
  const goButton = event.target.closest('[data-go]');
  if (goButton) return go(goButton.dataset.go);
  if (event.target.closest('#addItemButton')) return addItem();
  if (event.target.closest('.remove-item')) {
    const row = event.target.closest('.item-row');
    const box = document.getElementById('itemsBox');
    if (box.children.length > 1) row.remove();
    else toast('A solicitação precisa ter ao menos um item.', '', 'error');
    return;
  }
  if (event.target.closest('#newSupplierButton')) return supplierDialog.showModal();
  if (event.target.closest('#addQuoteButton')) return quoteDialogOpen(state.selectedRequestId);
  if (event.target.closest('#editRequestButton')) return openEditRequest(state.selectedRequestId).catch((error) => toast('Não foi possível editar', error.message, 'error'));
  if (event.target.closest('#deleteRequestButton')) {
    const current = state.solicitacoes.find((item) => item.id === state.selectedRequestId);
    return deleteRequest(state.selectedRequestId, current?.codigo || `#${state.selectedRequestId}`);
  }
  if (event.target.closest('#printRequestButton')) return printRequest(state.selectedRequestId);
  if (event.target.closest('#savePdfButton')) return savePdf(state.selectedRequestId);
  if (event.target.closest('#openQuotePhaseButton')) return runAction(() => api('POST', `/api/solicitacoes/${state.selectedRequestId}/abrir-cotacao`), 'Solicitação aberta para cotação.');
  if (event.target.closest('#evaluateButton')) return runAction(async () => {
    const evaluation = await api('POST', `/api/solicitacoes/${state.selectedRequestId}/avaliar`);
    state.lastEvaluation.set(state.selectedRequestId, evaluation);
  }, 'Cotações avaliadas e proposta selecionada.');
  if (event.target.closest('#approveButton')) {
    approvalDialog.querySelector('[name=solicitacaoId]').value = state.selectedRequestId;
    return approvalDialog.showModal();
  }
  if (event.target.closest('#rejectButton')) {
    rejectDialog.querySelector('[name=solicitacaoId]').value = state.selectedRequestId;
    return rejectDialog.showModal();
  }
  if (event.target.closest('#openSwaggerButton')) return window.compraflow.backend.openSwagger();
  if (event.target.closest('#openLogsButton')) return window.compraflow.backend.openLogs();
  if (event.target.closest('#openDataButton')) return window.compraflow.app.showDataFolder();
});

content.addEventListener('submit', async (event) => {
  if (event.target.id !== 'requestForm') return;
  event.preventDefault();
  const form = new FormData(event.target);
  const itens = [...document.querySelectorAll('.item-row')].map((row) => ({
    descricao: row.querySelector('[data-field=descricao]').value.trim(),
    quantidade: Number(row.querySelector('[data-field=quantidade]').value),
    unidade: row.querySelector('[data-field=unidade]').value.trim(),
    especificacao: row.querySelector('[data-field=especificacao]').value.trim() || null
  }));
  try {
    const created = await api('POST', '/api/solicitacoes', {
      solicitante: form.get('solicitante').trim(),
      departamento: form.get('departamento').trim(),
      justificativa: form.get('justificativa').trim(),
      criterioAvaliacao: form.get('criterioAvaliacao'),
      itens
    });
    toast('Solicitação criada', created.codigo, 'success');
    go('detalhe', { id: created.id });
  } catch (error) {
    toast('Não foi possível criar', error.message, 'error');
  }
});

document.getElementById('supplierForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = new FormData(event.target);
  try {
    await api('POST', '/api/fornecedores', {
      razaoSocial: form.get('razaoSocial').trim(),
      cnpj: form.get('cnpj').trim(),
      email: form.get('email').trim()
    });
    supplierDialog.close();
    event.target.reset();
    toast('Fornecedor cadastrado', '', 'success');
    go('fornecedores');
  } catch (error) {
    toast('Não foi possível cadastrar', error.message, 'error');
  }
});

document.getElementById('quoteForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = new FormData(event.target);
  const id = Number(form.get('solicitacaoId'));
  try {
    await api('POST', `/api/solicitacoes/${id}/cotacoes`, {
      fornecedorId: Number(form.get('fornecedorId')),
      valorProdutos: Number(form.get('valorProdutos')),
      frete: Number(form.get('frete')),
      moeda: form.get('moeda'),
      prazoEntregaDias: Number(form.get('prazoEntregaDias')),
      prazoPagamentoDias: Number(form.get('prazoPagamentoDias')),
      validade: form.get('validade')
    });
    quoteDialog.close();
    event.target.reset();
    toast('Cotação registrada', '', 'success');
    go('detalhe', { id });
  } catch (error) {
    toast('Não foi possível registrar', error.message, 'error');
  }
});

document.getElementById('approvalForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = new FormData(event.target);
  const id = Number(form.get('solicitacaoId'));
  try {
    await api('POST', `/api/solicitacoes/${id}/aprovar`, {
      nivelAprovador: form.get('nivelAprovador'),
      observacao: form.get('observacao').trim() || null
    });
    approvalDialog.close();
    event.target.reset();
    toast('Solicitação aprovada', '', 'success');
    go('detalhe', { id });
  } catch (error) {
    toast('Aprovação não concluída', error.message, 'error');
  }
});

document.getElementById('rejectForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = new FormData(event.target);
  const id = Number(form.get('solicitacaoId'));
  try {
    await api('POST', `/api/solicitacoes/${id}/rejeitar`, { motivo: form.get('motivo').trim() });
    rejectDialog.close();
    event.target.reset();
    toast('Solicitação rejeitada', '', 'success');
    go('detalhe', { id });
  } catch (error) {
    toast('Rejeição não concluída', error.message, 'error');
  }
});

document.addEventListener('click', (event) => {
  const nav = event.target.closest('[data-view]');
  if (nav) go(nav.dataset.view);
  const outsideGo = event.target.closest('[data-go]');
  if (outsideGo && !content.contains(outsideGo)) go(outsideGo.dataset.go);
});

document.getElementById('refreshButton').addEventListener('click', () => go(state.view, { id: state.selectedRequestId }));
window.compraflow.backend.onStopped(() => {
  document.getElementById('backendDot').classList.add('off');
  document.getElementById('backendLabel').textContent = 'Backend interrompido';
  toast('Backend local foi encerrado', 'Reabra o CompraFlow para reiniciar.', 'error');
});

ensureEditDialog();

(async () => {
  const status = await window.compraflow.backend.status();
  document.getElementById('appVersion').textContent = `CompraFlow Desktop v${status.version}`;
  document.getElementById('backendLabel').textContent = status.running ? 'Backend local ativo' : 'Backend indisponível';
  document.getElementById('backendDot').classList.toggle('off', !status.running);
  await go('dashboard');
})();
