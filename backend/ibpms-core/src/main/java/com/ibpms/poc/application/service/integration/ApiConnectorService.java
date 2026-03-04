package com.ibpms.poc.application.service.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.infrastructure.jpa.entity.integration.ApiConnectorEntity;
import com.ibpms.poc.infrastructure.jpa.repository.integration.ApiConnectorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApiConnectorService {

    private final ApiConnectorRepository repository;
    private final OutboundDispatcherService dispatcher;
    private final ObjectMapper mapper;

    public ApiConnectorService(ApiConnectorRepository repository, OutboundDispatcherService dispatcher,
            ObjectMapper mapper) {
        this.repository = repository;
        this.dispatcher = dispatcher;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ApiConnectorEntity> listAllConnectors() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ApiConnectorEntity> getConnector(UUID id) {
        return repository.findById(id);
    }

    @Transactional
    public ApiConnectorEntity createConnector(ApiConnectorEntity connector) {
        if (connector.getId() == null) {
            connector.setId(UUID.randomUUID());
        }
        return repository.save(connector);
    }

    @Transactional
    public ApiConnectorEntity updateConnector(UUID id, ApiConnectorEntity updatedData) {
        return repository.findById(id).map(existing -> {
            existing.setName(updatedData.getName());
            existing.setBaseUrl(updatedData.getBaseUrl());
            existing.setHttpMethod(updatedData.getHttpMethod());
            existing.setDefaultHeaders(updatedData.getDefaultHeaders());
            existing.setAuthConfig(updatedData.getAuthConfig());
            existing.setPgpPublicKey(updatedData.getPgpPublicKey());
            return repository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Connector not found: " + id));
    }

    @Transactional
    public void deleteConnector(UUID id) {
        repository.deleteById(id);
    }

    /**
     * Prueba el conector en vivo usando el Testing Playground del Frontend.
     */
    public String testPing(UUID id, String payloadJson) throws JsonProcessingException {
        ApiConnectorEntity connector = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Connector not found: " + id));

        // Transforma el JSON string genérico a Map para el dispatcher
        @SuppressWarnings("unchecked")
        Map<String, Object> payloadMap = mapper.readValue(payloadJson, Map.class);

        // Ejecución delegada simulando ser Camunda
        return dispatcher.dispatch(connector.getSystemCode(), "1.0", payloadMap);
    }
}
