---
id: HU-003
tipo: historia-de-usuario
titulo: Ciclo de vida de citas y catálogos
estado: Aprobada
epica: "[[EP-003-ciclo-de-vida-y-operacion]]"
sprint_sugerido: S4
dependencias: [HU-001, HU-002, MySQL, Flyway]
---

# HU-003 — Ciclo de vida de citas y catálogos

Como USER quiero consultar, cancelar y solicitar reprogramación de mis citas;
como PROFESSIONAL quiero consultar mi agenda y registrar el resultado de una
atención; como ADMIN quiero decidir reprogramaciones y mantener los catálogos.

## Criterios de aceptación

1. USER consulta solo sus citas y su historial; cancelar una cita futura no
   terminal libera las reservas y registra el cambio.
2. USER solicita reprogramar solo una cita APPROVED futura, conservando
   profesional y especialidad; slots nuevos quedan retenidos y la cita original
   conserva su horario hasta decisión ADMIN.
3. ADMIN aprueba/rechaza reprogramaciones; aprobar mueve la cita y libera
   slots antiguos, rechazar libera solo la retención nueva.
4. PROFESSIONAL consulta exclusivamente su agenda y puede marcar una cita
   aplicable como COMPLETED o NO_SHOW.
5. ADMIN gestiona EPS, planes y especialidades mediante baja lógica cuando
   puedan existir referencias.
6. Recuperación de contraseña usa token de un solo uso con expiración; su
   exposición de desarrollo es opt-in y cambiar contraseña revoca refresh.
7. UI consume los endpoints, incluye estados vacíos/errores y funciona en
   mobile/desktop.

## Definition of Done

- [x] Migración Flyway aplicada en MySQL 8.4, desde esquema vacío y baseline del compose.
- [x] Pruebas MySQL de ownership, cancelación, reprogramación y cierre profesional.
- [x] Pruebas backend y build frontend pasan.
- [x] Contrato y wiki reflejan evidencia comprobada.

## Evidencia de validación

- 2026-09-25 — `mvn -Pintegration-tests verify`: 7 pruebas MySQL PASS en la
   instancia aislada; el test cubre además reserva duplicada, catálogo,
   autorización y recuperación.
- 2026-09-25 — `mvn test` PASS y `npm run build` PASS.
- 2026-09-25 — Smoke test HTTP con datos sintéticos: registro/login,
  `APPROVED`/`REQUESTED`, decisión ADMIN, reprogramación, cancelación y
  revocación de refresh (reutilización devuelve 401).
- 2026-09-25 — Demo aislada en `localhost:5175` + API `localhost:8081`, con
   MySQL temporal y usuario de aplicación con privilegio limitado.
- 2026-09-25 — LOOP_01 y LOOP_02 registrados en
   [[../evidencia-s4-loops]]. LOOP_03 requiere diseño del estudiante.

## Aprobación

- 2026-09-25 — Aprobada por instrucción explícita del estudiante: “quiero que
  termines la aplicación, cumpliendo con las sesiones restantes”.