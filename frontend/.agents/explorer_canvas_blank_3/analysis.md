# Root Cause Analysis (RCA) — Blank Canvas Bug in MainLayout.vue

## Executive Summary
This document provides a Root Cause Analysis (RCA) of the blank canvas bug in `src/layouts/MainLayout.vue` during screen navigation and role changes. The bug manifests as a blank screen or a complete crash of the rendering hierarchy when navigating between screens or triggering role switches.

---

## 1. Problem Identification & Observations

### 1.1 Code Inspection of `src/layouts/MainLayout.vue`
In `src/layouts/MainLayout.vue`, lines 253 to 261 contain the following markup:

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

In `<script setup lang="ts">`, the route is imported and defined as:
```typescript
269: import { useRouter, useRoute } from 'vue-router';
...
280: const route = useRoute();
```

### 1.2 The Root Cause
1. **Global Route Reference Mismatch (Slot Shadowing Lack)**: 
   The `<router-view>` component exposes the currently matched route for each component slot via `v-slot="{ Component, route }"`. However, `MainLayout.vue` only destructures `v-slot="{ Component }"`. As a result, the `:key` attribute on line 257 references the global `route` object returned by `useRoute()` in `<script setup>`.
2. **Key Mutability During Transitions**:
   Because it uses the global `route` object, when a user navigates to a new path, the global `route.fullPath` updates *immediately*. During a transition (e.g., `<transition mode="out-in">`), the outgoing (leaving) component is still rendering in the DOM. Its `:key` expression is dynamically re-evaluated using the *new* route's path. Changing the key of a transitioning component forces Vue to immediately unmount or re-instantiate it, breaking the transition animation and causing a sudden unmount/blank canvas crash.
3. **TypeError Susceptibility**:
   In environments where the router is not fully initialized, during early routing hooks, or in unit tests (where `useRoute()` might return `undefined` or a partial object lacking `fullPath`), the expression `route.fullPath` throws a `TypeError: Cannot read properties of undefined (reading 'fullPath')`. This halts component rendering entirely.

---

## 2. Logic Chain

1. **Premise 1**: The `<component>` rendering is bound to `:key="route.fullPath + '-' + authStore.activeRole"`.
2. **Premise 2**: `route` is defined in setup as `const route = useRoute()`, representing the global route state.
3. **Premise 3**: During transitions, the global `route` changes before the outgoing component has finished transitioning out of the DOM.
4. **Premise 4**: If `route` is undefined or does not possess the `fullPath` property (e.g., during unit tests with mocked/stubbed router setups or initial ticks), accessing `route.fullPath` directly throws a `TypeError`.
5. **Deduction**: This TypeError halts rendering, leading to a blank canvas. If the key shifts during a transition, the virtual DOM diffing process triggers unexpected unmounting, causing a blank screen visual bug.

---

## 3. Proposed Fix Strategy

To fix this issue without breaking existing layouts, we recommend:
1. **Destructure `route` from `<router-view>`**:
   Change `<router-view v-slot="{ Component }">` to `<router-view v-slot="{ Component, route }">`. This binds `route` inside the slot to the specific matched route for that Component. The outgoing component will retain its key linked to the old route until it completely transitions out.
2. **Use Optional Chaining and Defensive Fallback**:
   Update the `:key` binding to:
   ```vue
   :key="route?.fullPath ? route.fullPath + '-' + authStore.activeRole : ''"
   ```
   This ensures that if `route` is undefined (or lacks `fullPath`), it falls back safely to an empty string, preventing any `TypeError` crash.

### Proposed Diff:
```diff
<<<<
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <!-- @Traceability(US = "US-001", CA = {"CA-12"}) Acierto UX: Keep-Alive retiene scroll y filtros en RAM para 0ms de carga en regresos -->
            <keep-alive include="Workdesk">
              <component :is="Component" :key="route.fullPath + '-' + authStore.activeRole" />
            </keep-alive>
          </transition>
        </router-view>
====
        <router-view v-slot="{ Component, route }">
          <transition name="fade" mode="out-in">
            <!-- @Traceability(US = "US-001", CA = {"CA-12"}) Acierto UX: Keep-Alive retiene scroll y filtros en RAM para 0ms de carga en regresos -->
            <keep-alive include="Workdesk">
              <component :is="Component" :key="route?.fullPath ? route.fullPath + '-' + authStore.activeRole : ''" />
            </keep-alive>
          </transition>
        </router-view>
>>>>
```

---

## 4. Verification Method

Once implemented, the fix should be verified using:
1. **Vitest Unit Tests**:
   Ensure all MainLayout tests pass successfully. Run:
   ```powershell
   npx vitest run src/tests/layouts/MainLayout.spec.ts
   npx vitest run src/tests/components/MainLayout.spec.ts
   ```
2. **Production Build Audit**:
   Build the frontend to ensure that the template compiler is happy with the slot destructuring and optional chaining:
   ```powershell
   npm run build
   ```
3. **Manual Screen Transition / Role Switch Validation**:
   Navigate between the screens (e.g., Modeler, Workdesk) and change roles. Verify that the view transitions smoothly without any blank screens or console errors.
