// @Traceability: US-003 - ADR-001
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
    private final com.ibpms.poc.application.service.JwtBlacklistService jwtBlacklistService;
    private final com.ibpms.poc.application.service.security.RoleHierarchyService roleHierarchyService;
    private final com.ibpms.poc.application.service.security.EntraIdSyncService entraIdSyncService;
    private final com.ibpms.poc.infrastructure.jpa.repository.security.RoleDelegationRepository roleDelegationRepository;

    // @Traceability(US="US-036", CA="CA-08", DESC="ADR-001 Inyección de Puertos y Servicios de Dominio (EntraIdSyncService)")
    public JwtAuthFilter(@org.springframework.context.annotation.Lazy JwtTokenProvider jwtTokenProvider, 
                         @org.springframework.context.annotation.Lazy com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository userRepository,
                         @org.springframework.context.annotation.Lazy com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository roleRepository,
                         @org.springframework.context.annotation.Lazy com.ibpms.poc.application.service.JwtBlacklistService jwtBlacklistService,
                         @org.springframework.context.annotation.Lazy com.ibpms.poc.application.service.security.RoleHierarchyService roleHierarchyService,
                         @org.springframework.context.annotation.Lazy com.ibpms.poc.application.service.security.EntraIdSyncService entraIdSyncService,
                         @org.springframework.context.annotation.Lazy com.ibpms.poc.infrastructure.jpa.repository.security.RoleDelegationRepository roleDelegationRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtBlacklistService = jwtBlacklistService;
        this.roleHierarchyService = roleHierarchyService;
        this.entraIdSyncService = entraIdSyncService;
        this.roleDelegationRepository = roleDelegationRepository;
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
                String tokenIdentifier;
                try {
                    String jti = jwtTokenProvider.getClaim(token, "jti");
                    tokenIdentifier = jti;
                } catch (Exception parseException) {
                    tokenIdentifier = null;
                }
                // @Traceability(US="US-036", CA="CA-14", DESC="Híbrido: Generación de tokenIdentifier (hash) con validación isTokenRevoked de DevDavid")
                if (tokenIdentifier == null) {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    StringBuilder hexString = new StringBuilder();
                    for (byte b : hash) {
                        hexString.append(String.format("%02x", b));
                    }
                    tokenIdentifier = hexString.toString();
                }

                String subject = jwtTokenProvider.getSubject(token);
                
                if (jwtBlacklistService.isTokenRevoked(tokenIdentifier) || jwtBlacklistService.isUserRevoked(subject)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token o sesión del usuario revocado administrativamente (Kill-Session).");
                    return;
                }
            } catch (Exception e) {
                // CA-01: Fail-Open Policy. Resiliencia ante caída del motor de Invalidación (Timeout Redis/DB).
                logger.error("[SRE RESILIENCE] Redis Fail-Open CATCH: Lista Negra inaccesible. Confiando en la criptografía del Token. Causa: " + e.getMessage());
                String method = request.getMethod();
                if (!"GET".equalsIgnoreCase(method) && !"OPTIONS".equalsIgnoreCase(method)) {
                    response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Sistema degradado. Mutaciones deshabilitadas.");
                    return;
                }
            }

            if (jwtTokenProvider.isValid(token)) {
                String subject = jwtTokenProvider.getSubject(token);
                
                // @Traceability(US="US-036", CA="CA-08", DESC="Híbrido: JIT Provisioning con Mutex(this) delegando al Servicio de Dominio EntraIdSyncService")
                java.util.Optional<com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity> userOpt = userRepository.findByUsername(subject);
                if (userOpt.isEmpty()) {
                    synchronized (this) {
                        userOpt = userRepository.findByUsername(subject);
                        if (userOpt.isEmpty()) {
                            try {
                                java.util.Map<String, String> claims = new java.util.HashMap<>();
                                claims.put("email", subject + "@sso.local");
                                claims.put("name", subject);
                                claims.put("Sucursal_ID", jwtTokenProvider.getClaim(token, "Sucursal_ID"));
                                claims.put("Codigo_Jefe", jwtTokenProvider.getClaim(token, "Codigo_Jefe"));

                                com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity newUser = 
                                    entraIdSyncService.provisionUser(subject, claims);
                                userOpt = java.util.Optional.of(newUser);
                            } catch (com.ibpms.poc.application.service.security.exceptions.PreconditionRequiredException e) {
                                response.setContentType("application/json");
                                response.setStatus(428);
                                response.getWriter().write("{\"error\": \"Precondition Required\", \"missing_fields\": " + new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(e.getMissingFields()) + "}");
                                return;
                            } catch (Exception e) {
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Error en aprovisionamiento JIT: " + e.getMessage());
                                return;
                            }
                        }
                    }
                }
                // CA-07 Soft-Delete: Interceptamos Token Vivo si el Usuario fue Desactivado
                if (com.ibpms.poc.infrastructure.jpa.entity.security.UserStatus.INACTIVE.equals(userOpt.get().getStatus())) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuario inactivo o revocado localmente (Soft-Delete).");
                    return;
                }

                // CA-02: Filtro de la Mochila Pesada (Anti-Token Bloat HTTP 431)
                List<String> rawRoles = jwtTokenProvider.getRoles(token);
                List<String> roles = rawRoles.stream()
                        .filter(r -> r.startsWith("ibpms_rol_") || r.startsWith("ROLE_"))
                        .map(r -> r.replace("ibpms_rol_", "").replace("ROLE_", ""))
                        .collect(Collectors.toList());
                
                // @Traceability: Retro-Remediación RBAC J-04 (T-20.4)
                // Carga de roles desde la Base de Datos para hibridación local
                for (com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity r : userOpt.get().getRoles()) {
                    String rName = r.getName().replace("ROLE_", "");
                    if (!roles.contains(rName)) {
                        roles.add(rName);
                    }
                }
                
                // CA-9 Inyección Dinámica de Delegaciones (Sustituciones Temporales)
                java.util.List<com.ibpms.poc.infrastructure.jpa.entity.security.RoleDelegationEntity> activeDelegations = 
                        roleDelegationRepository.findActiveDelegationsForDelegate(userOpt.get().getId(), java.time.LocalDateTime.now());
                
                for (com.ibpms.poc.infrastructure.jpa.entity.security.RoleDelegationEntity delegation : activeDelegations) {
                    if (delegation.getOwner() != null) {
                        for (com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity r : delegation.getOwner().getRoles()) {
                            String rName = r.getName().replace("ROLE_", "");
                            if (!roles.contains(rName)) roles.add(rName);
                        }
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
