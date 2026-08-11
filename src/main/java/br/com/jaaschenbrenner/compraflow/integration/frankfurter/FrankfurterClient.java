package br.com.jaaschenbrenner.compraflow.integration.frankfurter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Integração externa opcional para converter cotações em USD/EUR para BRL.
 * O fluxo padrão de demonstração usa BRL e não depende de internet.
 */
@FeignClient(name = "frankfurterClient", url = "${integrations.frankfurter.url}")
public interface FrankfurterClient {

    @GetMapping("/v2/rate/{base}/{quote}")
    CambioApiResponse obterTaxa(@PathVariable("base") String base,
                                @PathVariable("quote") String quote);
}
