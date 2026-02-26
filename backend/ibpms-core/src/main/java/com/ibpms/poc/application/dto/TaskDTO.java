package com.ibpms.poc.application.dto;

import java.util.List;

public class TaskDTO {
    private String id;
    private String name;
    private String caseId;
    private String assignee;
    private List<String> candidateGroups;
    private Integer priority;
    private String dueDate;
    private String createdAt;

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getAssignee() {
        return assignee;
    }

    public List<String> getCandidateGroups() {
        return candidateGroups;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public void setCandidateGroups(List<String> candidateGroups) {
        this.candidateGroups = candidateGroups;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
