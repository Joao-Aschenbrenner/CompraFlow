package br.com.jaaschenbrenner.compraflow.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.jaaschenbrenner.compraflow.api.dto.AprovacaoRequest;
import br.com.jaaschenbrenner.compraflow.api.dto.AvaliacaoCotacaoResponse;
import br.com.jaaschenbrenner.compraflow.api.dto.CotacaoRequest;
import br.com.jaaschenbrenner.compraflow.api.dto.CotacaoResponse;
import br.com.jaaschenbrenner.compraflow.api.dto.CriarSolicitacaoRequest;
import br.com.jaaschenbrenner.compraflow.api.dto.RejeicaoRequest;
import br.com.jaaschenbrenner.compraflow.api.dto.SolicitacaoResponse;
import br.com.jaaschenbrenner.compraflow.patterns.facade.CompraFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/solicitacoes")
@Tag(name = "Solicitações", description = "Fluxo completo de solicitações, cotações e aprovação")
public class SolicitacaoController {

    private final CompraFacade facade;

    public SolicitacaoController(CompraFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    @Operation(summary = "Criar solicitação de compra")
    public ResponseEntity<SolicitacaoResponse> criar(@Valid @RequestBody CriarSolicitacaoRequest request) {
        SolicitacaoResponse response = facade.criarSolicitacao(request);
        return ResponseEntity.created(URI.create("/api/solicitacoes/" + response.id())).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar solicitações")
    public List<SolicitacaoResponse> listar() {
        return facade.listarSolicitacoes();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar solicitação por ID")
    public SolicitacaoResponse buscar(@PathVariable Long id) {
        return facade.buscarSolicitacao(id);
    }

    @PostMapping("/{id}/abrir-cotacao")
    @Operation(summary = "Abrir solicitação para recebimento de cotações")
    public SolicitacaoResponse abrirCotacao(@PathVariable Long id) {
        return facade.abrirCotacao(id);
    }

    @PostMapping("/{id}/cotacoes")
    @Operation(summary = "Registrar cotação de fornecedor")
    public ResponseEntity<CotacaoResponse> registrarCotacao(@PathVariable Long id,
                                                             @Valid @RequestBody CotacaoRequest request) {
        CotacaoResponse response = facade.registrarCotacao(id, request);
        return ResponseEntity.created(URI.create("/api/solicitacoes/" + id + "/cotacoes/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}/cotacoes")
    @Operation(summary = "Listar cotações da solicitação")
    public List<CotacaoResponse> listarCotacoes(@PathVariable Long id) {
        return facade.listarCotacoes(id);
    }

    @PostMapping("/{id}/avaliar")
    @Operation(summary = "Avaliar cotações usando o Strategy escolhido na solicitação")
    public AvaliacaoCotacaoResponse avaliar(@PathVariable Long id) {
        return facade.avaliarCotacoes(id);
    }

    @PostMapping("/{id}/aprovar")
    @Operation(summary = "Aprovar solicitação respeitando o nível exigido")
    public SolicitacaoResponse aprovar(@PathVariable Long id,
                                       @Valid @RequestBody AprovacaoRequest request) {
        return facade.aprovar(id, request);
    }

    @PostMapping("/{id}/rejeitar")
    @Operation(summary = "Rejeitar solicitação")
    public SolicitacaoResponse rejeitar(@PathVariable Long id,
                                        @Valid @RequestBody RejeicaoRequest request) {
        return facade.rejeitar(id, request.motivo());
    }
}
