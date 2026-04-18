package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.agile.AgileProject;
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

    public AgileProjectRepositoryJpa(SpringDataAgileProjectRepository repository) {
        this.repository = repository;
    }

    public AgileProject save(AgileProject project) {
        return repository.save(project);
    }

    public Optional<AgileProject> findById(UUID id) {
        return repository.findById(id);
    }

    public Page<AgileProject> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public void updateStatus(UUID projectId, String status) {
        repository.updateStatus(projectId, status);
    }
}

interface SpringDataAgileProjectRepository extends JpaRepository<AgileProject, UUID> {
    @Modifying
    @Query("UPDATE AgileProject p SET p.status = :status, p.closedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") String status);
}

