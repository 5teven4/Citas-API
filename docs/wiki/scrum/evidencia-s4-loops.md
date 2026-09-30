# Evidencia de loops S4

Fecha de ejecución: 2026-09-25. Los cambios y las verificaciones ocurrieron
en el checkout actual; no se crearon commits ni se avanzó a S5/S6.

## LOOP_01 — doble reserva

| Iteración | Builder | Verifier | Resultado |
|---|---|---|---|
| 1 | Restauró temporalmente la traducción previa a `IllegalArgumentException` para observar el comportamiento defectuoso. | `mvn -Dtest=FlywayMigrationIT#secondReservationForAnOccupiedSlotReturnsConflict test` falló: esperaba `SlotAlreadyReservedException`, recibió `IllegalArgumentException`. | RED reproducible |
| 2 | Cambió el rechazo de horario ocupado a `SlotAlreadyReservedException`; el handler lo responde como HTTP 409. | El mismo test MySQL pasó: una reserva queda aprobada, la segunda se rechaza y solo persiste una cita. `SlotRulesTest` conserva además la regla pura de colisión. | PASS |

## LOOP_02 — reprogramación completa

| Iteración | Builder | Verifier | Resultado |
|---|---|---|---|
| 1 | Añadió V3, casos de ciclo de vida, endpoints REST y consumo UI. | El primer test MySQL reveló conversión horaria incorrecta de `DATETIME`; también se detectó que el baseline del compose ya contiene tablas S4. | RETRY |
| 2 | Hizo V3 compatible con ambos esquemas, mantuvo fechas como `LocalDateTime`, calculó expiraciones con el reloj MySQL y expuso el estado/horario propuesto de la solicitud en mis citas. | `FlywayMigrationIT` pasó en MySQL vacío y en el baseline de `database/reference/db.sql`; el escenario de reprogramación verificó slots viejos+nuevos durante `PENDING`, transferencia al aprobar, liberación al rechazar y visibilidad de `PENDING` para USER. `npm run build` pasó. | PASS técnico |

La verificación fue ejecutada después de los cambios, separada de la edición.
No equivale a una revisión humana externa. El perfil `integration-tests`
permite repetir la suite con Failsafe.

## LOOP_03 — reto independiente

**PENDIENTE_ESTUDIANTE.** El estudiante debe elegir el problema, meta,
alcance, presupuesto, condición de parada/escalamiento y evidencia del
Verifier conforme a `prompts/goal-loop/LOOP_03_RETO_INDEPENDIENTE.md`. No se
inventó un reto atribuible al estudiante.