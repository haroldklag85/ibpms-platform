# Original User Request

## 2026-06-01T22:20:12Z

Address the bug causing the central canvas to go completely blank during screen navigation and role changes.

Requirements:
1. RCA of the blank canvas in src/layouts/MainLayout.vue during navigation / role switches. Check if the dynamic :key assigned to the component inside <router-view> and <keep-alive> triggers a TypeError (e.g. if route or route.fullPath is undefined).
2. Modify src/layouts/MainLayout.vue to safely use the route object injected from <router-view> slot scope (v-slot="{ Component, route }").
3. Implement a robust and defensive :key binding with optional chaining and fallback (e.g. route?.fullPath ? route.fullPath + '-' + authStore.activeRole : '').
4. Ensure 100% of Vitest tests pass and npm run build succeeds without modification of historic regression assertions (Ley Global 4).

Write your plans and progress reports inside your working directory (c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\frontend\.agents\orchestrator_canvas_blank/). Keep progress.md updated as you advance.
Report completion back when all acceptance criteria are met.
