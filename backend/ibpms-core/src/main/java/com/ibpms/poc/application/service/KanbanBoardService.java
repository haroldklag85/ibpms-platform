package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.KanbanBoardDto;
import com.ibpms.poc.application.dto.KanbanColumnDto;
import com.ibpms.poc.application.dto.KanbanTaskDto;
import com.ibpms.poc.application.dto.KanbanTaskStateDto;
import com.ibpms.poc.application.port.in.DelegateTaskUseCase;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
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
    private final SimpMessagingTemplate messagingTemplate;

    public KanbanBoardService(KanbanTaskRepository taskRepository, KanbanBoardRepository boardRepository,
                              WorkdeskProjectionRepository projectionRepository, AgileTaskService agileTaskService,
                              KanbanColumnRepository columnRepository, SimpMessagingTemplate messagingTemplate) {
        this.taskRepository = taskRepository;
        this.boardRepository = boardRepository;
        this.projectionRepository = projectionRepository;
        this.agileTaskService = agileTaskService;
        this.columnRepository = columnRepository;
        this.messagingTemplate = messagingTemplate;
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

        // 1. Encontrar la tarea padre
        KanbanTaskEntity parent = taskRepository.findById(java.util.Objects.requireNonNull(java.util.UUID.fromString(parentTaskId)))
                .orElseThrow(() -> new RuntimeException("Tarea padre no encontrada (Ad-Hoc Delegation)"));

        // 2. Crear la Sub Tarea (Sprint-6: campos enriquecidos + CQRS originalTaskId)
        KanbanTaskEntity subTask = new KanbanTaskEntity();
        subTask.setBoard(parent.getBoard());
        subTask.setTitle(subTaskName);
        subTask.setDescription("Sub-tarea generada ad-hoc a partir de: " + parent.getTitle());
        subTask.setAssignee(assignee);
        subTask.setParentTask(parent);
        subTask.setOriginalTaskId(UUID.randomUUID().toString());

        // 3. Javers detectará automáticamente este Save gracias a Hibernate
        KanbanTaskEntity savedSubTask = taskRepository.save(subTask);

        return savedSubTask.getId().toString();
    }

    @Transactional(readOnly = true)
    public KanbanBoardDto getKanbanBoard(String tenantId, String projectId) {
        UUID boardId = UUID.fromString(projectId);
        List<KanbanTaskEntity> kanbanTasks = taskRepository.findByBoardIdOrderByUpdatedAtDesc(boardId);
        List<String> taskIds = kanbanTasks.stream().map(KanbanTaskEntity::getOriginalTaskId).collect(Collectors.toList());
        List<WorkdeskProjectionEntity> realTasks = projectionRepository.findAllById(taskIds);
        
        Map<String, WorkdeskProjectionEntity> realTaskMap = realTasks.stream()
            .collect(Collectors.toMap(WorkdeskProjectionEntity::getId, t -> t, (existing, replacement) -> existing));

        Map<String, List<KanbanTaskDto>> result = new HashMap<>();
        
        for (KanbanTaskEntity kt : kanbanTasks) {
            WorkdeskProjectionEntity realTask = realTaskMap.get(kt.getOriginalTaskId());
            if (realTask == null) {
                continue;
            }
            
            KanbanTaskDto dto = new KanbanTaskDto();
            dto.setId(kt.getId().toString());
            dto.setOriginalTaskId(kt.getOriginalTaskId());
            dto.setTitle(realTask.getTitle());
            dto.setAssignee(realTask.getAssignee());
            dto.setSlaExpirationDate(realTask.getSlaExpirationDate());
            
            String kanbanState;
            if ("PENDING".equalsIgnoreCase(realTask.getStatus())) {
                kanbanState = "TODO";
            } else if ("CLAIMED".equalsIgnoreCase(realTask.getStatus())) {
                kanbanState = "IN_PROGRESS";
            } else {
                kanbanState = "DONE";
            }
            dto.setState(kanbanState);

            result.computeIfAbsent(kanbanState, k -> new ArrayList<>()).add(dto);
        }

        List<KanbanColumnDto> columnsOutput = new ArrayList<>();
        List<String> predefined = List.of("TODO", "IN_PROGRESS", "DONE");
        
        for (String status : predefined) {
            KanbanColumnDto colDto = new KanbanColumnDto();
            colDto.setName(status);
            colDto.setTasks(result.getOrDefault(status, new ArrayList<>()));
            columnsOutput.add(colDto);
        }

        KanbanBoardDto boardDto = new KanbanBoardDto();
        boardDto.setColumns(columnsOutput);
        return boardDto;
    }
    
    @Transactional
    public KanbanTaskStateDto moveTask(String projectId, String taskId, String newStatus, String assignee) {
        KanbanTaskEntity task = taskRepository.findById(UUID.fromString(taskId))
                .orElseThrow(() -> new IllegalArgumentException("Task no encontrada"));
        
        try {
            if ("IN_PROGRESS".equalsIgnoreCase(newStatus)) {
                agileTaskService.claimTask(UUID.fromString(task.getOriginalTaskId()), assignee);
            } else if ("TODO".equalsIgnoreCase(newStatus)) {
                agileTaskService.unclaimTask(UUID.fromString(task.getOriginalTaskId()), assignee, null);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al cambiar estado de la tarea: " + e.getMessage(), e);
        }
        
        KanbanTaskStateDto response = new KanbanTaskStateDto(task.getId().toString(), newStatus, 1L);
        messagingTemplate.convertAndSend("/topic/workdesk/kanban", response);
        
        return response;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MÉTODOS PORTADOS DE SPRINT-6 (Legacy Support)
    // ═══════════════════════════════════════════════════════════════════════

    @Transactional
    public KanbanTaskEntity moveTaskLegacy(UUID taskId, String newStatus) {
        KanbanTaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task no encontrada"));
        task.setStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
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
