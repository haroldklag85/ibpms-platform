# Project: iBPMS Router Restructuring (Hallazgo 2)

## Architecture
- Framework: Vue 3 with Vue Router
- Main routing file: `src/router/index.ts`
- Security Mechanism: Route meta tags (`requiresAuth`, `roles`) checked in a global router navigation guard.
- Validation: Unit/integration tests via Vitest testing the routing table configurations.

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | M1: Exploration & Plan | Explore src/router/index.ts and src/tests/regression_hallazgo2.spec.ts to map existing structure and import paths. | None | DONE |
| 2 | M2: Routing & Import Modification | Update src/router/index.ts to assign correct requiresAuth and roles to all 32 screens, registering new views. | M1 | DONE |
| 3 | M3: Verification & Auditing | Run tests, build the application, run forensic audit checks, and ensure no regression. | M2 | DONE |

## Code Layout
- Routing config: `src/router/index.ts`
- Target Test suite: `src/tests/regression_hallazgo2.spec.ts`
- New view 1: `src/views/admin/GenericForm/GenericFormView.vue` (Import target)
- New view 2: `src/views/admin/Modeler/InstancesManager.vue` (Import target)

## Interface Contracts
### Route Meta format
- `requiresAuth: boolean`
- `roles: string[]` (for protected pages/routes)
