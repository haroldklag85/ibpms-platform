package com.ibpms.poc.infrastructure.adapters.security;

import com.ibpms.poc.application.port.out.UserPort;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
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
