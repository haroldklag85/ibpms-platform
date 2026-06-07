package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.in.DelegateTaskUseCase;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ibpms.poc.infrastructure.jpa.repository.KanbanBoardRepository;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanBoardEntity;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanColumnRepository;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanColumnEntity;

@Service
public class KanbanBoardService implements DelegateTaskUseCase {

    private final KanbanTaskRepository taskRepository;
    private final KanbanBoardRepository boardRepository;
    private final WorkdeskProjectionRepository projectionRepository;
    private final AgileTaskService agileTaskService;
    private final KanbanColumnRepository columnRepository;

    public KanbanBoardService(KanbanTaskRepository taskRepository, KanbanBoardRepository boardRepository,
                              WorkdeskProjectionRepository projectionRepository, AgileTaskService agileTaskService,
                              KanbanColumnRepository columnRepository) {
        this.taskRepository = taskRepository;
        this.boardRepository = boardRepository;
        this.projectionRepository = projectionRepository;
        this.agileTaskService = agileTaskService;
        this.columnRepository = columnRepository;
    }

    public List<KanbanBoardEntity> getAllBoards() {
        return boardRepository.findAll();
    }

    @Transactional
    public KanbanBoardEntity createBoard(KanbanBoardEntity board) {
        board.setId(UUID.randomUUID());
        return boardRepository.save(board);
    }

    public List<KanbanTaskEntity> getTasksByBoard(UUID boardId) {
        return taskRepository.findByBoardIdOrderByUpdatedAtDesc(boardId);
    }

    @Transactional
    public KanbanTaskEntity createTask(UUID boardId, KanbanTaskEntity task) {
        KanbanBoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board no encontrado"));
        task.setId(UUID.randomUUID());
        task.setBoard(board);
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public String delegateSubTask(String parentTaskId, String subTaskName, String assignee) {
        KanbanTaskEntity parent = taskRepository.findById(java.util.Objects.requireNonNull(java.util.UUID.fromString(parentTaskId)))
                .orElseThrow(() -> new RuntimeException("Tarea padre no encontrada (Ad-Hoc Delegation)"));

        KanbanTaskEntity subTask = new KanbanTaskEntity();
        subTask.setBoard(parent.getBoard());
        subTask.setOriginalTaskId(UUID.randomUUID().toString()); // Placeholder para compilación
        subTask.setStatus("TODO");
        subTask.setParentTask(parent);

        KanbanTaskEntity savedSubTask = taskRepository.save(subTask);
        return savedSubTask.getId().toString();
    }

    @Transactional
    public void updateTaskState(String taskId, String newState, KanbanStateMachine stateMachine) {
        KanbanTaskEntity task = taskRepository.findById(java.util.Objects.requireNonNull(java.util.UUID.fromString(taskId)))
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        stateMachine.validateTransition(task.getStatus(), newState);

        task.setStatus(newState);
        taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public Map<String, List<Map<String, Object>>> getBoardColumns(String tenantId, UUID boardId) {
        // 1. Obtener tareas del tablero Kanban
        List<KanbanTaskEntity> kanbanTasks = taskRepository.findByBoardIdOrderByUpdatedAtDesc(boardId);
        
        // 2. Obtener datos reales del WorkdeskProjectionRepository usando los originalTaskId
        List<String> taskIds = kanbanTasks.stream().map(KanbanTaskEntity::getOriginalTaskId).collect(Collectors.toList());
        List<WorkdeskProjectionEntity> realTasks = projectionRepository.findAllById(taskIds);
        Map<String, WorkdeskProjectionEntity> realTaskMap = realTasks.stream()
            .collect(Collectors.toMap(WorkdeskProjectionEntity::getId, t -> t, (existing, replacement) -> existing));

        List<KanbanColumnEntity> columns = columnRepository.findByBoardId(boardId);
        if (columns.isEmpty()) {
            columns = List.of();
        }

        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        
        for (KanbanTaskEntity kt : kanbanTasks) {
            WorkdeskProjectionEntity realTask = realTaskMap.get(kt.getOriginalTaskId());
            if (realTask == null) {
                continue; // Ignore or marked as completed
            }
            
            Map<String, Object> map = new HashMap<>();
            map.put("id", kt.getId());
            map.put("originalTaskId", kt.getOriginalTaskId());
            map.put("state", kt.getStatus());
            map.put("title", realTask.getTitle());
            map.put("assignee", realTask.getAssignee() != null ? realTask.getAssignee() : "Unassigned");
            map.put("slaExpirationDate", realTask.getSlaExpirationDate());
            if (kt.getBlockedReason() != null) {
                map.put("blockedReason", kt.getBlockedReason());
            }

            result.computeIfAbsent(kt.getStatus(), k -> new ArrayList<>()).add(map);
        }

        // 3. Agrupar dinámicamente según las columnas configuradas en KanbanColumnEntity
        List<Map<String, Object>> columnsOutput = new ArrayList<>();
        List<String> predefined = List.of("TODO", "IN_PROGRESS", "BLOCKED", "DONE");
        List<String> allStatus = new ArrayList<>(predefined);
        for (KanbanColumnEntity col : columns) {
            if (!allStatus.contains(col.getName())) {
                allStatus.add(col.getName());
            }
        }
        
        for (String status : allStatus) {
            Map<String, Object> colMap = new HashMap<>();
            colMap.put("name", status);
            colMap.put("tasks", result.getOrDefault(status, List.of()));
            columnsOutput.add(colMap);
        }

        return Map.of("columns", columnsOutput);
    }
    
    @Transactional
    public KanbanTaskEntity moveTask(UUID kanbanTaskId, String newStatus, String assignee, String blockedReason) {
        KanbanTaskEntity task = taskRepository.findById(kanbanTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Task no encontrada"));
        
        task.setStatus(newStatus);
        if ("BLOCKED".equals(newStatus)) {
            task.setBlockedReason(blockedReason);
        } else {
            task.setBlockedReason(null);
        }
        taskRepository.save(task);

        // Sincronización Bidireccional con Workdesk (Zero-Mock)
        if ("IN_PROGRESS".equalsIgnoreCase(newStatus)) {
            agileTaskService.claimTask(UUID.fromString(task.getOriginalTaskId()), assignee);
        } else if ("TODO".equalsIgnoreCase(newStatus)) {
            agileTaskService.unclaimTask(UUID.fromString(task.getOriginalTaskId()), assignee, null);
        }
        
        // Crear columna si el estado no existe en KanbanColumnEntity (Auto-escalabilidad)
        ensureColumnExists(task.getBoard(), newStatus);
        
        return task;
    }
    
    private void ensureColumnExists(KanbanBoardEntity board, String status) {
        List<KanbanColumnEntity> cols = columnRepository.findByBoardId(board.getId());
        boolean exists = cols.stream().anyMatch(c -> c.getName().equals(status));
        if (!exists) {
            KanbanColumnEntity newCol = new KanbanColumnEntity();
            newCol.setId(UUID.randomUUID());
            newCol.setBoardId(board.getId());
            newCol.setName(status);
            newCol.setPosition(cols.size() + 1);
            columnRepository.save(newCol);
        }
    }
}
