package com.agencia.destinosapi.service;

import com.agencia.destinosapi.dto.DestinoRequestDTO;
import com.agencia.destinosapi.dto.DestinoResponseDTO;
import com.agencia.destinosapi.exception.ResourceNotFoundException;
import com.agencia.destinosapi.model.Avaliacao;
import com.agencia.destinosapi.model.Destino;
import com.agencia.destinosapi.model.Usuario;
import com.agencia.destinosapi.repository.AvaliacaoRepository;
import com.agencia.destinosapi.repository.DestinoRepository;
import com.agencia.destinosapi.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Camada de servico (regras de negocio) dos destinos.
 *
 * Na etapa 1 esta classe guardava os destinos em um ConcurrentHashMap (memoria
 * da JVM: tudo se perdia ao reiniciar a aplicacao). Agora ela delega o
 * armazenamento aos repositories do Spring Data JPA, e os dados ficam no
 * PostgreSQL. O Controller continua sem saber disso: a separacao em camadas
 * permitiu trocar a persistencia sem alterar a camada web.
 *
 * Cada metodo publico e uma transacao (@Transactional): ou tudo e gravado, ou
 * nada e (ex.: excluir um destino remove suas avaliacoes e o proprio destino
 * de forma atomica).
 */
@Service
public class DestinoService {

    private final DestinoRepository destinoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public DestinoService(DestinoRepository destinoRepository,
                          AvaliacaoRepository avaliacaoRepository,
                          UsuarioRepository usuarioRepository) {
        this.destinoRepository = destinoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public DestinoResponseDTO cadastrar(DestinoRequestDTO dto) {
        Destino destino = new Destino(
                dto.nome().trim(),
                dto.localizacao().trim(),
                dto.descricao(),
                dto.atividadesTuristicas(),
                dto.disponibilidadeHoteis()
        );
        return DestinoResponseDTO.de(destinoRepository.save(destino));
    }

    /**
     * Lista todos os destinos. Se "nome" e/ou "localizacao" forem informados,
     * filtra o resultado (contem, sem diferenciar maiusculas). Atende tanto
     * "listar todos" quanto "pesquisar" com um unico metodo de leitura.
     */
    @Transactional(readOnly = true)
    public List<DestinoResponseDTO> listar(String nome, String localizacao) {
        return destinoRepository
                .findByNomeContainingIgnoreCaseAndLocalizacaoContainingIgnoreCase(
                        filtro(nome), filtro(localizacao), Sort.by("nome"))
                .stream()
                .map(DestinoResponseDTO::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public DestinoResponseDTO buscarPorId(Long id) {
        return DestinoResponseDTO.de(buscarEntidade(id));
    }

    @Transactional
    public DestinoResponseDTO atualizar(Long id, DestinoRequestDTO dto) {
        Destino destino = buscarEntidade(id);
        destino.setNome(dto.nome().trim());
        destino.setLocalizacao(dto.localizacao().trim());
        destino.setDescricao(dto.descricao());
        destino.substituirAtividades(dto.atividadesTuristicas());
        destino.setDisponibilidadeHoteis(dto.disponibilidadeHoteis());
        // A entidade e "gerenciada": o Hibernate detecta as alteracoes e faz o
        // UPDATE automaticamente ao final da transacao (dirty checking).
        return DestinoResponseDTO.de(destino);
    }

    /**
     * Registra a avaliacao do usuario autenticado para o destino e recalcula a
     * media. Se o usuario ja avaliou este destino, a nota anterior e substituida
     * (um usuario, uma avaliacao por destino).
     */
    @Transactional
    public DestinoResponseDTO avaliar(Long destinoId, double nota, String username) {
        Destino destino = buscarEntidade(destinoId);
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario '" + username + "' nao foi encontrado"));

        Avaliacao avaliacao = avaliacaoRepository
                .findByDestinoIdAndUsuarioId(destinoId, usuario.getId())
                .orElseGet(() -> new Avaliacao(destino, usuario));
        avaliacao.registrarNota(nota);
        avaliacaoRepository.saveAndFlush(avaliacao);

        Double media = avaliacaoRepository.calcularMedia(destinoId);
        long total = avaliacaoRepository.countByDestinoId(destinoId);
        destino.atualizarAvaliacoes(arredondar(media == null ? 0.0 : media), (int) total);
        return DestinoResponseDTO.de(destino);
    }

    @Transactional
    public void excluir(Long id) {
        Destino destino = buscarEntidade(id);
        // As avaliacoes referenciam o destino (chave estrangeira), entao saem primeiro.
        avaliacaoRepository.excluirPorDestinoId(id);
        destinoRepository.delete(destino);
    }

    private Destino buscarEntidade(Long id) {
        return destinoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destino com id " + id + " nao foi encontrado"));
    }

    /** Filtro ausente vira "", que casa com qualquer valor no "contem". */
    private String filtro(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
