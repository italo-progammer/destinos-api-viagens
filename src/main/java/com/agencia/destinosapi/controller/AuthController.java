package com.agencia.destinosapi.controller;

import com.agencia.destinosapi.dto.RegistroUsuarioDTO;
import com.agencia.destinosapi.dto.UsuarioResponseDTO;
import com.agencia.destinosapi.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de conta de usuario.
 *
 * Como a autenticacao e HTTP Basic, nao existe um endpoint de "login" que
 * devolva token: o cliente envia usuario e senha em cada requisicao. O GET /me
 * serve para conferir se as credenciais estao corretas e qual e o perfil.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** POST /api/auth/registro  (publico) - cria um usuario com perfil USER. */
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody RegistroUsuarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(dto));
    }

    /** GET /api/auth/me  (autenticado) - dados do usuario dono das credenciais enviadas. */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> usuarioAutenticado(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.buscarPorUsername(authentication.getName()));
    }
}
