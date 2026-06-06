package com.ibpms.poc.domain.port;

import com.ibpms.poc.domain.model.TaskDraft;
import java.util.Optional;
import java.util.UUID;

public interface TaskDraftRepository {
    TaskDraft save(TaskDraft draft);
    Optional<TaskDraft> findById(UUID id);
    Optional<TaskDraft> findByTaskIdAndUserId(String taskId, String userId);
    void deleteById(UUID id);
    void deleteAll();
}
