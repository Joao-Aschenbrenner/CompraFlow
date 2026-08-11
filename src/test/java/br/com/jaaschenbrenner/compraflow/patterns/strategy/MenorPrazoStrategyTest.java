package br.com.jaaschenbrenner.compraflow.patterns.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class MenorPrazoStrategyTest {

    @Test
    void deveSelecionarMenorPrazo() {
        List<OfertaCotacao> ofertas = List.of(
                new OfertaCotacao(1L, "Fornecedor A", new BigDecimal("3900.00"), 10, 30),
                new OfertaCotacao(2L, "Fornecedor B", new BigDecimal("4200.00"), 2, 20),
                new OfertaCotacao(3L, "Fornecedor C", new BigDecimal("4000.00"), 5, 30));

        DecisaoCotacao decisao = new MenorPrazoStrategy().selecionar(ofertas);

        assertEquals(2L, decisao.vencedora().cotacaoId());
    }
}
