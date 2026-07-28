# Root Cause Analysis: Blank Canvas Bug in MainLayout.vue

## Executive Summary
During screen navigation and role changes in the iBPMS Platform, the main canvas occasionally goes completely blank. This issue is caused by a `TypeError` thrown when evaluating the dynamic `:key` assigned to the router's active component. If the layout attempts to access properties on an unresolved or undefined `route` object, the Vue rendering pipeline crashes. Because there is no top-level error boundary capturing this, the entire router-view renders nothing, resulting in a blank screen.

---

## 1. Current Implementation Analysis

In `src/layouts/MainLayout.vue` (lines 251-261), the router rendering block is configured as follows:

```html
<!-- Lienzo donde se renderizan las vistas secundarias (Router View) -->
<div class="flex-1 overflow-auto bg-transparent relative">
  <router-view v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <!-- @Traceability(US = "US-001", CA = {"CA-12"}) Acierto UX: Keep-Alive retiene scroll y filtros en RAM para 0ms de carga en regresos -->
      <keep-alive include="Workdesk">
        <component :is="Component" :key="route.fullPath + '-' + authStore.activeRole" />
      </keep-alive>
    </transition>
  </router-view>
</div>
```

### Route Provenance
1. **Setup Level**: The script setup block (line 280) defines `const route = useRoute();`.
2. **Template Level**: Because the `<router-view>` slot destructuring only extracts `Component` (`v-slot="{ Component }"`), the variable `route` referenced in `:key="route.fullPath + '-' + authStore.activeRole"` falls back to the setup-defined `route` object (retrieved via `useRoute()`).

---

## 2. Root Cause Analysis

### Cause 1: Global vs. Slot-Scoped Route References during Transitions
* **The Global Hook (`useRoute`)**: The globally injected `useRoute()` updates instantly as soon as a navigation transition starts.
* **The Slot-Scoped Route**: In Vue Router 4, `<router-view>` provides a slot-scope object `{ Component, route }`. The slot-injected `route` represents the matched route *for that specific component instance*.
* **The Defect**:
  During navigation transitions (configured with `mode="out-in"`), the old component is still leaving the DOM while the new component is entering.
  * Because the template references the global `route` (from `useRoute()`), the leaving component's key is re-evaluated immediately using the *new* route's path.
  * This dynamic key change forces the leaving component to suddenly re-instantiate or mutate its key, disrupting the transition animation, breaking the `<keep-alive>` caching mechanism, and potentially causing mismatched route parameters to be passed to components.
  * If the route change is accompanied by a role switch that purges the routing topology, the global route object may temporarily hold undefined or unmatched parameters, resulting in rendering exceptions.

### Cause 2: Missing Guards for Undefined `route` and `fullPath`
If we attempt to switch to the slot-scoped `route` to solve Cause 1 (by using `v-slot="{ Component, route }"`), we introduce a new vulnerability:
* **Unit Testing Stubs**:
  In `src/tests/layouts/MainLayout.spec.ts`, the router view is stubbed out for unit tests as:
  ```typescript
  const RouterViewStub = { template: '<div><slot :Component="{}" /></div>' };
  ```
  This stub only binds `Component` to the slot; it does not bind `route`.
* **The TypeError**:
  If the slot-scope is changed to `v-slot="{ Component, route }"` and `:key="route.fullPath ..."` is evaluated:
  * In the unit test, `route` will destructure as `undefined`.
  * Accessing `route.fullPath` will throw: `TypeError: Cannot read properties of undefined (reading 'fullPath')`.
  * This error halts the rendering pipeline and fails the tests/crashes the component.
* **Edge-case Navigation**:
  During initial app load, routing redirects, or when navigating to unauthorized/unmapped paths (e.g., during active role switches where topology is purged), the slot-injected `route` object can temporarily be `undefined` or lack a `fullPath` property, crashing the user interface.

---

## 3. Recommended Fix Strategy

To establish a robust contract and eliminate both runtime and testing-time TypeErrors, we should adopt the following two-part change:

### Part A: Destructure the Slot-Scoped Route
Modify the `<router-view>` wrapper to extract both `Component` and `route`. This ensures the key remains pinned to the specific route matched by the transitioning component.

```html
<router-view v-slot="{ Component, route }">
```

### Part B: Implement Defensive Optional Chaining & Fallback Key
Use a ternary condition and optional chaining on `route` to avoid any possible `TypeError` if `route` or `route.fullPath` is absent.

```html
<component :is="Component" :key="route?.fullPath ? route.fullPath + '-' + authStore.activeRole : ''" />
```

#### Why this resolves the issue:
1. **If `route` is undefined** (e.g., in unit tests using the stub `RouterViewStub` or during unmapped transition frames):
   * `route?.fullPath` safely short-circuits to `undefined`.
   * The ternary condition evaluates this as falsy and returns the safe fallback value `''`. No TypeError is thrown, and rendering completes successfully.
2. **If `route` is defined**:
   * It uses the slot-specific `route.fullPath` concatenated with `authStore.activeRole`. This stabilizes transition keys and ensures that the `<keep-alive>` component can accurately track and cache instances of the `Workdesk` view per route-role combination.
