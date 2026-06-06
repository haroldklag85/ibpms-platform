# Context - Glosario de Datos Unificado (Propuesta 2)

## Goal
Implement the Unified Data Glossary (Glosario de Datos Unificado) for the nomenclature rule input in `BpmnDesigner.vue` (US-005, CA-5).

## Active Task
- Implement the "Glosario de Variables de Negocio" section and state in `BpmnDesigner.vue`.
- Enable manual variable declaration and persistence in BPMN XML custom extension elements.
- Merge manual variables dynamically with active forms, webhooks, and session context.
- Create an autocomplete popover/editor for the nomenclature rule input.
- Render color-coded pills/chips inside the input container.
- Add a friendly, dummies-tone explanatory tooltip.
- Add unit tests in `BpmnDesigner.spec.ts` under the CA-5 scope.

## Key Files
- `frontend/src/views/admin/Modeler/BpmnDesigner.vue`
- `frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts`
- `ORIGINAL_REQUEST.md`

## Relevant Commands
- Unit tests: `npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts`
- All frontend tests: `npx vitest run`
- Frontend build: `npm run build` (run from frontend directory or project root)
