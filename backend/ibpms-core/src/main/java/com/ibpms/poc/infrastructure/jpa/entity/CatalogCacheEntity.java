package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_catalog_cache")
public class CatalogCacheEntity {

    @Id
    @Column(length = 100)
    private String id; // Ej. CRM_CUSTOMER_SEGMENTS

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload; // El JSON del CRM crudo

    @Column(nullable = false)
    private LocalDateTime lastSyncAt;

    protected CatalogCacheEntity() {
    }

    public CatalogCacheEntity(String id, String payload, LocalDateTime lastSyncAt) {
        this.id = id;
        this.payload = payload;
        this.lastSyncAt = lastSyncAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public LocalDateTime getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(LocalDateTime lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }
}
