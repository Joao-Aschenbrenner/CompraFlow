package br.com.jaaschenbrenner.compraflow.patterns.strategy;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import br.com.jaaschenbrenner.compraflow.domain.CriterioCotacao;
import br.com.jaaschenbrenner.compraflow.exception.RegraNegocioException;
import br.com.jaaschenbrenner.compraflow.patterns.singleton.MoneyRoundingPolicy;

@Component
public class CustoBeneficioStrategy implements SelecionarCotacaoStrategy {

    private static final BigDecimal PESO_PRECO = new BigDecimal("0.60");
    private static final BigDecimal PESO_PRAZO = new BigDecimal("0.25");
    private static final BigDecimal PESO_PAGAMENTO = new BigDecimal("0.15");
    private static final MathContext MC = MathContext.DECIMAL64;

    @Override
    public CriterioCotacao criterio() {
        return CriterioCotacao.CUSTO_BENEFICIO;
    }

    @Override
    public DecisaoCotacao selecionar(List<OfertaCotacao> ofertas) {
        if (ofertas == null || ofertas.isEmpty()) {
            throw new RegraNegocioException("Não existem cotações válidas para avaliação.");
        }

        BigDecimal maxPreco = ofertas.stream()
                .map(OfertaCotacao::totalEmBrl)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);
        int maxPrazo = ofertas.stream().mapToInt(OfertaCotacao::prazoEntregaDias).max().orElse(1);
        int maxPagamento = ofertas.stream().mapToInt(OfertaCotacao::prazoPagamentoDias).max().orElse(1);

        ScoredOffer melhor = ofertas.stream()
                .map(oferta -> new ScoredOffer(oferta, score(oferta, maxPreco, maxPrazo, maxPagamento)))
                .min(Comparator.comparing(ScoredOffer::score)
                        .thenComparing(s -> s.oferta().totalEmBrl()))
                .orElseThrow();

        return new DecisaoCotacao(
                melhor.oferta(),
                MoneyRoundingPolicy.getInstance().money(melhor.score()),
                "Melhor custo-benefício: 60% preço, 25% prazo de entrega e 15% condição de pagamento. Menor score vence.");
    }

    private BigDecimal score(OfertaCotacao oferta, BigDecimal maxPreco, int maxPrazo, int maxPagamento) {
        BigDecimal preco = maxPreco.signum() == 0
                ? BigDecimal.ZERO
                : oferta.totalEmBrl().divide(maxPreco, MC).multiply(PESO_PRECO, MC);

        BigDecimal prazo = maxPrazo == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(oferta.prazoEntregaDias())
                        .divide(BigDecimal.valueOf(maxPrazo), MC)
                        .multiply(PESO_PRAZO, MC);

        BigDecimal pagamentoPenalty;
        if (maxPagamento == 0) {
            pagamentoPenalty = BigDecimal.ZERO;
        } else {
            pagamentoPenalty = BigDecimal.valueOf(maxPagamento - oferta.prazoPagamentoDias())
                    .divide(BigDecimal.valueOf(maxPagamento), MC)
                    .multiply(PESO_PAGAMENTO, MC);
        }

        return preco.add(prazo, MC).add(pagamentoPenalty, MC);
    }

    private record ScoredOffer(OfertaCotacao oferta, BigDecimal score) {
    }
}
