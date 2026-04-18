package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.agile.AgileTask;
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

    public AgileTaskRepositoryJpa(SpringDataAgileTaskRepository repository) {
        this.repository = repository;
    }

    public AgileTask save(AgileTask task) {
        return repository.save(task);
    }

    public Optional<AgileTask> findById(UUID id) {
        return repository.findById(id);
    }

    public Optional<AgileTask> findByIdForUpdate(UUID id) {
        return repository.findByIdForUpdate(id);
    }

    public Page<AgileTask> findByProjectIdAndStatusNot(UUID projectId, String excludeStatus, Pageable pageable) {
        return repository.findByProjectIdAndStatusNot(projectId, excludeStatus, pageable);
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

interface SpringDataAgileTaskRepository extends JpaRepository<AgileTask, UUID> {
    Page<AgileTask> findByProjectIdAndStatusNot(UUID projectId, String excludeStatus, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")})
    @Query("SELECT t FROM AgileTask t WHERE t.id = :id")
    Optional<AgileTask> findByIdForUpdate(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE AgileTask t SET t.status = 'DELETED' WHERE t.id = :id")
    void softDelete(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE AgileTask t SET t.position = :position WHERE t.id = :id")
    void updatePosition(@Param("id") UUID id, @Param("position") int position);

    @Modifying
    @Query("UPDATE AgileTask t SET t.status = 'CANCELLED' WHERE t.projectId = :projectId AND t.status NOT IN ('DONE', 'CANCELLED', 'DELETED')")
    int bulkCancelTasks(@Param("projectId") UUID projectId);
}

