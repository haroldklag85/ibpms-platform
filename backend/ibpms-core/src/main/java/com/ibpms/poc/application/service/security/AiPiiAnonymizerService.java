package com.ibpms.poc.application.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CA-05: Enmascarador de Personal Identifiable Information (PII).
 * Evita la fuga LOPD/GDPR hacia servidores LLM externos.
 */
@Service
public class AiPiiAnonymizerService {

    private static final Logger log = LoggerFactory.getLogger(AiPiiAnonymizerService.class);

    // Detección simplificada de Teléfonos (Mínimo 8 dígitos consecutivos o separados por guiones)
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b(?:\\d\\s*[-.]?\\s*){8,15}\\d\\b");
    
    // Detección Cruda de E-Mails
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");

    /**
     * Reemplaza las huellas PII por alias anónimos previo a inyectar al Prompt.
     */
    public String maskSensitiveData(String incomingPrompt) {
        if (incomingPrompt == null) return null;

        String anonymized = incomingPrompt;
        
        Matcher phoneMatcher = PHONE_PATTERN.matcher(anonymized);
        if (phoneMatcher.find()) {
            anonymized = phoneMatcher.replaceAll("[TEL_ENMASCARADO]");
            log.debug("[APPSEC-PII] ☎️ Teléfono Ocultado en el payload NLP.");
        }

        Matcher emailMatcher = EMAIL_PATTERN.matcher(anonymized);
        if (emailMatcher.find()) {
            anonymized = emailMatcher.replaceAll("[EMAIL_ENMASCARADO]");
            log.debug("[APPSEC-PII] 📧 E-Mail Ocultado en el payload NLP.");
        }

        return anonymized;
    }
}
