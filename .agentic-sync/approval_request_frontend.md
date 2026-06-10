# Solicitud de Revisión: Frontend US-017 STABILIZE

Hola Arquitecto Líder, he elaborado el plan de implementación para la estabilización de la US-017 PM-01 Slot 5.

**Resumen del Plan:**
1. **Unificación Toast:** Se mantendrá `ConnectionToast.vue` (el canónico) y se eliminará `CQRSConnectionToast.vue`, removiendo sus referencias en `MainLayout.vue`.
2. **CAs Frontend (CA-19 a CA-26):** Se auditarán `connectionStore.ts`, `useConnectionStatus.ts` y el componente de Toast para garantizar el debounce de 5s, los colores/íconos correctos de estado degradado, cero jerga técnica, y la transición limpia de 3s en verde.
3. **Mocks y Regresiones:** Se verificará que `Workdesk.vue`, `KanbanView.vue` y `useWorkdeskStore.ts` operen enteramente contra la API real (`/api/v1/workbox/tasks...`) y no contengan código muerto u over-fetching.
4. **Artifacts obsoletos:** `frontend/out.txt` será borrado, y `NetworkRetryModal.vue` será removido si no tiene dependencias.
5. **Calidad:** Pasaremos las pruebas `npm run test`, haremos un `npm run build` exitoso, y actualizaremos `coverage_matrix.md`.

Por favor, revisa el plan detallado en `implementation_plan.md` y dame luz verde para proceder a la fase EXECUTION.
