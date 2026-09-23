---
type: decision
status: verified
updated: 2026-09-16
sources: [SRC-README, SRC-TECH, SRC-NORMALIZATION]
---

# Decisiones confirmadas

## D-001 — Wiki global única

**Estado:** DECISIÓN. La wiki común se mantiene en
`citas-api/docs/wiki/llm-wiki/`; no se crean wikis paralelas por agente.

## D-002 — Fuentes raw inmutables

**Estado:** DECISIÓN. Las especificaciones canónicas se mantienen en su ruta
original y se registran con hash. La IA modifica solo la capa `wiki/` y la
configuración `schema/`.

## D-003 — Persistencia normalizada

**Estado:** DECISIÓN. MySQL 8.4 usa un modelo hasta 3FN con catálogos, FKs y
tablas puente. Los snapshots históricos se documentan explícitamente.

## D-004 — Exclusión de reserva

**Estado:** DECISIÓN. Citas y reprogramaciones comparten una reserva central de
slots; la exclusión se respalda con unicidad por slot y transacciones de
aplicación.

## D-005 — Baseline Flyway del incremento S2

**Estado:** DECISIÓN. La API usa `baseline-on-migrate` versión 0 para convivir
con el esquema de referencia inicializado por Docker. La migración V1 contiene
el bootstrap idempotente de autenticación para permitir una base vacía.
