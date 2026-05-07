# Implementation Plan: Frontend US-007 Bloque 2

Este plan detalla la remediación de 8 GAPs detectados en la auditoría de la US-007, aplicando principios de Clean Code, TDD y Zero-Trust UI.

## Fase 1: Sanitización y DOM (GAP-03, GAP-21, GAP-24)
1. **GAP-03 (DOMPurify):**
   - Instalar `dompurify` y `@types/dompurify`.
   - Modificar `DmnGridManual` para que toda celda (expresiones FEEL o texto) pase por `DOMPurify.sanitize()` antes de renderizarse.
   - Reforzar `DmnAntiXss.spec.ts` para inyecciones con `<img onerror="...">`.
2. **GAP-21 (Buscador In-App):**
   - Crear `components/dmn/DmnGridSearch.vue`.
   - Implementar la intercepción de `Ctrl+F` (`keydown` preventDefault).
   - Conectar la búsqueda con `useDmnStore` y agregar clases CSS (`.search-highlight`) en las celdas coincidentes dentro de `DmnGridManual`.
   - Crear tests de resaltado y scroll.
3. **GAP-24 (Dropdown Zod en Headers):**
   - Actualizar los headers en `DmnGridManual`.
   - Reemplazar el input de texto libre por un `<select>` vinculado a las variables validadas en Zod presentes en el Store/BFF.
   - Testear en Vitest la restricción de tipeo libre.

## Fase 2: Resiliencia y Conexiones (GAP-22)
4. **GAP-22 (Timeouts SSE):**
   - Modificar `useDmnStore.ts` en la función de apertura `EventSource`.
   - Agregar temporizador de 30 segundos (`setTimeout`) para inactividad total inicial.
   - Agregar temporizador de 15 segundos para "stall" entre mensajes cuando ya se reciben filas.
   - Agregar estado visual de `TIMEOUT` o advertencias de lentitud.
   - Testear la resiliencia en Store.

## Fase 3: Pruebas y Cobertura (GAP-08, GAP-10, GAP-23, GAP-25)
5. **GAP-08 (Test Virtual Scrolling):**
   - Agregar o modificar tests en `DmnGridManual.spec.ts` para verificar que al inyectar 50 filas, en el DOM (`.dmn-row`) existan menos filas montadas (comprobando limitación de render).
6. **GAP-10 (Test Panic Modal):**
   - Testear comportamiento del input estricto ("CONFIRMO_V2") y simular la llamada `POST /api/v1/dmn/{id}/rollback` para el botón de reversión.
7. **GAP-23 (Coexistencia UI):**
   - Crear test que monte `DmnIntelligence.vue` y asegure la visualización simultánea de `.nlp-panel` y `.dmn-grid`.
8. **GAP-25 (Editabilidad XML):**
   - Validar en tests que al inyectar o disparar un modo desarrollador con XML pre-existente, las celdas conserven sus atributos de `contenteditable` y controles de grilla.

## Fase 4: Build y Handoff Final
- Ejecutar suite Vitest (`npm run test:unit`).
- Ejecutar validación de compilación rigurosa (`npm run build`).
- Consolidación del trabajo vía `git commit` y `git push` a `sprint-6`.
