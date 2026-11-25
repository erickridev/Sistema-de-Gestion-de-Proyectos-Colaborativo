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

1. Clona el repositorio y entra en su carpeta:

```powershell
git clone https://github.com/erickridev/Sistema-de-Gestion-de-Proyectos-Colaborativo.git
cd Sistema-de-Gestion-de-Proyectos-Colaborativo
```

2. Crea la base MySQL `proyectoapi` y define las variables de entorno:

```powershell
$env:DB_USERNAME="tu_usuario"
$env:DB_PASSWORD="tu_contraseña"
$env:JWT_SECRET="clave_aleatoria_en_base64_de_al_menos_32_bytes"
```

Puedes generar `JWT_SECRET` con PowerShell:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
```

3. Para crear el primer administrador, define antes del primer arranque:

```powershell
$env:BOOTSTRAP_ADMIN_EMAIL="admin@ejemplo.com"
$env:BOOTSTRAP_ADMIN_PASSWORD="elige_una_clave_segura"
```

Los roles `ROLE_USER` y `ROLE_ADMIN` se crean automáticamente. El administrador es opcional y solo se crea si el correo no existe; nunca se cambia el rol ni la clave de una cuenta existente. Retira estas dos variables tras crearlo. El registro público siempre asigna `ROLE_USER`.

4. Con JDK 21 y `JAVA_HOME` configurado, ejecuta:

```powershell
.\mvnw.cmd package
& "$env:JAVA_HOME/bin/java.exe" -jar .\target\Sistema-de-Gestion-de-Proyectos-Colaborativo-0.0.1-SNAPSHOT.jar
```

La API estará disponible en http://localhost:8080 y Swagger en http://localhost:8080/swagger-ui.html.

### Pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas usan H2 en memoria y una clave JWT exclusiva de pruebas; no necesitan MySQL ni credenciales de producción. Incluyen comprobaciones de permisos con los filtros de seguridad activos.

### Endpoints principales

- `POST /api/auth/register` y `POST /api/auth/login`: públicos.
- `/api/proyectos`, `/api/categorias` y `/api/tareas`: consultas para `USER` y `ADMIN`; escrituras para `ADMIN`.
- `/api/usuarios` y `/api/roles`: solo `ADMIN`.
- `PATCH /api/tareas/{id}/estado`: cambio de estado, solo `ADMIN`.

Los POST de proyectos y categorías ignoran el ID enviado. Las entradas inválidas devuelven `400`; los conflictos de integridad devuelven `409`.

Los proyectos resuelven su categoría desde la base de datos y devuelven `404` si no existe. Las actualizaciones se ejecutan en transacciones: si una relación es inválida, no se guardan cambios parciales. No se eliminan automáticamente tareas o proyectos asociados; una eliminación que rompa sus relaciones devuelve `409`.

Las contraseñas admiten entre 8 y 72 caracteres y un máximo de 72 bytes UTF-8. Las respuestas de seguridad `401` y `403` usan el mismo JSON de error de la API (`status`, `message`, `timestamp`). Los errores de método o formato HTTP conservan sus códigos `405` y `415`.

`DB_URL` permite cambiar la URL de MySQL. La configuración desactiva las consultas durante la serialización (`open-in-view`) y la impresión de SQL.

### Frontend Nexo

La interfaz se sirve desde la misma aplicación en `http://localhost:8080/`. No necesita instalar dependencias de Node ni ejecutar otro servidor. Está implementada con HTML, CSS y módulos JavaScript en `src/main/resources/static`.

- Inicio de sesión y registro conectados a la API.
- Resumen con métricas y progreso calculados a partir de las tareas reales.
- Proyectos con búsqueda, filtro por categoría y fechas.
- Tablero de tareas con búsqueda, filtros por proyecto y estado, y vista «Mis tareas».
- Administradores: crear, editar y eliminar proyectos, tareas, categorías y roles personalizados; asignar tareas y administrar usuarios.
- Miembros: consultar proyectos, categorías y tareas. Los permisos se verifican en el servidor.
- Diseño adaptable a computadora, tablet y móvil: navegación plegable, tablas como fichas en pantallas pequeñas, controles táctiles y formularios con acciones visibles. En teléfonos, las tablas conservan los mismos datos y las mismas acciones que en escritorio.
- Sesión JWT en `sessionStorage`, cierre de sesión y control de expiración. `GET /api/auth/me` devuelve la identidad y los permisos actuales sin exponer contraseñas.

#### Vista previa sin MySQL

Con JDK 21 y `JAVA_HOME` configurado:

```powershell
.\scripts\preview.ps1
```

Abre `http://localhost:8081/` e inicia sesión con `admin@nexo.local` y `NexoPreview2026`. Este modo usa exclusivamente el classpath de pruebas, H2 en memoria y datos ilustrativos que desaparecen al detenerlo. Se limita a la interfaz local y fuerza una base de datos y credenciales independientes de las variables de producción. Las credenciales de vista previa no se incluyen en la aplicación de producción.

#### Pruebas de frontend

Con Node.js instalado:

```powershell
node --test src/test/frontend/core.test.mjs
```

Las pruebas de Maven también comprueban que la interfaz es pública y que los endpoints de datos siguen protegidos.
