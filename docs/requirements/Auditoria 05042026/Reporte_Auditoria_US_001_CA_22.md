# Reporte de Auditoría Forense: US-001 - CA-22
**Fecha:** 2026-05-06
**Auditor:** Agente Arquitecto Líder
**Protocolo:** Top-Down (Cero Búsqueda Semántica)

## 1. Objetivo de la Auditoría
Validar el cumplimiento del **CA-22** (Filtros Facetados para la Grilla del Workdesk) de la historia **US-001** (Motor Core). 
Este criterio dictamina la existencia de filtros facetados (por Tipo de Tarea `origin` y por Estado SLA `status`).
Los requerimientos estrictos son:
*   Filtros aplicados mostrados como "Chips" removibles.
*   Cálculos *server-side* que resetean la página actual a la primera (página 1 / index 0).
*   Enumeraciones precisas (`BPMN`, `Gantt`, `Kanban` | `Verde`, `Amarillo`, `Rojo`, `Negro`).
*   Retención de los parámetros de filtrado en la sesión para soportar navegaciones iterativas, apalancándose en la directiva `KeepAlive` de Vue.js.

## 2. Ruta de Navegación Estructural
Atendiendo al flujo estricto Top-Down sin indexación de búsqueda semántica:
1. Lectura del SSOT: `docs/requirements/epics/epic_A_motor_core.md`.
2. Inspección del componente base de despliegue: `frontend/src/views/Workdesk.vue`.
3. Verificación de los bindings bidireccionales y la capa de reactividad de los inputs filtrantes (`typeFilter`, `slaFilter`).
4. Verificación de los métodos de llamadas a red (`loadData`).

## 3. Hallazgos: Fugas de Sesión y Violación UX
Se detectaron las siguientes fallas durante la auditoría del ciclo de vida del UI:
*   **Acierto (Manejo Server-Side):** El método `loadData` invoca `fetchGlobalInbox(0, ...)` asegurando con éxito el paso 6 del criterio: resetear forzosamente a la página inicial, propagando `typeFilter` y `slaFilter` al servidor en lugar de ejecutarlos en el DOM.
*   **Brecha UX (Ausencia de Chips Visuales):** Contrario al CA-22, los filtros `origin` y `status` se han construido como `select` rígidos en la cabecera. La única implementación de Chips presente (`store.facets`) corresponde en realidad a una contaminación proveniente del CA-29 (Filtro por status con conteo), por lo que un usuario no puede "remover" su filtro de Tipo o SLA desde un chip en el canvas, forzándolo a volver al dropdown.
*   **Brecha de Enumeración:** Faltan valores en el frontend para soportar las tareas tipo `Gantt` definidas por el CA.
*   **Brecha Arquitectónica Front-End (Fuga de Sesión):** Al estar definidos como `const typeFilter = ref('');` sin suscribirse a la pila de estado de `Pinia`, a la URL (Query Router) o a un caché de Storage Local/KeepAlive superior, todo el filtrado se pierde al salir de la ruta `Workdesk` e ingresar nuevamente, violando frontalmente el dictamen "se preservarán en la sesión mediante KeepAlive (CA-12)".

## 4. Inyección de Trazabilidad
Para atar estos hallazgos a la remediación de la UI, se ha operado sobre `Workdesk.vue`:
*   Se inyectó `// @Traceability(US = "US-001", CA = {"CA-22"})` sobre el `select` correspondiente al "Filtro Tipo".
*   Se inyectó idéntica directriz `// @Traceability(US = "US-001", CA = {"CA-22"})` sobre el "Filtro Nivel de SLA".

## 5. Actualización de Deuda Técnica
Se ha redactado la alerta pertinente en `scaffolding/tasks/task.md`. Se orienta como resolución inmediata la reestructuración de los inputs hacia un componente de "Chips", integrando la memoria persistente de estado (vía `SessionStorage` o inyectando los parámetros reactivos en el propio Router Histórico de Vue).

**ESTADO DE LA AUDITORÍA:** COMPLETADA.
