package com.ibpms.poc.infrastructure.jpa.entity.security;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_security_delegation")
public class DelegationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegator_id", nullable = false)
    private UserEntity delegator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitute_id", nullable = false)
    private UserEntity substitute;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public DelegationEntity() {}

    public DelegationEntity(UserEntity delegator, UserEntity substitute, LocalDateTime startDate, LocalDateTime endDate) {
        this.delegator = delegator;
        this.substitute = substitute;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UserEntity getDelegator() { return delegator; }
    public void setDelegator(UserEntity delegator) { this.delegator = delegator; }
    public UserEntity getSubstitute() { return substitute; }
    public void setSubstitute(UserEntity substitute) { this.substitute = substitute; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public boolean isCurrentlyValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && !now.isBefore(startDate) && !now.isAfter(endDate);
    }
}
