---
id: HU-002
tipo: historia-de-usuario
titulo: Disponibilidad, reserva y decisión administrativa
estado: Aprobada
epica: "[[EP-002-agendamiento-y-aprobacion]]"
sprint_sugerido: S3
dependencias: [HU-001, MySQL, Flyway]
---

# HU-002 — Disponibilidad, reserva y decisión administrativa

Como USER quiero consultar horarios y solicitar una cita ficticia, para
agendar atención según la disponibilidad de un PROFESSIONAL. Como ADMIN quiero
gestionar profesionales y decidir las solicitudes especializadas; como
PROFESSIONAL quiero publicar bloques de disponibilidad.

## Criterios de aceptación

1. ADMIN puede crear/consultar profesionales sintéticos, asignarles sede y
   especialidad.
2. PROFESSIONAL solo puede crear, listar y retirar sus propios bloques futuros;
   no puede crear bloques solapados ni en sedes no asignadas.
3. USER puede consultar slots libres por sede, especialidad, profesional y
   fecha; una especialidad de 60 minutos solo se ofrece cuando existen dos
   slots consecutivos.
4. Una cita de Medicina General se crea `APPROVED`; una especializada se crea
   `REQUESTED`.
5. Una reserva incompatible no puede ocupar los mismos slots, incluso en una
   segunda petición.
6. ADMIN puede aprobar o rechazar una solicitud `REQUESTED`; el rechazo exige
   motivo y libera los slots.
7. Los endpoints aplican rol y ownership, y el frontend muestra los resultados
   y errores del flujo.

## Definition of Done

- [ ] Migración Flyway y contratos REST documentados.
- [ ] Pruebas de slot 30/60, doble reserva y autorización pasan.
- [ ] UI de administración, disponibilidad y aprobación consume REST real.
- [ ] Build frontend y pruebas backend pasan.
- [ ] Wiki con evidencia comprobada actualizada.

## Aprobación

- 2026-09-25 — Aprobada por instrucción explícita del estudiante: “continúa”,
  “crea unos casos de testeo” y “realízalo”.
