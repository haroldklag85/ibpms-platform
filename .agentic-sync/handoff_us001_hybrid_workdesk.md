# Handoff Architecture - US-001: Workdesk Híbrido (CQRS)

**Para:** Equipo de Desarrollo (Backend & Frontend Agents)
**Prioridad:** CRÍTICA (Resolución de Brechas UAT)
**Origen:** `gap_analysis_us001.md`

Este documento formaliza la reingeniería necesaria para cumplir con la US-001, alineándose estrictamente a la política de **Vertical Slice Development**, ADR-001 (Hexagonal), ADR-008 (Kanban en JPA) y NFR-PER-01 (Latencia < 800ms).

---

## 1. Capa de Base de Datos (DB Migration)
Para aislar la carga de consultas y permitir el cruce de tareas Camunda y Kanban respetando el SLA, construiremos un modelo de lectura (Query Model) mediante CQRS.

**Acción:** Crear script Liquibase (`create-workdesk-projection.sql`)
- **Tabla:** `ibpms_workdesk_projection`
- **Columnas Requeridas:**
  - `id` (VARCHAR PK - Hash del ID original + Sistema)
  - `source_system` (VARCHAR: 'BPMN' o 'KANBAN')
  - `original_task_id` (VARCHAR - ID de Camunda o JPA)
  - `title` (VARCHAR)
  - `assignee` (VARCHAR - nullable)
  - `candidate_group` (VARCHAR - nullable)
  - `sla_expiration_date` (TIMESTAMP - INDEXADO OBLIGATORIAMENTE)
  - `status` (VARCHAR)
  - `payload_metadata` (JSONB - Para heredar variables extra)

## 2. Capa de Dominio / Infraestructura (Domain Logic & Sync)
La tabla de proyección debe poblarse de forma reactiva (Eventual Consistency).

**Acciones Backend:**
- **Camunda Sync:** Crear un `org.camunda.bpm.engine.delegate.TaskListener` global (registrado en BpmnParseListener) que intercepte eventos `create`, `update`, `assignment`, `complete`. Al dispararse, debe instanciar un adapter que haga `UPSERT` en `ibpms_workdesk_projection`.
- **Kanban Sync:** Enganchar un `@EntityListeners` en la Entidad principal de Tareas Kanban (`KanbanTaskEntity.java`). En los eventos `@PostPersist` y `@PostUpdate`, replicar los cambios hacia la tabla de proyección.
- **Importante (ADR-001):** La lógica del CQRS Query Model (Repositorio de lectura) vivirá en la capa `application` y usará una entidad de solo-lectura en la capa de `infrastructure/jpa`. No tocar el `domain` core.

## 3. Capa de API y Contrato (Backend API Layer)
Exponer el nuevo punto de entrada unificado para la UI.

**Acciones Backend:**
- **Controlador:** Crear `WorkdeskQueryController.java` (`@RestController`).
- **Endpoint:** `GET /api/v1/workdesk/global-inbox`
- **Paginación:** Aceptar parámetros `?page=0&size=50&sort=slaExpirationDate,asc` (Implementar `Pageable` nativo de Spring Data).
- **Contrato DTO (API-First):**
```json
// Respuesta esperada: List<WorkdeskGlobalItemDTO>
{
  "content": [
    {
      "unifiedId": "BPMN-9a8b7c",
      "sourceSystem": "BPMN",
      "originalTaskId": "9a8b7c",
      "title": "Aprobación Legal",
      "slaExpirationDate": "2026-10-25T15:00:00Z",
      "status": "URGENT",
      "assignee": "maria.lopez"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 50,
    "totalElements": 150
  }
}
```

## 4. Capa de Estado (Frontend: Pinia Store)
**Acciones Frontend:**
- **Store:** Crear/Modificar `useWorkdeskStore.ts` (Pinia).
- **Acción:** `fetchGlobalInbox(page, size)`.
- **Estado:** Debe manejar la carga asíncrona (`isLoading`), los errores (`isError`) y almacenar la lista consolidada tipada contra el DTO definido arriba.

## 5. Capa de Vista (Frontend: UI View)
**Acciones Frontend:**
- **Componente:** Refactorizar el componente del Data Grid en `Pantalla 1: Workdesk`.
- **Validación Visual:** 
  - Iterar el array unificado del Store.
  - Renderizar Semáforo/Tick-Tock SLA dinámico usando la variable `slaExpirationDate`.
  - Inyectar íconos visuales (⚡ o 📋) dependiendo si el campo `sourceSystem` es BPMN o KANBAN.

---
**Criterio de Cierre (DoD):** El código debe compilar, las migraciones deben aplicar exitosamente y el endpoint debe retornar HTTP 200 proveyendo el arreglo paginado en Postman (< 800ms) antes de marcar el ticket como completado.
