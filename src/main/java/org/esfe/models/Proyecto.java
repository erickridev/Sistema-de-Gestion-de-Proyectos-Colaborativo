
package org.esfe.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "proyectos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @NotNull(message = "La categoria es obligatoria")
    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @JsonIgnore
    @AssertTrue(message = "La categoria debe tener un ID valido")
    public boolean isCategoriaValida() {
        return categoria == null || (categoria.getId() != null && categoria.getId() > 0);
    }

    @JsonIgnore
    @AssertTrue(message = "La fecha de fin no puede ser anterior a la de inicio")
    public boolean isFechasValidas() {
        return fechaInicio == null || fechaFin == null || !fechaFin.isBefore(fechaInicio);
    }
}
