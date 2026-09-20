package com.agencia.destinosapi.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA que representa um destino de viagem oferecido pela agencia.
 *
 * Diferente da etapa 1 (objetos guardados em um Map em memoria e perdidos ao
 * reiniciar a aplicacao), agora cada Destino e uma linha da tabela "destinos"
 * no PostgreSQL. As atividades turisticas ficam em uma tabela auxiliar
 * ("destino_atividades") e as notas individuais na tabela "avaliacoes".
 *
 * mediaAvaliacao e totalAvaliacoes sao valores derivados da tabela de
 * avaliacoes, mantidos aqui para que a listagem nao precise recalcular a
 * media de todos os destinos a cada consulta.
 */
@Entity
@Table(name = "destinos")
public class Destino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 200)
    private String localizacao;

    @Column(length = 2000)
    private String descricao;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "destino_atividades", joinColumns = @JoinColumn(name = "destino_id"))
    @Column(name = "atividade", nullable = false, length = 150)
    @BatchSize(size = 50)
    private List<String> atividadesTuristicas = new ArrayList<>();

    @Column(nullable = false)
    private boolean disponibilidadeHoteis;

    @Column(name = "media_avaliacao", nullable = false)
    private double mediaAvaliacao = 0.0;

    @Column(name = "total_avaliacoes", nullable = false)
    private int totalAvaliacoes = 0;

    /** Exigido pelo JPA. */
    protected Destino() {
    }

    public Destino(String nome, String localizacao, String descricao,
                   List<String> atividadesTuristicas, boolean disponibilidadeHoteis) {
        this.nome = nome;
        this.localizacao = localizacao;
        this.descricao = descricao;
        this.disponibilidadeHoteis = disponibilidadeHoteis;
        substituirAtividades(atividadesTuristicas);
    }

    /**
     * Substitui a lista de atividades, ignorando itens nulos ou em branco.
     * A lista existente e reaproveitada (clear + add) porque o Hibernate
     * acompanha alteracoes na colecao gerenciada.
     */
    public void substituirAtividades(List<String> novasAtividades) {
        this.atividadesTuristicas.clear();
        if (novasAtividades != null) {
            for (String atividade : novasAtividades) {
                if (atividade != null && !atividade.isBlank()) {
                    this.atividadesTuristicas.add(atividade.trim());
                }
            }
        }
    }

    /** Atualiza os valores derivados das avaliacoes (media e quantidade). */
    public void atualizarAvaliacoes(double media, int total) {
        this.mediaAvaliacao = media;
        this.totalAvaliacoes = total;
    }

    public Long getId() {
        return id;
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

    public boolean isDisponibilidadeHoteis() {
        return disponibilidadeHoteis;
    }

    public void setDisponibilidadeHoteis(boolean disponibilidadeHoteis) {
        this.disponibilidadeHoteis = disponibilidadeHoteis;
    }

    public double getMediaAvaliacao() {
        return mediaAvaliacao;
    }

    public int getTotalAvaliacoes() {
        return totalAvaliacoes;
    }
}
