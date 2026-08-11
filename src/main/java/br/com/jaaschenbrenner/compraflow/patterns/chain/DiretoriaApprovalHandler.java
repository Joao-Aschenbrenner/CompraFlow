package br.com.jaaschenbrenner.compraflow.patterns.chain;

import java.math.BigDecimal;

import br.com.jaaschenbrenner.compraflow.domain.NivelAprovacao;

public class DiretoriaApprovalHandler extends ApprovalHandler {

    public DiretoriaApprovalHandler() {
        super(null);
    }

    @Override
    protected boolean aceita(BigDecimal valorTotalBrl) {
        return true;
    }

    @Override
    protected NivelAprovacao nivel() {
        return NivelAprovacao.DIRETORIA;
    }
}
