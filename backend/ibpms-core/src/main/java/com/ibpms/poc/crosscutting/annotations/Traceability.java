package com.ibpms.poc.crosscutting.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para inyectar trazabilidad con Historias de Usuario (US) y Criterios de Aceptación (CA).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Traceability {
    String US() default "";
    String[] CA() default {};
}
