package com.agencia.destinosapi;

import com.agencia.destinosapi.model.Usuario;
import com.agencia.destinosapi.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integracao dos fluxos de conta: login (HTTP Basic contra o banco),
 * registro de novos usuarios e armazenamento seguro da senha.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void usuariosDeTesteAutenticamComSeusPerfis() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.perfil").value("ADMIN"));

        mockMvc.perform(get("/api/auth/me").with(httpBasic("usuario", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("usuario"))
                .andExpect(jsonPath("$.perfil").value("USER"));
    }

    @Test
    void meSemCredenciaisRetorna401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void respostaNuncaExpoeASenha() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void senhaEGravadaCriptografadaComBCrypt() {
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();

        assertNotEquals("admin123", admin.getSenha());
        assertTrue(admin.getSenha().startsWith("$2"), "o hash deveria estar no formato BCrypt");
        assertTrue(passwordEncoder.matches("admin123", admin.getSenha()));
    }

    @Test
    void registroCriaUsuarioComPerfilUserQueConsegueAutenticar() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Maria Teste", "username": "maria.teste", "senha": "segredo123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("maria.teste"))
                .andExpect(jsonPath("$.perfil").value("USER"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        // consegue autenticar com a senha cadastrada
        mockMvc.perform(get("/api/auth/me").with(httpBasic("maria.teste", "segredo123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("USER"));

        // e, sendo USER, nao pode cadastrar destinos
        mockMvc.perform(post("/api/destinos")
                        .with(httpBasic("maria.teste", "segredo123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"X\", \"localizacao\": \"Y\"}"))
                .andExpect(status().isForbidden());

        assertEquals("USER", usuarioRepository.findByUsername("maria.teste").orElseThrow().getPerfil().name());
    }

    @Test
    void registroNaoPermiteEscolherPerfilAdmin() throws Exception {
        // o campo "perfil" enviado pelo cliente e simplesmente ignorado
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Espertinho", "username": "espertinho", "senha": "segredo123", "perfil": "ADMIN"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("USER"));
    }

    @Test
    void registroComUsernameDuplicadoRetorna409() throws Exception {
        String corpo = """
                {"nome": "Duplicado", "username": "duplicado", "senha": "segredo123"}
                """;

        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void registroComDadosInvalidosRetorna400() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "", "username": "Nome Invalido", "senha": "123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.camposInvalidos.nome").exists())
                .andExpect(jsonPath("$.camposInvalidos.username").exists())
                .andExpect(jsonPath("$.camposInvalidos.senha").exists());
    }
}
