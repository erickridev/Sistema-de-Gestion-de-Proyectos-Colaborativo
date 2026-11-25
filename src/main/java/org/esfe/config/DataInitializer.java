package org.esfe.config;

import org.esfe.models.Rol;
import org.esfe.models.Usuario;
import org.esfe.repositories.RolRepository;
import org.esfe.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {
    private final RolRepository roles;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(RolRepository roles, UsuarioRepository usuarios, PasswordEncoder encoder,
                           @Value("${app.bootstrap.admin-email:}") String adminEmail,
                           @Value("${app.bootstrap.admin-password:}") String adminPassword) {
        this.roles = roles;
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.adminEmail = adminEmail.trim().toLowerCase(java.util.Locale.ROOT);
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Rol admin = ensureRole("ROLE_ADMIN");
        ensureRole("ROLE_USER");
        if (adminEmail.isBlank() && adminPassword.isEmpty()) return;
        if (adminEmail.isBlank() || adminPassword.isBlank()
                || adminEmail.length() > 100
                || !adminEmail.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")
                || adminPassword.length() < 8
                || adminPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Configure un correo valido y una clave de administrador de 8 caracteres como minimo y 72 bytes como maximo");
        }
        // Nunca promover ni sobrescribir una cuenta existente desde la configuracion.
        if (usuarios.existsByEmail(adminEmail)) return;
        Usuario usuario = new Usuario();
        usuario.setLogin("admin");
        usuario.setNombre("Administrador");
        usuario.setEmail(adminEmail);
        usuario.setPassword(encoder.encode(adminPassword));
        usuario.setRol(admin);
        usuarios.save(usuario);
    }

    private Rol ensureRole(String nombre) {
        return roles.findByNombre(nombre).orElseGet(() -> roles.save(new Rol(null, nombre)));
    }
}
