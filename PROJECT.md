# Project: iBPMS Platform US-005 Version Tag & Suggestion

## Architecture
- Frontend: Vue 3 + TypeScript + Pinia + Vite (in `frontend/`)
- Backend: Java 17 + Spring Boot + Camunda (in `backend/ibpms-core/`)

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | Exploration | Search and identify code locations for frontend and backend changes | None | DONE |
| 2 | Backend Validation | Implement SemVer validation in CamundaBpmnValidationAdapter and tests | M1 | DONE |
| 3 | Frontend Timeline & Suggestion | Implement timeline log fallback correction, version tag auto-suggestion and unit tests | M2 | DONE |
| 4 | Verification & Build | Verify builds, run Vitest, JUnit tests, and push to sprint-6 branch | M3 | DONE |

## Interface Contracts
### Frontend ↔ Backend
- Validating draft XML: calls `/validate` or `/validate-draft` using multipart/form-data.
- Version tag format: must be valid SemVer (e.g. `1.0.0`) in the XML.

## Code Layout
- Backend validation adapter: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapter/CamundaBpmnValidationAdapter.java`
- Backend version tag validation test: `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/adapter/BpmnVersionTagValidationTest.java`
- Frontend designer view: `frontend/src/components/BpmnDesigner.vue` or similar
- Frontend designer test: `frontend/src/components/BpmnDesigner.spec.ts` or similar
