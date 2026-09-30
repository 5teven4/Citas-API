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

## [2026-09-25] learn | LOOP_00: incremento S3 autorizado

- HECHO: se aprobó y documentó HU-002, EP-002, casos de prueba S3 y contrato
  REST por instrucción del estudiante.
- HECHO: V2 incorpora profesionales, disponibilidad, slots, reservas, estados
  e historial; el backend expone administración, bloques, catálogos,
  disponibilidad, reserva y decisión administrativa.
- HECHO: `mvn test` PASS con 5 pruebas, incluidas reglas 30/60 y reserva
  incompatible; `npm run build` PASS tras incorporar cliente REST S3.
- PREGUNTA ABIERTA: faltan pruebas de integración MySQL y pantallas de roles
  ADMIN/PROFESSIONAL antes de declarar S3 en verde.

## [2026-09-25] learn | LOOP_00: continuación en Fase 6

- HECHO: se añadió `GET /api/v1/admin/appointments/requests`, protegido para
  ADMIN, y se documentó en el contrato S3. La API devuelve datos sintéticos
  de citas `REQUESTED` necesarios para decidir.
- HECHO: la UI S3 ahora lee `roles` del access token y carga catálogos REST;
  ofrece reserva USER, publicación de bloques PROFESSIONAL y creación de
  profesionales/aprobación o rechazo ADMIN, sin IDs de catálogo fijos.
- HECHO: `mvn test` PASS, 6 pruebas (3 SlotRules, 2 JwtService, 1
  SchedulingService); `npm run build` PASS (TypeScript y Vite).
- HECHO: ambos repos están en `develop`, con hooks `pre-commit` activados;
  el último commit rastreado en cada uno es `test(s3): add local quality
  gates`. El historial no demuestra commits de sesión S2 ni S3; hay cambios
  S3 locales todavía no versionados.
- PREGUNTA ABIERTA: Fase 6 continúa `PENDIENTE_DECISION_HUMANA`. Aún faltan
  pruebas S3 contra MySQL y autorización/end-to-end por rol; el build no las
  demuestra. Tampoco hay evidencia histórica suficiente para reconstruir
  trazabilidad de commits S2/S3 sin crear commits nuevos o inventar historial.
- Próxima acción: decidir cómo aportar/aceptar trazabilidad histórica y
  habilitar validación S3 integrada; no iniciar backlog S4-S6 hasta que el
  gate S1-S3 sea PASS.

## [2026-09-25] learn | S4: ciclo de vida y cierre del alcance

- HECHO: a petición explícita del estudiante, el alcance de esta corrida se
  limitó hasta S4; no se iniciaron cambios de S5/S6 ni workflows n8n.
- HECHO: se añadieron V3, ciclo de citas, reprogramación transaccional,
  agenda/cierre PROFESSIONAL, filtros/historial USER, catálogos CRUD con baja
  lógica y recuperación de password con token hash/expirable/de un solo uso.
- HECHO: migraciones V1–V3 y validación JPA pasan tanto en MySQL vacío como
  sobre `database/reference/db.sql`, el baseline del compose.
- HECHO: `mvn -Pintegration-tests verify` pasó 7 pruebas MySQL; `mvn test`
  y `npm run build` pasaron. La UI consume los contratos S4 y el frontend
  local respondió HTTP 200.
- HECHO: `LOOP_01` tiene evidencia RED→GREEN de segunda reserva; `LOOP_02`
  pasó verificación técnica de reprogramación, retención/transferencia de
  slots, estados visibles y build. Ver [[../scrum/evidencia-s4-loops]].
- PREGUNTA ABIERTA: `LOOP_03` queda `PENDIENTE_ESTUDIANTE`: el reto debe ser
  diseñado y justificado por el estudiante, no por el agente.
- PREGUNTA ABIERTA: la activación del token temporal de recuperación es
  opt-in local; SMTP sigue sin configurarse. No se hizo merge a `main` ni se
  crearon commits de sesión sin petición explícita.
- Próxima acción: el estudiante diseña LOOP_03 y decide si versiona/integra
  S4; no continuar a S5/S6 en esta corrida.

## [2026-09-25] verify | S4: smoke test de contratos y sesión

- HECHO: el checkout respondió HTTP 200 en la UI local y Spring Boot inició
  en el puerto alternativo 8081 contra `citas_s4_empty_validation`; el
  contenedor que reserva 8080 no fue modificado.
