# Handoff Backend - Iteración 42 (US-043: CA-1 a CA-6)

## Propósito
Modificar la percepción temporal de Camunda inyectando un `BusinessCalendar` personalizado. Esto permitirá que los SLAs (Due Dates y Timers) pausen su reloj en fines de semana y horarios no hábiles, aplicando lógicas asíncronas para cálculos retroactivos y husos horarios dinámicos.

## Criterios de Aceptación Cubiertos (Backend)
* **CA-1 (Custom BusinessCalendar):** Proveer e inyectar un Custom `BusinessCalendar` en el Engine Configuration de Camunda (`ProcessEngineConfigurationImpl`). Este componente interceptará el cálculo de los `Due Dates` de las `UserTasks`, sumando el tiempo parametrizado exclusivamente durante horas hábiles (Ej: Lunes a Viernes, 8 a 17h) leídas desde la base de datos de configuración.
* **CA-2 (Exención Sistémica):** El `BusinessCalendar` debe leer la propiedad de extensión BPMN (Ej: `camunda:property name="isBusinessSla" value="false"`). Si es un timer de integración o MLOps `false`, el cálculo volverá al estándar UTC 24/7 sin pausas.
* **CA-3 (Anti-Deadlocks Retroactivos):** Crear el Endpoint de actualización de SLA. Si el payload marca `aplicarRetroactivamente = true`, el Backend **TIENE PROHIBIDO** hacer `UPDATE` masivo síncrono. Debe encolar un proceso asíncrono `@Async` (Batch Job) que itere los trabajos activos en `ACT_RU_JOB` actualizándolos de a lotes, devolviendo al Frontend un HTTP 202 Accepted.
* **CA-4 (Husos Horarios Híbridos):** El Custom Calendar debe ponderar la Zona Horaria del Asociado (`Assignee` u homólogo) para aplicar las horas hábiles correctas (Ej: UTC-5 vs UTC+1).
* **CA-5 (API Feriados & Fallback):** Construir los Endpoints CRUD para la `SlaHolidayEntity`, permitiendo al Frontend inyectar feriados fijos manualmente.
* *(Nota: CA-6 "Alertas Tempranas" se abstraerá a un evento interno genérico si el motor de notificaciones US-049 está diferido a V2).*

## Tareas Java (Prioridad 1 - Tarea Bloqueante)
1. Estructurar las tablas CRUD para Horario Comercial (`BusinessHoursEntity`) y Feriados (`HolidayEntity`).
2. Codificar la clase `CustomBusinessCalendar` e inyectarla en Camunda.
3. Crear el Job Asíncrono de Re-cálculo masivo (CA-3).
