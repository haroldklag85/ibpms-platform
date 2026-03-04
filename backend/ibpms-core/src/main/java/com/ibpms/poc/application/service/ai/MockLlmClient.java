package com.ibpms.poc.application.service.ai;

import org.springframework.stereotype.Service;

@Service
public class MockLlmClient {

    private final DlpInterceptor dlpInterceptor;

    public MockLlmClient(DlpInterceptor dlpInterceptor) {
        this.dlpInterceptor = dlpInterceptor;
    }

    /**
     * Simula la invocación a la red REST de un modelo externo (ej. OpenAI,
     * Anthropic).
     */
    public String infer(String promptTemplateId, String rawInputData) {

        // 1. Interceptar los datos salientes y aplicar CERO-FUGA PII
        String sanitizedInput = dlpInterceptor.redactSensibleData(rawInputData);

        // 2. Aquí iría el HTTP POST real hacia la API del LLM publico
        // restTemplate.postForEntity(url, sanitizedInput, String.class);

        return "Respuesta simulada generada por IA basándose de forma segura en: " + sanitizedInput;
    }
}
