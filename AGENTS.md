# Instrucciones de `citas-api`

## Misión

Implementa la API del sistema FCV Citas con Java 21, Spring Boot 3.5.x, Maven, arquitectura hexagonal, MySQL 8.4, Flyway, Spring Security y REST/JSON.

## Arquitectura

- El dominio y los casos de uso no dependen de Spring, JPA ni HTTP.
- Persistencia, seguridad y controladores son adaptadores.
- Los controladores validan y traducen HTTP; no concentran reglas de negocio.
- Los cambios de esquema usan migraciones Flyway y se contrastan con `database/reference/db.sql`.

## Seguridad y datos

- Passwords mediante BCrypt; nunca en texto plano ni logs.
- Access y refresh JWT separados; el refresh se guarda solo como hash y puede revocarse.
- Secretos exclusivamente por variables de entorno.
- Aplica autorización por rol y ownership cuando existan endpoints protegidos.

## Flujo de trabajo

1. Lee la HU aprobada en `docs/wiki/scrum/` y las fuentes de la wiki global.
2. Identifica reglas, contratos, migraciones y pruebas afectadas.
3. Implementa el cambio mínimo coherente.
4. Ejecuta pruebas unitarias e integración relevantes.
5. Actualiza la wiki solo con hechos comprobados, decisiones aprobadas o preguntas abiertas durables.

No edites `citas-web` desde este repositorio.
