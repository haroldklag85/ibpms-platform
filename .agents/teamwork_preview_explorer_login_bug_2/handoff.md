# Handoff Report: Login Bug Investigation

## 1. Observation

Direct observations and evidence from the codebase:

### 1.1 Axios 401 Interceptor Issue
In `frontend/src/services/apiClient.ts` (lines 160-165), the 401 response interceptor intercepts all HTTP 401 errors, emitting a global error dispatch and returning an unresolved `Promise`:
```typescript
        if (error.response && error.response.status === 401) {
            console.warn('CA-27: Emitiendo Soft-Lock por Expiración de Token en Backend');
            const event = new CustomEvent('global-error-dispatch', { detail: { type: 'SESSION_EXPIRED' } });
            window.dispatchEvent(event);
            return new Promise(() => {}); // Interceptar y suspender en lugar de destruir estado
        }
```
This halts credentials checking endpoints (like `/auth/emergency-login` or `/auth/login`) which throw 401 on failure, preventing the catch block in `BreakGlassLogin.vue` from executing and notifying the user.

### 1.2 Justification Textarea in `BreakGlassLogin.vue`
In `frontend/src/components/auth/BreakGlassLogin.vue` (lines 46-52), the justification textarea is marked `required` but lacks a `data-testid` attribute:
```html
        <textarea 
          v-model="emergencyForm.justification"
          required 
          rows="3"
          placeholder="Describa el motivo de la activación del protocolo (Ej: Caída masiva de EntraID / Redis Outage)..."
          class="w-full bg-white border-2 border-red-100 rounded-xl p-3 text-xs font-medium text-gray-900 focus:border-red-500 focus:ring-0 transition-all placeholder:text-red-200 resize-none"
        ></textarea>
```

### 1.3 Error Banner in `BreakGlassLogin.vue`
In `frontend/src/components/auth/BreakGlassLogin.vue` (lines 71-74), the error banner is statically styled:
```html
    <div v-if="error" data-testid="login-error-banner" class="mt-4 p-3 bg-red-100 border-l-4 border-red-600 rounded flex items-start gap-3">
       <span class="material-symbols-outlined text-red-600 text-[18px]">error</span>
       <p class="text-[11px] text-red-800 font-bold leading-tight">{{ error }}</p>
    </div>
```

### 1.4 E2E Test Cases in `emergency-login-feedback.spec.ts`
In `frontend/e2e/emergency-login-feedback.spec.ts`, none of the tests fill in the justification field prior to submission (e.g. lines 9-11):
```typescript
        await page.fill('[data-testid="email-input"]', 'no-existe@alpha.com');
        await page.fill('[data-testid="password-input"]', 'cualquiera');
        await page.click('[data-testid="login-submit"]');
```
As the justification textarea is HTML5 `required`, submitting the form fails browser validation. The spec also lacks assertions for styled error CSS classes.

---

## 2. Logic Chain

1. **R1 (Axios 401 Interceptor Bypass)**: Because `/auth/login`, `/auth/emergency-login`, `/auth/break-glass`, and `/auth/change-password` are credential checking endpoints, they must return their actual error responses instead of being suspended/intercepted. Therefore, we check `error.config.url` and immediately return `Promise.reject(error)` if it matches these URLs.
2. **R2 (Justification testid)**: Adding `data-testid="justification-input"` to the `textarea` in `BreakGlassLogin.vue` will satisfy the contract and allow E2E tests to locate and interact with the input.
3. **R3 (Dynamic styling)**:
   - The backend `AuthSyncController.java` returns specific `code` fields for errors:
     - `USER_NOT_FOUND` (HTTP 401)
     - `INVALID_PASSWORD` (HTTP 401)
     - `ACCOUNT_DISABLED` (HTTP 403)
     - Network errors do not provide a response.
   - We store the code in a reactive `errorCode` ref during form submission.
   - The banner div uses a dynamic `:class` binding matching the required styles:
     - `bg-amber-50 border-amber-500 text-amber-800` for `USER_NOT_FOUND`
     - `bg-red-50 border-red-600 text-red-800` for `INVALID_PASSWORD`
     - `bg-gray-100 border-gray-400 text-gray-700` for `ACCOUNT_DISABLED`
     - `bg-red-900 border-red-700 text-red-50` for network failures.
   - We also dynamically color the inner icon (`text-amber-500`, `text-red-600`, `text-gray-500`, `text-red-300`) and make the paragraph text color inherit parent text color by removing `text-red-800`.
4. **E2E Tests (`emergency-login-feedback.spec.ts`)**: Adding `await page.fill('[data-testid="justification-input"]', '...')` before clicking submit prevents HTML5 validation failure. Adding `expect(banner).toHaveClass(...)` assertions verifies the styling.

---

## 3. Caveats

- We assume the Axios `error.config.url` represents the request path (relative or absolute). Using `url.includes('/auth/path')` ensures robust matching regardless of prepended `baseURL`.
- Network errors (aborted request or network down) are detected by `!err.response` or `err.code === 'ERR_NETWORK'`, mapping to a generic `'NETWORK_ERROR'` which applies the dark red `bg-red-900` banner styles.

---

## 4. Conclusion

The exact code edits are proposed via three unified patch files stored in the working directory:
- `apiClient.patch`
- `BreakGlassLogin.patch`
- `emergency-login-feedback.patch`

These patches correctly modify `apiClient.ts` to bypass 401 interceptors for critical auth endpoints, update `BreakGlassLogin.vue` with the dynamic CSS classes and the missing `data-testid`, and adjust the Playwright E2E tests to fill the justification and assert the correct styling classes.

---

## 5. Verification Method

### 5.1 Verification Commands
The implementing agent can apply the patches and verify using:
1. Apply the patches:
   ```bash
   git apply .agents/teamwork_preview_explorer_login_bug_2/apiClient.patch
   git apply .agents/teamwork_preview_explorer_login_bug_2/BreakGlassLogin.patch
   git apply .agents/teamwork_preview_explorer_login_bug_2/emergency-login-feedback.patch
   ```
2. Build the frontend to check for compile errors:
   ```bash
   npm run build --prefix frontend
   ```
3. Run the Playwright E2E tests specifically targeting login/emergency scenarios:
   ```bash
   npx playwright test e2e/emergency-login-feedback.spec.ts --project=login-tests --config=frontend/playwright.config.ts
   ```

### 5.2 Files to Inspect
- `frontend/src/services/apiClient.ts`
- `frontend/src/components/auth/BreakGlassLogin.vue`
- `frontend/e2e/emergency-login-feedback.spec.ts`

### 5.3 Invalidation Conditions
- If the backend returns different code strings (e.g. `USER_INEXISTENT` instead of `USER_NOT_FOUND`), the dynamic styling will fall back to the network failure style.
