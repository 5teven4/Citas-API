# Contrato REST S4

Todos los endpoints listados requieren `Authorization: Bearer <access token>`
salvo recuperación/restablecimiento de contraseña.

| Método | Ruta | Rol | Resultado |
|---|---|---|---|
| GET | `/api/v1/appointments/me?status&from&to` | USER | Citas propias, motivo de rechazo y última reprogramación |
| POST | `/api/v1/appointments/{id}/cancel` | USER | Cancela cita futura, libera slots y registra historial |
| POST | `/api/v1/appointments/{id}/reschedule-requests` | USER | Retiene el nuevo horario sin liberar el original |
| GET | `/api/v1/appointments/me/{id}/history` | USER | Historial de estados de una cita propia |
| GET | `/api/v1/professionals/me/appointments?from&to&locationId` | PROFESSIONAL | Agenda propia; rango máximo de 31 días |
| PATCH | `/api/v1/professionals/me/appointments/{id}/completion` | PROFESSIONAL | Marca `COMPLETED` o `NO_SHOW` |
| GET | `/api/v1/admin/reschedule-requests` | ADMIN | Solicitudes `PENDING` |
| PATCH | `/api/v1/admin/reschedule-requests/{id}/decision` | ADMIN | Aprueba o rechaza; el rechazo exige motivo |
| GET/POST | `/api/v1/admin/catalogs/eps` | ADMIN | Consulta/crea EPS |
| PUT/PATCH | `/api/v1/admin/catalogs/eps/{id}` y `/active` | ADMIN | Edita o activa/desactiva EPS |
| GET/POST | `/api/v1/admin/catalogs/eps/{epsId}/plans` | ADMIN | Consulta/crea planes |
| PUT/PATCH | `/api/v1/admin/catalogs/plans/{id}` y `/active` | ADMIN | Edita o activa/desactiva planes |
| GET/POST | `/api/v1/admin/catalogs/specialties` | ADMIN | Consulta/crea especialidades de 30 o 60 min |
| PUT/PATCH | `/api/v1/admin/catalogs/specialties/{id}` y `/active` | ADMIN | Edita o activa/desactiva especialidades |
| POST | `/api/v1/auth/password-recovery` | Público | Mensaje genérico; token solo con opt-in de desarrollo |
| POST | `/api/v1/auth/password-reset` | Público | Consume token de un solo uso y revoca refresh activos |
| POST | `/api/v1/auth/logout` | Público con refresh token | Revoca la sesión refresh actual |

Las bajas de catálogos son lógicas. Para habilitar la devolución del token en
el laboratorio local, usar `APP_SECURITY_PASSWORD_RESET_EXPOSE_TOKEN=true`;
no configurar esa variable en despliegues compartidos o productivos.

## Validación

- `mvn test`: pruebas unitarias sin requisito de base activa.
- `mvn -Pintegration-tests verify`: pruebas `*IT` contra MySQL 8.4. Configurar
  `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`,
  `JWT_ACCESS_SECRET` y `JWT_REFRESH_SECRET` para una base aislada de pruebas.
- `npm run build` en `citas-web`: TypeScript y build Vite.