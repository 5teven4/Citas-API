---
type: domain
status: verified
updated: 2026-09-16
sources: [SRC-PRD, SRC-SCHEMA, SRC-3FN]
---

# Dominio de citas y agenda

## Agenda y disponibilidad

Un profesional publica bloques futuros en sedes donde está asignado. Cada
bloque se discretiza en slots de 30 minutos. Una especialidad dura 30 o 60
minutos y la segunda opción exige dos slots consecutivos.

## Estados de cita

- La cita general usa Medicina General y nace `APPROVED`.
- La cita especializada nace `REQUESTED`; ADMIN la aprueba o rechaza y el
  rechazo requiere motivo.
- Una cita puede terminar en `CANCELLED`, `COMPLETED` o `NO_SHOW` según las
  reglas del PRD. Los cambios se auditan.

## Reprogramación

Solo aplica a una cita aprobada y futura. La propuesta nueva retiene slots sin
liberar los de la cita original. Al aprobar, una transacción debe transferir la
reserva; al rechazar, libera la propuesta y conserva la cita original.

## Invariantes

- Ningún slot puede estar reservado dos veces.
- Especialidad y profesional deben estar activos y relacionados.
- El profesional solo agenda en una sede asignada.
- Los cambios de estado deben ser explícitos y auditables.

La implementación relacional de estas reglas está en [[data-model]].
