package com.ibpms.poc.application.port.out;

import com.ibpms.poc.domain.model.BpmnProcessDesign;
import java.util.Optional;
import java.util.UUID;

public interface BpmnDesignPort {
    Optional<BpmnProcessDesign> findById(UUID id);
    Optional<BpmnProcessDesign> findByTechnicalId(String technicalId);
    BpmnProcessDesign save(BpmnProcessDesign design);
}
