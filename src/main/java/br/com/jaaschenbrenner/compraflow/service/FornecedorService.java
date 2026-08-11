package br.com.jaaschenbrenner.compraflow.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.jaaschenbrenner.compraflow.domain.Fornecedor;
import br.com.jaaschenbrenner.compraflow.exception.RecursoNaoEncontradoException;
import br.com.jaaschenbrenner.compraflow.exception.RegraNegocioException;
import br.com.jaaschenbrenner.compraflow.repository.FornecedorRepository;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    @Transactional
    public Fornecedor criar(String razaoSocial, String cnpj, String email) {
        if (fornecedorRepository.existsByCnpj(cnpj)) {
            throw new RegraNegocioException("Já existe um fornecedor cadastrado com este CNPJ.");
        }
        return fornecedorRepository.save(new Fornecedor(razaoSocial, cnpj, email));
    }

    @Transactional(readOnly = true)
    public List<Fornecedor> listarAtivos() {
        return fornecedorRepository.findAllByAtivoTrueOrderByRazaoSocialAsc();
    }

    @Transactional(readOnly = true)
    public Fornecedor buscarAtivo(Long id) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado: " + id));
        if (!fornecedor.isAtivo()) {
            throw new RegraNegocioException("Fornecedor está inativo: " + id);
        }
        return fornecedor;
    }
}
