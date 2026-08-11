package br.com.jaaschenbrenner.compraflow.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.jaaschenbrenner.compraflow.domain.Moeda;
import br.com.jaaschenbrenner.compraflow.domain.StatusCotacao;

public record CotacaoResponse(
        Long id,
        Long solicitacaoId,
        Long fornecedorId,
        String fornecedor,
        BigDecimal valorProdutos,
        BigDecimal frete,
        BigDecimal totalOriginal,
        Moeda moeda,
        Integer prazoEntregaDias,
        Integer prazoPagamentoDias,
        LocalDate validade,
        StatusCotacao status) {
}
