package com.ibpms.poc.infrastructure.web.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class OutboundPiiMaskingAdviceTest {

    private OutboundPiiMaskingAdvice advice;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        advice = new OutboundPiiMaskingAdvice(objectMapper);
    }

    @Test
    void testSupportsReturnsTrue() {
        assertTrue(advice.supports(null, null));
    }

    @Test
    void test_OutboundPiiMasking_Sterilizes_SSN_and_CC() throws Exception {
        // @Traceability: US-000 - CA-4
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        ServerHttpResponse res = mock(ServerHttpResponse.class);
        MethodParameter returnType = mock(MethodParameter.class);

        Map<String, String> payload = new HashMap<>();
        payload.put("name", "John Doe");
        payload.put("ssn", "123-45-6789");
        payload.put("creditCard", "1234567812345678");
        payload.put("id", "8902");

        // Act
        Object processedBody = advice.beforeBodyWrite(payload, returnType, MediaType.APPLICATION_JSON, null, req, res);

        // Assert
        assertNotNull(processedBody);
        assertTrue(processedBody instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, String> maskedPayload = (Map<String, String>) processedBody;
        
        assertEquals("John Doe", maskedPayload.get("name"), "El PII masking no debe corromper campos normales");
        assertEquals("8902", maskedPayload.get("id"));
        
        assertEquals("[CONFIDENCIAL - CLASE PII]", maskedPayload.get("ssn"));
        assertEquals("[CONFIDENCIAL - CLASE PII]", maskedPayload.get("creditCard"));
    }

    @Test
    void test_OutboundPiiMasking_StringBody() {
        // Arrange
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        ServerHttpResponse res = mock(ServerHttpResponse.class);
        MethodParameter returnType = mock(MethodParameter.class);
        
        String body = "User info: SSN=987-65-4321 and CC=8765432187654321";

        // Act
        Object processedBody = advice.beforeBodyWrite(body, returnType, MediaType.TEXT_PLAIN, null, req, res);

        // Assert
        assertEquals("User info: SSN=[CONFIDENCIAL - CLASE PII] and CC=[CONFIDENCIAL - CLASE PII]", processedBody);
    }
}
