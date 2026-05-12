package com.ibpms.poc.infrastructure.jpa.entity;

import com.ibpms.poc.crosscutting.annotations.Traceability;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ibpms_tenant_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Traceability(US = "US-004", CA = {"CA-18"})
public class TenantConfigEntity {

    @Id
    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "webhook_sla_hours", nullable = false)
    private Integer webhookSlaHours;
}
