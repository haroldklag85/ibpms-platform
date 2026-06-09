# Synthesis: Login and Break-Glass Auth Feedback Bug Fixes

## Consensus
There is total consensus between all explorer reports on the root causes and locations for the required changes:

1. **R1: Fix Promise Hanging on 401 Auth Errors**
   - **Root Cause:** The Axios response interceptor in `frontend/src/services/apiClient.ts` captures every 401 response and returns a pending `new Promise(() => {})` to prevent session destruction on standard operations. However, this stops authentication endpoints (like `/auth/emergency-login` and `/auth/login`) from rejecting the promise, leaving form submissions hung in a loading state.
   - **Fix Location:** `frontend/src/services/apiClient.ts` inside the response 401 interceptor.
   - **Resolution:** Check if the config URL matches credential-checking endpoints (e.g. `/auth/emergency-login` or `/auth/login`) and immediately return `Promise.reject(error)` if so.

2. **R2: Handle Justification Field in E2E Tests and Form**
   - **Fix Location (Vue component):** `frontend/src/components/auth/BreakGlassLogin.vue` justification `<textarea>`.
   - **Resolution (Vue component):** Add `data-testid="justification-input"` to the `<textarea>`.
   - **Fix Location (Tests):** `frontend/e2e/emergency-login-feedback.spec.ts` in all relevant test cases.
   - **Resolution (Tests):** Update all 7 E2E tests to locate the justification input via `page.locator('[data-testid="justification-input"]')` or `page.fill('[data-testid="justification-input"]', ...)` and fill it with a non-empty string before clicking submit.

3. **R3: Dynamic Error Banner Styling**
   - **Fix Location:** `frontend/src/components/auth/BreakGlassLogin.vue`.
   - **Resolution:** Implement a computed property to bind the banner classes dynamically based on the error code/type. The classes must match the prompt requirements exactly:
     - Amber (`bg-amber-50 border-amber-500 text-amber-800`) for `USER_NOT_FOUND`
     - Red (`bg-red-50 border-red-600 text-red-800`) for `INVALID_PASSWORD`
     - Gray (`bg-gray-100 border-gray-400 text-gray-700`) for `ACCOUNT_DISABLED`
     - Dark Red (`bg-red-900 border-red-700 text-red-50`) for network connection failures (or when there is no response/network error).

## Resolved Conflicts
No conflicts identified. Both Explorer 1 and Explorer 3 agree on the files and the logical flow of changes.

## Gaps
None. The required Tailwind classes and testid names are explicitly given in the user requirements.

## Next Steps
Spawn a Worker to implement these changes across the three files:
- `frontend/src/services/apiClient.ts`
- `frontend/src/components/auth/BreakGlassLogin.vue`
- `frontend/e2e/emergency-login-feedback.spec.ts`
And verify by running the Playwright tests.
