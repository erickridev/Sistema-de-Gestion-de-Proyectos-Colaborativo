
package org.esfe.repositories;

import org.esfe.models.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    // Buscar tareas asignadas a un usuario
    List<Tarea> findByUsuarioAsignadoId(Long usuarioId);

    // Buscar tareas pertenecientes a un proyecto
    List<Tarea> findByProyectoId(Long proyectoId);

    // Buscar tareas por estado
    List<Tarea> findByEstado(Tarea.EstadoTarea estado);

    // Buscar tareas de un proyecto por estado
    List<Tarea> findByProyectoIdAndEstado(
            Long proyectoId,
            Tarea.EstadoTarea estado
    );

    // Buscar tareas asignadas a un usuario dentro de un proyecto
    List<Tarea> findByProyectoIdAndUsuarioAsignadoId(
            Long proyectoId,
            Long usuarioId
    );
}
