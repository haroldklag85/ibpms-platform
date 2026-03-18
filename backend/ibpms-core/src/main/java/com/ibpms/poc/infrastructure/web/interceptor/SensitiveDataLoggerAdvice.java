package com.ibpms.poc.infrastructure.web.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * CA-53: Interceptor de bitácora transversal para enmascarar campos sensibles.
 * Captura todos los RequestBody antes de pasarlos al Controller y loguea el payload seguro.
 */
@ControllerAdvice
public class SensitiveDataLoggerAdvice extends RequestBodyAdviceAdapter {

    private static final Logger log = LoggerFactory.getLogger(SensitiveDataLoggerAdvice.class);
    private static final Pattern SENSITIVE_KEYS = Pattern.compile(".*(password|pwd|secret|token).*", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true; 
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        
        if (log.isInfoEnabled() && body != null) {
            try {
                // Hacemos un log simple. Si el body contiene Maps o DTOs, Jackson lo castearía.
                String maskedStr = maskObject(body);
                log.info("📥 INCOMING REST PAYLOAD (CA-53 MASKED): {}", maskedStr);
            } catch (Exception e) {
                log.trace("No pudo enmascarar body", e);
            }
        }
        
        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }

    private String maskObject(Object obj) {
        if (obj == null) return "null";
        
        String str = obj.toString();
        // Método defensivo y simple: reemplazar valores que parezcan estar asociados a llaves sensibles.
        // Como interceptamos el objeto Java despues del mapping, hacer una mascara sobre el toString o 
        // JSON crudo. Para CA-53 hacemos un replace Regex robusto:
        
        return str.replaceAll("(?i)(password|pwd|secret|token)(\"\\s*:\\s*\"|=)[^\",}]*(?=\",|}|,)", "$1$2********");
    }
}
