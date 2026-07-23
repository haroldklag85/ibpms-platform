# Project: iBPMS Platform Toolbar & Simulation Redesign
# Scope: US-005 & US-007 Follow-up Requirements

## Architecture
- **Frontend Layer**: Vue 3 SPA using TypeScript, TailwindCSS, and Pinia.
  - Modeler View: `frontend/src/views/admin/Modeler/BpmnDesigner.vue`
  - Integration Store: `frontend/src/stores/useIntegrationStore.ts`
- **Backend Layer**: Java Spring Boot, Hibernate, Liquibase, PostgreSQL.
  - Controllers: `BpmnDesignController.java`
  - Tests: `DataMappingIntegrityTest.java`

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | Exploration & Analysis | Codebase investigation and mapping of endpoints, test suites, and components | None | DONE |
| 2 | Backend Implementation | Version history bug fixes, JSON alignment, Swagger documentation, and DataMappingIntegrityTest stabilization | M1 | DONE |
| 3 | Frontend Implementation | Stepper UI, Resizable push-layout sidebar, Accordion phases, Hot Path Traversal highlights, and FormData payload integration | M2 | DONE |
| 4 | Verification & Audit | Backend Maven integration test suite and Frontend production build | M3 | DONE |
| 5 | Exploration & TDD Setup | Code investigation for Bug 1 (auto-save/navigation/lock) and Bug 2 (XML load exception) | M4 | DONE |
| 6 | Bug 2 & Backend Fixes | Catch IllegalArgumentException in getProcessXml in BpmnDesignController.java and return basic XML, ensure test compilation | M5 | DONE |
| 7 | Bug 1 & Frontend Refinements | Implement auto-save, lock renewal, URL synchronization, toaster alerts, toolbar/drawer UX adjustments, and TDD tests | M6 | DONE |
| 8 | Verification & Audit | Run JUnit backend tests, Playwright E2E and CT tests with GPU, run Forensic Auditor | M7 | DONE |
| 9 | Technical ID Immutability | Fix technical ID mutation when loading existing processes, run unit tests, and build | M8 | DONE |


## Interface Contracts
- **BPMN Version History API**: `/api/v1/design/processes/{processDefinitionKey}/versions`
  - Error case: catch `IllegalArgumentException` → return empty list with HTTP 200 OK.
  - DTO schema keys required: `version`, `date` (or `updatedAt`), `author` (or `createdBy`), `status`, and any existing keys.
- **BPMN Validation API**: `/validate` (multipart/form-data with file upload).

## Code Layout
- Modeler Views: `frontend/src/views/admin/Modeler/BpmnDesigner.vue`
- Pinia Stores: `frontend/src/stores/useIntegrationStore.ts`
- Backend Controllers: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/BpmnDesignController.java`
- Backend Integration Tests: `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/DataMappingIntegrityTest.java`
