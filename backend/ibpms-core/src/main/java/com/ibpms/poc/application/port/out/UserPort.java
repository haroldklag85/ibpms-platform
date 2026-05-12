package com.ibpms.poc.application.port.out;

import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;

import java.util.Optional;

public interface UserPort {
    Optional<UserEntity> findByUsername(String username);
}
