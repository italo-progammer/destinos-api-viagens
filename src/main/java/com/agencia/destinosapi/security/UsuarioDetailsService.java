package com.agencia.destinosapi.security;

import com.agencia.destinosapi.model.Usuario;
import com.agencia.destinosapi.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ponte entre o Spring Security e o banco de dados: dado um username, busca o
 * Usuario na tabela "usuarios" e o converte para UserDetails (usuario, hash da
 * senha e perfil/role). A conferencia da senha e feita pelo Spring Security
 * comparando a senha enviada com o hash BCrypt guardado.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getSenha())
                // roles("ADMIN") vira a authority "ROLE_ADMIN"
                .roles(usuario.getPerfil().name())
                .disabled(!usuario.isAtivo())
                .build();
    }
}
