package br.com.jaaschenbrenner.compraflow.integration.frankfurter;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CambioApiResponse(
        LocalDate date,
        String base,
        String quote,
        BigDecimal rate) {
}
