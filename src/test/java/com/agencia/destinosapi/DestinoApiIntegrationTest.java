package com.agencia.destinosapi;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integracao da API: exercitam controller + service + repository +
 * banco (H2 em memoria) + Spring Security, usando os usuarios de teste criados
 * pelo DataInitializer (admin/admin123 e usuario/user123).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DestinoApiIntegrationTest {

    private static final String ADMIN = "admin";
    private static final String ADMIN_SENHA = "admin123";
    private static final String USER = "usuario";
    private static final String USER_SENHA = "user123";

    @Autowired
    private MockMvc mockMvc;

    private static String destinoJson(String nome, String localizacao) {
        return """
                {
                  "nome": "%s",
                  "localizacao": "%s",
                  "descricao": "Destino criado pelos testes",
                  "atividadesTuristicas": ["Passeio", "Gastronomia"],
                  "disponibilidadeHoteis": true
                }
                """.formatted(nome, localizacao);
    }

    /** Cadastra um destino como ADMIN e devolve o id gerado pelo banco. */
    private long criarDestino(String nome, String localizacao) throws Exception {
        String corpo = mockMvc.perform(post("/api/destinos")
                        .with(httpBasic(ADMIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(destinoJson(nome, localizacao)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, "$.id")).longValue();
    }

    // ------------------------------------------------------------------
    // Consulta publica
    // ------------------------------------------------------------------

    @Test
    void listagemEPublicaEIncluiDestinosDeExemplo() throws Exception {
        mockMvc.perform(get("/api/destinos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[*].nome", hasItem("Praia do Rosa")));
    }

    @Test
    void pesquisaPorNomeELocalizacaoNaoDiferenciaMaiusculas() throws Exception {
        criarDestino("Fernando de Noronha", "Pernambuco, Brasil");

        mockMvc.perform(get("/api/destinos").param("nome", "NORONHA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome", hasItem("Fernando de Noronha")));

        mockMvc.perform(get("/api/destinos").param("localizacao", "pernambuco"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome", hasItem("Fernando de Noronha")));
    }

    @Test
    void buscarPorIdInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/destinos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void idNaoNumericoRetorna400EmVezDe500() throws Exception {
        mockMvc.perform(get("/api/destinos/abc"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------
    // Autenticacao (401) e autorizacao por perfil (403)
    // ------------------------------------------------------------------

    @Test
    void cadastrarSemAutenticacaoRetorna401() throws Exception {
        mockMvc.perform(post("/api/destinos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(destinoJson("Sem Login", "Lugar Nenhum")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void senhaIncorretaRetorna401() throws Exception {
        mockMvc.perform(post("/api/destinos")
                        .with(httpBasic(ADMIN, "senha-errada"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(destinoJson("Senha Errada", "Lugar Nenhum")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioInexistenteRetorna401() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(httpBasic("fantasma", "qualquer")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userNaoPodeCadastrarAtualizarNemExcluir() throws Exception {
        long id = criarDestino("Protegido", "Sao Paulo, Brasil");

        mockMvc.perform(post("/api/destinos")
                        .with(httpBasic(USER, USER_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(destinoJson("Proibido", "Lugar Nenhum")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(put("/api/destinos/" + id)
                        .with(httpBasic(USER, USER_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(destinoJson("Alterado", "Lugar Nenhum")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/destinos/" + id)
                        .with(httpBasic(USER, USER_SENHA)))
                .andExpect(status().isForbidden());

        // continua existindo e sem alteracao
        mockMvc.perform(get("/api/destinos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Protegido"));
    }

    @Test
    void atualizarEExcluirSemAutenticacaoRetorna401() throws Exception {
        long id = criarDestino("Somente Admin", "Curitiba, Brasil");

        mockMvc.perform(put("/api/destinos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(destinoJson("Alterado", "Curitiba, Brasil")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/destinos/" + id))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    // CRUD como ADMIN (persistencia real no banco)
    // ------------------------------------------------------------------

    @Test
    void adminCadastraEDestinoFicaPersistido() throws Exception {
        mockMvc.perform(post("/api/destinos")
                        .with(httpBasic(ADMIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(destinoJson("Bonito", "Mato Grosso do Sul, Brasil")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Bonito"))
                .andExpect(jsonPath("$.mediaAvaliacao").value(0.0))
                .andExpect(jsonPath("$.totalAvaliacoes").value(0))
                .andExpect(jsonPath("$.atividadesTuristicas.length()").value(2));

        mockMvc.perform(get("/api/destinos").param("nome", "bonito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].localizacao").value("Mato Grosso do Sul, Brasil"));
    }

    @Test
    void cadastroComDadosInvalidosRetorna400ComCamposInvalidos() throws Exception {
        mockMvc.perform(post("/api/destinos")
                        .with(httpBasic(ADMIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"\", \"localizacao\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.camposInvalidos.nome").exists())
                .andExpect(jsonPath("$.camposInvalidos.localizacao").exists());
    }

    @Test
    void jsonMalformadoRetorna400() throws Exception {
        mockMvc.perform(post("/api/destinos")
                        .with(httpBasic(ADMIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ isto nao e json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void adminAtualizaDestino() throws Exception {
        long id = criarDestino("Nome Antigo", "Local Antigo");

        mockMvc.perform(put("/api/destinos/" + id)
                        .with(httpBasic(ADMIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Nome Novo", "localizacao": "Local Novo",
                                 "descricao": "Atualizado", "atividadesTuristicas": ["Trilha"],
                                 "disponibilidadeHoteis": false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Novo"))
                .andExpect(jsonPath("$.disponibilidadeHoteis").value(false))
                .andExpect(jsonPath("$.atividadesTuristicas.length()").value(1));

        mockMvc.perform(get("/api/destinos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Novo"))
                .andExpect(jsonPath("$.localizacao").value("Local Novo"));
    }

    @Test
    void atualizarSemInformarAtividadesNaoDeixaListaNula() throws Exception {
        long id = criarDestino("Com Atividades", "Local");

        mockMvc.perform(put("/api/destinos/" + id)
                        .with(httpBasic(ADMIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Sem Atividades\", \"localizacao\": \"Local\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atividadesTuristicas.length()").value(0));
    }

    @Test
    void atualizarDestinoInexistenteRetorna404() throws Exception {
        mockMvc.perform(put("/api/destinos/999999")
                        .with(httpBasic(ADMIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(destinoJson("Nada", "Nada")))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminExcluiDestinoJuntoComSuasAvaliacoes() throws Exception {
        long id = criarDestino("Para Excluir", "Local");

        mockMvc.perform(patch("/api/destinos/" + id + "/avaliacoes")
                        .with(httpBasic(USER, USER_SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\": 3}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/destinos/" + id)
                        .with(httpBasic(ADMIN, ADMIN_SENHA)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/destinos/" + id))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/destinos/" + id)
                        .with(httpBasic(ADMIN, ADMIN_SENHA)))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // Avaliacoes
    // ------------------------------------------------------------------

    @Test
    void avaliarSemAutenticacaoRetorna401() throws Exception {
        long id = criarDestino("Avaliacao Anonima", "Local");

        mockMvc.perform(patch("/api/destinos/" + id + "/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\": 5}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void avaliacoesRecalculamAMediaEUmUsuarioTemUmaNotaPorDestino() throws Exception {
        long id = criarDestino("Avaliacao Media", "Local");
        String rota = "/api/destinos/" + id + "/avaliacoes";

        // USER avalia com 4
        mockMvc.perform(patch(rota).with(httpBasic(USER, USER_SENHA))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\": 4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaAvaliacao").value(4.0))
                .andExpect(jsonPath("$.totalAvaliacoes").value(1));

        // ADMIN avalia com 5 -> media (4 + 5) / 2
        mockMvc.perform(patch(rota).with(httpBasic(ADMIN, ADMIN_SENHA))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\": 5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaAvaliacao").value(4.5))
                .andExpect(jsonPath("$.totalAvaliacoes").value(2));

        // USER avalia de novo com 2: substitui a nota 4 -> media (2 + 5) / 2, total continua 2
        mockMvc.perform(patch(rota).with(httpBasic(USER, USER_SENHA))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaAvaliacao").value(3.5))
                .andExpect(jsonPath("$.totalAvaliacoes").value(2));

        // a media fica gravada no banco e aparece na consulta publica
        mockMvc.perform(get("/api/destinos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaAvaliacao").value(3.5))
                .andExpect(jsonPath("$.totalAvaliacoes").value(2));
    }

    @Test
    void notaForaDoIntervaloRetorna400() throws Exception {
        long id = criarDestino("Nota Invalida", "Local");
        String rota = "/api/destinos/" + id + "/avaliacoes";

        mockMvc.perform(patch(rota).with(httpBasic(USER, USER_SENHA))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\": 6}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.camposInvalidos.nota").exists());

        mockMvc.perform(patch(rota).with(httpBasic(USER, USER_SENHA))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\": -1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void avaliarDestinoInexistenteRetorna404() throws Exception {
        mockMvc.perform(patch("/api/destinos/999999/avaliacoes").with(httpBasic(USER, USER_SENHA))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\": 3}"))
                .andExpect(status().isNotFound());
    }
}
