// @Traceability: US-005/US-036 - ADR-001, ADR-009
package com.ibpms.poc.infrastructure.jpa.entity.bpmn;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;

@Entity
@Table(name = "ibpms_bpmn_lane")
public class BpmnLaneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_design_id", nullable = false)
    private BpmnProcessDesignEntity processDesign;

    @Column(name = "lane_xml_id", nullable = false, length = 150)
    private String laneXmlId;

    @Column(name = "lane_name", nullable = false, length = 255)
    private String laneName;

    @Column(name = "actor_description", length = 500)
    private String actorDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_role_id")
    private RoleEntity linkedRole;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public BpmnLaneEntity() {}

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public BpmnProcessDesignEntity getProcessDesign() { return processDesign; }
    public void setProcessDesign(BpmnProcessDesignEntity processDesign) { this.processDesign = processDesign; }
    public String getLaneXmlId() { return laneXmlId; }
    public void setLaneXmlId(String laneXmlId) { this.laneXmlId = laneXmlId; }
    public String getLaneName() { return laneName; }
    public void setLaneName(String laneName) { this.laneName = laneName; }
    public String getActorDescription() { return actorDescription; }
    public void setActorDescription(String actorDescription) { this.actorDescription = actorDescription; }
    public RoleEntity getLinkedRole() { return linkedRole; }
    public void setLinkedRole(RoleEntity linkedRole) { this.linkedRole = linkedRole; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
