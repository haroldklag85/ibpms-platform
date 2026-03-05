package com.ibpms.core.sac.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_sac_mailbox")
@Getter
@Setter
public class SacMailbox {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false, unique = true)
    private String alias;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "key_vault_ref_id", nullable = false)
    private String keyVaultReferenceId; // Stored instead of Client Secret

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MailboxProtocol protocol;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "default_bpmn_process", nullable = false)
    private String defaultBpmnProcessId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum MailboxProtocol {
        GRAPH,
        IMAP_DEPRECATED
    }
}
