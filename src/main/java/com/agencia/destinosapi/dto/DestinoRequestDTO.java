package com.agencia.destinosapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Dados de entrada para cadastrar (POST) ou atualizar (PUT) um destino.
 *
 * Usar um DTO em vez de expor a entidade evita que o cliente envie ou dependa
 * de campos controlados internamente, como "id", "mediaAvaliacao" e
 * "totalAvaliacoes".
 */
public record DestinoRequestDTO(

        @NotBlank(message = "O nome do destino e obrigatorio")
        @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres")
        String nome,

        @NotBlank(message = "A localizacao do destino e obrigatoria")
        @Size(max = 200, message = "A localizacao deve ter no maximo 200 caracteres")
        String localizacao,

        @Size(max = 2000, message = "A descricao deve ter no maximo 2000 caracteres")
        String descricao,

        List<String> atividadesTuristicas,

        boolean disponibilidadeHoteis
) {
}
