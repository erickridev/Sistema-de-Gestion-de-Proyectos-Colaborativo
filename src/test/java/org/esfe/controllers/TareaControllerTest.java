package org.esfe.controllers;

import org.esfe.models.Tarea;
import org.esfe.services.TareaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
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

    @Test
    @WithMockUser(username = "admin@ejemplo.com", roles = {"ADMIN"})
    void testObtenerTareaPorIdConAutenticacion() throws Exception {
        Tarea tarea = new Tarea();
        tarea.setId(1L);
        tarea.setTitulo("Implementar Seguridad");

        when(tareaService.buscarPorId(1L)).thenReturn(Optional.of(tarea));

        mockMvc.perform(get("/api/tareas/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Implementar Seguridad"));
    }

    @Test
    void testObtenerTareaSinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(get("/api/tareas/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}