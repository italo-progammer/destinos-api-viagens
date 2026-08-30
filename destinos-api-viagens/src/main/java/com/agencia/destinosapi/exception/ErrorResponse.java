package com.agencia.destinosapi.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato padronizado de resposta de erro devolvido pela API,
 * para que todo cliente (app, parceiro comercial, etc.) saiba
 * sempre o que esperar em caso de falha.
 */
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String erro;
    private String mensagem;
    private Map<String, String> camposInvalidos;

    public ErrorResponse(int status, String erro, String mensagem) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
    }

    public ErrorResponse(int status, String erro, String mensagem, Map<String, String> camposInvalidos) {
        this(status, erro, mensagem);
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
