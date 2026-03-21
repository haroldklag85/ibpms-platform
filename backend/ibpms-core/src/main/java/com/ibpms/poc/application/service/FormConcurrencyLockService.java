package com.ibpms.poc.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * GAP-4: Sistema de Bloqueo Concurrente Mutex para Formularios usando Redis.
 * Evita la sobre-escritura trágica entre analistas (Pestañas Múltiples o Colaboradores).
 */
@Service
public class FormConcurrencyLockService {

    private static final Logger log = LoggerFactory.getLogger(FormConcurrencyLockService.class);
    private final StringRedisTemplate redisTemplate;

    // Prefijo de la llave y expiración por inactividad
    private static final String LOCK_PREFIX = "ibpms:form:lock:";
    private static final Duration LOCK_TTL = Duration.ofMinutes(5);

    public FormConcurrencyLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Intenta reclamar un diseño de formulario.
     * Retorna verdadero si lo logra (o si ya era el dueño). Falso si está bloqueado por otro.
     */
    public boolean acquireLock(String formId, String userId) {
        String key = LOCK_PREFIX + formId;
        String currentOwner = redisTemplate.opsForValue().get(key);

        if (currentOwner == null) {
            redisTemplate.opsForValue().set(key, userId, LOCK_TTL);
            log.info("[SRE-MUTEX] Lock Adquirido. Form: {}, User: {}", formId, userId);
            return true;
        }

        if (currentOwner.equals(userId)) {
            // Renovar el lease time si es el mismo dueño haciendo ping
            redisTemplate.expire(key, LOCK_TTL);
            return true;
        }

        log.warn("[SRE-MUTEX] ⚠️ Conflicto Detectado. User {} intentó invadir Form {}, ya retenido por {}", userId, formId, currentOwner);
        return false;
    }

    /**
     * Libera el mutex explícitamente cuando el usuario sale de la pestaña.
     */
    public void releaseLock(String formId, String userId) {
        String key = LOCK_PREFIX + formId;
        String currentOwner = redisTemplate.opsForValue().get(key);

        if (userId.equals(currentOwner)) {
            redisTemplate.delete(key);
            log.info("[SRE-MUTEX] Lock Liberado exitosamente. Form: {}", formId);
        }
    }
}
