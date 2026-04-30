package com.ibpms.poc.application.service.ui;

import com.ibpms.poc.application.dto.MenuTopologyDTO;
import com.ibpms.poc.infrastructure.jpa.entity.security.PermissionEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class MenuLayoutService {

    private final UserRepository userRepository;

    public MenuLayoutService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Cacheable(value = "menuTopology", key = "#username")
    @Transactional(readOnly = true)
    public MenuTopologyDTO computeTopologyForUser(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<String> activeMenus = new HashSet<>();

        for (RoleEntity role : user.getRoles()) {
            for (PermissionEntity permission : role.getPermissions()) {
                activeMenus.add(permission.getName());
            }
        }

        return new MenuTopologyDTO(activeMenus);
    }

    @CacheEvict(value = "menuTopology", key = "#username")
    public void invalidateMenuTopology(String username) {
        // Ejecución proxy para auto-curación (CA-32)
    }

    public void validateRoleImmutability(RoleEntity role) {
        if ("SUPER_ADMIN".equalsIgnoreCase(role.getName()) || "NATIVE_ADMIN".equalsIgnoreCase(role.getName())) {
            throw new AccessDeniedException("Native roles such as SUPER_ADMIN are immutable and cannot be altered.");
        }
    }
}
