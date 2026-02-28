package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "profile_bpmn_assignment")
public class ProfileBpmnAssignmentEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private IbpmsProfileEntity profile;

    @Column(name = "bpmn_process_key", nullable = false, length = 100)
    private String bpmnProcessKey;

    @Column(name = "bpmn_lane_id", nullable = false, length = 100)
    private String bpmnLaneId;

    public ProfileBpmnAssignmentEntity() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public IbpmsProfileEntity getProfile() {
        return profile;
    }

    public void setProfile(IbpmsProfileEntity profile) {
        this.profile = profile;
    }

    public String getBpmnProcessKey() {
        return bpmnProcessKey;
    }

    public void setBpmnProcessKey(String bpmnProcessKey) {
        this.bpmnProcessKey = bpmnProcessKey;
    }

    public String getBpmnLaneId() {
        return bpmnLaneId;
    }

    public void setBpmnLaneId(String bpmnLaneId) {
        this.bpmnLaneId = bpmnLaneId;
    }
}
