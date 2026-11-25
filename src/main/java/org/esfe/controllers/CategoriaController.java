
package org.esfe.controllers;

import org.esfe.models.Categoria;
import jakarta.validation.Valid;
import org.esfe.services.CategoriaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // READ: Listar todas las categorías
    @GetMapping
    public ResponseEntity<List<Categoria>> listar() {

        return ResponseEntity.ok(
                categoriaService.listarTodas()
        );
    }

    // READ: Buscar categoría por ID
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscarPorId(
            @PathVariable Long id) {

        return categoriaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CREATE: Registrar categoría
    @PostMapping
    public ResponseEntity<Categoria> crear(
            @Valid @RequestBody Categoria categoria) {

        categoria.setId(null);
        Categoria nuevaCategoria =
                categoriaService.guardar(categoria);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevaCategoria);
    }

    // UPDATE: Actualizar categoría
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Categoria categoria) {

        return categoriaService.actualizar(id, categoria)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE: Eliminar categoría
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        if (!categoriaService.eliminar(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
