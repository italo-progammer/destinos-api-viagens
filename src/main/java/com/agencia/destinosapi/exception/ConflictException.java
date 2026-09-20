package com.agencia.destinosapi.exception;

/**
 * Lancada quando a operacao conflita com o estado atual dos dados
 * (por exemplo, username ja cadastrado). Convertida em HTTP 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String mensagem) {
        super(mensagem);
    }
}
