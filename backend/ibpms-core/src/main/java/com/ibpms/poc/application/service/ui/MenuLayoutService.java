package com.ibpms.poc.application.service.ui;

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
import java.util.List;

@Service
public class MenuLayoutService {

    private static final List<String> MACRO_MODULES = List.of(
            "WORKDESK", "SERVICE_DELIVERY", "BAM", "MODELER", "INTEGRATION", "PROJECTS", "ADMINISTRATION"
    );

    private final UserRepository userRepository;

    public MenuLayoutService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Cacheable(value = "menuTopology", key = "#username")
    @Transactional(readOnly = true)
    public Set<String> computeTopologyForUser(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Early Return: Bypass para roles nativos (SUPER_ADMIN, SYSTEM_ADMIN)
        boolean isNativeAdmin = user.getRoles().stream()
                .anyMatch(role -> "SUPER_ADMIN".equalsIgnoreCase(role.getName()) || 
                                  "SYSTEM_ADMIN".equalsIgnoreCase(role.getName()) ||
                                  "ROLE_SUPER_ADMIN".equalsIgnoreCase(role.getName()) ||
                                  "ROLE_SYSTEM_ADMIN".equalsIgnoreCase(role.getName()));
                                  
        if (isNativeAdmin) {
            return java.util.Collections.unmodifiableSet(new HashSet<>(MACRO_MODULES));
        }

        Set<String> activeMenus = new HashSet<>();

        for (RoleEntity role : user.getRoles()) {
            if (role.getPermissions() != null) {
                for (PermissionEntity permission : role.getPermissions()) {
                    String permName = permission.getName().toUpperCase();
                    for (String module : MACRO_MODULES) {
                        if (permName.contains(module)) {
                            activeMenus.add(module);
                        }
                    }
                }
            }
        }

        return activeMenus;
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
