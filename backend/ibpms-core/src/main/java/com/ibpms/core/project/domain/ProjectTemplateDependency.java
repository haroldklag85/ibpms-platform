package com.ibpms.core.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "ib_project_template_dependency")
@Getter
@Setter
public class ProjectTemplateDependency {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private String id = UUID.randomUUID().toString();

    @Column(name = "template_id", nullable = false)
    private String templateId; // Para limpieza masiva rapida

    @Column(name = "source_task_id", nullable = false)
    private String sourceTaskId;

    @Column(name = "target_task_id", nullable = false)
    private String targetTaskId;

    @Column(name = "dependency_type", nullable = false)
    private String dependencyType = "FS"; // Finish-to-Start por defecto

    @Column(name = "lag_hours")
    private Integer lagHours = 0;
}
