---
type: architecture
status: verified
updated: 2026-09-16
sources: [SRC-3FN, SRC-SCHEMA, SRC-NORMALIZATION, SRC-ERD]
---

# Modelo de datos y normalización

## Diseño normalizado

El esquema separa usuarios, roles y perfiles profesionales; catálogos de
especialidades, sedes, EPS, planes, regímenes y estados; y transacciones de
citas, disponibilidad, reprogramación y auditoría. Las relaciones N:M se
resuelven con tablas puente.

La afiliación enlaza usuario y plan, desde el que se conoce EPS y régimen. Por
tanto, esos nombres no se duplican en usuario ni cita. La duración y la franja
de una cita son snapshots explícitos para conservar el acuerdo histórico si el
catálogo cambia.

## Reservas y concurrencia

`professional_slots` representa intervalos atómicos de 30 minutos.
`slot_reservations` centraliza tanto las reservas de cita como las retenciones
de reprogramación. Su unicidad por slot impide que los dos flujos ocupen la
misma franja. Las transiciones que liberan o transfieren slots deben ejecutarse
transaccionalmente.

## Dependencias funcionales relevantes

- `users.id` determina identidad y estado; email y documento son claves
  candidatas.
- `professionals.id` determina el perfil y tiene relación 1:1 con `users`.
- `eps_plans.id` determina EPS, régimen y atributos del plan.
- `appointments.id` determina participantes, especialidad, estado y franja.
- `professional_slots.id` determina un intervalo; la reserva vigente es única
  por slot.

Para una evaluación académica por forma normal, consulta también `SRC-3FN` y
la justificación detallada de `SRC-NORMALIZATION`.
