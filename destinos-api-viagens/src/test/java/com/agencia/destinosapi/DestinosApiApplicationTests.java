package com.agencia.destinosapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Teste basico que garante que o contexto do Spring sobe corretamente,
 * ou seja, que todos os beans (controllers, services) sao carregados
 * sem erro de configuracao.
 */
@SpringBootTest
class DestinosApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
