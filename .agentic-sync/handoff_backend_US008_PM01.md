# Handoff Backend — Iteración Sprint PM-01, Slot 4

## 1. Metadatos y SSOT
- **Iteración/Sprint:** Sprint PM-01, Slot 4
- **Rama de Trabajo:** `sprint-8/pm-01/us-008-kanban-real`
- **User Story:** US-008 (Vista Kanban)
- **Criterios de Aceptación (CAs) Target:** CA-4, CA-5, CA-6, CA-8, CA-12
- **Path del SSOT:** `docs/requirements/v1_user_stories_index.md` → `docs/requirements/epics/epic_A_motor_core.md` (Líneas 737-830)
- **Flujo de Trabajo:** Backend -> Frontend -> QA

## 2. Alineación Arquitectónica y ADRs
- **ADR-010 (Zero-Mock):** Eliminación total de entidades falsas de Kanban (`KanbanTaskEntity` mock). La integración será real con la API del Workdesk.
- **ADR-001 (Arquitectura Hexagonal):** Lógica del Kanban reside en el dominio. Las consultas al Workdesk (`WorkdeskProjectionRepository`) se hacen a través del Service o un Port específico.
- **Trazabilidad:** Al conectar el Kanban con las tareas reales de Camunda/Workdesk, garantizamos la coherencia de los datos y cumplimos con el mandato de la Metodología de Cadenas de Capacidad (Cadena 2).

> 📋 **DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:**
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

## 3. Rutas Exactas y Contexto Preexistente
- **Archivo a modificar/eliminar:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/KanbanTaskEntity.java` -> ELIMINAR O REFACTORIZAR campos mockeados.
- **Archivo a modificar:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/KanbanBoardService.java` -> Modificar para inyectar `WorkdeskProjectionRepository` y `AgileTaskService`.
- **Archivo a modificar:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/rest/KanbanController.java` -> Implementar endpoints descritos en los Snippets.

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

### Identificación de Mocks (Para Eliminar):
- Tablas y clases que simulen un ciclo de vida paralelo al Workdesk (`KanbanTaskEntity` con datos duros).
- Endpoints en `KanbanController` que devuelven listas quemadas.

### Backend - Endpoint Kanban Columns
Endpoint que devuelve las tareas reales agrupadas en formato Kanban:
```java
// En KanbanController.java
@GetMapping("/api/v1/projects/{projectId}/kanban")
public ResponseEntity<KanbanBoardDto> getKanbanBoard(@PathVariable String projectId) {
    // 1. Consultar WorkdeskProjectionRepository
    // 2. Mapear PENDING -> TODO, CLAIMED -> IN_PROGRESS
    // 3. Devolver objeto con listas por columna (CA-8 hard-limit 7 columnas)
}
```

### Backend - Endpoint State Machine (CA-6 y CA-12)
Endpoint para mover tarjetas:
```java
@PatchMapping("/api/v1/projects/{projectId}/kanban/tasks/{taskId}/state")
public ResponseEntity<KanbanTaskStateDto> updateTaskState(
        @PathVariable String projectId,
        @PathVariable String taskId,
        @RequestBody KanbanStateUpdatePayload payload) {
    
    // Si new_status == "IN_PROGRESS", llamar a agileTaskService.claimTask(taskId, assignee)
    // Si new_status == "TODO", llamar a agileTaskService.unclaimTask(taskId)
    // Publicar evento en Websocket /topic/workdesk/kanban (CA-12)
    // Retornar 200 OK con {id, status, version}
}
```

## 5. Matriz de QA y Testing Atómico
**Agente QA / TDD Backend:**
- `KanbanIntegrationServiceTest.java` (o similar) en la capa de test de integración.

| Test Name | CA Evaluado | Aserción Esperada |
|-----------|-------------|-------------------|
| testGetKanbanBoardReturnsRealTasks | CA-5, CA-8 | Retorna 200 OK. La estructura contiene columnas `TODO` y `IN_PROGRESS` pobladas con tareas de Workdesk. |
| testPatchTaskStateToInProgressCallsClaim | CA-6 | Retorna 200 OK. Verifica que al mandar estado `IN_PROGRESS` se invoca `taskService.claimTask`. |
| testPatchTaskStateEmitsWebsocketEvent | CA-12 | Retorna 200 OK y verifica que el `SimpMessagingTemplate` emite el mensaje a `/topic/workdesk/kanban`. |

## 6. Mensaje de Despacho (Comunicación al Agente Especialista)

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve). 
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> "Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B."

📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
- Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
- Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
