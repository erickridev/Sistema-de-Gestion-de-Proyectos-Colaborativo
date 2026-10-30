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

    @GetMapping
    public ResponseEntity<List<Proyecto>> listar() {
        return ResponseEntity.ok(proyectoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proyecto> buscarPorId(@PathVariable Long id) {
        return proyectoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Proyecto>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(proyectoService.listarPorCategoria(categoriaId));
    }

    @PostMapping
    public ResponseEntity<Proyecto> crear(@RequestBody Proyecto proyecto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proyectoService.guardar(proyecto));
    }

    // --- MÉTODOS DE ACTUALIZACIÓN Y ELIMINACIÓN ---

    @PutMapping("/{id}")
    public ResponseEntity<Proyecto> actualizar(@PathVariable Long id, @RequestBody Proyecto proyecto) {
        return proyectoService.buscarPorId(id)
                .map(proyectoExistente -> {
                    proyecto.setId(id); // Asegura que se actualice el ID correcto
                    Proyecto actualizado = proyectoService.guardar(proyecto);
                    return ResponseEntity.ok(actualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        proyectoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}