---
type: domain
status: verified
updated: 2026-09-16
sources: [SRC-README, SRC-PRD, SRC-SESSIONS]
---

# Visión del proyecto

FCV Citas es un sistema académico ficticio de agendamiento de citas. No
representa procesos internos reales de FCV y no admite datos personales reales.
Su propósito es practicar desarrollo asistido por agentes, especificaciones,
pruebas, automatizaciones y trazabilidad.

## Actores

- **USER:** se registra, mantiene su afiliación, busca horarios y gestiona sus
  citas.
- **PROFESSIONAL:** administra disponibilidad y consulta solo su agenda.
- **ADMIN:** configura catálogos, profesionales, solicitudes especializadas y
  reprogramaciones.

## Alcance y hitos

El backend es `citas-api`, el frontend es `citas-web` y ambos son repositorios
independientes coordinados desde este workspace. La progresión S2–S6 exige
especificaciones, wiki global, pruebas, autonomía y n8n según el hito. Ver
[[architecture-runtime]] y [[appointment-domain]].

## Hechos clave

- HECHO: los datos de pacientes, profesionales, credenciales y transacciones
  son sintéticos.
- HECHO: las dos sedes del laboratorio son HIC e ICV.
- HECHO: la wiki global vive en `citas-api/docs/wiki/llm-wiki/`.
