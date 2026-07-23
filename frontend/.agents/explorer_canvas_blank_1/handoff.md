# Handoff Report — Blank Canvas Bug Investigation

This report outlines the Root Cause Analysis (RCA) and proposed fix strategy for the blank canvas bug in `MainLayout.vue` inside the `ibpms-platform/frontend` application.

## 1. Observation

Direct observations made during codebase exploration and test suite execution:

1. **Target Component Logic:**
   In `src/layouts/MainLayout.vue`, lines 253–260 contain the component definition for secondary view rendering:
   ```vue
   253:         <router-view v-slot="{ Component }">
   254:           <transition name="fade" mode="out-in">
   255:             <!-- @Traceability(US = "US-001", CA = {"CA-12"}) Acierto UX: Keep-Alive retiene scroll y filtros en RAM para 0ms de carga en regresos -->
   256:             <keep-alive include="Workdesk">
   257:               <component :is="Component" :key="route.fullPath + '-' + authStore.activeRole" />
   258:             </keep-alive>
   259:           </transition>
   260:         </router-view>
   ```
2. **Vue Router Layout Binding:**
   The `route` variable referenced in the key expression above is resolved at the component setup level (lines 269 and 280):
   ```typescript
   269: import { useRouter, useRoute } from 'vue-router';
   ...
   280: const route = useRoute();
   ```
3. **Vitest Warnings Logged:**
   During test suite execution (`npx vitest run`), warning logs show key evaluation anomalies:
   ```
   [Vue warn]: Component is missing template or render function: {}
     at <Anonymous key="undefined-null" > 
     at <KeepAlive include="Workdesk" > 
     at <BaseTransition mode="out-in" appear=false persisted=false ... > 
     at <Transition name="fade" mode="out-in" > 
     at <Anonymous> 
     at <MainLayout ref="VTU_COMPONENT" > 
     at <VTUROOT>
   ```
4. **Router Guard Execution:**
   In `src/router/RouteGuards.ts`, the decentralized navigation security guard `rbacGuard` handles hydration and updates of the roles and global status:
   ```typescript
   53:         if (!hasAccess) {
   54:             console.warn(`[SECURITY 403] Interceptor Obscurity CA-3 Activado. Ocultando URL ${to.path}. Rol activo provisto: ${activeRole || 'Ninguno'}`);
   55:             // CA-3: Falso 404. Se mantiene URL intacta en barra de navegación, el render pasa a NotFound.
   56:             authStore.isGlobal404 = true;
   57:             return next(); // Pasa la barrera del Router, pero el DOM colapsa en App.vue
   58:         }
   59:         authStore.isGlobal404 = false;
   ```
   In `App.vue`, a reactive hide mechanism handles the DOM when a route is unauthorized:
   ```vue
   57:   <!-- Vue Router Main Canvas -->
   58:   <RouterView v-show="!authStore.isHydrating && !authStore.isGlobal404" />
   ```

---

## 2. Logic Chain

1. **Premise 1 (From Observation 1 & 2):**
   The dynamic `:key` assigned to the child component within the layout's `<router-view>` uses the layout's global setup-level `route` object (`const route = useRoute()`) to read `route.fullPath`.
2. **Premise 2 (From Vue Router behavior):**
   When the router is initializing, during asynchronous hydration, or when a view mounts in test environments that do not register a router provider (or mock it incompletely), `useRoute()` will return `undefined` or a partial object without `fullPath`.
3. **Premise 3 (From Javascript evaluation):**
   Attempting to access a property (`fullPath`) on `undefined` (`route.fullPath`) throws a runtime `TypeError: Cannot read properties of undefined (reading 'fullPath')`.
4. **Premise 4 (From Observation 3):**
   When Vitest renders `MainLayout` with a mock route object lacking `fullPath` or with a stubbed `<router-view>` (which fails to pass a local `route` object via scope slot), the evaluated key falls back to `"undefined-null"`. In production, this causes the app canvas to crash completely and render a blank canvas if `route` evaluates to `undefined`.
5. **Premise 5 (From Observation 1 & Vue Transition lifecycle):**
   During transition transitions from **Route A** to **Route B** with `mode="out-in"`, the layout's global `route` changes to Route B immediately. Because the dynamic key uses this global `route.fullPath`, Route A's component (which is currently fading out) is forced to re-render using Route B's key. This key mismatch causes state corruption and unmounting crashes for the outgoing component, leading to transition failures and the blank screen symptom.
6. **Premise 6 (Fix Logic):**
   Using slot destructuring (`v-slot="{ Component, route }"`) binds the route key strictly to the local VNode instance currently rendered by the slot, preserving the key during fade-out transitions. Utilizing optional chaining and fallback (`route?.fullPath ? route.fullPath + '-' + authStore.activeRole : ''`) guarantees the expression resolves to `''` if `route` or its properties are missing, preventing `TypeError` crashes.

---

## 3. Caveats

* **Assumptions:** We assume that the layout component will always be rendered inside a router context in production, but we still apply defensive optional chaining to secure unit testing and early boot stages.
* **Scope Limits:** We did not investigate backend auth controller roles verification or other layouts, as the issue is restricted to the dynamic key within `MainLayout.vue`.

---

## 4. Conclusion

The blank canvas bug is a combination of a **TypeError** due to missing/undefined route properties and a **transition key mismatch** when referencing layout-level `useRoute()` instead of slot-scoped `route`. 

**Actionable Fix:**
Update the `<router-view>` slot destructured properties and the dynamic `:key` expression in `src/layouts/MainLayout.vue` as follows:
* Destructure `route` from `<router-view>` slot scope.
* Update key expression to defensively handle undefined states via optional chaining and fallback: `route?.fullPath ? route.fullPath + '-' + authStore.activeRole : ''`.

See the git patch: `.agents/explorer_canvas_blank_1/mainlayout.patch` for precise lines to modify.

---

## 5. Verification Method

To verify the correction:
1. Apply the patch `mainlayout.patch` to `src/layouts/MainLayout.vue`.
2. Run project tests:
   ```bash
   npx vitest run
   ```
3. Inspect warning logs in the test console to ensure the following warning is no longer generated for `MainLayout.spec.ts`:
   `[Vue warn]: Component is missing template or render function: {}` and `key="undefined-null"`
4. Verify layout navigation switches between user roles and paths (e.g. `/workdesk` and `/admin`) visually render the components correctly without producing a white screen of death.
