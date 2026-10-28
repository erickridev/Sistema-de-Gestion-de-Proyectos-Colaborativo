package org.esfe.services;

import org.esfe.models.Usuario;
import org.esfe.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("usuario@ejemplo.com");
    }

    @Test
    void testLoadUserByUsernameExitoso() {
        when(usuarioRepository.findByEmail("usuario@ejemplo.com")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = usuarioService.loadUserByUsername("usuario@ejemplo.com");

        assertNotNull(userDetails);
        assertEquals("usuario@ejemplo.com", userDetails.getUsername());
        verify(usuarioRepository, times(1)).findByEmail("usuario@ejemplo.com");
    }

    @Test
    void testLoadUserByUsernameNoEncontrado() {
        when(usuarioRepository.findByEmail("noexiste@ejemplo.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            usuarioService.loadUserByUsername("noexiste@ejemplo.com");
        });
    }
}