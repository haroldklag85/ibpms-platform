package com.ibpms.poc.application.service;

import org.springframework.stereotype.Service;

@Service
public class PromptNormalizer {

    public String normalize(String prompt) {
        if (prompt == null) {
            return "";
        }
        return prompt.toLowerCase()
                     .trim()
                     .replaceAll("\\s+", " ")
                     .replaceAll("[\\.,]$", "")
                     .replace(",", ""); // Elimina comas internas y puntuación final irrelevante
    }
}
