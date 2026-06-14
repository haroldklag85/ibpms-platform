package com.ibpms.poc.application.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PromptPiiScrubber {

    // Lista heurística de nombres de variables PII según diccionario Zod
    private static final String[] PII_VARIABLE_KEYWORDS = {
        "dni", "nombre", "apellido", "email", "telefono", "direccion", "ssn", "identificacion", "cliente_dni", "nombre_completo", "correo"
    };

    public static class ScrubResult {
        private final String scrubbedPrompt;
        private final Map<String, String> reverseDictionary;

        public ScrubResult(String scrubbedPrompt, Map<String, String> reverseDictionary) {
            this.scrubbedPrompt = scrubbedPrompt;
            this.reverseDictionary = reverseDictionary;
        }

        public String getScrubbedPrompt() { return scrubbedPrompt; }
        public Map<String, String> getReverseDictionary() { return reverseDictionary; }
    }

    public ScrubResult scrub(String prompt) {
        if (prompt == null) return new ScrubResult("", new HashMap<>());

        String scrubbed = prompt;
        Map<String, String> reverseDict = new HashMap<>();
        int varCounter = 1;

        for (String keyword : PII_VARIABLE_KEYWORDS) {
            // Buscamos la palabra completa, sin ser parte de otra palabra
            Pattern pattern = Pattern.compile("\\b" + keyword + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(scrubbed);
            
            while (matcher.find()) {
                String match = matcher.group();
                String alias = "var_" + varCounter++;
                reverseDict.put(alias, match);
                scrubbed = scrubbed.replaceFirst("(?i)\\b" + keyword + "\\b", alias);
            }
        }
        
        return new ScrubResult(scrubbed, reverseDict);
    }

    public String restore(String generatedXml, Map<String, String> reverseDictionary) {
        if (generatedXml == null || reverseDictionary == null || reverseDictionary.isEmpty()) {
            return generatedXml;
        }
        
        String restored = generatedXml;
        for (Map.Entry<String, String> entry : reverseDictionary.entrySet()) {
            // Se reemplazan los alias generados por sus nombres originales
            restored = restored.replace(entry.getKey(), entry.getValue());
        }
        return restored;
    }
}
