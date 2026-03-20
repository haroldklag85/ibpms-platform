package com.ibpms.poc.infrastructure.jpa.repository.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    
    // Optimizador para Kill-Switch sin traer toda la entidad
    @org.springframework.data.jpa.repository.Query("SELECT u.isActive FROM UserEntity u WHERE u.username = :username")
    Optional<Boolean> isUserActive(String username);
}
