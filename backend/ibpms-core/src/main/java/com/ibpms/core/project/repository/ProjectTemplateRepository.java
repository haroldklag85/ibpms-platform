package com.ibpms.core.project.repository;

import com.ibpms.core.project.domain.ProjectTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectTemplateRepository extends JpaRepository<ProjectTemplate, String> {
}
