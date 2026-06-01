# Strategy Plan — Security Bypass Resolution (Hallazgo 1)

## Overview
Currently, the route definition for the Dead Letter Queue (DLQ) Dashboard (`DlqDashboard`) in `src/router/index.ts` utilizes the property `requiredRole: 'ADMIN_IT'` inside the `meta` block. However, the application's central RBAC navigation guard (`rbacGuard` inside `src/router/RouteGuards.ts`) only evaluates authorized access using the standard `roles` property (which must be an array of strings). Because `requiredRole` is used, the navigation guard bypasses checking authorization entirely, allowing any authenticated user to access the route. Furthermore, the regression test `src/tests/regression_hallazgo1.spec.ts` asserts that both `ROLE_ADMIN_IT` and `ROLE_SUPER_ADMIN` should have access to the DLQ Dashboard, whereas all other roles (such as `ROLE_OPERARIO`) must be denied access.

This plan details the steps to fix this security bypass and align the configuration and tests.

---

## Detailed Steps

### Step 1: Update Route Definition in `src/router/index.ts`
Modify the routing definition for the `DlqDashboard` component. Change the `meta` configuration to replace the non-standard `requiredRole` property with the standard `roles` array containing both `ROLE_ADMIN_IT` and `ROLE_SUPER_ADMIN`.

**Before:**
```typescript
                {
                    path: 'admin/integration/dlq',
                    name: 'DlqDashboard',
                    component: () => import('@/views/admin/Integration/DlqDashboard.vue'),
                    meta: { requiresAuth: true, requiredRole: 'ADMIN_IT' }
                },
```

**After:**
```typescript
                {
                    path: 'admin/integration/dlq',
                    name: 'DlqDashboard',
                    component: () => import('@/views/admin/Integration/DlqDashboard.vue'),
                    meta: { requiresAuth: true, roles: ['ROLE_ADMIN_IT', 'ROLE_SUPER_ADMIN'] }
                },
```

### Step 2: Update the Static Verification Test in `src/tests/views/admin/Integration/DlqDashboard.spec.ts`
The test suite for the DLQ Dashboard contains an assertion `TEST-F05` which performs a regex match on `src/router/index.ts` to verify the presence of `requiredRole: 'ADMIN_IT'`. Since we are changing `requiredRole` to `roles` and expanding the permitted roles, this regex search will fail unless we update it.

Update the test `TEST-F05` in `src/tests/views/admin/Integration/DlqDashboard.spec.ts` to match the new `roles: ['ROLE_ADMIN_IT', 'ROLE_SUPER_ADMIN']` schema.

**Before:**
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

**After:**
```typescript
    it('TEST-F05: Debe verificar que la ruta está protegida con roles ROLE_ADMIN_IT y ROLE_SUPER_ADMIN', () => {
        // Leemos router.ts
        const routerPath = path.resolve(process.cwd(), 'src/router/index.ts');
        const routerSource = fs.readFileSync(routerPath, 'utf-8');

        // Regex simple para atrapar la defincion de la ruta dlq, con o sin slash inicial, y que contenga los roles especificados
        const dlqRoutePattern = /path:\s*['"`]\/?admin\/integration\/dlq['"`][\s\S]*?meta:\s*\{[^}]*roles:\s*\[\s*['"`]ROLE_ADMIN_IT['"`]\s*,\s*['"`]ROLE_SUPER_ADMIN['"`]\s*\]/;
        expect(dlqRoutePattern.test(routerSource)).toBe(true);
    });
```

### Step 3: Verify the Navigation Guard Behavior
The central navigation guard `rbacGuard` (`src/router/RouteGuards.ts`) is executed before resolving any route (`router.beforeResolve(rbacGuard)`). Let's review how it handles the updated route meta configuration:
1. `rbacGuard` resets `authStore.isGlobal404 = false`.
2. It verifies the presence of the JWT token. If none is found and `requiresAuth` is `true`, it redirects to `/login`.
3. If a token exists but `authStore.user` is null (e.g. page refresh), it hydrates the authentication details.
4. Next, it checks: `if (to.meta.roles && Array.isArray(to.meta.roles))`. Since `DlqDashboard` will now have `roles` defined as an array, this block is entered.
5. It reads the user's active role: `const activeRole = authStore.activeRole;`.
6. It checks: `const hasAccess = activeRole ? (to.meta.roles as string[]).includes(activeRole) : false;`.
   - If the active role is `ROLE_ADMIN_IT` or `ROLE_SUPER_ADMIN`, `hasAccess` is `true`, the guard does nothing extra and execution proceeds to `next()`, rendering the page successfully.
   - If the active role is anything else (e.g. `ROLE_OPERARIO` or `undefined`), `hasAccess` is `false`. It enters the error block, sets `authStore.isGlobal404 = true`, and calls `next()`, which causes the rendering to collapse into the 404/403 page while maintaining the URL intact (Security by Obscurity).
This matches the exact logic asserted in `src/tests/regression_hallazgo1.spec.ts`.

---

## Verification and Testing
Once changes are implemented by the implementer agent, they should run:
```bash
npx vitest run src/tests/regression_hallazgo1.spec.ts
npx vitest run src/tests/views/admin/Integration/DlqDashboard.spec.ts
```
Both test suites should pass (green state).
