# Reporte de Auditoría Forense: US-001 - CA-23
**Fecha:** 2026-05-06
**Auditor:** Agente Arquitecto Líder
**Protocolo:** Top-Down (Cero Búsqueda Semántica)

## 1. Objetivo de la Auditoría
Validar el cumplimiento del **CA-23** (Fórmula Determinista para la Columna "Avance") de la historia **US-001** (Motor Core). 
Este criterio dictamina que la columna de Avance del Workdesk debe mostrar una barra de progreso que refleje el avance posicional de la tarea basado en su motor de origen:
*   **BPMN:** `(Índice ordinal de la UserTask actual) / (Total de UserTasks del proceso) × 100`
*   **Kanban:** `(Índice ordinal de la columna actual) / (Total de columnas) × 100`
Si el cálculo no es viable, debe degradarse grácilmente mostrando `N/D`.

## 2. Ruta de Navegación Estructural
Atendiendo al flujo estricto Top-Down sin indexación de búsqueda semántica:
1. Lectura del SSOT: `docs/requirements/epics/epic_A_motor_core.md`.
2. Búsqueda de la definición de Proyección (CQRS): `WorkdeskProjectionEntity.java`.
3. Búsqueda de los responsables de la persistencia: `CamundaTaskSyncListener.java` y `KanbanTaskSyncListener.java`.
4. Búsqueda de la representación visual: `frontend/src/views/Workdesk.vue`.

## 3. Hallazgos: Omisión Estructural de Lógica de Progreso
Se detectaron las siguientes brechas de implementación:
*   **Acierto (Degradación Front-end):** En `Workdesk.vue`, la directiva `v-if="task.progressPercent != null"` renderiza correctamente la barra de progreso diseñada, y en caso de nulidad hace *fallback* correctamente a `N/D`, cumpliendo la sección 5 del CA.
*   **Brecha Funcional Crítica (Backend):** Ambos sincronizadores del CQRS (`CamundaTaskSyncListener` y `KanbanTaskSyncListener`) tienen la responsabilidad exclusiva de mapear los eventos crudos hacia el Inbox Global. Sin embargo, **ninguno extrae, calcula ni mapea** el campo `progressPercent` en la entidad `WorkdeskProjectionEntity`. 
    * En Camunda, no se intercepta el BpmnModelInstance para contar las tareas de usuario.
    * En Kanban, no se inyecta el índice de las columnas ni su contexto total.
    * En consecuencia, la base de datos registra perpetuamente valores `null` en `progress_percent`, lo que ocasiona que el usuario final nunca vea la barra de progreso, sino únicamente el texto degradado "N/D".

## 4. Inyección de Trazabilidad
Para visibilizar y atar la corrección al ciclo de calidad:
*   **Backend (`CamundaTaskSyncListener.java`):** Se inyectó `// @Traceability(US = "US-001", CA = {"CA-23"})` indicando el *TODO* para calcular el `progressPercent`.
*   **Backend (`KanbanTaskSyncListener.java`):** Se inyectó `// @Traceability(US = "US-001", CA = {"CA-23"})` para el mismo propósito.
*   **Frontend (`Workdesk.vue`):** Se inyectó `// @Traceability(US = "US-001", CA = {"CA-23"})` encima de la etiqueta `<td>` que renderiza la columna de avance.

## 5. Actualización de Deuda Técnica
El hallazgo arquitectónico fue registrado en `scaffolding/tasks/task.md`. Se recomienda que los EventListeners (o en su defecto un *Projection Builder Service* adicional) inyecten dependencias hacia los servicios nativos (Repository API en Camunda o Board Service en Kanban) para resolver las fracciones ordinales en tiempo de guardado.

**ESTADO DE LA AUDITORÍA:** COMPLETADA.
