package br.com.jaaschenbrenner.compraflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejeicaoRequest(
        @NotBlank @Size(max = 500) String motivo) {
}
