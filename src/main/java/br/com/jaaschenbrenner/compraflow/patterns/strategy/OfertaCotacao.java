package br.com.jaaschenbrenner.compraflow.patterns.strategy;

import java.math.BigDecimal;

public record OfertaCotacao(
        Long cotacaoId,
        String fornecedor,
        BigDecimal totalEmBrl,
        int prazoEntregaDias,
        int prazoPagamentoDias) {
}
