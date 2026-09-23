---
id: HU-001
tipo: historia-de-usuario
titulo: Registro e inicio de sesión JWT
estado: Aprobada
epica: "[[EP-001-identidad-y-acceso]]"
esfuerzo: Alto
sprint_sugerido: S2
dependencias: [MySQL, Flyway]
---

# HU-001 — Registro e inicio de sesión JWT

## Historia de usuario

Como USER, quiero registrarme e iniciar sesión con mi email y contraseña para acceder de forma segura a las funciones de citas.

## Alcance

- Registro con nombres, apellidos, tipo/número de documento, email, teléfono y contraseña.
- Unicidad de email y documento.
- Login con access y refresh JWT; refresh rotativo y logout revocable.
- Rol USER inicial, password con BCrypt y persistencia mediante Flyway.

## Reglas de negocio

- Passwords no se almacenan ni registran en texto plano.
- Access y refresh son distintos; el refresh se persiste solo como hash.
- Email y documento son únicos.

## Criterios de aceptación

### CA-01 — Registro válido

Dado un visitante con datos válidos y únicos, cuando envía el registro, entonces se crea un USER activo con contraseña hasheada y rol USER.

### CA-02 — Duplicados bloqueados

Dado un email o documento existente, cuando se intenta registrar, entonces la API responde conflicto sin crear un segundo usuario.

### CA-03 — Login exitoso

Dado un USER con contraseña válida, cuando inicia sesión, entonces recibe un access JWT y un refresh JWT sin exponer el hash de contraseña.

### CA-04 — Refresh y logout

Dado un refresh vigente, cuando se refresca la sesión, entonces recibe un par nuevo y el token previo se revoca. Dado un logout, el refresh indicado deja de ser utilizable.

## Definition of Done

- [ ] Migración Flyway y restricciones de identidad verificadas.
- [ ] Pruebas automatizadas para CA-01 a CA-04.
- [ ] No hay secretos, passwords ni tokens en logs o respuestas indebidas.
- [ ] Contrato REST y wiki actualizados con evidencia.

## Aprobación

- 2026-09-16 — Aprobada para el incremento S2 a partir de la solicitud de continuar el entregable mínimo y de RF-01 a RF-03 del PRD.

## Evidencia de validación

- 2026-09-16 — `mvn test` PASS: emisión y validación de access/refresh JWT.
- 2026-09-16 — Prueba HTTP manual PASS con datos sintéticos: CA-01 a CA-04,
  incluidos duplicados, rotación y revocación por logout.
- 2026-09-16 — Flyway baseline `0` y migración `1` aplicados con éxito en
  MySQL 8.4.
- 2026-09-16 — `npm run build` PASS en `citas-web`; formularios de registro y
  login integrados por REST directo.
