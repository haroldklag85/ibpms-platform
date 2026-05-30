## 2026-05-30T00:47:14Z

Resolve the recurring login bug in the iBPMS authentication and Break-Glass flow, ensuring that incorrect credentials, disabled user accounts, and connection issues provide proper and styled feedback to the user, and ensure the Playwright E2E tests pass successfully.

Working directory: C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform
Integrity mode: development

## Requirements

### R1. Fix Promise Hanging on 401 Auth Errors
Modify the Axios response interceptor in `frontend/src/services/apiClient.ts` to bypass 401 interception/suspension for credential-checking endpoints (such as `/auth/login`, `/auth/emergency-login`, `/auth/break-glass`, `/auth/change-password`). These should return `Promise.reject(error)` so that caller `catch` blocks can execute.

### R2. Handle Justification Field in E2E Tests and Form
Add `data-testid="justification-input"` to the justification textarea in `frontend/src/components/auth/BreakGlassLogin.vue`. Update the Playwright E2E tests in `frontend/e2e/emergency-login-feedback.spec.ts` (ESC-01, ESC-02, ESC-03, ESC-04, ESC-05, ESC-06, ESC-07) to fill this field with a valid non-empty string before submitting the form.

### R3. Dynamic Error Banner Styling
Implement dynamic styling/colors on the error banner in `BreakGlassLogin.vue` based on the error code or type to match the visual expectations of the test suite:
- Amber (`bg-amber-50 border-amber-500 text-amber-800`) when user does not exist (`USER_NOT_FOUND`).
- Red (`bg-red-50 border-red-600 text-red-800`) when password is incorrect (`INVALID_PASSWORD`).
- Gray (`bg-gray-100 border-gray-400 text-gray-700`) when account is disabled (`ACCOUNT_DISABLED`).
- Dark Red (`bg-red-900 border-red-700 text-red-50`) for network connection failures.

## Acceptance Criteria

### E2E Test Suite Pass
- [ ] Running `npx playwright test e2e/emergency-login-feedback.spec.ts` from `ibpms-platform/frontend` succeeds with 7/7 tests passing.
- [ ] No manual browser intervention is required for the tests to pass.
