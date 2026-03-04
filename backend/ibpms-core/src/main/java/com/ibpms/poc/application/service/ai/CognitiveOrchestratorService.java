package com.ibpms.poc.application.service.ai;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Sistema Nervioso Central Cognitivo.
 * Centraliza invocaciones LLM asegurando:
 * 1. Output en JSON estricto.
 * 2. Failover Automático entre proveedores (Ej. OpenAI -> Claude).
 * 3. Enmascaramiento Local Pre-vuelo.
 */
@Service
public class CognitiveOrchestratorService {

    private final DlpInterceptor dlpInterceptor;

    public CognitiveOrchestratorService(DlpInterceptor dlpInterceptor) {
        this.dlpInterceptor = dlpInterceptor;
    }

    /**
     * Orquesta la tarea generativa (CA-8, CA-31).
     */
    @CircuitBreaker(name = "primaryLlm", fallbackMethod = "fallbackToSecondaryLlm")
    public String executeGenerativeTask(String systemPrompt, Map<String, Object> executionContext) {
        // 1. DLP Masking (Redacción mecánica)
        String maskedPrompt = dlpInterceptor.redactSensibleData(systemPrompt);

        // 2. Pre-packaged Context (Impide que el LLM decida cuando/como hacer fetching)
        String packagedPayload = buildStrictJsonPayload(maskedPrompt, executionContext);

        // 3. Fallo primario dispara el CircuitBreaker automáticamente
        return invokePrimaryAzureOpenAi(packagedPayload);
    }

    /**
     * Método Fallback ejecutado por Resilience4j cuando `primaryLlm` falla.
     */
    public String fallbackToSecondaryLlm(String systemPrompt, Map<String, Object> executionContext, Throwable t) {
        String maskedPrompt = dlpInterceptor.redactSensibleData(systemPrompt);
        String packagedPayload = buildStrictJsonPayload(maskedPrompt, executionContext);

        return invokeSecondaryAwsBedrock(packagedPayload);
    }

    private String buildStrictJsonPayload(String prompt, Map<String, Object> context) {
        // Simulador de JSON Out (CA-1) obligando Strict JSON Format a nivel API de
        // OpenAI.
        return "{\"messages\": [{\"role\":\"system\",\"content\":\"" + prompt
                + "\"}], \"response_format\": {\"type\": \"json_object\"}}";
    }

    private String invokePrimaryAzureOpenAi(String payload) {
        // Llamada WebClient al API Primario
        return "{\"status\": \"success\", \"provider\": \"Primary-LLM-Azure\", \"response\": \"Analizado exitosamente.\"}";
    }

    private String invokeSecondaryAwsBedrock(String payload) {
        // Llamada WebClient al API Secundario (Fallback)
        return "{\"status\": \"success\", \"provider\": \"Fallback-LLM-AWS\", \"response\": \"Analizado exitosamente tras failover.\"}";
    }
}
