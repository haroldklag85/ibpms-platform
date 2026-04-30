package com.ibpms.poc.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.lang.NonNull;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filtro JWT stateless — se ejecuta una sola vez por request.
 * Lee la cabecera Authorization: Bearer <token>, lo valida con JwtTokenProvider
 * y puebla el SecurityContextHolder con el principal y los roles del token.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository userRepository;
    private final com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository roleRepository;
    private final com.ibpms.poc.infrastructure.jpa.repository.security.DelegationRepository delegationRepository;
    private final com.ibpms.poc.infrastructure.jpa.repository.security.TokenBlacklistRepository tokenBlacklistRepository;
    private final com.ibpms.poc.application.service.security.RoleHierarchyService roleHierarchyService;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider, 
                         com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository userRepository,
                         com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository roleRepository,
                         com.ibpms.poc.infrastructure.jpa.repository.security.DelegationRepository delegationRepository,
                         com.ibpms.poc.infrastructure.jpa.repository.security.TokenBlacklistRepository tokenBlacklistRepository,
                         com.ibpms.poc.application.service.security.RoleHierarchyService roleHierarchyService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.delegationRepository = delegationRepository;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.roleHierarchyService = roleHierarchyService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(AUTHORIZATION_HEADER);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length()).trim();

            // CA-14 y CA-01: Exorcismo JWT con Tolerancia a Fallos (Fail-Open)
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    hexString.append(String.format("%02x", b));
                }
                if (tokenBlacklistRepository.existsByTokenSignature(hexString.toString())) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token purgado en la Lista Negra (Kill-Session).");
                    return;
                }
            } catch (Exception e) {
                // CA-01: Fail-Open Policy. Resiliencia ante caída del motor de Invalidación (Timeout Redis/DB).
                logger.error("[SRE RESILIENCE] Redis Fail-Open CATCH: Lista Negra inaccesible. Confiando en la criptografía del Token. Causa: " + e.getMessage());
            }

            if (jwtTokenProvider.isValid(token)) {
                String subject = jwtTokenProvider.getSubject(token);
                
                // CA-8 JIT Provisioning (Aprovisionamiento Silencioso SSO)
                java.util.Optional<com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity> userOpt = userRepository.findByUsername(subject);
                if (userOpt.isEmpty()) {
                    com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity newUser = new com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity();
                    newUser.setUsername(subject);
                    newUser.setEmail(subject + "@sso.local"); // Stub, idealmente vendría en el claim
                    newUser.setIsExternalIdp(true);
                    newUser.setIsActive(true);
                    com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity baseRole = roleRepository.findByName("ROLE_CIUDADANO_INTERNO")
                            .orElseGet(() -> roleRepository.save(new com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity("ROLE_CIUDADANO_INTERNO", "JIT Default Role")));
                    newUser.getRoles().add(baseRole);
                    userRepository.save(newUser);
                    userOpt = java.util.Optional.of(newUser);
                }
                
                // CA-5 Kill Switch: Interceptamos Token Vivo si el Usuario fue Desactivado
                if (!userOpt.get().getIsActive()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuario inactivo o revocado localmente (Kill-Switch).");
                    return;
                }

                // CA-02: Filtro de la Mochila Pesada (Anti-Token Bloat HTTP 431)
                List<String> rawRoles = jwtTokenProvider.getRoles(token);
                List<String> roles = rawRoles.stream()
                        .filter(r -> r.startsWith("ibpms_rol_"))
                        .map(r -> r.replace("ibpms_rol_", ""))
                        .collect(Collectors.toList());
                
                // CA-9 Inyección Dinámica de Delegaciones (Sustituciones Temporales)
                java.util.List<com.ibpms.poc.infrastructure.jpa.entity.security.DelegationEntity> activeDelegations = 
                        delegationRepository.findActiveDelegationsForSubstitute(userOpt.get().getId(), java.time.LocalDateTime.now());
                
                for (com.ibpms.poc.infrastructure.jpa.entity.security.DelegationEntity delegation : activeDelegations) {
                    for (com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity r : delegation.getDelegator().getRoles()) {
                        String rName = r.getName().replace("ROLE_", "");
                        if (!roles.contains(rName)) roles.add(rName);
                    }
                }

                // US-036 CA-6: Enriquecer roles directos con herencia piramidal CTE
                try {
                    java.util.Set<String> directRoleSet = new java.util.LinkedHashSet<>(roles);
                    java.util.Set<String> effectiveRoles = roleHierarchyService.resolveAllEffectiveRoles(directRoleSet);
                    roles = new java.util.ArrayList<>(effectiveRoles);
                } catch (Exception e) {
                    // Fail-Open: Si la jerarquía falla, mantenemos los roles directos del token.
                    logger.warn("[SRE RESILIENCE] Role hierarchy resolution failed. Using direct roles only. Causa: " + e.getMessage());
                }

                var authorities = roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toList());

                var auth = new UsernamePasswordAuthenticationToken(subject, null, authorities);
                String tenantId = jwtTokenProvider.getClaim(token, "tenant_id");
                if (tenantId != null) {
                    auth.setDetails(java.util.Map.of("tenant_id", tenantId));
                }
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            // Si el token es inválido, o usuario revocado, no se establece contexto → Spring devuelve 401
        }
        chain.doFilter(request, response);
    }
}
