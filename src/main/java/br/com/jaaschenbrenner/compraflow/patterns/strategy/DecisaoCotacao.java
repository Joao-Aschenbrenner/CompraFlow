package br.com.jaaschenbrenner.compraflow.patterns.strategy;

import java.math.BigDecimal;

public record DecisaoCotacao(
        OfertaCotacao vencedora,
        BigDecimal score,
        String justificativa) {
}
