# Root Cause Analysis (RCA) — Blank Canvas Bug in MainLayout.vue

## Core Findings
During screen navigation, role changes, or component hydration, `MainLayout.vue` can fail to render its route canvas, leading to a blank screen (white screen of death). This is caused by:
1. **Dynamic Key TypeError**: The component key expression `:key="route.fullPath + '-' + authStore.activeRole"` references the layout's global `const route = useRoute()` object. If the route object or its `fullPath` property is undefined (such as during early loading, router hydration, layout transitions, or mock testing contexts), accessing `route.fullPath` throws a fatal `TypeError` that breaks Vue's render cycle.
2. **Transition Key Mismatch**: During `<transition mode="out-in">`, the global `route` immediately updates to the target route before the outgoing component has finished fading out. This triggers a reactive key change on the outgoing component during its unmount phase, causing invalid rendering states, premature unmounts, and canvas crashes.

---

## 1. Problem boundary & Detailed RCA

### 1.1 Dynamic Key Vulnerability
In `src/layouts/MainLayout.vue` (lines 253–260), the Secondary Canvas renders secondary views via `<router-view>` and matches instance caching via `<keep-alive>` using a dynamic key:
```vue
<router-view v-slot="{ Component }">
  <transition name="fade" mode="out-in">
    <keep-alive include="Workdesk">
      <component :is="Component" :key="route.fullPath + '-' + authStore.activeRole" />
    </keep-alive>
  </transition>
</router-view>
```
The expression `route.fullPath` relies on the component-level reactive `route` reference, defined in the layout setup:
```typescript
import { useRouter, useRoute } from 'vue-router';
...
const route = useRoute();
```
* **Production Risk:** During initialization, dynamic routing registration (lazy-loading routes), or auth redirection guards (`rbacGuard`), there are critical moments where the Vue Router is hydrating, and `useRoute()` may return `undefined` or a partial object without `fullPath` (e.g. before matching completion). Any attempt to read `route.fullPath` directly throws a fatal `TypeError: Cannot read properties of undefined (reading 'fullPath')`.
* **Test Risk:** In testing scenarios (such as `MainLayout.spec.ts`), `<router-view>` is often stubbed out:
  ```typescript
  const RouterViewStub = { template: '<div><slot :Component="{}" /></div>' };
  ```
  Because the stub does not provide a mock `route` object in slot scope, the layout will throw errors if slot destructuring is used without a fallback, or if the global mock is not fully populated.

### 1.2 Transition Phase Mismatch
When the router navigates from **Route A** to **Route B**:
1. The global reactive `route` (from `useRoute()`) updates immediately to Route B's path.
2. Under `mode="out-in"`, the `<transition>` keeps Route A's component mounted to fade it out first.
3. The layout template forces a re-render of Route A's component because its `:key` dynamically changes to Route B's path (since it references the global `route.fullPath`).
4. This mismatch triggers unmounting errors, lifecycle hooks failure, or visual canvas breakage, resulting in a blank viewport.

---

## 2. Evidence Chain

1. **Target File and Location:**
   - **File Path:** `src/layouts/MainLayout.vue`
   - **Line Numbers:** 253–260
   - **Lines of Code:**
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

2. **Test Warnings and Mock Findings:**
   - During project test executions (`npx vitest run`), warning logs reveal matching anomalies:
     ```
     [Vue warn]: Component is missing template or render function: {}
       at <Anonymous key="undefined-null" > 
       at <KeepAlive include="Workdesk" > 
       at <BaseTransition mode="out-in" appear=false persisted=false ... > 
       at <Transition name="fade" mode="out-in" > 
     ```
     This confirms that key resolution issues (`undefined-null`) are present when the active route metadata is missing.

---

## 3. Fix Strategy & Proposed Patch

To prevent the blank canvas bug, we propose two modifications:
1. **Destructure Slot Route:** Destructure the route directly from the `<router-view>` slot: `v-slot="{ Component, route }"`. The slot-injected `route` is a local reference that aligns with the matched component instance currently in the slot, protecting the outgoing component from receiving the new route key during the fade-out transition.
2. **Defensive Key Expression:** Introduce optional chaining (`route?.fullPath`) and a fallback string (`''`) to ensure that even if the route (or its fullPath) is empty, missing, or undefined (e.g. in test stubs or during early layout mount), the key resolves gracefully without throwing a `TypeError`.

### Proposed Diff Patch (`mainlayout.patch`)

```diff
diff --git a/src/layouts/MainLayout.vue b/src/layouts/MainLayout.vue
index 327fa1e..a7bf42d 100644
--- a/src/layouts/MainLayout.vue
+++ b/src/layouts/MainLayout.vue
@@ -250,9 +250,9 @@
       
       <!-- Lienzo donde se renderizan las vistas secundarias (Router View) -->
       <div class="flex-1 overflow-auto bg-transparent relative">
-        <router-view v-slot="{ Component }">
+        <router-view v-slot="{ Component, route }">
           <transition name="fade" mode="out-in">
             <!-- @Traceability(US = "US-001", CA = {"CA-12"}) Acierto UX: Keep-Alive retiene scroll y filtros en RAM para 0ms de carga en regresos -->
             <keep-alive include="Workdesk">
-              <component :is="Component" :key="route.fullPath + '-' + authStore.activeRole" />
+              <component :is="Component" :key="route?.fullPath ? route.fullPath + '-' + authStore.activeRole : ''" />
             </keep-alive>
           </transition>
         </router-view>
```

---

## 4. Verification Method

To verify the effectiveness of the fix:
1. **Apply the patch** to `src/layouts/MainLayout.vue`.
2. **Run all tests** using Vitest:
   ```bash
   npx vitest run
   ```
3. **Verify specific test suites** covering layout behavior:
   - Check `src/tests/layouts/MainLayout.spec.ts` to ensure it passes without warning logs regarding missing render functions or keys.
   - Check that route transition states operate successfully under different user roles (`ROLE_SUPER_ADMIN`, `ROLE_OPERADOR`).
