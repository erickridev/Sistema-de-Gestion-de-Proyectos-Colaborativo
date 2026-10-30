package org.esfe.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Ignora las propiedades generadas por UserDetails para que Jackson no falle en Swagger / REST
@JsonIgnoreProperties(
        value = {"authorities", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "username"},
        allowGetters = false
)
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String login;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Solo se permite escribir la contraseña en la petición; nunca se devuelve en el JSON de respuesta
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    // --- Métodos obligatorios de UserDetails para Spring Security ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (rol != null && rol.getNombre() != null) {
            String nombreRol = rol.getNombre();
            // Evita duplicar el prefijo si el rol ya se llama 'ROLE_ADMIN'
            if (!nombreRol.startsWith("ROLE_")) {
                nombreRol = "ROLE_" + nombreRol;
            }
            return List.of(new SimpleGrantedAuthority(nombreRol));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return this.email; // Se utiliza el email como identificador principal para el login
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}