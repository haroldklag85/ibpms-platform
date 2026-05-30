// @Traceability: US-003 - ADR-001
package com.ibpms.poc.application.port.out;

import com.ibpms.poc.application.dto.FormDesignDTO;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Port para la persistencia de FormDesign.
 * Aísla el dominio de las implementaciones específicas de base de datos (Ej. Spring Data JPA).
 * Requerimiento: US-003 (iForm Maestro - Formularios Dinámicos)
 * Soporta CAs: CA-01 (Creación), CA-11 (Inmutabilidad), CA-21 (Versionamiento Histórico), CA-26 (Borrado Lógico).
 */
public interface FormDesignPort {
    /** Recupera todos los formularios cuyo estado no es DELETED. */
    List<FormDesignDTO> findAllActive();
    
    /** Recupera una versión histórica inmutable de un diseño de formulario. */
    Optional<FormDesignDTO> findByTechnicalNameAndVersion(String technicalName, Integer version);
    
    /** Busca un formulario por su identificador UUID interno. */
    Optional<FormDesignDTO> findById(UUID id);
    
    /** Devuelve el historial completo de versiones de un mismo patrón (mismo technicalName). */
    List<FormDesignDTO> findAllByTechnicalName(String technicalName);
    
    /** Recupera la versión más reciente (Top 1) dada una nomenclatura de formulario. */
    Optional<FormDesignDTO> findTopByTechnicalNameOrderByVersionDesc(String technicalName);
    
    // Commands
    /** Persiste un formulario completamente nuevo partiendo de un DTO. */
    FormDesignDTO createNew(FormDesignDTO dto);
    
    /** Guarda modificaciones. Si el formulario ya está activo, la lógica de negocio exige que se pase una nueva versión. */
    FormDesignDTO saveVersion(FormDesignDTO dto);
    
    /** Ejecuta un Soft Delete sobre el registro indicado (cambio de status a DELETED). */
    void updateStatusToDeleted(UUID id);
}
