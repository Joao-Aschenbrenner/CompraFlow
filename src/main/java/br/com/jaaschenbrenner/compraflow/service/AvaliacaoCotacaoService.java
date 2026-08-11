package br.com.jaaschenbrenner.compraflow.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.jaaschenbrenner.compraflow.config.PoliticasCompraProperties;
import br.com.jaaschenbrenner.compraflow.domain.Cotacao;
import br.com.jaaschenbrenner.compraflow.domain.NivelAprovacao;
import br.com.jaaschenbrenner.compraflow.domain.SolicitacaoCompra;
import br.com.jaaschenbrenner.compraflow.domain.StatusSolicitacao;
import br.com.jaaschenbrenner.compraflow.exception.RegraNegocioException;
import br.com.jaaschenbrenner.compraflow.patterns.chain.ApprovalChain;
import br.com.jaaschenbrenner.compraflow.patterns.strategy.DecisaoCotacao;
import br.com.jaaschenbrenner.compraflow.patterns.strategy.OfertaCotacao;
import br.com.jaaschenbrenner.compraflow.patterns.strategy.SelecionarCotacaoStrategy;
import br.com.jaaschenbrenner.compraflow.patterns.strategy.CotacaoStrategyResolver;
import br.com.jaaschenbrenner.compraflow.service.model.ResultadoAvaliacaoCotacao;

@Service
public class AvaliacaoCotacaoService {

    private final SolicitacaoService solicitacaoService;
    private final CotacaoService cotacaoService;
    private final CambioService cambioService;
    private final CotacaoStrategyResolver strategyResolver;
    private final ApprovalChain approvalChain;
    private final PoliticasCompraProperties properties;

    public AvaliacaoCotacaoService(SolicitacaoService solicitacaoService,
                                   CotacaoService cotacaoService,
                                   CambioService cambioService,
                                   CotacaoStrategyResolver strategyResolver,
                                   ApprovalChain approvalChain,
                                   PoliticasCompraProperties properties) {
        this.solicitacaoService = solicitacaoService;
        this.cotacaoService = cotacaoService;
        this.cambioService = cambioService;
        this.strategyResolver = strategyResolver;
        this.approvalChain = approvalChain;
        this.properties = properties;
    }

    @Transactional
    public ResultadoAvaliacaoCotacao avaliar(Long solicitacaoId) {
        SolicitacaoCompra solicitacao = solicitacaoService.buscar(solicitacaoId);
        if (solicitacao.getStatus() != StatusSolicitacao.EM_COTACAO) {
            throw new RegraNegocioException("A solicitação precisa estar EM_COTACAO para ser avaliada.");
        }

        List<Cotacao> cotacoes = cotacaoService.listarDaSolicitacao(solicitacaoId).stream()
                .filter(c -> !c.getValidade().isBefore(LocalDate.now()))
                .toList();

        if (cotacoes.size() < properties.getMinQuotes()) {
            throw new RegraNegocioException(
                    "São necessárias pelo menos " + properties.getMinQuotes() + " cotações válidas para avaliar a compra.");
        }

        List<OfertaCotacao> ofertas = new ArrayList<>();
        for (Cotacao cotacao : cotacoes) {
            ofertas.add(new OfertaCotacao(
                    cotacao.getId(),
                    cotacao.getFornecedor().getRazaoSocial(),
                    cambioService.converterParaBrl(cotacao.totalOriginal(), cotacao.getMoeda()),
                    cotacao.getPrazoEntregaDias(),
                    cotacao.getPrazoPagamentoDias()));
        }

        SelecionarCotacaoStrategy strategy = strategyResolver.resolver(solicitacao.getCriterioAvaliacao());
        DecisaoCotacao decisao = strategy.selecionar(ofertas);

        Cotacao vencedora = cotacoes.stream()
                .filter(c -> c.getId().equals(decisao.vencedora().cotacaoId()))
                .findFirst()
                .orElseThrow(() -> new RegraNegocioException("Cotação vencedora não encontrada."));

        cotacoes.forEach(c -> {
            if (c.getId().equals(vencedora.getId())) {
                c.selecionar();
            } else {
                c.descartar();
            }
        });
        cotacaoService.salvarTodas(cotacoes);

        NivelAprovacao nivel = approvalChain.resolver(decisao.vencedora().totalEmBrl());
        solicitacao.aguardarAprovacao(vencedora.getId(), decisao.vencedora().totalEmBrl(), nivel);
        solicitacaoService.salvar(solicitacao);

        return new ResultadoAvaliacaoCotacao(
                vencedora,
                decisao.vencedora().totalEmBrl(),
                solicitacao.getCriterioAvaliacao(),
                nivel,
                decisao.justificativa());
    }
}
