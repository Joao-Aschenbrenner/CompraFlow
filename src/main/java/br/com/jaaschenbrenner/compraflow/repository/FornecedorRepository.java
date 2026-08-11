package br.com.jaaschenbrenner.compraflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.jaaschenbrenner.compraflow.domain.Fornecedor;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    boolean existsByCnpj(String cnpj);
    List<Fornecedor> findAllByAtivoTrueOrderByRazaoSocialAsc();
}
