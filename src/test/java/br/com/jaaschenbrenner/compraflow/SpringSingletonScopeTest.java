package br.com.jaaschenbrenner.compraflow;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import br.com.jaaschenbrenner.compraflow.config.PoliticasCompraProperties;

@SpringBootTest
class SpringSingletonScopeTest {

    @Autowired
    ApplicationContext context;

    @Test
    void beanDePoliticasDeveUsarEscopoSingletonPadraoDoSpring() {
        PoliticasCompraProperties primeira = context.getBean(PoliticasCompraProperties.class);
        PoliticasCompraProperties segunda = context.getBean(PoliticasCompraProperties.class);
        assertSame(primeira, segunda);
    }
}
