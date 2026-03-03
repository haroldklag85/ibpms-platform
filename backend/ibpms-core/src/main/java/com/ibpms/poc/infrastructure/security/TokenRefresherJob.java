package com.ibpms.poc.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ibpms.poc.infrastructure.jpa.entity.integration.ApiConnectorEntity;
import com.ibpms.poc.infrastructure.jpa.repository.integration.ApiConnectorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Trabajo Programado (Pantalla 8) - Identity Refreshing Invisible (CA-20).
 * Busca conectores con Auth Oauth2 cuyo token expire pronto, e invoca
 * "grant_type=refresh_token".
 */
@Component
public class TokenRefresherJob {

    private static final Logger log = LoggerFactory.getLogger(TokenRefresherJob.class);

    private final ApiConnectorRepository connectorRepository;
    private final ObjectMapper mapper;

    public TokenRefresherJob(ApiConnectorRepository connectorRepository, ObjectMapper mapper) {
        this.connectorRepository = connectorRepository;
        this.mapper = mapper;
    }

    // Se ejecuta cada 1 minuto (cron de prueba). En PROD usaría "0 */5 * * * *".
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void refreshTokenTask() {
        log.debug("Ejecutando TokenRefresherJob (Pre-emptive OAuth2 Refresh)...");

        List<ApiConnectorEntity> connectors = connectorRepository.findAll();

        for (ApiConnectorEntity connector : connectors) {
            if (connector.getAuthConfig() == null)
                continue;

            try {
                JsonNode authConf = mapper.readTree(connector.getAuthConfig());

                // Criterio CA-20: Tipo OAUTH2 con expires_at cercano (< 5 min)
                if (authConf.has("type") && "OAUTH2".equalsIgnoreCase(authConf.get("type").asText())
                        && authConf.has("expires_at")) {

                    LocalDateTime expiresAt = LocalDateTime.parse(authConf.get("expires_at").asText());
                    LocalDateTime threshold = LocalDateTime.now().plusMinutes(5);

                    if (expiresAt.isBefore(threshold)) {
                        log.info(
                                "Token OAuth2 para conector [{}] a punto de expirar. Iniciando Refresh Flow preemptivo.",
                                connector.getSystemCode());

                        // Aquí se ejecutaría la llamada POST real al Auth Server usando RestTemplate
                        // Simulamos el éxito:
                        String newAccessToken = "mocked-new-jwt-" + System.currentTimeMillis();
                        LocalDateTime newExpiration = LocalDateTime.now().plusHours(1);

                        // Actualizar BD
                        ((ObjectNode) authConf).put("token", newAccessToken);
                        ((ObjectNode) authConf).put("expires_at", newExpiration.toString());

                        connector.setAuthConfig(mapper.writeValueAsString(authConf));
                        connector.setUpdatedAt(LocalDateTime.now());
                        connectorRepository.save(connector);

                        log.info("Refresh Flow exitoso para [{}]. Nueva caducidad: {}", connector.getSystemCode(),
                                newExpiration);
                    }
                }

            } catch (Exception e) {
                log.error("Fallo al inspeccionar config de OAuth2 para conector {}", connector.getSystemCode(), e);
            }
        }
    }
}
