package com.ibpms.poc.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Servicio de Invalidación de Sesiones (Blacklisting).
 * Atiende a la US-036 y US-038 usando Redis para un Kill-Session efectivo.
 * Implementa política Fail-Open en caso de caída de la infraestructura de caché (CA-14).
 */
@com.ibpms.poc.crosscutting.annotations.Traceability(US = "US-036", CA = {"CA-14", "CA-21", "CA-25"})
@Service
public class JwtBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(JwtBlacklistService.class);
    private static final String USER_KEY_PREFIX = "blacklist:user:";
    private static final String TOKEN_KEY_PREFIX = "blacklist:token:";
    
    private final StringRedisTemplate redisTemplate;

    public JwtBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Inserta un JTI (o firma de token) en Redis con un TTL dinámico para revocar la sesión.
     */
    public void blacklistToken(String tokenIdentifier, long expirationSeconds) {
        log.warn("SUDO Action: Inyectando token/jti [{}] en la Blacklist de Invalidación (Redis).", tokenIdentifier);
        try {
            redisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + tokenIdentifier, "revoked", expirationSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error al persistir revocación de token en Redis.", e);
        }
    }

    /**
     * Valida si un token específico ha sido revocado.
     * Implementa FAIL-OPEN: Si Redis falla, se permite el paso.
     */
    public boolean isTokenRevoked(String tokenIdentifier) {
        if (tokenIdentifier == null) return false;
        try {
            Boolean hasKey = redisTemplate.hasKey(TOKEN_KEY_PREFIX + tokenIdentifier);
            return hasKey != null && hasKey;
        } catch (Exception e) {
            log.warn("Capa de Blacklist (Redis) inaccesible al verificar token. Aplicando política FAIL-OPEN.");
            return false;
        }
    }

    /**
     * Revoca todas las sesiones activas de un usuario inyectando su ID en la lista negra global.
     */
    public void revokeSession(String userId) {
        log.warn("SUDO Action: Exorcización. Revocando todas las sesiones para el usuario [{}]", userId);
        try {
            redisTemplate.opsForValue().set(USER_KEY_PREFIX + userId, "revoked", 24, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Error al persistir revocación de usuario en Redis para [{}].", userId, e);
        }
    }

    /**
     * Verifica si el usuario ha sido revocado globalmente (Kill-Switch).
     * Implementa FAIL-OPEN: Si Redis falla, se permite el paso.
     */
    public boolean isUserRevoked(String userId) {
        if (userId == null) return false;
        try {
            Boolean hasKey = redisTemplate.hasKey(USER_KEY_PREFIX + userId);
            return hasKey != null && hasKey;
        } catch (Exception e) {
            log.warn("Capa de Blacklist (Redis) inaccesible al verificar usuario [{}]. Aplicando política FAIL-OPEN.", userId);
            return false;
        }
    }
}
