package com.ibpms.poc.application.event;

public class SecurityAnomalyEvent {
    
    private final String type;
    private final String userId;
    private final String resourceId;
    
    public SecurityAnomalyEvent(String type, String userId, String resourceId) {
        this.type = type;
        this.userId = userId;
        this.resourceId = resourceId;
    }
    
    public String getType() { return type; }
    public String getUserId() { return userId; }
    public String getResourceId() { return resourceId; }
}
