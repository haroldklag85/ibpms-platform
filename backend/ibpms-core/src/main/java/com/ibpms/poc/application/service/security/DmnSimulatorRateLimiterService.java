package com.ibpms.poc.application.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * GAP-20: Rate Limiter para DMN Simulator (20 rq/min).
 * Utiliza Redis-based sliding window para consistencia multi-nodo.
 */
@Service
public class DmnSimulatorRateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(DmnSimulatorRateLimiterService.class);
    private final StringRedisTemplate redisTemplate;

    public DmnSimulatorRateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Valida si el usuario puede realizar una petición al simulador.
     * Sliding window básica usando ZSET.
     */
    public boolean tryConsumeToken(String userId) {
        String key = "rate_limit:simulator:" + userId;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - 60000; // 1 minuto

        try {
            // Eliminar registros más viejos de 1 minuto
            redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
            
            // Contar peticiones actuales en la ventana
            Long count = redisTemplate.opsForZSet().zCard(key);
            
            if (count != null && count >= 20) {
                log.warn("[RATE-LIMIT] Usuario {} ha excedido el límite del simulador DMN (Max 20/min).", userId);
                return false;
            }
            
            // Añadir petición actual
            redisTemplate.opsForZSet().add(key, String.valueOf(currentTime) + "-" + Math.random(), currentTime);
            redisTemplate.expire(key, Duration.ofMinutes(1));
            
            return true;
        } catch (Exception e) {
            log.error("[RATE-LIMIT] Falla en la validación de Redis para el usuario {}. Abierto por fail-open.", userId, e);
            return true; // Fail-open pattern
        }
    }
}
