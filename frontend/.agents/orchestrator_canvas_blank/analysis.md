# Synthesized Root Cause Analysis: Blank Canvas Bug in MainLayout.vue

## Consensus Findings
1. **Scope Lack of Slot-Scoped Route**:
   The `<router-view>` in `src/layouts/MainLayout.vue` was only destructuring `{ Component }` and omitting `{ Component, route }`. As a result, the dynamic `:key` attribute of the child `<component>` referenced the globally injected `route` object from `useRoute()` in the layout's `<script setup>`.
2. **Dynamic Key Instability during Transition**:
   The global `route` object updates immediately at the start of navigation. During transitions (where `mode="out-in"` is used), the outgoing component's key changes immediately, causing rendering disruptions, unmounting, or crashes.
3. **TypeError during Testing & Edge Cases**:
   In unit tests (such as `MainLayout.spec.ts`), the router-view is mocked using a custom stub `RouterViewStub = { template: '<div><slot :Component="{}" /></div>' }`. This stub does not provide a `route` slot parameter. If the layout destructures `route` without defensive guards, `route` resolves to `undefined`, causing `route.fullPath` to throw a `TypeError: Cannot read properties of undefined (reading 'fullPath')`.
4. **Resolution Strategy**:
   - Destructure `route` from the slot: `<router-view v-slot="{ Component, route }">`
   - Use defensive optional chaining with fallback: `:key="route?.fullPath ? route.fullPath + '-' + authStore.activeRole : ''"`

## Resolved Conflicts
- No conflicts were observed between Explorer 2 and Explorer 3. Both identified the exact same slot destructuring lack and the testing stub TypeError cause.

## Gaps
- None. The root cause is fully mapped and verified.
