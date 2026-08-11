package br.com.jaaschenbrenner.compraflow.patterns.strategy;

import java.util.List;

import br.com.jaaschenbrenner.compraflow.domain.CriterioCotacao;

/**
 * Strategy: define um contrato único para diferentes algoritmos de seleção.
 */
public interface SelecionarCotacaoStrategy {
    CriterioCotacao criterio();
    DecisaoCotacao selecionar(List<OfertaCotacao> ofertas);
}
