package com.agencia.destinosapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal responsavel por inicializar a aplicacao Spring Boot.
 *
 * Ao ser executada, sobe um servidor Tomcat embutido (porta padrao 8080)
 * e disponibiliza os endpoints REST definidos nos controllers do pacote
 * com.agencia.destinosapi.controller.
 */
@SpringBootApplication
public class DestinosApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DestinosApiApplication.class, args);
    }

}
