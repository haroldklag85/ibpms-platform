package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "ibpms_process_locks")
public class ProcessLockEntity {

    @Id
    @Column(name = "process_definition_key")
    private String processDefinitionKey;

    @Column(name = "locked_by", nullable = false)
    private String lockedBy;

    @Column(name = "locked_at", nullable = false)
    private LocalDateTime lockedAt;

    @Column(name = "browser_session_id")
    private String browserSessionId;

    public ProcessLockEntity() {
    }

    public ProcessLockEntity(String processDefinitionKey, String lockedBy, LocalDateTime lockedAt, String browserSessionId) {
        this.processDefinitionKey = processDefinitionKey;
        this.lockedBy = lockedBy;
        this.lockedAt = lockedAt;
        this.browserSessionId = browserSessionId;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    public String getBrowserSessionId() {
        return browserSessionId;
    }

    public void setBrowserSessionId(String browserSessionId) {
        this.browserSessionId = browserSessionId;
    }
}
