package com.ibpms.poc.infrastructure.jpa.repository.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.TokenBlacklistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklistEntity, UUID> {
    boolean existsByTokenSignature(String tokenSignature);
    void deleteByExpiresAtBefore(LocalDateTime now); // Para limpieza periódica cron
}
