package com.ibpms.core.sac.service;

import com.ibpms.core.sac.exception.ConnectionValidationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class MailboxConnectionManager {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Valida la conexion real con MS Graph OAuth2 endpoint.
     * 
     * @param tenantId     Tenant ID de Azure AD
     * @param clientId     Client ID de la app registrada
     * @param clientSecret El secret en texto plano (antes de enviarlo a Key Vault)
     */
    public void validateGraphConnection(String tenantId, String clientId, String clientSecret) {
        String tokenUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("scope", "https://graph.microsoft.com/.default");
        map.add("client_secret", clientSecret);
        map.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ConnectionValidationException(
                        "No se pudo obtener token de MS Graph. HTTP " + response.getStatusCode().value());
            }
        } catch (HttpClientErrorException e) {
            throw new ConnectionValidationException(
                    "Credenciales rechazadas por MS Graph: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(),
                    e);
        } catch (Exception e) {
            throw new ConnectionValidationException(
                    "Error inesperado validando conexión con Microsoft: " + e.getMessage(), e);
        }
    }
}
