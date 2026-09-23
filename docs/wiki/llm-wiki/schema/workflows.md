# Flujos operativos

## Ingesta de una fuente

1. Verificar que la fuente sea pertinente y autorizada.
2. Asignar un ID `SRC-*`, ruta, fecha y hash en `raw/source-registry.md`.
3. Extraer hechos, decisiones, reglas, entidades y preguntas abiertas.
4. Actualizar las páginas wiki pertinentes; crear una nueva solo si tiene una
   responsabilidad distinta.
5. Actualizar `wiki/index.md` y escribir una entrada `ingest` en `wiki/log.md`.
6. Informar qué se actualizó, contradicciones detectadas y qué no se verificó.

## Consulta

1. Leer el índice y las páginas relevantes.
2. Seguir enlaces y contrastar los hechos de alto impacto contra `SRC-*`.
3. Entregar una respuesta con evidencia, inferencias y vacíos.
4. Ofrecer archivar el resultado si es durable; no archivar conversaciones
   triviales ni datos sensibles.

## Learn

1. Identificar un hecho, una preferencia, una pregunta o una decisión durable.
2. Verificarlo o pedir confirmación cuando corresponda.
3. Integrarlo en una sola página canónica y enlazarla.
4. Registrar `learn` en el log.

## Lint

Revisar mensualmente o antes de un hito: enlaces rotos, páginas sin enlaces
entrantes, duplicados, contradicciones, fuentes sin ingestar, preguntas
antiguas y decisiones sin evidencia. Registrar `lint` aun cuando no haya
cambios.
