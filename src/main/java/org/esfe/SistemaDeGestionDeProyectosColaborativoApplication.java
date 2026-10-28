package org.esfe;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SecurityScheme(
		name = "Bearer Authentication",
		type = SecuritySchemeType.HTTP,
		bearerFormat = "JWT",
		scheme = "bearer"
)
@SpringBootApplication
public class SistemaDeGestionDeProyectosColaborativoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemaDeGestionDeProyectosColaborativoApplication.class, args);
	}

}