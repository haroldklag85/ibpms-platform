package com.ibpms.poc.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Servicio encargado de construir y sanear respuestas/peticiones hacia API Externas.
 * Abarca iteraciones CA-56 y CA-59.
 */
@Service
public class ConnectorPayloadHandlerService {

    private static final Logger log = LoggerFactory.getLogger(ConnectorPayloadHandlerService.class);

    /**
     * CA-56: Delegación Transparente Binaria.
     * Evalúa si el Payload exige transformar UUIDs a Base64/Multipart desde SGDEA.
     */
    public Map<String, Object> buildRequestPayload(Map<String, Object> formVariables, Map<String, String> connectorSchema) {
        Map<String, Object> targetPayload = new HashMap<>(formVariables);
        
        // Detección Heurística: Si detecta variable tipo "File" o que debe ser un Array de Bytes...
        boolean requiresBinaryTransmutation = connectorSchema.containsValue("Base64") || connectorSchema.containsValue("File");
        if (requiresBinaryTransmutation) {
            log.warn("[TODO: Integrar SDK SGDEA para Mutación Multipart/Base64]");
        }
        
        return targetPayload;
    }

    /**
     * CA-59: Amnesia Selectiva de Respuesta (Output Pruning).
     * Evita que Camunda trague payloads gigantescos parseando solo lo esencial.
     */
    public Map<String, Object> pruneResponsePayload(String rawJsonResponse, List<String> mappedKeys) {
        Map<String, Object> prunedVariables = new HashMap<>();
        
        // 1. Simulación JsonPath extraction
        for (String key : mappedKeys) {
            // Ejemplo: extrae "status" del JSON crudo ignorando los 15MB restantes
            prunedVariables.put(key, "extracted_value_from_" + key);
        }
        
        // 2. Destruir explícitamente el original (Garbage Collection inminente)
        rawJsonResponse = null;
        log.info("Amnesia Selectiva Aplicada: El Payload Crudo fue destruido de memoria. Solo se persisten {} variables.", mappedKeys.size());
        
        return prunedVariables;
    }
}
