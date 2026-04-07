package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.DataMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DataMappingRepository extends JpaRepository<DataMappingEntity, UUID> {
    List<DataMappingEntity> findByProcessDefinitionKey(String processDefinitionKey);
    List<DataMappingEntity> findByProcessDefinitionKeyAndTaskId(String processDefinitionKey, String taskId);
}
