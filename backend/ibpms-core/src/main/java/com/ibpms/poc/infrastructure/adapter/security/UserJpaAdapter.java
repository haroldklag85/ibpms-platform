// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.adapter.security;

import com.ibpms.poc.application.port.out.UserPort;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador JPA para el puerto de Usuarios.
 * Permite a la capa de negocio consultar usuarios sin depender de Spring Data JPA.
 */
@Component
@Traceability(US = "US-001", CA = {"CA-28"})
public class UserJpaAdapter implements UserPort {

    private final UserRepository repository;

    public UserJpaAdapter(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        return repository.findByUsername(username);
    }
}
