Perform a Root Cause Analysis (RCA) of the blank canvas issue in `src/layouts/MainLayout.vue` during screen navigation and role changes.
Verify whether the dynamic `:key` inside `<router-view>` and `<keep-alive>` can trigger a TypeError if `route` or `route.fullPath` is undefined.
Inspect `src/layouts/MainLayout.vue`, trace where the `route` object is coming from, and recommend a precise fix strategy without implementing it.
Read `c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\frontend\.agents\orchestrator_canvas_blank\SCOPE.md` for context.
Write your analysis to `analysis.md` in your working directory and then output a handoff report.
