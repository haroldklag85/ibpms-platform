package com.ibpms.poc.application.port.out;

import com.ibpms.poc.domain.model.kanban.KanbanTask;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KanbanTaskPort {
    KanbanTask save(KanbanTask task);
    Optional<KanbanTask> findById(UUID id);
    List<KanbanTask> findByBoardId(UUID boardId);
}
