# Registro cronológico de la LLM Wiki

> Append-only. Cada entrada inicia con `## [YYYY-MM-DD] tipo | título`.

## [2026-09-16] bootstrap | Wiki global FCV Citas

- Creada la estructura `raw/`, `wiki/` y `schema/`.
- Añadido `AGENTS.md` con flujos INGEST, QUERY, LEARN y LINT.
- Inicializado el índice, las páginas de proyecto, dominio, arquitectura,
  datos, decisiones y riesgos.

## [2026-09-16] ingest | Especificaciones y esquema iniciales

- Registradas nueve fuentes canónicas con ruta y SHA-256 en
  `raw/source-registry.md`.
- Integradas reglas de citas, arquitectura MySQL/Flyway y normalización 3FN.
- Sin contradicciones críticas detectadas entre PRD, restricciones y esquema.
- Pendiente: ingesta de Scrum cuando se aprueben las historias de usuario.

## [2026-09-16] lint | Integridad inicial de la wiki

- Verificados 13 archivos Markdown de la wiki y sus enlaces internos.
- Verificados los hashes de las nueve fuentes registradas contra sus rutas
  canónicas del workspace.
- Resultado: PASS. Sin enlaces rotos ni desajustes de fuente detectados.

## [2026-09-16] learn | Incremento S2 de autenticación

- Registradas las fuentes `SRC-API-S2` y `SRC-MIGRATION-S2`.
- Creada [[implementation-s2-auth]] con endpoints, Flyway, seguridad y
  evidencia de integración.
- Verificados registro, duplicados, login, rotación/revocación de refresh,
  health y versiones Flyway 0/1 con datos sintéticos.

## [2026-09-16] learn | Frontend inicial S2

- Registrada `SRC-WEB-S2` y actualizado el incremento S2 con React/TypeScript/Vite.
- Verificado `npm run build` PASS; los formularios consumen registro y login
  directamente contra `citas-api` mediante `VITE_API_URL`.

## [2026-09-16] lint | Cierre técnico de S2

- Verificados 14 archivos Markdown, enlaces internos y hashes de las 12 fuentes
  registradas.
- Confirmadas salidas de `mvn test` y `npm run build`.
- Resultado: PASS. MySQL permanece saludable; los contenedores temporales de
  pruebas de API fueron retirados.
