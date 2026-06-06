# Context - iBPMS Platform Toolbar & Simulation Redesign

## Goal
Coordinate and implement the requirements defined in ORIGINAL_REQUEST.md under the heading '## Follow-up — 2026-06-06T19:18:24Z'.

## Active Task
- Milestone 1: Exploration and Analysis.

## Key Files
- `frontend/src/views/admin/Modeler/BpmnDesigner.vue`
- `frontend/src/stores/useIntegrationStore.ts`
- `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/BpmnDesignController.java`
- `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/DataMappingIntegrityTest.java`

## Relevant Commands
- Backend tests: `mvn clean test -Dtest=DataMappingIntegrityTest,BpmnDeployContractTest,SandboxGovernanceTest`
- Frontend build: `npm run build`
