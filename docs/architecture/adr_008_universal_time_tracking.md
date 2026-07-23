# ADR-007: Arquitectura Transversal del Módulo SLA y Time-Tracking

**Status:** Aprobado
**Date:** 2026-03-07
**Context:** po_instruction_sla_timer_reusability.md (Unificación de Métricas Horas-Hombre)
**Autor:** Lead Software Architect

## 1. Contexto y Problema
El Product Owner ha levantado una alerta estratégica fundamental: El iBPMS posee 3 motores de ejecución de trabajo distintos:
1.  **Camunda BPMN:** Tareas estructuradas.
2.  **Kanban Ágil:** Tarjetas volátiles (ADR-007).
3.  **Planner Gantt:** Nodos de WBS con precedencias.

Si cada módulo construye su propia tabla de base de datos para registrar las horas trabajadas (`billable hours`) o su propio reloj de SLA, el sistema de Inteligencia de Negocios (BAM) será incapaz de consolidar el costo real de un empleado a fin de mes. Se requiere un diseño que cumpla el principio DRY (Don't Repeat Yourself) llevado a nivel de micro-arquitectura.

## 2. Decisión Arquitectónica: Patrón de Componente Polimórfico (Hexagonal)
Se decreta la creación de un Subdominio transversal denominado **`TimeTracking_Context`**, completamente agnóstico al tipo de tarea que lo invoca.

### A. Diseño de Base de Datos (Backend - Polimorfismo)
El Backend (Spring Boot) no creará llaves foráneas duras atadas a `camunda_task_id` o `kanban_card_id`. En su lugar, utilizará el **Patrón de Asociación Polimórfica**:

Se creará una tabla central unificada `ibpms_time_logs`:
*   `id` (UUID, PK)
*   `user_id` (UUID) -> Operario que sudó la hora.
*   `reference_id` (String/UUID) -> ID abstracto de la tarea.
*   **`reference_type` (Enum)** -> Dictamina el origen: `[TASK_BPMN, TASK_AGILE, TASK_GANTT]`.
*   `duration_minutes` (Int) -> Esfuerzo neto incurrido.
*   `log_timestamp` (DateTime) 

**Ventaja Hexagonal:** Cuando el módulo financiero consulte "Cuántas horas trabajó Pedro hoy", hará un humilde `SELECT SUM(duration_minutes) WHERE user_id = X`, sin importarle si Pedro arregló un bug (Ágil) o tramitó un contrato (BPMN).

### B. Consumo de API (Driving Ports)
El módulo expondrá un único puerto REST (Controller):
*   `POST /api/v1/time-tracking/logs`
    *   Payload: `{ "referenceId": "...", "referenceType": "...", "minutes": 60 }`
Ningún otro módulo tiene permiso de escribir tiempos en base de datos. Todos deben consumir este puerto interno.

### C. Diseño de Interfaz de Usuario (Frontend Vue 3 - Mixin/Componente)
El equipo Frontend desarrollará un único componente atómico llamado `<UniversalSlaTimer />`.
Este componente recibirá estrictamente 3 *Props* (parámetros de entrada):
1.  `referenceId`
2.  `referenceType`
3.  `targetSLAEnd` (Fecha límite calculada previamente)

**Inyección Diagnóstica:** Este mismo `<UniversalSlaTimer>` será importado e incrustado en la Pantalla 1 (Bandeja BPMN), en la Pantalla 3 (Tarjetas Kanban) y en la Pantalla 10.B (Modal de Gantt). El componente se encargará internamente de pintarse de verde/rojo y gestionar el botón de `Start/Stop` disparando las peticiones Axios al puerto central. 

## 3. Consecuencias y Reglas
*   **Prohibición de "Silos de Tiempo":** Queda estrictamente prohibido que la tabla de tareas de Kanban o la extensión de Camunda guarden columnas como `horas_invertidas`. Solo el esquema central de `TimeTracking_Context` es el dueño de esta data.
*   **Impacto Front-End:** El componente timer debe ser "tonto" (Dumb Component). No debe saber qué es Camunda ni qué es un Kanban. Solo debe saber cómo contar tiempo y llamar a una API pasándole un String y un Enum.
