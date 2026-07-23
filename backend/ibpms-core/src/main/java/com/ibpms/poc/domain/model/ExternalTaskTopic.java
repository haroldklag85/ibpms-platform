package com.ibpms.poc.domain.model;

import java.time.LocalDateTime;

public class ExternalTaskTopic {
    private String topicName;
    private String description;
    private String workerClass;
    private Boolean isActive;
    private LocalDateTime registeredAt;

    public ExternalTaskTopic() {}

    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getWorkerClass() { return workerClass; }
    public void setWorkerClass(String workerClass) { this.workerClass = workerClass; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}
