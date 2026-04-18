package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.agile.AgileSlaChangelog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class AgileSlaChangelogRepositoryJpa {

    private final SpringDataAgileSlaChangelogRepository repository;

    public AgileSlaChangelogRepositoryJpa(SpringDataAgileSlaChangelogRepository repository) {
        this.repository = repository;
    }

    public AgileSlaChangelog save(AgileSlaChangelog entity) {
        return repository.save(entity);
    }

    public Page<AgileSlaChangelog> findByTaskIdOrderByChangedAtDesc(UUID taskId, Pageable pageable) {
        return repository.findByTaskIdOrderByChangedAtDesc(taskId, pageable);
    }
}

interface SpringDataAgileSlaChangelogRepository extends JpaRepository<AgileSlaChangelog, UUID> {
    Page<AgileSlaChangelog> findByTaskIdOrderByChangedAtDesc(UUID taskId, Pageable pageable);
}

