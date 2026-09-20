package com.agencia.destinosapi.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato padronizado de resposta de erro devolvido pela API (inclusive pelos
 * erros de seguranca 401/403), para que todo cliente saiba sempre o que esperar.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String erro;
    private final String mensagem;
    private final Map<String, String> camposInvalidos;

    public ErrorResponse(int status, String erro, String mensagem) {
        this(status, erro, mensagem, null);
    }

    public ErrorResponse(int status, String erro, String mensagem, Map<String, String> camposInvalidos) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.camposInvalidos = camposInvalidos;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getErro() {
        return erro;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Map<String, String> getCamposInvalidos() {
        return camposInvalidos;
    }
}
