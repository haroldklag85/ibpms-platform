package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.TaskDraft;
import com.ibpms.poc.domain.port.TaskDraftRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class TaskDraftRepositoryJpa implements TaskDraftRepository {

    private final SpringDataTaskDraftRepository springDataRepository;

    public TaskDraftRepositoryJpa(SpringDataTaskDraftRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public TaskDraft save(TaskDraft draft) {
        return springDataRepository.save(draft);
    }

    @Override
    public Optional<TaskDraft> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<TaskDraft> findByTaskIdAndUserId(String taskId, String userId) {
        return springDataRepository.findByTaskIdAndUserId(taskId, userId);
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        springDataRepository.deleteAll();
    }
}

interface SpringDataTaskDraftRepository extends JpaRepository<TaskDraft, UUID> {
    Optional<TaskDraft> findByTaskIdAndUserId(String taskId, String userId);
}
