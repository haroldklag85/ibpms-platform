package com.ibpms.poc.infrastructure.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CA-63, CA-67: Annotation that indicates an endpoint operates in Sandbox mode.
 * The SandboxInterceptor will catch this and enforce isolation constraints (max 3 instances).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SandboxOperation {
}
