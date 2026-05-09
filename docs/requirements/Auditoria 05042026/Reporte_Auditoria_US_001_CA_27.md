# Reporte de Auditoría Estricta: US-001 (CA-27)
## Vocabulario Completo de Acciones WebSocket

### 🗺️ Ruta Estructural Navegada (Top-Down)
1. `view_file: docs/requirements/epics/epic_A_motor_core.md` (Lectura comprensiva del criterio de aceptación CA-27 enfocado en el contrato de 4 acciones de WebSocket: REMOVE, ADD, UPDATE, PRIORITY_CHANGE).
2. `list_dir: backend/ibpms-core/src/main/java/com/ibpms/poc/application/dto/` (Exploración jerárquica de la capa DTO del backend).
3. `view_file: WsWorkdeskEventDTO.java` (Evaluación del contrato payload de eventos en el backend).
4. `list_dir: frontend/src/stores/` (Exploración de manejadores de estado globales en el frontend).
5. `view_file: useWorkdeskStore.ts` (Verificación de los listeners STOMP y las funciones de inyección y reordenamiento del Data Store).

### 🏷️ Archivos Etiquetados con Éxito (`@Traceability`)
*   `WsWorkdeskEventDTO.java`: Inyectada la etiqueta estandarizada `// @Traceability(US = "US-001", CA = {"CA-27"})` en la clase base del contrato DTO.
*   `useWorkdeskStore.ts`: Inyectada la etiqueta `// @Traceability(US = "US-001", CA = {"CA-27"})` sobre la línea de subscripción al WebSocket.

### 🚨 Brechas de Implementación y Deuda Técnica Detectada
Durante el análisis de la sincronización bidireccional mediante WebSocket se encontró una **Deuda Técnica de Implementación Funcional** en el Frontend:

1. **Violación de Ordenamiento (CA-01):** El criterio explícitamente exige que un evento de tipo `ADD` (nueva tarea asignada) debe incorporarse a la grilla respetando el ordenamiento de Acuerdos de Nivel de Servicio (SLA) y nivel de impacto. Sin embargo, la función `_handleWsAdd(payload)` en `useWorkdeskStore.ts` utiliza el método `.unshift()` crudo, forzando todas las tareas nuevas a la cima de la grilla independientemente de su urgencia matemática, rompiendo la experiencia operativa del agente.
2. **Contaminación del Contrato STOMP:** El Listener STOMP configurado en Vue implementa cláusulas de conmutación (switch cases) no soportadas o definidas por el criterio, tales como `TASK_UNCLAIMED`, `TASK_FORCE_UNCLAIMED`, y `TASKS_BULK_UPDATED`. Esto contamina el vocabulario de 4 primitivas de dominio definido en el requerimiento y promueve deuda técnica al mantener lógica espuria no gobernada.

**Plan de Remediación:**
La función de inyección `_handleWsAdd` debe refactorizarse para que ejecute una inserción binaria o un recálculo reactivo (re-sort) del array de tareas basado en la propiedad `slaExpirationDate` e `impactLevel`, coincidiendo exactamente con el algoritmo JPQL del backend.
Además, el switch case debe sanitizarse para re-mapear o descartar los eventos fuera del vocabulario.

El hallazgo se encuentra formalizado y sumariado en `task.md`.
