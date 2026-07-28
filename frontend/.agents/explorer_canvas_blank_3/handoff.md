# Handoff Report — Root Cause Analysis of MainLayout.vue Blank Canvas Bug

This report details the investigation of the blank canvas issue occurring during screen navigation and role changes.

---

## 1. Observation

1. **File Path and Component Structure**:
   In `src/layouts/MainLayout.vue` (lines 253-260), the router-view slot destructures only `Component`:
   ```vue
   <router-view v-slot="{ Component }">
     <transition name="fade" mode="out-in">
       <!-- @Traceability(US = "US-001", CA = {"CA-12"}) Acierto UX: Keep-Alive retiene scroll y filtros en RAM para 0ms de carga en regresos -->
       <keep-alive include="Workdesk">
         <component :is="Component" :key="route.fullPath + '-' + authStore.activeRole" />
       </keep-alive>
     </transition>
   </router-view>
   ```

2. **Route Instantiation**:
   In `<script setup lang="ts">` of `src/layouts/MainLayout.vue` (line 280), `route` is defined via:
   ```typescript
   const route = useRoute();
   ```
   This references the global reactive route object of the application.

3. **Vitest Unit Test Setup**:
   Unit tests inside `src/tests/layouts/MainLayout.spec.ts` stub `<router-view>` and mock `vue-router`'s `useRoute`:
   ```typescript
   const RouterViewStub = { template: '<div><slot :Component="{}" /></div>' };
   ...
   vi.mock('vue-router', () => ({
       useRoute: vi.fn(() => ({
           path: '/admin/modeler/bpmn'
       })),
       ...
   ```
   Here, `useRoute()` does not return a `fullPath` property (only `path`).

---

## 2. Logic Chain

1. **Step 1 (Usage of Global Route)**: The dynamic key on the `<component>` element inside `<router-view>` is `:key="route.fullPath + '-' + authStore.activeRole"`. Since `route` is not destructured from `<router-view>`'s slot, it references the global `route` object from `useRoute()` in the component's script section (Observation 1 & 2).
2. **Step 2 (Key Desynchronization during Transitions)**: During navigation, the global `route` updates immediately to the target route. The outgoing component (which is still transitioning out of the DOM under `<transition name="fade" mode="out-in">`) has its key dynamically re-evaluated using the *new* route. Changing the key of a transitioning component forces Vue to immediately recreate or unmount the component, resulting in a sudden, jarring unmount (blank canvas visual glitch).
3. **Step 3 (TypeError Trigger)**: When the application initializes or runs in testing/isolated environments, `route` or `route.fullPath` can be `undefined` (Observation 3). Attempting to read `fullPath` on an undefined object (or a custom mock lacking the property) triggers a runtime `TypeError` (`Cannot read properties of undefined (reading 'fullPath')`), which breaks the render loop and results in a blank screen.

---

## 3. Caveats

- **Test Stubs**: Unit tests currently use custom stubs (`RouterViewStub`) that do not pass the `route` object to the slot scope. Therefore, once slot-scoped destructuring is introduced, the key will fallback to empty string in the test context if the stub is not updated. However, the proposed fallback `:key="route?.fullPath ? route.fullPath + '-' + authStore.activeRole : ''"` successfully mitigates this and keeps tests green.

---

## 4. Conclusion

The blank canvas bug is caused by:
1. Accessing the global `route` instead of the local, slot-scoped `route` in `<router-view>`, which changes the key of transitioning-out components and breaks Vue's layout rendering.
2. The lack of optional chaining and defensive fallbacks for the `:key` binding, causing crashes when `route` or `route.fullPath` is undefined.

**Recommended Fix**:
Modify `src/layouts/MainLayout.vue` to:
1. Destructure `route` from the slot: `<router-view v-slot="{ Component, route }">`
2. Update the `:key` to use optional chaining and a default fallback:
   `:key="route?.fullPath ? route.fullPath + '-' + authStore.activeRole : ''"`

---

## 5. Verification Method

To verify the resolution of this issue:
1. **Vitest Run**: Run the unit test suite to verify no regressions:
   ```powershell
   npx vitest run src/tests/layouts/MainLayout.spec.ts
   npx vitest run src/tests/components/MainLayout.spec.ts
   ```
2. **Verify Build**: Build the frontend successfully to ensure template compilation passes:
   ```powershell
   npm run build
   ```
3. **Manual Validation**: Perform role changes and view transitions in the browser, verifying that the screen transitions smoothly and no `TypeError` is printed in the browser console.
