package com.ibpms.poc.infrastructure.web.client;

import com.ibpms.poc.application.port.out.CrmClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Adaptador Driven — Cliente CRM (REST via WebClient).
 * Implementa CrmClientPort para obtener metadatos de cliente desde un CRM
 * externo.
 * La URL base se configura en application.yml bajo crm.base-url.
 */
@Component
public class CrmWebClientAdapter implements CrmClientPort {

    private final WebClient webClient;

    public CrmWebClientAdapter(@Value("${crm.base-url:http://localhost:9090}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public String obtenerNombreCliente(String clienteId) {
        try {
            Map<?, ?> response = webClient.get()
                    .uri("/api/clients/{id}", clienteId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return response != null ? (String) response.get("fullName") : null;
        } catch (Exception e) {
            // Fail gracefully: el expediente se crea aún sin el nombre del CRM
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerMetadatosCliente(String clienteId) {
        try {
            return webClient.get()
                    .uri("/api/crm/clients/{id}/metadata", clienteId)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .onErrorResume(e -> Mono.just(Map.of("riesgo", "DESCONOCIDO", "segmento", "RETAIL")))
                    .block();
        } catch (Exception e) {
            return Map.of("riesgo", "FALLO_CRM");
        }
    }

    @Override
    public boolean checkVipStatus(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }

        String domain = email.substring(email.indexOf("@") + 1);

        try {
            // Emulando la ruta /api/crm/clients/{domain} del requerimiento
            Map<String, Object> response = webClient.get()
                    .uri("/api/crm/clients/domain/{domain}", domain)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> {
                        // Dummy Logic: Si falla el CRM o no existe, miramos si el dominio es "vip.com"
                        boolean fallbackVip = domain.equalsIgnoreCase("gob.co") || domain.equalsIgnoreCase("vip.com");
                        return Mono.just(Map.of("isVip", fallbackVip));
                    })
                    .block();

            if (response != null && response.containsKey("isVip")) {
                Object isVip = response.get("isVip");
                return (isVip instanceof Boolean) && (Boolean) isVip;
            }
            return false;
        } catch (Exception e) {
            return false; // Ante duda, cliente estándar
        }
    }
}
