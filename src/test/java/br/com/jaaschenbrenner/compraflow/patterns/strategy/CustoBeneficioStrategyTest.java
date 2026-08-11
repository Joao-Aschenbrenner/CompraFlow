package br.com.jaaschenbrenner.compraflow.patterns.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class CustoBeneficioStrategyTest {

    @Test
    void deveConsiderarPrecoPrazoECondicaoDePagamento() {
        List<OfertaCotacao> ofertas = List.of(
                new OfertaCotacao(1L, "Office", new BigDecimal("4200.00"), 8, 30),
                new OfertaCotacao(2L, "Tech", new BigDecimal("4050.00"), 15, 10),
                new OfertaCotacao(3L, "Rapida", new BigDecimal("4350.00"), 3, 30));

        DecisaoCotacao decisao = new CustoBeneficioStrategy().selecionar(ofertas);

        assertEquals(3L, decisao.vencedora().cotacaoId());
    }
}
