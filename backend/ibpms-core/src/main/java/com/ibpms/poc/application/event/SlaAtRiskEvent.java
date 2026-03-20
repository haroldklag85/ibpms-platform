package com.ibpms.poc.application.event;

public class SlaAtRiskEvent {

    private final String processInstanceId;
    private final String executionId;
    private final String activityId;

    public SlaAtRiskEvent(String processInstanceId, String executionId, String activityId) {
        this.processInstanceId = processInstanceId;
        this.executionId = executionId;
        this.activityId = activityId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getActivityId() {
        return activityId;
    }
}
