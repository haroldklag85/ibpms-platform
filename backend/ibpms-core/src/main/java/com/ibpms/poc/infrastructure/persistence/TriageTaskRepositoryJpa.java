// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.DomainPage;
import com.ibpms.poc.domain.model.TriageTask;
import com.ibpms.poc.domain.port.TriageTaskRepository;
import com.ibpms.poc.infrastructure.jpa.entity.TriageTaskJpaEntity;
import com.ibpms.poc.infrastructure.jpa.mapper.TriageTaskMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

@Repository
public class TriageTaskRepositoryJpa implements TriageTaskRepository {

    private final SpringDataTriageTaskRepository repository;
    private final TriageTaskMapper mapper;

    public TriageTaskRepositoryJpa(SpringDataTriageTaskRepository repository, TriageTaskMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public TriageTask save(TriageTask task) {
        TriageTaskJpaEntity entity = mapper.toEntity(task);
        TriageTaskJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<TriageTask> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<TriageTask> findByIdForUpdate(UUID id) {
        return repository.findByIdForUpdate(id).map(mapper::toDomain);
    }

    @Override
    public DomainPage<TriageTask> findByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TriageTaskJpaEntity> jpaPage = repository.findByStatus(status, pageable);
        List<TriageTask> content = jpaPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();
        return new DomainPage<>(content, jpaPage.getTotalElements(), page, size);
    }

    @Override
    public void deleteByStatusAndUpdatedAtBefore(String status, ZonedDateTime cutoff) {
        repository.deleteOldRecords(status, cutoff);
    }
}

interface SpringDataTriageTaskRepository extends JpaRepository<TriageTaskJpaEntity, UUID> {
    Page<TriageTaskJpaEntity> findByStatus(String status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")})
    @Query("SELECT t FROM TriageTaskJpaEntity t WHERE t.id = :id")
    Optional<TriageTaskJpaEntity> findByIdForUpdate(@Param("id") UUID id);

    @Modifying
    @Query("DELETE FROM TriageTaskJpaEntity t WHERE t.status = :status AND t.updatedAt < :cutoff")
    void deleteOldRecords(@Param("status") String status, @Param("cutoff") ZonedDateTime cutoff);
}
