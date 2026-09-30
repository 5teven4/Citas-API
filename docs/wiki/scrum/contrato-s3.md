# Contrato REST S3

Todos los endpoints requieren `Authorization: Bearer <access token>`.

| Método | Ruta | Rol | Propósito |
|---|---|---|---|
| POST | `/api/v1/admin/professionals` | ADMIN | Crea perfil profesional y asignaciones |
| POST | `/api/v1/professionals/me/availability-blocks` | PROFESSIONAL | Publica bloque futuro |
| GET | `/api/v1/availability` | autenticado | Consulta inicios disponibles |
| GET | `/api/v1/admin/appointments/requests` | ADMIN | Lista solicitudes `REQUESTED` para decisión |
| POST | `/api/v1/appointments` | USER | Reserva general o especializada |
| PATCH | `/api/v1/admin/appointments/{id}/decision` | ADMIN | Aprueba o rechaza solicitud |

Una cita general responde `APPROVED`; una especialidad configurada con
aprobación administrativa responde `REQUESTED`. Un conflicto de slot responde
HTTP 409. Un rechazo requiere `reason` y libera sus reservas.
