# Casos de prueba — S3

| ID | Caso | Resultado esperado |
|---|---|---|
| S3-01 | ADMIN crea profesional con sede y especialidad | Profesional disponible para su configuración |
| S3-02 | PROFESSIONAL crea bloque futuro válido | Se generan slots de 30 min |
| S3-03 | PROFESSIONAL crea bloque solapado o sede ajena | Respuesta de validación; no se crean slots |
| S3-04 | USER consulta especialidad de 30 min | Se ofrece un slot libre |
| S3-05 | USER consulta especialidad de 60 min | Solo se ofrecen dos slots consecutivos libres |
| S3-06 | USER confirma cita general | Cita `APPROVED` y slot retenido |
| S3-07 | USER confirma cita especializada | Cita `REQUESTED` y slots retenidos |
| S3-08 | Segunda reserva toma slot retenido | Conflicto, sin segunda reserva |
| S3-09 | ADMIN rechaza sin motivo | Validación rechaza la operación |
| S3-10 | ADMIN rechaza con motivo | Estado `REJECTED` y slots liberados |
| S3-11 | Rol/ownership inválido | Respuesta 403 |

La implementación debe automatizar como mínimo S3-05, S3-06, S3-07, S3-08,
S3-09, S3-10 y S3-11.
