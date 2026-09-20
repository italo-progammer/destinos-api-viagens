package com.agencia.destinosapi.exception;

/**
 * Lancada quando um recurso solicitado (por id) nao existe.
 * E convertida em HTTP 404 pelo GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensagem) {
        super(mensagem);
    }
}
