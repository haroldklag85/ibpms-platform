# Reporte de Auditoría Forense: US-001 - CA-20
**Fecha:** 2026-05-06
**Auditor:** Agente Arquitecto Líder
**Protocolo:** Top-Down (Cero Búsqueda Semántica)

## 1. Objetivo de la Auditoría
Validar el cumplimiento del **CA-20** (Contrato API Estandarizado para la Grilla del Workdesk) de la historia **US-001**. 
Este requerimiento define explícitamente el contrato arquitectónico de interacción Frontend-Backend:
*   URI unificada: `GET /api/v1/workdesk/tasks` (con endpoint delegado `/tasks/{userId}`).
*   Documentación estricta con anotaciones OpenAPI/Swagger.
*   Query params: `page`, `size` (máx 100), `search`, `origin`, `status`, `sort`.
*   Formato de Respuesta Sanitizado: Wrapper estricto en el formato `{ data: [...], pagination: { ... } }`.

## 2. Ruta de Navegación Estructural
Atendiendo al flujo estricto Top-Down sin indexación de búsqueda semántica:
1. Lectura del SSOT: `docs/requirements/epics/epic_A_motor_core.md`.
2. Inspección del manejador de estado en Frontend: `frontend/src/stores/useWorkdeskStore.ts`.
3. Verificación de la capa Controller del Backend: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/WorkdeskQueryController.java`.
4. Análisis cruzado de los Data Transfer Objects (DTOs): `WorkdeskResponseDTO` y `WorkdeskGlobalItemDTO`.

## 3. Hallazgos Estratégicos y Deuda Arquitectónica
La validación demuestra que el endpoint fue desarrollado, pero ignora masivamente los estándares dictados por el contrato, incurriendo en múltiples infracciones de dominio y diseño API:

*   **Brecha de Enrutamiento REST:** La URI construida es `/api/v1/workdesk/global-inbox` en lugar de `/api/v1/workdesk/tasks`. Para el modelo delegado, en lugar de un *Path Variable* `/tasks/{userId}`, se usa un frágil query param opcional `?delegatedUserId=`.
*   **Brecha Estructural del Wrapper (DTO Mismatch):** El Backend retorna directamente el objeto `WorkdeskResponseDTO` que envuelve un objeto `Page` plano propio de Spring Data (`{ degraded: false, content: { content: [], totalElements: ... } }`). Esto anula por completo el wrapper canónico `{ data, pagination }` exigido. Adicionalmente, el DTO de los ítems (`WorkdeskGlobalItemDTO`) retiene el mapeo interno de Java (`title`, `slaExpirationDate`) en vez de serializar a la estructura estandarizada (`name`, `sla_deadline`).
*   **Amnesia de Parámetros:** El Controlador Java omite completamente definir `@RequestParam` para capturar `origin` (typeFilter) y `status` (statusFilter). Esto ocasiona que el Backend ignore por completo el filtrado facetado que el usuario intente aplicar desde la UI.
*   **Ausencia de Documentación Viva:** Carencia total de anotaciones `@Operation` o `@ApiResponse` requeridas para Swagger/OpenAPI.
*   **Degradación del HTTP 400:** El CA-10 estipuló que exceder el Hard Limit de 100 registros debe emitir un `HTTP 400 Bad Request`. El backend simplemente lanza un `IllegalArgumentException` sin capturarlo semánticamente, lo que genera un `HTTP 500 Internal Server Error`.

## 4. Inyección de Trazabilidad
Se operó el Controller core:
*   `WorkdeskQueryController.java`: Inyección de comentario forense y anotación `@Traceability(US = "US-001", CA = {"CA-20"})` en la firma del endpoint principal `@GetMapping("/global-inbox")`.

## 5. Actualización de Deuda Técnica
La matriz de seguimiento `scaffolding/tasks/task.md` fue alimentada con esta severa Brecha Arquitectónica para priorizar su refactorización (alineación de URIs, inyección de Query Params, y mapeos customizados Jackson JSON) previo a cualquier paso a certificación funcional.

**ESTADO DE LA AUDITORÍA:** COMPLETADA.
