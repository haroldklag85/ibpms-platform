package com.ibpms.poc.application.service.mailbox;

import com.ibpms.poc.domain.exception.ConnectionValidationException;
import com.ibpms.poc.infrastructure.jpa.entity.SacMailboxEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class MailboxConnectionManager {

    private final RestTemplate restTemplate;

    public MailboxConnectionManager() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Validates live connection against Microsoft Graph OAuth API.
     * Required before persisting any SacMailbox to the database.
     */
    public void validateConnection(SacMailboxEntity mailbox, String rawClientSecret) {
        if (!"GRAPH".equalsIgnoreCase(mailbox.getProtocol())) {
            log.warn("IMAP protocol is deprecated. Forcing GRAPH API.");
            throw new ConnectionValidationException(
                    "Legacy protocols like IMAP are prohibited. Please use Microsoft Graph API.");
        }

        log.info("Attempting live OAuth 2.0 validation for tenant: {}", mailbox.getTenantId());

        String tokenEndpoint = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token",
                mailbox.getTenantId());

        try {
            // In a real scenario, we send grant_type=client_credentials and check if MS
            // issues a token
            // For the sake of this mock POC logic requested in Epica 13, we ping the tenant
            // endpoint.
            // A mocked HTTP call here simulating the token request payload
            ResponseEntity<String> response = restTemplate.postForEntity(tokenEndpoint, Map.of(
                    "client_id", mailbox.getClientId(),
                    "client_secret", rawClientSecret,
                    "grant_type", "client_credentials"), String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ConnectionValidationException(
                        "MS Graph rejected the credentials with status: " + response.getStatusCode());
            }

            log.info("MS Graph OAuth connection validated successfully for alias: {}", mailbox.getAlias());

        } catch (HttpClientErrorException e) {
            log.error("Live validation failed for tenant: {}. Error: {}", mailbox.getTenantId(), e.getMessage());
            throw new ConnectionValidationException(
                    "Connection refused by Microsoft Azure. Invalid Client ID or Secret: "
                            + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Network error during validation: {}", e.getMessage());
            throw new ConnectionValidationException("Network connectivity to Microsoft Graph failed.");
        }
    }
}
