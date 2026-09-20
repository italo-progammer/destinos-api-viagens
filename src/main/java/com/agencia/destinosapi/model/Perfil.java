package com.agencia.destinosapi.model;

/**
 * Perfis de acesso da API.
 *
 * ADMIN: gerencia o catalogo (cadastrar, atualizar e excluir destinos) e tambem
 *        pode avaliar destinos.
 * USER : usuario autenticado comum; pode consultar e avaliar destinos.
 *
 * No Spring Security cada perfil vira a "role" ROLE_ADMIN / ROLE_USER.
 */
public enum Perfil {
    ADMIN,
    USER
}
