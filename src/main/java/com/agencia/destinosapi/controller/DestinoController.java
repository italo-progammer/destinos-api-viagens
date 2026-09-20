package com.agencia.destinosapi.controller;

import com.agencia.destinosapi.dto.AvaliacaoRequestDTO;
import com.agencia.destinosapi.dto.DestinoRequestDTO;
import com.agencia.destinosapi.dto.DestinoResponseDTO;
import com.agencia.destinosapi.service.DestinoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Endpoints REST de gerenciamento de destinos de viagem.
 *
 * Esta camada NAO contem regra de negocio nem acessa o banco: recebe a
 * requisicao HTTP, delega ao DestinoService e devolve a resposta adequada.
 * Quem pode chamar cada endpoint e definido no SecurityConfig.
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

    /** POST /api/destinos  (ADMIN) */
    @PostMapping
    public ResponseEntity<DestinoResponseDTO> cadastrar(@Valid @RequestBody DestinoRequestDTO dto) {
        DestinoResponseDTO criado = destinoService.cadastrar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    /**
     * GET /api/destinos                    (publico)
     * GET /api/destinos?nome=praia
     * GET /api/destinos?localizacao=bahia
     */
    @GetMapping
    public ResponseEntity<List<DestinoResponseDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String localizacao) {
        return ResponseEntity.ok(destinoService.listar(nome, localizacao));
    }

    /** GET /api/destinos/{id}  (publico) */
    @GetMapping("/{id}")
    public ResponseEntity<DestinoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(destinoService.buscarPorId(id));
    }

    /** PUT /api/destinos/{id}  (ADMIN) */
    @PutMapping("/{id}")
    public ResponseEntity<DestinoResponseDTO> atualizar(@PathVariable Long id,
                                                         @Valid @RequestBody DestinoRequestDTO dto) {
        return ResponseEntity.ok(destinoService.atualizar(id, dto));
    }

    /**
     * Registra a avaliacao do usuario autenticado e recalcula a media do destino.
     * PATCH e usado (em vez de PUT) porque a operacao altera apenas parte do
     * recurso (media e quantidade de avaliacoes), sem substitui-lo.
     *
     * PATCH /api/destinos/{id}/avaliacoes  (USER ou ADMIN)
     */
    @PatchMapping("/{id}/avaliacoes")
    public ResponseEntity<DestinoResponseDTO> avaliar(@PathVariable Long id,
                                                       @Valid @RequestBody AvaliacaoRequestDTO dto,
                                                       Authentication authentication) {
        return ResponseEntity.ok(destinoService.avaliar(id, dto.nota(), authentication.getName()));
    }

    /** DELETE /api/destinos/{id}  (ADMIN) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        destinoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
