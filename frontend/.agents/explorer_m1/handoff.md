# Handoff Report - Hallazgo 2 Analysis and Planning

This handoff report contains the planning and proposed modifications to address **Hallazgo 2: Unprotected Modeler and Admin Routes Security**.

## 1. Observation

- **Routing Configuration File**: Located at `src/router/index.ts`.
- **Test File**: Located at `src/tests/regression_hallazgo2.spec.ts`.
- **Target Views**:
  - `src/views/admin/GenericForm/GenericFormView.vue` (Exists on filesystem but currently lacks a route definition in `src/router/index.ts`).
  - `src/views/admin/Modeler/InstancesManager.vue` (Exists on filesystem but currently lacks a route definition in `src/router/index.ts`).

- **Test Execution Details**:
  Running `npx vitest run src/tests/regression_hallazgo2.spec.ts` fails with **13 failed assertions** out of 53 tests.
  Verbatim test failure snippet:
  ```
  FAIL  src/tests/regression_hallazgo2.spec.ts > Regression - Hallazgo 2: Unprotected Modeler and Admin Routes Security Test > Ruta: /admin/intake > debe denegar el acceso a /admin/intake para un usuario con rol no autorizado (ROLE_OPERARIO)
  AssertionError: expected false to be true
  ```
  The specific test failures are:
  - **Access allowed when it should be denied (missing `roles` metadata):**
    - `/admin/intake`
    - `/admin/generic-form`
    - `/admin/project-builder`
    - `/admin/integration/mapper`
    - `/admin/modeler/instances`
    - `/admin/integration/catalog`
    - `/sgdea/vault`
  - **Access denied when it should be allowed (mismatched `roles` array content):**
    - `/admin/pmo/settings` (Expected: `ROLE_ADMIN_IT`, found: `Global Admin`)
    - `/admin` (Expected: `ROLE_ADMIN_IT`, found: `Global Admin`)
    - `/ai/prompts` (Expected: `ROLE_SUPER_ADMIN`, `ROLE_ANALYST_IT`, found: `Global Admin`, `prompt_engineer`)
    - `/admin/mailboxes` (Expected: `ROLE_SUPER_ADMIN`, `ROLE_ADMIN_IT`, found: `Global Admin`)

## 2. Logic Chain

1. **RBAC Guard Design**: In `src/router/RouteGuards.ts`, the router guard evaluates roles if they are defined as an array in `to.meta.roles` (lines 47-59):
   ```typescript
   if (to.meta.roles && Array.isArray(to.meta.roles)) {
       const activeRole = authStore.activeRole;
       const hasAccess = activeRole ? (to.meta.roles as string[]).includes(activeRole) : false;
       if (!hasAccess) {
           authStore.isGlobal404 = true;
           return next();
       }
   }
   ```
2. **Missing `roles` metadata**: If `meta.roles` is undefined, `rbacGuard` skips the check, allowing any authenticated user (e.g., `ROLE_OPERARIO`) to access the page. This explains the 7 routes that fail because they allow unauthorized access.
3. **Mismatched `roles` content**: If `meta.roles` does not contain the roles expected in the test (e.g., `/admin` currently has `['ROLE_SUPER_ADMIN', 'Global Admin']` instead of `['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT']`), the guard blocks legitimate users, triggering `isGlobal404 = true` for them. This explains the 6 tests failing because access is denied to authorized roles.
4. **Missing route definitions**: `/admin/generic-form` and `/admin/modeler/instances` need to be added to the nested routes list under `MainLayout` using the lazy-loading pattern `() => import(...)` to conform to Vue project practices and avoid circular dependency warnings.

## 3. Caveats

- **No Caveats**: The scope of investigation is clean, and the required modifications map one-to-one with the regression test expectations.

## 4. Conclusion

The implementation worker must make the following modifications to `src/router/index.ts`:
1. Add route for `/admin/generic-form` dynamically importing `@/views/admin/GenericForm/GenericFormView.vue` with roles `['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT']`.
2. Add route for `/admin/modeler/instances` dynamically importing `@/views/admin/Modeler/InstancesManager.vue` with roles `['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT']`.
3. Add `roles` arrays to the metadata of `/admin/intake`, `/admin/project-builder`, `/admin/integration/mapper`, `/admin/integration/catalog`, and `/sgdea/vault`.
4. Update `roles` arrays for `/admin`, `/admin/pmo/settings`, `/ai/prompts`, and `/admin/mailboxes` to match the test definitions.

A pre-packaged patch file has been created as `route_updates.patch` in this agent's folder (`.agents/explorer_m1/`).

### Mapping of the 32 Principal Vistas/Screens to Routes:
Below is the status of the 32 principal views in `src/router/index.ts`:

