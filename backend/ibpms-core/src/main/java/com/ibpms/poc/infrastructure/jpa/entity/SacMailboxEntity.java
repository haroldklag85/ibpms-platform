package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sac_mailboxes")
@Data
@NoArgsConstructor
public class SacMailboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String alias;

    @Column(nullable = false)
    private String protocol; // e.g., "GRAPH", "IMAP" (IMAP will be deprecated/prevented from UI)

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    // SECURITY: Blueprint V1 enforces Client Secrets go to Azure Key Vault
    // Never store plain text secrets or raw encryption in DB.
    @Column(name = "key_vault_reference_id", nullable = false)
    private String keyVaultReferenceId;

    @Column(name = "default_bpmn_process_id", nullable = false)
    private String defaultBpmnProcessId;

    @Column(nullable = false)
    private boolean active = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
