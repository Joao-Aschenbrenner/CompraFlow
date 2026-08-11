package br.com.jaaschenbrenner.compraflow.api.dto;

public record FornecedorResponse(
        Long id,
        String razaoSocial,
        String cnpj,
        String email,
        boolean ativo) {
}
