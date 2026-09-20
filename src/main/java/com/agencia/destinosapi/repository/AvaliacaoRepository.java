package com.agencia.destinosapi.repository;

import com.agencia.destinosapi.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findByDestinoIdAndUsuarioId(Long destinoId, Long usuarioId);

    long countByDestinoId(Long destinoId);

    /** Media das notas de um destino (null quando ainda nao ha avaliacoes). */
    @Query("select avg(a.nota) from Avaliacao a where a.destino.id = :destinoId")
    Double calcularMedia(@Param("destinoId") Long destinoId);

    /** Remove todas as avaliacoes de um destino (usado antes de exclui-lo). */
    @Modifying
    @Query("delete from Avaliacao a where a.destino.id = :destinoId")
    void excluirPorDestinoId(@Param("destinoId") Long destinoId);
}
