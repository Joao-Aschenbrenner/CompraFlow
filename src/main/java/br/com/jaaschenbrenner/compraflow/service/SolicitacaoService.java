package br.com.jaaschenbrenner.compraflow.service;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.jaaschenbrenner.compraflow.api.dto.CriarSolicitacaoRequest;
import br.com.jaaschenbrenner.compraflow.api.dto.ItemSolicitacaoRequest;
import br.com.jaaschenbrenner.compraflow.domain.ItemSolicitacao;
import br.com.jaaschenbrenner.compraflow.domain.NivelAprovacao;
import br.com.jaaschenbrenner.compraflow.domain.SolicitacaoCompra;
import br.com.jaaschenbrenner.compraflow.exception.RecursoNaoEncontradoException;
import br.com.jaaschenbrenner.compraflow.repository.SolicitacaoCompraRepository;

@Service
public class SolicitacaoService {

    private final SolicitacaoCompraRepository solicitacaoRepository;

    public SolicitacaoService(SolicitacaoCompraRepository solicitacaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Transactional
    public SolicitacaoCompra criar(CriarSolicitacaoRequest request) {
        SolicitacaoCompra solicitacao = new SolicitacaoCompra(
                novoCodigo(),
                request.solicitante().trim(),
                request.departamento().trim(),
                request.justificativa().trim(),
                request.criterioAvaliacao());

        itensDoRequest(request).forEach(solicitacao::adicionarItem);
        return solicitacaoRepository.save(solicitacao);
    }

    @Transactional
    public SolicitacaoCompra atualizar(Long id, CriarSolicitacaoRequest request) {
        SolicitacaoCompra solicitacao = buscar(id);
        solicitacao.atualizarRascunho(
                request.solicitante().trim(),
                request.departamento().trim(),
                request.justificativa().trim(),
                request.criterioAvaliacao(),
                itensDoRequest(request));
        return solicitacaoRepository.save(solicitacao);
    }

    @Transactional
    public void excluir(Long id) {
        SolicitacaoCompra solicitacao = buscar(id);
        solicitacao.validarExclusao();
        solicitacaoRepository.delete(solicitacao);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoCompra> listar() {
        return solicitacaoRepository.findAllByOrderByCriadaEmDesc();
    }

    @Transactional(readOnly = true)
    public SolicitacaoCompra buscar(Long id) {
        return solicitacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação não encontrada: " + id));
    }

    @Transactional
    public SolicitacaoCompra abrirCotacao(Long id) {
        SolicitacaoCompra solicitacao = buscar(id);
        solicitacao.abrirCotacao();
        return solicitacaoRepository.save(solicitacao);
    }

    @Transactional
    public SolicitacaoCompra aprovar(Long id, NivelAprovacao nivelAprovador, String observacao) {
        SolicitacaoCompra solicitacao = buscar(id);
        solicitacao.aprovar(nivelAprovador, observacao);
        return solicitacaoRepository.save(solicitacao);
    }

    @Transactional
    public SolicitacaoCompra rejeitar(Long id, String motivo) {
        SolicitacaoCompra solicitacao = buscar(id);
        solicitacao.rejeitar(motivo);
        return solicitacaoRepository.save(solicitacao);
    }

    @Transactional
    public SolicitacaoCompra salvar(SolicitacaoCompra solicitacao) {
        return solicitacaoRepository.save(solicitacao);
    }

    private List<ItemSolicitacao> itensDoRequest(CriarSolicitacaoRequest request) {
        List<ItemSolicitacao> itens = new ArrayList<>();
        for (ItemSolicitacaoRequest item : request.itens()) {
            itens.add(new ItemSolicitacao(
                    item.descricao().trim(),
                    item.quantidade(),
                    item.unidade().trim().toUpperCase(Locale.ROOT),
                    item.especificacao()));
        }
        return itens;
    }

    private String novoCodigo() {
        return "SC-" + Year.now().getValue() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
