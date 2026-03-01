package com.ibpms.poc.infrastructure.camunda.delegate;

import com.ibpms.poc.infrastructure.jpa.entity.OutboundConfigEntity;
import com.ibpms.poc.infrastructure.jpa.repository.OutboundConfigRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.Expression;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Task Ejecutor Genérico (Outbound).
 * Se asigna como "Delegate Class" en el Modeler de Camunda:
 * => `com.ibpms.poc.infrastructure.camunda.delegate.GenericRestDelegate`
 * Utiliza Field Injection para obtener "outboundConfigId" desde BPMN.
 */
@Component
public class GenericRestDelegate implements JavaDelegate {

    private final OutboundConfigRepository outboundConfigRepository;
    private final RestTemplate restTemplate;

    // Inyección de campos desde Camunda Modeler (Field Injection)
    private Expression outboundConfigId;

    public GenericRestDelegate(OutboundConfigRepository outboundConfigRepository) {
        this.outboundConfigRepository = outboundConfigRepository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        if (outboundConfigId == null) {
            throw new IllegalArgumentException("Variable 'outboundConfigId' no mapeada en Camunda Field Injection");
        }

        String configIdStr = (String) outboundConfigId.getValue(execution);
        UUID configId = UUID.fromString(configIdStr);

        Optional<OutboundConfigEntity> configOpt = outboundConfigRepository.findById(configId);

        if (configOpt.isEmpty()) {
            throw new RuntimeException("Outbound Config No encontrada en BBDD: " + configIdStr);
        }

        OutboundConfigEntity config = configOpt.get();

        // Armar el payload con todas las variables de proceso
        Map<String, Object> processVariables = execution.getVariables();

        HttpHeaders headers = new HttpHeaders();
        // Lógica pseudo-básica de Auth
        if ("BEARER_TOKEN".equals(config.getAuthType().name())) {
            String token = (String) execution.getVariable("OUTBOUND_EXT_TOKEN");
            if (token != null)
                headers.setBearerAuth(token);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(processVariables, headers);
        HttpMethod method = HttpMethod.valueOf(config.getHttpMethod());

        // Disparar invocación externa asíncrona
        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                config.getEndpointUrl(),
                method,
                entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class);

        // Guardar la respuesta como variable del motor BPMN en caso de querer enrutar
        // "Si hay error o no"
        execution.setVariable("OUTBOUND_RESPONSE_STATUS", response.getStatusCode().value());
        if (response.getBody() != null) {
            execution.setVariable("OUTBOUND_RESPONSE_BODY", response.getBody());
        }
    }
}
