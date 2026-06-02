# Project Plan: Hallazgo 1 Security Bypass Resolution

## Architecture & Scope
- Target File: `src/router/index.ts`
- Goal: Fix security bypass in the `DlqDashboard` route.
- Action: Replace `requiredRole` with `roles` property, containing `['ROLE_ADMIN_IT', 'ROLE_SUPER_ADMIN']`.
- Verification Test: `src/tests/regression_hallazgo1.spec.ts`

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | Exploration & Strategy | Analyze router configuration, routing logic, and the regression spec file. | None | DONE |
| 2 | Implementation | Implement change in `src/router/index.ts` to use `roles` properties. | M1 | DONE |
| 3 | Verification & Review | Run Vitest and build compilation. Conduct reviews and forensic audits. | M2 | DONE |

## Interface & Contract Changes
- The route `DlqDashboard`'s meta property must change from `requiredRole: 'ROLE_ADMIN_IT'` (or similar) to `roles: ['ROLE_ADMIN_IT', 'ROLE_SUPER_ADMIN']`.
