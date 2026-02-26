package com.ibpms.poc.application.port.out;

import com.ibpms.poc.domain.model.Expediente;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida: Contrato de persistencia del Expediente.
 * La capa de infraestructura (JPA) implementa esta interfaz.
 */
public interface ExpedienteRepositoryPort {

    /** Persiste un nuevo expediente o actualiza uno existente. */
    Expediente save(Expediente expediente);

    /** Busca un expediente por su ID interno. */
    Optional<Expediente> findById(UUID id);

    /** Busca un expediente por su businessKey (deduplicación / idempotencia). */
    Optional<Expediente> findByBusinessKey(String businessKey);

    /** Lista todos los expedientes activos (paginado). */
    List<Expediente> findAll(int page, int size);
}
