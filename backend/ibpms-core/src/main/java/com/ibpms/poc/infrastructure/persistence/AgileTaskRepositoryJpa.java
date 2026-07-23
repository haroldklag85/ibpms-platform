// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.infrastructure.jpa.entity.agile.AgileTaskJpaEntity;
import com.ibpms.poc.infrastructure.jpa.mapper.agile.AgileTaskMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

@Repository
public class AgileTaskRepositoryJpa {

    private final SpringDataAgileTaskRepository repository;
    private final AgileTaskMapper mapper;

    public AgileTaskRepositoryJpa(SpringDataAgileTaskRepository repository, AgileTaskMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public AgileTask save(AgileTask task) {
        AgileTaskJpaEntity entity = mapper.toEntity(task);
        AgileTaskJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    public void delete(AgileTask task) {
        repository.delete(mapper.toEntity(task));
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public long countByProjectIdAndStatusNotIn(UUID projectId, java.util.List<String> statuses) {
        return repository.countByProjectIdAndStatusNotIn(projectId, statuses);
    }

    public java.util.List<AgileTask> findPortfolioByOwner(String owner) {
        return repository.findPortfolioByOwner(owner).stream()
                .map(mapper::toDomain)
                .toList();
    }

    public java.util.List<AgileTask> findByStatus(String status) {
        return repository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    public Optional<AgileTask> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    public Optional<AgileTask> findByIdForUpdate(UUID id) {
        return repository.findByIdForUpdate(id).map(mapper::toDomain);
    }

    public Page<AgileTask> findByProjectIdAndStatusNot(UUID projectId, String excludeStatus, Pageable pageable) {
        return repository.findByProjectIdAndStatusNot(projectId, excludeStatus, pageable).map(mapper::toDomain);
    }

    public Page<AgileTask> findByProjectIdAndStatusNotIn(UUID projectId, java.util.List<String> statuses, Pageable pageable) {
        return repository.findByProjectIdAndStatusNotIn(projectId, statuses, pageable).map(mapper::toDomain);
    }

    public Optional<AgileTask> findNextAvailableTaskForUpdate() {
        Page<AgileTaskJpaEntity> page = repository.findNextAvailableTaskForUpdate(org.springframework.data.domain.PageRequest.of(0, 1));
        return page.hasContent() ? Optional.of(mapper.toDomain(page.getContent().get(0))) : Optional.empty();
    }

    public void softDelete(UUID id) {
        repository.softDelete(id);
    }

    public void updatePosition(UUID id, int position) {
        repository.updatePosition(id, position);
    }

    public int bulkCancelTasks(UUID projectId) {
        return repository.bulkCancelTasks(projectId);
    }
}

interface SpringDataAgileTaskRepository extends JpaRepository<AgileTaskJpaEntity, UUID> {
    Page<AgileTaskJpaEntity> findByProjectIdAndStatusNot(UUID projectId, String excludeStatus, Pageable pageable);
    
    Page<AgileTaskJpaEntity> findByProjectIdAndStatusNotIn(UUID projectId, java.util.List<String> statuses, Pageable pageable);
    
    Page<AgileTaskJpaEntity> findByProjectId(UUID projectId, Pageable pageable);

    long countByProjectIdAndStatusNotIn(UUID projectId, java.util.List<String> statuses);

    java.util.List<AgileTaskJpaEntity> findByStatus(String status);

    // Mock query para el portafolio (CA-7)
    @Query("SELECT t FROM AgileTaskJpaEntity t WHERE t.status NOT IN ('DONE', 'DELETED', 'CANCELLED')")
    java.util.List<AgileTaskJpaEntity> findPortfolioByOwner(@Param("owner") String owner);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")})
    @Query("SELECT t FROM AgileTaskJpaEntity t WHERE t.id = :id")
    Optional<AgileTaskJpaEntity> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")})
    @Query("SELECT t FROM AgileTaskJpaEntity t WHERE t.status = 'AVAILABLE' ORDER BY t.slaDeadline ASC")
    Page<AgileTaskJpaEntity> findNextAvailableTaskForUpdate(Pageable pageable);

    @Modifying
    @Query("UPDATE AgileTaskJpaEntity t SET t.status = 'DELETED' WHERE t.id = :id")
    void softDelete(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE AgileTaskJpaEntity t SET t.position = :position WHERE t.id = :id")
    void updatePosition(@Param("id") UUID id, @Param("position") int position);

    @Modifying
    @Query("UPDATE AgileTaskJpaEntity t SET t.status = 'CANCELLED' WHERE t.projectId = :projectId AND t.status NOT IN ('DONE', 'CANCELLED', 'DELETED')")
    int bulkCancelTasks(@Param("projectId") UUID projectId);
}
