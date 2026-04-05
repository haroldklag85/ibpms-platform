package com.ibpms.poc.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio Dummy para Invalidación de Sesiones (Blacklisting).
 * Atiende a la US-036 bajo la condición de que la US-038 es dueña de la integración final con Redis.
 */
@Service
public class JwtBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(JwtBlacklistService.class);
    
    // Fallback Dummy en memoria (HashMap) si US-038 no ha consolidado Redis
    private final ConcurrentHashMap<String, Long> dummyRedisBlacklist = new ConcurrentHashMap<>();

    /**
     * Inserta un ID de usuario o un JTI de sesión en la lista negra para forzar su desconexión (CA-21 / CA-14).
     * @param userId El identificador del usuario a revocar.
     */
    public void revokeSession(String userId) {
        log.warn("SUDO Action: Inyectando usuario [{}] en la Blacklist de Invalidación. (Dummy implementation).", userId);
        dummyRedisBlacklist.put(userId, System.currentTimeMillis());
    }

    public boolean isRevoked(String userId) {
        return dummyRedisBlacklist.containsKey(userId);
    }
}
