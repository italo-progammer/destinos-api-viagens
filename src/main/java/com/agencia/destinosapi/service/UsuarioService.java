package com.agencia.destinosapi.service;

import com.agencia.destinosapi.dto.RegistroUsuarioDTO;
import com.agencia.destinosapi.dto.UsuarioResponseDTO;
import com.agencia.destinosapi.exception.ConflictException;
import com.agencia.destinosapi.exception.ResourceNotFoundException;
import com.agencia.destinosapi.model.Perfil;
import com.agencia.destinosapi.model.Usuario;
import com.agencia.destinosapi.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio dos usuarios. A senha e criptografada (BCrypt) aqui,
 * antes de chegar ao banco: o texto puro nunca e armazenado.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Auto-cadastro: o novo usuario sempre recebe o perfil USER. */
    @Transactional
    public UsuarioResponseDTO registrar(RegistroUsuarioDTO dto) {
        return criar(dto.nome(), dto.username(), dto.senha(), Perfil.USER);
    }

    /** Cria um usuario com o perfil informado (usado tambem pelo DataInitializer). */
    @Transactional
    public UsuarioResponseDTO criar(String nome, String username, String senha, Perfil perfil) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new ConflictException("O username '" + username + "' ja esta em uso");
        }
        Usuario usuario = new Usuario(nome, username, passwordEncoder.encode(senha), perfil);
        return UsuarioResponseDTO.de(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .map(UsuarioResponseDTO::de)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario '" + username + "' nao foi encontrado"));
    }

    @Transactional(readOnly = true)
    public boolean existe(String username) {
        return usuarioRepository.existsByUsername(username);
    }
}
