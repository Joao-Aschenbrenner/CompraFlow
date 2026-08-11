package br.com.jaaschenbrenner.compraflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class CompraFlowWorkflowTest {

    private static final Pattern ID_PATTERN = Pattern.compile("\\\"id\\\"\\s*:\\s*(\\d+)");

    @Autowired
    MockMvc mockMvc;

    @Test
    void deveExecutarFluxoDeSolicitacaoCotacaoAvaliacaoEAprovacao() throws Exception {
        mockMvc.perform(get("/api/fornecedores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(3)));

        String criarSolicitacao = """
                {
                  "solicitante": "João",
                  "departamento": "Tecnologia",
                  "justificativa": "Renovação de monitores da equipe",
                  "criterioAvaliacao": "CUSTO_BENEFICIO",
                  "itens": [
                    {
                      "descricao": "Monitor 27 polegadas",
                      "quantidade": 4,
                      "unidade": "UN",
                      "especificacao": "IPS, resolução QHD"
                    }
                  ]
                }
                """;

        MvcResult criada = mockMvc.perform(post("/api/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarSolicitacao))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RASCUNHO"))
                .andReturn();

        long solicitacaoId = extractId(criada.getResponse().getContentAsString());

        mockMvc.perform(post("/api/solicitacoes/{id}/abrir-cotacao", solicitacaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_COTACAO"));

        LocalDate validade = LocalDate.now().plusDays(10);
        registrarCotacao(solicitacaoId, 1, "4000.00", "200.00", 8, 30, validade);
        registrarCotacao(solicitacaoId, 2, "3900.00", "150.00", 15, 10, validade);
        registrarCotacao(solicitacaoId, 3, "4300.00", "50.00", 3, 30, validade);

        mockMvc.perform(post("/api/solicitacoes/{id}/avaliar", solicitacaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criterio").value("CUSTO_BENEFICIO"))
                .andExpect(jsonPath("$.fornecedorVencedor").value("Entrega Rápida Distribuidora"))
                .andExpect(jsonPath("$.nivelAprovacaoExigido").value("GERENTE"));

        mockMvc.perform(post("/api/solicitacoes/{id}/aprovar", solicitacaoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nivelAprovador":"GERENTE","observacao":"Compra necessária e dentro do orçamento."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"));
    }

    private void registrarCotacao(long solicitacaoId, long fornecedorId, String produtos,
                                  String frete, int entrega, int pagamento, LocalDate validade) throws Exception {
        String body = """
                {
                  "fornecedorId": %d,
                  "valorProdutos": %s,
                  "frete": %s,
                  "moeda": "BRL",
                  "prazoEntregaDias": %d,
                  "prazoPagamentoDias": %d,
                  "validade": "%s"
                }
                """.formatted(fornecedorId, produtos, frete, entrega, pagamento, validade);

        mockMvc.perform(post("/api/solicitacoes/{id}/cotacoes", solicitacaoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    private long extractId(String json) {
        Matcher matcher = ID_PATTERN.matcher(json);
        if (!matcher.find()) {
            throw new AssertionError("Não foi possível localizar o ID no JSON: " + json);
        }
        return Long.parseLong(matcher.group(1));
    }
}
