# citas-api

Repositorio backend del proyecto. **No contiene implementación de negocio inicial**.

## Incremento S2 implementado
- Java 21 + Spring Boot 3.5.x + Maven.
- MySQL 8.4 + Flyway (`V1__auth_bootstrap.sql`).
- Registro, login, refresh rotativo y logout con JWT.
- Passwords BCrypt; refresh tokens hasheados y revocables.
- Health: `GET /actuator/health`.

## Ejecutar

Desde la raíz, inicia MySQL con `docker compose up -d mysql`. Para ejecutar la
API con el toolchain incluido:

```powershell
docker compose run --rm --service-ports citas-api-dev mvn spring-boot:run
```

Endpoints iniciales:
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

## Documentación compartida
- `docs/wiki/scrum/`: épicas/HU generadas con la Skill Scrum.
- `docs/wiki/llm-wiki/`: única LLM Wiki global del workspace.
- `automations/n8n/`: JSON exportados en S5/S6.

Lee el PRD en la carpeta raíz antes de inicializar Spring Boot.
# Hooks locales

Activa los hooks versionados antes de hacer commits:

```sh
git config core.hooksPath .githooks
```

El hook `pre-commit` bloquea archivos de entorno, patrones de secretos en los
cambios preparados y ejecuta `mvn test` (o un contenedor Maven efímero cuando
Maven no está instalado en el host).
