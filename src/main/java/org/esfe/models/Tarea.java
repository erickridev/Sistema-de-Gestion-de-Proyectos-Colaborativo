
package org.esfe.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.*;

@Entity
@Table(name = "tareas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tarea {

    // IDENTIFICADOR
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TITULO DE LA TAREA
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "El título no puede superar 150 caracteres")
    @Column(nullable = false, length = 150)
    private String titulo;

    // DESCRIPCION DE LA TAREA
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // ESTADO DE LA TAREA
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoTarea estado = EstadoTarea.PENDIENTE;

    // PROYECTO AL QUE PERTENECE
    @NotNull(message = "Debe seleccionar un proyecto")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proyecto_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Proyecto proyecto;

    // USUARIO ASIGNADO A LA TAREA
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler",
            "password",
            "authorities",
            "accountNonExpired",
            "accountNonLocked",
            "credentialsNonExpired",
            "enabled",
            "username"
    })
    private Usuario usuarioAsignado;

    // ESTADOS DISPONIBLES
    public enum EstadoTarea {
        PENDIENTE,
        EN_PROGRESO,
        COMPLETADA
    }

    // ESTABLECER ESTADO INICIAL
    @PrePersist
    public void establecerEstadoInicial() {
        if (estado == null) {
            estado = EstadoTarea.PENDIENTE;
        }
    }
}
