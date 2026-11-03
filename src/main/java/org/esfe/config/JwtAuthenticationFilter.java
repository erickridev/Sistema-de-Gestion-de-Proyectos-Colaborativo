
package org.esfe.config;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.esfe.models.Usuario;
import org.esfe.services.UsuarioService;

import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    // INYECCION DE DEPENDENCIAS
    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioService usuarioService
    ) {
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    // OMITIR RUTAS PUBLICAS Y SOLICITUDES OPTIONS
    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        String path = request.getServletPath();

        return HttpMethod.OPTIONS.matches(request.getMethod())
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/login")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    // VALIDAR TOKEN JWT
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // SI NO HAY TOKEN, CONTINUAR
        // SPRING SECURITY DECIDIRA SI LA RUTA ES PUBLICA
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7).trim();

        // VALIDAR TOKEN VACIO
        if (jwt.isEmpty()) {
            enviarError401(response);
            return;
        }

        try {

            // OBTENER EMAIL DEL TOKEN
            String email = jwtService.obtenerEmailDelToken(jwt);

            if (email == null || email.isBlank()) {
                enviarError401(response);
                return;
            }

            // VERIFICAR SI YA EXISTE AUTENTICACION
            if (SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                // BUSCAR USUARIO EN BASE DE DATOS
                Usuario usuario = usuarioService
                        .buscarPorEmail(email)
                        .orElse(null);

                // VALIDAR EXISTENCIA Y ESTADO DEL USUARIO
                if (usuario == null ||
                        !usuario.isEnabled() ||
                        !usuario.isAccountNonLocked() ||
                        !usuario.isAccountNonExpired() ||
                        !usuario.isCredentialsNonExpired()) {

                    enviarError401(response);
                    return;
                }

                // VALIDAR TOKEN
                if (!jwtService.esTokenValido(
                        jwt, usuario.getEmail())) {

                    enviarError401(response);
                    return;
                }

                // OBTENER ROLES DEL USUARIO
                var authorities = usuario.getAuthorities();

                // EVITAR AUTENTICAR USUARIOS SIN ROLES
                if (authorities == null || authorities.isEmpty()) {
                    enviarError401(response);
                    return;
                }

                // CREAR AUTENTICACION
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                usuario,
                                null,
                                authorities
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                // REGISTRAR AUTENTICACION
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }

        } catch (JwtException | IllegalArgumentException ex) {

            // LIMPIAR CONTEXTO DE SEGURIDAD
            SecurityContextHolder.clearContext();

            // TOKEN INVALIDO O EXPIRADO
            enviarError401(response);
            return;
        }

        // CONTINUAR CON LA SOLICITUD
        filterChain.doFilter(request, response);
    }

    // RESPUESTA PARA TOKENS INVALIDOS
    private void enviarError401(
            HttpServletResponse response
    ) throws IOException {

        SecurityContextHolder.clearContext();

        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Token JWT invalido o expirado"
        );
    }
}
