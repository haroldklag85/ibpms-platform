package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entidad JPA - Routing de Anexos (CA-5 SGDEA).
 * Permite enlazar un componente 'file' de Vue directamente con
 * Series Documentales del SGDEA o URLs de SharePoint nativamente
 * sin escribir código adicional.
 */
@Entity
@Table(name = "ibpms_form_attachment_routing")
public class FormAttachmentRoutingEntity {

    public enum TargetSystem {
        SGDEA, SHAREPOINT
    }

    @Id
    private UUID id;

    @Column(name = "form_design_id", nullable = false)
    private UUID formDesignId;

    @Column(name = "component_id", nullable = false, length = 100)
    private String componentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_system", nullable = false, length = 20)
    private TargetSystem targetSystem;

    @Column(name = "target_config", columnDefinition = "JSON")
    private String targetConfig;

    public FormAttachmentRoutingEntity() {
        this.id = UUID.randomUUID();
    }

    public FormAttachmentRoutingEntity(UUID formDesignId, String componentId, TargetSystem targetSystem,
            String targetConfig) {
        this();
        this.formDesignId = formDesignId;
        this.componentId = componentId;
        this.targetSystem = targetSystem;
        this.targetConfig = targetConfig;
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public UUID getFormDesignId() {
        return formDesignId;
    }

    public String getComponentId() {
        return componentId;
    }

    public TargetSystem getTargetSystem() {
        return targetSystem;
    }

    public String getTargetConfig() {
        return targetConfig;
    }
}
