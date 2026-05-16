package com.ibpms.poc.application.port.out;

import com.ibpms.poc.domain.model.BpmnProcessDesign;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

/**
 * Puerto de Salida para persistencia de diseños BPMN.
 * @Traceability: US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
 */
public interface BpmnDesignPort {
    Optional<BpmnProcessDesign> findById(UUID id);
    Optional<BpmnProcessDesign> findByTechnicalId(String technicalId);
    List<BpmnProcessDesign> findAll();
    BpmnProcessDesign save(BpmnProcessDesign design);
}
