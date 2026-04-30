package com.ibpms.poc.infrastructure.bpm.scheduler;

import org.springframework.context.ApplicationEvent;

public class SlaAtRiskEvent extends ApplicationEvent {
    
    private final String taskId;
    private final String processInstanceId;

    public SlaAtRiskEvent(Object source, String taskId, String processInstanceId) {
        super(source);
        this.taskId = taskId;
        this.processInstanceId = processInstanceId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }
}
