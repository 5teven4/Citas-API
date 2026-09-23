---
type: architecture
status: verified
updated: 2026-09-16
sources: [SRC-API-S2, SRC-MIGRATION-S2, SRC-WEB-S2, SRC-PRD, SRC-TECH]
---

# Implementación S2 — identidad y acceso

## Alcance implementado

`citas-api` está inicializado con Java 21, Spring Boot 3.5.x, Maven, JPA,
Spring Security, Flyway, MySQL y Actuator. La primera HU se expone mediante:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /actuator/health`

`citas-web` está inicializado con React, TypeScript y Vite. Incluye formularios
de registro e inicio de sesión que consumen la API por `VITE_API_URL`, sin BFF.
Su build de producción fue verificado en el contenedor Node del proyecto.

Passwords usan BCrypt. Los access y refresh son JWT distintos; el refresh se
persiste solo como SHA-256, tiene identificador JWT aleatorio para evitar
colisiones y se revoca en cada rotación o logout.

## Migración y conexión

`V1__auth_bootstrap.sql` crea de forma idempotente las tablas de autenticación
mínimas y los roles para una base vacía. En la base de referencia ya creada por
Docker, Flyway registra baseline versión 0 y aplica V1 sin alterar las tablas
existentes. La aplicación valida el esquema con JPA y conecta a `mysql:3306`
dentro de Compose; desde el host la base se publica en `localhost:3307`.

## Evidencia verificada

El 2026-09-16 se ejecutaron `mvn test`, health y una prueba HTTP con datos
sintéticos. Registro, bloqueo de duplicados, login, refresh rotativo, rechazo
del refresh previo y rechazo posterior a logout completaron correctamente. La
tabla `flyway_schema_history` contiene baseline `0` y migración `1` exitosas.

## Pendiente

La HU aún necesita pruebas automatizadas de controlador/persistencia para todos
sus criterios y la recuperación de contraseña del PRD queda fuera de este
incremento. Ver [[risks-open-questions]] y
[[../../scrum/historias-de-usuario/HU-001-registro-login-jwt]].
