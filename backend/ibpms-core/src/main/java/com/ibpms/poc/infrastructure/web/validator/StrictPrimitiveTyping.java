package com.ibpms.poc.infrastructure.web.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * CA-50: Validador Cero-Trust que asegura el tipado estricto.
 * Condena el envío de strings que disfrazan enteros o booleanos.
 */
@Documented
@Constraint(validatedBy = StrictPrimitiveTypingValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrictPrimitiveTyping {
    String message() default "Violación de Tipado Estricto (CA-50). Valores primitivos (números/booleanos) no pueden ser inyectados maliciosamente como Strings.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
