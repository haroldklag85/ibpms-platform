package com.ibpms.poc.infrastructure.adapters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.ports.out.GenericProcessDefinitionPort;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnProcessDesignRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador de infraestructura para obtener diseño de procesos desde JPA.
 * Requerimiento: US-039 (Formulario Genérico Dinámico)
 * Permite cumplir CA-5 extrayendo la whitelist desde PostgreSQL.
 */
@Component
public class ProcessDesignJpaAdapter implements GenericProcessDefinitionPort {

    private final BpmnProcessDesignRepository processDesignRepository;
    private final ObjectMapper objectMapper;

    public ProcessDesignJpaAdapter(BpmnProcessDesignRepository processDesignRepository, ObjectMapper objectMapper) {
        this.processDesignRepository = processDesignRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<String> getGenericFormWhitelist(String processKey) {
        return processDesignRepository.findByTechnicalId(processKey)
                .map(BpmnProcessDesignEntity::getGenericFormWhitelist)
                .map(whitelistStr -> {
                    if (whitelistStr != null && !whitelistStr.isBlank()) {
                        try {
                            return objectMapper.readValue(whitelistStr, new TypeReference<List<String>>() {});
                        } catch (Exception e) {
                            return null;
                        }
                    }
                    return null;
                }).orElse(null);
    }
}
