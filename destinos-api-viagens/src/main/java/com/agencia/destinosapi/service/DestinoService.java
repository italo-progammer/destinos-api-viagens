package com.agencia.destinosapi.service;

import com.agencia.destinosapi.dto.DestinoRequestDTO;
import com.agencia.destinosapi.exception.ResourceNotFoundException;
import com.agencia.destinosapi.model.Destino;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Camada de servico (regra de negocio) do sistema de destinos.
 *
 * Concentra toda a logica de negocio da aplicacao, mantendo o controller
 * enxuto (responsavel apenas por expor a rota HTTP e traduzir requisicao/
 * resposta) e a entidade Destino livre de logica de persistencia.
 *
 * Armazenamento: nesta primeira versao os dados sao mantidos em memoria,
 * atraves de um ConcurrentHashMap, simulando um repositorio. Essa escolha
 * atende ao escopo do desafio (nao ha exigencia de banco de dados) e, ao
 * mesmo tempo, isola o "acesso a dados" de forma que, futuramente, essa
 * classe possa passar a delegar as operacoes para um Spring Data
 * JpaRepository sem que o Controller precise ser alterado.
 */
@Service
public class DestinoService {

    private final Map<Long, Destino> destinos = new ConcurrentHashMap<>();
    private final AtomicLong sequenciaId = new AtomicLong(0);

    /**
     * Cadastra um novo destino de viagem.
     */
    public Destino cadastrar(DestinoRequestDTO dto) {
        Long novoId = sequenciaId.incrementAndGet();
        Destino destino = new Destino(
                novoId,
                dto.getNome(),
                dto.getLocalizacao(),
                dto.getDescricao(),
                dto.getAtividadesTuristicas(),
                dto.isDisponibilidadeHoteis()
        );
        destinos.put(novoId, destino);
        return destino;
    }

    /**
     * Lista todos os destinos cadastrados. Se "nome" ou "localizacao" forem
     * informados (nao nulos/nao vazios), filtra o resultado, atendendo tanto
     * ao caso de "listar todos" quanto ao caso de "pesquisar por nome ou
     * localizacao" com um unico metodo de leitura.
     */
    public List<Destino> listar(String nome, String localizacao) {
        return destinos.values().stream()
                .filter(d -> nome == null || nome.isBlank()
                        || d.getNome().toLowerCase().contains(nome.toLowerCase()))
                .filter(d -> localizacao == null || localizacao.isBlank()
                        || d.getLocalizacao().toLowerCase().contains(localizacao.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Busca um destino pelo id. Lanca ResourceNotFoundException caso nao exista.
     */
    public Destino buscarPorId(Long id) {
        Destino destino = destinos.get(id);
        if (destino == null) {
            throw new ResourceNotFoundException("Destino com id " + id + " nao foi encontrado");
        }
        return destino;
    }

    /**
     * Atualiza os dados de um destino existente.
     */
    public Destino atualizar(Long id, DestinoRequestDTO dto) {
        Destino destino = buscarPorId(id);
        destino.setNome(dto.getNome());
        destino.setLocalizacao(dto.getLocalizacao());
        destino.setDescricao(dto.getDescricao());
        destino.setAtividadesTuristicas(dto.getAtividadesTuristicas());
        destino.setDisponibilidadeHoteis(dto.isDisponibilidadeHoteis());
        return destino;
    }

    /**
     * Registra uma nova avaliacao para o destino, recalculando sua media.
     */
    public Destino avaliar(Long id, double nota) {
        Destino destino = buscarPorId(id);
        destino.registrarAvaliacao(nota);
        return destino;
    }

    /**
     * Remove um destino do sistema.
     */
    public void excluir(Long id) {
        Destino destino = buscarPorId(id);
        destinos.remove(destino.getId());
    }
}
