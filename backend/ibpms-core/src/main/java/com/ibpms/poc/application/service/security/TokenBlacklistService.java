package com.ibpms.poc.application.service.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.TokenBlacklistEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.TokenBlacklistRepository;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

/**
 * Servicio de Aplicación para Gestión de Lista Negra de Tokens JWT.
 * 
 * @Traceability(US = "US-038", CA = {"CA-02"})
 */
@Service
@Transactional
public class TokenBlacklistService {

    private final TokenBlacklistRepository blacklistRepository;

    public TokenBlacklistService(TokenBlacklistRepository blacklistRepository) {
        this.blacklistRepository = blacklistRepository;
    }

    /**
     * Añade un token a la lista negra si no existe previamente.
     * @param token Token a bloquear.
     */
    // @Traceability: US-038 - CA-02 (ADR-001 Refactor)
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
