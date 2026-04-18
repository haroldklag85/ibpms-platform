package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "ibpms_external_task_topics")
public class ExternalTaskTopicEntity {

    @Id
    @Column(name = "topic_name", nullable = false)
    private String topicName;

    @Column(name = "description")
    private String description;

    @Column(name = "worker_class")
    private String workerClass;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    public ExternalTaskTopicEntity() {
        this.registeredAt = LocalDateTime.now();
    }

    public ExternalTaskTopicEntity(String topicName, String description, String workerClass) {
        this();
        this.topicName = topicName;
        this.description = description;
        this.workerClass = workerClass;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWorkerClass() {
        return workerClass;
    }

    public void setWorkerClass(String workerClass) {
        this.workerClass = workerClass;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}
