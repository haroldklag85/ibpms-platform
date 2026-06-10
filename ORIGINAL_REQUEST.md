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
