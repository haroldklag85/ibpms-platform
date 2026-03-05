package com.ibpms.core.project.repository;

import com.ibpms.core.project.domain.ProjectTemplateDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTemplateDependencyRepository extends JpaRepository<ProjectTemplateDependency, String> {

    @Modifying
    @Query("DELETE FROM ProjectTemplateDependency d WHERE d.templateId = :templateId")
    void deleteByTemplateId(String templateId);

    List<ProjectTemplateDependency> findByTemplateId(String templateId);
}
