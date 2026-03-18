package com.ibpms.poc.infrastructure.web.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Map;

public class StrictPrimitiveTypingValidator implements ConstraintValidator<StrictPrimitiveTyping, Map<String, Object>> {

    @Override
    public boolean isValid(Map<String, Object> variables, ConstraintValidatorContext context) {
        if (variables == null) {
            return true;
        }

        for (Object val : variables.values()) {
            if (val instanceof String) {
                String strVal = (String) val;
                // CA-50: Si la cadena representa numéricamente un Entero, Long o Double simple sin símbolos ni espacios
                // O es un "true" / "false" literales, entonces el JSON fue inyectado maliciosamente como texto
                // en lugar de su tipo natural.
                if (strVal.matches("^-?\\d+$") || strVal.matches("^-?\\d+\\.\\d+$") ||
                    strVal.equalsIgnoreCase("true") || strVal.equalsIgnoreCase("false")) {
                    return false;
                }
            }
        }
        return true;
    }
}
