package com.agencia.destinosapi.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Nota de uma avaliacao. Deve estar entre 0 e 5, seguindo o padrao comum de
 * avaliacoes de apps de turismo e hospedagem.
 */
public record AvaliacaoRequestDTO(

        @NotNull(message = "A nota da avaliacao e obrigatoria")
        @DecimalMin(value = "0.0", message = "A nota minima e 0.0")
        @DecimalMax(value = "5.0", message = "A nota maxima e 5.0")
        Double nota
) {
}
