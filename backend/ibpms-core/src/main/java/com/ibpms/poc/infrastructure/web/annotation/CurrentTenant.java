package com.ibpms.poc.infrastructure.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Traceability: US-001, CA-14 (Consolidación de Identidad / Tenant)
 * Anotación para inyectar automáticamente el Tenant ID extraído del contexto de seguridad.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentTenant {
}
