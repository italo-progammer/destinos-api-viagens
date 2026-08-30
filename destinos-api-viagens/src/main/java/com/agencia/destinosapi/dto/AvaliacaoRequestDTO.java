package com.agencia.destinosapi.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * DTO utilizado para receber a nota de uma nova avaliacao de um destino.
 * A nota deve estar entre 0 e 5, seguindo o padrao comum de avaliacoes
 * (semelhante ao usado por apps de turismo e hospedagem).
 */
public class AvaliacaoRequestDTO {

    @NotNull(message = "A nota da avaliacao e obrigatoria")
    @DecimalMin(value = "0.0", message = "A nota minima e 0.0")
    @DecimalMax(value = "5.0", message = "A nota maxima e 5.0")
    private Double nota;

    public AvaliacaoRequestDTO() {
    }

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }
}
