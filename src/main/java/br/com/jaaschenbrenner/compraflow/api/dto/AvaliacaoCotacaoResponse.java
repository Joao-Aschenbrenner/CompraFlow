package br.com.jaaschenbrenner.compraflow.api.dto;

import java.math.BigDecimal;

import br.com.jaaschenbrenner.compraflow.domain.CriterioCotacao;
import br.com.jaaschenbrenner.compraflow.domain.Moeda;
import br.com.jaaschenbrenner.compraflow.domain.NivelAprovacao;

public record AvaliacaoCotacaoResponse(
        Long solicitacaoId,
        CriterioCotacao criterio,
        Long cotacaoVencedoraId,
        String fornecedorVencedor,
        BigDecimal valorOriginal,
        Moeda moedaOriginal,
        BigDecimal valorTotalBrl,
        Integer prazoEntregaDias,
        Integer prazoPagamentoDias,
        NivelAprovacao nivelAprovacaoExigido,
        String justificativa) {
}
