# 🚀 Sistema de Gestión de Proyectos Colaborativo

Un sistema API RESTful desarrollado con **Spring Boot 3** y **Java 21** para la administración colaborativa de proyectos y tareas. Incluye autenticación y autorización mediante **Spring Security** y **JWT**, gestión de persistencia con **Spring Data JPA**, validación de datos integrada y una suite completa de pruebas unitarias e integración.

---

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 21
* **Framework:** Spring Boot 3
* **Seguridad:** Spring Security & JWT (JSON Web Tokens)
* **Persistencia:** Spring Data JPA / Hibernate
* **Base de Datos:** MySQL / H2 (en memoria para pruebas)
* **Herramientas de Utilidad:** Lombok, Jakarta Bean Validation
* **Pruebas:** JUnit 5, Mockito, MockMvc
* **Construcción:** Maven

---

## 📋 Características Principales

* 🔐 **Autenticación & Autorización JWT:** Registro e inicio de sesión con generación de tokens sin estado (Stateless).
* 📁 **Gestión de Proyectos (CRUD):** Creación, consulta, edición y eliminación de proyectos.
* 📝 **Gestión de Tareas (CRUD):** Asignación de tareas por proyecto y por usuario con estados de avance (`PENDIENTE`, etc.).
* 🛡️ **Manejo Global de Excepciones:** Intercepción centralizada de errores (`GlobalExceptionHandler`) devolviendo respuestas JSON estructuradas (`400 Bad Request`, `401 Unauthorized`, `404 Not Found`).
* ✅ **Validación de Datos:** Uso de `@Valid` para restringir formatos de correo, longitudes de contraseña y campos obligatorios.
* 🧪 **Suite de Pruebas Automáticas:** Pruebas unitarias de servicios y pruebas de integración para controladores con `MockMvc`.

---

## 🚀 Guía de Inicio Rápido

### Prerrequisitos

* **JDK 21** o superior instalado.
* **Maven** instalado (o usar el ejecutable `./mvnw` incluido).
* **MySQL** (configurado según propiedades).

### Instalación y Ejecución Local

1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/erickridev/Sistema-de-Gestion-de-Proyectos-Colaborativo.git](https://github.com/erickridev/Sistema-de-Gestion-de-Proyectos-Colaborativo.git)
   cd Sistema-de-Gestion-de-Proyectos-Colaborativo

    Configurar la base de datos:
    Configura tus credenciales de MySQL en src/main/resources/application.properties:
    Properties

    spring.datasource.url=jdbc:mysql://localhost:3306/proyectoapi
    spring.datasource.username=tu_usuario
    spring.datasource.password=tu_contraseña
    spring.jpa.hibernate.ddl-auto=update

    Ejecutar la aplicación:
    Bash

    ./mvnw spring-boot:run

    La API estará disponible en http://localhost:8080.

🧪 Ejecución de Pruebas

Para ejecutar la suite completa de pruebas automáticas (13 tests):
Bash

./mvnw test

📬 Endpoints Principales
Autenticación (/api/auth)

    POST /api/auth/register - Registro de nuevos usuarios.

    POST /api/auth/login - Autenticación y obtención de token JWT.

Proyectos (/api/proyectos) (Requiere Token JWT)

    GET /api/proyectos - Listar todos los proyectos.

    GET /api/proyectos/{id} - Obtener un proyecto por ID.

    POST /api/proyectos - Crear un nuevo proyecto.

    PUT /api/proyectos/{id} - Actualizar un proyecto existente.

    DELETE /api/proyectos/{id} - Eliminar un proyecto.

Tareas (/api/tareas) (Requiere Token JWT)

    GET /api/tareas - Listar tareas.

    GET /api/tareas/{id} - Obtener detalle de una tarea.

    POST /api/tareas - Crear una tarea asignada.

    PUT /api/tareas/{id} - Actualizar tarea.

    DELETE /api/tareas/{id} - Eliminar tarea.