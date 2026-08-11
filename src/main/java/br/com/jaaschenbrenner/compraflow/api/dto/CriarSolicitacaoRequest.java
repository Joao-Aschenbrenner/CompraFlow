package br.com.jaaschenbrenner.compraflow.api.dto;

import java.util.List;

import br.com.jaaschenbrenner.compraflow.domain.CriterioCotacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarSolicitacaoRequest(
        @NotBlank @Size(max = 120) String solicitante,
        @NotBlank @Size(max = 100) String departamento,
        @NotBlank @Size(max = 1000) String justificativa,
        @NotNull CriterioCotacao criterioAvaliacao,
        @NotEmpty List<@Valid ItemSolicitacaoRequest> itens) {
}
