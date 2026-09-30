---
type: architecture
status: verified
updated: 2026-09-30
sources: [SRC-PRD, SRC-TECH]
---

# Frontend: sistema UX/UI

## Alcance

**HECHO:** La interfaz se mantiene en `citas-web`, con React, TypeScript y Vite,
consumiendo directamente la API REST configurada por `VITE_API_URL`. No se
modificaron endpoints, payloads, autenticación ni reglas de negocio.

**HECHO:** La actualización visual se aplicó sobre la capa de estilos y tokens;
los componentes funcionales existentes conservan sus handlers y estados.

## Sistema visual

**DECISIÓN:** La interfaz usa una fuente única de tokens en
`citas-web/src/styles/tokens.css`, importada por `src/styles.css`.

Los tokens cubren:

- marca y superficies: `#C40700`, `#FFFFFF` y `#F2F2F2`;
- texto, bordes, foco y feedback semántico;
- tipografía, pesos y escala de espaciado;
- radios, sombras, alturas de controles y breakpoints.

**DECISIÓN:** Los componentes consumen variables semánticas (`--color-*`,
`--space-*`, `--text-*`) en lugar de valores de color o espaciado dispersos.
El rojo corporativo se reserva para acciones primarias, enlaces destacados y
estados activos; los errores usan un token semántico independiente.

**DECISIÓN:** El flujo de agendamiento usa Material UI (`@mui/material`,
Emotion e iconos MUI) con un tema centralizado en `citas-web/src/muiTheme.ts`.
Se utilizan `Autocomplete` para especialidad, sede y profesional, `Stepper`
para mostrar el progreso, `Alert` para estados de consulta y botones de horario
con etiquetas de fecha/hora legibles.

## Patrones UX/UI aplicados

**HECHO:** Formularios, botones, filtros, listados, estados vacíos y mensajes de
éxito/error comparten tokens de superficie, borde, foco y jerarquía tipográfica.

**HECHO:** Los controles mantienen áreas táctiles mínimas, foco visible,
`prefers-reduced-motion` y composición responsive para formularios y listados.

**HECHO:** El panel de agendamiento guía al usuario en tres pasos: tipo de
atención, profesional/fecha y horario. La selección de profesional se filtra
por especialidad y sede usando el endpoint existente; no se inventó un
contrato REST nuevo.

## Datos semilla de desarrollo

**HECHO:** `V4__dev_synthetic_doctor_seed.sql` crea tres profesionales
sintéticos (`Laura Gómez`, `Andrés Rojas`, `Sofía Martínez`), asociaciones con
Medicina General/Cardiología y sedes HIC/ICV, además de bloques y slots futuros
para pruebas manuales.

**REGLA:** Estos nombres, correos, matrículas y teléfonos son datos de
laboratorio. No representan profesionales reales ni deben reutilizarse fuera
del entorno de desarrollo.

**VALIDACIÓN:** El 30/09/2026 la API devolvió los tres doctores mediante
`GET /api/v1/catalogs/professionals` y el usuario sintético pudo consultar
especialidades, sedes y combinaciones con profesionales disponibles.

**INFERENCIA:** El frontend actual no contiene todavía un router ni una
estructura de `AppShellLayout`/sidebar real; por eso esta iteración mejora la
consistencia visual del flujo existente sin inventar rutas o navegación que no
estén respaldadas por el contrato actual.

## Referencia visual externa

**DECISIÓN:** La página pública de citas médicas de EPS Sanitas se usa solo
como referencia de composición: jerarquía sanitaria clara, bloque informativo,
acceso principal visible y adaptación móvil. No se copian su marca, recursos,
textos, canales de atención ni arquitectura funcional.

La pantalla de autenticación de FCV Citas adopta un layout dividido en desktop:
contexto del servicio a la izquierda y formulario a la derecha. En móvil, ambos
bloques se reorganizan en una sola columna. Fuente visual consultada el
30/09/2026: <https://www.epssanitas.com/usuarios/web/nuevo-portal-eps/citas-medicas>.

## Validación

- `npx tsc -b`: ejecutado correctamente el 30/09/2026.
- `npm run build`: ejecutado correctamente el 30/09/2026.
- No se modificaron `src/api.ts` ni contratos REST como parte del ajuste UX/UI.
- La validación visual con lector de pantalla real y pruebas de viewport deben
  completarse en una revisión manual posterior.

## Referencias

- [[../index|Índice de la wiki]]
- [[decisions|Decisiones confirmadas]]
- `skills/stitch-design-to-frontend/skills/stitch-design-to-frontend/references/frontend-design-system.md`
- `skills/stitch-design-to-frontend/skills/stitch-design-to-frontend/references/frontend-quality-a11y-testing.md`
- [Material UI — instalación](https://mui.com/material-ui/getting-started/installation/)
- [Material UI — Stepper](https://mui.com/material-ui/react-stepper/)
- [Ministerio de Salud — preparación para citas médicas](https://www.minsalud.gov.co/sites/rid/Lists/BibliotecaDigital/RIDE/DE/CA/Ilustrar-al-paciente-en-autocuidado-seguridad.pdf)
- [MedlinePlus — seguridad del paciente](https://medlineplus.gov/patientsafety.html)
