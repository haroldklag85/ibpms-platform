# Handoff Report - Milestone 1 - Hallazgo 1 Security Bypass Resolution

## 1. Observation
We observed the following files and code snippets in the frontend workspace (`c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\frontend`):

*   **Route Definition for `DlqDashboard`**:
    *   **File Path**: `src/router/index.ts`
    *   **Snippet (Lines 159-163)**:
        ```typescript
        {
            path: 'admin/integration/dlq',
            name: 'DlqDashboard',
            component: () => import('@/views/admin/Integration/DlqDashboard.vue'),
            meta: { requiresAuth: true, requiredRole: 'ADMIN_IT' }
        },
        ```
    *   **Navigation Guard Registration (Line 210)**:
        ```typescript
        router.beforeResolve(rbacGuard);
        ```

*   **Authentication & Authorization Guard (`rbacGuard`)**:
    *   **File Path**: `src/router/RouteGuards.ts`
    *   **Snippet (Lines 46-59)**:
        ```typescript
        // 2. Verificación RBAC Estricta (Solo si la ruta especifica .roles)
        if (to.meta.roles && Array.isArray(to.meta.roles)) {
            const activeRole = authStore.activeRole;

            // Validar el acceso contra el rol seleccionado actualmente (activeRole)
            const hasAccess = activeRole ? (to.meta.roles as string[]).includes(activeRole) : false;
            
            if (!hasAccess) {
                console.warn(`[SECURITY 403] Interceptor Obscurity CA-3 Activado. Ocultando URL ${to.path}. Rol activo provisto: ${activeRole || 'Ninguno'}`);
                // CA-3: Falso 404. Se mantiene URL intacta en barra de navegación, el render pasa a NotFound.
                authStore.isGlobal404 = true;
                return next(); // Pasa la barrera del Router, pero el DOM colapsa en App.vue
            }
        }
        ```

*   **Regression Test Suite**:
    *   **File Path**: `src/tests/regression_hallazgo1.spec.ts`
    *   **Snippet (Lines 17-35)**:
        ```typescript
        it('debe denegar el acceso a la ruta DLQ (/admin/integration/dlq) si el usuario NO tiene el rol requerido (ROLE_ADMIN_IT o ROLE_SUPER_ADMIN)', async () => {
            const authStore = useAuthStore();
            
            // Simular usuario autenticado pero con rol básico de OPERARIO
            authStore.token = 'MOCK-JWT-TOKEN';
            authStore.user = { 
                username: 'operario_user', 
                roles: ['ROLE_OPERARIO'] 
            };
            authStore.activeRole = 'ROLE_OPERARIO';
            authStore.isGlobal404 = false;

            // Intentar navegar al DLQ Dashboard
            await router.push('/admin/integration/dlq');

            // En estado RED, el test fallará aquí porque requiredRole no se evalúa por el guard y isGlobal404 queda en false (acceso concedido).
            // En estado GREEN, el guard detectará el rol no autorizado a través de la propiedad 'roles' y establecerá isGlobal404 a true.
            expect(authStore.isGlobal404).toBe(true);
        });
        ```

