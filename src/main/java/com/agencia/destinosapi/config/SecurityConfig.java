package com.agencia.destinosapi.config;

import com.agencia.destinosapi.security.RestAccessDeniedHandler;
import com.agencia.destinosapi.security.RestAuthenticationEntryPoint;
import com.agencia.destinosapi.security.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracao central de seguranca da API.
 *
 * Autenticacao : HTTP Basic (usuario e senha no cabecalho Authorization). Os
 *                usuarios vem do banco (UsuarioDetailsService) e a senha e
 *                conferida contra o hash BCrypt.
 * Sessao       : STATELESS - o servidor nao guarda sessao; cada requisicao
 *                precisa trazer as credenciais.
 * CSRF         : desabilitado, pois a API e stateless e nao usa cookies de sessao.
 * Autorizacao  : regras por metodo HTTP + URL, com base no perfil (ADMIN / USER).
 *
 * Matriz de acesso (a primeira regra que casar vence):
 *
 *   POST   /api/auth/registro          publico
 *   GET    /api/destinos, /{id}        publico (consulta)
 *   PATCH  /api/destinos/{id}/avaliacoes   USER ou ADMIN
 *   POST   /api/destinos               ADMIN
 *   PUT    /api/destinos/{id}          ADMIN
 *   DELETE /api/destinos/{id}          ADMIN
 *   qualquer outra rota (ex.: /api/auth/me)   qualquer usuario autenticado
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationProvider authenticationProvider,
                                                   RestAuthenticationEntryPoint authenticationEntryPoint,
                                                   RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(authenticationEntryPoint)   // 401
                        .accessDeniedHandler(accessDeniedHandler))            // 403
                .authorizeHttpRequests(auth -> auth
                        // ---- publico ----
                        .requestMatchers(HttpMethod.POST, "/api/auth/registro").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/destinos", "/api/destinos/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // ---- usuarios autenticados (USER ou ADMIN) ----
                        .requestMatchers(HttpMethod.PATCH, "/api/destinos/*/avaliacoes").hasAnyRole("USER", "ADMIN")
                        // ---- somente ADMIN: operacoes que alteram o catalogo ----
                        .requestMatchers(HttpMethod.POST, "/api/destinos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/destinos/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/destinos/*").hasRole("ADMIN")
                        // ---- tudo o que nao foi listado exige autenticacao ----
                        .anyRequest().authenticated());

        return http.build();
    }

    /** Liga o Spring Security aos usuarios do banco e ao BCrypt. */
    @Bean
    public AuthenticationProvider authenticationProvider(UsuarioDetailsService usuarioDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /** BCrypt: hash com salt aleatorio; o mesmo texto gera hashes diferentes. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
