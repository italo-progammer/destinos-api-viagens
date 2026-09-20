package com.agencia.destinosapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Usuario da API. E a entidade consultada pelo Spring Security no momento da
 * autenticacao (veja UsuarioDetailsService).
 *
 * A coluna "senha" guarda apenas o HASH BCrypt da senha, nunca o texto puro.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** Hash BCrypt da senha (60 caracteres). */
    @Column(nullable = false, length = 100)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    @Column(nullable = false)
    private boolean ativo = true;

    /** Exigido pelo JPA. */
    protected Usuario() {
    }

    public Usuario(String nome, String username, String senhaCriptografada, Perfil perfil) {
        this.nome = nome;
        this.username = username;
        this.senha = senhaCriptografada;
        this.perfil = perfil;
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getUsername() {
        return username;
    }

    public String getSenha() {
        return senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
