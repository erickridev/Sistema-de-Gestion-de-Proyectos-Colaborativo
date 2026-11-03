
package org.esfe.services;

import org.esfe.models.Proyecto;
import org.esfe.repositories.ProyectoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;

    public ProyectoService(ProyectoRepository proyectoRepository) {
        this.proyectoRepository = proyectoRepository;
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
    public Proyecto guardar(Proyecto proyecto) {
        return proyectoRepository.save(proyecto);
    }

    // UPDATE: Actualizar proyecto
    public Optional<Proyecto> actualizar(Long id, Proyecto proyectoActualizado) {

        return proyectoRepository.findById(id).map(proyecto -> {

            proyecto.setTitulo(proyectoActualizado.getTitulo());
            proyecto.setDescripcion(proyectoActualizado.getDescripcion());
            proyecto.setFechaInicio(proyectoActualizado.getFechaInicio());
            proyecto.setFechaFin(proyectoActualizado.getFechaFin());
            proyecto.setCategoria(proyectoActualizado.getCategoria());

            return proyectoRepository.save(proyecto);
        });
    }

    // DELETE: Eliminar proyecto
    public boolean eliminar(Long id) {

        if (!proyectoRepository.existsById(id)) {
            return false;
        }

        proyectoRepository.deleteById(id);

        return true;
    }
}
