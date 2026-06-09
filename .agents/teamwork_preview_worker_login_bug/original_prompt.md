## 2026-05-30T00:50:02Z
Your identity is: Codebase Worker.
Your working directory is: C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\.agents\teamwork_preview_worker_login_bug

Your task is to implement the fixes for the login and Break-Glass auth feedback bug as detailed below:

R1. Fix Promise Hanging on 401 Auth Errors:
In `frontend/src/services/apiClient.ts`, locate the 401 interceptor:
```typescript
        if (error.response && error.response.status === 401) {
            console.warn('CA-27: Emitiendo Soft-Lock por Expiración de Token en Backend');
            ...
```
Modify it to check the request URL and bypass the 401 interception/suspension for credential-checking endpoints (such as `/auth/login`, `/auth/emergency-login`, `/auth/break-glass`, `/auth/change-password`). These should return `Promise.reject(error)` immediately so that the calling component's catch block executes.

R2. Handle Justification Field in E2E Tests and Form:
- In `frontend/src/components/auth/BreakGlassLogin.vue`, add `data-testid="justification-input"` to the justification textarea.
- In `frontend/e2e/emergency-login-feedback.spec.ts`, update all 7 test cases (ESC-01, ESC-02, ESC-03, ESC-04, ESC-05, ESC-06, ESC-07) to fill the justification field with a valid non-empty string before submitting the form. Use:
  `await page.fill('[data-testid="justification-input"]', 'some justification string');`

R3. Dynamic Error Banner Styling:
In `frontend/src/components/auth/BreakGlassLogin.vue`, implement dynamic styling on the error banner based on the error code or type:
- Amber (`bg-amber-50 border-amber-500 text-amber-800`) when user does not exist (`USER_NOT_FOUND`).
- Red (`bg-red-50 border-red-600 text-red-800`) when password is incorrect (`INVALID_PASSWORD`).
- Gray (`bg-gray-100 border-gray-400 text-gray-700`) when account is disabled (`ACCOUNT_DISABLED`).
- Dark Red (`bg-red-900 border-red-700 text-red-50`) for network connection failures.
- Make sure to update the Playwright tests in `frontend/e2e/emergency-login-feedback.spec.ts` to assert these classes exist on the banner depending on the error scenario (e.g. `await expect(banner).toHaveClass(/bg-amber-50/);`, etc.).

Verification:
- Run Playwright E2E tests: `cd frontend && npx playwright test e2e/emergency-login-feedback.spec.ts`
- Ensure all 7/7 tests pass successfully.

Write your handoff.md report inside your working directory. Ensure it lists:
1. What was changed.
2. The verification command and exact output (passing tests).

MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A Forensic Auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.