*   **Integration Route Test**:
    *   **File Path**: `src/tests/views/admin/Integration/DlqDashboard.spec.ts`
    *   **Snippet (Lines 79-87)**:
        ```typescript
        it('TEST-F05: Debe verificar que la ruta está protegida con requiredRole ADMIN_IT', () => {
            // Leemos router.ts
            const routerPath = path.resolve(process.cwd(), 'src/router/index.ts');
            const routerSource = fs.readFileSync(routerPath, 'utf-8');

            // Regex simple para atrapar la defincion de la ruta dlq, con o sin slash inicial
            const dlqRoutePattern = /path:\s*['"`]\/?admin\/integration\/dlq['"`][\s\S]*?meta:\s*\{[^}]*requiredRole:\s*['"`]ADMIN_IT['"`]/;
            expect(dlqRoutePattern.test(routerSource)).toBe(true);
        });
        ```

---

## 2. Logic Chain
1. **Observation 1**: The route `DlqDashboard` `/admin/integration/dlq` defines `requiredRole: 'ADMIN_IT'` in its metadata (`src/router/index.ts:162`).
2. **Observation 2**: The navigation guard `rbacGuard` (`src/router/RouteGuards.ts:46`) only performs the RBAC (role-based access control) verification if `to.meta.roles` is defined and is an array.
3. **Deduction 1**: Since the route metadata defines `requiredRole` instead of `roles`, `to.meta.roles` is `undefined`.
4. **Deduction 2**: As a result, the guard completely bypasses authorization checks for `DlqDashboard`. Any authenticated user, regardless of their role, can access `/admin/integration/dlq` (Security Bypass / RED state of regression test).
5. **Observation 3**: The regression test file `src/tests/regression_hallazgo1.spec.ts` attempts to navigate an unauthorized user (`ROLE_OPERARIO`) to `/admin/integration/dlq` and asserts that `authStore.isGlobal404` becomes `true`. It fails currently because access is bypassed.
6. **Observation 4**: The test file `src/tests/views/admin/Integration/DlqDashboard.spec.ts` contains `TEST-F05` which performs a regex match on `src/router/index.ts` source code, asserting the presence of `requiredRole: 'ADMIN_IT'`.
7. **Deduction 3**: Replacing `requiredRole: 'ADMIN_IT'` with `roles: ['ROLE_ADMIN_IT', 'ROLE_SUPER_ADMIN']` in `src/router/index.ts` is required to fix the security bypass and make the regression test pass. However, doing so will cause `TEST-F05` in `DlqDashboard.spec.ts` to fail because the regex pattern expects `requiredRole: 'ADMIN_IT'`.
8. **Conclusion**: Resolving the security bypass requires a two-part code modification: replacing the metadata property in `src/router/index.ts` and updating the static analysis regex test in `DlqDashboard.spec.ts`.

---

## 3. Caveats
- **Active Role Assumption**: We assume that `authStore.activeRole` is the authoritative role used for RBAC decisions in the application, which is consistent with the other route definitions like `IntakeTriage` (`roles: ['Global Admin', 'ROLE_SUPER_ADMIN']`) and `AdminSettings` (`roles: ['ROLE_SUPER_ADMIN', 'Global Admin']`).
- **Build/Test execution**: As a read-only agent, we have not executed the test runner (`vitest`) to verify current failure status, but the code structure confirms the RED/GREEN behavior specified in comments.

---

## 4. Conclusion & Proposed Strategy Plan
To resolve the Security Bypass for Hallazgo 1, we propose the following changes:

### Step 1: Update the Route Definition in `src/router/index.ts`
Modify the route definition of `DlqDashboard` to replace `requiredRole` with `roles` specifying `['ROLE_ADMIN_IT', 'ROLE_SUPER_ADMIN']`.

*   **File**: `src/router/index.ts` (Lines 159-163)
*   **Before**:
    ```typescript
                    {
                        path: 'admin/integration/dlq',
                        name: 'DlqDashboard',
                        component: () => import('@/views/admin/Integration/DlqDashboard.vue'),
                        meta: { requiresAuth: true, requiredRole: 'ADMIN_IT' }
                    },
    ```
*   **After**:
    ```typescript
                    {
                        path: 'admin/integration/dlq',
                        name: 'DlqDashboard',
                        component: () => import('@/views/admin/Integration/DlqDashboard.vue'),
                        meta: { requiresAuth: true, roles: ['ROLE_ADMIN_IT', 'ROLE_SUPER_ADMIN'] }
                    },
    ```

### Step 2: Update the Router Verification Test in `DlqDashboard.spec.ts`
Modify `TEST-F05` in `src/tests/views/admin/Integration/DlqDashboard.spec.ts` to assert the correct `roles` list format.

*   **File**: `src/tests/views/admin/Integration/DlqDashboard.spec.ts` (Lines 79-87)
*   **Before**:
    ```typescript
        it('TEST-F05: Debe verificar que la ruta está protegida con requiredRole ADMIN_IT', () => {
            // Leemos router.ts
            const routerPath = path.resolve(process.cwd(), 'src/router/index.ts');
            const routerSource = fs.readFileSync(routerPath, 'utf-8');

            // Regex simple para atrapar la defincion de la ruta dlq, con o sin slash inicial
            const dlqRoutePattern = /path:\s*['"`]\/?admin\/integration\/dlq['"`][\s\S]*?meta:\s*\{[^}]*requiredRole:\s*['"`]ADMIN_IT['"`]/;
            expect(dlqRoutePattern.test(routerSource)).toBe(true);
        });
    ```
*   **After**:
    ```typescript
        it('TEST-F05: Debe verificar que la ruta está protegida con roles ROLE_ADMIN_IT y ROLE_SUPER_ADMIN', () => {
            // Leemos router.ts
            const routerPath = path.resolve(process.cwd(), 'src/router/index.ts');
            const routerSource = fs.readFileSync(routerPath, 'utf-8');

            // Regex simple para atrapar la defincion de la ruta dlq, con o sin slash inicial
            const dlqRoutePattern = /path:\s*['"`]\/?admin\/integration\/dlq['"`][\s\S]*?meta:\s*\{[^}]*roles:\s*\[\s*['"`]ROLE_ADMIN_IT['"`]\s*,\s*['"`]ROLE_SUPER_ADMIN['"`]\s*\]/;
            expect(dlqRoutePattern.test(routerSource)).toBe(true);
        });
    ```

### How the Navigation Guard Handles It:
1. `rbacGuard` will intercept navigation to `/admin/integration/dlq`.
2. It detects `to.meta.roles` which is now `['ROLE_ADMIN_IT', 'ROLE_SUPER_ADMIN']`.
3. It evaluates if the current active role (`authStore.activeRole`) is included in that array.
4. If it's NOT included (e.g. `ROLE_OPERARIO`), it triggers "Cyber Gaslighting": sets `authStore.isGlobal404 = true` and returns `next()`, causing the view to render a 404 (Obscurity) without modifying the URL.
5. If it IS included (e.g. `ROLE_ADMIN_IT` or `ROLE_SUPER_ADMIN`), it calls `next()` normally and resolves the route.

---

## 5. Verification Method
To independently verify this resolution after implementing the changes:

1.  **Run the Regression Test Suite**:
    Execute the following command in the frontend workspace terminal:
    ```bash
    npx vitest run src/tests/regression_hallazgo1.spec.ts
    ```
    *Verification Condition*: The tests should pass (GREEN state).

2.  **Run the Component / Route Test Suite**:
    Execute the following command in the frontend workspace terminal:
    ```bash
    npx vitest run src/tests/views/admin/Integration/DlqDashboard.spec.ts
    ```
    *Verification Condition*: The test `TEST-F05` should pass (GREEN state).

3.  **Run All Tests**:
    Execute all frontend unit and integration tests to ensure no regressions:
    ```bash
    npx vitest run
    ```
    *Verification Condition*: All tests pass successfully.
