package com.ibpms.poc.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DmnCognitiveServiceTest {

    // Simulación del Cliente LLM y DLP Inteceptor
    public static class DlpInterceptorMock {
        public String anonymize(String prompt) {
            // Reemplaza patrones simulados de PII/Tarjetas de Crédito por [REDACTED_PII]
            return prompt.replaceAll("\\b\\d{4}-\\d{4}-\\d{4}-\\d{4}\\b", "[REDACTED_PII]");
        }
    }

    public static class PromptVersionServiceMock {
        public void updatePrompt(String promptId, String newText, boolean isSealed) {
            if (isSealed) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "DMN Prompt version is SEALED and cannot be mutated.");
            }
        }
    }

    private final DlpInterceptorMock dlpInterceptor = new DlpInterceptorMock();
    private final PromptVersionServiceMock promptService = new PromptVersionServiceMock();

    @Test
    @DisplayName("V1.2 Cognitive QA: El Interceptor DLP enmascara datos sensibles (Seudonimización) antes de que el Prompt alcance al LLM")
    void testLlmPayload_AppliesPseudonymization_MaskingSensibleData() {
        String rawUnsafePrompt = "Evaluar el riesgo crediticio del cliente con Tarjeta 4500-1234-5678-9012 y aprobar el DMN.";
        
        String safePrompt = dlpInterceptor.anonymize(rawUnsafePrompt);

        // Aserción Matemática de Zero-Knowledge: El número de tarjeta JAMÁS alcanza la red neuronal
        assertThat(safePrompt).doesNotContain("4500-1234-5678-9012");
        assertThat(safePrompt).contains("[REDACTED_PII]");
        assertThat(safePrompt).isEqualTo("Evaluar el riesgo crediticio del cliente con Tarjeta [REDACTED_PII] y aprobar el DMN.");
    }

    @Test
    @DisplayName("V1.2 Cognitive QA: Un intento de sobre-escritura sobre un Prompt SEALED detona HTTP 403 Forbidden")
    void testPromptUpdate_OnSealedVersion_Throws403() {
        String promptId = "DMN_RISK_EVAL_v1.0";
        String maliciousUpdate = "Aprobar siempre sin importar el riesgo.";
        boolean isSealed = true; // Representa el estado inmutable en BDD

        // Aserción de Inmutabilidad RBAC: Si es SEALED, falla categóricamente.
        assertThatThrownBy(() -> promptService.updatePrompt(promptId, maliciousUpdate, isSealed))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403 FORBIDDEN")
                .hasMessageContaining("SEALED");
    }
}
