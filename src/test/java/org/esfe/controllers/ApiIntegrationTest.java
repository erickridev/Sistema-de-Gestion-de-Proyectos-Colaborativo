package org.esfe.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.esfe.models.Categoria;
import org.esfe.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Sin transaccion de prueba: verifica commits, restricciones y serializacion reales.
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:api-integration;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class ApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository usuarios;
    @Autowired TareaRepository tareas;
    @Autowired ProyectoRepository proyectos;
    @Autowired CategoriaRepository categorias;

    @BeforeEach
    void cleanData() {
        tareas.deleteAll();
        proyectos.deleteAll();
        categorias.deleteAll();
        usuarios.deleteAll();
    }

    @Test
    void registrationLoginAndJwtAuthorization() throws Exception {
        register("Usuario@Example.com", "ClaveSegura123");
        var usuario = usuarios.findByEmail("usuario@example.com").orElseThrow();
        assertEquals("ROLE_USER", usuario.getRol().getNombre());
        assertNotEquals("ClaveSegura123", usuario.getPassword());
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", "USUARIO@example.com", "password", "ClaveSegura123"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = mapper.readTree(response).get("token").asText();
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Usuario"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/api/proyectos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/proyectos").header("Authorization", "bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(post("/api/proyectos").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"usuario@example.com\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
        usuarios.deleteById(usuario.getId());
        mvc.perform(get("/api/proyectos").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void securityErrorsAreJson() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/proyectos"))
                .andExpect(status().isUnauthorized()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401)).andExpect(jsonPath("$.timestamp").exists());
        for (String token : new String[]{"invalido", ""}) {
            mvc.perform(get("/api/proyectos").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
        }
    }

    @Test
    void servesFrontendWithoutOpeningProtectedEndpoints() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("index.html"));
        mvc.perform(get("/index.html")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
        mvc.perform(get("/assets/app.js")).andExpect(status().isOk());
        mvc.perform(get("/assets/core.mjs")).andExpect(status().isOk());
        mvc.perform(get("/assets/styles.css")).andExpect(status().isOk());
        mvc.perform(get("/favicon.svg")).andExpect(status().isOk());
        mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsDuplicateEmailsAndOversizedUnicodePasswords() throws Exception {
        register("usuario@example.com", "ClaveSegura123");
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registrationBody("USUARIO@example.com", "OtraClave123")))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registrationBody("otro@example.com", "á".repeat(40))))
                .andExpect(status().isBadRequest());
        assertEquals(1, usuarios.count());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void projectResolvesCategoryAndRejectsMissingRelations() throws Exception {
        Categoria categoria = categorias.saveAndFlush(new Categoria(null, "Real"));
        String response = mvc.perform(post("/api/proyectos").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("titulo", "Proyecto", "categoria",
                                Map.of("id", categoria.getId(), "nombre", "Falso")))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.categoria.nombre").value("Real"))
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(response).get("id").asLong();
        String missingCategory = "{\"titulo\":\"Cambio\",\"categoria\":{\"id\":9223372036854775807}}";
        mvc.perform(post("/api/proyectos").contentType(MediaType.APPLICATION_JSON).content(missingCategory))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/proyectos/" + id).contentType(MediaType.APPLICATION_JSON).content(missingCategory))
                .andExpect(status().isNotFound());
        assertEquals(1, proyectos.count());
        assertEquals("Proyecto", proyectos.findById(id).orElseThrow().getTitulo());
        mvc.perform(delete("/api/categorias/" + categoria.getId()))
                .andExpect(status().isConflict());
        assertTrue(categorias.existsById(categoria.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void taskLifecyclePreservesRelationsAndHidesCredentials() throws Exception {
        register("asignado@example.com", "ClaveSegura123");
        long usuarioId = usuarios.findByEmail("asignado@example.com").orElseThrow().getId();
        Categoria categoria = categorias.saveAndFlush(new Categoria(null, "Categoria"));
        var proyecto = new org.esfe.models.Proyecto();
        proyecto.setTitulo("Proyecto");
        proyecto.setCategoria(categoria);
        proyecto = proyectos.saveAndFlush(proyecto);
        String body = mapper.writeValueAsString(Map.of("titulo", "Tarea", "proyecto", Map.of("id", proyecto.getId()),
                "usuarioAsignado", Map.of("id", usuarioId, "password", "ignorada")));
        String response = mvc.perform(post("/api/tareas").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.usuarioAsignado.password").doesNotExist())
                .andExpect(jsonPath("$.usuarioAsignado.authorities").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(response).get("id").asLong();
        mvc.perform(get("/api/tareas/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.proyecto.categoria.nombre").value("Categoria"));
        mvc.perform(put("/api/tareas/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("titulo", "Cambio", "proyecto", Map.of("id", proyecto.getId()),
                                "usuarioAsignado", Map.of("id", Long.MAX_VALUE)))))
                .andExpect(status().isNotFound());
        assertEquals("Tarea", tareas.findById(id).orElseThrow().getTitulo());
        mvc.perform(patch("/api/tareas/" + id + "/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"COMPLETADA\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("COMPLETADA"));
        mvc.perform(delete("/api/proyectos/" + proyecto.getId())).andExpect(status().isConflict());
        mvc.perform(delete("/api/tareas/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/proyectos/" + proyecto.getId())).andExpect(status().isNoContent());
        mvc.perform(delete("/api/categorias/" + categoria.getId())).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void roleAdministrationProtectsBuiltInRoles() throws Exception {
        String response = mvc.perform(post("/api/roles").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"auditor\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.nombre").value("ROLE_AUDITOR"))
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(response).get("id").asLong();
        mvc.perform(put("/api/roles/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"revisor\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("ROLE_REVISOR"));
        mvc.perform(delete("/api/roles/" + id)).andExpect(status().isNoContent());
        long adminId = roles.findByNombre("ROLE_ADMIN").orElseThrow().getId();
        mvc.perform(put("/api/roles/" + adminId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"otro\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/roles/" + adminId)).andExpect(status().isForbidden());
    }

    @Autowired RolRepository roles;

    @Test
    @WithMockUser(roles = "ADMIN")
    void preservesHttpErrors() throws Exception {
        mvc.perform(patch("/api/proyectos/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed()).andExpect(header().exists("Allow"));
        mvc.perform(post("/api/proyectos").contentType(MediaType.TEXT_PLAIN).content("texto"))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(get("/api/ruta-inexistente")).andExpect(status().isNotFound());
    }

    private void register(String email, String password) throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registrationBody(email, password)))
                .andExpect(status().isCreated());
    }

    private String registrationBody(String email, String password) throws Exception {
        return mapper.writeValueAsString(Map.of("login", "usuario", "nombre", "Usuario", "email", email,
                "password", password, "rol", Map.of("nombre", "ROLE_ADMIN")));
    }
}
