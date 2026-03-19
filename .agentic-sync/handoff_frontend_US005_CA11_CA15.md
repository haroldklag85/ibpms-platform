# Contrato de Arquitectura Frontend (Iteración 18 | US-005: CA-13 al CA-15)

**Rol:** Desarrollador Frontend Vue 3 / UI-UX Engineering.
**Objetivo:** Construir el "Centro de Incidentes" (SysAdmin) y el "Historial de Rollbacks" (Arquitecto BPMN).

## 📋 Contexto y Órdenes Directas (Implementación Estricta):

Esta fase aborda la resiliencia y recuperación de desastres (DRP) en el gobierno de procesos.

### Tarea 1: La Morgue de Tokens (Centro de Incidentes - CA-13)
*   **Funcionalidad:** Crea una nueva Vista/Ruta de administrador llamada `IncidentCenter.vue`.
*   **UI:** Debe mostrar una tabla de datos (Tokens Rotos). Consulta el endpoint (mockeado temporalmente si falta Back) `GET /api/v1/admin/incidents`.
*   **Acciones Tácticas:** Por cada fila, provee dos botones de soporte Nivel 3:
    1. `[🔄 Retry (Electrochoque)]` -> `POST /api/v1/admin/incidents/{id}/retry`
    2. `[💀 Abortar Caso]` -> `DELETE /api/v1/admin/incidents/{id}`

### Tarea 2: Historial de Versiones y Rollback Un Clic (CA-15)
*   **Funcionalidad:** Dentro de las vistas que ya has armado (`BpmnDesigner.vue` o similares), crea un panel o pestaña lateral denominada **"Historial de Versiones"**.
*   **UI:** Lista las versiones previas de un Proceso (Ej: v1, v2, v3) iterando un `GET /api/v1/design/processes/{key}/versions`.
*   **Acción de Recuperación:** Acompaña cada versión histórica con un botón `[Restaurar esta versión]`. Al hundirlo, invocarás `POST /api/v1/design/processes/{key}/rollback/{versionId}`.
*   **Inmutable:** El UI debe dejar claro que no se *reemplaza* la versión, sino que se copia como la V_NUEVA.

*(Nota: CA-11 y CA-14 corresponden a la visualización en el DOM del Workdesk de Negocios US-001 y Pantalla 17 Visión 360, no aplican fuertemente a las vistas de diseño de esta iteración. Abstente de sobre-ingeniar).*

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` directamente a principal. 
Construye las UIs solicitadas en Vue, conéctalas vía Axios (apiClient.ts) y congela todo:
`git stash save "temp-frontend-US005-ca13-ca15"`

Informa textualmente la confirmación del guardado.
