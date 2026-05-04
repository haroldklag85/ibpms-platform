# Solicitud de Aprobación - Plan Frontend (US-051 CA-06 a CA-10)

**Para:** Arquitecto Líder
**De:** Agente Frontend (DevDavid)

He completado el análisis y elaborado el plan de implementación detallado para cubrir los requerimientos funcionales CA-06, CA-07, CA-08, CA-09 y CA-10 asociados a la US-051.

**Resumen del Plan:**
1. **CA-06 (Auto-Collapse Sidebar):** Se modificará `useMenuStore.ts` y `MainLayout.vue` para que evalúen los roles y oculten los grupos (acordeones) cuando no contengan elementos hijos autorizados.
2. **CA-07 (Composición de Workdesk):** Confirmado y adaptado en `Workdesk.vue` vía la lógica dinámica de `<component :is="Comp">`.
3. **CA-08 (Ocultar botones destructivos):** Se introducirá `hasWritePermission` en `authStore.ts` y se aplicará `v-if` a todos los botones que alteren estado (ej. "Revocar Todo", "Crear Nuevo Usuario", etc.) en `IdentityGovernance.vue`.
4. **CA-09 (Interceptor Sudo-Mode):** Se modificará `apiClient.ts` para capturar asíncronamente las peticiones POST/DELETE mediante una promesa suspendida. Invoca el modal centralizado `SudoModal.vue` y, tras validar con éxito, retoma la transmisión de red agregando la cabecera `X-Sudo-Token`.
5. **CA-10 (Telemetría de API Keys):** En `IdentityGovernance.vue`, la `Secret Key` aparecerá ofuscada inicialmente. Al pulsar "Mostrar", se dispara una llamada silenciosa al backend (`/forensics/iso-override`) como auditoría forense y, al resolver, remueve el ofuscamiento visual.

El plan no requiere añadir librerías y respeta las normativas C4 y TDD documentadas.

Solicito su **Aprobación Formal** para pasar al estado `EXECUTION` y consolidar el código.
