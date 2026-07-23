# Handoff Report: Root Cause Analysis on Blank Canvas Bug

## 1. Observation
* **Affected File**: `src/layouts/MainLayout.vue`
* **Vulnerable Key Code** (lines 253-260):
  ```html
  <router-view v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <!-- @Traceability(US = "US-001", CA = {"CA-12"}) Acierto UX: Keep-Alive retiene scroll y filtros en RAM para 0ms de carga en regresos -->
      <keep-alive include="Workdesk">
        <component :is="Component" :key="route.fullPath + '-' + authStore.activeRole" />
      </keep-alive>
    </transition>
  </router-view>
  ```
* **Vitest Execution Command**: `npx vitest run src/tests/layouts/MainLayout.spec.ts`
* **Verbatim Test Logs**:
  ```
  [Vue warn]: Component is missing template or render function:  {} 
    at <Anonymous key="undefined-null" > 
    at <KeepAlive include="Workdesk" > 
    at <BaseTransition mode="out-in" appear=false persisted=false  ... > 
    at <Transition name="fade" mode="out-in" > 
    at <Anonymous> 
    at <MainLayout ref="VTU_COMPONENT" > 
    at <VTUROOT>
  ```
* **Test Stub Definition** in `src/tests/layouts/MainLayout.spec.ts` (lines 9-10 and 13-16):
  ```typescript
  const RouterViewStub = { template: '<div><slot :Component="{}" /></div>' };
  
  vi.mock('vue-router', () => ({
      useRoute: vi.fn(() => ({
          path: '/admin/modeler/bpmn'
      })),
      ...
  }));
  ```

---

## 2. Logic Chain
1. In `src/layouts/MainLayout.vue` line 253, the template utilizes `<router-view v-slot="{ Component }">`, which fails to destructure the injected slot-scope `route` object.
2. Because of this, the `route` object evaluated on line 257 (inside `:key="route.fullPath + '-' + authStore.activeRole"`) is fetched from the script-setup variable `const route = useRoute();` (line 280).
3. The setup-level `useRoute()` updates instantly as soon as a navigation transition starts. This causes the leaving component to suddenly re-evaluate its key with the new route info, disrupting the leaving transition animation, and potentially mismatching roles/parameters.
4. To fix this, destructuring `route` from the slot (`v-slot="{ Component, route }"`) is required so that the key is pinned to the route associated with that specific component instance (preventing key change on leaving component).
5. However, in `src/tests/layouts/MainLayout.spec.ts`, the custom `RouterViewStub` does not pass the `route` property in the slot binding (`template: '<div><slot :Component="{}" /></div>'`).
6. If the template destructures `route` from the slot but the stub does not provide it, `route` resolves to `undefined` in tests.
7. Consequently, referencing `route.fullPath` causes a `TypeError: Cannot read properties of undefined (reading 'fullPath')`.
8. Any `TypeError` thrown during component rendering halts the Vue rendering pipeline. Without an error boundary, this causes the layout slot to crash, rendering nothing (resulting in the blank canvas).
9. Applying defensive optional chaining and a fallback check (`route?.fullPath ? route.fullPath + '-' + authStore.activeRole : ''`) ensures that if `route` is undefined (in test stubs or during transitional routing phases), it safely falls back to `''` instead of throwing a `TypeError`.

---

## 3. Caveats
* The behavior of slot destructuring was analyzed based on Vue Router 4 specifications and simulated via Vitest. No runtime execution of active role switches on a live dev server was performed due to network and read-only investigator constraints.
* It is assumed that the active role fallback isn't needed when the route is not loaded (or a blank string key `''` is acceptable as a fallback during transitional states).

---

## 4. Conclusion
The blank canvas bug is caused by a `TypeError` when evaluating the dynamic `:key` on the layout's active component when `route` or `route.fullPath` is undefined or unresolved.
To fix the issue, the following two changes must be applied in `src/layouts/MainLayout.vue`:
1. Destructure `route` from the router view slot:
   ```html
   <router-view v-slot="{ Component, route }">
   ```
2. Use defensive optional chaining:
   ```html
   <component :is="Component" :key="route?.fullPath ? route.fullPath + '-' + authStore.activeRole : ''" />
   ```

---

## 5. Verification Method
1. Apply the recommended modifications to `src/layouts/MainLayout.vue`.
2. Run `npx vitest run src/tests/layouts/MainLayout.spec.ts` in the `ibpms-platform/frontend` directory. The tests should pass successfully.
3. Boot up the local server with `npm run dev`, trigger a role change, and verify that navigation transitions proceed smoothly without triggering a blank screen.
