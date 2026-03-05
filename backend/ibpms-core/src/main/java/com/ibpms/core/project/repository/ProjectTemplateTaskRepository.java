package com.ibpms.core.project.repository;

import com.ibpms.core.project.domain.ProjectTemplateTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTemplateTaskRepository extends JpaRepository<ProjectTemplateTask, String> {

    @Query("SELECT t FROM ProjectTemplateTask t JOIN t.milestone m JOIN m.phase p WHERE p.template.id = :templateId")
    List<ProjectTemplateTask> findAllTasksByTemplateId(String templateId);
}
