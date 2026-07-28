# 🏗️ Handoff Arquitectónico Backend — US-008: Kanban Zero-Mock (Híbrido)

## 1. Metadatos y SSOT
- **Sprint/Iteración**: Sprint PM-01, Slot 4
- **User Story**: US-008 (Vista Kanban)
- **Branch de trabajo**: `sprint-8/pm-01/us-008-kanban-real`
- **SSOT**: `docs/requirements/epics/epic_B_workdesk.md`
- **Flujo de Trabajo**: Backend -> Frontend -> QA

## 2. Alineación Arquitectónica y ADRs
- **ADR-010 (Zero-Mock)**: Se prohíbe mantener la entidad `KanbanTaskEntity` como un silo aislado. Todas las tareas mostradas en Kanban DEBEN ser proyecciones reales de `WorkdeskProjectionEntity` (Camunda/AgileTasks).
- **Escalabilidad Corporativa (Híbrido)**: El sistema permite tableros dinámicos. Si un estado (columna) no existe nativamente en Workdesk (ej. `BLOCKED` o estados customizados), el backend lo crea dinámicamente en `KanbanColumnEntity` y persiste el estado de la tarea en `KanbanTaskEntity`, usándolo como una tabla de enlace (decorator) en lugar de duplicar los datos de negocio (título, descripción, assignee).
- **Sincronización Bidireccional**: Si un estado Kanban mapea a un estado nativo de Workdesk (`TODO` -> `PENDING`, `IN_PROGRESS` -> `CLAIMED`), el backend DEBE invocar a `AgileTaskService` para mantener la coherencia transaccional.

## 3. Rutas Exactas y Contexto Preexistente
- `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/KanbanTaskEntity.java`: Contiene atributos duplicados que deben ser eliminados.
- `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/KanbanBoardService.java`: El método `getBoardColumns` actualmente agrupa en memoria basándose en la entidad falsa.
- `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/KanbanController.java`: Endpoints actuales para Kanban.

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

### A. Refactorización de `KanbanTaskEntity.java`
Eliminar los atributos duplicados de negocio. Convertirla en una entidad de enlace (Decorator).
```java
@Entity
@Table(name = "ibpms_kanban_task")
public class KanbanTaskEntity {
    @Id
    @Column(columnDefinition = "bpchar")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private KanbanBoardEntity board;

    // ELIMINAR title, description, slaDueDate, assignee, priority.
    
    // NUEVO: Enlace a la tarea real del Workdesk
    @Column(name = "original_task_id", nullable = false)
    private String originalTaskId; 

    @Column(name = "status", nullable = false, length = 50)
    private String status; // Mapea al nombre de la columna Kanban (ej. "TODO", "IN_PROGRESS", "BLOCKED", o custom)

    @Lob
    @Column(name = "blocked_reason", columnDefinition = "TEXT")
    private String blockedReason;
    
    // ... getters y setters
}
```
*Nota: Asegúrate de crear el Liquibase changelog o actualizar el DDL si JPA ddl-auto no está en update.*

### B. Refactorización de `KanbanBoardService.java`
Modificar `getBoardColumns` para que haga un JOIN lógico con `WorkdeskProjectionEntity`.
```java
@Transactional(readOnly = true)
public Map<String, List<Map<String, Object>>> getBoardColumns(String tenantId, UUID boardId) {
    // 1. Obtener tareas del tablero Kanban
    List<KanbanTaskEntity> kanbanTasks = taskRepository.findByBoardId(boardId);
    
    // 2. Obtener datos reales del WorkdeskProjectionRepository usando los originalTaskId
    List<String> taskIds = kanbanTasks.stream().map(KanbanTaskEntity::getOriginalTaskId).collect(Collectors.toList());
    List<WorkdeskProjectionEntity> realTasks = projectionRepository.findAllById(taskIds);
    Map<String, WorkdeskProjectionEntity> realTaskMap = realTasks.stream()
        .collect(Collectors.toMap(WorkdeskProjectionEntity::getOriginalTaskId, t -> t));

    // 3. Agrupar dinámicamente según las columnas configuradas en KanbanColumnEntity
    // Si la tarea existe en Kanban pero ya no en Workdesk, se ignora o se marca como completada.
    // Combinar el estado custom de Kanban con los datos de Workdesk (title, assignee, sla).
}
```

Modificar `moveTask`:
```java
@Transactional
public KanbanTaskEntity moveTask(UUID kanbanTaskId, String newStatus, String assignee, String blockedReason) {
    KanbanTaskEntity task = taskRepository.findById(kanbanTaskId)
            .orElseThrow(() -> new IllegalArgumentException("Task no encontrada"));
    
    task.setStatus(newStatus);
    if ("BLOCKED".equals(newStatus)) {
        task.setBlockedReason(blockedReason);
    }
    taskRepository.save(task);

    // Sincronización Bidireccional con Workdesk (Zero-Mock)
    if ("IN_PROGRESS".equalsIgnoreCase(newStatus)) {
        agileTaskService.claimTask(UUID.fromString(task.getOriginalTaskId()), assignee);
    } else if ("TODO".equalsIgnoreCase(newStatus)) {
        agileTaskService.unclaimTask(UUID.fromString(task.getOriginalTaskId()), assignee, null);
    }
    // Estados custom no afectan a Workdesk directamente, a menos que el usuario defina reglas.
    
    // Crear columna si el estado no existe en KanbanColumnEntity (Auto-escalabilidad)
    ensureColumnExists(task.getBoard(), newStatus);
    
    return task;
}
```

## 5. Matriz de QA y Testing Atómico
**Script sugerido**: `KanbanIntegrationServiceTest.java` (JUnit 5 + Mockito)
| Test Name | CA Evaluado | Aserción Esperada |
|-----------|-------------|-------------------|
| `testMoveTaskToInProgressClaimsInWorkdesk` | CA-01, CA-02 | Verificar que `agileTaskService.claimTask` fue invocado al mover a "IN_PROGRESS". |
| `testMoveTaskToTodoUnclaimsInWorkdesk` | CA-01, CA-02 | Verificar que `agileTaskService.unclaimTask` fue invocado. |
| `testGetBoardColumnsMergesRealData` | CA-08 | Verificar que el JSON resultante contiene `title` y `assignee` provenientes de `WorkdeskProjectionEntity`. |
| `testMoveToCustomStateCreatesColumn` | Escalabilidad | Verificar que mover a un estado no existente crea el `KanbanColumnEntity`. |

## 6. Mensaje de Despacho (Comunicación al Agente Especialista)
> "Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B."
