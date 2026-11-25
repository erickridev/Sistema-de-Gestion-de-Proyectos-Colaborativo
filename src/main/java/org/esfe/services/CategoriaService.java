
package org.esfe.services;

import org.esfe.models.Categoria;
import org.esfe.repositories.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // READ: Listar todas las categorías
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    // READ: Buscar categoría por ID
    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    // CREATE: Guardar categoría
    @Transactional
    public Categoria guardar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    // UPDATE: Actualizar categoría
    @Transactional
    public Optional<Categoria> actualizar(Long id, Categoria categoriaActualizada) {

        return categoriaRepository.findById(id).map(categoria -> {

            categoria.setNombre(categoriaActualizada.getNombre());

            return categoriaRepository.save(categoria);
        });
    }

    // DELETE: Eliminar categoría
    @Transactional
    public boolean eliminar(Long id) {

        if (!categoriaRepository.existsById(id)) {
            return false;
        }

        categoriaRepository.deleteById(id);

        return true;
    }
}
