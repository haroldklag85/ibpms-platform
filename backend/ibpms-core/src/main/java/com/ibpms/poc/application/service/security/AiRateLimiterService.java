package com.ibpms.poc.application.service.security;

import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CA-02: Rate Limiting Anti-DoW (Denial of Wallet).
 * Restringe la cantidad de llamadas generativas a la API LLM por Usuario.
 */
@Service
public class AiRateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(AiRateLimiterService.class);

    // Diccionario Atómico en Memoria para guardar el estado del Token Bucket por Usuario.
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    // Política Estricta: Máximo 5 Peticiones. Se recarga el cubo a razón de 5 por cada 1 Minuto.
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build();
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Consume un Token de Uso Prompteado.
     * @param userId Identificador del usuario (Extraído del SecurityContext -> JWT).
     * @return true si tiene saldo disponible y fue consumido, false si está castigado (Rate Limited).
     */
    public boolean tryConsumeToken(String userId) {
        Bucket bucket = cache.computeIfAbsent(userId, k -> createNewBucket());
        
        if (bucket.tryConsume(1)) {
            log.debug("[ANTI-DOW] Token LLM consumido para usuario {}. Espacio restante disponible.", userId);
            return true;
        } else {
            log.warn("[ANTI-DOW] RATE LIMIT EXCEDIDO ⚠️. Usuario {} ha agotado sus 5 peticiones por minuto. Bloqueando tráfico hacia OpenAI.", userId);
            return false;
        }
    }
}
