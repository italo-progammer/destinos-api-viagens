package com.agencia.destinosapi.dto;

import com.agencia.destinosapi.model.Destino;

import java.util.ArrayList;
import java.util.List;

/**
 * Dados de saida de um destino. Separa o que a API devolve da entidade JPA
 * (que pode ter colecoes lazy, relacionamentos etc.).
 */
public record DestinoResponseDTO(
        Long id,
        String nome,
        String localizacao,
        String descricao,
        List<String> atividadesTuristicas,
        boolean disponibilidadeHoteis,
        double mediaAvaliacao,
        int totalAvaliacoes
) {

    public static DestinoResponseDTO de(Destino destino) {
        return new DestinoResponseDTO(
                destino.getId(),
                destino.getNome(),
                destino.getLocalizacao(),
                destino.getDescricao(),
                new ArrayList<>(destino.getAtividadesTuristicas()),
                destino.isDisponibilidadeHoteis(),
                destino.getMediaAvaliacao(),
                destino.getTotalAvaliacoes()
        );
    }
}
