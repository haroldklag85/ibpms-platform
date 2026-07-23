// @Traceability: US-005/US-036 - ADR-001, ADR-009
package com.ibpms.poc.infrastructure.jpa.entity.security;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;
import com.ibpms.poc.infrastructure.jpa.entity.bpmn.BpmnLaneEntity;

@Entity
@Table(name = "ibpms_lane_role_assignment")
public class LaneRoleAssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lane_id", nullable = false)
    private BpmnLaneEntity lane;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Column(name = "can_initiate", nullable = false)
    private Boolean canInitiate = false;

    @Column(name = "can_execute", nullable = false)
    private Boolean canExecute = true;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "assigned_by", length = 255)
    private String assignedBy;

    public LaneRoleAssignmentEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public BpmnLaneEntity getLane() { return lane; }
    public void setLane(BpmnLaneEntity lane) { this.lane = lane; }
    public RoleEntity getRole() { return role; }
    public void setRole(RoleEntity role) { this.role = role; }
    public Boolean getCanInitiate() { return canInitiate; }
    public void setCanInitiate(Boolean canInitiate) { this.canInitiate = canInitiate; }
    public Boolean getCanExecute() { return canExecute; }
    public void setCanExecute(Boolean canExecute) { this.canExecute = canExecute; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
}
