package br.com.jaaschenbrenner.compraflow.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configurações de negócio centralizadas. Como bean do Spring, esta classe usa
 * o escopo singleton padrão do container, conectando o conceito do curso com IoC/DI.
 */
@Component
@ConfigurationProperties(prefix = "compraflow.policies")
public class PoliticasCompraProperties {
    private int minQuotes = 3;
    private final Approval approval = new Approval();

    public int getMinQuotes() {
        return minQuotes;
    }

    public void setMinQuotes(int minQuotes) {
        this.minQuotes = minQuotes;
    }

    public Approval getApproval() {
        return approval;
    }

    public static class Approval {
        private BigDecimal coordinatorLimit = new BigDecimal("2000.00");
        private BigDecimal managerLimit = new BigDecimal("10000.00");
        private BigDecimal directorLimit = new BigDecimal("50000.00");

        public BigDecimal getCoordinatorLimit() {
            return coordinatorLimit;
        }

        public void setCoordinatorLimit(BigDecimal coordinatorLimit) {
            this.coordinatorLimit = coordinatorLimit;
        }

        public BigDecimal getManagerLimit() {
            return managerLimit;
        }

        public void setManagerLimit(BigDecimal managerLimit) {
            this.managerLimit = managerLimit;
        }

        public BigDecimal getDirectorLimit() {
            return directorLimit;
        }

        public void setDirectorLimit(BigDecimal directorLimit) {
            this.directorLimit = directorLimit;
        }
    }
}
