package br.com.jaaschenbrenner.compraflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.jaaschenbrenner.compraflow.domain.Cotacao;

public interface CotacaoRepository extends JpaRepository<Cotacao, Long> {
    List<Cotacao> findAllBySolicitacaoIdOrderByIdAsc(Long solicitacaoId);
    boolean existsBySolicitacaoIdAndFornecedorId(Long solicitacaoId, Long fornecedorId);
}
