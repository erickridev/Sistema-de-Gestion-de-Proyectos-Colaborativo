
package org.esfe.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

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

    // INYECCION DE DEPENDENCIAS
    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    // LISTAR TODAS LAS TAREAS
    @GetMapping
    public ResponseEntity<List<Tarea>> listar() {

        return ResponseEntity.ok(
                tareaService.listarTodas()
        );
    }

    // BUSCAR TAREA POR ID
    @GetMapping("/{id}")
    public ResponseEntity<Tarea> buscarPorId(
            @PathVariable Long id) {

        return tareaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // LISTAR TAREAS POR PROYECTO
    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<Tarea>> listarPorProyecto(
            @PathVariable Long proyectoId) {

        return ResponseEntity.ok(
                tareaService.listarPorProyecto(proyectoId)
        );
    }

    // LISTAR TAREAS POR USUARIO
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Tarea>> listarPorUsuario(
            @PathVariable Long usuarioId) {

        return ResponseEntity.ok(
                tareaService.listarPorUsuario(usuarioId)
        );
    }

    // LISTAR TAREAS POR ESTADO
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Tarea>> listarPorEstado(
            @PathVariable Tarea.EstadoTarea estado) {

        return ResponseEntity.ok(
                tareaService.listarPorEstado(estado)
        );
    }

    // LISTAR TAREAS POR PROYECTO Y ESTADO
    @GetMapping("/proyecto/{proyectoId}/estado/{estado}")
    public ResponseEntity<List<Tarea>> listarPorProyectoYEstado(
            @PathVariable Long proyectoId,
            @PathVariable Tarea.EstadoTarea estado) {

        return ResponseEntity.ok(
                tareaService.listarPorProyectoYEstado(
                        proyectoId, estado
                )
        );
    }

    // CREAR NUEVA TAREA
    @PostMapping
    public ResponseEntity<Tarea> crear(
            @Valid @RequestBody Tarea tarea) {

        // Evitar que el cliente establezca el ID
        tarea.setId(null);

        Tarea nuevaTarea = tareaService.guardar(tarea);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevaTarea);
    }

    // ACTUALIZAR TAREA
    @PutMapping("/{id}")
    public ResponseEntity<Tarea> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Tarea tarea) {

        return tareaService.actualizar(id, tarea)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CAMBIAR ESTADO DE UNA TAREA
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Tarea> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request) {

        return tareaService.cambiarEstado(
                        id, request.estado())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ELIMINAR TAREA
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        boolean eliminado = tareaService.eliminar(id);

        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    // DTO PARA CAMBIAR ESTADO
    public record CambiarEstadoRequest(
            @NotNull(message = "El estado es obligatorio")
            Tarea.EstadoTarea estado
    ) {
    }
}
