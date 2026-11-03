
package org.esfe.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.esfe.config.JwtAuthenticationFilter;
import org.esfe.models.Tarea;
import org.esfe.services.TareaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TareaController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class TareaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TareaService tareaService;

    private Tarea tarea;

    @BeforeEach
    void setUp() {

        tarea = new Tarea();
        tarea.setId(1L);
        tarea.setTitulo("Diseñar Base de Datos");
        tarea.setDescripcion("Crear las tablas del sistema");
        tarea.setEstado(Tarea.EstadoTarea.PENDIENTE);
    }

    // PRUEBA 1: OBTENER TAREA POR ID
    @Test
    void testObtenerTareaPorIdExitoso() throws Exception {

        when(tareaService.buscarPorId(1L))
                .thenReturn(Optional.of(tarea));

        mockMvc.perform(
                        get("/api/tareas/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.titulo")
                                .value("Diseñar Base de Datos")
                )
                .andExpect(
                        jsonPath("$.estado")
                                .value("PENDIENTE")
                );

        verify(tareaService).buscarPorId(1L);
    }

    // PRUEBA 2: TAREA NO ENCONTRADA
    @Test
    void testObtenerTareaNoEncontrada() throws Exception {

        when(tareaService.buscarPorId(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/tareas/99")
                )
                .andExpect(status().isNotFound());

        verify(tareaService).buscarPorId(99L);
    }

    // PRUEBA 3: LISTAR TAREAS
    @Test
    void testListarTareas() throws Exception {

        when(tareaService.listarTodas())
                .thenReturn(List.of(tarea));

        mockMvc.perform(
                        get("/api/tareas")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(
                        jsonPath("$[0].titulo")
                                .value("Diseñar Base de Datos")
                );

        verify(tareaService).listarTodas();
    }

    // PRUEBA 4: LISTAR TAREAS DE UN PROYECTO
    @Test
    void testListarTareasPorProyecto() throws Exception {

        when(tareaService.listarPorProyecto(2L))
                .thenReturn(List.of(tarea));

        mockMvc.perform(
                        get("/api/tareas/proyecto/2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(tareaService).listarPorProyecto(2L);
    }

    // PRUEBA 5: LISTAR TAREAS DE UN USUARIO
    @Test
    void testListarTareasPorUsuario() throws Exception {

        when(tareaService.listarPorUsuario(3L))
                .thenReturn(List.of(tarea));

        mockMvc.perform(
                        get("/api/tareas/usuario/3")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(tareaService).listarPorUsuario(3L);
    }

    // PRUEBA 6: ELIMINAR TAREA EXISTENTE
    @Test
    void testEliminarTarea() throws Exception {

        when(tareaService.eliminar(1L))
                .thenReturn(true);

        mockMvc.perform(
                        delete("/api/tareas/1")
                )
                .andExpect(status().isNoContent());

        verify(tareaService).eliminar(1L);
    }

    // PRUEBA 7: ELIMINAR TAREA INEXISTENTE
    @Test
    void testEliminarTareaNoEncontrada() throws Exception {

        when(tareaService.eliminar(99L))
                .thenReturn(false);

        mockMvc.perform(
                        delete("/api/tareas/99")
                )
                .andExpect(status().isNotFound());

        verify(tareaService).eliminar(99L);
    }

    // PRUEBA 8: CAMBIAR ESTADO DE TAREA
    @Test
    void testCambiarEstadoTarea() throws Exception {

        tarea.setEstado(Tarea.EstadoTarea.COMPLETADA);

        when(tareaService.cambiarEstado(
                1L,
                Tarea.EstadoTarea.COMPLETADA
        )).thenReturn(Optional.of(tarea));

        String json = """
                {
                    "estado": "COMPLETADA"
                }
                """;

        mockMvc.perform(
                        patch("/api/tareas/1/estado")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.estado")
                                .value("COMPLETADA")
                );

        verify(tareaService).cambiarEstado(
                1L,
                Tarea.EstadoTarea.COMPLETADA
        );
    }
}
