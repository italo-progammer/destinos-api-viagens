package com.agencia.destinosapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Garante que o contexto do Spring sobe corretamente: beans, entidades JPA
 * (o Hibernate valida o mapeamento ao criar as tabelas) e configuracao de
 * seguranca. Usa o perfil "test" (H2 em memoria).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DestinosApiApplicationTests {

    @Test
    void contextLoads() {
    }
}
