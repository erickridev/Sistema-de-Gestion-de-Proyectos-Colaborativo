package org.esfe.services;

import org.esfe.models.Tarea;
import org.esfe.repositories.TareaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TareaServiceTest {

    @Mock
    private TareaRepository tareaRepository;

    @InjectMocks
    private TareaService tareaService;

    private Tarea tarea;

    @BeforeEach
    void setUp() {
        tarea = new Tarea();
        tarea.setId(1L);
        tarea.setTitulo("Diseñar Base de Datos");
        tarea.setEstado(Tarea.EstadoTarea.PENDIENTE);
    }

    @Test
    void testGuardarTarea() {
        when(tareaRepository.save(tarea)).thenReturn(tarea);

        Tarea resultado = tareaService.guardar(tarea);

        assertNotNull(resultado);
        assertEquals(Tarea.EstadoTarea.PENDIENTE, resultado.getEstado());
        verify(tareaRepository, times(1)).save(tarea);
    }

    @Test
    void testBuscarPorId() {
        when(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea));

        Optional<Tarea> resultado = tareaService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Diseñar Base de Datos", resultado.get().getTitulo());
    }

    @Test
    void testListarPorUsuario() {
        when(tareaRepository.findByUsuarioAsignadoId(1L)).thenReturn(List.of(tarea));

        List<Tarea> tareas = tareaService.listarPorUsuario(1L);

        assertFalse(tareas.isEmpty());
        assertEquals(1, tareas.size());
        verify(tareaRepository, times(1)).findByUsuarioAsignadoId(1L);
    }
}