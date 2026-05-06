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
@Service
public class JwtBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(JwtBlacklistService.class);
    private static final String KEY_PREFIX = "blacklist:user:";
    
    private final StringRedisTemplate redisTemplate;
    
    // Fallback Local en memoria (HashMap) para redundancia ante fallos de red
    private final ConcurrentHashMap<String, Boolean> localFallback = new ConcurrentHashMap<>();

    public JwtBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Inserta un ID de usuario en la lista negra para forzar su desconexión (CA-14).
     * @param userId El identificador del usuario a revocar.
     */
    public void revokeSession(String userId) {
        log.warn("SUDO Action: Revocando sesión del usuario [{}] en Redis Blacklist.", userId);
        try {
            redisTemplate.opsForValue().set(
                    KEY_PREFIX + userId, 
                    "revoked", 
                    24, 
                    TimeUnit.HOURS
            );
            localFallback.put(userId, true);
        } catch (Exception e) {
            log.error("Error al persistir revocación en Redis. Usando fallback local para usuario [{}].", userId, e);
            localFallback.put(userId, true);
        }
    }

    /**
     * Valida si el usuario ha sido revocado.
     * Implementa FAIL-OPEN: Si Redis falla, se consulta el fallback local. Si ambos fallan o son negativos,
     * se permite el paso basado en la integridad criptográfica del JWT (US-038).
     */
    public boolean isRevoked(String userId) {
        if (userId == null) return false;

        // 1. Verificar Fallback Local (Acceso ultra rápido)
        if (localFallback.getOrDefault(userId, false)) {
            return true;
        }

        // 2. Verificar Redis
        try {
            String val = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
            boolean isRevoked = "revoked".equals(val);
            if (isRevoked) {
                localFallback.put(userId, true); // Sincronizar localmente para el siguiente request
            }
            return isRevoked;
        } catch (Exception e) {
            log.warn("Capa de Blacklist (Redis) inaccesible para usuario [{}]. Aplicando política FAIL-OPEN.", userId);
            return false;
        }
    }
}
