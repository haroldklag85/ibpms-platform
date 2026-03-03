package com.ibpms.poc.application.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class OutboundDispatcher {

    private final CryptographyService cryptographyService;

    public OutboundDispatcher(CryptographyService cryptographyService) {
        this.cryptographyService = cryptographyService;
    }

    /**
     * Valida el payload, asegura las comunicaciones y delega la ejecución al APIM.
     * En iBPMS (V1), solo enviamos JSON. La traducción SOAP/Legacy se da en el
     * APIM.
     */
    public void dispatchRestJson(String targetUrl, String payload, String pgpPublicKeyAscii) throws Exception {
        validateAntiSsrf(targetUrl);

        String finalPayload = payload;
        if (pgpPublicKeyAscii != null && !pgpPublicKeyAscii.isBlank()) {
            finalPayload = cryptographyService.encryptPayloadPgp(payload, pgpPublicKeyAscii);
        }

        // Aquí usaríamos RestTemplate/WebClient real para hacer el HTTP POST
        simulateApimHttpCall(targetUrl, finalPayload);
    }

    /**
     * CA-12 SSRF Defense: El motor BPMN o el diseñador pueden intentar inyectar
     * URLs internas.
     * iBPMS actuando de Integration Hub DEBE bloquear peticiones de la Intranet
     * Privada.
     */
    protected void validateAntiSsrf(String urlString) {
        try {
            URI uri = new URI(urlString);
            String host = uri.getHost();

            if (host == null) {
                throw new SecurityException("SSRF Guardrail: Host no puede ser nulo.");
            }

            String lowerHost = host.toLowerCase();

            // Bloquear Localhost y Loopbacks
            if (lowerHost.equals("localhost") || lowerHost.startsWith("127.")) {
                throw new SecurityException(
                        "SSRF Guardrail: Se bloqueó una solicitud a la red local (Localhost / 127.x.x.x)");
            }

            // Bloquear Cloud Metadata (AWS / Azure)
            if (lowerHost.equals("169.254.169.254")) {
                throw new SecurityException(
                        "SSRF Guardrail: Intento de extracción de metadatos Cloud (169.254.169.254) bloqueado.");
            }

            // Bloquear 0.0.0.0
            if (lowerHost.equals("0.0.0.0")) {
                throw new SecurityException("SSRF Guardrail: Enrutamiento wildcard (0.0.0.0) bloqueado.");
            }

        } catch (URISyntaxException e) {
            throw new SecurityException("SSRF Guardrail: URL mal formada.", e);
        }
    }

    private void simulateApimHttpCall(String targetUrl, String finalPayload) {
        // En un entorno de Producción haríamos:
        // HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.APPLICATION_JSON);
        // ... WebClient(targetUrl).post().body(finalPayload)
    }
}