- HECHO: smoke test HTTP sintético comprobó registro/login, cita general
  `APPROVED`, especializada `REQUESTED`, decisión ADMIN, reprogramación
  visible `PENDING`, aprobación con cambio de horario y cancelación.
- HECHO: cierre de sesión revocó refresh; reutilizarlo devolvió HTTP 401.
- HECHO: `mvn -Pintegration-tests verify` terminó con 6 pruebas unitarias y
  7 de integración MySQL en PASS; `npm run build` PASS.
- ALCANCE: toda la prueba manual usó usuarios ficticios en la base temporal
  local `citas_s4_empty_validation`. No se usó ni modificó la base regular.
- PREGUNTA ABIERTA: LOOP_03 sigue esperando el reto diseñado por el estudiante.

## [2026-09-25] verify | S4: demo local aislada

- HECHO: el frontend del checkout corre en `http://localhost:5175/` y la API
  en `http://localhost:8081/`; ambos responden correctamente.
- HECHO: la demo apunta únicamente a `citas_s4_empty_validation`, un MySQL
  temporal con datos sintéticos. El usuario runtime tiene privilegios solo
  sobre esa base y root requiere contraseña temporal generada en memoria.
- HECHO: se dejó intacto el contenedor existente que reserva el puerto 8080
  y no se usó la base habitual del compose.
- ALCANCE: las sesiones S5/S6 y n8n no se tocaron. LOOP_03 sigue pendiente
  de diseño por el estudiante.
## [2026-09-30] learn | actualización UX/UI del frontend

- HECHO: `citas-web/src/styles/tokens.css` centraliza tokens de color,
  tipografía, espaciado, bordes, foco, feedback y controles.
- HECHO: `citas-web/src/styles.css` aplica la paleta corporativa y estados
  visuales consistentes sin modificar API, payloads ni handlers.
- DECISIÓN: mantener el flujo funcional existente y no inventar router/sidebar
  hasta que existan rutas respaldadas por el contrato del frontend.
- VALIDACIÓN: `npx tsc -b` pasó; la revisión visual con lector de pantalla y
  viewports queda pendiente.
- Ver [[frontend-ux-ui]].

## [2026-09-30] learn | referencia visual para autenticación

- DECISIÓN: usar la página pública de citas de EPS Sanitas únicamente como
  referencia de composición y jerarquía, conservando identidad y contenido de
  FCV Citas.
- HECHO: la autenticación adopta composición dividida en desktop y una sola
  columna en móvil; no se modificaron contratos REST ni handlers.
- VALIDACIÓN: `npx tsc -b` y `npm run build` pasaron.
- Ver [[frontend-ux-ui]].

## [2026-09-30] verify | recuperación de API local y MySQL

- HECHO: el frontend recibía `ERR_EMPTY_RESPONSE` al invocar
  `POST /api/v1/auth/register` en `localhost:8080`.
- HECHO: `citas-api-dev` estaba `Up`, pero ejecutaba únicamente
  `tail -f /dev/null`; no había un proceso Java escuchando en el puerto 8080.
- HECHO: al iniciar Spring Boot, Flyway detectó checksums diferentes para V2 y
  V3 y detuvo el arranque para proteger la base existente.
- DECISIÓN: con autorización explícita del usuario se eliminó y reconstruyó
  solo el volumen `fcv-citas-training_mysql_data`, que contenía datos locales
  sintéticos.
- VALIDACIÓN: el esquema reconstruido contiene 22 tablas, 2 sedes y 1
  especialidad; `/actuator/health` respondió HTTP 200 y el endpoint de registro
  respondió HTTP 400 ante un cuerpo inválido, en lugar de cerrar la conexión.
- Ver [[architecture-runtime]] y [[risks-open-questions]].

## [2026-09-30] learn | MUI y semilla de doctores para agendamiento

- DECISIÓN: incorporar Material UI con un tema centralizado para mejorar el
  flujo de agendamiento sin cambiar el contrato REST.
- HECHO: el flujo usa Autocomplete para especialidad, sede y profesional,
  Stepper para el progreso, Alert para estados y botones de horario legibles.
- HECHO: la migración V4 crea tres doctores, asociaciones sintéticas y
  disponibilidad futura para pruebas manuales.
- VALIDACIÓN: `npm run build` pasó; la API devolvió doctores para Medicina
  General y Cardiología en las sedes semilla.
- FUENTES UX: documentación oficial de MUI y material público de MinSalud y
  MedlinePlus sobre preparación y participación segura en citas médicas.
- Ver [[frontend-ux-ui]].

