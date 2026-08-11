package br.com.jaaschenbrenner.compraflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.jaaschenbrenner.compraflow.domain.SolicitacaoCompra;

public interface SolicitacaoCompraRepository extends JpaRepository<SolicitacaoCompra, Long> {
    List<SolicitacaoCompra> findAllByOrderByCriadaEmDesc();
}
