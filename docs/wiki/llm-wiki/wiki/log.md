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

## [2026-09-25] learn | LOOP_00: auditoría S1–S3 y gate de cierre

- Fase alcanzada: 1–4 auditoría y consolidación; Fase 6 cerrada como
  `PENDIENTE_DECISION_HUMANA`.
- HECHO: ambos repositorios tienen solo `main` y un único commit inicial
  fechado 2026-09-23; no hay rama `develop` ni commits trazables `feat(s2)` o
  de S3.
- HECHO: `citas-api` contiene el incremento S2 de autenticación y la wiki
  registra evidencia previa de pruebas, pero en esta corrida `mvn` no está
  disponible en el entorno. `citas-web` contiene el formulario de
  registro/login, pero su build falla aquí con `spawn EPERM` de esbuild.
- HECHO: no hay implementaciones, HU aprobadas, pruebas específicas ni hooks
  versionados que demuestren los flujos de S3, Red→Green, secreto bloqueado y
  commit posterior permitido.
- Próxima acción: el estudiante debe aprobar las HU de S3 y decidir cómo
  restaurar/proveer la trazabilidad faltante y las herramientas locales antes
  de autorizar correcciones. No se inició S4–S6.

## [2026-09-25] learn | LOOP_00: corrección de evidencia de entorno

- HECHO: `npm run build` del checkout `citas-web` de este workspace completó
  correctamente fuera del sandbox (Vite 7.3.6). El `spawn EPERM` previo fue
  una limitación del sandbox, no un fallo del proyecto.
- HECHO: los contenedores Docker activos montan otro checkout,
  `C:\\Users\\IA ACADEMY 12\\Git\\FCV_Proyecto_Citas_v1`, con una aplicación
  Angular distinta. Sus resultados no se usan como evidencia de este
  workspace React/Spring.
- PREGUNTA ABIERTA: queda pendiente ejecutar Maven contra este checkout, que
  no incluye Maven Wrapper y no tiene `mvn` disponible en el host.

## [2026-09-25] learn | LOOP_00: verificación real de compilación

- HECHO: un contenedor Maven efímero montado exclusivamente sobre el checkout
  actual de `citas-api` ejecutó `mvn test` con éxito: 2 pruebas, 0 fallos y 0
  errores (`JwtServiceTest`).
- HECHO: el frontend React/Vite y las pruebas JWT pasan sus verificaciones
  disponibles, pero su cobertura no prueba aún los flujos ni las reglas de S3.

## [2026-09-25] learn | LOOP_00: quality gate local S3

- HECHO: se versionaron hooks `pre-commit` en ambos repositorios y se activaron
  localmente mediante `core.hooksPath=.githooks`.
- HECHO: el fixture sintético de secreto fue bloqueado por ambos hooks (FAIL).
  Tras retirarlo, el hook web ejecutó `npm run build` con éxito y el hook API
  ejecutó `mvn test` en un contenedor Maven con caché: 2 pruebas, 0 fallos.
- DECISIÓN PENDIENTE: este gate cubre secreto y verificación disponible; no
  sustituye las pruebas funcionales de S3, que requieren HU aprobadas e
  implementación de los flujos de citas.
