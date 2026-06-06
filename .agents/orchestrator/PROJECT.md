# Project: BpmnDesigner - Glosario de Datos Unificado (Propuesta 2)
# Scope: US-005 CA-5 Nomenclature Rule UX/UI Enhancement

## Architecture
- **Frontend Layer**: Vue 3 SPA using TypeScript, TailwindCSS, and Pinia.
- **Components involved**:
  - `BpmnDesigner.vue`: Main BPMN Modeler component.
  - `BpmnDesigner.spec.ts`: Unit tests for BpmnDesigner.
- **Design Goals**:
  - Add collapsible "Glosario de Variables de Negocio" card section in process properties panel.
  - Support manual variable declaration (key, type) persisted in BPMN XML custom extension elements.
  - Dynamically merge manual variables with linked forms (loaded via `fetchForms()`), active webhooks/connectors, and session context (`session.user_name`, `session.email`).
  - Replace nomenclature input with an autocomplete pill/tag editor that triggers on `{` and inserts `{glosario.<variable_key>}` (or `{session.user_name}`).
  - Render color-coded pills/chips for variables in the input.
  - Add a premium explanatory tooltip in a friendly "dummies-tone" explaining the shared glossary concept.

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|---|---|---|---|
| 1 | Exploration & Analysis | Analyze BpmnDesigner.vue, XML handling, state, and BpmnDesigner.spec.ts | None | DONE |
| 2 | Implementation | Implement Glosario section, merging logic, XML persistence, pill editor, and tooltip | M1 | IN_PROGRESS |
| 3 | Testing & Verification | Write unit tests in BpmnDesigner.spec.ts, run Vitest, and execute production build | M2 | PLANNED |

## Interface Contracts
- **BPMN XML Extension Elements**: Custom extension elements used to store manual variables.
- **Nomenclature Rule XML Mapping**: Persists to/from BPMN XML `ReglaNomenclatura` root property.

## Code Layout
- Modeler Views: `frontend/src/views/admin/Modeler/BpmnDesigner.vue`
- Unit Tests: `frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts`
- Pinia Stores: `frontend/src/stores/useIntegrationStore.ts` (or auth/process stores if applicable)
