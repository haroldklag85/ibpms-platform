package com.ibpms.poc.infrastructure.adapters.security;

import com.ibpms.poc.application.ports.out.ImpersonationPort;
import com.ibpms.poc.application.ports.out.RoleHierarchyPort;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

@Component
public class ImpersonationJpaAdapter implements ImpersonationPort {

    private final JdbcTemplate jdbcTemplate;
    private final RoleHierarchyPort roleHierarchyPort;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public ImpersonationJpaAdapter(JdbcTemplate jdbcTemplate, 
                                   RoleHierarchyPort roleHierarchyPort, 
                                   JwtTokenProvider jwtTokenProvider,
                                   UserRepository userRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.roleHierarchyPort = roleHierarchyPort;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public void logImpersonationEvent(UUID adminId, UUID targetUserId, String action, String ipAddress, String userAgent) {
        String correlationId = MDC.get("correlation_id");
        String sql = "INSERT INTO ibpms_impersonation_audit_log (id, admin_id, target_user_id, action, ip_address, user_agent, correlation_id, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        jdbcTemplate.update(sql, UUID.randomUUID(), adminId, targetUserId, action, ipAddress, userAgent, correlationId);
    }

    @Override
    public boolean isUserImpersonable(UUID targetUserId) {
        var userOpt = userRepository.findById(targetUserId);
        if (userOpt.isEmpty()) return false;
        
        var user = userOpt.get();
        Set<String> directRoles = new HashSet<>();
        user.getRoles().forEach(r -> directRoles.add(r.getName().replace("ROLE_", "")));
        
        Set<String> effectiveRoles = roleHierarchyPort.resolveAllEffectiveRoles(directRoles);
        return !effectiveRoles.contains("SUPER_ADMIN");
    }

    @Override
    public String generateImpersonationToken(UUID adminId, UUID targetUserId) {
        var userOpt = userRepository.findById(targetUserId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no existe");
        }
        var user = userOpt.get();
        List<String> roles = new ArrayList<>();
        user.getRoles().forEach(r -> roles.add(r.getName().replace("ROLE_", "")));

        // Aquí usamos generateImpersonationToken de JwtTokenProvider
        // Asumo que si adminId es null, no estamos impersonando a nadie (es el JWT de salida).
        return jwtTokenProvider.generateImpersonationToken(user.getUsername(), roles, "default_tenant", adminId != null ? adminId.toString() : null);
    }

    @Override
    public UUID getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getId())
                .orElse(null);
    }
}
