package com.agencia.destinosapi.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * DTO utilizado para receber os dados de entrada nas operacoes de
 * cadastro (POST) e atualizacao (PUT) de um destino.
 *
 * Usar um DTO em vez de expor a entidade Destino diretamente evita que o
 * cliente da API envie ou dependa de campos que sao controlados internamente,
 * como "id", "mediaAvaliacao" e "totalAvaliacoes".
 */
public class DestinoRequestDTO {

    @NotBlank(message = "O nome do destino e obrigatorio")
    private String nome;

    @NotBlank(message = "A localizacao do destino e obrigatoria")
    private String localizacao;

    private String descricao;

    private List<String> atividadesTuristicas;

    private boolean disponibilidadeHoteis;

    public DestinoRequestDTO() {
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
}
