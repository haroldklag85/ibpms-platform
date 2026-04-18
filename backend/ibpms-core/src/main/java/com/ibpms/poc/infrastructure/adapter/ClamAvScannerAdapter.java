package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.domain.port.ClamAvScanner;
import com.ibpms.poc.infrastructure.config.ClamAvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * REST adapter for ClamAV sidecar anti-malware scanning (US-004 CA-11).
 * Communicates with ClamAV's REST API endpoint.
 * In production, this hits the containerized ClamAV sidecar.
 * In tests, this entire bean is replaced by @MockBean.
 */
@Component
public class ClamAvScannerAdapter implements ClamAvScanner {

    private static final Logger log = LoggerFactory.getLogger(ClamAvScannerAdapter.class);
    private final WebClient webClient;
    private final ClamAvProperties properties;

    public ClamAvScannerAdapter(ClamAvProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .baseUrl(properties.getUrl())
                .build();
    }

    @Override
    public ScanResult scan(byte[] fileBytes, String fileName) {
        try {
            log.info("Scanning file [{}] ({} bytes) via ClamAV at {}", fileName, fileBytes.length, properties.getUrl());

            String response = webClient.post()
                    .uri("/scan")
                    .bodyValue(fileBytes)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(properties.getTimeoutMs()))
                    .onErrorResume(e -> {
                        log.error("ClamAV scan failed for file [{}]: {}", fileName, e.getMessage());
                        return Mono.just("UNAVAILABLE");
                    })
                    .block();

            if (response == null || response.contains("UNAVAILABLE")) {
                log.warn("ClamAV service unavailable. Applying fail-secure for file [{}].", fileName);
                return ScanResult.UNAVAILABLE;
            }

            if (response.contains("FOUND") || response.contains("INFECTED")) {
                log.warn("MALWARE DETECTED in file [{}]!", fileName);
                return ScanResult.INFECTED;
            }

            log.info("File [{}] scanned CLEAN.", fileName);
            return ScanResult.CLEAN;

        } catch (Exception e) {
            log.error("ClamAV adapter exception for file [{}]: {}", fileName, e.getMessage());
            return ScanResult.UNAVAILABLE;
        }
    }
}
