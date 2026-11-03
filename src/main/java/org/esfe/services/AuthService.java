
package org.esfe.services;

import org.esfe.config.JwtService;

import org.esfe.dtos.AuthRequest;
import org.esfe.dtos.AuthResponse;
import org.esfe.dtos.RegisterRequest;

import org.esfe.models.Rol;
import org.esfe.models.Usuario;

import org.esfe.repositories.RolRepository;
import org.esfe.repositories.UsuarioRepository;

import org.springframework.http.HttpStatus;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // REGISTRO DE USUARIOS
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = request.email().trim().toLowerCase();

        // VERIFICAR CORREO DUPLICADO
        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo electrónico ya está registrado"
            );
        }

        // BUSCAR ROL PREDETERMINADO
        Rol rolUsuario = rolRepository
                .findByNombre("ROLE_USER")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "No existe el rol ROLE_USER"
                ));

        // CREAR USUARIO
        Usuario usuario = new Usuario();

        usuario.setLogin(request.login().trim());
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);

        // CIFRAR CONTRASEÑA
        usuario.setPassword(
                passwordEncoder.encode(request.password())
        );

        // ASIGNAR ROL SEGURO
        usuario.setRol(rolUsuario);

        // GUARDAR EN MYSQL
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // GENERAR JWT
        String token = jwtService.generarToken(usuarioGuardado);

        return new AuthResponse(token);
    }

    // INICIO DE SESION
    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        // AUTENTICAR USUARIO
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        // BUSCAR USUARIO AUTENTICADO
        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Credenciales inválidas"
                ));

        // GENERAR JWT
        String token = jwtService.generarToken(usuario);

        return new AuthResponse(token);
    }
}
