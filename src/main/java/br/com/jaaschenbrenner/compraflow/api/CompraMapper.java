package br.com.jaaschenbrenner.compraflow.api;

import java.util.List;

import org.springframework.stereotype.Component;

import br.com.jaaschenbrenner.compraflow.api.dto.AvaliacaoCotacaoResponse;
import br.com.jaaschenbrenner.compraflow.api.dto.CotacaoResponse;
import br.com.jaaschenbrenner.compraflow.api.dto.FornecedorResponse;
import br.com.jaaschenbrenner.compraflow.api.dto.ItemSolicitacaoResponse;
import br.com.jaaschenbrenner.compraflow.api.dto.SolicitacaoResponse;
import br.com.jaaschenbrenner.compraflow.domain.Cotacao;
import br.com.jaaschenbrenner.compraflow.domain.Fornecedor;
import br.com.jaaschenbrenner.compraflow.domain.SolicitacaoCompra;
import br.com.jaaschenbrenner.compraflow.service.model.ResultadoAvaliacaoCotacao;

@Component
public class CompraMapper {

    public FornecedorResponse toResponse(Fornecedor fornecedor) {
        return new FornecedorResponse(
                fornecedor.getId(), fornecedor.getRazaoSocial(), fornecedor.getCnpj(),
                fornecedor.getEmail(), fornecedor.isAtivo());
    }

    public SolicitacaoResponse toResponse(SolicitacaoCompra solicitacao) {
        List<ItemSolicitacaoResponse> itens = solicitacao.getItens().stream()
                .map(item -> new ItemSolicitacaoResponse(
                        item.getId(), item.getDescricao(), item.getQuantidade(),
                        item.getUnidade(), item.getEspecificacao()))
                .toList();

        return new SolicitacaoResponse(
                solicitacao.getId(),
                solicitacao.getCodigo(),
                solicitacao.getSolicitante(),
                solicitacao.getDepartamento(),
                solicitacao.getJustificativa(),
                solicitacao.getStatus(),
                solicitacao.getCriterioAvaliacao(),
                solicitacao.getCriadaEm(),
                solicitacao.getCotacaoSelecionadaId(),
                solicitacao.getValorSelecionadoBrl(),
                solicitacao.getNivelAprovacaoExigido(),
                solicitacao.getNivelAprovador(),
                solicitacao.getObservacaoDecisao(),
                solicitacao.getDecididaEm(),
                itens);
    }

    public CotacaoResponse toResponse(Cotacao cotacao) {
        return new CotacaoResponse(
                cotacao.getId(),
                cotacao.getSolicitacao().getId(),
                cotacao.getFornecedor().getId(),
                cotacao.getFornecedor().getRazaoSocial(),
                cotacao.getValorProdutos(),
                cotacao.getFrete(),
                cotacao.totalOriginal(),
                cotacao.getMoeda(),
                cotacao.getPrazoEntregaDias(),
                cotacao.getPrazoPagamentoDias(),
                cotacao.getValidade(),
                cotacao.getStatus());
    }

    public AvaliacaoCotacaoResponse toResponse(ResultadoAvaliacaoCotacao resultado) {
        Cotacao cotacao = resultado.vencedora();
        return new AvaliacaoCotacaoResponse(
                cotacao.getSolicitacao().getId(),
                resultado.criterio(),
                cotacao.getId(),
                cotacao.getFornecedor().getRazaoSocial(),
                cotacao.totalOriginal(),
                cotacao.getMoeda(),
                resultado.totalBrl(),
                cotacao.getPrazoEntregaDias(),
                cotacao.getPrazoPagamentoDias(),
                resultado.nivelAprovacao(),
                resultado.justificativa());
    }
}
