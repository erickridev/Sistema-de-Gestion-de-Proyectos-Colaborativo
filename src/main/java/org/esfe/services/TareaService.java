package org.esfe.services;

import org.esfe.models.Tarea;
import org.esfe.repositories.ProyectoRepository;
import org.esfe.repositories.TareaRepository;
import org.esfe.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TareaService {

    private final TareaRepository tareaRepository;
    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;

    // Inyección de dependencias por constructor
    public TareaService(TareaRepository tareaRepository,
                        ProyectoRepository proyectoRepository,
                        UsuarioRepository usuarioRepository) {
        this.tareaRepository = tareaRepository;
        this.proyectoRepository = proyectoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Tarea> listarTodas() {
        return tareaRepository.findAll();
    }

    public Optional<Tarea> buscarPorId(Long id) {
        return tareaRepository.findById(id);
    }

    public List<Tarea> listarPorProyecto(Long proyectoId) {
        return tareaRepository.findByProyectoId(proyectoId);
    }

    public List<Tarea> listarPorUsuario(Long usuarioId) {
        return tareaRepository.findByUsuarioAsignadoId(usuarioId);
    }

    public Tarea guardar(Tarea tarea) {
        if (tarea.getProyecto() != null && tarea.getProyecto().getId() != null) {
            proyectoRepository.findById(tarea.getProyecto().getId())
                    .ifPresent(tarea::setProyecto);
        }
        if (tarea.getUsuarioAsignado() != null && tarea.getUsuarioAsignado().getId() != null) {
            usuarioRepository.findById(tarea.getUsuarioAsignado().getId())
                    .ifPresent(tarea::setUsuarioAsignado);
        }
        return tareaRepository.save(tarea);
    }

    public void eliminar(Long id) {
        tareaRepository.deleteById(id);
    }
}