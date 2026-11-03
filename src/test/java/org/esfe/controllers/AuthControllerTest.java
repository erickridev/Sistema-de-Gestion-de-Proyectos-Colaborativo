
package org.esfe.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.esfe.config.JwtAuthenticationFilter;
import org.esfe.dtos.AuthRequest;
import org.esfe.dtos.AuthResponse;
import org.esfe.dtos.RegisterRequest;
import org.esfe.services.AuthService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // PRUEBA 1: LOGIN EXITOSO
    @Test
    void testLoginExitoso() throws Exception {

        AuthRequest request = new AuthRequest(
                "admin@ejemplo.com",
                "ClaveSegura123"
        );

        AuthResponse respuesta =
                new AuthResponse("mocked-jwt-token");

        when(authService.login(any(AuthRequest.class)))
                .thenReturn(respuesta);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.token")
                                .value("mocked-jwt-token")
                );

        verify(authService, times(1))
                .login(any(AuthRequest.class));
    }

    // PRUEBA 2: REGISTRO EXITOSO
    @Test
    void testRegistroExitoso() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "usuario01",
                "Usuario de Prueba",
                "usuario@ejemplo.com",
                "ClaveSegura123"
        );

        AuthResponse respuesta =
                new AuthResponse("token-registro");

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(respuesta);

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.token")
                                .value("token-registro")
                );

        verify(authService, times(1))
                .register(any(RegisterRequest.class));
    }

    // PRUEBA 3: REGISTRO CON NOMBRE VACIO
    @Test
    void testRegistroConNombreVacio() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "usuario01",
                "",
                "usuario@ejemplo.com",
                "ClaveSegura123"
        );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    // PRUEBA 4: REGISTRO CON CORREO INVALIDO
    @Test
    void testRegistroConCorreoInvalido() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "usuario01",
                "Usuario de Prueba",
                "correo-invalido",
                "ClaveSegura123"
        );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    // PRUEBA 5: REGISTRO CON PASSWORD CORTO
    @Test
    void testRegistroConPasswordCorto() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "usuario01",
                "Usuario de Prueba",
                "usuario@ejemplo.com",
                "123"
        );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    // PRUEBA 6: LOGIN SIN PASSWORD
    @Test
    void testLoginSinPassword() throws Exception {

        AuthRequest request = new AuthRequest(
                "admin@ejemplo.com",
                ""
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}
