# Handoff Report — Login and Break-Glass Auth Feedback Bug Fixes

## 1. Observation
We observed the following regarding the bug and codebase state:
- In `frontend/src/services/apiClient.ts` (lines 160-165 before changes), all 401 unauthorized errors were intercepted and suspended:
  ```typescript
  if (error.response && error.response.status === 401) {
      console.warn('CA-27: Emitiendo Soft-Lock por Expiración de Token en Backend');
      const event = new CustomEvent('global-error-dispatch', { detail: { type: 'SESSION_EXPIRED' } });
      window.dispatchEvent(event);
      return new Promise(() => {}); // Interceptar y suspender en lugar de destruir estado
  }
  ```
  This prevented the catch block of components calling credential-checking endpoints (such as `/auth/login`, `/auth/emergency-login`, `/auth/break-glass`, and `/auth/change-password`) from executing because the promise returned from the interceptor never resolved or rejected.
- In `frontend/src/components/auth/BreakGlassLogin.vue`, the justification textarea element lacked a `data-testid` attribute (lines 46-52 before changes):
  ```vue
  <textarea 
    v-model="emergencyForm.justification"
    required 
    rows="3"
    placeholder="Describa el motivo de la activación del protocolo (Ej: Caída masiva de EntraID / Redis Outage)..."
    class="..."
  ></textarea>
  ```
- The error banner in `BreakGlassLogin.vue` had static class names for styling:
  ```vue
  <div v-if="error" data-testid="login-error-banner" class="mt-4 p-3 bg-red-100 border-l-4 border-red-600 rounded flex items-start gap-3">
     <span class="material-symbols-outlined text-red-600 text-[18px]">error</span>
     <p class="text-[11px] text-red-800 font-bold leading-tight">{{ error }}</p>
  </div>
  ```
- Running the Playwright tests initially failed because the justification input was required by the form but not filled in the tests, nor did the tests assert the specific dynamic colors:
  ```
  Running 7 tests using 1 worker
  x  1 [login-tests] › e2e\emergency-login-feedback.spec.ts:7:5 › Emergency Login — Feedback Diferenciado › ESC-01: Muestra banner ámbar cuando el usuario no existe (6.3s)
  ```

## 2. Logic Chain
1. To prevent hanging promises on authentication/credential checks, we bypassed the 401 interceptor's suspension for critical endpoints (`/auth/login`, `/auth/emergency-login`, `/auth/break-glass`, `/auth/change-password`) by returning `Promise.reject(error)` immediately.
2. To fill out the justification field in the E2E tests, we added `data-testid="justification-input"` to the justification textarea.
3. We updated all 7 test cases in `frontend/e2e/emergency-login-feedback.spec.ts` to locate this field using `[data-testid="justification-input"]` and fill it with a non-empty string (`'some justification string'`) before clicking the submit button.
4. To style the error banner dynamically based on the error code/type:
   - We created a reactive `errorCode` ref in the script tag of `BreakGlassLogin.vue`.
   - We populated `errorCode` inside the catch block of `handleEmergencyLogin`.
   - We mapped the `errorCode` to the specified classes via a computed property:
     - `USER_NOT_FOUND` -> `bg-amber-50 border-amber-500 text-amber-800`
     - `INVALID_PASSWORD` -> `bg-red-50 border-red-600 text-red-800`
     - `ACCOUNT_DISABLED` -> `bg-gray-100 border-gray-400 text-gray-700`
     - `NETWORK_ERROR` (connection failure) -> `bg-red-900 border-red-700 text-red-50`
   - We bound these classes to the banner element and removed hardcoded child text colors so they inherit the parent banner wrapper styling.
5. We updated the assertions in the Playwright spec file to check that the correct classes exist on the banner depending on the error scenario using `await expect(banner).toHaveClass(...)`.

## 3. Caveats
- No caveats.

## 4. Conclusion
The login promise hanging issue on 401 errors, justification field handling in the form/tests, and dynamic error banner styling have been successfully implemented and tested.

## 5. Verification Method
To independently verify the changes, execute the following command:
```bash
cd frontend && npx playwright test e2e/emergency-login-feedback.spec.ts
```

### Exact Output of Verifying Command:
```
Running 7 tests using 1 worker

  ok 1 [login-tests] › e2e\emergency-login-feedback.spec.ts:7:5 › Emergency Login — Feedback Diferenciado › ESC-01: Muestra banner ámbar cuando el usuario no existe (1.1s)
  ok 2 [login-tests] › e2e\emergency-login-feedback.spec.ts:20:5 › Emergency Login — Feedback Diferenciado › ESC-02: Muestra banner rojo cuando la contraseña es incorrecta (1.1s)
  ok 3 [login-tests] › e2e\emergency-login-feedback.spec.ts:33:5 › Emergency Login — Feedback Diferenciado › ESC-03: Login exitoso redirige a /workdesk sin banner (1.4s)
  ok 4 [login-tests] › e2e\emergency-login-feedback.spec.ts:45:5 › Emergency Login — Feedback Diferenciado › ESC-04: Muestra banner gris cuando la cuenta está deshabilitada (Mock) (1.1s)
  ok 5 [login-tests] › e2e\emergency-login-feedback.spec.ts:64:5 › Emergency Login — Feedback Diferenciado › ESC-05: El banner se limpia al reintentar (1.7s)
  ok 6 [login-tests] › e2e\emergency-login-feedback.spec.ts:85:5 › Emergency Login — Feedback Diferenciado › ESC-06: El banner se destruye al volver a SSO (1.6s)
  ok 7 [login-tests] › e2e\emergency-login-feedback.spec.ts:103:5 › Emergency Login — Feedback Diferenciado › ESC-07: Muestra banner rojo oscuro genérico cuando el backend está caído (Mock) (924ms)

  7 passed (12.8s)
```
