
package org.esfe.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import org.esfe.models.Rol;
import org.esfe.models.Usuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final long EXPIRACION = 86400000L;

    private static final String CLAVE_PRUEBA =
            Base64.getEncoder().encodeToString(
                    "ClaveDePruebaJWTSeguraDe32Bytes123456"
                            .getBytes(StandardCharsets.UTF_8)
            );

    private JwtService jwtService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {

        // CREAR SERVICIO JWT
        jwtService = new JwtService(
                CLAVE_PRUEBA,
                EXPIRACION
        );

        // CREAR ROL DE PRUEBA
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre("ROLE_ADMIN");

        // CREAR USUARIO DE PRUEBA
        usuario = new Usuario();
        usuario.setEmail("test@ejemplo.com");
        usuario.setRol(rol);
    }

    // PRUEBA 1: GENERAR Y VALIDAR TOKEN
    @Test
    void testGenerarYValidarToken() {

        String token = jwtService.generarToken(usuario);

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals(
                3,
                token.split("\\.").length
        );

        String emailExtraido =
                jwtService.obtenerEmailDelToken(token);

        assertEquals(
                "test@ejemplo.com",
                emailExtraido
        );

        assertTrue(
                jwtService.esTokenValido(
                        token,
                        usuario.getEmail()
                )
        );
    }

    // PRUEBA 2: RECHAZAR EMAIL DIFERENTE
    @Test
    void testTokenConEmailIncorrecto() {

        String token = jwtService.generarToken(usuario);

        assertFalse(
                jwtService.esTokenValido(
                        token,
                        "otro@ejemplo.com"
                )
        );
    }

    // PRUEBA 3: RECHAZAR TOKEN ALTERADO
    @Test
    void testTokenAlterado() {

        String token = jwtService.generarToken(usuario);

        String[] partes = token.split("\\.");

        // ALTERAR EL PAYLOAD DEL TOKEN
        String payload = partes[1];

        char primerCaracter = payload.charAt(0);

        char nuevoCaracter =
                primerCaracter == 'A' ? 'B' : 'A';

        String payloadAlterado =
                nuevoCaracter + payload.substring(1);

        String tokenAlterado =
                partes[0] + "." +
                        payloadAlterado + "." +
                        partes[2];

        assertThrows(
                JwtException.class,
                () -> jwtService.obtenerEmailDelToken(
                        tokenAlterado
                )
        );
    }

    // PRUEBA 4: RECHAZAR EXPIRACION INVALIDA
    @Test
    void testExpiracionInvalida() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService(
                        CLAVE_PRUEBA,
                        0
                )
        );
    }

    // PRUEBA 5: RECHAZAR CLAVE CORTA
    @Test
    void testClaveCorta() {

        String claveCorta = Base64.getEncoder()
                .encodeToString(
                        "clave-corta".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService(
                        claveCorta,
                        EXPIRACION
                )
        );
    }

    // PRUEBA 6: RECHAZAR TOKEN EXPIRADO
    @Test
    void testTokenExpirado() throws InterruptedException {

        // TOKEN CON EXPIRACION DE 1 SEGUNDO
        JwtService servicioTemporal = new JwtService(
                CLAVE_PRUEBA,
                1000L
        );

        String token = servicioTemporal.generarToken(usuario);

        // ESPERAR A QUE EXPIRE
        Thread.sleep(2100);

        assertThrows(
                ExpiredJwtException.class,
                () -> servicioTemporal.obtenerEmailDelToken(
                        token
                )
        );
    }

    // PRUEBA 7: RECHAZAR USUARIO SIN ROL
    @Test
    void testUsuarioSinRol() {

        usuario.setRol(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.generarToken(usuario)
        );
    }

    // PRUEBA 8: RECHAZAR EMAIL VACIO
    @Test
    void testEmailVacio() {

        usuario.setEmail("");

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.generarToken(usuario)
        );
    }

    // PRUEBA 9: RECHAZAR TOKEN VACIO
    @Test
    void testTokenVacio() {

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.obtenerEmailDelToken("")
        );
    }

    // PRUEBA 10: RECHAZAR EMAIL NULO
    @Test
    void testValidacionConEmailNulo() {

        String token = jwtService.generarToken(usuario);

        assertFalse(
                jwtService.esTokenValido(
                        token,
                        null
                )
        );
    }
}
