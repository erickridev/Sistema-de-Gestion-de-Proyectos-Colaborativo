
package org.esfe.services;

import org.esfe.models.Proyecto;
import org.esfe.repositories.ProyectoRepository;
import org.esfe.repositories.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProyectoService(ProyectoRepository proyectoRepository, CategoriaRepository categoriaRepository) {
        this.proyectoRepository = proyectoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // READ: Listar todos los proyectos
    public List<Proyecto> listarTodos() {
        return proyectoRepository.findAll();
    }

    // READ: Buscar proyecto por ID
    public Optional<Proyecto> buscarPorId(Long id) {
        return proyectoRepository.findById(id);
    }

    // READ: Listar proyectos por categoría
    public List<Proyecto> listarPorCategoria(Long categoriaId) {
        return proyectoRepository.findByCategoriaId(categoriaId);
    }

    // CREATE: Guardar proyecto
    @Transactional
    public Proyecto guardar(Proyecto proyecto) {
        resolverCategoria(proyecto);
        return proyectoRepository.save(proyecto);
    }

    // UPDATE: Actualizar proyecto
    @Transactional
    public Optional<Proyecto> actualizar(Long id, Proyecto proyectoActualizado) {

        return proyectoRepository.findById(id).map(proyecto -> {

            resolverCategoria(proyectoActualizado);

            proyecto.setTitulo(proyectoActualizado.getTitulo());
            proyecto.setDescripcion(proyectoActualizado.getDescripcion());
            proyecto.setFechaInicio(proyectoActualizado.getFechaInicio());
            proyecto.setFechaFin(proyectoActualizado.getFechaFin());
            proyecto.setCategoria(proyectoActualizado.getCategoria());

            return proyectoRepository.save(proyecto);
        });
    }

    // DELETE: Eliminar proyecto
    @Transactional
    public boolean eliminar(Long id) {

        if (!proyectoRepository.existsById(id)) {
            return false;
        }

        proyectoRepository.deleteById(id);

        return true;
    }

    private void resolverCategoria(Proyecto proyecto) {
        if (proyecto == null || proyecto.getCategoria() == null
                || proyecto.getCategoria().getId() == null || proyecto.getCategoria().getId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar una categoria valida");
        }
        proyecto.setCategoria(categoriaRepository.findById(proyecto.getCategoria().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La categoria no existe")));
    }
}
