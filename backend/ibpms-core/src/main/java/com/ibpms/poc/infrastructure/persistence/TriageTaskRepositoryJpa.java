package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.TriageTask;
import com.ibpms.poc.domain.port.TriageTaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TriageTaskRepositoryJpa implements TriageTaskRepository {

    private final SpringDataTriageTaskRepository repository;

    public TriageTaskRepositoryJpa(SpringDataTriageTaskRepository repository) {
        this.repository = repository;
    }

    @Override
    public TriageTask save(TriageTask task) {
        return repository.save(task);
    }

    @Override
    public Optional<TriageTask> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Page<TriageTask> findByStatus(String status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }

    @Override
    public void deleteByStatusAndUpdatedAtBefore(String status, ZonedDateTime cutoff) {
        repository.deleteOldRecords(status, cutoff);
    }

    interface SpringDataTriageTaskRepository extends JpaRepository<TriageTask, UUID> {
        Page<TriageTask> findByStatus(String status, Pageable pageable);

        @Modifying
        @Query("DELETE FROM TriageTask t WHERE t.status = :status AND t.updatedAt < :cutoff")
        void deleteOldRecords(@Param("status") String status, @Param("cutoff") ZonedDateTime cutoff);
    }
}
