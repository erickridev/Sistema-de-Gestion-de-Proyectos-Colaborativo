package org.esfe.config;

import org.esfe.models.Rol;
import org.esfe.models.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre("ADMIN");

        usuario = new Usuario();
        usuario.setEmail("test@ejemplo.com");
        usuario.setPassword("password123");
        usuario.setRol(rol);
    }

    @Test
    void testGenerarYValidarToken() {
        String token = jwtService.generarToken(usuario);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        String emailExtraido = jwtService.obtenerEmailDelToken(token);
        assertEquals("test@ejemplo.com", emailExtraido);

        boolean esValido = jwtService.esTokenValido(token, usuario.getEmail());
        assertTrue(esValido);
    }
}
