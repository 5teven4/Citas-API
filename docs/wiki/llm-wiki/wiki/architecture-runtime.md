---
type: architecture
status: verified
updated: 2026-09-30
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

## Operación local con Docker Compose

**HECHO:** Los servicios `citas-api-dev` y `citas-web-dev` se mantienen activos
con `tail -f /dev/null`; que el contenedor figure como `Up` no implica que
Spring Boot o Vite estén ejecutándose. Para iniciar la API se usa:

```powershell
docker compose exec citas-api-dev mvn spring-boot:run
```

**HECHO:** El 30/09/2026 la API devolvía conexiones vacías en el puerto 8080
porque Spring Boot no estaba activo. Al iniciarlo, Flyway detuvo el arranque por
checksums diferentes en las migraciones V2 y V3 respecto de
`flyway_schema_history`.

**DECISIÓN:** Con autorización explícita y al tratarse de datos locales
sintéticos, se reconstruyó únicamente el volumen MySQL mediante
`scripts/reset-db.ps1 -Force`. No se eliminaron otros volúmenes ni archivos.
Tras el reset se verificaron 22 tablas, 2 sedes, 1 especialidad, 0 usuarios, 0
profesionales y 0 citas.

**HECHO:** Después de iniciar Spring Boot, `/actuator/health` respondió HTTP 200
y `POST /api/v1/auth/register` con un cuerpo vacío respondió HTTP 400 de
validación, confirmando que la API ya no producía `ERR_EMPTY_RESPONSE`.

## Reglas de seguridad

Secretos solo por variables de entorno; no deben versionarse ni registrarse.
Passwords se almacenan mediante hash adaptativo y los tokens de refresh/reset
se almacenan como hash, no en texto claro.
