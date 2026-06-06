package com.ibpms.poc.application.port.out;

import com.ibpms.poc.domain.model.kanban.KanbanColumn;
import java.util.List;
import java.util.UUID;

public interface KanbanColumnPort {
    KanbanColumn save(KanbanColumn column);
    List<KanbanColumn> findByBoardId(UUID boardId);
    long countByBoardId(UUID boardId);
    void deleteById(UUID id);
}
