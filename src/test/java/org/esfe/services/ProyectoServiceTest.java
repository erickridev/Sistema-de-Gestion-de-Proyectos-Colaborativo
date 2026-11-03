
package org.esfe.services;

import org.esfe.models.Categoria;
import org.esfe.models.Proyecto;
import org.esfe.repositories.ProyectoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @InjectMocks
    private ProyectoService proyectoService;

    private Proyecto proyecto;
    private Categoria categoria;

    @BeforeEach
    void setUp() {

        // CREAR CATEGORIA DE PRUEBA
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Desarrollo de Software");

        // CREAR PROYECTO DE PRUEBA
        proyecto = new Proyecto();
        proyecto.setId(1L);
        proyecto.setTitulo("Sistema de Gestión");
        proyecto.setDescripcion("Sistema colaborativo");
        proyecto.setFechaInicio(LocalDate.of(2026, 1, 10));
        proyecto.setFechaFin(LocalDate.of(2026, 12, 20));
        proyecto.setCategoria(categoria);
    }

    // PRUEBA 1: GUARDAR PROYECTO
    @Test
    void testGuardarProyecto() {

        when(proyectoRepository.save(proyecto))
                .thenReturn(proyecto);

        Proyecto resultado = proyectoService.guardar(proyecto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Sistema de Gestión", resultado.getTitulo());

        verify(proyectoRepository).save(proyecto);
    }

    // PRUEBA 2: BUSCAR PROYECTO EXISTENTE
    @Test
    void testBuscarPorIdExitoso() {

        when(proyectoRepository.findById(1L))
                .thenReturn(Optional.of(proyecto));

        Optional<Proyecto> resultado =
                proyectoService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());

        verify(proyectoRepository).findById(1L);
    }

    // PRUEBA 3: BUSCAR PROYECTO INEXISTENTE
    @Test
    void testBuscarPorIdNoEncontrado() {

        when(proyectoRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<Proyecto> resultado =
                proyectoService.buscarPorId(99L);

        assertTrue(resultado.isEmpty());

        verify(proyectoRepository).findById(99L);
    }

    // PRUEBA 4: LISTAR TODOS LOS PROYECTOS
    @Test
    void testListarTodos() {

        when(proyectoRepository.findAll())
                .thenReturn(List.of(proyecto));

        List<Proyecto> resultado =
                proyectoService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals(
                "Sistema de Gestión",
                resultado.get(0).getTitulo()
        );

        verify(proyectoRepository).findAll();
    }

    // PRUEBA 5: LISTAR SIN PROYECTOS
    @Test
    void testListarTodosVacio() {

        when(proyectoRepository.findAll())
                .thenReturn(List.of());

        List<Proyecto> resultado =
                proyectoService.listarTodos();

        assertTrue(resultado.isEmpty());

        verify(proyectoRepository).findAll();
    }

    // PRUEBA 6: LISTAR POR CATEGORIA
    @Test
    void testListarPorCategoria() {

        when(proyectoRepository.findByCategoriaId(1L))
                .thenReturn(List.of(proyecto));

        List<Proyecto> resultado =
                proyectoService.listarPorCategoria(1L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getCategoria().getId());

        verify(proyectoRepository).findByCategoriaId(1L);
    }

    // PRUEBA 7: ACTUALIZAR PROYECTO EXISTENTE
    @Test
    void testActualizarProyectoExitoso() {

        Proyecto datosActualizados = new Proyecto();
        datosActualizados.setTitulo("Sistema Actualizado");
        datosActualizados.setDescripcion("Nueva descripción");
        datosActualizados.setFechaInicio(
                LocalDate.of(2026, 2, 1)
        );
        datosActualizados.setFechaFin(
                LocalDate.of(2026, 11, 30)
        );
        datosActualizados.setCategoria(categoria);

        when(proyectoRepository.findById(1L))
                .thenReturn(Optional.of(proyecto));

        when(proyectoRepository.save(proyecto))
                .thenReturn(proyecto);

        Optional<Proyecto> resultado =
                proyectoService.actualizar(1L, datosActualizados);

        assertTrue(resultado.isPresent());

        assertEquals(
                "Sistema Actualizado",
                resultado.get().getTitulo()
        );

        assertEquals(
                "Nueva descripción",
                resultado.get().getDescripcion()
        );

        assertEquals(
                LocalDate.of(2026, 11, 30),
                resultado.get().getFechaFin()
        );

        verify(proyectoRepository).findById(1L);
        verify(proyectoRepository).save(proyecto);
    }

    // PRUEBA 8: ACTUALIZAR PROYECTO INEXISTENTE
    @Test
    void testActualizarProyectoNoEncontrado() {

        Proyecto datosActualizados = new Proyecto();
        datosActualizados.setTitulo("Proyecto Nuevo");

        when(proyectoRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<Proyecto> resultado =
                proyectoService.actualizar(99L, datosActualizados);

        assertTrue(resultado.isEmpty());

        verify(proyectoRepository).findById(99L);
        verify(proyectoRepository, never())
                .save(any(Proyecto.class));
    }

    // PRUEBA 9: ELIMINAR PROYECTO EXISTENTE
    @Test
    void testEliminarProyectoExitoso() {

        when(proyectoRepository.existsById(1L))
                .thenReturn(true);

        boolean resultado = proyectoService.eliminar(1L);

        assertTrue(resultado);

        verify(proyectoRepository).existsById(1L);
        verify(proyectoRepository).deleteById(1L);
    }

    // PRUEBA 10: ELIMINAR PROYECTO INEXISTENTE
    @Test
    void testEliminarProyectoNoEncontrado() {

        when(proyectoRepository.existsById(99L))
                .thenReturn(false);

        boolean resultado = proyectoService.eliminar(99L);

        assertFalse(resultado);

        verify(proyectoRepository).existsById(99L);
        verify(proyectoRepository, never())
                .deleteById(anyLong());
    }
}
