package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.DataMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DataMappingRepository extends JpaRepository<DataMappingEntity, Long> {
    List<DataMappingEntity> findByProcessDefinitionKey(String processDefinitionKey);
}
