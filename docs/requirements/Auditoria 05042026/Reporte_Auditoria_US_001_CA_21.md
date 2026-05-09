# Reporte de Auditoría Forense: US-001 - CA-21
**Fecha:** 2026-05-06
**Auditor:** Agente Arquitecto Líder
**Protocolo:** Top-Down (Cero Búsqueda Semántica)

## 1. Objetivo de la Auditoría
Validar el cumplimiento del **CA-21** (Definición del Skill-Based Routing y Skipeo Justificado) de la historia **US-001** (Motor Core). 
Este requerimiento define:
*   Algoritmo de asignación basado en habilidades (Skills) cruzadas contra la tarea más antigua.
*   Fallback Universal que asigne independientemente de los skills, pero emitiendo una auditoría de negocio obligatoria al log (`AuditLogService`).
*   Modal de skipeo justificado en la interfaz con campo libre (mínimo 10 caracteres) cuando se elige la opción "Otro".
*   Asiento inmutable por skipeo y una "Alerta de Supervisor" si ocurren >3 skips **consecutivos**.

## 2. Ruta de Navegación Estructural
Atendiendo al flujo estricto Top-Down sin indexación de búsqueda semántica:
1. Lectura del SSOT: `docs/requirements/epics/epic_A_motor_core.md`.
2. Inspección del front-end (UI de skipeo y motivos): `frontend/src/views/Workdesk.vue`.
3. Inspección del controlador web de asignación y enrutamiento atómico: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/WorkdeskAttendNextController.java`.

## 3. Hallazgos Estratégicos y Deuda Técnica
La validación demuestra que gran parte de la orquestación está escrita, sin embargo existen tres brechas críticas de seguridad/dominio de negocio en el Backend:

*   **Brecha de Validación / Vulnerabilidad (Mismatch de Enum):** El Frontend correctamente presenta el dropdown y pasa el valor `OTHER` hacia el servicio REST cuando se requiere especificar el motivo libre. No obstante, el Controlador Backend evalúa estáticamente `if ("OTRO".equalsIgnoreCase(skipReason.skipReason()))`. Al no coincidir las cadenas (`OTHER` != `OTRO`), la regla de validación de los 10 caracteres mínimos es ignorada por completo, permitiendo envíos vacíos.
*   **Brecha de Regla de Negocio (Alerta de Supervisor):** El requerimiento ordena auditar si un operario realiza 3 skips *consecutivos*. La implementación técnica (`taskSkipRepository.countRecentSkips(tenantId, currentUserId, since)`) realiza un simple conteo de todos los skips dentro de la última hora (independiente de si hubo tareas atendidas exitosamente entre ellos). Además, "Activar una alerta" fue implementado como un mero `log.warn`, lo cual carece de la formalidad de negocio requerida.
*   **Brecha de Trazabilidad (Audit Log ausente en Fallback):** Al activarse el Fallback Universal por falta de matching en los Skills, el requerimiento obliga a asentar el suceso en el `Audit Log` inmutable. Actualmente el backend solo inyecta una advertencia de consola (`log.warn`), omitiendo el acople funcional con el `AuditLogService`.
*   **Acierto Funcional (Frontend):** La construcción del modal (`showSkipModal`) y sus deshabilitaciones automáticas previniendo envíos dobles cumple con el estándar de UX.

## 4. Inyección de Trazabilidad
Se operaron los siguientes componentes:
*   `frontend/src/views/Workdesk.vue`: Inyección `@Traceability(US = "US-001", CA = {"CA-21"})` sobre el modal `<Transition>`.
*   `WorkdeskAttendNextController.java`: Inyección de la trazabilidad y comentarios de la deuda técnica sobre el Fallback, sobre la validación de `OTHER` vs `OTRO` y sobre el conteo generalizado de `countRecentSkips`.

## 5. Actualización de Deuda Técnica
La matriz de deuda técnica `scaffolding/tasks/task.md` ha sido actualizada resumiendo la falla de dominio (Mismatch de enumeración) y el cálculo erróneo de rachas consecutivas. 

**ESTADO DE LA AUDITORÍA:** COMPLETADA.
