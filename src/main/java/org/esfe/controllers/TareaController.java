package org.esfe.controllers;

import org.esfe.models.Tarea;
import org.esfe.services.TareaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {

    private final TareaService tareaService;

    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    @GetMapping
    public ResponseEntity<List<Tarea>> listar() {
        return ResponseEntity.ok(tareaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarea> buscarPorId(@PathVariable Long id) {
        return tareaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<Tarea>> listarPorProyecto(@PathVariable Long proyectoId) {
        return ResponseEntity.ok(tareaService.listarPorProyecto(proyectoId));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Tarea>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(tareaService.listarPorUsuario(usuarioId));
    }

    @PostMapping
    public ResponseEntity<Tarea> crear(@RequestBody Tarea tarea) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tareaService.guardar(tarea));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarea> actualizar(@PathVariable Long id, @RequestBody Tarea tarea) {
        return tareaService.buscarPorId(id)
                .map(tareaExistente -> {
                    tarea.setId(id);
                    return ResponseEntity.ok(tareaService.guardar(tarea));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tareaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}