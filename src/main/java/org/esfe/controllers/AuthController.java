
package org.esfe.controllers;

import jakarta.validation.Valid;

import org.esfe.dtos.AuthRequest;
import org.esfe.dtos.AuthResponse;
import org.esfe.dtos.RegisterRequest;

import org.esfe.services.AuthService;
import org.esfe.models.Usuario;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // INYECCION DE DEPENDENCIAS
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // REGISTRAR NUEVO USUARIO
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse respuesta = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    // INICIAR SESION
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request) {

        AuthResponse respuesta = authService.login(request);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/me")
    public CurrentUser me(@AuthenticationPrincipal Usuario usuario) {
        return new CurrentUser(usuario.getId(), usuario.getLogin(), usuario.getNombre(), usuario.getEmail(),
                usuario.getAuthorities().stream().map(authority -> authority.getAuthority()).toList());
    }

    public record CurrentUser(Long id, String login, String nombre, String email, java.util.List<String> authorities) {}
}
