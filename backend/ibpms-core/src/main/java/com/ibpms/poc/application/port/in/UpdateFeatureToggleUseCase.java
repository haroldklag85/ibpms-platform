// @Traceability: US-003 - ADR-001
package com.ibpms.poc.application.port.in;

import com.ibpms.poc.crosscutting.annotations.Traceability;

/**
 * @Traceability(US = "US-001", CA = {"CA-08"})
 * POR QUÉ: Caso de uso primario (inbound port) que define el contrato de negocio 
 * para consultar y actualizar Feature Toggles, desacoplando el controlador web de la implementación de dominio/persistencia.
 */
@Traceability(US = "US-001", CA = {"CA-08"})
public interface UpdateFeatureToggleUseCase {
    boolean isFeatureEnabled(String tenantId, String toggleKey);
    boolean updateFeatureToggle(String tenantId, String toggleKey, Boolean enabled, String changedBy);
}
