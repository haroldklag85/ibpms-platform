# Handoff: AI DEVELOPER AGENT - FRONTEND
**Iteración:** 64-DEV (US-001 / CA-16 al CA-18)
**Contexto de Memoria Aislada:** Vue 3 / Pinia / TypeScript. NO conoces DB profunda ni código Java.

## 1. MISIÓN Y REGLA DE ORO V2
Integrar la UI y consumir la nueva API para cerrar lado cliente los CA-16 al CA-18 (US-001). 
**REGLA DE ORO V2:** Ignora todo desarrollo que apunte a funcionalidades futuras (ej. Dashboards históricos, paneles analíticos avanzados). Focalízate en la V1 core.

## 2. NOTAS DE IMPLEMENTACIÓN TÉCNICA (TIN)
1. **Global Heartbeat (CA-11):** ESTRICTAMENTE PROHIBIDO usar `setInterval` en los `<TaskCard>`. Debes referenciar un `useTimeStore` que maneje `requestAnimationFrame` exportando `Date.now()` reactivo. Los cards calculan con un `computed`.
2. **Full-Text Search Frontend (CA-10):** El `@input` de la búsqueda obligatoriamente debe estar envuelto en un lodash `debounce` o `useDebounceFn` (VueUse) de 300ms.
3. **UX Transitions & KeepAlive (CA-11/13):** En `MainLayout`, envuelve el `router-view` en `<keep-alive include="WorkdeskView">`. Usa `<transition-group name="list">` con `.list-leave-to { opacity: 0; transform: translateX(30px); }`.
4. **WebSocket Throttling:** Valida la sesión JWT en CONNECT. Payloads atómicos obligatorios (Ej. `{ type: 'REMOVE', taskId: 'uuid' }`). Jamás enviar JSON gigantesco. Usa Throttler si caen masivos.
5. **Iconografía Nativa (FCP):** Prohibido `<img src="...">` tradicional. Usa inline SVG (ej. Lucide/Phosphor) para resguardar First Contentful Paint.

## 3. ENTREGABLE ESTRICTO
Consolidar el DOM, validar reactividad y guardar cambios en un stash aislado usando:
`git stash save "temp-frontend-US001-CA16-18"`
Notifica al humano cuando termines.
