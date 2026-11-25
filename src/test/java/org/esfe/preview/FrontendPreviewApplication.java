package org.esfe.preview;

import org.esfe.SistemaDeGestionDeProyectosColaborativoApplication;
import org.esfe.models.*;
import org.esfe.repositories.*;
import org.springframework.boot.SpringApplication;

import java.time.LocalDate;

/** Vista previa local y temporal. Solo se incluye en el classpath de pruebas. */
public class FrontendPreviewApplication {
    public static void main(String[] args) {
        var application = new SpringApplication(SistemaDeGestionDeProyectosColaborativoApplication.class);
        // Opciones explicitas para aislar la vista previa de variables de produccion.
        var context = application.run(
                "--server.port=8081", "--server.address=127.0.0.1",
                "--spring.datasource.url=jdbc:h2:mem:nexo-preview;DB_CLOSE_DELAY=-1",
                "--spring.datasource.driver-class-name=org.h2.Driver",
                "--spring.datasource.username=sa", "--spring.datasource.password=",
                "--spring.jpa.hibernate.ddl-auto=create-drop", "--spring.jpa.open-in-view=false",
                "--jwt.secret=TmV4b1ByZXZpZXdUZW1wb3JhcnlTZWNyZXRPbmx5Rm9yVGVzdGluZw==",
                "--app.bootstrap.admin-email=admin@nexo.local",
                "--app.bootstrap.admin-password=NexoPreview2026"
        );
        var categorias = context.getBean(CategoriaRepository.class);
        var proyectos = context.getBean(ProyectoRepository.class);
        var tareas = context.getBean(TareaRepository.class);
        var usuario = context.getBean(UsuarioRepository.class).findByEmail("admin@nexo.local").orElseThrow();
        var producto = categorias.save(new Categoria(null, "Producto"));
        var desarrollo = categorias.save(new Categoria(null, "Desarrollo"));
        var operaciones = categorias.save(new Categoria(null, "Operaciones"));
        String[] titulos = {"Una nueva experiencia digital", "Portal del equipo", "Procesos que conectan"};
        String[] descripciones = {"Diseñamos una experiencia más simple, cercana y útil para nuestros usuarios.",
                "El punto de encuentro para compartir recursos y construir en equipo.",
                "Pequeñas mejoras que hacen una gran diferencia en el trabajo diario."};
        Categoria[] grupos = {producto, desarrollo, operaciones};
        String[][] nombres = {{"Investigar necesidades de usuarios", "Diseñar la nueva navegación", "Preparar prototipo interactivo"},
                {"Definir la estructura del portal", "Conectar servicios de la API", "Revisar la experiencia móvil"},
                {"Documentar el proceso actual", "Compartir propuesta con el equipo", "Recoger comentarios"}};
        for (int i = 0; i < titulos.length; i++) {
            var proyecto = new Proyecto(null, titulos[i], descripciones[i], LocalDate.now().minusDays(7),
                    LocalDate.now().plusDays(14 + i * 7), grupos[i]);
            proyecto = proyectos.save(proyecto);
            for (int j = 0; j < 3; j++) {
                var tarea = new Tarea(null, nombres[i][j], "Coordinar los detalles y compartir avances con el equipo.",
                        j == 0 ? Tarea.EstadoTarea.COMPLETADA : j == 1 ? Tarea.EstadoTarea.EN_PROGRESO : Tarea.EstadoTarea.PENDIENTE,
                        proyecto, j != 2 ? usuario : null);
                tareas.save(tarea);
            }
        }
    }
}
