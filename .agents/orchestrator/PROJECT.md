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
|---|---|---|---|---|
| 1 | Exploration & Analysis | Codebase investigation and mapping of endpoints, test suites, and components | None | PLANNED |
| 2 | Backend Implementation | Version history bug fixes, JSON alignment, Swagger documentation, and DataMappingIntegrityTest stabilization | M1 | PLANNED |
| 3 | Frontend Implementation | Stepper UI, Resizable push-layout sidebar, Accordion phases, Hot Path Traversal highlights, and FormData payload integration | M2 | PLANNED |
| 4 | Verification & Audit | Backend Maven integration test suite and Frontend production build | M3 | PLANNED |

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
