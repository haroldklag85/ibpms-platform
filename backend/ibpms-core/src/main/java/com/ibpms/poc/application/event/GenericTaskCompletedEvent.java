package com.ibpms.poc.application.event;

import java.util.Map;

public class GenericTaskCompletedEvent {
    private final String taskId;
    private final String processInstanceId;
    private final String userId;
    private final Map<String, Object> submittedVariables;

    public GenericTaskCompletedEvent(String taskId, String processInstanceId, String userId, Map<String, Object> submittedVariables) {
        this.taskId = taskId;
        this.processInstanceId = processInstanceId;
        this.userId = userId;
        this.submittedVariables = submittedVariables;
    }

    public String getTaskId() { return taskId; }
    public String getProcessInstanceId() { return processInstanceId; }
    public String getUserId() { return userId; }
    public Map<String, Object> getSubmittedVariables() { return submittedVariables; }
}
