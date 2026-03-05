package com.ibpms.core.project.repository;

import com.ibpms.core.project.domain.ProjectBaseline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectBaselineRepository extends JpaRepository<ProjectBaseline, String> {
    Optional<ProjectBaseline> findByProjectIdAndIsActiveTrue(String projectId);
}
