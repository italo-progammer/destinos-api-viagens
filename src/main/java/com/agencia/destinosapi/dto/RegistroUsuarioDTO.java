package com.agencia.destinosapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dados para o auto-cadastro de um novo usuario. Note que o perfil NAO e
 * informado pelo cliente: todo usuario registrado por este fluxo recebe o
 * perfil USER, o que impede que alguem se cadastre como ADMIN.
 */
public record RegistroUsuarioDTO(

        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 100, message = "O nome deve ter no maximo 100 caracteres")
        String nome,

        @NotBlank(message = "O username e obrigatorio")
        @Pattern(regexp = "^[a-z0-9._-]{3,50}$",
                message = "O username deve ter de 3 a 50 caracteres: letras minusculas, numeros, ponto, hifen ou underline")
        String username,

        @NotBlank(message = "A senha e obrigatoria")
        @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres")
        String senha
) {
}
