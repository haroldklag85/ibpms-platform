# Project: Login Bug Fix

## Architecture
- Axios API client handles all HTTP requests in the frontend. We need to bypass the 401 response interceptor for credential checking endpoints so that they fail immediately instead of attempting intercept/redirect/suspension.
- `BreakGlassLogin.vue` is a Vue component containing the Break Glass / Emergency Login form. It needs a justification input with `data-testid="justification-input"`, and dynamic styling/colors on the error banner depending on the error code/type.
- Playwright E2E tests in `emergency-login-feedback.spec.ts` test the Emergency Login flow. They must fill in the justification field prior to submission, and verify the styled error message for different login failures.

## Code Layout
- Axios Client: `frontend/src/services/apiClient.ts`
- Break Glass Component: `frontend/src/components/auth/BreakGlassLogin.vue`
- Playwright E2E Tests: `frontend/e2e/emergency-login-feedback.spec.ts`

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | Exploration | Inspect `apiClient.ts`, `BreakGlassLogin.vue`, and E2E tests to plan the exact code changes | None | DONE |
| 2 | Implementation | Apply the fixes to Axios response interceptor, Vue component form & styling, and E2E tests | M1 | DONE |
| 3 | Verification | Run Reviewers and Challengers to verify tests pass and logic is correct | M2 | DONE |
| 4 | Audit | Run Forensic Auditor to verify no integrity violations and validate solution | M3 | DONE |

## Interface Contracts
- `/auth/login`, `/auth/emergency-login`, `/auth/break-glass`, `/auth/change-password` must not be suspended/intercepted by the 401 Axios response interceptor.
- `BreakGlassLogin.vue` justification field must have `data-testid="justification-input"`.
- Error Banner in `BreakGlassLogin.vue` must use:
  - `bg-amber-50 border-amber-500 text-amber-800` for `USER_NOT_FOUND`
  - `bg-red-50 border-red-600 text-red-800` for `INVALID_PASSWORD`
  - `bg-gray-100 border-gray-400 text-gray-700` for `ACCOUNT_DISABLED`
  - `bg-red-900 border-red-700 text-red-50` for network failures
