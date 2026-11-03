
package org.esfe.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Debe proporcionar una dirección de correo válida")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
