package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.FormEvent;
import com.ibpms.poc.domain.port.FormEventRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ibpms.poc.infrastructure.jpa.entity.FormEventEntity;

import java.util.List;
import java.util.Optional;
import com.ibpms.poc.domain.model.EventType;
import java.util.UUID;

@Repository
public class FormEventRepositoryJpa implements FormEventRepository {

    private final SpringDataFormEventRepository springDataRepository;

    public FormEventRepositoryJpa(SpringDataFormEventRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    private FormEventEntity toEntity(FormEvent domain) {
        if (domain == null) return null;
        return FormEventEntity.builder()
            .eventId(domain.getEventId())
            .eventType(domain.getEventType())
            .taskId(domain.getTaskId())
            .processInstanceId(domain.getProcessInstanceId())
            .userId(domain.getUserId())
            .payloadJson(domain.getPayloadJson())
            .schemaVersion(domain.getSchemaVersion())
            .idempotencyKey(domain.getIdempotencyKey())
            .originalEventId(domain.getOriginalEventId())
            .createdAt(domain.getCreatedAt())
            .build();
    }

    private FormEvent toDomain(FormEventEntity entity) {
        if (entity == null) return null;
        return FormEvent.builder()
            .eventId(entity.getEventId())
            .eventType(entity.getEventType())
            .taskId(entity.getTaskId())
            .processInstanceId(entity.getProcessInstanceId())
            .userId(entity.getUserId())
            .payloadJson(entity.getPayloadJson())
            .schemaVersion(entity.getSchemaVersion())
            .idempotencyKey(entity.getIdempotencyKey())
            .originalEventId(entity.getOriginalEventId())
            .createdAt(entity.getCreatedAt())
            .build();
    }

    @Override
    public FormEvent save(FormEvent event) {
        FormEventEntity saved = springDataRepository.save(toEntity(event));
        return toDomain(saved);
    }

    @Override
    public Optional<FormEvent> findById(UUID id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<FormEvent> findAll() {
        return springDataRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<FormEvent> findByProcessInstanceIdAndEventType(String processInstanceId, EventType eventType) {
        return springDataRepository.findByProcessInstanceIdAndEventType(processInstanceId, eventType)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteAll() {
        springDataRepository.deleteAll();
    }
}

interface SpringDataFormEventRepository extends JpaRepository<FormEventEntity, UUID> {
    List<FormEventEntity> findByProcessInstanceIdAndEventType(String processInstanceId, EventType eventType);
}
