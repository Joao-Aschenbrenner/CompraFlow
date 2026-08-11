package br.com.jaaschenbrenner.compraflow.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.jaaschenbrenner.compraflow.api.dto.CotacaoRequest;
import br.com.jaaschenbrenner.compraflow.domain.Cotacao;
import br.com.jaaschenbrenner.compraflow.domain.Fornecedor;
import br.com.jaaschenbrenner.compraflow.domain.SolicitacaoCompra;
import br.com.jaaschenbrenner.compraflow.domain.StatusSolicitacao;
import br.com.jaaschenbrenner.compraflow.exception.RegraNegocioException;
import br.com.jaaschenbrenner.compraflow.repository.CotacaoRepository;

@Service
public class CotacaoService {

    private final CotacaoRepository cotacaoRepository;
    private final SolicitacaoService solicitacaoService;
    private final FornecedorService fornecedorService;

    public CotacaoService(CotacaoRepository cotacaoRepository,
                          SolicitacaoService solicitacaoService,
                          FornecedorService fornecedorService) {
        this.cotacaoRepository = cotacaoRepository;
        this.solicitacaoService = solicitacaoService;
        this.fornecedorService = fornecedorService;
    }

    @Transactional
    public Cotacao registrar(Long solicitacaoId, CotacaoRequest request) {
        SolicitacaoCompra solicitacao = solicitacaoService.buscar(solicitacaoId);
        if (solicitacao.getStatus() != StatusSolicitacao.EM_COTACAO) {
            throw new RegraNegocioException("Cotações só podem ser registradas quando a solicitação está EM_COTACAO.");
        }
        if (request.validade().isBefore(LocalDate.now())) {
            throw new RegraNegocioException("A cotação está vencida.");
        }
        if (cotacaoRepository.existsBySolicitacaoIdAndFornecedorId(solicitacaoId, request.fornecedorId())) {
            throw new RegraNegocioException("Este fornecedor já enviou cotação para a solicitação.");
        }

        Fornecedor fornecedor = fornecedorService.buscarAtivo(request.fornecedorId());
        Cotacao cotacao = new Cotacao(
                solicitacao,
                fornecedor,
                request.valorProdutos(),
                request.frete(),
                request.moeda(),
                request.prazoEntregaDias(),
                request.prazoPagamentoDias(),
                request.validade());
        return cotacaoRepository.save(cotacao);
    }

    @Transactional(readOnly = true)
    public List<Cotacao> listarDaSolicitacao(Long solicitacaoId) {
        solicitacaoService.buscar(solicitacaoId);
        return cotacaoRepository.findAllBySolicitacaoIdOrderByIdAsc(solicitacaoId);
    }

    @Transactional
    public void salvarTodas(List<Cotacao> cotacoes) {
        cotacaoRepository.saveAll(cotacoes);
    }
}
