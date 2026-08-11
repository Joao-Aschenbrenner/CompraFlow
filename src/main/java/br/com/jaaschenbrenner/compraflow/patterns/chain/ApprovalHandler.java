package br.com.jaaschenbrenner.compraflow.patterns.chain;

import java.math.BigDecimal;

import br.com.jaaschenbrenner.compraflow.domain.NivelAprovacao;

/**
 * Chain of Responsibility: cada elo decide se consegue tratar o valor ou delega ao próximo.
 */
public abstract class ApprovalHandler {
    private final ApprovalHandler next;

    protected ApprovalHandler(ApprovalHandler next) {
        this.next = next;
    }

    public NivelAprovacao resolver(BigDecimal valorTotalBrl) {
        if (aceita(valorTotalBrl)) {
            return nivel();
        }
        if (next == null) {
            return nivel();
        }
        return next.resolver(valorTotalBrl);
    }

    protected abstract boolean aceita(BigDecimal valorTotalBrl);

    protected abstract NivelAprovacao nivel();
}
