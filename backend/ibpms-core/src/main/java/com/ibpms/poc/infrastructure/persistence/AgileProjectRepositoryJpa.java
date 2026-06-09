// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.agile.AgileProject;
import com.ibpms.poc.infrastructure.jpa.entity.agile.AgileProjectJpaEntity;
import com.ibpms.poc.infrastructure.jpa.mapper.agile.AgileProjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AgileProjectRepositoryJpa {

    private final SpringDataAgileProjectRepository repository;
    private final AgileProjectMapper mapper;

    public AgileProjectRepositoryJpa(SpringDataAgileProjectRepository repository, AgileProjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public AgileProject save(AgileProject project) {
        AgileProjectJpaEntity entity = mapper.toEntity(project);
        AgileProjectJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    public Optional<AgileProject> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    public Page<AgileProject> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }

    public void updateStatus(UUID projectId, String status) {
        repository.updateStatus(projectId, status);
    }
}

interface SpringDataAgileProjectRepository extends JpaRepository<AgileProjectJpaEntity, UUID> {
    @Modifying
    @Query("UPDATE AgileProjectJpaEntity p SET p.status = :status, p.closedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") String status);
}
