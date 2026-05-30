package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.TaskDraft;
import com.ibpms.poc.domain.port.TaskDraftRepository;
import com.ibpms.poc.infrastructure.jpa.entity.TaskDraftJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.Optional;
import java.util.UUID;

// @Traceability: US-003 - ADR-001 - Repositorio de Persistencia
@Repository
@Traceability(US = "US-003", CA = {"CA-91"})
public class TaskDraftRepositoryJpa implements TaskDraftRepository {

    private final SpringDataTaskDraftRepository springDataRepository;

    public TaskDraftRepositoryJpa(SpringDataTaskDraftRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public TaskDraft save(TaskDraft draft) {
        TaskDraftJpaEntity entity = mapToEntity(draft);
        TaskDraftJpaEntity saved = springDataRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<TaskDraft> findById(UUID id) {
        return springDataRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Optional<TaskDraft> findByTaskIdAndUserId(String taskId, String userId) {
        return springDataRepository.findByTaskIdAndUserId(taskId, userId).map(this::mapToDomain);
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        springDataRepository.deleteAll();
    }

    private TaskDraft mapToDomain(TaskDraftJpaEntity entity) {
        if (entity == null) return null;
        return TaskDraft.builder()
                .id(entity.getId())
                .taskId(entity.getTaskId())
                .userId(entity.getUserId())
                .currentStep(entity.getCurrentStep())
                .partialData(entity.getPartialData())
                .schemaVersion(entity.getSchemaVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private TaskDraftJpaEntity mapToEntity(TaskDraft domain) {
        if (domain == null) return null;
        return TaskDraftJpaEntity.builder()
                .id(domain.getId())
                .taskId(domain.getTaskId())
                .userId(domain.getUserId())
                .currentStep(domain.getCurrentStep())
                .partialData(domain.getPartialData())
                .schemaVersion(domain.getSchemaVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}

interface SpringDataTaskDraftRepository extends JpaRepository<TaskDraftJpaEntity, UUID> {
    Optional<TaskDraftJpaEntity> findByTaskIdAndUserId(String taskId, String userId);
}
