package com.ibpms.poc.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "sac_mailbox")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SacMailbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String alias;

    @Column(nullable = false)
    private String emailAddress;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String clientId;

    // ZERO-TRUST: We only store the Azure Key Vault Reference ID, NEVER the raw
    // Client Secret
    @Column(nullable = false)
    private String keyVaultReferenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MailboxProtocol protocol;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private String defaultBpmnProcessId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum MailboxProtocol {
        GRAPH, IMAP_DEPRECATED
    }
}
