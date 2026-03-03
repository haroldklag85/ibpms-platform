package com.ibpms.poc.infrastructure.jpa.entity.integration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA - Conector de API reutilizable (Pantalla 8).
 * Base de la arquitectura de integraciones asíncronas seguras.
 */
@Entity
@Table(name = "ibpms_api_connector")
public class ApiConnectorEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "system_code", nullable = false, unique = true, length = 100)
    private String systemCode;

    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;

    @Column(name = "http_method", nullable = false, length = 20)
    private String httpMethod;

    @Column(name = "default_headers", columnDefinition = "JSON")
    private String defaultHeaders;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "is_cached", nullable = false)
    private boolean isCached;

    @Column(name = "cache_ttl_minutes")
    private Integer cacheTtlMinutes;

    @Column(name = "auth_config", columnDefinition = "JSON")
    private String authConfig;

    @Column(name = "pgp_public_key", columnDefinition = "TEXT")
    private String pgpPublicKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ApiConnectorEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters y Setters ---

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getDefaultHeaders() {
        return defaultHeaders;
    }

    public void setDefaultHeaders(String defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isCached() {
        return isCached;
    }

    public void setCached(boolean isCached) {
        this.isCached = isCached;
    }

    public Integer getCacheTtlMinutes() {
        return cacheTtlMinutes;
    }

    public void setCacheTtlMinutes(Integer cacheTtlMinutes) {
        this.cacheTtlMinutes = cacheTtlMinutes;
    }

    public String getAuthConfig() {
        return authConfig;
    }

    public void setAuthConfig(String authConfig) {
        this.authConfig = authConfig;
    }

    public String getPgpPublicKey() {
        return pgpPublicKey;
    }

    public void setPgpPublicKey(String pgpPublicKey) {
        this.pgpPublicKey = pgpPublicKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
