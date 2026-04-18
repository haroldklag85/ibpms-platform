# Solicitud de Revisión Arquitectónica — Frontend Sprint 5.1

> **Agente:** Desarrollador Frontend Senior
> **Fecha:** 2026-04-18
> **Handoff Fuente:** `.agentic-sync/handoff_frontend_sprint5_1.md`
> **Rama:** `sprint-5/iteracion4`

## Resumen del Plan

He culminado la etapa de investigación e inventariado el alcance para remediación e integración de los contratos Backend Sprint 5.1. 

**Componentes Nuevos (UI & TDD obligatorios):**
1. **`TaskPreviewModal.vue`** (CA-5): Mostrará la vista Read-Only para tareas Claimables extraída desde `/preview` (Store: `fetchTaskPreview`).
2. **`ClaimAuditTrail.vue`** (CA-9): Anidado como lista de pasos/timeline, extraído de `/audit-trail` (Store: `fetchAuditTrail`). 

**Modificaciones a Existentes:**
- **`WorkdeskGrid.vue`**: Invocará el Launch del `TaskPreviewModal` en las tareas 'AVAILABLE' y sustituirá el popup aburrido global del `handleUnclaim` por un Dialog Modal defensivo (CA-7).
- **`useFormStore.ts` & `DynamicForm.vue`**: Se extenderá el `catch` de 400 Bad Request en el store, parseando la validación Zod cruzada con la RFC 7807 del Backend (`errors: [{field, message}]`). Este flujo alimentará la caja indicadora nativa debajo de cada campo en `DynamicField.vue` (CA-2).
- **`DmnIntelligence.vue`**: Se implementará sanitización nativa invocando e importando `dompurify` (con tipings correspondientes) antes de delegar cualquier string raw del LLM al renderizador XML subyacente. 

### QA & Calidad
- Todos los componentes nuevos tendrán el Unit Testing correspondiente en `component-tests` con Vitest `mount()`.
- Se comprobará mediante un build exhaustivo el compilado.

## Preguntas Abiertas
Ninguna, el Handoff es sumamente explícito.

Al esperar tu sello formal, procederé de inmediato con TDD iterativo.
