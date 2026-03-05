package com.ibpms.poc.application.service.inbox;

import com.ibpms.poc.domain.exception.ConnectionValidationException;
import com.ibpms.poc.domain.model.SacMailbox;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailboxConnectionManager {

    private final WebClient webClient;

    // Simular el cliente que hablaría con Azure Key Vault en producción real
    // (Por ejemplo, un SecretClient de azure-security-keyvault-secrets)

    public MailboxConnectionManager(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Valida de manera en VIVO la conexion contra Microsoft Graph (Auth/Token
     * endpoint)
     * para asegurar que el Tenant, Client ID y el Secreto ubicado en KV sean
     * correctos.
     * Retorna el Payload del Token o arroja ConnectionValidationException.
     */
    public boolean validateConnection(SacMailbox mailbox, String rawSecretToTest) {
        if (mailbox.getProtocol() == SacMailbox.MailboxProtocol.IMAP_DEPRECATED) {
            throw new ConnectionValidationException("Protocolo IMAP está deprecado por Microsoft. Use OAUTH2 (GRAPH).");
        }

        log.info("Iniciando validación Graph API para el buzón alias: {}", mailbox.getAlias());

        String tokenEndpoint = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token",
                mailbox.getTenantId());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", mailbox.getClientId());
        formData.add("scope", "https://graph.microsoft.com/.default");
        formData.add("client_secret", rawSecretToTest);
        formData.add("grant_type", "client_credentials");

        try {
            String tokenResponse = webClient.post()
                    .uri(tokenEndpoint)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (tokenResponse != null && tokenResponse.contains("access_token")) {
                log.info("Conexión validada con éxito (HTTP 200) para el Tenant: {}", mailbox.getTenantId());
                return true;
            } else {
                throw new ConnectionValidationException("La API de Microsoft no devolvió el access_token esperado.");
            }
        } catch (WebClientResponseException e) {
            log.error("Fallo de autenticación con Graph API: {}", e.getResponseBodyAsString());
            throw new ConnectionValidationException(
                    "Fallo al autenticar contra Microsoft Graph. Verifique las credenciales.");
        } catch (Exception e) {
            log.error("Excepción inesperada al conectar con MS Graph", e);
            throw new ConnectionValidationException("Excepción inesperada de red conectando al servidor OAuth.");
        }
    }
}
