// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.agile.AgileTimebox;
import com.ibpms.poc.infrastructure.jpa.entity.agile.AgileTimeboxJpaEntity;
import com.ibpms.poc.infrastructure.jpa.mapper.agile.AgileTimeboxMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AgileTimeboxRepositoryJpa {

    private final SpringDataAgileTimeboxRepository repository;
    private final AgileTimeboxMapper mapper;

    public AgileTimeboxRepositoryJpa(SpringDataAgileTimeboxRepository repository, AgileTimeboxMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public AgileTimebox save(AgileTimebox timebox) {
        AgileTimeboxJpaEntity entity = mapper.toEntity(timebox);
        AgileTimeboxJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    public List<AgileTimebox> findByProjectIdOrderByStartDateAsc(UUID projectId) {
        return repository.findByProjectIdOrderByStartDateAsc(projectId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    public Optional<AgileTimebox> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
}

interface SpringDataAgileTimeboxRepository extends JpaRepository<AgileTimeboxJpaEntity, UUID> {
    List<AgileTimeboxJpaEntity> findByProjectIdOrderByStartDateAsc(UUID projectId);
}
