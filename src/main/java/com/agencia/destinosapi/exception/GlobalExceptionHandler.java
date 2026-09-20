package com.agencia.destinosapi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Intercepta as excecoes lancadas pelos controllers e as transforma em
 * respostas HTTP consistentes (mesmo formato JSON: ErrorResponse), evitando
 * que o cliente receba stack traces do Java.
 *
 * Erros causados pelo cliente (dados invalidos, rota inexistente, id
 * malformado...) devolvem 4xx; apenas falhas realmente inesperadas devolvem 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return montar(HttpStatus.NOT_FOUND, "Recurso nao encontrado", ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleRotaInexistente(NoResourceFoundException ex) {
        return montar(HttpStatus.NOT_FOUND, "Recurso nao encontrado", "A rota informada nao existe");
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return montar(HttpStatus.CONFLICT, "Conflito", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegridade(DataIntegrityViolationException ex) {
        log.warn("Violacao de integridade no banco: {}", ex.getMostSpecificCause().getMessage());
        return montar(HttpStatus.CONFLICT, "Conflito",
                "A operacao viola uma regra de integridade dos dados (registro duplicado ou em uso)");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            campos.merge(fieldError.getField(), String.valueOf(fieldError.getDefaultMessage()),
                    (atual, nova) -> atual + "; " + nova);
        }
        ErrorResponse erro = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Dados invalidos",
                "Um ou mais campos enviados sao invalidos",
                campos
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleCorpoIlegivel(HttpMessageNotReadableException ex) {
        return montar(HttpStatus.BAD_REQUEST, "Requisicao invalida",
                "O corpo da requisicao esta ausente ou nao e um JSON valido");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return montar(HttpStatus.BAD_REQUEST, "Requisicao invalida",
                "O valor informado para '" + ex.getName() + "' e invalido");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex) {
        return montar(HttpStatus.METHOD_NOT_ALLOWED, "Metodo nao permitido",
                "O metodo HTTP " + ex.getMethod() + " nao e suportado nesta rota");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNaoSuportado(HttpMediaTypeNotSupportedException ex) {
        return montar(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de conteudo nao suportado",
                "Envie o corpo da requisicao como application/json");
    }

    /**
     * Sem este handler, o "catch-all" abaixo transformaria um acesso negado em 500.
     * (As regras por URL do SecurityConfig respondem 403 antes de chegar aqui, mas
     * este metodo protege caso alguem passe a usar @PreAuthorize nos controllers.)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAcessoNegado(AccessDeniedException ex) {
        return montar(HttpStatus.FORBIDDEN, "Acesso negado",
                "Voce nao tem permissao para executar esta operacao");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Erro inesperado ao processar a requisicao", ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado ao processar a requisicao");
    }

    private ResponseEntity<ErrorResponse> montar(HttpStatus status, String erro, String mensagem) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), erro, mensagem));
    }
}
