package com.agencia.destinosapi.exception;

/**
 * Excecao lancada quando um destino solicitado (por id) nao existe.
 * E capturada pelo GlobalExceptionHandler e convertida em uma resposta
 * HTTP 404 (Not Found) com uma mensagem amigavel.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensagem) {
        super(mensagem);
    }
}
