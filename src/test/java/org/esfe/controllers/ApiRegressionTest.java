package org.esfe.controllers;

import org.esfe.models.Categoria;
import org.esfe.repositories.CategoriaRepository;
import org.esfe.repositories.ProyectoRepository;
import org.esfe.repositories.RolRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiRegressionTest {
    @Autowired MockMvc mvc;
    @Autowired CategoriaRepository categorias;
    @Autowired ProyectoRepository proyectos;
    @Autowired RolRepository roles;

    @Test
    void initializesRolesAndRegistersUser() throws Exception {
        assertTrue(roles.findByNombre("ROLE_USER").isPresent());
        assertTrue(roles.findByNombre("ROLE_ADMIN").isPresent());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"login":"nuevo","nombre":"Nuevo Usuario","email":"nuevo@example.com","password":"ClaveSegura123"}
                        """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(get("/api/proyectos")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotCreateOrChangeTaskStatus() throws Exception {
        mvc.perform(post("/api/proyectos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/tareas/1/estado").contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"COMPLETADA\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void creationCannotOverwriteExistingRecords() throws Exception {
        Categoria original = categorias.saveAndFlush(new Categoria(null, "Original"));
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":" + original.getId() + ",\"nombre\":\"Nueva\"}"))
                .andExpect(status().isCreated());
        assertEquals("Original", categorias.findById(original.getId()).orElseThrow().getNombre());
        String body = "{\"titulo\":\"Original\",\"categoria\":{\"id\":" + original.getId() + "}}";
        String result = mvc.perform(post("/api/proyectos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(result).get("id").asLong();
        mvc.perform(post("/api/proyectos").contentType(MediaType.APPLICATION_JSON)
                .content(body.replace("\"Original\"", "\"Nuevo\"").replace("{\"titulo\"", "{\"id\":" + id + ",\"titulo\"")))
                .andExpect(status().isCreated());
        assertEquals("Original", proyectos.findById(id).orElseThrow().getTitulo());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsInvalidRequests() throws Exception {
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\" \"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/proyectos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Proyecto\",\"categoria\":{\"id\":1},\"fechaInicio\":\"2026-11-20\",\"fechaFin\":\"2026-01-01\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/proyectos/abc")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/proyectos").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/tareas/1/estado").contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"INVALIDO\"}"))
                .andExpect(status().isBadRequest());
    }
}
