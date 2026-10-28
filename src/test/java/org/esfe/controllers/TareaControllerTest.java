package org.esfe.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.esfe.models.Tarea;
import org.esfe.services.TareaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TareaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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
    @WithMockUser(username = "user@ejemplo.com", roles = {"USER"})
    void testObtenerTareaPorIdExitoso() throws Exception {
        when(tareaService.buscarPorId(1L)).thenReturn(Optional.of(tarea));

        mockMvc.perform(get("/api/tareas/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Diseñar Base de Datos"));
    }

    @Test
    void testObtenerTareaSinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(get("/api/tareas/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}