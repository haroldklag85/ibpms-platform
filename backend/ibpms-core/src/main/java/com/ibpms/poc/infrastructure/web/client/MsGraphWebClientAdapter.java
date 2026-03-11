package com.ibpms.poc.infrastructure.web.client;

import com.ibpms.poc.application.port.out.MsGraphClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Adaptador Driven — Cliente MS Graph API.
 * Implementa MsGraphClientPort para interactuar con Microsoft 365.
 * En V1 usa autenticación OAuth2 Client Credentials configurada en
 * application.yml.
 * El token se gestiona por Spring Security OAuth2 Client (recomendado para
 * V1.1).
 */
@Component
public class MsGraphWebClientAdapter implements MsGraphClientPort {

    private final WebClient webClient;

    public MsGraphWebClientAdapter(
            @Value("${msgraph.base-url:https://graph.microsoft.com/v1.0}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(java.util.Objects.requireNonNull(baseUrl)).build();
    }

    @Override
    public String generarBorradorEmail(String destinatario, String asunto, String cuerpoHtml) {
        // Payload estándar de MS Graph API para crear un mensaje borrador
        var payload = Map.of(
                "subject", asunto,
                "body", Map.of("contentType", "HTML", "content", cuerpoHtml),
                "toRecipients", java.util.List.of(
                        Map.of("emailAddress", Map.of("address", destinatario))));

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri("/me/messages")
                    .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
                    .bodyValue(java.util.Objects.requireNonNull(payload))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return response != null ? (String) response.get("id") : null;
        } catch (Exception e) {
            // Loguear en producción — no bloquear el flujo principal
            return null;
        }
    }

    @Override
    public String crearEventoCalendario(String expedienteId, String fechaIso, String descripcion) {
        var payload = Map.of(
                "subject", "Expediente: " + expedienteId,
                "body", Map.of("contentType", "HTML", "content", descripcion),
                "start", Map.of("dateTime", fechaIso, "timeZone", "America/Bogota"),
                "end", Map.of("dateTime", fechaIso, "timeZone", "America/Bogota"));

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri("/me/events")
                    .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
                    .bodyValue(java.util.Objects.requireNonNull(payload))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return response != null ? (String) response.get("id") : null;
        } catch (Exception e) {
            return null;
        }
    }
}
