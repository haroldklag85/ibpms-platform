package com.ibpms.poc.application.port.out;

import com.ibpms.poc.domain.model.kanban.TimeLogEntry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeLogPort {
    TimeLogEntry save(TimeLogEntry entry);
    List<TimeLogEntry> findByReferenceId(UUID referenceId);
    Optional<TimeLogEntry> findActiveByUserAndReference(String userId, UUID referenceId);
    Optional<TimeLogEntry> findById(UUID id);
}
