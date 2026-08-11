package br.com.jaaschenbrenner.compraflow.patterns.singleton;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class MoneyRoundingPolicyTest {

    @Test
    void deveRetornarSempreAMesmaInstancia() {
        MoneyRoundingPolicy primeira = MoneyRoundingPolicy.getInstance();
        MoneyRoundingPolicy segunda = MoneyRoundingPolicy.getInstance();

        assertSame(primeira, segunda);
    }

    @Test
    void deveArredondarValoresMonetariosParaDuasCasas() {
        assertEquals(new BigDecimal("10.13"),
                MoneyRoundingPolicy.getInstance().money(new BigDecimal("10.125")));
    }
}
