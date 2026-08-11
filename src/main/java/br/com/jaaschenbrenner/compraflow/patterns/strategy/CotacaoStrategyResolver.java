package br.com.jaaschenbrenner.compraflow.patterns.strategy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import br.com.jaaschenbrenner.compraflow.domain.CriterioCotacao;
import br.com.jaaschenbrenner.compraflow.exception.RegraNegocioException;

@Component
public class CotacaoStrategyResolver {

    private final Map<CriterioCotacao, SelecionarCotacaoStrategy> strategies;

    public CotacaoStrategyResolver(List<SelecionarCotacaoStrategy> strategies) {
        Map<CriterioCotacao, SelecionarCotacaoStrategy> map = new EnumMap<>(CriterioCotacao.class);
        for (SelecionarCotacaoStrategy strategy : strategies) {
            map.put(strategy.criterio(), strategy);
        }
        this.strategies = Map.copyOf(map);
    }

    public SelecionarCotacaoStrategy resolver(CriterioCotacao criterio) {
        SelecionarCotacaoStrategy strategy = strategies.get(criterio);
        if (strategy == null) {
            throw new RegraNegocioException("Estratégia de cotação não implementada: " + criterio);
        }
        return strategy;
    }
}
