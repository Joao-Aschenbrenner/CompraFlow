package br.com.jaaschenbrenner.compraflow.patterns.singleton;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Singleton Lazy Holder em Java puro. É imutável e thread-safe sem sincronização explícita.
 */
public final class MoneyRoundingPolicy {

    private MoneyRoundingPolicy() {
    }

    private static class Holder {
        private static final MoneyRoundingPolicy INSTANCE = new MoneyRoundingPolicy();
    }

    public static MoneyRoundingPolicy getInstance() {
        return Holder.INSTANCE;
    }

    public BigDecimal money(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
