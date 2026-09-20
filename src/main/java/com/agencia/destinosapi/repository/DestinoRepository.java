package com.agencia.destinosapi.repository;

import com.agencia.destinosapi.model.Destino;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acesso a dados da entidade Destino.
 *
 * Ao estender JpaRepository o Spring gera, em tempo de execucao, toda a
 * implementacao de save, findById, findAll, delete, count etc. Nao ha SQL
 * escrito a mao: a consulta abaixo e derivada do proprio nome do metodo.
 */
@Repository
public interface DestinoRepository extends JpaRepository<Destino, Long> {

    /**
     * Pesquisa por nome E localizacao (contem, sem diferenciar maiusculas).
     * Uma string vazia ("") corresponde a qualquer valor, entao o service
     * usa "" quando o filtro nao foi informado. O Spring Data ja escapa os
     * curingas LIKE (% e _) digitados pelo usuario.
     */
    List<Destino> findByNomeContainingIgnoreCaseAndLocalizacaoContainingIgnoreCase(
            String nome, String localizacao, Sort sort);
}
