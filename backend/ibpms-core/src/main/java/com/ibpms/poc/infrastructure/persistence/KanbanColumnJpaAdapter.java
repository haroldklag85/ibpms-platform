package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.application.port.out.KanbanColumnPort;
import com.ibpms.poc.domain.model.kanban.KanbanColumn;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanColumnEntity;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanColumnRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class KanbanColumnJpaAdapter implements KanbanColumnPort {

    private final KanbanColumnRepository repository;

    public KanbanColumnJpaAdapter(KanbanColumnRepository repository) {
        this.repository = repository;
    }

    @Override
    public KanbanColumn save(KanbanColumn column) {
        KanbanColumnEntity entity = new KanbanColumnEntity();
        entity.setId(column.getId());
        entity.setBoardId(column.getBoardId());
        entity.setName(column.getName());
        entity.setPosition(column.getPosition());

        entity = repository.save(entity);
        return toDomain(entity);
    }

    @Override
    public List<KanbanColumn> findByBoardId(UUID boardId) {
        return repository.findByBoardId(boardId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByBoardId(UUID boardId) {
        return repository.countByBoardId(boardId);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private KanbanColumn toDomain(KanbanColumnEntity entity) {
        return new KanbanColumn(
                entity.getId(),
                entity.getBoardId(),
                entity.getName(),
                entity.getPosition()
        );
    }
}
