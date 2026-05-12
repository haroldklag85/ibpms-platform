package com.ibpms.poc.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Servicio Integrado para Invalidación de Sesiones (Blacklisting).
 * Atiende a la US-036 y US-038 usando Redis para un Kill-Session efectivo.
 */
@com.ibpms.poc.crosscutting.annotations.Traceability(US = "US-036", CA = {"CA-21", "CA-25"})
@Service
public class JwtBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(JwtBlacklistService.class);
    
    private final StringRedisTemplate redisTemplate;

    public JwtBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Inserta un JTI (o hash de token) en Redis con un TTL para revocar la sesión.
     */
    public void blacklistToken(String tokenIdentifier, long expirationSeconds) {
        log.warn("SUDO Action: Inyectando token/jti [{}] en la Blacklist de Invalidación (Redis).", tokenIdentifier);
        redisTemplate.opsForValue().set("blacklist:token:" + tokenIdentifier, "revoked", expirationSeconds, TimeUnit.SECONDS);
    }

    public boolean isRevoked(String tokenIdentifier) {
        Boolean hasKey = redisTemplate.hasKey("blacklist:token:" + tokenIdentifier);
        return hasKey != null && hasKey;
    }

    /**
     * Revoca todas las sesiones activas de un usuario inyectando su ID en la lista negra global.
     */
    public void revokeSession(String userId) {
        log.warn("SUDO Action: Exorcización. Revocando todas las sesiones para el usuario [{}]", userId);
        redisTemplate.opsForValue().set("blacklist:user:" + userId, "revoked", 24, TimeUnit.HOURS);
    }

    /**
     * Verifica si el usuario ha sido revocado globalmente (Kill-Switch).
     */
    public boolean isUserRevoked(String userId) {
        Boolean hasKey = redisTemplate.hasKey("blacklist:user:" + userId);
        return hasKey != null && hasKey;
    }
}
