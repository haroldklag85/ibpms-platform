package com.ibpms.poc.infrastructure.jpa.entity.security;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_security_token_blacklist")
public class TokenBlacklistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "token_signature", nullable = false, unique = true, length = 128)
    private String tokenSignature;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    public TokenBlacklistEntity() {}

    public TokenBlacklistEntity(String tokenSignature, LocalDateTime expiresAt, UserEntity user) {
        this.tokenSignature = tokenSignature;
        this.expiresAt = expiresAt;
        this.user = user;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTokenSignature() { return tokenSignature; }
    public void setTokenSignature(String tokenSignature) { this.tokenSignature = tokenSignature; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
}
