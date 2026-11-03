
package org.esfe.controllers;

import org.esfe.models.Proyecto;
import org.esfe.services.ProyectoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proyectos")
public class ProyectoController {

    private final ProyectoService proyectoService;

    public ProyectoController(ProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }

    // READ: Listar todos los proyectos
    @GetMapping
    public ResponseEntity<List<Proyecto>> listar() {

        return ResponseEntity.ok(proyectoService.listarTodos());
    }

    // READ: Buscar proyecto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Proyecto> buscarPorId(@PathVariable Long id) {

        return proyectoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // READ: Listar proyectos por categoría
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Proyecto>> listarPorCategoria(
            @PathVariable Long categoriaId) {

        return ResponseEntity.ok(
                proyectoService.listarPorCategoria(categoriaId)
        );
    }

    // CREATE: Registrar proyecto
    @PostMapping
    public ResponseEntity<Proyecto> crear(
            @RequestBody Proyecto proyecto) {

        Proyecto nuevoProyecto = proyectoService.guardar(proyecto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nuevoProyecto);
    }

    // UPDATE: Actualizar proyecto
    @PutMapping("/{id}")
    public ResponseEntity<Proyecto> actualizar(
            @PathVariable Long id,
            @RequestBody Proyecto proyecto) {

        return proyectoService.actualizar(id, proyecto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE: Eliminar proyecto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        boolean eliminado = proyectoService.eliminar(id);

        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
