package com.agencia.destinosapi.dto;

import com.agencia.destinosapi.model.Perfil;
import com.agencia.destinosapi.model.Usuario;

/**
 * Dados publicos de um usuario. Nunca inclui a senha (nem o hash).
 */
public record UsuarioResponseDTO(
        Long id,
        String nome,
        String username,
        Perfil perfil
) {

    public static UsuarioResponseDTO de(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getUsername(),
                usuario.getPerfil()
        );
    }
}
