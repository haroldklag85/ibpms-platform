# 📋 Solicitud de Revisión Arquitectónica - US-036 (Identity Governance Frontend)

**Para:** Arquitecto Líder
**De:** Agente Frontend (sprint-6/uat-certification)

## Resumen del Plan Propuesto
He elaborado el plan de implementación para abarcar los requerimientos funcionales CA-26 al CA-32 relacionados con la Gobernanza de Topología Visual (US-036), cumpliendo con el protocolo Zero-Mock y la arquitectura Zero-Trust.

Los cambios estructurales a ejecutar serán:
1. **`useMenuStore.ts` (CA-31)**: Se conectará la hidratación del layout al endpoint de producción `GET /api/v1/users/me/menu-layout`, prescindiendo de los JWT Claims. Se implementará `purgeTopology()` como mecanismo de limpieza.
2. **`apiClient.ts` (CA-32)**: Se extenderá el interceptor Axios. Ante la detección de un código 403 (Forbidden), se invocará inmediatamente `menuStore.purgeTopology()` para lograr la auto-curación del estado de Pinia, asegurando que el cliente elimine el menú obsoleto que le ha sido revocado.
3. **`IdentityGovernance.vue` (CA-27, CA-28, CA-29)**: Se aplicará una refactorización al modal *Role Factory*, subdividiéndolo en pestañas (Tabs): "Información Básica" y "Topología Visual". En el de topología, se habilitarán interruptores (switches) por macro-módulo. Por inmutabilidad, estos se deshabilitarán automáticamente si se detecta la edición de un rol fundacional (`SUPER_ADMIN`, `SYSTEM_ADMIN`).
4. **`MainLayout.vue` (CA-26)**: Si `useMenuStore.layout` resuelve en un array vacío tras el `fetch` (significando que el usuario carece de menús), en lugar de renderizar la aplicación colapsada, se desplegará una pantalla/dashboard neutral "Página de Bienvenida" para no dejar al usuario bloqueado.

Finalmente, generaré las pruebas unitarias (Vitest) para `useMenuStore.spec.ts` y el test del interceptor (403 auto-healing). Todo se hará bajo TDD (Red -> Green -> Refactor).

Solicito el Veredicto Oficial para proceder a Modo **EXECUTION**.
