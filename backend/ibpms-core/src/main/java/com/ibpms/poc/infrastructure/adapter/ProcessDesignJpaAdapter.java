// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.port.out.GenericProcessDefinitionPort;
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
                .map(whitelistMap -> {
                    if (whitelistMap != null && whitelistMap.containsKey("allowedForms")) {
                        Object obj = whitelistMap.get("allowedForms");
                        if (obj instanceof List) {
                            return (List<String>) obj;
                        }
                    }
                    return null;
                }).orElse(null);
    }
}
