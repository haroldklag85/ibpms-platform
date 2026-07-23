# Changes

This document contains a log of the changes made to resolve the blank canvas bug in `src/layouts/MainLayout.vue`.

## Code Modifications

### 1. `src/layouts/MainLayout.vue`
- Destructured `route` from the `<router-view>` slot scope (`v-slot="{ Component, route }"`).
- Replaced the direct use of the setup-scope `route.fullPath` key binding with optional chaining and a fallback key:
  ```vue
  :key="route?.fullPath ? route.fullPath + '-' + authStore.activeRole : ''"
  ```
- This prevents the dynamic key from updating prematurely during transitions (which caused rendering disruptions/blank canvas issues) and avoids `TypeError` when `route` is undefined.

### 2. `src/tests/layouts/MainLayout.spec.ts`
- Added a unit test to verify that when `route` is undefined in the slot scope (e.g. mock stubs in tests), the layout safely defaults to an empty string (`''`) fallback key.
- Added a unit test to verify that when `route` is provided in the slot scope, the layout binds the correct dynamic key structure (`route.fullPath + '-' + activeRole`).
- Used a recursive vnode tree traversal to find and assert the keys of the rendered components dynamically.

## Verification
- Run commands:
  - `npx vitest run src/tests/layouts/MainLayout.spec.ts src/tests/components/MainLayout.spec.ts` (PASSED, 9/9 tests)
  - `npm run build` (PASSED, successfully compiled production bundle)
