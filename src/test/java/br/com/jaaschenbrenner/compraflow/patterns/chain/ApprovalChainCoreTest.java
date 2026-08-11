package br.com.jaaschenbrenner.compraflow.patterns.chain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.jaaschenbrenner.compraflow.domain.NivelAprovacao;

class ApprovalChainCoreTest {

    private final ApprovalHandler chain = new CoordenadorApprovalHandler(
            new BigDecimal("2000.00"),
            new GerenteApprovalHandler(
                    new BigDecimal("10000.00"),
                    new DiretorApprovalHandler(
                            new BigDecimal("50000.00"),
                            new DiretoriaApprovalHandler())));

    @Test
    void deveEscalonarAprovacaoConformeValor() {
        assertEquals(NivelAprovacao.COORDENADOR, chain.resolver(new BigDecimal("1500.00")));
        assertEquals(NivelAprovacao.GERENTE, chain.resolver(new BigDecimal("5000.00")));
        assertEquals(NivelAprovacao.DIRETOR, chain.resolver(new BigDecimal("20000.00")));
        assertEquals(NivelAprovacao.DIRETORIA, chain.resolver(new BigDecimal("90000.00")));
    }
}
