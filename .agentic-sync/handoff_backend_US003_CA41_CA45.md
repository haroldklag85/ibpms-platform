# Contrato de Arquitectura Backend (US-003 Iteración 9: CA-41 al CA-45)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Adaptar los endpoints de contexto del formulario para inyectar variables persistidas previamente en el motor BPMN (Data Binding Front-to-Back).

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
En esta iteración nos centramos en las siguientes capacidades, omitiendo intencionalmente características diferidas a la V2 (como Multi-idioma).

Tu enfoque exclusivo de Backend es:
*   **CA-43 (Data Binding - Precarga Automática desde Camunda):** El frontend requiere que, al cargar el formulario asinado a una tarea de Camunda (`GET /api/v1/workbox/tasks/{id}/form-context`), el Backend devuelva no solo el esquema, sino también un bloque llamado `prefillData` (Map<String, Object>). Este bloque debe contener las variables **históricas y de ejecución activa** almacenadas en Camunda para la instancia de proceso subyacente de la que deriva la tarea. 

## 📐 Reglas de Desarrollo:
1. Invoca el `TaskService` y `RuntimeService` (o `HistoryService` si es retroactivo) de Camunda para extraer el mapa de `ProcessVariables` asociadas a la Instancia de Proceso o Tarea.
2. Adjunta dichas variables dentro del mega-DTO (`FormContextDTO` o similar) como `prefillData`.
3. Evita alterar otros controladores no relacionados.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a la rama principal.
En cuanto finalices la integración en el endpoint y verifiques unitariamente, congela los cambios localmente:
`git stash save "temp-backend-US003-ca41-ca45"`

No cierres el chat ni asumas roles de orquestación. Solo notifica: "Stash Backend Terminado".
