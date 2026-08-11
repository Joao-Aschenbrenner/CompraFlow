package br.com.jaaschenbrenner.compraflow.api.dto;

import br.com.jaaschenbrenner.compraflow.domain.NivelAprovacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AprovacaoRequest(
        @NotNull NivelAprovacao nivelAprovador,
        @Size(max = 500) String observacao) {
}
