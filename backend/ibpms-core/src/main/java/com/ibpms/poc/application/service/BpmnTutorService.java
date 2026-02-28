package com.ibpms.poc.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.dmn.NlpAgentPort;
import com.ibpms.poc.application.port.in.BpmnTutorUseCase;
import com.ibpms.poc.infrastructure.web.dto.BpmnAnalysisResultDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class BpmnTutorService implements BpmnTutorUseCase {

    private final NlpAgentPort nlpAgentPort;
    private final ObjectMapper objectMapper;

    public BpmnTutorService(NlpAgentPort nlpAgentPort, ObjectMapper objectMapper) {
        this.nlpAgentPort = nlpAgentPort;
        this.objectMapper = objectMapper;
    }

    @Override
    public BpmnAnalysisResultDTO evaluateProcess(String xmlPayload, boolean strictMode) {
        // Enviar al LLM
        String jsonResponse = nlpAgentPort.analyzeBpmnSemantics(xmlPayload, strictMode);

        try {
            // Deserializar el JSON exacto a nuestro DTO
            return objectMapper.readValue(jsonResponse, BpmnAnalysisResultDTO.class);
        } catch (JsonProcessingException e) {
            System.err.println("Error deserializando la respuesta LLM: " + e.getMessage());

            // Circuit Breaker lógico para evitar que explote la Vista
            BpmnAnalysisResultDTO fallback = new BpmnAnalysisResultDTO();
            fallback.setCriticalIsoViolations(new ArrayList<>());
            fallback.setStructuralAntiPatterns(new ArrayList<>());
            fallback.setAutomationOpportunities(new ArrayList<>());
            fallback.setInternalScore1To100(0);
            return fallback;
        }
    }
}