| # | Vue Component Path | Route Path | Current Meta | Action / Proposed Meta |
|---|---|---|---|---|
| 1 | `Login.vue` | `/login` | `{ isPublic: true }` | Keep intact |
| 2 | `Portal.vue` | `/` (Portal) | `{ requiresAuth: true }` | Keep intact |
| 3 | `Workdesk.vue` | `/workdesk` | `{ requiresAuth: true }` | Keep intact |
| 4 | `IntakeTriageView.vue` | `/intake-triage` | `{ title: 'Triaje Intake', roles: ['Global Admin', 'ROLE_SUPER_ADMIN'] }` | Keep intact |
| 5 | `kanban/KanbanView.vue` | `/kanban` | None | Keep intact |
| 6 | `admin/SettingsView.vue` | `/admin` | `{ requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'Global Admin'] }` | **Update roles**: `['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT']` |
| 7 | `admin/GenericForm/GenericFormView.vue` | `/admin/generic-form` | *Not registered* | **Add Route**: `{ requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] }` |
| 8 | `admin/IncidentCenter.vue` | `/admin/incidents` | `{ requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT'] }` | Keep intact |
| 9 | `admin/Modeler/BpmnDesigner.vue` | `/admin/modeler/bpmn` | `{ requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] }` | Keep intact |
| 10 | `admin/Modeler/FormList.vue` | `/admin/modeler/forms` | `{ requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] }` | Keep intact |
| 11 | `admin/Modeler/FormDesigner.vue` | `/admin/modeler/forms/designer` | `{ requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] }` | Keep intact |
| 12 | `admin/Modeler/DmnIntelligence.vue` | `/admin/modeler/dmn` | `{ requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT'] }` | Keep intact |
| 13 | `admin/Modeler/InstancesManager.vue` | `/admin/modeler/instances` | *Not registered* | **Add Route**: `{ requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT'] }` |
| 14 | `inbox/InboxView.vue` | `/inbox` | `{ requiresAuth: true }` | Keep intact |
| 15 | `admin/ServiceDelivery/IntakeManual.vue` | `/admin/intake` | `{ requiresAuth: true }` | **Add roles**: `['ROLE_SUPER_ADMIN']` |
| 16 | `admin/ServiceDelivery/Customer360.vue` | `/admin/customer360` | `{ requiresAuth: true }` | Keep intact |
| 17 | `public/CustomerPortal.vue` | `/portal/tracking` | `{ requiresAuth: false, isPublic: true }` | Keep intact |
| 18 | `public/PublicIntake.vue` | `/public/start/:processKey` | `{ requiresAuth: false, isPublic: true }` | Keep intact |
| 19 | `admin/ProjectBuilder/ProjectBuilder.vue` | `/admin/project-builder` | `{ requiresAuth: true }` | **Add roles**: `['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT']` |
| 20 | `admin/ProjectBuilder/ProjectManager.vue` | `/admin/projects/manager` | `{ requiresAuth: true }` | Keep intact |
| 21 | `admin/ProjectBuilder/AgileHub.vue` | `/admin/projects/agile-hub/:projectId?` | `{ requiresAuth: true }` | Keep intact |
| 22 | `admin/Analytics/DashboardBAM.vue` | `/admin/analytics/bam` | `{ requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'Global Admin'] }` | Keep intact |
| 23 | `admin/Integration/ConnectorCatalog.vue` | `/admin/integration/catalog` | `{ requiresAuth: true }` | **Add roles**: `['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT']` |
| 24 | `admin/Integration/ConnectorBuilder.vue` | `/admin/integration/builder` | `{ requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT'] }` | Keep intact |
| 25 | `admin/Integration/VisualMapper.vue` | `/admin/integration/mapper` | `{ requiresAuth: true }` | **Add roles**: `['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT']` |
| 26 | `admin/Integration/DlqDashboard.vue` | `/admin/integration/dlq` | `{ requiresAuth: true, roles: ['ROLE_ADMIN_IT', 'ROLE_SUPER_ADMIN'] }` | Keep intact |
| 27 | `admin/SGDEA/DocumentGrid.vue` | `/sgdea/vault` | `{ title: 'Bóveda Documental', requiresAuth: true }` | **Add roles**: `['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT']` |
| 28 | `admin/AI/PromptLibrary.vue` | `/ai/prompts` | `{ title: 'Librería de Prompts', requiresAuth: true, roles: ['Global Admin', 'prompt_engineer'] }` | **Update roles**: `['ROLE_SUPER_ADMIN', 'ROLE_ANALYST_IT']` |
| 29 | `admin/AI/SacConfigManager.vue` | `/admin/mailboxes` | `{ title: 'Buzones Inbound Graph', requiresAuth: true, roles: ['Global Admin'] }` | **Update roles**: `['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT']` |
| 30 | `admin/Security/IdentityGovernance.vue` | `/admin/security/identity` | `{ title: 'Gobernanza de Identidades', requiresAuth: true, roles: ['ROLE_SUPER_ADMIN', 'SUPER_ADMIN', 'Global Admin', 'ibpms_rol_SUPER_ADMIN'] }` | Keep intact |
| 31 | `admin/PMO/PmoSettings.vue` | `/admin/pmo/settings` | `{ title: 'Configuración PMO / SLA', requiresAuth: true, roles: ['Global Admin', 'ROLE_SUPER_ADMIN'] }` | **Update roles**: `['ROLE_SUPER_ADMIN', 'ROLE_ADMIN_IT']` |
| 32 | `admin/RbacManager/RbacManagerView.vue` | *None (Deprecated)* | *N/A* | Deprecated in production in favor of `IdentityGovernance.vue`. No action needed. |

## 5. Verification Method

To verify the fixes independently:
1. Apply the patch file:
   ```powershell
   git apply ibpms-platform/frontend/.agents/explorer_m1/route_updates.patch
   ```
2. Run the regression test command in directory `ibpms-platform/frontend`:
   ```powershell
   npx vitest run src/tests/regression_hallazgo2.spec.ts
   ```
3. Confirm that all 53 test cases are passing (GREEN flag).
