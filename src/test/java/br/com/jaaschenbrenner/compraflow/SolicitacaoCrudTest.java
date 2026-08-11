package br.com.jaaschenbrenner.compraflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class SolicitacaoCrudTest {

    private static final Pattern ID_PATTERN = Pattern.compile("\\\"id\\\"\\s*:\\s*(\\d+)");

    @Autowired
    MockMvc mockMvc;

    @Test
    void deveEditarEExcluirSolicitacaoEnquantoRascunho() throws Exception {
        long id = criarRascunho("Original", "TI");

        String atualizado = """
                {
                  "solicitante": "João Atualizado",
                  "departamento": "Compras",
                  "justificativa": "Pedido revisado antes da abertura da cotação",
                  "criterioAvaliacao": "MENOR_PRECO",
                  "itens": [
                    {"descricao":"Notebook corporativo","quantidade":2,"unidade":"UN","especificacao":"16 GB RAM"},
                    {"descricao":"Dock USB-C","quantidade":2,"unidade":"UN","especificacao":"100 W"}
                  ]
                }
                """;

        mockMvc.perform(put("/api/solicitacoes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solicitante").value("João Atualizado"))
                .andExpect(jsonPath("$.departamento").value("Compras"))
                .andExpect(jsonPath("$.criterioAvaliacao").value("MENOR_PRECO"))
                .andExpect(jsonPath("$.itens.length()").value(2));

        mockMvc.perform(delete("/api/solicitacoes/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/solicitacoes/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void naoDeveEditarNemExcluirDepoisDeAbrirCotacao() throws Exception {
        long id = criarRascunho("Bloqueado", "Financeiro");

        mockMvc.perform(post("/api/solicitacoes/{id}/abrir-cotacao", id))
                .andExpect(status().isOk());

        String body = """
                {
                  "solicitante":"Tentativa",
                  "departamento":"Outro",
                  "justificativa":"Não deve alterar",
                  "criterioAvaliacao":"MENOR_PRAZO",
                  "itens":[{"descricao":"Item","quantidade":1,"unidade":"UN","especificacao":"Teste"}]
                }
                """;

        mockMvc.perform(put("/api/solicitacoes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(delete("/api/solicitacoes/{id}", id))
                .andExpect(status().isUnprocessableEntity());
    }

    private long criarRascunho(String solicitante, String departamento) throws Exception {
        String body = """
                {
                  "solicitante":"%s",
                  "departamento":"%s",
                  "justificativa":"Teste de CRUD",
                  "criterioAvaliacao":"CUSTO_BENEFICIO",
                  "itens":[{"descricao":"Mouse sem fio","quantidade":3,"unidade":"UN","especificacao":"Bluetooth"}]
                }
                """.formatted(solicitante, departamento);

        MvcResult result = mockMvc.perform(post("/api/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        Matcher matcher = ID_PATTERN.matcher(result.getResponse().getContentAsString());
        if (!matcher.find()) throw new AssertionError("ID não encontrado");
        return Long.parseLong(matcher.group(1));
    }
}
