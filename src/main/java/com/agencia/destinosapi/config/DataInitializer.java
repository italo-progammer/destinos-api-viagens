package com.agencia.destinosapi.config;

import com.agencia.destinosapi.dto.DestinoRequestDTO;
import com.agencia.destinosapi.model.Perfil;
import com.agencia.destinosapi.repository.DestinoRepository;
import com.agencia.destinosapi.service.DestinoService;
import com.agencia.destinosapi.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Popula o banco na inicializacao: cria os usuarios de teste (um ADMIN e um
 * USER) e alguns destinos de exemplo. E IDEMPOTENTE: so cria o que ainda nao
 * existe, entao reiniciar a aplicacao nao duplica registros.
 *
 * Pode ser desligado com app.seed.enabled=false (variavel APP_SEED_ENABLED).
 * As senhas vem das propriedades e sao gravadas ja criptografadas (BCrypt).
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private final UsuarioService usuarioService;
    private final DestinoService destinoService;
    private final DestinoRepository destinoRepository;

    private final String adminUsername;
    private final String adminPassword;
    private final String userUsername;
    private final String userPassword;

    public DataInitializer(UsuarioService usuarioService,
                           DestinoService destinoService,
                           DestinoRepository destinoRepository,
                           @Value("${app.seed.admin.username}") String adminUsername,
                           @Value("${app.seed.admin.password}") String adminPassword,
                           @Value("${app.seed.user.username}") String userUsername,
                           @Value("${app.seed.user.password}") String userPassword) {
        this.usuarioService = usuarioService;
        this.destinoService = destinoService;
        this.destinoRepository = destinoRepository;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.userUsername = userUsername;
        this.userPassword = userPassword;
    }

    @Override
    public void run(String... args) {
        criarUsuariosDeTeste();
        criarDestinosDeExemplo();
    }

    private void criarUsuariosDeTeste() {
        if (!usuarioService.existe(adminUsername)) {
            usuarioService.criar("Administrador", adminUsername, adminPassword, Perfil.ADMIN);
        }
        if (!usuarioService.existe(userUsername)) {
            usuarioService.criar("Usuario Comum", userUsername, userPassword, Perfil.USER);
        }
    }

    private void criarDestinosDeExemplo() {
        if (destinoRepository.count() > 0) {
            return;
        }
        destinoService.cadastrar(new DestinoRequestDTO(
                "Praia do Rosa",
                "Imbituba, Santa Catarina, Brasil",
                "Praia paradisiaca com trilhas, mirantes e vida noturna.",
                List.of("Surf", "Trilha ecologica", "Observacao de baleias"),
                true));

        destinoService.cadastrar(new DestinoRequestDTO(
                "Machu Picchu",
                "Cusco, Peru",
                "Cidadela inca localizada no topo da Cordilheira dos Andes.",
                List.of("Trilha Inca", "Visita guiada", "Fotografia"),
                true));
    }
}
