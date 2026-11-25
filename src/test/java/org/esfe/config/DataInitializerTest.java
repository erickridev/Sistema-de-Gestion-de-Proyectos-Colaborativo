package org.esfe.config;

import org.esfe.models.Rol;
import org.esfe.models.Usuario;
import org.esfe.repositories.RolRepository;
import org.esfe.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class DataInitializerTest {
    @Test
    void createsAdminWithEncodedPasswordAndIsIdempotent() {
        RolRepository roles = mock(RolRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        Rol admin = new Rol(1L, "ROLE_ADMIN");
        when(roles.findByNombre("ROLE_ADMIN")).thenReturn(Optional.of(admin));
        when(roles.findByNombre("ROLE_USER")).thenReturn(Optional.of(new Rol(2L, "ROLE_USER")));
        when(encoder.encode("ClaveSegura123")).thenReturn("hash");
        when(usuarios.existsByEmail("admin@example.com")).thenReturn(false, true);
        DataInitializer initializer = new DataInitializer(roles, usuarios, encoder,
                "ADMIN@example.com", "ClaveSegura123");
        initializer.run(null);
        initializer.run(null);
        var captor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios, times(1)).save(captor.capture());
        assertEquals("hash", captor.getValue().getPassword());
        assertEquals(admin, captor.getValue().getRol());
        assertEquals("admin@example.com", captor.getValue().getEmail());
    }

    @Test
    void rejectsPartialBootstrapCredentials() {
        RolRepository roles = mock(RolRepository.class);
        when(roles.findByNombre(anyString())).thenReturn(Optional.of(new Rol(1L, "ROLE_USER")));
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        DataInitializer initializer = new DataInitializer(roles, usuarios, mock(PasswordEncoder.class),
                "admin@example.com", "");
        assertThrows(IllegalStateException.class, () -> initializer.run(null));
        verifyNoInteractions(usuarios);
    }
}
