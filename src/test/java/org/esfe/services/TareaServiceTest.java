
package org.esfe.services;

import org.esfe.models.Proyecto;
import org.esfe.models.Tarea;
import org.esfe.models.Usuario;

import org.esfe.repositories.ProyectoRepository;
import org.esfe.repositories.TareaRepository;
import org.esfe.repositories.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TareaServiceTest {

    @Mock
    private TareaRepository tareaRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TareaService tareaService;

    private Tarea tarea;
    private Proyecto proyecto;
    private Usuario usuario;

    @BeforeEach
    void setUp() {

        proyecto = new Proyecto();
        proyecto.setId(2L);
        proyecto.setTitulo("Sistema de Gestión");

        usuario = new Usuario();
        usuario.setId(3L);
        usuario.setEmail("usuario@ejemplo.com");

        tarea = new Tarea();
        tarea.setId(1L);
        tarea.setTitulo("Diseñar Base de Datos");
        tarea.setDescripcion("Crear tablas del sistema");
        tarea.setEstado(Tarea.EstadoTarea.PENDIENTE);
        tarea.setProyecto(proyecto);
        tarea.setUsuarioAsignado(usuario);
    }

    // PRUEBA 1: GUARDAR TAREA
    @Test
    void testGuardarTarea() {

        when(proyectoRepository.findById(2L))
                .thenReturn(Optional.of(proyecto));

        when(usuarioRepository.findById(3L))
                .thenReturn(Optional.of(usuario));

        when(tareaRepository.save(tarea))
                .thenReturn(tarea);

        Tarea resultado = tareaService.guardar(tarea);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(
                Tarea.EstadoTarea.PENDIENTE,
                resultado.getEstado()
        );

        verify(proyectoRepository).findById(2L);
        verify(usuarioRepository).findById(3L);
        verify(tareaRepository).save(tarea);
    }

    // PRUEBA 2: BUSCAR TAREA EXISTENTE
    @Test
    void testBuscarPorId() {

        when(tareaRepository.findById(1L))
                .thenReturn(Optional.of(tarea));

        Optional<Tarea> resultado =
                tareaService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(
                "Diseñar Base de Datos",
                resultado.get().getTitulo()
        );

        verify(tareaRepository).findById(1L);
    }

    // PRUEBA 3: BUSCAR TAREA INEXISTENTE
    @Test
    void testBuscarPorIdNoEncontrada() {

        when(tareaRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<Tarea> resultado =
                tareaService.buscarPorId(99L);

        assertTrue(resultado.isEmpty());
        verify(tareaRepository).findById(99L);
    }

    // PRUEBA 4: LISTAR TODAS LAS TAREAS
    @Test
    void testListarTodas() {

        when(tareaRepository.findAll())
                .thenReturn(List.of(tarea));

        List<Tarea> resultado = tareaService.listarTodas();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());

        verify(tareaRepository).findAll();
    }

    // PRUEBA 5: LISTAR POR USUARIO
    @Test
    void testListarPorUsuario() {

        when(tareaRepository.findByUsuarioAsignadoId(3L))
                .thenReturn(List.of(tarea));

        List<Tarea> resultado =
                tareaService.listarPorUsuario(3L);

        assertEquals(1, resultado.size());
        assertEquals(
                "Diseñar Base de Datos",
                resultado.get(0).getTitulo()
        );

        verify(tareaRepository)
                .findByUsuarioAsignadoId(3L);
    }

    // PRUEBA 6: LISTAR POR PROYECTO
    @Test
    void testListarPorProyecto() {

        when(tareaRepository.findByProyectoId(2L))
                .thenReturn(List.of(tarea));

        List<Tarea> resultado =
                tareaService.listarPorProyecto(2L);

        assertEquals(1, resultado.size());

        verify(tareaRepository).findByProyectoId(2L);
    }

    // PRUEBA 7: GUARDAR SIN PROYECTO
    @Test
    void testGuardarSinProyecto() {

        tarea.setProyecto(null);

        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> tareaService.guardar(tarea)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                excepcion.getStatusCode()
        );

        verify(tareaRepository, never())
                .save(any(Tarea.class));
    }

    // PRUEBA 8: GUARDAR CON PROYECTO INEXISTENTE
    @Test
    void testGuardarConProyectoInexistente() {

        when(proyectoRepository.findById(2L))
                .thenReturn(Optional.empty());

        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> tareaService.guardar(tarea)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                excepcion.getStatusCode()
        );

        verify(tareaRepository, never())
                .save(any(Tarea.class));
    }

    // PRUEBA 9: GUARDAR CON USUARIO INEXISTENTE
    @Test
    void testGuardarConUsuarioInexistente() {

        when(proyectoRepository.findById(2L))
                .thenReturn(Optional.of(proyecto));

        when(usuarioRepository.findById(3L))
                .thenReturn(Optional.empty());

        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> tareaService.guardar(tarea)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                excepcion.getStatusCode()
        );

        verify(tareaRepository, never())
                .save(any(Tarea.class));
    }

    // PRUEBA 10: ASIGNAR ESTADO PREDETERMINADO
    @Test
    void testGuardarConEstadoNulo() {

        tarea.setEstado(null);

        when(proyectoRepository.findById(2L))
                .thenReturn(Optional.of(proyecto));

        when(usuarioRepository.findById(3L))
                .thenReturn(Optional.of(usuario));

        when(tareaRepository.save(tarea))
                .thenReturn(tarea);

        Tarea resultado = tareaService.guardar(tarea);

        assertEquals(
                Tarea.EstadoTarea.PENDIENTE,
                resultado.getEstado()
        );
    }

    // PRUEBA 11: CAMBIAR ESTADO
    @Test
    void testCambiarEstado() {

        when(tareaRepository.findById(1L))
                .thenReturn(Optional.of(tarea));

        when(tareaRepository.save(tarea))
                .thenReturn(tarea);

        Optional<Tarea> resultado =
                tareaService.cambiarEstado(
                        1L,
                        Tarea.EstadoTarea.COMPLETADA
                );

        assertTrue(resultado.isPresent());

        assertEquals(
                Tarea.EstadoTarea.COMPLETADA,
                resultado.get().getEstado()
        );

        verify(tareaRepository).save(tarea);
    }

    // PRUEBA 12: ELIMINAR TAREA EXISTENTE
    @Test
    void testEliminarTarea() {

        when(tareaRepository.existsById(1L))
                .thenReturn(true);

        boolean resultado = tareaService.eliminar(1L);

        assertTrue(resultado);

        verify(tareaRepository).deleteById(1L);
    }

    // PRUEBA 13: ELIMINAR TAREA INEXISTENTE
    @Test
    void testEliminarTareaNoEncontrada() {

        when(tareaRepository.existsById(99L))
                .thenReturn(false);

        boolean resultado = tareaService.eliminar(99L);

        assertFalse(resultado);

        verify(tareaRepository, never())
                .deleteById(anyLong());
    }


    // PRUEBA 14: LISTAR TAREAS POR ESTADO
    @Test
    void testListarPorEstado() {

        when(tareaRepository.findByEstado(
                Tarea.EstadoTarea.PENDIENTE
        )).thenReturn(List.of(tarea));

        List<Tarea> resultado = tareaService.listarPorEstado(
                Tarea.EstadoTarea.PENDIENTE
        );

        assertEquals(1, resultado.size());
        assertEquals(
                Tarea.EstadoTarea.PENDIENTE,
                resultado.get(0).getEstado()
        );

        verify(tareaRepository).findByEstado(
                Tarea.EstadoTarea.PENDIENTE
        );
    }

    // PRUEBA 15: LISTAR POR PROYECTO Y ESTADO
    @Test
    void testListarPorProyectoYEstado() {

        when(tareaRepository.findByProyectoIdAndEstado(
                2L,
                Tarea.EstadoTarea.PENDIENTE
        )).thenReturn(List.of(tarea));

        List<Tarea> resultado =
                tareaService.listarPorProyectoYEstado(
                        2L,
                        Tarea.EstadoTarea.PENDIENTE
                );

        assertEquals(1, resultado.size());

        verify(tareaRepository).findByProyectoIdAndEstado(
                2L,
                Tarea.EstadoTarea.PENDIENTE
        );
    }

    // PRUEBA 16: ACTUALIZAR TAREA EXISTENTE
    @Test
    void testActualizarTareaExitosamente() {

        Tarea datos = new Tarea();
        datos.setTitulo("Diseñar nueva base de datos");
        datos.setDescripcion("Actualizar estructura");
        datos.setEstado(Tarea.EstadoTarea.EN_PROGRESO);
        datos.setProyecto(proyecto);
        datos.setUsuarioAsignado(usuario);

        when(tareaRepository.findById(1L))
                .thenReturn(Optional.of(tarea));

        when(proyectoRepository.findById(2L))
                .thenReturn(Optional.of(proyecto));

        when(usuarioRepository.findById(3L))
                .thenReturn(Optional.of(usuario));

        when(tareaRepository.save(tarea))
                .thenReturn(tarea);

        Optional<Tarea> resultado =
                tareaService.actualizar(1L, datos);

        assertTrue(resultado.isPresent());

        assertEquals(
                "Diseñar nueva base de datos",
                resultado.get().getTitulo()
        );

        assertEquals(
                Tarea.EstadoTarea.EN_PROGRESO,
                resultado.get().getEstado()
        );

        verify(tareaRepository).findById(1L);
        verify(proyectoRepository).findById(2L);
        verify(usuarioRepository).findById(3L);
        verify(tareaRepository).save(tarea);
    }

    // PRUEBA 17: ACTUALIZAR TAREA INEXISTENTE
    @Test
    void testActualizarTareaNoEncontrada() {

        Tarea datos = new Tarea();
        datos.setTitulo("Nueva tarea");

        when(tareaRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<Tarea> resultado =
                tareaService.actualizar(99L, datos);

        assertTrue(resultado.isEmpty());

        verify(tareaRepository).findById(99L);

        verify(tareaRepository, never())
                .save(any(Tarea.class));
    }

    // PRUEBA 18: CAMBIAR ESTADO A NULL
    @Test
    void testCambiarEstadoNulo() {

        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> tareaService.cambiarEstado(1L, null)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                excepcion.getStatusCode()
        );

        verify(tareaRepository, never())
                .save(any(Tarea.class));
    }

    // PRUEBA 19: CAMBIAR ESTADO DE TAREA INEXISTENTE
    @Test
    void testCambiarEstadoTareaNoEncontrada() {

        when(tareaRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<Tarea> resultado =
                tareaService.cambiarEstado(
                        99L,
                        Tarea.EstadoTarea.COMPLETADA
                );

        assertTrue(resultado.isEmpty());

        verify(tareaRepository).findById(99L);

        verify(tareaRepository, never())
                .save(any(Tarea.class));
    }

}
