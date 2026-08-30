package com.agencia.destinosapi.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa um destino de viagem oferecido pela agencia.
 *
 * Nesta primeira versao do sistema, os objetos desta classe sao mantidos
 * apenas em memoria (dentro de um Map, na camada de service), sem persistencia
 * em banco de dados. A estrutura foi desenhada, porem, para que a migracao
 * futura para JPA/Hibernate seja simples (bastaria anotar a classe com
 * @Entity e trocar o "repositorio" em memoria por um JpaRepository).
 */
public class Destino {

    private Long id;
    private String nome;
    private String localizacao;
    private String descricao;
    private List<String> atividadesTuristicas = new ArrayList<>();
    private boolean disponibilidadeHoteis;

    // Controle da media de avaliacoes
    private double mediaAvaliacao;
    private int totalAvaliacoes;

    public Destino() {
    }

    public Destino(Long id, String nome, String localizacao, String descricao,
                   List<String> atividadesTuristicas, boolean disponibilidadeHoteis) {
        this.id = id;
        this.nome = nome;
        this.localizacao = localizacao;
        this.descricao = descricao;
        this.atividadesTuristicas = atividadesTuristicas != null ? atividadesTuristicas : new ArrayList<>();
        this.disponibilidadeHoteis = disponibilidadeHoteis;
        this.mediaAvaliacao = 0.0;
        this.totalAvaliacoes = 0;
    }

    /**
     * Registra uma nova nota de avaliacao e recalcula a media incrementalmente.
     * Formula: novaMedia = ((mediaAtual * totalAtual) + novaNota) / (totalAtual + 1)
     */
    public void registrarAvaliacao(double nota) {
        double somaAtual = this.mediaAvaliacao * this.totalAvaliacoes;
        this.totalAvaliacoes += 1;
        this.mediaAvaliacao = (somaAtual + nota) / this.totalAvaliacoes;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<String> getAtividadesTuristicas() {
        return atividadesTuristicas;
    }

    public void setAtividadesTuristicas(List<String> atividadesTuristicas) {
        this.atividadesTuristicas = atividadesTuristicas;
    }

    public boolean isDisponibilidadeHoteis() {
        return disponibilidadeHoteis;
    }

    public void setDisponibilidadeHoteis(boolean disponibilidadeHoteis) {
        this.disponibilidadeHoteis = disponibilidadeHoteis;
    }

    public double getMediaAvaliacao() {
        return mediaAvaliacao;
    }

    public void setMediaAvaliacao(double mediaAvaliacao) {
        this.mediaAvaliacao = mediaAvaliacao;
    }

    public int getTotalAvaliacoes() {
        return totalAvaliacoes;
    }

    public void setTotalAvaliacoes(int totalAvaliacoes) {
        this.totalAvaliacoes = totalAvaliacoes;
    }
}
