package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.DmnTranslationRequestDTO;
import com.ibpms.poc.application.dto.DmnTranslationResponseDTO;
import com.ibpms.poc.application.port.in.GenerarDmnAiUseCase;
import com.ibpms.poc.application.port.out.NlpAgentPort;
import org.springframework.stereotype.Service;

@Service
public class GenerarDmnAiService implements GenerarDmnAiUseCase {

    private final NlpAgentPort nlpAgentPort;

    public GenerarDmnAiService(NlpAgentPort nlpAgentPort) {
        this.nlpAgentPort = nlpAgentPort;
    }

    @Override
    public DmnTranslationResponseDTO generarDesdeTexto(DmnTranslationRequestDTO request) {
        if (request == null || request.getPrompt() == null
                || request.getPrompt().trim().isEmpty()) {
            throw new IllegalArgumentException("La regla de negocio en lenguaje natural es obligatoria.");
        }

        // Delegar la generación al puerto del LLM (Ej. OpenAI o Claude)
        String dmnXml = nlpAgentPort.generateDmnXml(request.getPrompt());

        // Limpiamos la salida por si el LLM envuelve la respuesta en Backticks a pesar
        // del System Prompt
        if (dmnXml != null && dmnXml.startsWith("```xml")) {
            dmnXml = dmnXml.substring(6);
            if (dmnXml.endsWith("```")) {
                dmnXml = dmnXml.substring(0, dmnXml.length() - 3);
            }
        }

        return new DmnTranslationResponseDTO(dmnXml.trim());
    }
}
