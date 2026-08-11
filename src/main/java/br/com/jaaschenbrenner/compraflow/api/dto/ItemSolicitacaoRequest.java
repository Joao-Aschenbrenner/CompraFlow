package br.com.jaaschenbrenner.compraflow.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ItemSolicitacaoRequest(
        @NotBlank @Size(max = 180) String descricao,
        @NotNull @Min(1) Integer quantidade,
        @NotBlank @Size(max = 20) String unidade,
        @Size(max = 1000) String especificacao) {
}
