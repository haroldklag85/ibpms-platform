# Contrato de Arquitectura Frontend (Iteración 22 | US-005: CA-31 a CA-35)

**Rol:** Desarrollador Frontend Vue 3 / UI-UX Engineering.
**Objetivo:** Implementar la reactividad defensiva del diseñador, la visualización de estados del ciclo de vida y la captura de SLA a nivel de nodo.

## 📋 Contexto y Órdenes de Implementación:

### Tarea 1: Etiquetas Visuales y Acción de Archivo (CA-31, CA-32)
*   Agrega Badges visuales en el Explorador Lateral de procesos: `Borrador`, `Activo_V[X]`, y `Archivado`.
*   Añade el botón `[📦 Archivar]` a los procesos `Activos`. Al clic, dispara el endpoint `POST /api/v1/design/processes/{key}/archive`. Captura y muestra un error si el backend responde HTTP 409 (Instances Running).

### Tarea 2: Invalidación Defensiva del Pre-Flight (CA-33)
*   Inyecta un listener al evento de edición del lienzo: `modeler.on('commandStack.changed', ...)`.
*   Al detectarse que el XML fue tocado (agregaron o borraron nodos), si el estado de Pre-Flight era "Éxito" (y el botón `[🚀 DESPLEGAR]` estaba iluminado), debes resetear la bandera a `PENDIENTE` y volver a apagar o grisar el botón de despliegue, obligando a re-validar antes de mandar a producción la nueva alteración.

### Tarea 3: Solicitud de Despliegue al Gestor de Release (CA-34)
*   Si el usuario tiene rol `BPMN_Designer` (Simulado en Front), en vez de "Desplegar", el botón debe decir `[📩 Solicitar Despliegue]`.
*   Al presionarlo, después de que el Pre-Flight sea exitoso, llama a `POST /api/v1/design/processes/{key}/request-deploy`. 

### Tarea 4: Atributo Custom SLA en Tareas (CA-35)
*   En el panel derecho de Modeler (Properties Panel), añade un input nativo o simulado llamado **SLA Objetivo (Ej: PT48H)**.
*   Enlaza este valor modificando el XML subyacente de la UserTask o ServiceTask bajo la propiedad extendida de Camunda: `<camunda:property name="SLA" value="PT48H" />`.

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` directamente a principal.
Lanza tu código al congelador en stash:
`git stash save "temp-frontend-US005-ca31-ca35"`

Informa textualmente la confirmación del guardado.
