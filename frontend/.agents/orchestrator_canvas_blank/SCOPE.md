# Scope: iBPMS Platform Central Canvas Blank Screen Bug Fix

## Architecture
- Frontend Application: Vue.js, Vue Router, Pinia
- File of Interest: `ibpms-platform/frontend/src/layouts/MainLayout.vue`
- Problem: Canvas goes completely blank during screen navigation and role changes.
- Cause: The dynamic `:key` assigned to `<component :is="Component" :key="route.fullPath + '-' + authStore.activeRole" />` inside `<router-view>` and `<keep-alive>` triggers a TypeError (e.g. if `route` or `route.fullPath` is undefined/unreactive).

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|---|---|---|---|
| 1 | RCA & Investigation | Explore MainLayout.vue, route object lifecycle, and simulate behavior in Vitest | None | DONE |
| 2 | Implementation | Modify MainLayout.vue to safely use the slot-injected route object and apply optional chaining fallback for the :key | M1 | DONE |
| 3 | Verification & Audit | Run all Vitest tests and npm run build; perform Forensic Audit | M2 | DONE |

## Interface Contracts
- MainLayout.vue uses `v-slot="{ Component, route }"` to destructure the injected slot-scope `route` object.
- MainLayout.vue uses a robust and defensive key: `route?.fullPath ? route.fullPath + '-' + authStore.activeRole : ''`.
