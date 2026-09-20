package com.agencia.destinosapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Nota dada por um usuario a um destino.
 *
 * Relacionamentos:
 *  - muitas avaliacoes -> um Destino  (@ManyToOne)
 *  - muitas avaliacoes -> um Usuario  (@ManyToOne)
 *
 * A constraint unica (usuario, destino) garante no proprio banco que cada
 * usuario tenha no maximo uma avaliacao por destino; avaliar de novo apenas
 * atualiza a nota anterior.
 */
@Entity
@Table(name = "avaliacoes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_avaliacao_usuario_destino",
                columnNames = {"usuario_id", "destino_id"}))
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double nota;

    @Column(name = "data_avaliacao", nullable = false)
    private LocalDateTime dataAvaliacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destino_id", nullable = false)
    private Destino destino;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Exigido pelo JPA. */
    protected Avaliacao() {
    }

    public Avaliacao(Destino destino, Usuario usuario) {
        this.destino = destino;
        this.usuario = usuario;
    }

    public void registrarNota(double nota) {
        this.nota = nota;
        this.dataAvaliacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public double getNota() {
        return nota;
    }

    public LocalDateTime getDataAvaliacao() {
        return dataAvaliacao;
    }

    public Destino getDestino() {
        return destino;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
