package com.ibpms.poc.domain.service.security;

import com.ibpms.poc.domain.exception.SoDViolationException;

/**
 * Servicio de Dominio puro para validar Segregación de Funciones (SoD) CA-06.
 */
public class SoDValidatorDomainService {

    /**
     * Valida que el creador de una tarea no sea el mismo que intenta aprobarla/completarla.
     * 
     * @param creatorId Identificador del creador (ej. username o UUID).
     * @param approverId Identificador del usuario que intenta aprobar la tarea.
     * @throws SoDViolationException si creatorId y approverId son iguales.
     */
    public void validate(String creatorId, String approverId) {
        if (creatorId != null && approverId != null && creatorId.equalsIgnoreCase(approverId)) {
            throw new SoDViolationException(
                "Violación de Segregación de Funciones (SoD): El usuario '" + approverId + 
                "' intentó aprobar una transacción o tarea que él mismo creó."
            );
        }
    }
}
