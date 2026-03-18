package com.ibpms.poc.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Sanitizador de Inputs de UI antes de pasar al motor Camunda.
 */
@Service
public class FormFieldCleanserService {

    // Regex para Monedas: Quita símbolos como $, , y . (para guardarlo como
    // numérico en Camunda)
    // Asume que el usuario envía "$ 1.500"
    private static final Pattern CURRENCY_MASK = Pattern.compile("^\\$\\s?[\\d.,]+$");
    
    private final ObjectMapper objectMapper;

    public FormFieldCleanserService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void cleanseVariables(Map<String, Object> variables) {
        if (variables == null)
            return;

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            if (entry.getValue() instanceof String) {
                String value = (String) entry.getValue();

                // Detectar máscara de moneda simple para V1
                if (CURRENCY_MASK.matcher(value).matches()) {
                    String cleanNumeral = value.replaceAll("[^\\d]", "");
                    try {
                        Long num = Long.parseLong(cleanNumeral);
                        variables.put(entry.getKey(), num);
                    } catch (NumberFormatException ignored) {
                    }
                }

                // Otras máscaras de RUT / Teléfono pueden añadirse aquí...
            } else if (entry.getValue() instanceof List || entry.getValue() instanceof Map) {
                // CA-34: Serialización para Data Grids
                try {
                    variables.put(entry.getKey(), objectMapper.writeValueAsString(entry.getValue()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error parseando objeto anidado a JSON (CA-34)", e);
                }
            }
        }
    }
}
