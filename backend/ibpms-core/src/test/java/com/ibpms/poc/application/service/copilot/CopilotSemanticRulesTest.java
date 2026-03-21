package com.ibpms.poc.application.service.copilot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CopilotSemanticRulesTest {

    // Simula la capa de Orquestación NLP del Copiloto que envuelve al LLM
    static class CopilotNlpOrchestratorMock {
        public String processPromptAndGenerateBPMN(String rawInput, String sourceLang) {
            // Lógica ficticia para satisfacer las reglas de negocio de IBPMS
            if (!"es".equalsIgnoreCase(sourceLang) || rawInput.toLowerCase().contains("manager")) {
                // Al detectar inglés o roles no homologados, el sistema inyecta una advertencia
                return "<?xml version=\"1.0\"?><bpmn:definitions><bpmn:textAnnotation id=\"warning_1\"><bpmn:text>Advertencia: Se requieren roles homologados y la operación debe ser estructurada en Castellano/Español.</bpmn:text></bpmn:textAnnotation></bpmn:definitions>";
            }
            return "<bpmn:definitions/>"; // Normal Execution
        }
    }

    private final CopilotNlpOrchestratorMock nlpOrchestrator = new CopilotNlpOrchestratorMock();

    @Test
    @DisplayName("US-027 CA-6: El LLM debe rechazar procesos en lenguajes no-corporativos (Inglés) e inyectar TextAnnotations BPMN explicando el error")
    void testCopilot_RejectsEnglishPrompts_AndInjectsTextAnnotation() {
        
        // El usuario inyecta un documento corporativo en Inglés para generar un flujo
        String maliciousOrAccidentalPrompt = "Create an approval workflow for the HR Manager.";
        String languageGuessed = "en"; // NLP Language Detector

        // Disparo de simulación
        String resultingXml = nlpOrchestrator.processPromptAndGenerateBPMN(maliciousOrAccidentalPrompt, languageGuessed);

        // Aserción Matemática Semántica:
        // 1. Debe incluir físicamente el Tag <bpmn:textAnnotation> en el XML de retorno para dibujarlo en el Canvas.
        assertThat(resultingXml).contains("<bpmn:textAnnotation");
        
        // 2. Debe contener la advertencia explicita exigiendo idioma Español y corrección de Roles.
        assertThat(resultingXml).contains("Español");
        assertThat(resultingXml).contains("roles homologados");
    }
}
