package br.com.jaaschenbrenner.compraflow.patterns.chain;

import java.math.BigDecimal;

import br.com.jaaschenbrenner.compraflow.domain.NivelAprovacao;

public class GerenteApprovalHandler extends ApprovalHandler {
    private final BigDecimal limite;

    public GerenteApprovalHandler(BigDecimal limite, ApprovalHandler next) {
        super(next);
        this.limite = limite;
    }

    @Override
    protected boolean aceita(BigDecimal valorTotalBrl) {
        return valorTotalBrl.compareTo(limite) <= 0;
    }

    @Override
    protected NivelAprovacao nivel() {
        return NivelAprovacao.GERENTE;
    }
}
