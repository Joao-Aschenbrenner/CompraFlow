(() => {
  const originalRenderDetalhe = window.renderDetalhe;
  if (typeof originalRenderDetalhe !== 'function') return;

  async function request(method, endpoint, body) {
    const response = await window.compraflow.api.request(method, endpoint, body);
    if (!response.ok) {
      const detail = response?.data?.detail || response?.data?.message || response?.data?.title || `Erro HTTP ${response.status}`;
      throw new Error(detail);
    }
    return response.data;
  }

  const escapeHtml = (value) => String(value ?? '')
    .replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;').replaceAll("'", '&#039;');

  function ensureEditDialog() {
    if (document.getElementById('editRequestDialog')) return;
    const dialog = document.createElement('dialog');
    dialog.id = 'editRequestDialog';
    dialog.className = 'dialog wide edit-dialog';
    dialog.innerHTML = `
      <form method="dialog" id="editRequestForm">
        <div class="dialog-head"><div><h3>Editar solicitação</h3><p>Disponível enquanto o pedido estiver em rascunho.</p></div><button class="icon-btn" value="cancel">×</button></div>
        <input type="hidden" name="solicitacaoId" />
        <div class="form-grid two">
          <label>Solicitante<input name="solicitante" required maxlength="120"></label>
          <label>Departamento<input name="departamento" required maxlength="100"></label>
        </div>
        <label>Justificativa<textarea name="justificativa" rows="4" required maxlength="1000"></textarea></label>
        <label>Estratégia<select name="criterioAvaliacao"><option value="MENOR_PRECO">Menor preço</option><option value="MENOR_PRAZO">Menor prazo</option><option value="CUSTO_BENEFICIO">Custo-benefício</option></select></label>
        <div class="section-head"><div><h3>Itens</h3><p>Altere, remova ou adicione itens antes da cotação.</p></div><button type="button" class="btn ghost small" id="editAddItemButton">＋ Adicionar item</button></div>
        <div id="editItemsBox" class="edit-items"></div>
        <div class="dialog-actions"><button class="btn ghost" value="cancel">Cancelar</button><button class="btn primary" id="saveEditRequestButton" value="default">Salvar alterações</button></div>
      </form>`;
    document.body.appendChild(dialog);

    document.getElementById('editAddItemButton').addEventListener('click', () => addEditItem());
    document.getElementById('editItemsBox').addEventListener('click', (event) => {
      const button = event.target.closest('.remove-edit-item');
      if (!button) return;
      const box = document.getElementById('editItemsBox');
      if (box.children.length <= 1) return window.toast?.('A solicitação precisa ter pelo menos um item.');
      button.closest('.edit-item').remove();
    });
    document.getElementById('editRequestForm').addEventListener('submit', submitEdit);
  }

  function addEditItem(item = {}) {
    const row = document.createElement('div');
    row.className = 'edit-item';
    row.innerHTML = `
      <label>Descrição<input data-field="descricao" required maxlength="180" value="${escapeHtml(item.descricao || '')}"></label>
      <label>Qtd.<input data-field="quantidade" type="number" min="1" required value="${Number(item.quantidade || 1)}"></label>
      <label>Unidade<input data-field="unidade" required maxlength="20" value="${escapeHtml(item.unidade || 'UN')}"></label>
      <label>Especificação<input data-field="especificacao" maxlength="1000" value="${escapeHtml(item.especificacao || '')}"></label>
      <button type="button" class="icon-btn remove-edit-item">×</button>`;
    document.getElementById('editItemsBox').appendChild(row);
  }

  async function openEdit(id) {
    ensureEditDialog();
    const r = await request('GET', `/api/solicitacoes/${id}`);
    if (r.status !== 'RASCUNHO') throw new Error('Somente solicitações em rascunho podem ser editadas.');
    const form = document.getElementById('editRequestForm');
    form.solicitacaoId.value = r.id;
    form.solicitante.value = r.solicitante;
    form.departamento.value = r.departamento;
    form.justificativa.value = r.justificativa;
    form.criterioAvaliacao.value = r.criterioAvaliacao;
    const box = document.getElementById('editItemsBox');
    box.innerHTML = '';
    (r.itens || []).forEach(addEditItem);
    if (!r.itens?.length) addEditItem();
    document.getElementById('editRequestDialog').showModal();
  }

  async function submitEdit(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const itens = [...document.querySelectorAll('#editItemsBox .edit-item')].map((row) => ({
      descricao: row.querySelector('[data-field=descricao]').value.trim(),
      quantidade: Number(row.querySelector('[data-field=quantidade]').value),
      unidade: row.querySelector('[data-field=unidade]').value.trim(),
      especificacao: row.querySelector('[data-field=especificacao]').value.trim() || null
    }));
    const body = {
      solicitante: form.solicitante.value.trim(),
      departamento: form.departamento.value.trim(),
      justificativa: form.justificativa.value.trim(),
      criterioAvaliacao: form.criterioAvaliacao.value,
      itens
    };
    try {
      await request('PUT', `/api/solicitacoes/${form.solicitacaoId.value}`, body);
      document.getElementById('editRequestDialog').close();
      window.toast?.('Solicitação atualizada', 'Alterações salvas com sucesso.', 'success');
      await window.go('detalhe', { id: Number(form.solicitacaoId.value) });
    } catch (error) {
      window.toast?.('Não foi possível editar', error.message, 'error');
    }
  }

  async function deleteRequest(id, code) {
    if (!confirm(`Excluir definitivamente a solicitação ${code}?\n\nEsta ação não pode ser desfeita.`)) return;
    try {
      await request('DELETE', `/api/solicitacoes/${id}`);
      window.toast?.('Solicitação excluída', code, 'success');
      await window.go('solicitacoes');
    } catch (error) {
      window.toast?.('Não foi possível excluir', error.message, 'error');
    }
  }

  function money(value, currency = 'BRL') {
    if (value === null || value === undefined) return '—';
    try { return new Intl.NumberFormat('pt-BR', { style: 'currency', currency }).format(Number(value)); }
    catch (_) { return `${value} ${currency}`; }
  }

  function formatDateTime(value) {
    if (!value) return '—';
    return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
  }

  function reportHtml(r, quotes) {
    const selected = quotes.find((q) => q.id === r.cotacaoSelecionadaId);
    const itemRows = (r.itens || []).map((i) => `<tr><td>${escapeHtml(i.descricao)}</td><td>${i.quantidade}</td><td>${escapeHtml(i.unidade)}</td><td>${escapeHtml(i.especificacao || '—')}</td></tr>`).join('');
    const quoteRows = quotes.length ? quotes.map((q) => `<tr class="${q.id === r.cotacaoSelecionadaId ? 'selected' : ''}"><td>${escapeHtml(q.fornecedor)}</td><td>${escapeHtml(q.moeda)}</td><td>${escapeHtml(money(q.totalOriginal, q.moeda))}</td><td>${q.prazoEntregaDias} dias</td><td>${q.prazoPagamentoDias} dias</td></tr>`).join('') : '<tr><td colspan="5">Nenhuma cotação registrada.</td></tr>';
    return `<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><title>${escapeHtml(r.codigo)}</title><style>
      @page{size:A4;margin:14mm}*{box-sizing:border-box}body{font-family:Arial,sans-serif;color:#172033;font-size:12px;margin:0}h1{font-size:24px;margin:0}.brand{font-size:13px;color:#5b6475}.head{display:flex;justify-content:space-between;border-bottom:2px solid #172033;padding-bottom:14px;margin-bottom:18px}.badge{display:inline-block;border:1px solid #cfd5df;border-radius:999px;padding:4px 9px;font-weight:700}.grid{display:grid;grid-template-columns:1fr 1fr;gap:10px 28px;margin:14px 0}.field span{display:block;color:#6b7280;font-size:10px;text-transform:uppercase;margin-bottom:3px}.field b{font-size:12px}.box{border:1px solid #d9dee8;border-radius:8px;padding:12px;margin:14px 0}.box h2{font-size:14px;margin:0 0 9px}table{width:100%;border-collapse:collapse;margin-top:8px}th,td{text-align:left;border-bottom:1px solid #e5e7eb;padding:7px 6px;vertical-align:top}th{font-size:10px;text-transform:uppercase;color:#6b7280}.selected td{font-weight:700;background:#f3f6fb}.footer{margin-top:24px;padding-top:10px;border-top:1px solid #d9dee8;color:#6b7280;font-size:10px}.decision{border-left:4px solid #172033;padding-left:10px}.justification{white-space:pre-wrap;line-height:1.5}
    </style></head><body><div class="head"><div><div class="brand">CompraFlow · Solicitação de Compra</div><h1>${escapeHtml(r.codigo)}</h1></div><div class="badge">${escapeHtml(r.status)}</div></div>
      <div class="grid"><div class="field"><span>Solicitante</span><b>${escapeHtml(r.solicitante)}</b></div><div class="field"><span>Departamento</span><b>${escapeHtml(r.departamento)}</b></div><div class="field"><span>Critério</span><b>${escapeHtml(r.criterioAvaliacao)}</b></div><div class="field"><span>Criada em</span><b>${escapeHtml(formatDateTime(r.criadaEm))}</b></div></div>
      <div class="box"><h2>Justificativa</h2><div class="justification">${escapeHtml(r.justificativa)}</div></div>
      <div class="box"><h2>Itens solicitados</h2><table><thead><tr><th>Descrição</th><th>Qtd.</th><th>Unidade</th><th>Especificação</th></tr></thead><tbody>${itemRows}</tbody></table></div>
      <div class="box"><h2>Cotações</h2><table><thead><tr><th>Fornecedor</th><th>Moeda</th><th>Total</th><th>Entrega</th><th>Pagamento</th></tr></thead><tbody>${quoteRows}</tbody></table></div>
      <div class="box decision"><h2>Resultado / aprovação</h2><div class="grid"><div class="field"><span>Cotação selecionada</span><b>${escapeHtml(selected?.fornecedor || '—')}</b></div><div class="field"><span>Valor selecionado</span><b>${escapeHtml(money(r.valorSelecionadoBrl))}</b></div><div class="field"><span>Nível exigido</span><b>${escapeHtml(r.nivelAprovacaoExigido || '—')}</b></div><div class="field"><span>Nível aprovador</span><b>${escapeHtml(r.nivelAprovador || '—')}</b></div></div><div class="field"><span>Observação / decisão</span><b>${escapeHtml(r.observacaoDecisao || '—')}</b></div></div>
      <div class="footer">Documento gerado pelo CompraFlow em ${escapeHtml(new Intl.DateTimeFormat('pt-BR',{dateStyle:'short',timeStyle:'short'}).format(new Date()))}.</div></body></html>`;
  }

  async function documentData(id) {
    const [r, quotes] = await Promise.all([
      request('GET', `/api/solicitacoes/${id}`),
      request('GET', `/api/solicitacoes/${id}/cotacoes`)
    ]);
    return { r, html: reportHtml(r, quotes) };
  }

  async function printRequest(id) {
    try {
      const { html } = await documentData(id);
      const result = await window.compraflow.document.print(html);
      if (result.success) window.toast?.('Impressão enviada', 'Use a janela do Windows para escolher a impressora.', 'success');
      else if (result.failureReason && !/canceled/i.test(result.failureReason)) window.toast?.('Falha ao imprimir', result.failureReason, 'error');
    } catch (error) { window.toast?.('Falha ao imprimir', error.message, 'error'); }
  }

  async function savePdf(id) {
    try {
      const { r, html } = await documentData(id);
      const result = await window.compraflow.document.savePdf(html, `CompraFlow-${r.codigo}`);
      if (result.saved) window.toast?.('PDF salvo', result.path, 'success');
    } catch (error) { window.toast?.('Falha ao gerar PDF', error.message, 'error'); }
  }

  async function enhanceDetail(id) {
    const r = await request('GET', `/api/solicitacoes/${id}`);
    const card = document.querySelector('.detail-card');
    if (!card) return;
    let actions = card.querySelector('.document-actions');
    if (!actions) {
      actions = document.createElement('div');
      actions.className = 'document-actions';
      const editDelete = r.status === 'RASCUNHO' ? `<button class="btn ghost" id="editRequestButton">✎ Editar</button><button class="btn danger" id="deleteRequestButton">Excluir</button>` : '';
      actions.innerHTML = `${editDelete}<button class="btn ghost" id="printRequestButton">Imprimir</button><button class="btn ghost" id="savePdfButton">Salvar PDF</button>`;
      const actionRow = card.querySelector('.action-row');
      if (actionRow) actionRow.insertAdjacentElement('afterend', actions); else card.appendChild(actions);
    }
    document.getElementById('editRequestButton')?.addEventListener('click', () => openEdit(id).catch((e) => window.toast?.('Não foi possível editar', e.message, 'error')));
    document.getElementById('deleteRequestButton')?.addEventListener('click', () => deleteRequest(id, r.codigo));
    document.getElementById('printRequestButton')?.addEventListener('click', () => printRequest(id));
    document.getElementById('savePdfButton')?.addEventListener('click', () => savePdf(id));
  }

  window.renderDetalhe = async function(id) {
    await originalRenderDetalhe(id);
    await enhanceDetail(id);
  };

  ensureEditDialog();
})();
