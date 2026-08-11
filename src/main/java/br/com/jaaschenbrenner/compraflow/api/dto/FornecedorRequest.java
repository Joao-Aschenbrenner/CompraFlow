package br.com.jaaschenbrenner.compraflow.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FornecedorRequest(
        @NotBlank @Size(max = 160) String razaoSocial,
        @NotBlank @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter exatamente 14 dígitos") String cnpj,
        @NotBlank @Email @Size(max = 180) String email) {
}
