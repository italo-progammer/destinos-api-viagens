package com.agencia.destinosapi.config;

import com.agencia.destinosapi.dto.DestinoRequestDTO;
import com.agencia.destinosapi.service.DestinoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Popula a API com alguns destinos de exemplo assim que a aplicacao sobe,
 * apenas para facilitar os testes manuais (ex: via Postman ou Insomnia)
 * sem precisar cadastrar tudo do zero. Nao e obrigatorio para o
 * funcionamento da API e pode ser removido sem impacto.
 */
@Configuration
public class DataInitializer implements CommandLineRunner {

    private final DestinoService destinoService;

    public DataInitializer(DestinoService destinoService) {
        this.destinoService = destinoService;
    }

    @Override
    public void run(String... args) {
        DestinoRequestDTO praia = new DestinoRequestDTO();
        praia.setNome("Praia do Rosa");
        praia.setLocalizacao("Imbituba, Santa Catarina, Brasil");
        praia.setDescricao("Praia paradisiaca com trilhas, mirantes e vida noturna.");
        praia.setAtividadesTuristicas(List.of("Surf", "Trilha ecologica", "Observacao de baleias"));
        praia.setDisponibilidadeHoteis(true);
        destinoService.cadastrar(praia);

        DestinoRequestDTO montanha = new DestinoRequestDTO();
        montanha.setNome("Machu Picchu");
        montanha.setLocalizacao("Cusco, Peru");
        montanha.setDescricao("Cidadela inca localizada no topo da Cordilheira dos Andes.");
        montanha.setAtividadesTuristicas(List.of("Trilha Inca", "Visita guiada", "Fotografia"));
        montanha.setDisponibilidadeHoteis(true);
        destinoService.cadastrar(montanha);
    }
}
