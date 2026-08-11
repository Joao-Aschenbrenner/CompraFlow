package br.com.jaaschenbrenner.compraflow.api.dto;

public record ItemSolicitacaoResponse(
        Long id,
        String descricao,
        Integer quantidade,
        String unidade,
        String especificacao) {
}
