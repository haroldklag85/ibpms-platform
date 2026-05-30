// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.agile.AgileSlaChangelog;
import com.ibpms.poc.infrastructure.jpa.entity.agile.AgileSlaChangelogJpaEntity;
import com.ibpms.poc.infrastructure.jpa.mapper.agile.AgileSlaChangelogMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class AgileSlaChangelogRepositoryJpa {

    private final SpringDataAgileSlaChangelogRepository repository;
    private final AgileSlaChangelogMapper mapper;

    public AgileSlaChangelogRepositoryJpa(SpringDataAgileSlaChangelogRepository repository, AgileSlaChangelogMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public AgileSlaChangelog save(AgileSlaChangelog domain) {
        AgileSlaChangelogJpaEntity entity = mapper.toEntity(domain);
        AgileSlaChangelogJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    public Page<AgileSlaChangelog> findByTaskIdOrderByChangedAtDesc(UUID taskId, Pageable pageable) {
        Page<AgileSlaChangelogJpaEntity> entities = repository.findByTaskIdOrderByChangedAtDesc(taskId, pageable);
        return entities.map(mapper::toDomain);
    }
}

interface SpringDataAgileSlaChangelogRepository extends JpaRepository<AgileSlaChangelogJpaEntity, UUID> {
    Page<AgileSlaChangelogJpaEntity> findByTaskIdOrderByChangedAtDesc(UUID taskId, Pageable pageable);
}
