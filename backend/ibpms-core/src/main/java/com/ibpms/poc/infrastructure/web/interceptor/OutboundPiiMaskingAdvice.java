package com.ibpms.poc.infrastructure.web.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class OutboundPiiMaskingAdvice implements ResponseBodyAdvice<Object> {
    
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // Aplica a todas las respuestas para interceptar PII
        return true; 
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, 
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null) return null;
        
        try {
            if (body instanceof String) {
                String bodyStr = (String) body;
                return bodyStr.replaceAll("(\\d{3}-\\d{2}-\\d{4}|\\d{16})", "[CONFIDENCIAL - CLASE PII]");
            } else {
                // Responsabilidad del backend: correcta re-serialización del object tree
                String jsonStr = objectMapper.writeValueAsString(body);
                String masked = jsonStr.replaceAll("(\\d{3}-\\d{2}-\\d{4}|\\d{16})", "[CONFIDENCIAL - CLASE PII]");
                
                // Si hubo enmascaramiento, deserializamos de vuelta al tipo original
                if (!jsonStr.equals(masked)) {
                    return objectMapper.readValue(masked, body.getClass());
                }
            }
        } catch(Exception ignored) {
            log.warn("No se pudo procesar la esterilización de PII saliente", ignored);
        }
        
        return body;
    }
}
