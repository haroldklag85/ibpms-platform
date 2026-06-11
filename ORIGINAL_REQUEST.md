# Original User Request

## Initial Request — 2026-06-10T19:59:00Z

Implement process version tag auto-suggestion homologated to v0 (0.0.0) for new draft processes in the BPMN Modeler (US-005).

Working directory: z:/home/haroltandrsgmezagu/proyectos/ibpms-platform
Integrity mode: development

## Requirements

### R1. Modeler Version Tag Suggestion
In BpmnDesigner.vue, modify the auto-suggestion logic so that when a process is a draft (currentVersion is 0) or lacks a version tag, the version tag is suggested as '0.0.0' instead of '1.0.0'.

### R2. Unit Test Alignment
In BpmnDesigner.spec.ts, update the test case "Debe auto-sugerir '1.0.0' para el Version Tag..." to "Debe auto-sugerir '0.0.0' para el Version Tag...", asserting that processVersionTag and the businessObject's versionTag attribute are set to '0.0.0'.

### R3. Compliance with ADR-001, Yudhi Clean Code, and SRE Discipline
- Ensure strict typing (no any at all costs in newly modified files).
- Keep composition API setup below 150 lines or modularized.
- Enforce Zero-Mock principles: run and verify that frontend tests pass in WSL.
- Compile and build successfully with npm run build.

## Acceptance Criteria

### Modeler UI
- [ ] Version tag auto-suggests "0.0.0" when creating or loading a new process (v0).
- [ ] Vitest unit tests in BpmnDesigner.spec.ts pass successfully in WSL.
- [ ] Frontend production build compiles cleanly without errors.

## Follow-up — 2026-06-11T01:05:22Z

Resolve the BPMN Modeler (US-005) bugs (HTTP 400 Bad Request on deploy request, HTTP 409 Conflict on lock heartbeat, and E2E test timeout failure due to 404 redirection).

Working directory: z:/home/haroltandrsgmezagu/proyectos/ibpms-platform
Integrity mode: development

## Requirements

### R1. Disable ID Técnico modification after draft creation
In `frontend/src/views/admin/Modeler/BpmnDesigner.vue`, disable the `ID Técnico` input field once a process has been saved or exists as a draft. Specifically, ensure that `isNewProcess.value` is set to `false` after a successful call to `saveDraft` in the UI, and bind the input field's `:disabled` attribute to `!isNewProcess`.

### R2. Automatic Lock Acquisition upon Process Load
In `frontend/src/views/admin/Modeler/BpmnDesigner.vue`, implement automatic lock acquisition when a process is loaded or created (after verifying that it is not locked by another user). This will send a `POST /design/processes/{id}/lock` request to the backend so the database has an active lock, avoiding `409 (Conflict)` errors during subsequent heartbeat calls.

### R3. E2E Test activeRole Bypass via atob Interceptor
In `frontend/e2e/certification/us005-bpmn-modeler-persistence.e2e.spec.ts`, modify the `beforeEach` hook to override the global `window.atob` function. The overridden function must intercept the decoding of the JWT payload, shifting the role `"ibpms_rol_SUPER_ADMIN"` to the first element in the `roles` array. This ensures Pinia's `authStore` initializes `activeRole` as `"ROLE_SUPER_ADMIN"`, bypassing the navigation guard's fake 404 page and allowing Playwright to locate and interact with the modeler.

## Acceptance Criteria

### UI Modeler Behavior
- [ ] The `ID Técnico` input field is disabled when viewing or editing an existing saved process.
- [ ] Opening or creating a process automatically initiates a lock request (`POST /lock`), which registers the lock in the backend.
- [ ] No `409 (Conflict)` errors are thrown in the console by the heartbeat timer.

### E2E Certification
- [ ] Playwright E2E test `us005-bpmn-modeler-persistence.e2e.spec.ts` passes 100% in WSL.
- [ ] Frontend production build compiles successfully with `npm run build`.

## Test-First Strategy (Mandatory)
The agent team must strictly follow this execution order:
1. First, implement the changes to `frontend/e2e/certification/us005-bpmn-modeler-persistence.e2e.spec.ts` (the `window.atob` interceptor).
2. Run the E2E test suite using WSL to confirm that the test fails (or fails on the newly expected behaviors like auto-locking, rather than timing out on 404).
3. Once the test failure is validated and documented, implement the frontend changes in `BpmnDesigner.vue` (R1 and R2).
4. Run the E2E test suite again to confirm all tests pass successfully (green light).

## Required Subagent Skills & Standards
Every specialist agent assigned to this task must apply and follow these skills:
- `addyosmani_sre_discipline` (for Zero-Mock database enforcement and validation)
- `addyosmani_planning` (for systematic task planning)
- `addyosmani_code_review` (for pre-commit validation)
- `yudhi_architecture_compliance` (for structural hexagonal compliance)
- `yudhi_database_migrations` (for DB mapping verification)
- `handoff_quality_standard` (for quality documentation handoffs)

