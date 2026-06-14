package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.application.port.out.TimeLogPort;
import com.ibpms.poc.domain.model.kanban.TimeLogEntry;
import com.ibpms.poc.infrastructure.jpa.entity.TimeLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.TimeLogRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TimeLogJpaAdapter implements TimeLogPort {

    private final TimeLogRepository repository;

    public TimeLogJpaAdapter(TimeLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public TimeLogEntry save(TimeLogEntry entry) {
        TimeLogEntity entity = new TimeLogEntity();
        entity.setId(entry.getId());
        entity.setReferenceId(entry.getReferenceId());
        entity.setReferenceType(entry.getReferenceType());
        entity.setStartedAt(entry.getStartedAt());
        entity.setStoppedAt(entry.getStoppedAt());
        entity.setDurationMinutes(entry.getDurationMinutes());
        entity.setUserId(entry.getUserId());
        entity.setCreatedAt(entry.getCreatedAt());

        entity = repository.save(entity);
        return toDomain(entity);
    }

    @Override
    public List<TimeLogEntry> findByReferenceId(UUID referenceId) {
        return repository.findByReferenceId(referenceId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TimeLogEntry> findActiveByUserAndReference(String userId, UUID referenceId) {
        return repository.findByUserIdAndReferenceIdAndStoppedAtIsNull(userId, referenceId)
                .map(this::toDomain);
    }

    @Override
    public Optional<TimeLogEntry> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    private TimeLogEntry toDomain(TimeLogEntity entity) {
        return new TimeLogEntry(
                entity.getId(),
                entity.getReferenceId(),
                entity.getReferenceType(),
                entity.getStartedAt(),
                entity.getStoppedAt(),
                entity.getDurationMinutes(),
                entity.getUserId(),
                entity.getCreatedAt()
        );
    }
}
