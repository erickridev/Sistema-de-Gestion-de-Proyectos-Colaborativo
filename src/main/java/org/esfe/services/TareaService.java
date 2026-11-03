
package org.esfe.services;

import org.esfe.models.Proyecto;
import org.esfe.models.Tarea;
import org.esfe.models.Usuario;

import org.esfe.repositories.ProyectoRepository;
import org.esfe.repositories.TareaRepository;
import org.esfe.repositories.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class TareaService {

    private static final Logger logger =
            LoggerFactory.getLogger(TareaService.class);

    private final TareaRepository tareaRepository;
    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;

    // INYECCION DE DEPENDENCIAS
    public TareaService(
            TareaRepository tareaRepository,
            ProyectoRepository proyectoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.tareaRepository = tareaRepository;
        this.proyectoRepository = proyectoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // LISTAR TODAS LAS TAREAS
    @Transactional(readOnly = true)
    public List<Tarea> listarTodas() {
        return tareaRepository.findAll();
    }

    // BUSCAR TAREA POR ID
    @Transactional(readOnly = true)
    public Optional<Tarea> buscarPorId(Long id) {
        return tareaRepository.findById(id);
    }

    // LISTAR TAREAS POR PROYECTO
    @Transactional(readOnly = true)
    public List<Tarea> listarPorProyecto(Long proyectoId) {
        return tareaRepository.findByProyectoId(proyectoId);
    }

    // LISTAR TAREAS POR USUARIO
    @Transactional(readOnly = true)
    public List<Tarea> listarPorUsuario(Long usuarioId) {
        return tareaRepository.findByUsuarioAsignadoId(usuarioId);
    }

    // LISTAR TAREAS POR ESTADO
    @Transactional(readOnly = true)
    public List<Tarea> listarPorEstado(Tarea.EstadoTarea estado) {
        return tareaRepository.findByEstado(estado);
    }

    // LISTAR TAREAS POR PROYECTO Y ESTADO
    @Transactional(readOnly = true)
    public List<Tarea> listarPorProyectoYEstado(
            Long proyectoId,
            Tarea.EstadoTarea estado
    ) {
        return tareaRepository.findByProyectoIdAndEstado(
                proyectoId, estado
        );
    }

    // CREAR O GUARDAR TAREA
    @Transactional
    public Tarea guardar(Tarea tarea) {

        if (tarea == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los datos de la tarea son obligatorios"
            );
        }

        // VALIDAR TITULO
        if (tarea.getTitulo() == null ||
                tarea.getTitulo().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El titulo de la tarea es obligatorio"
            );
        }

        // VALIDAR PROYECTO
        if (tarea.getProyecto() == null ||
                tarea.getProyecto().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe seleccionar un proyecto"
            );
        }

        Long proyectoId = tarea.getProyecto().getId();

        logger.info(
                "Intentando guardar tarea. Proyecto ID recibido: {}",
                proyectoId
        );

        // BUSCAR PROYECTO EN LA BASE DE DATOS
        Proyecto proyecto = proyectoRepository
                .findById(proyectoId)
                .orElseThrow(() -> {

                    logger.warn(
                            "No se encontro el proyecto con ID: {}",
                            proyectoId
                    );

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "El proyecto no existe"
                    );
                });

        logger.info(
                "Proyecto encontrado. ID: {}, titulo: {}",
                proyecto.getId(),
                proyecto.getTitulo()
        );

        tarea.setProyecto(proyecto);

        // VALIDAR USUARIO ASIGNADO (OPCIONAL)
        if (tarea.getUsuarioAsignado() != null) {

            Long usuarioId =
                    tarea.getUsuarioAsignado().getId();

            if (usuarioId == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El usuario asignado debe tener un ID"
                );
            }

            Usuario usuario = usuarioRepository
                    .findById(usuarioId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "El usuario asignado no existe"
                            )
                    );

            tarea.setUsuarioAsignado(usuario);
        }

        // ESTABLECER ESTADO INICIAL
        if (tarea.getEstado() == null) {
            tarea.setEstado(Tarea.EstadoTarea.PENDIENTE);
        }

        // GUARDAR TAREA
        Tarea tareaGuardada = tareaRepository.save(tarea);

        logger.info(
                "Tarea guardada correctamente. ID: {}",
                tareaGuardada.getId()
        );

        return tareaGuardada;
    }

    // ACTUALIZAR TAREA
    @Transactional
    public Optional<Tarea> actualizar(Long id, Tarea datos) {

        if (datos == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los datos de la tarea son obligatorios"
            );
        }

        return tareaRepository.findById(id).map(tarea -> {

            tarea.setTitulo(datos.getTitulo());
            tarea.setDescripcion(datos.getDescripcion());

            if (datos.getEstado() != null) {
                tarea.setEstado(datos.getEstado());
            }

            tarea.setProyecto(datos.getProyecto());
            tarea.setUsuarioAsignado(
                    datos.getUsuarioAsignado()
            );

            return guardar(tarea);
        });
    }

    // CAMBIAR ESTADO DE UNA TAREA
    @Transactional
    public Optional<Tarea> cambiarEstado(
            Long id,
            Tarea.EstadoTarea nuevoEstado
    ) {

        if (nuevoEstado == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar un estado valido"
            );
        }

        return tareaRepository.findById(id).map(tarea -> {

            tarea.setEstado(nuevoEstado);

            Tarea actualizada = tareaRepository.save(tarea);

            logger.info(
                    "Estado actualizado. Tarea ID: {}, estado: {}",
                    id,
                    nuevoEstado
            );

            return actualizada;
        });
    }

    // ELIMINAR TAREA
    @Transactional
    public boolean eliminar(Long id) {

        if (!tareaRepository.existsById(id)) {
            return false;
        }

        tareaRepository.deleteById(id);

        logger.info(
                "Tarea eliminada correctamente. ID: {}",
                id
        );

        return true;
    }
}
