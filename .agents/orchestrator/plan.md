# Implementation Plan - Glosario de Datos Unificado (Propuesta 2)

This plan details the steps required to implement the unified data glossary for the nomenclature rule input field in BpmnDesigner.vue to improve the UX/UI of CA-5 under US-005.

## Milestones

### Milestone 1: Exploration and Analysis
- **Goal**: Investigate BpmnDesigner.vue structure, how properties panel lists inputs, how BPMN XML is loaded/parsed/saved, how variables (forms, webhooks, session) are currently structured/accessed, and how unit tests are structured in `BpmnDesigner.spec.ts`.
- **Method**: Spawn a `teamwork_preview_explorer` subagent.
- **Verification**: Explorer completes investigation, producing an `analysis.md` detailing the file structures and showing the exact locations of the panels, XML serializers/parsers, and existing tests.

### Milestone 2: Implementation of Glosario de Variables
- **Goal**: Implement the "Glosario de Variables de Negocio" section and state in BpmnDesigner.vue, XML parser/writer for manual variables, dynamic variable merging, token autocomplete menu on `{`, token color-coded pill rendering, and the explanatory tooltip.
- **Method**: Spawn a `teamwork_preview_worker` subagent.
- **Verification**: Worker writes code changes to BpmnDesigner.vue, compiles, and performs initial sanity checks.

### Milestone 3: Testing, Verification, and Auditing
- **Goal**: Write new component unit tests in BpmnDesigner.spec.ts under the CA-5 scope to verify all required behaviors (R4), run the frontend unit tests, build the project successfully, and run the Forensic Auditor to verify integrity.
- **Method**: Spawn `teamwork_preview_reviewer` and `teamwork_preview_auditor` subagents.
- **Verification**: 100% of unit tests pass under `npx vitest run`, production build succeeds (`npm run build`), and Forensic Auditor reports CLEAN.

## Traceability Requirement
- Every modified file must include the comment: `// @Traceability: US-005, CA-5`
