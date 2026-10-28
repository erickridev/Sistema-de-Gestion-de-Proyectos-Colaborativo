package org.esfe.services;

import org.esfe.models.Proyecto;
import org.esfe.repositories.ProyectoRepository;
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
class ProyectoServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @InjectMocks
    private ProyectoService proyectoService;

    private Proyecto proyecto;

    @BeforeEach
    void setUp() {
        proyecto = new Proyecto();
        proyecto.setId(1L);
        proyecto.setTitulo("Sistema de Gestión"); // Usa setTitulo
        proyecto.setDescripcion("Descripción del proyecto");
    }

    @Test
    void testGuardarProyecto() {
        when(proyectoRepository.save(proyecto)).thenReturn(proyecto);

        Proyecto resultado = proyectoService.guardar(proyecto);

        assertNotNull(resultado);
        assertEquals("Sistema de Gestión", resultado.getTitulo()); // Usa getTitulo
        verify(proyectoRepository, times(1)).save(proyecto);
    }

    @Test
    void testBuscarPorIdExitoso() {
        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyecto));

        Optional<Proyecto> resultado = proyectoService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
    }

    @Test
    void testListarTodos() {
        when(proyectoRepository.findAll()).thenReturn(List.of(proyecto));

        List<Proyecto> lista = proyectoService.listarTodos();

        assertFalse(lista.isEmpty());
        assertEquals(1, lista.size());
    }
}