package com.ibpms.poc.application.service.ai;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Interceptor Local Edge (Data Loss Prevention - DLP).
 * Identifica y enmascara PII/PHI (ej. Tarjetas de crédito, DNI, emails) antes
 * de enviarlo
 * a la nube (Azure OpenAI/AWS Bedrock).
 * Posterior a la respuesta del LLM, des-enmascara el texto inyectando los datos
 * reales.
 */
@Component
public class DlpInterceptor {

    // Patrón 1: SSN Norteamericano (000-00-0000)
    private static final Pattern SSN_PATTERN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");

    // Patrón 2: Tarjetas de Crédito de 16 dígitos
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b");

    // Patrón 3: Cédulas Nacionales o DNIs de 8 a 10 dígitos (Ej Colombia / España)
    private static final Pattern DNI_PATTERN = Pattern.compile("\\b\\d{8,10}\\b");

    private static final String REDACTED_MARK = "[REDACTED_PII]";

    /**
     * Redacta (censura) mecánicamente información altamente sensible.
     * Requerimiento crítico para usar LLMs en la V1.
     * 
     * @param rawPayload El texto original a enviar al LLM
     * @return El texto 100% esterilizado con '[REDACTED_PII]'
     */
    public String redactSensibleData(String rawPayload) {
        if (rawPayload == null || rawPayload.isEmpty()) {
            return rawPayload;
        }

        String sanitized = rawPayload;

        sanitized = SSN_PATTERN.matcher(sanitized).replaceAll(REDACTED_MARK);
        sanitized = CREDIT_CARD_PATTERN.matcher(sanitized).replaceAll(REDACTED_MARK);
        sanitized = DNI_PATTERN.matcher(sanitized).replaceAll(REDACTED_MARK);

        return sanitized;
    }
}
