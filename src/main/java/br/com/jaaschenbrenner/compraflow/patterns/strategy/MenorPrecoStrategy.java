package br.com.jaaschenbrenner.compraflow.patterns.strategy;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import br.com.jaaschenbrenner.compraflow.domain.CriterioCotacao;
import br.com.jaaschenbrenner.compraflow.exception.RegraNegocioException;

@Component
public class MenorPrecoStrategy implements SelecionarCotacaoStrategy {

    @Override
    public CriterioCotacao criterio() {
        return CriterioCotacao.MENOR_PRECO;
    }

    @Override
    public DecisaoCotacao selecionar(List<OfertaCotacao> ofertas) {
        OfertaCotacao vencedora = ofertas.stream()
                .min(Comparator.comparing(OfertaCotacao::totalEmBrl)
                        .thenComparing(OfertaCotacao::prazoEntregaDias))
                .orElseThrow(() -> new RegraNegocioException("Não existem cotações válidas para avaliação."));

        return new DecisaoCotacao(
                vencedora,
                BigDecimal.ZERO,
                "Menor valor total convertido para BRL; prazo de entrega usado como desempate.");
    }
}
