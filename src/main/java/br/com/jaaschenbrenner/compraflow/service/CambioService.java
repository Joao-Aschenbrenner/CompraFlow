package br.com.jaaschenbrenner.compraflow.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import br.com.jaaschenbrenner.compraflow.domain.Moeda;
import br.com.jaaschenbrenner.compraflow.exception.IntegracaoExternaException;
import br.com.jaaschenbrenner.compraflow.integration.frankfurter.CambioApiResponse;
import br.com.jaaschenbrenner.compraflow.integration.frankfurter.FrankfurterClient;
import br.com.jaaschenbrenner.compraflow.patterns.singleton.MoneyRoundingPolicy;
import feign.FeignException;

@Service
public class CambioService {

    private final FrankfurterClient frankfurterClient;

    public CambioService(FrankfurterClient frankfurterClient) {
        this.frankfurterClient = frankfurterClient;
    }

    public BigDecimal converterParaBrl(BigDecimal valor, Moeda moeda) {
        if (moeda == Moeda.BRL) {
            return MoneyRoundingPolicy.getInstance().money(valor);
        }

        try {
            CambioApiResponse resposta = frankfurterClient.obterTaxa(moeda.name(), Moeda.BRL.name());
            if (resposta == null || resposta.rate() == null || resposta.rate().signum() <= 0) {
                throw new IntegracaoExternaException("Serviço de câmbio retornou uma taxa inválida.");
            }
            return MoneyRoundingPolicy.getInstance().money(valor.multiply(resposta.rate()));
        } catch (FeignException ex) {
            throw new IntegracaoExternaException(
                    "Não foi possível consultar a taxa de câmbio para " + moeda + ".", ex);
        }
    }
}
