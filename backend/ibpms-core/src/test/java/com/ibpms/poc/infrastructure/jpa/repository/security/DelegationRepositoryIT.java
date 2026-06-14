package com.ibpms.poc.infrastructure.jpa.repository.security;

import com.ibpms.poc.AbstractIntegrationIT;
import com.ibpms.poc.infrastructure.jpa.entity.security.DelegationEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DelegationRepositoryIT extends AbstractIntegrationIT {

    @Autowired
    private DelegationRepository delegationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    @DisplayName("Debe persistir correctamente las relaciones delegator y substitute verificando sus UUIDs")
    void testDelegationPersistenceAndUUIDs() {
        // Arrange
        UserEntity delegator = new UserEntity();
        delegator.setUsername("delegator_test");
        delegator.setEmail("delegator_test@test.com");
        delegator.setCreatedAt(LocalDateTime.now());
        
        UserEntity substitute = new UserEntity();
        substitute.setUsername("substitute_test");
        substitute.setEmail("substitute_test@test.com");
        substitute.setCreatedAt(LocalDateTime.now());

        delegator = userRepository.saveAndFlush(delegator);
        substitute = userRepository.saveAndFlush(substitute);

        LocalDateTime now = LocalDateTime.now();
        DelegationEntity delegation = new DelegationEntity(delegator, substitute, now, now.plusDays(7));
        delegation.setReason("Prueba de Testcontainers");

        // Act
        DelegationEntity savedDelegation = delegationRepository.saveAndFlush(delegation);
        entityManager.clear(); // Limpiamos la caché de primer nivel para forzar la lectura desde la DB

        DelegationEntity retrievedDelegation = delegationRepository.findById(savedDelegation.getId()).orElseThrow();

        // Assert
        assertThat(retrievedDelegation).isNotNull();
        assertThat(retrievedDelegation.getDelegator().getId()).isEqualTo(delegator.getId());
        assertThat(retrievedDelegation.getSubstitute().getId()).isEqualTo(substitute.getId());
        assertThat(retrievedDelegation.getReason()).isEqualTo("Prueba de Testcontainers");
        assertThat(retrievedDelegation.getSubstitute().getId()).isInstanceOf(UUID.class);
    }
}
