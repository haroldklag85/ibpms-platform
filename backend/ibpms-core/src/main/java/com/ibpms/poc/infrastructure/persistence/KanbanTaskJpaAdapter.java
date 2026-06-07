package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.application.port.out.KanbanTaskPort;
import com.ibpms.poc.domain.model.kanban.KanbanState;
import com.ibpms.poc.domain.model.kanban.KanbanTask;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanBoardEntity;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanTaskRepository;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class KanbanTaskJpaAdapter implements KanbanTaskPort {

    private final KanbanTaskRepository repository;

    public KanbanTaskJpaAdapter(KanbanTaskRepository repository) {
        this.repository = repository;
    }

    @Override
    public KanbanTask save(KanbanTask task) {
        KanbanTaskEntity entity;
        if (task.getId() != null) {
            entity = repository.findById(task.getId()).orElse(new KanbanTaskEntity());
        } else {
            entity = new KanbanTaskEntity();
        }

        entity.setId(task.getId());
        
        KanbanBoardEntity board = new KanbanBoardEntity();
        board.setId(task.getBoardId());
        entity.setBoard(board);
        
        // originalTaskId needs to be set
        entity.setOriginalTaskId(task.getId().toString());

        entity = repository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<KanbanTask> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<KanbanTask> findByBoardId(UUID boardId) {
        return repository.findByBoardIdOrderByUpdatedAtDesc(boardId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private KanbanTask toDomain(KanbanTaskEntity entity) {
        KanbanTask task = new KanbanTask();
        task.setId(entity.getId());
        task.setBoardId(entity.getBoard().getId());
        task.setTitle("Workdesk Data (Zero-Mock)");
        task.setDescription("N/A");
        task.setStatus(KanbanState.TODO);
        task.setAssignee("N/A");
        task.setPriority("N/A");
        task.setBlockedReason("N/A");
        if (entity.getCreatedAt() != null) {
            task.setCreatedAt(entity.getCreatedAt().atZone(ZoneId.systemDefault()));
        }
        if (entity.getUpdatedAt() != null) {
            task.setUpdatedAt(entity.getUpdatedAt().atZone(ZoneId.systemDefault()));
        }

        return task;
    }
}
