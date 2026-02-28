package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "idp_group_mapping")
public class IdpGroupMappingEntity {

    @Id
    private UUID id;

    @Column(name = "idp_group_id", unique = true, nullable = false, length = 255)
    private String idpGroupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private IbpmsProfileEntity profile;

    public IdpGroupMappingEntity() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIdpGroupId() {
        return idpGroupId;
    }

    public void setIdpGroupId(String idpGroupId) {
        this.idpGroupId = idpGroupId;
    }

    public IbpmsProfileEntity getProfile() {
        return profile;
    }

    public void setProfile(IbpmsProfileEntity profile) {
        this.profile = profile;
    }
}
