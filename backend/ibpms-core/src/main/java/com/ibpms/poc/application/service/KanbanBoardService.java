package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.in.DelegateTaskUseCase;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ibpms.poc.infrastructure.jpa.repository.KanbanBoardRepository;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanBoardEntity;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;

@Service
public class KanbanBoardService implements DelegateTaskUseCase {

    private final KanbanTaskRepository taskRepository;
    private final KanbanBoardRepository boardRepository;

    public KanbanBoardService(KanbanTaskRepository taskRepository, KanbanBoardRepository boardRepository) {
        this.taskRepository = taskRepository;
        this.boardRepository = boardRepository;
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

    @Transactional
    public KanbanTaskEntity moveTaskLegacy(UUID taskId, String newStatus) {
        KanbanTaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task no encontrada"));
        task.setStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public String delegateSubTask(String parentTaskId, String subTaskName, String assignee) {

        // 1. Encontrar la tarea padre
        KanbanTaskEntity parent = taskRepository.findById(java.util.Objects.requireNonNull(java.util.UUID.fromString(parentTaskId)))
                .orElseThrow(() -> new RuntimeException("Tarea padre no encontrada (Ad-Hoc Delegation)"));

        // 2. Crear la Sub Tarea
        KanbanTaskEntity subTask = new KanbanTaskEntity();
        subTask.setBoard(parent.getBoard()); // Hereda el mismo tablero (o expdiente)
        subTask.setTitle(subTaskName);
        subTask.setDescription("Sub-tarea generada ad-hoc a partir de: " + parent.getTitle());
        subTask.setAssignee(assignee);
        subTask.setParentTask(parent); // Auto-Referencia JPA

        // 3. Javers detectará automáticamente este Save gracias a Hibernate y disparará
        // un Shadow Commit de Creación
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
    public Map<String, List<Map<String, Object>>> getBoardColumns(String tenantId) {
        List<KanbanTaskEntity> tasks = taskRepository.findTasksByTenantId(tenantId);
        
        // Agrupar por estado (TODO, IN_PROGRESS, BLOCKED, DONE)
        Map<String, List<Map<String, Object>>> grouped = tasks.stream()
            .collect(Collectors.groupingBy(
                KanbanTaskEntity::getStatus,
                Collectors.mapping(
                    task -> Map.of(
                        "id", task.getId(),
                        "title", task.getTitle(),
                        "state", task.getStatus(),
                        "assignee", task.getAssignee() != null ? task.getAssignee() : "Unassigned"
                        // El object map puede extenderse si hay SLA, description u otros datos necesarios para UI
                    ),
                    Collectors.toList()
                )
            ));
            
        return Map.of("columns", 
            List.of(
                Map.of("name", "TODO", "tasks", grouped.getOrDefault("TODO", List.of())),
                Map.of("name", "IN_PROGRESS", "tasks", grouped.getOrDefault("IN_PROGRESS", List.of())),
                Map.of("name", "BLOCKED", "tasks", grouped.getOrDefault("BLOCKED", List.of())),
                Map.of("name", "DONE", "tasks", grouped.getOrDefault("DONE", List.of()))
            )
        );
    }
}
