# Esquema de la wiki

## Tipos de página

| Tipo | Ubicación | Responsabilidad |
|---|---|---|
| Síntesis | `wiki/` | visión de un área del proyecto |
| Dominio | `wiki/` | entidades, reglas y flujos funcionales |
| Arquitectura | `wiki/` | decisiones y límites técnicos |
| Decisión | `wiki/decisions.md` | decisiones confirmadas y su fundamento |
| Riesgo | `wiki/risks-open-questions.md` | incertidumbres, contradicciones y trabajo pendiente |
| Análisis durable | `wiki/analyses/` | comparación o investigación aprobada para conservar |

## Frontmatter mínimo

Las nuevas páginas wiki deben comenzar con:

```yaml
---
type: domain | architecture | analysis | decision
status: draft | verified | needs-review
updated: YYYY-MM-DD
sources: [SRC-ID]
---
```

No agregues frontmatter a una página existente si eso solo duplica metadatos
sin aportar valor. Usa enlaces Obsidian hacia páginas existentes y los IDs de
fuente `SRC-*` para permitir trazabilidad.

## Convenciones editoriales

- Una afirmación relevante debe indicar su fuente o marcarse INFERENCIA.
- Una página debe tener una responsabilidad principal y enlaces bidireccionales
  relevantes, no enlaces decorativos.
- Conserva el español como idioma principal y los identificadores técnicos en
  inglés cuando correspondan al código.
- No reescribas el historial de `log.md`; agrega entradas al inicio o final
  siguiendo su convención, sin alterar entradas previas.
