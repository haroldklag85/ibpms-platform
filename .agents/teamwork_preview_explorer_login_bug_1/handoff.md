# Handoff Report: Emergency Login Bug Investigation

## 1. Observation
I investigated the three designated files:
- `frontend/src/services/apiClient.ts`
- `frontend/src/components/auth/BreakGlassLogin.vue`
- `frontend/e2e/emergency-login-feedback.spec.ts`

### Observations from `frontend/src/services/apiClient.ts`
At lines 160-165, the Axios interceptor handles 401 response errors by emitting a session-expired event and returning a pending promise:
```typescript
        if (error.response && error.response.status === 401) {
            console.warn('CA-27: Emitiendo Soft-Lock por Expiración de Token en Backend');
            const event = new CustomEvent('global-error-dispatch', { detail: { type: 'SESSION_EXPIRED' } });
            window.dispatchEvent(event);
            return new Promise(() => {}); // Interceptar y suspender en lugar de destruir estado
        }
```
This intercepts *every* 401 response, including authentication attempts.

### Observations from `frontend/src/components/auth/BreakGlassLogin.vue`
At lines 46-52:
```vue
        <textarea 
          v-model="emergencyForm.justification"
          required 
          rows="3"
          placeholder="Describa el motivo de la activación del protocolo (Ej: Caída masiva de EntraID / Redis Outage)..."
          class="w-full bg-white border-2 border-red-100 rounded-xl p-3 text-xs font-medium text-gray-900 focus:border-red-500 focus:ring-0 transition-all placeholder:text-red-200 resize-none"
        ></textarea>
```
The justification `textarea` has no `data-testid` attribute.

At lines 71-74:
```vue
    <div v-if="error" data-testid="login-error-banner" class="mt-4 p-3 bg-red-100 border-l-4 border-red-600 rounded flex items-start gap-3">
       <span class="material-symbols-outlined text-red-600 text-[18px]">error</span>
       <p class="text-[11px] text-red-800 font-bold leading-tight">{{ error }}</p>
    </div>
```
The error banner uses statically defined Tailwind utility classes (`bg-red-100 border-red-600 text-red-800 text-red-600`) and does not adapt visual presentation dynamically based on the error.

### Observations from `frontend/e2e/emergency-login-feedback.spec.ts`
This E2E test file checks for distinct visual classes and texts:
- `ESC-01`: "Muestra banner ámbar cuando el usuario no existe"
- `ESC-02`: "Muestra banner rojo cuando la contraseña es incorrecta"
- `ESC-04`: "Muestra banner gris cuando la cuenta está deshabilitada (Mock)"
- `ESC-07`: "Muestra banner rojo oscuro genérico cuando el backend está caído (Mock)"

## 2. Logic Chain
1. **R1 (Axios 401 Interception)**:
   - *Observation*: The Axios response interceptor intercepts all 401 errors, emitting a `SESSION_EXPIRED` event and returning an unresolved `new Promise(() => {})`.
   - *Inference*: When a user tries to authenticate on the emergency login form with invalid credentials and receives a 401, the calling component's `try/catch` block inside `handleEmergencyLogin` never catches the rejection. The login button is stuck in the loading state, and the error banner is never displayed.
   - *Resolution*: Checking if `error.config?.url` contains `/auth/` allows the interceptor to bypass the soft-lock logic for auth calls, calling `Promise.reject(error)` instead. This allows the login component to receive the error and provide user feedback.

2. **R2 (Justification Input testid)**:
   - *Observation*: The justification `<textarea>` lacks `data-testid="justification-input"`.
   - *Inference*: E2E test frameworks cannot target the input reliably via testid matching.
   - *Resolution*: Adding `data-testid="justification-input"` to the `<textarea>` solves this limitation.

3. **R3 (Dynamic Error Banner)**:
   - *Observation*: The Playwright test expects distinct visual representations (amber, red, gray, dark red) depending on why the login failed.
   - *Inference*: A computed property inside `BreakGlassLogin.vue` that inspects the contents of `error.value` and returns class mappings (e.g. `bg-amber-100 border-amber-600` for "no existe", `bg-gray-100 border-gray-600` for "desactivada", etc.) makes the layout fully dynamic and compliant with the spec.

## 3. Caveats
- I did not execute the code changes directly in the workspace since my role is limited to a read-only investigation.
- I assumed the Tailwind CSS configuration has classes for `bg-amber-100`, `border-amber-600`, `bg-gray-100`, `border-gray-600`, `bg-red-950`, `border-red-800`, `text-amber-800`, `text-amber-600`, `text-gray-800`, `text-gray-600`, `text-red-200`, and `text-red-400`. These are standard Tailwind classes.

## 4. Conclusion
We must implement three specific modifications:
- Update the Axios response interceptor in `frontend/src/services/apiClient.ts` to bypass 401 soft-locking if the request URL contains `/auth/`.
- Add `data-testid="justification-input"` to the justification `textarea` in `frontend/src/components/auth/BreakGlassLogin.vue`.
- Create a computed property `bannerStyles` in `frontend/src/components/auth/BreakGlassLogin.vue` to map specific error messages to Tailwind classes (amber, red, gray, dark red), and apply them dynamically to the banner, icon, and message text elements.

The patch file `login_bug_fixes.patch` is created inside the working directory (`.agents/teamwork_preview_explorer_login_bug_1/login_bug_fixes.patch`) with the exact modifications.

## 5. Verification Method
1. Apply the patch:
   `git apply .agents/teamwork_preview_explorer_login_bug_1/login_bug_fixes.patch`
2. Run the frontend unit tests to ensure no regressions:
   `npm run test -- --run`
3. Start the dev server in E2E mode:
   `npm run dev:e2e`
4. Run the Playwright E2E tests:
   `npx playwright test frontend/e2e/emergency-login-feedback.spec.ts`
