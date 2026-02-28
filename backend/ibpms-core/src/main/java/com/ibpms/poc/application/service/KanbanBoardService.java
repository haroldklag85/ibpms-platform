package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.in.DelegateTaskUseCase;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KanbanBoardService implements DelegateTaskUseCase {

    private final KanbanTaskRepository taskRepository;

    public KanbanBoardService(KanbanTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional
    public String delegateSubTask(String parentTaskId, String subTaskName, String assignee) {

        // 1. Encontrar la tarea padre
        KanbanTaskEntity parent = taskRepository.findById(java.util.UUID.fromString(parentTaskId))
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
}
