package com.ibpms.poc.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Externalized configuration for the ClamAV anti-malware sidecar (US-004 CA-11).
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ibpms.clamav")
public class ClamAvProperties {

    /** Base URL of the ClamAV REST endpoint (e.g. http://localhost:3310). */
    private String url = "http://localhost:3310";

    /** Timeout in milliseconds for the scanner connection. */
    private int timeoutMs = 5000;
}
