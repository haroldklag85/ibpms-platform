# Reporte de Auditoría Forense: US-001 - CA-25
**Fecha:** 2026-05-06
**Auditor:** Agente Arquitecto Líder
**Protocolo:** Top-Down (Cero Búsqueda Semántica)

## 1. Objetivo de la Auditoría
Validar el cumplimiento del **CA-25** (Recálculo de Semáforos al Volver de Pestaña Inactiva) de la historia **US-001** (Motor Core). 
Este criterio dictamina que al detectar el evento `visibilitychange` (la pestaña vuelve a estar activa), el Heartbeat Store ejecutará un recálculo INMEDIATO de los semáforos y, si la inactividad supera los 5 minutos, disparará el auto-refresco del CA-31.

## 2. Ruta de Navegación Estructural
Siguiendo las normativas de cero-alucinación y evasión de grep semántico:
1. Lectura del índice de requisitos en `docs/requirements/epics/epic_A_motor_core.md`.
2. Listado y ubicación manual de la capa de persistencia volátil (`frontend/src/stores/`).
3. Inspección del archivo gestor del tiempo: `timeStore.ts`.
4. Inspección del orquestador visual principal: `Workdesk.vue`.

## 3. Hallazgos: DOM Event Listener Race Condition
Durante la revisión se halló un defecto de implementación sutil pero severo: **Condición de Carrera en Eventos DOM (Race Condition)**.
*   **Acierto Reactivo:** El recálculo de la marca de tiempo global se efectúa correctamente en el `timeStore.ts` actualizando la referencia de Vue `currentTick`, lo que desencadena un recálculo instantáneo en los "SLA Pills" de la UI.
*   **Brecha Funcional:** El mecanismo de autorefresco del CA-31 (superando los 5 mins de inactividad) fue saboteado involuntariamente. 
    *   `timeStore.startEngine()` añade su listener del evento `visibilitychange` de primero.
    *   Posteriormente, `Workdesk.vue` añade su propio listener para disparar el refresco CA-31.
    *   Al retornar la visibilidad, el listener de `timeStore` se ejecuta primero, restableciendo `lastUpdateTime = Date.now()`.
    *   Inmediatamente después se ejecuta el listener de `Workdesk.vue`, evaluando `timeStore.getInactivityMs()`. Puesto que el store acaba de resetear la variable un milisegundo antes, la diferencia temporal siempre es `~0`. 
    *   Por consecuencia, **la condición `inactividad > 300,000ms` nunca se cumple y el inbox jamás se refresca automáticamente tras largas ausencias.**

## 4. Inyección de Trazabilidad
Se procedió con la inyección de los marcadores de la matriz de cobertura ISO 27001 para que QA pueda forzar su remediación:
*   **En `timeStore.ts`:** Se inyectó `// @Traceability(US = "US-001", CA = {"CA-25"})` encima del método `_onVisibilityChange`.
*   **En `Workdesk.vue`:** Se actualizó la etiqueta existente para referenciar ambos CAs acoplados `// @Traceability(US = "US-001", CA = {"CA-25", "CA-31"})` encima de la función `onVisibilityReturn`.

## 5. Actualización de Deuda Técnica
La brecha ha sido tipificada y agregada al documento maestro `scaffolding/tasks/task.md`. Se recomienda su remediación extrayendo la orquestación del evento de reactivación hacia un bus de eventos central, o permitiendo que `timeStore.ts` mantenga una variable separada para el cálculo de inactividad o que emita el trigger hacia la vista.

**ESTADO DE LA AUDITORÍA:** COMPLETADA.
