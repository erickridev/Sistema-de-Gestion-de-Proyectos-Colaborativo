
package org.esfe.repositories;

import org.esfe.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar usuario por correo electrónico
    // Utilizado por Spring Security y JWT
    Optional<Usuario> findByEmail(String email);

    // Verificar si un correo ya está registrado
    boolean existsByEmail(String email);
}
