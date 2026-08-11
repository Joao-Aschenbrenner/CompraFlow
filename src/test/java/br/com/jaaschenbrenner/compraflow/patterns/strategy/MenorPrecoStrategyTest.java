package br.com.jaaschenbrenner.compraflow.patterns.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class MenorPrecoStrategyTest {

    @Test
    void deveSelecionarMenorPreco() {
        List<OfertaCotacao> ofertas = List.of(
                new OfertaCotacao(1L, "Fornecedor A", new BigDecimal("4300.00"), 7, 30),
                new OfertaCotacao(2L, "Fornecedor B", new BigDecimal("3990.00"), 15, 10),
                new OfertaCotacao(3L, "Fornecedor C", new BigDecimal("4500.00"), 3, 30));

        DecisaoCotacao decisao = new MenorPrecoStrategy().selecionar(ofertas);

        assertEquals(2L, decisao.vencedora().cotacaoId());
    }
}
