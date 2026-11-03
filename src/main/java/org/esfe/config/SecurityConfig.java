
package org.esfe.config;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    // INYECCION DE DEPENDENCIAS
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthFilter,
            UserDetailsService userDetailsService
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    // CONFIGURACION DE SEGURIDAD
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        http

                // DESACTIVAR CSRF PARA API REST CON JWT
                .csrf(AbstractHttpConfigurer::disable)

                // NO UTILIZAR SESIONES HTTP
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // CONFIGURACION DE PERMISOS
                .authorizeHttpRequests(auth -> auth

                        // SOLICITUDES OPTIONS
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // SWAGGER
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // REGISTRO E INICIO DE SESION
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // CONSULTAR CATEGORIAS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categorias",
                                "/api/categorias/**"
                        ).hasAnyRole("ADMIN", "USER")

                        // ADMINISTRAR CATEGORIAS
                        .requestMatchers(
                                "/api/categorias",
                                "/api/categorias/**"
                        ).hasRole("ADMIN")

                        // CONSULTAR PROYECTOS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/proyectos",
                                "/api/proyectos/**"
                        ).hasAnyRole("ADMIN", "USER")

                        // ADMINISTRAR PROYECTOS
                        .requestMatchers(
                                "/api/proyectos",
                                "/api/proyectos/**"
                        ).hasRole("ADMIN")

                        // CONSULTAR TAREAS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/tareas",
                                "/api/tareas/**"
                        ).hasAnyRole("ADMIN", "USER")

                        // ADMINISTRAR TAREAS
                        .requestMatchers(
                                "/api/tareas",
                                "/api/tareas/**"
                        ).hasRole("ADMIN")

                        // ADMINISTRAR USUARIOS
                        .requestMatchers(
                                "/api/usuarios",
                                "/api/usuarios/**"
                        ).hasRole("ADMIN")

                        // ADMINISTRAR ROLES
                        .requestMatchers(
                                "/api/roles",
                                "/api/roles/**"
                        ).hasRole("ADMIN")

                        // RESTO DE ENDPOINTS
                        .requestMatchers("/api/**").authenticated()

                        // DENEGAR OTRAS RUTAS NO CONFIGURADAS
                        .anyRequest().denyAll()
                )

                // ERRORES DE AUTENTICACION Y AUTORIZACION
                .exceptionHandling(exception -> exception

                        // ERROR 401
                        .authenticationEntryPoint(
                                (request, response, authException) ->
                                        response.sendError(
                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                "Autenticacion requerida"
                                        )
                        )

                        // ERROR 403
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) ->
                                        response.sendError(
                                                HttpServletResponse.SC_FORBIDDEN,
                                                "No tiene permisos para realizar esta accion"
                                        )
                        )
                )

                // PROVEEDOR DE AUTENTICACION
                .authenticationProvider(authenticationProvider())

                // FILTRO JWT
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // PROVEEDOR DE AUTENTICACION
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    // ADMINISTRADOR DE AUTENTICACION
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {

        return config.getAuthenticationManager();
    }

    // CIFRADO DE CONTRASEÑAS
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
