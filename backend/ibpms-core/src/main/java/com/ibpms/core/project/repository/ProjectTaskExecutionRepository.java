package com.ibpms.core.project.repository;

import com.ibpms.core.project.domain.ProjectTaskExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTaskExecutionRepository extends JpaRepository<ProjectTaskExecution, String> {
    List<ProjectTaskExecution> findByProjectId(String projectId);
}
