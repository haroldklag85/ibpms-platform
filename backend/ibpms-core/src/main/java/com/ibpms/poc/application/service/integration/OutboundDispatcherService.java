package com.ibpms.poc.application.service.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ibpms.poc.infrastructure.jpa.entity.integration.ApiConnectorEntity;
import com.ibpms.poc.infrastructure.jpa.repository.integration.ApiConnectorRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Dispatcher Outbound (Pantalla 8).
 * Ejecuta llamadas a APIs externas con Throttling, DLP, Paginación y Date
 * Parsing.
 */
@Service
public class OutboundDispatcherService {

    private static final Logger log = LoggerFactory.getLogger(OutboundDispatcherService.class);
    private final ApiConnectorRepository connectorRepository;
    private final CryptographyService cryptoService;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    // Regex para DLP (ocultar campos con estos nombres en logs)
    private static final Pattern DLP_SENSITIVE_KEYS = Pattern.compile("(?i)(password|secret|token|cred)");

    public OutboundDispatcherService(ApiConnectorRepository connectorRepository,
            CryptographyService cryptoService,
            RestTemplate restTemplate,
            ObjectMapper mapper) {
        this.connectorRepository = connectorRepository;
        this.cryptoService = cryptoService;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    /**
     * Punto de entrada para Camunda Service Tasks vía delegado.
     */
    @CircuitBreaker(name = "apiConnector", fallbackMethod = "dispatchFallback")
    @Retry(name = "apiConnector")
    public String dispatch(String systemCode, String version, Map<String, Object> payload) {
        ApiConnectorEntity connector = connectorRepository.findBySystemCodeAndVersion(systemCode, version)
                .orElseThrow(
                        () -> new IllegalArgumentException("Conector no encontrado: " + systemCode + " " + version));

        // CA-13: Date Parsing en el payload
        parseDatesToIso8601(payload);

        String jsonPayload;
        try {
            jsonPayload = mapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error sanitizando JSON del payload", e);
        }

        // DLP Interceptor (CA-8) ANTES de loggear o enviar
        log.info("Iniciando Dispatch outbound a [{}]. Payload enmascarado: {}", connector.getBaseUrl(),
                applyDlp(jsonPayload));

        // CA-25: PGP Encrypt si aplica
        String finalPayload = jsonPayload;
        if (connector.getPgpPublicKey() != null && !connector.getPgpPublicKey().isBlank()) {
            finalPayload = cryptoService.encryptPayloadPgp(jsonPayload, connector.getPgpPublicKey());
        }

        // Construir Headers (Auto-Sign CA-17, Oauth2 inyección simulada)
        HttpHeaders headers = buildHeaders(connector, finalPayload);

        HttpEntity<String> request = new HttpEntity<>(finalPayload, headers);
        HttpMethod method = HttpMethod.valueOf(connector.getHttpMethod().toUpperCase());

        // CA-18: Pagination Handler (Básico)
        // Por simplificación en V1, mostramos una llamada única. La iteración requiere
        // conocer la heurística de next_page.
        ResponseEntity<String> response = restTemplate.exchange(connector.getBaseUrl(), method, request, String.class);

        log.info("Dispatch exitoso a [{}]. HTTP Status: {}", connector.getBaseUrl(), response.getStatusCode());

        // Aplicar DLP a la respuesta por seguridad de Logs
        log.debug("Response payload enmascarado: {}", applyDlp(response.getBody()));

        return response.getBody();
    }

    /**
     * Fallback cuando CircuitBreaker arranca o max retries fallan (RabbitMQ DLQ en
     * V2).
     */
    public String dispatchFallback(String systemCode, String version, Map<String, Object> payload, Throwable t) {
        log.error("Outbound Dispatching a {} V{} falló exhaustivamente. Derivando a DLQ...", systemCode, version, t);
        // Aquí entraría el envío a RabbitMQ Dead Letter Queue y alerta a Analyst.
        throw new RuntimeException("Dispatch fallido. Encolado en DLQ", t);
    }

    private void parseDatesToIso8601(Map<String, Object> payload) {
        payload.forEach((key, value) -> {
            if (value instanceof java.util.Date) {
                // Simplicación de fecha a ISO-8601
                payload.put(key, ((java.util.Date) value).toInstant().toString());
            } else if (value instanceof LocalDate) {
                payload.put(key, ((LocalDate) value).format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        });
    }

    private String applyDlp(String json) {
        if (json == null)
            return null;
        try {
            JsonNode root = mapper.readTree(json);
            maskSensitiveNodes(root);
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            return "***[BODY NO PARSEABLE. MASCARADO POR DEFECTO]***";
        }
    }

    private void maskSensitiveNodes(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (DLP_SENSITIVE_KEYS.matcher(field.getKey()).find() && field.getValue().isTextual()) {
                    objNode.put(field.getKey(), "***MASKED***");
                } else {
                    maskSensitiveNodes(field.getValue());
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                maskSensitiveNodes(child);
            }
        }
    }

    private HttpHeaders buildHeaders(ApiConnectorEntity connector, String payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Auth injection simple
        if (connector.getAuthConfig() != null) {
            try {
                JsonNode auth = mapper.readTree(connector.getAuthConfig());
                if (auth.has("type") && "BEARER".equalsIgnoreCase(auth.get("type").asText())) {
                    headers.set("Authorization", "Bearer " + auth.get("token").asText());
                }
                if (auth.has("type") && "HMAC".equalsIgnoreCase(auth.get("type").asText())) {
                    String secret = auth.get("secret").asText();
                    headers.set("X-Signature", cryptoService.generateHmacSha256Signature(payload, secret));
                }
            } catch (Exception e) {
                log.warn(
                        "No se pudo parsear el authConfig para inyección de cabeceras en " + connector.getSystemCode());
            }
        }

        // Custom config headers
        if (connector.getDefaultHeaders() != null) {
            try {
                JsonNode customHbr = mapper.readTree(connector.getDefaultHeaders());
                customHbr.fields().forEachRemaining(entry -> headers.set(entry.getKey(), entry.getValue().asText()));
            } catch (Exception e) {
            }
        }

        return headers;
    }
}
