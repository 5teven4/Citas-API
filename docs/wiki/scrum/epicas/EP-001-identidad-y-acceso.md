---
id: EP-001
tipo: epica
titulo: Identidad y acceso seguro
estado: Aprobada
historias: [HU-001]
dependencias: [MySQL, Flyway]
---

# EP-001 — Identidad y acceso seguro

## Objetivo

Permitir que un usuario ficticio se registre e inicie sesión de forma segura para habilitar los flujos posteriores de citas.

## Alcance

- Registro USER con identidad única.
- Login, refresh rotativo y logout.
- Hash de contraseña, roles y persistencia MySQL mediante Flyway.

## Fuera de alcance

- Interfaz web final.
- Envío SMTP de recuperación de contraseña.

## Historias de usuario

- [[HU-001-registro-login-jwt]]

## Criterio de completitud

- [ ] HU-001 con criterios validados y evidencia automatizada.
- [ ] Migración Flyway ejecutada contra MySQL.
