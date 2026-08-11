package br.com.jaaschenbrenner.compraflow.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.jaaschenbrenner.compraflow.api.dto.FornecedorRequest;
import br.com.jaaschenbrenner.compraflow.api.dto.FornecedorResponse;
import br.com.jaaschenbrenner.compraflow.patterns.facade.CompraFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fornecedores")
@Tag(name = "Fornecedores", description = "Cadastro e consulta de fornecedores")
public class FornecedorController {

    private final CompraFacade facade;

    public FornecedorController(CompraFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    @Operation(summary = "Cadastrar fornecedor")
    public ResponseEntity<FornecedorResponse> criar(@Valid @RequestBody FornecedorRequest request) {
        FornecedorResponse response = facade.criarFornecedor(request);
        return ResponseEntity.created(URI.create("/api/fornecedores/" + response.id())).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar fornecedores ativos")
    public List<FornecedorResponse> listar() {
        return facade.listarFornecedores();
    }
}
