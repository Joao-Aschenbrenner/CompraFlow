package br.com.jaaschenbrenner.compraflow.patterns.strategy;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import br.com.jaaschenbrenner.compraflow.domain.CriterioCotacao;
import br.com.jaaschenbrenner.compraflow.exception.RegraNegocioException;

@Component
public class MenorPrazoStrategy implements SelecionarCotacaoStrategy {

    @Override
    public CriterioCotacao criterio() {
        return CriterioCotacao.MENOR_PRAZO;
    }

    @Override
    public DecisaoCotacao selecionar(List<OfertaCotacao> ofertas) {
        OfertaCotacao vencedora = ofertas.stream()
                .min(Comparator.comparingInt(OfertaCotacao::prazoEntregaDias)
                        .thenComparing(OfertaCotacao::totalEmBrl))
                .orElseThrow(() -> new RegraNegocioException("Não existem cotações válidas para avaliação."));

        return new DecisaoCotacao(
                vencedora,
                BigDecimal.ZERO,
                "Menor prazo de entrega; valor total em BRL usado como desempate.");
    }
}
