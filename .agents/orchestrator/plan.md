# Implementation Plan - iBPMS Platform Toolbar & Simulation Redesign

This plan details the steps required to implement the toolbar and simulation redesign, backend and frontend fixes, and stabilization of backend tests.

## Milestones

### Milestone 1: Exploration and Analysis
- **Goal**: Locate and analyze the target files in both frontend and backend.
  - Locate `BpmnDesignController.java`, `DataMappingIntegrityTest.java`, AbstractIntegrationTest, and associated OpenAPI setup.
  - Locate `BpmnDesigner.vue`, `useIntegrationStore.ts`, and see how the current toolbar, popup simulation modal, and version history panels are implemented.
  - Inspect the bpmn-js setup to understand how element overlays, highlights, and custom classes are added/removed.
- **Method**: Spawn a `teamwork_preview_explorer` subagent.
- **Verification**: Explorer completes investigation, producing an `analysis.md` outlining exact file paths, component structures, API response structures, and a concrete change strategy.

### Milestone 2: Backend Implementation & Stabilization
- **Goal**: Implement all backend requirements:
  - catch `IllegalArgumentException` in `/processes/{processDefinitionKey}/versions` and return HTTP 200 with `List.of()`.
  - Enrich versions DTO with `version`, `date` (or `updatedAt`), `author` (or `createdBy`), `status` properties.
  - Add OpenAPI Swagger annotations to `/deploy` and `/validate` endpoints in `BpmnDesignController.java`.
  - Inherit `DataMappingIntegrityTest.java` from `AbstractIntegrationTest`.
- **Method**: Spawn a `teamwork_preview_worker` subagent with backend expertise.
- **Verification**: Compile and run the backend tests: `mvn clean test -Dtest=DataMappingIntegrityTest,BpmnDeployContractTest,SandboxGovernanceTest`.

### Milestone 3: Frontend Implementation & Integration
- **Goal**: Implement all frontend requirements:
  - Stepper Toolbar: Redesign toolbar into a 6-step stepper. Apply Glassmorphism styles (`backdrop-filter: blur(8px)`, semi-transparent borders). Highlight the card corresponding to the current phase based on `currentVersion`. Disable Step 6 (Gestor de Instancias) if `currentVersion === 0` (or `versionHistory` is empty/draft) and add the tooltip. For Step 5 (Despliegue), restrict a user with `BPMN_Designer` role to read-only mode (approve/reject buttons blocked). Ensure responsiveness.
  - Simulation Sidebar: Convert "Validar y simular" popup into a push-layout right sidebar. Ensure it shrinks the BPMN canvas when open. Implement dragging (resizing) using native mouse events in Vue 3 (range 400px-700px). Auto-hide the bpmn-js properties panel when open, restore it when closed.
  - Vertical Accordion: Structure the three phases (Linter Local, Pre-Flight Analyzer, Sandbox Simulator) as vertical collapsible sections.
  - Hot Path Traversal: Add green animated halos (`highlight-executed`) on executed nodes in the BPMN canvas. Ensure they are removed upon clicking "Limpiar trayectoria" or closing the sidebar. Include a variables grid to view, edit, and delete variables stored in localStorage.
  - Version History: Map backend JSON keys to the frontend DTO. If error fetching versions, assign an empty array `[]` and show the empty state ("No hay versiones publicadas aún").
  - FormData Payload: Update `validateProcess` in `useIntegrationStore.ts` to send FormData.
- **Method**: Spawn a `teamwork_preview_worker` subagent with frontend expertise.
- **Verification**: Run frontend production build: `npm run build`.

### Milestone 4: Verification & Final Audit
- **Goal**: Run regression test suites, verify overall correctness, and run the Forensic Auditor to ensure no integrity violations.
- **Method**: Spawn `teamwork_preview_reviewer` and `teamwork_preview_auditor` subagents.
- **Verification**: Backend and frontend tests pass, and auditor confirms clean execution.
