package br.com.jaaschenbrenner.compraflow.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.jaaschenbrenner.compraflow.domain.Moeda;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CotacaoRequest(
        @NotNull Long fornecedorId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valorProdutos,
        @NotNull @DecimalMin(value = "0.00") BigDecimal frete,
        @NotNull Moeda moeda,
        @NotNull @Min(1) Integer prazoEntregaDias,
        @NotNull @Min(0) Integer prazoPagamentoDias,
        @NotNull @FutureOrPresent LocalDate validade) {
}
