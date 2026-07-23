package com.ibpms.poc.application.service.webhook;

import com.ibpms.poc.application.service.WebhookIntakeService;
import com.ibpms.poc.infrastructure.config.WebhookProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

import com.ibpms.poc.crosscutting.annotations.Traceability;

/**
 * T5: HmacValidationTest (US-004 CA-10)
 * Validates HMAC signature validation and mode switching.
 */
@Traceability(US = "US-004", CA = {"CA-10"})
class HmacValidationTest {

    private WebhookIntakeService service;
    private static final String SECRET = "test-hmac-secret-key-32-chars!!";

    @BeforeEach
    void setUp() {
        WebhookProperties props = new WebhookProperties();
        props.getSecurity().setMode("HMAC");
        props.getSecurity().setHmacSecret(SECRET);
        service = new WebhookIntakeService(null, null, null, null, null, null, props, null, null);
    }

    @Test
    @DisplayName("CA-10: Valid HMAC signature → pass")
    void validHmacPasses() throws Exception {
        String body = "{\"test\": \"payload\"}";
        String signature = computeHmac(body, SECRET);

        assertTrue(service.validateHmacSignature(body, signature));
    }

    @Test
    @DisplayName("CA-10: Invalid HMAC signature → reject")
    void invalidHmacFails() {
        String body = "{\"test\": \"payload\"}";
        String badSignature = "obviously-wrong-signature";

        assertFalse(service.validateHmacSignature(body, badSignature));
    }

    @Test
    @DisplayName("CA-10: Null signature header → reject")
    void nullSignatureRejected() {
        String body = "{\"test\": \"payload\"}";
        assertFalse(service.validateHmacSignature(body, null));
    }

    @Test
    @DisplayName("CA-10: Bearer mode skips HMAC validation entirely")
    void bearerModeSkipsHmac() {
        WebhookProperties bearerProps = new WebhookProperties();
        bearerProps.getSecurity().setMode("BEARER");
        WebhookIntakeService bearerService = new WebhookIntakeService(null, null, null, null, null, null, bearerProps, null, null);

        // Even without a signature, Bearer mode passes
        assertTrue(bearerService.validateHmacSignature("{}", null));
    }

    private String computeHmac(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmacBytes);
    }
}

