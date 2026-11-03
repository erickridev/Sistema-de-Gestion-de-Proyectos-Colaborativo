
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

import java.util.List;
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
        usuario.setLogin("usuario01");
        usuario.setNombre("Usuario de Prueba");
        usuario.setEmail("usuario@ejemplo.com");
    }

    // PRUEBA 1: CARGAR USUARIO EXISTENTE
    @Test
    void testLoadUserByUsernameExitoso() {

        when(usuarioRepository.findByEmail("usuario@ejemplo.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails resultado =
                usuarioService.loadUserByUsername(
                        "usuario@ejemplo.com"
                );

        assertNotNull(resultado);

        assertEquals(
                "usuario@ejemplo.com",
                resultado.getUsername()
        );

        verify(usuarioRepository).findByEmail(
                "usuario@ejemplo.com"
        );
    }

    // PRUEBA 2: CARGAR USUARIO INEXISTENTE
    @Test
    void testLoadUserByUsernameNoEncontrado() {

        when(usuarioRepository.findByEmail("noexiste@ejemplo.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> usuarioService.loadUserByUsername(
                        "noexiste@ejemplo.com"
                )
        );

        verify(usuarioRepository).findByEmail(
                "noexiste@ejemplo.com"
        );
    }

    // PRUEBA 3: VERIFICAR USUARIO DEVUELTO
    @Test
    void testLoadUserByUsernameDevuelveUsuarioCorrecto() {

        when(usuarioRepository.findByEmail("usuario@ejemplo.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails resultado =
                usuarioService.loadUserByUsername(
                        "usuario@ejemplo.com"
                );

        assertSame(usuario, resultado);

        verify(usuarioRepository).findByEmail(
                "usuario@ejemplo.com"
        );
    }

    // PRUEBA 4: LISTAR TODOS LOS USUARIOS
    @Test
    void testListarTodos() {

        when(usuarioRepository.findAll())
                .thenReturn(List.of(usuario));

        List<Usuario> resultado =
                usuarioService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        assertEquals(
                "usuario@ejemplo.com",
                resultado.get(0).getEmail()
        );

        verify(usuarioRepository).findAll();
    }

    // PRUEBA 5: LISTAR USUARIOS VACIO
    @Test
    void testListarTodosVacio() {

        when(usuarioRepository.findAll())
                .thenReturn(List.of());

        List<Usuario> resultado =
                usuarioService.listarTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(usuarioRepository).findAll();
    }

    // PRUEBA 6: BUSCAR USUARIO POR ID
    @Test
    void testBuscarPorIdExitoso() {

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado =
                usuarioService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());

        verify(usuarioRepository).findById(1L);
    }

    // PRUEBA 7: BUSCAR USUARIO INEXISTENTE POR ID
    @Test
    void testBuscarPorIdNoEncontrado() {

        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<Usuario> resultado =
                usuarioService.buscarPorId(99L);

        assertTrue(resultado.isEmpty());

        verify(usuarioRepository).findById(99L);
    }

    // PRUEBA 8: BUSCAR USUARIO POR EMAIL
    @Test
    void testBuscarPorEmail() {

        when(usuarioRepository.findByEmail("usuario@ejemplo.com"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado =
                usuarioService.buscarPorEmail(
                        "usuario@ejemplo.com"
                );

        assertTrue(resultado.isPresent());

        assertEquals(
                "usuario@ejemplo.com",
                resultado.get().getEmail()
        );

        verify(usuarioRepository).findByEmail(
                "usuario@ejemplo.com"
        );
    }

    // PRUEBA 9: GUARDAR USUARIO
    @Test
    void testGuardarUsuario() {

        when(usuarioRepository.save(usuario))
                .thenReturn(usuario);

        Usuario resultado =
                usuarioService.guardar(usuario);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());

        assertEquals(
                "usuario@ejemplo.com",
                resultado.getEmail()
        );

        verify(usuarioRepository).save(usuario);
    }

    // PRUEBA 10: ELIMINAR USUARIO
    @Test
    void testEliminarUsuario() {

        usuarioService.eliminar(1L);

        verify(usuarioRepository, times(1))
                .deleteById(1L);
    }
}
