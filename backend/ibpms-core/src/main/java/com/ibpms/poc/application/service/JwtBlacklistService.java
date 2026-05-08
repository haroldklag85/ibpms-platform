package com.ibpms.poc.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Servicio de Invalidación de Sesiones (Blacklisting) (CA-14).
 * Integra Redis para revocación inmediata y persistente.
 * Implementa política Fail-Open en caso de caída de la infraestructura de caché.
 */
// @Traceability: US-036 - CA-25
// @Traceability: US-036 - CA-21 Infraestructura de Blacklist JWT para Kill-Session
@com.ibpms.poc.crosscutting.annotations.Traceability(US = "US-036", CA = {"CA-21", "CA-25"})
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
     * Inserta un ID de usuario en la lista negra para forzar su desconexión (CA-14).
     * @param userId El identificador del usuario a revocar.
     */
    public void revokeSession(String userId) {
        log.warn("SUDO Action: Revocando sesión completa del usuario [{}] en Redis Blacklist.", userId);
        try {
            redisTemplate.opsForValue().set(
                    USER_KEY_PREFIX + userId, 
                    "revoked", 
                    24, 
                    TimeUnit.HOURS
            );
        } catch (Exception e) {
            log.error("Error al persistir revocación de usuario en Redis para [{}].", userId, e);
        }
    }

    /**
     * Inserta la firma de un token específico en la lista negra.
     * @param tokenSignature La firma SHA-256 del token a revocar.
     */
    public void revokeToken(String tokenSignature) {
        log.warn("SUDO Action: Revocando token específico en Redis Blacklist.");
        try {
            redisTemplate.opsForValue().set(
                    TOKEN_KEY_PREFIX + tokenSignature, 
                    "revoked", 
                    24, 
                    TimeUnit.HOURS
            );
        } catch (Exception e) {
            log.error("Error al persistir revocación de token en Redis.", e);
        }
    }

    /**
     * Valida si la sesión del usuario ha sido revocada.
     * Implementa FAIL-OPEN: Si Redis falla, se permite el paso.
     */
    public boolean isUserRevoked(String userId) {
        if (userId == null) return false;
        try {
            return "revoked".equals(redisTemplate.opsForValue().get(USER_KEY_PREFIX + userId));
        } catch (Exception e) {
            log.warn("Capa de Blacklist (Redis) inaccesible al verificar usuario [{}]. Aplicando política FAIL-OPEN.", userId);
            return false;
        }
    }

    /**
     * Valida si un token específico ha sido revocado.
     * Implementa FAIL-OPEN: Si Redis falla, se permite el paso.
     */
    public boolean isTokenRevoked(String tokenSignature) {
        if (tokenSignature == null) return false;
        try {
            return "revoked".equals(redisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + tokenSignature));
        } catch (Exception e) {
            log.warn("Capa de Blacklist (Redis) inaccesible al verificar token. Aplicando política FAIL-OPEN.");
            return false;
        }
    }
}
