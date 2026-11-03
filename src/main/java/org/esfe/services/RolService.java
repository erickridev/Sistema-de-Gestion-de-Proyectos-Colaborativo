
package org.esfe.services;

import org.esfe.models.Rol;
import org.esfe.repositories.RolRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class RolService {

    private final RolRepository rolRepository;

    // INYECCION DE DEPENDENCIAS
    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    // LISTAR TODOS LOS ROLES
    @Transactional(readOnly = true)
    public List<Rol> listarTodos() {
        return rolRepository.findAll();
    }

    // BUSCAR ROL POR ID
    @Transactional(readOnly = true)
    public Optional<Rol> buscarPorId(Long id) {
        return rolRepository.findById(id);
    }

    // GUARDAR O ACTUALIZAR ROL
    @Transactional
    public Rol guardar(Rol rol) {

        if (rol == null ||
                rol.getNombre() == null ||
                rol.getNombre().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre del rol es obligatorio"
            );
        }

        // Normalizar nombre del rol
        String nombre = rol.getNombre()
                .trim()
                .toUpperCase();

        if (!nombre.startsWith("ROLE_")) {
            nombre = "ROLE_" + nombre;
        }

        rol.setNombre(nombre);

        // Evitar nombres duplicados
        Optional<Rol> rolExistente =
                rolRepository.findByNombre(nombre);

        if (rolExistente.isPresent() &&
                !rolExistente.get().getId().equals(rol.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un rol con ese nombre"
            );
        }

        return rolRepository.save(rol);
    }

    // ELIMINAR ROL
    @Transactional
    public boolean eliminar(Long id) {

        Optional<Rol> rolOptional = rolRepository.findById(id);

        if (rolOptional.isEmpty()) {
            return false;
        }

        Rol rol = rolOptional.get();

        // Proteger roles principales
        if ("ROLE_ADMIN".equals(rol.getNombre()) ||
                "ROLE_USER".equals(rol.getNombre())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No se pueden eliminar los roles principales"
            );
        }

        try {
            rolRepository.delete(rol);
            rolRepository.flush();

        } catch (DataIntegrityViolationException ex) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el rol porque tiene usuarios asociados"
            );
        }

        return true;
    }
}
