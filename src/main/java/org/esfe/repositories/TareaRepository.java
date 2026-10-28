package org.esfe.repositories;

import org.esfe.models.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {
    List<Tarea> findByUsuarioAsignadoId(Long usuarioId);
    List<Tarea> findByProyectoId(Long proyectoId);
}