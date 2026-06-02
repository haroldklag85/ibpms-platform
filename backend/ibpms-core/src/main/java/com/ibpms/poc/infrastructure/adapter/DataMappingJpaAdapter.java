// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.port.out.DataMappingPort;
import com.ibpms.poc.domain.model.DataMapping;
import com.ibpms.poc.infrastructure.jpa.entity.DataMappingEntity;
import com.ibpms.poc.infrastructure.jpa.repository.DataMappingRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DataMappingJpaAdapter implements DataMappingPort {

    private final DataMappingRepository repository;

    public DataMappingJpaAdapter(DataMappingRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DataMapping> findByProcessDefinitionKey(String processDefinitionKey) {
        return repository.findByProcessDefinitionKey(processDefinitionKey).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public DataMapping save(DataMapping dataMapping) {
        DataMappingEntity entity = new DataMappingEntity();
        if (dataMapping.getId() != null) {
            entity.setId(dataMapping.getId());
        }
        entity.setProcessDefinitionKey(dataMapping.getProcessDefinitionKey());
        entity.setTaskId(dataMapping.getTaskId());
        entity.setConnectorId(dataMapping.getConnectorId());
        entity.setMappingJson(dataMapping.getMappingJson());
        entity.setLastValidatedAt(dataMapping.getLastValidatedAt());
        
        DataMappingEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    private DataMapping toDomain(DataMappingEntity entity) {
        DataMapping domain = new DataMapping();
        domain.setId(entity.getId());
        domain.setProcessDefinitionKey(entity.getProcessDefinitionKey());
        domain.setTaskId(entity.getTaskId());
        domain.setConnectorId(entity.getConnectorId());
        domain.setMappingJson(entity.getMappingJson());
        domain.setLastValidatedAt(entity.getLastValidatedAt());
        return domain;
    }
}
