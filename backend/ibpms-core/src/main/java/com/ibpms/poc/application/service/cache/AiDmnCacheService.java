package com.ibpms.poc.application.service.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CA-02: Caché Criptográfica (SHA-256) Semántica para Ahorro Táctico DMN.
 * Evade costos HTTP a OpenAI si el Prompt y Diccionario exacto ya fue calculado.
 */
@Service
public class AiDmnCacheService {

    private static final Logger log = LoggerFactory.getLogger(AiDmnCacheService.class);

    // Mock Memory Cache (En V1.2 real se inyecta StringRedisTemplate apuntando al puerto 6379).
    private final Map<String, String> redisMockCache = new ConcurrentHashMap<>();

    public String generateSha256Hash(String prompt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(prompt.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Entorno carece de soporte criptográfico SHA-256", e);
        }
    }

    /**
     * @return El XML pre-generado si hay Match exacto, o null si es Miss.
     */
    public String checkCacheHit(String userPrompt) {
        String hashKey = generateSha256Hash(userPrompt);
        if (redisMockCache.containsKey(hashKey)) {
            log.info("[SRE-CACHE] ⚡ CACHE HIT (Hash: {}). Retornando DMN clonado en milisegundos. Costo 0 Tokens Cloud.", hashKey.substring(0, 8));
            return redisMockCache.get(hashKey);
        }
        log.debug("[SRE-CACHE] Cache Miss para Hash {}. Procediendo a delegar red hacia LLM.", hashKey.substring(0, 8));
        return null;
    }

    public void putCache(String userPrompt, String generatedDmnXml) {
        String hashKey = generateSha256Hash(userPrompt);
        redisMockCache.put(hashKey, generatedDmnXml);
        log.debug("[SRE-CACHE] DMN inyectado a Redis con llave {}.", hashKey.substring(0, 8));
    }
}
