package br.com.jaaschenbrenner.compraflow.service.model;

import java.math.BigDecimal;

import br.com.jaaschenbrenner.compraflow.domain.CriterioCotacao;
import br.com.jaaschenbrenner.compraflow.domain.Cotacao;
import br.com.jaaschenbrenner.compraflow.domain.NivelAprovacao;

public record ResultadoAvaliacaoCotacao(
        Cotacao vencedora,
        BigDecimal totalBrl,
        CriterioCotacao criterio,
        NivelAprovacao nivelAprovacao,
        String justificativa) {
}
