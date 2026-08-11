package br.com.jaaschenbrenner.compraflow.patterns.facade;

import java.util.List;

import org.springframework.stereotype.Component;

import br.com.jaaschenbrenner.compraflow.api.CompraMapper;
import br.com.jaaschenbrenner.compraflow.api.dto.AprovacaoRequest;
import br.com.jaaschenbrenner.compraflow.api.dto.AvaliacaoCotacaoResponse;
import br.com.jaaschenbrenner.compraflow.api.dto.CotacaoRequest;
import br.com.jaaschenbrenner.compraflow.api.dto.CotacaoResponse;
import br.com.jaaschenbrenner.compraflow.api.dto.CriarSolicitacaoRequest;
import br.com.jaaschenbrenner.compraflow.api.dto.FornecedorRequest;
import br.com.jaaschenbrenner.compraflow.api.dto.FornecedorResponse;
import br.com.jaaschenbrenner.compraflow.api.dto.SolicitacaoResponse;
import br.com.jaaschenbrenner.compraflow.service.AvaliacaoCotacaoService;
import br.com.jaaschenbrenner.compraflow.service.CotacaoService;
import br.com.jaaschenbrenner.compraflow.service.FornecedorService;
import br.com.jaaschenbrenner.compraflow.service.SolicitacaoService;

/**
 * Facade do domínio de compras. Controllers conversam com uma interface simples,
 * enquanto esta classe coordena serviços, persistência, Strategy, aprovação e integrações.
 */
@Component
public class CompraFacade {

    private final FornecedorService fornecedorService;
    private final SolicitacaoService solicitacaoService;
    private final CotacaoService cotacaoService;
    private final AvaliacaoCotacaoService avaliacaoService;
    private final CompraMapper mapper;

    public CompraFacade(FornecedorService fornecedorService,
                        SolicitacaoService solicitacaoService,
                        CotacaoService cotacaoService,
                        AvaliacaoCotacaoService avaliacaoService,
                        CompraMapper mapper) {
        this.fornecedorService = fornecedorService;
        this.solicitacaoService = solicitacaoService;
        this.cotacaoService = cotacaoService;
        this.avaliacaoService = avaliacaoService;
        this.mapper = mapper;
    }

    public FornecedorResponse criarFornecedor(FornecedorRequest request) {
        return mapper.toResponse(fornecedorService.criar(
                request.razaoSocial().trim(), request.cnpj(), request.email().trim()));
    }

    public List<FornecedorResponse> listarFornecedores() {
        return fornecedorService.listarAtivos().stream().map(mapper::toResponse).toList();
    }

    public SolicitacaoResponse criarSolicitacao(CriarSolicitacaoRequest request) {
        return mapper.toResponse(solicitacaoService.criar(request));
    }

    public SolicitacaoResponse atualizarSolicitacao(Long id, CriarSolicitacaoRequest request) {
        return mapper.toResponse(solicitacaoService.atualizar(id, request));
    }

    public void excluirSolicitacao(Long id) {
        solicitacaoService.excluir(id);
    }

    public List<SolicitacaoResponse> listarSolicitacoes() {
        return solicitacaoService.listar().stream().map(mapper::toResponse).toList();
    }

    public SolicitacaoResponse buscarSolicitacao(Long id) {
        return mapper.toResponse(solicitacaoService.buscar(id));
    }

    public SolicitacaoResponse abrirCotacao(Long id) {
        return mapper.toResponse(solicitacaoService.abrirCotacao(id));
    }

    public CotacaoResponse registrarCotacao(Long solicitacaoId, CotacaoRequest request) {
        return mapper.toResponse(cotacaoService.registrar(solicitacaoId, request));
    }

    public List<CotacaoResponse> listarCotacoes(Long solicitacaoId) {
        return cotacaoService.listarDaSolicitacao(solicitacaoId).stream().map(mapper::toResponse).toList();
    }

    public AvaliacaoCotacaoResponse avaliarCotacoes(Long solicitacaoId) {
        return mapper.toResponse(avaliacaoService.avaliar(solicitacaoId));
    }

    public SolicitacaoResponse aprovar(Long solicitacaoId, AprovacaoRequest request) {
        return mapper.toResponse(solicitacaoService.aprovar(solicitacaoId, request.nivelAprovador(), request.observacao()));
    }

    public SolicitacaoResponse rejeitar(Long solicitacaoId, String motivo) {
        return mapper.toResponse(solicitacaoService.rejeitar(solicitacaoId, motivo));
    }
}
