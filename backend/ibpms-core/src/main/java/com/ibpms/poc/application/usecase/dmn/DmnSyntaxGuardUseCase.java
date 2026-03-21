package com.ibpms.poc.application.usecase.dmn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CA-08 / CA-09: Guardrails Sintáctos (Filtro Anti-Token y Escudo Lógico DMN).
 * Valida la castidad de las reglas antes y después de interactuar con el LLM.
 */
@Service
public class DmnSyntaxGuardUseCase {

    private static final Logger log = LoggerFactory.getLogger(DmnSyntaxGuardUseCase.class);

    // CA-08: Bloqueo de Dot Notation
    private static final Pattern DOT_NOTATION_PATTERN = Pattern.compile("[a-zA-Z_]+\\.[a-zA-Z_]+");

    /**
     * CA-09: Previene Limite de Tokens
     * Trunca brutalmente el Prompt Humano a 1000 caracteres antes de encolarlo a OpenAI.
     */
    public String validateAndTruncatePrompt(String humanPrompt) {
        if (humanPrompt == null) return "";
        if (humanPrompt.length() > 1000) {
            log.warn("[GUARDRAIL] Prompt Humano truncado (>1000 chars). Previniendo Token Limit Exhaustion.");
            return humanPrompt.substring(0, 1000);
        }
        return humanPrompt;
    }

    /**
     * CA-08 y CA-09: Chequea el Output Genérico del LLM.
     */
    public void validateAiOutputXml(String dmnXml) {
        // Bloqueo de Dot Notation: Las reglas matemáticas DMN V1 no soportan introspección de Objetos Complejos
        Matcher dotMatcher = DOT_NOTATION_PATTERN.matcher(dmnXml);
        if (dotMatcher.find()) {
            log.error("[GUARDRAIL] Intento de Inyección Complex (Dot Notation) detectado: {}", dotMatcher.group());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El Modelo DMN contiene variables con 'Dot Notation' (Ej. 'Cliente.Mora'). En V1 solo se toleran variables planas de primer nivel.");
        }

        // Límite de Frecuencia Cognitiva: Máximo 50 Filas
        int ruleCount = dmnXml.split("<rule\\s*").length - 1;
        if (ruleCount > 50) {
            log.error("[GUARDRAIL] Modelo DMN excedió capacidad estructural: {} filas", ruleCount);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La Tabla autogenerada excede el límite de 50 filas. El diseño debe subdividirse en tablas atómicas.");
        }

        // Un solo Output Compuesto (V1 No soporta Múltiples variables devueltas)
        int outputCols = dmnXml.split("<output\\s*").length - 1;
        if (outputCols > 1) {
            log.error("[GUARDRAIL] Múltiples salidas detectadas en DMN: {}", outputCols);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El Modelo posee múltiples Columnas de Salida (Output). V1 requiere Salidas Atómicas Singulares.");
        }

        // Vacíos Lógicos
        if (dmnXml.contains("<text></text>")) {
            log.error("[GUARDRAIL] El LLM devolvió disyunciones `<text>` vacías.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El documento contiene disyunciones o celdas matemáticas vacías. Operación cancelada por sanidad operativa.");
        }

        log.info("[GUARDRAIL] Verificación de Sanidad Matemática completada. XML Correcto.");
    }
}
