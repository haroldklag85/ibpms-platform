package com.ibpms.core.project.repository;

import com.ibpms.core.project.domain.ProjectTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ProjectTemplateRepository extends JpaRepository<ProjectTemplate, String> {
}
