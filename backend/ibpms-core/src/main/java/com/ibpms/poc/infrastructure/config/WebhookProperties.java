package com.ibpms.poc.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Externalized configuration for Webhook Intake (US-004).
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ibpms.webhook")
public class WebhookProperties {

    private Security security = new Security();
    private Payload payload = new Payload();
    private PreTriage preTriage = new PreTriage();

    @Data
    public static class Security {
        /** HMAC or BEARER */
        private String mode = "HMAC";
        private String hmacSecret = "";
    }

    @Data
    public static class Payload {
        /** Maximum payload size in bytes. Default: 10MB */
        private long maxSizeBytes = 10_485_760L;
    }

    @Data
    public static class PreTriage {
        /** Camunda Process Definition Key for the pre-triage workflow. */
        private String processDefinitionKey = "Process_PreTriaje_Intake";
    }
}
