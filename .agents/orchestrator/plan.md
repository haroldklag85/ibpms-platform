# Implementation Plan - iBPMS Platform US-005 Stabilization & Bug Fixing

This plan details the steps required to stabilize US-005 and resolve Bug 1 (auto-save, url query parameter sync, lock expiration) and Bug 2 (draft XML loading HTTP 500 error), following TDD.

## Milestones

### Milestone 5: Exploration & TDD Setup
- **Goal**: Code investigation for Bug 1 and Bug 2:
  - Check `BpmnDesigner.vue`'s `onMounted` and lifecycle hooks for `timeStore.startEngine()` and `timeStore.stopEngine()`.
  - Analyze how `processId` query parameter is handled in the URL and check `router.replace` navigation.
  - Review lock renewal and lock expiration banner mechanism in the frontend.
  - Find the `/processes/{processDefinitionKey}/xml` endpoint in `BpmnDesignController.java` and where the database fetches XML.
  - Look at the test suites (`BpmnDeployContractTest.java`, `DataMappingIntegrityTest.java`, `SandboxGovernanceTest.java` in backend and Playwright E2E / CT in frontend).
- **Method**: Spawn a `teamwork_preview_explorer` subagent.
- **Verification**: Explorer completes the analysis and writes `analysis.md` inside `.agents/explorer_us005`.

### Milestone 6: Bug 2 & Backend Fixes
- **Goal**: Resolve Bug 2 by modifying `/api/v1/design/processes/{processDefinitionKey}/xml` in `BpmnDesignController.java`.
  - Catch `IllegalArgumentException` (thrown when draft process doesn't exist/has null XML in database).
  - Return HTTP 200 OK with a basic default BPMN XML template.
  - Apply `@Traceability: US-005, CA-64` tag in production code.
- **Method**: Spawn a `teamwork_preview_worker` subagent for backend.
- **Verification**: Verify that backend compiles and JUnit tests run successfully:
  `mvn -f backend/pom.xml test -pl ibpms-core -Dtest=BpmnDeployContractTest,DataMappingIntegrityTest,SandboxGovernanceTest`

### Milestone 7: Bug 1 & Frontend Refinements
- **Goal**: Resolve Bug 1, implement toast notifications, and apply UI stepper/drawer refinements in `BpmnDesigner.vue` and related store/components:
  - **Bug 1 Fixes**:
    - Call `timeStore.startEngine()` on mount and `timeStore.stopEngine()` on unmount.
    - Synchronize query parameter `processId` reactively in URL via `router.replace`.
    - Implement the lock expiration warning banner with a "renew lock" click action.
  - **Toaster & Errors**:
    - Display background auto-save network error messages using a 5-second self-fading Toast.
    - Status message on the top bar ("Guardado hace x segundos", "Validado").
  - **Stepper UI Refinements**:
    - Step 1 named "Inicio" (contains Process Explorer, Import, Export, and "Guardar" manual button).
    - Status badges "BORRADOR" and "SANDBOX" as flat, rounded, read-only gray badges with tooltips (no hover/pointer cursor).
    - Version display (v0, v1, v2...) synced with Camunda version tag (initialized to "1" for new processes).
    - Step 6 ("Operación") disabled for draft `v0` (or versionHistory empty) with helper tooltip.
    - Step 5 ("Despliegue") read-only for `BPMN_Designer` role (approve/reject buttons blocked).
    - Responsiveness (horizontal scrolling or collapse).
    - Glassmorphism: translucent background and fine border styles.
  - **Cajón Lateral Derecho Unificado (Trazabilidad Drawer)**:
    - Slide-out, resizable push-layout drawer for versions and audit logs.
    - Hides bpmn-js properties panel on open, restores it on close.
    - Displays version history and audit log in tabs.
    - Displays empty state for draft `v0` processes.
    - Requires 20+ chars comments justification for deploy/reject actions.
  - **TDD Tests**:
    - Add/update unit tests in `BpmnDesigner.spec.ts` for these lifecycle, URL param, stepper, drawer, and lock renewal behaviors.
  - **Traceability**: Add comments `// @Traceability: US-005, CA-XX` where modified.
- **Method**: Spawn a `teamwork_preview_worker` subagent for frontend.
- **Verification**: Run frontend Component Tests (`npm run test:ct`) and frontend build (`npm run --prefix frontend build`) to ensure they compile and pass.

### Milestone 8: Verification & Audit
- **Goal**: Run Playwright E2E tests and forensic audit.
  - Run Playwright E2E: `PLAYWRIGHT_USE_GPU=true npx playwright test --config=playwright.e2e.config.ts`
  - Run Forensic Auditor to independently verify that all requirements are implemented correctly and cleanly.
- **Method**: Spawn `teamwork_preview_reviewer` and `teamwork_preview_auditor` subagents.
- **Verification**: 100% tests pass and Forensic Auditor reports CLEAN.

### Milestone 9: Technical ID Immutability (T-01 Modeler Bugfix)
- **Goal**: Implement technical ID immutability when loading an existing process to prevent mismatch and save errors.
  - Introduce `isNewProcess` reactively in `BpmnDesigner.vue` (initialized to `true`).
  - Set `isNewProcess` to `true` in `createNewProcess` and `false` in `loadProcess`.
  - Modify the `currentProcessName` watcher to only set `processId` if `isNewProcess.value` is true.
  - Write unit test cases in `BpmnDesigner.spec.ts` (TDD style) to verify both new process slug creation and existing process ID preservation.
  - Add `@Traceability: US-005, CA-15` annotations.
- **Method**: Spawn `teamwork_preview_worker` (Vue specialist).
- **Verification**: Run `npm run test:unit` and `npm run build` inside `frontend`. Run `teamwork_preview_auditor` to audit.
