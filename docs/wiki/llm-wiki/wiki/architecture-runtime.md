---
type: architecture
status: verified
updated: 2026-09-16
sources: [SRC-README, SRC-TECH, SRC-COMPOSE]
---

# Arquitectura y ejecución

## Componentes

- `citas-api`: Java 21, Spring Boot 3.5.x, Maven, arquitectura hexagonal,
  Spring Data JPA, Flyway y MySQL 8.4.
- `citas-web`: TypeScript con React o Angular; consume REST directamente, sin
  Express ni BFF.
- MySQL se publica en `localhost:3307` para procesos del host. Dentro de
  Docker Compose, el API usa `mysql:3306`.

## Persistencia

`database/reference/db.sql` carga el esquema inicial del laboratorio cuando se
crea el volumen MySQL. El backend real debe evolucionar su base mediante
migraciones Flyway. El incremento S2 aplica la migración inicial de
autenticación y usa baseline para convivir con el esquema de referencia ya
existente. Ver [[implementation-s2-auth]] y [[data-model]].

## Reglas de seguridad

Secretos solo por variables de entorno; no deben versionarse ni registrarse.
Passwords se almacenan mediante hash adaptativo y los tokens de refresh/reset
se almacenan como hash, no en texto claro.
