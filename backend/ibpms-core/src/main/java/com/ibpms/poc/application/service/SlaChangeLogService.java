package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.agile.AgileSlaChangelog;
import com.ibpms.poc.infrastructure.persistence.AgileSlaChangelogRepositoryJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class SlaChangeLogService {

    private final AgileSlaChangelogRepositoryJpa repository;

    public SlaChangeLogService(AgileSlaChangelogRepositoryJpa repository) {
        this.repository = repository;
    }

    @Transactional
    public void logSlaModification(UUID taskId, ZonedDateTime previous, ZonedDateTime newValue, String changedBy) {
        AgileSlaChangelog log = AgileSlaChangelog.builder()
                .taskId(taskId)
                .previousValue(previous)
                .newValue(newValue)
                .changedBy(changedBy)
                .build();
        repository.save(log);
    }

    public Page<AgileSlaChangelog> getLogsByTask(UUID taskId, Pageable pageable) {
        return repository.findByTaskIdOrderByChangedAtDesc(taskId, pageable);
    }
}
