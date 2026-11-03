
package org.esfe.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.esfe.models.Rol;
import org.esfe.services.RolService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RolController {

    private final RolService rolService;

    // INYECCION DE DEPENDENCIAS
    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    // LISTAR TODOS LOS ROLES
    @GetMapping
    public ResponseEntity<List<Rol>> listar() {
        return ResponseEntity.ok(
                rolService.listarTodos()
        );
    }

    // BUSCAR ROL POR ID
    @GetMapping("/{id}")
    public ResponseEntity<Rol> buscarPorId(
            @PathVariable Long id) {

        return rolService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CREAR NUEVO ROL
    @PostMapping
    public ResponseEntity<Rol> crear(
            @Valid @RequestBody RolRequest request) {

        Rol rol = new Rol();
        rol.setNombre(request.nombre());

        Rol nuevoRol = rolService.guardar(rol);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevoRol);
    }

    // ACTUALIZAR ROL
    @PutMapping("/{id}")
    public ResponseEntity<Rol> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody RolRequest request) {

        return rolService.buscarPorId(id)
                .map(rolExistente -> {

                    // Impedir renombrar roles principales
                    if ("ROLE_ADMIN".equals(rolExistente.getNombre()) ||
                            "ROLE_USER".equals(rolExistente.getNombre())) {

                        throw new org.springframework.web.server.ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "No se pueden modificar los roles principales"
                        );
                    }

                    rolExistente.setNombre(request.nombre());

                    return ResponseEntity.ok(
                            rolService.guardar(rolExistente)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ELIMINAR ROL
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        boolean eliminado = rolService.eliminar(id);

        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    // DTO PARA CREAR Y ACTUALIZAR ROLES
    public record RolRequest(

            @NotBlank(message = "El nombre del rol es obligatorio")
            @Size(max = 45, message = "El nombre no puede superar 45 caracteres")
            String nombre

    ) {
    }
}
