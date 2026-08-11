package br.com.jaaschenbrenner.compraflow.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.com.jaaschenbrenner.compraflow.domain.CriterioCotacao;
import br.com.jaaschenbrenner.compraflow.domain.NivelAprovacao;
import br.com.jaaschenbrenner.compraflow.domain.StatusSolicitacao;

public record SolicitacaoResponse(
        Long id,
        String codigo,
        String solicitante,
        String departamento,
        String justificativa,
        StatusSolicitacao status,
        CriterioCotacao criterioAvaliacao,
        LocalDateTime criadaEm,
        Long cotacaoSelecionadaId,
        BigDecimal valorSelecionadoBrl,
        NivelAprovacao nivelAprovacaoExigido,
        NivelAprovacao nivelAprovador,
        String observacaoDecisao,
        LocalDateTime decididaEm,
        List<ItemSolicitacaoResponse> itens) {
}
