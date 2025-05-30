# 🗒️ Blog App - API

API REST desarrollada con Spring Boot y PostgreSQL, diseñada para gestionar publicaciones, usuarios y categorías en un blog. Este proyecto aplica principios de arquitectura limpia, **seguridad JWT**, **gestión de estados de posts**, notificaciones por correo y documentación interactiva para una experiencia robusta y escalable.

---
## ✨ Características Principales

- **Registro y autenticación segura**:
    - Registro con validación de datos y envío de **correo de bienvenida** (Spring Mail Starter).
    - Logueo con JWT para acceso a endpoints protegidos.
- **Gestión de Posts**:
    - Creación, edición y eliminación de posts en estado **BORRADOR** o **PUBLICADO**.
- **Gestión de Roles (Spring Security)**:
    - **Usuario Estándar**: Publicar, guardar borradores y gestionar sus posts.
    - **Administrador**: Control total sobre posts, usuarios, categorías y etiquetas.
- **Paginación**: Listado de posts públicos con paginación (usando `Pageable` de Spring Data JPA).
- **Documentación Integrada**: Swagger UI para explorar y probar endpoints.
- **Multi-entornos**: Configuración separada para **local**, **dev** y **prod** usando Spring Profiles.
- **Pruebas Automatizadas**:
    - **Unitarias**: Pruebas a los controllers y services con JUnit y Mockito.
    - **Integración**: Pruebas a los repositories con `@DataJpaTest`.

---
## 🛠 **Tecnologías**

- **Spring Boot 3**: Arquitectura modular y configuración ágil.
- **PostgreSQL**: Almacenamiento persistente y relaciones entre entidades.
- **Docker y Docker Compose**: Empaquetado y despliegue de la base de datos.
- **Spring Security + JWT**: Autenticación stateless y protección de rutas.
- **Spring Data JPA**: Paginación, queries personalizadas y repositorios.
- **Swagger (OpenAPI 3)**: Documentación interactiva en `/swagger-ui/index.html`.
- **Spring Boot Starter Mail**: Notificaciones por correo electrónico.
- **Spring Profiles**: Configuración flexible para entornos `local`, `dev`, `prod`.
- **Lombok**: Simplificación de código.
- **JUnit 5 y Mockito**: Pruebas unitarias y de integración.

---
## 🧩 **Arquitectura**

- **Capas Claramente Definidas**:
    - Controladores REST (Manejo de solicitudes HTTP).
    - Servicios (Lógica de negocio y validaciones).
    - Repositorios (Comunicación con PostgreSQL).
- **DTOs y Mappers**: Transferencia segura de datos y conversión entre entidades.
- **Entidades con Estados**: Posts con campo `status` (Ej: `DRAFT`, `PUBLISHED`).

---
## 🔌 **Endpoints Clave**

```plaintext
# Autenticación  
POST /api/v1/auth/signup   → Registro + correo de bienvenida.  
POST /api/v1/auth/login    → Genera token JWT.  

# Posts  
POST /api/v1/posts          → Crea post.
GET /api/v1/posts?page=0&size=10  → Lista posts públicos paginados.  

# Administración (solo ADMIN)  
GET /api/v1/users          → Lista todos los usuarios.  
DELETE /api/v1/posts/{id}  → Elimina cualquier post.  

# Documentación  
GET /swagger-ui/index.html  → Interfaz de Swagger.  
```

---
## 🧪 Ejecución de Pruebas

```shell
mvn test
```

---
## 🧑‍💻 Autor

Stefano Fabricio Rodriguez Avalos

[LinkedIn](https://www.linkedin.com/in/stefanofabriciorodriguezavalos)

