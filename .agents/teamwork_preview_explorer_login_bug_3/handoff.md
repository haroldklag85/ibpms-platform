# Handoff Report — 2026-05-30T00:49:02Z

## 1. Observation
We observed the following files and details:

### A. Axios 401 Interceptor
File: `frontend/src/services/apiClient.ts`
Line Numbers: 160-165
Verbatim Code:
```typescript
        if (error.response && error.response.status === 401) {
            console.warn('CA-27: Emitiendo Soft-Lock por Expiración de Token en Backend');
            const event = new CustomEvent('global-error-dispatch', { detail: { type: 'SESSION_EXPIRED' } });
            window.dispatchEvent(event);
            return new Promise(() => {}); // Interceptar y suspender en lugar de destruir estado
        }
```

### B. Justification Input in BreakGlassLogin.vue
File: `frontend/src/components/auth/BreakGlassLogin.vue`
Line Numbers: 46-52
Verbatim Code:
```html
        <textarea 
          v-model="emergencyForm.justification"
          required 
          rows="3"
          placeholder="Describa el motivo de la activación del protocolo (Ej: Caída masiva de EntraID / Redis Outage)..."
          class="w-full bg-white border-2 border-red-100 rounded-xl p-3 text-xs font-medium text-gray-900 focus:border-red-500 focus:ring-0 transition-all placeholder:text-red-200 resize-none"
        ></textarea>
```

### C. Error Banner in BreakGlassLogin.vue
File: `frontend/src/components/auth/BreakGlassLogin.vue`
Line Numbers: 71-74
Verbatim Code:
```html
    <div v-if="error" data-testid="login-error-banner" class="mt-4 p-3 bg-red-100 border-l-4 border-red-600 rounded flex items-start gap-3">
       <span class="material-symbols-outlined text-red-600 text-[18px]">error</span>
       <p class="text-[11px] text-red-800 font-bold leading-tight">{{ error }}</p>
    </div>
```

### D. Playwright Tests
File: `frontend/e2e/emergency-login-feedback.spec.ts`
Line Numbers: 7-106
Key Test Scenarios:
- `ESC-01`: "Muestra banner ámbar cuando el usuario no existe"
- `ESC-02`: "Muestra banner rojo cuando la contraseña es incorrecta"
- `ESC-04`: "Muestra banner gris cuando la cuenta está deshabilitada (Mock)"
- `ESC-07`: "Muestra banner rojo oscuro genérico cuando el backend está caído (Mock)"

### E. Backend AuthSyncController.java Error Responses
File: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/security/AuthSyncController.java`
Line Numbers: 106-169
Key HTTP Status Codes and Error JSON formats:
- User Not Found: returns `401 Unauthorized` with `{"code": "USER_NOT_FOUND", "message": "No existe una cuenta asociada al correo proporcionado."}`
- Invalid Password: returns `401 Unauthorized` with `{"code": "INVALID_PASSWORD", "message": "La contraseña proporcionada es incorrecta."}`
- Account Disabled: returns `403 Forbidden` with `{"code": "ACCOUNT_DISABLED", "message": "La cuenta existe pero se encuentra deshabilitada. Contacte al administrador."}`

---

## 2. Logic Chain
1. **R1: Axios Response Interceptor for 401 errors**
   - *Observation A* shows that when any request gets a `401` status, the response interceptor catches it, fires a `'global-error-dispatch'` event, and returns a pending Promise (`new Promise(() => {})`).
   - If `/auth/emergency-login` returns a `401` (due to a non-existent user or an incorrect password as described in *Observation E*), this interceptor will capture it and return a pending Promise.
   - Therefore, the request in `BreakGlassLogin.vue` never completes (neither resolves nor rejects). The form submission logic remains stuck in `loading` state, no error state is set, and the error banner is never displayed, failing tests `ESC-01` and `ESC-02`.
   - **Conclusion**: We must modify `apiClient.ts` to check the request URL and bypass the `401` suspension interceptor for `/auth/emergency-login` (and `/auth/login`), rejecting the error instead.

2. **R2: Justification Input with `data-testid="justification-input"`**
   - *Observation B* shows that the `textarea` used for justification does not have a `data-testid` attribute.
   - **Conclusion**: We must add `data-testid="justification-input"` to the `textarea` in `BreakGlassLogin.vue` to align with the requirements.

3. **R3: Dynamic Error Banner Styling in BreakGlassLogin.vue**
   - *Observation C* shows that the error banner uses hardcoded red classes (`bg-red-100 border-red-600 text-red-800` and icon `text-red-600`).
   - *Observation D* shows that playwright tests expect specific banner feedback styles:
     - Amber banner for non-existent users (e.g. `bg-amber-100 border-amber-600 text-amber-800` / icon `text-amber-600`).
     - Gray banner for disabled accounts (e.g. `bg-gray-100 border-gray-600 text-gray-800` / icon `text-gray-600`).
     - Dark Red banner for server down/connection failures (e.g. `bg-red-900 border-red-950 text-red-100` / icon `text-red-400`).
     - Standard Red banner for wrong password / other failures (e.g. `bg-red-100 border-red-600 text-red-800` / icon `text-red-600`).
   - **Conclusion**: We should add an `errorCode` state, modify `handleEmergencyLogin` to populate it from `err.response?.data?.code` (or `'NETWORK_ERROR'` if the server is unreachable), and use Vue `computed` properties to dynamically bind classes for the banner container and icon.

---

## 3. Caveats
- The exact color classes matching the designer's expectation for "ámbar", "gris", "rojo oscuro", and "rojo" are assumed based on Tailwind's design system standards.
- Tests in `emergency-login-feedback.spec.ts` only assert content presence (e.g., `.toContainText()`) and visibility, rather than checking the exact Tailwind class list. The style implementation choice is scoped to standard clean semantic colors.

---

## 4. Conclusion
To fix the emergency login feedback and prevent hanging on 401s:
1. Modify `frontend/src/services/apiClient.ts` inside the response interceptor for 401 errors to reject requests to `/auth/emergency-login` immediately.
2. Add `data-testid="justification-input"` to the justification textarea in `frontend/src/components/auth/BreakGlassLogin.vue`.
3. Update `frontend/src/components/auth/BreakGlassLogin.vue` to dynamically apply styling to the error banner based on whether the error relates to a missing account (amber), a disabled account (gray), a connection failure (dark red), or an incorrect password (standard red).

Proposed patches and files have been written to the working directory:
- `apiClient.patch`: Patch file for R1.
- `BreakGlassLogin.patch`: Patch file for R2 & R3.
- `proposed_BreakGlassLogin.vue`: Complete proposed component file for reference.

---

## 5. Verification Method
1. **Inspect Patch Applications**:
   Apply `apiClient.patch` and `BreakGlassLogin.patch` to the codebase.
2. **Execution Command**:
   Run the Playwright E2E tests targeting the emergency login spec:
   ```bash
   cd frontend
   npx playwright test e2e/emergency-login-feedback.spec.ts --project=login-tests
   ```
3. **Invalidation Conditions**:
   - If any of the tests in `emergency-login-feedback.spec.ts` time out, the Axios 401 interceptor is still intercepting the error and returning a pending promise.
   - If `ESC-01` fails, the banner is not visible or contains incorrect text.
   - If the justification textarea fails to be selected, verify that the `data-testid="justification-input"` attribute is present.
