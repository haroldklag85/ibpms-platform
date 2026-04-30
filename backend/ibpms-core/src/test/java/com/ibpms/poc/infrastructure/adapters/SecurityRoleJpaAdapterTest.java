package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.domain.model.security.SecurityRole;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityRoleJpaAdapterTest {

    @Mock
    private RoleRepository repository;

    @InjectMocks
    private SecurityRoleJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByIsVipRestrictedTrue_ReturnsDomains() {
        RoleEntity entity = new RoleEntity();
        entity.setName("VIP_ROLE");
        entity.setIsVipRestricted(true);
        when(repository.findByIsVipRestrictedTrue()).thenReturn(List.of(entity));

        List<SecurityRole> result = adapter.findByIsVipRestrictedTrue();

        assertEquals(1, result.size());
        assertEquals("VIP_ROLE", result.get(0).getName());
    }
}
