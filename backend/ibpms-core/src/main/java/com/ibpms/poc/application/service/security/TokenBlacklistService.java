package com.ibpms.poc.application.service.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.TokenBlacklistEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.TokenBlacklistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Service
@Transactional
public class TokenBlacklistService {

    private final TokenBlacklistRepository blacklistRepository;

    public TokenBlacklistService(TokenBlacklistRepository blacklistRepository) {
        this.blacklistRepository = blacklistRepository;
    }

    // @Traceability: Retro-Remediación ADR-001
    public void blacklistToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            TokenBlacklistEntity blackToken = new TokenBlacklistEntity(hexString.toString(), LocalDateTime.now().plusDays(1), null);
            blacklistRepository.save(blackToken);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error encriptando token para blacklist", e);
        }
    }
}
