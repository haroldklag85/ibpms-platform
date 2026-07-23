package com.ibpms.poc.application.port.out;

import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.Optional;

/**
 * Puerto de salida para acceder a la información de Usuarios.
 */
@Traceability(US = "US-001", CA = {"CA-28"})
public interface UserPort {
    Optional<UserEntity> findByUsername(String username);
}
