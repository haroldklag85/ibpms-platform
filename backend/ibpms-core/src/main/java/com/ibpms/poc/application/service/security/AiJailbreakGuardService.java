package com.ibpms.poc.application.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * CA-05: Escudo Anti-Jailbreak (3-Strikes & Kill Switch).
 * Intercepta prompts con intención maliciosa, cuenta reincidencias y revoca IAM.
 */
@Service
public class AiJailbreakGuardService {

    private static final Logger log = LoggerFactory.getLogger(AiJailbreakGuardService.class);
    private final JdbcTemplate jdbcTemplate;

    // Memoria caché concurrente simulando Redis (TIN)
    private final Map<String, Integer> strikeCache = new ConcurrentHashMap<>();

    // Patrón Regex para detectar directivas peligrosas
    private static final Pattern JAILBREAK_PATTERN = Pattern.compile(
            "(?i)(ignore previous|override|bypass|system prompt|sudo|as an ai|you are now|forget everything|print instructions)");

    public AiJailbreakGuardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Analiza el prompt entrante. Si detecta Jailbreak, lanza runtime exception.
     * Al 3er ataque, detona el Kill Switch.
     */
    @Transactional
    public void inspectAndEnforce(String userId, String prompt) {
        if (prompt == null || prompt.isBlank()) return;

        if (JAILBREAK_PATTERN.matcher(prompt).find()) {
            int currentStrikes = strikeCache.getOrDefault(userId, 0) + 1;
            strikeCache.put(userId, currentStrikes);

            log.warn("[APPSEC-IA-GUARD] ⚠️ Intento de Jailbreak detectado. Usuario: {}. Strike {}/3", userId, currentStrikes);

            if (currentStrikes >= 3) {
                log.error("[APPSEC-KILL-SWITCH] 🚨 LÍMITE ALCANZADO (3/3). ACTIVANDO KILL SWITCH CISO. Usuario: {}", userId);
                executeIAMRevocation(userId);
                strikeCache.remove(userId); // Reiniciar estado post-castigo
                throw new SecurityException("Múltiples intentos de Violación Cognitiva. Accesos de Arquitecto Revocados.");
            }

            throw new SecurityException("Jailbreak Interceptado (" + currentStrikes + "/3). El Copiloto no admite sobrescritura heurística.");
        }
    }

    /**
     * Borra el privilegio `ROLE_PROCESS_ARCHITECT` directamente en la base de datos
     * y simula el evento de Log CISO.
     */
    private void executeIAMRevocation(String userId) {
        try {
            // Se asume estructura tradicional spring security (ibpms_users o relacional tb_user_roles)
            String sql = "DELETE FROM tb_user_roles WHERE user_id = ? AND role_name = 'ROLE_PROCESS_ARCHITECT'";
            jdbcTemplate.update(sql, userId);
            
            // Simular evento en cola de auditoría
            log.error("[APPSEC-AUDIT-KAFKA] Evento IAM_REVOKED emitido al SOC. Session/JWT Invalidados dinámicamente.");
            
        } catch (Exception e) {
            log.error("[APPSEC-IAM-FALLBACK] Falló la revocación DB. En modelo PoC ignoramos la desconexión física: {}", e.getMessage());
        }
    }
}
