# Contrato de Arquitectura Frontend (Iteración 24 | US-005: CA-41, CA-42, CA-44, CA-45)

**Rol:** Desarrollador Frontend Vue 3 / UI-UX Engineering.
**Objetivo:** Consolidar la vista empresarial con Logs de auditoría, soporte completo para carriles inter-empresariales (Colaboración) e inyección de Conectores API en Service Tasks.

## 📋 Contexto y Órdenes de Implementación:

### Tarea 1: Multi-Pool / Colaboración Visual (CA-44)
*   En tu `CustomPaletteProvider` creado en la Fase 20, debes añadir obligatoriamente las herramientas nativas: `create.participant` (Pools/Carriles principales) y `connect.message-flow` (Flujos de mensaje). Permite que el analista dibuje cajas negras externas.

### Tarea 2: Service Task a Conectores Hub (CA-45)
*   Simula la conexión al "Hub de Integraciones". Al seleccionar una `ServiceTask` en el Lienzo, el Panel de Propiedades debe exhibir un Dropdown (igual que hicimos con los UnitTasks y Forms).
*   El Dropdown consultará `GET /api/v1/integrations/connectors`. Sus opciones mapearan un valor XML extendido (Ej: `<camunda:connector>o365_mail</camunda:connector>` o `<camunda:delegateExpression>${o365MailAdapter}</camunda:delegateExpression>`).

### Tarea 3: Panel de Auditoría / Git Log Frontal (CA-42)
*   Añade una pestaña inferior o lateral "📜 Historial de Cambios".
*   Invocará a `GET /api/v1/design/processes/{key}/audit-logs` renderizando una tabla sencilla (Acción, Usuario, Fecha, Snapshot Version).

### Tarea 4: Consolidación del Sandbox Físico (CA-41)
*   Modifica el botón Sandbox (Creado en la iteración 19) para que pase de ser un simulador front visual, a un disparador de la API dura: `POST /api/v1/design/processes/sandbox-spawn`. El frontend solo esperará un Toast 200 de "Ejecución simulada sin errores".

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` directamente a principal.
Congela tu código en stash:
`git stash save "temp-frontend-US005-ca41-ca45"`

Informa textualmente la confirmación del guardado.
