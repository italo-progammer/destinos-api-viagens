package com.agencia.destinosapi.controller;

import com.agencia.destinosapi.dto.AvaliacaoRequestDTO;
import com.agencia.destinosapi.dto.DestinoRequestDTO;
import com.agencia.destinosapi.model.Destino;
import com.agencia.destinosapi.service.DestinoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST responsavel por expor os endpoints de gerenciamento de
 * destinos de viagem. Esta camada NAO contem regra de negocio: apenas
 * recebe a requisicao HTTP, delega para o DestinoService e devolve a
 * resposta HTTP adequada (status code + corpo).
 *
 * Prefixo de rota: /api/destinos
 */
@RestController
@RequestMapping("/api/destinos")
public class DestinoController {

    private final DestinoService destinoService;

    public DestinoController(DestinoService destinoService) {
        this.destinoService = destinoService;
    }

    /**
     * Cadastra um novo destino de viagem.
     * POST /api/destinos
     */
    @PostMapping
    public ResponseEntity<Destino> cadastrar(@Valid @RequestBody DestinoRequestDTO dto) {
        Destino novoDestino = destinoService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoDestino);
    }

    /**
     * Lista todos os destinos, com suporte opcional a pesquisa por
     * nome e/ou localizacao via query params.
     * GET /api/destinos
     * GET /api/destinos?nome=praia
     * GET /api/destinos?localizacao=bahia
     */
    @GetMapping
    public ResponseEntity<List<Destino>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String localizacao) {
        return ResponseEntity.ok(destinoService.listar(nome, localizacao));
    }

    /**
     * Retorna os detalhes de um destino especifico.
     * GET /api/destinos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Destino> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(destinoService.buscarPorId(id));
    }

    /**
     * Atualiza os dados de um destino existente.
     * PUT /api/destinos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Destino> atualizar(@PathVariable Long id,
                                              @Valid @RequestBody DestinoRequestDTO dto) {
        return ResponseEntity.ok(destinoService.atualizar(id, dto));
    }

    /**
     * Registra uma nova avaliacao para o destino, recalculando sua media.
     * PATCH e usado (em vez de PUT) porque a operacao altera parcialmente
     * o recurso (apenas a media/quantidade de avaliacoes), sem substitui-lo
     * por completo.
     * PATCH /api/destinos/{id}/avaliacoes
     */
    @PatchMapping("/{id}/avaliacoes")
    public ResponseEntity<Destino> avaliar(@PathVariable Long id,
                                            @Valid @RequestBody AvaliacaoRequestDTO dto) {
        return ResponseEntity.ok(destinoService.avaliar(id, dto.getNota()));
    }

    /**
     * Remove um destino do sistema.
     * DELETE /api/destinos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        destinoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
