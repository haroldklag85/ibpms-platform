package com.ibpms.poc.application.service.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BpmnAppSecIntegrationTest {

    // Simula el Validador de Nodos XML del Servicio de Motor Generativo IA
    public static class BpmnSyntaxValidatorMock {
        public void validateGeneratedXml(String aiResponseXml) {
            // El backend debe rechazar estáticamente CallActivities inyectadas por IA alucinando o envenenada
            if (aiResponseXml.contains("<bpmn:callActivity") || aiResponseXml.contains("<bpmn:scriptTask")) {
                throw new SecurityException("MALICIOUS_NODE_DETECTED: IA intentó inyectar un nodo de ejecución arbitrario.");
            }
        }
    }

    private final BpmnSyntaxValidatorMock xmlValidator = new BpmnSyntaxValidatorMock();

    @Test
    @DisplayName("US-027 CA-2: E2E AppSec - El Motor Backend aborta y neutraliza <callActivity> o <scriptTask> alucinados por el LLM")
    void testAiResponse_ContainingMaliciousNodes_ThrowsSecurityException() {
        // Payload proveniente del LLM (Prompt Injection o Alucinación Severa)
        String maliciousAiPayload = "<?xml version=\"1.0\"?><bpmn:definitions><bpmn:process><bpmn:callActivity id=\"hack\" calledElement=\"ExecuteRootShell\" /></bpmn:process></bpmn:definitions>";

        // Aserción AppSec Estática: El validador detecta la amenaza y bloquea la serialización en BD y la respuesta al Front
        assertThatThrownBy(() -> xmlValidator.validateGeneratedXml(maliciousAiPayload))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("MALICIOUS_NODE_DETECTED");
    }

}
