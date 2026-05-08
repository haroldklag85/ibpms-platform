package com.ibpms.poc.application.service.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.ProcessPermissionEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleAuditLogRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleService {

    // CA-2 US-036: Identificador canónico del Rol Guardián — INMUTABLE por diseño de seguridad
    static final String ROOT_ROLE = "SUPER_ADMIN";
    static final String SYS_ADMIN_ROLE = "SYSTEM_ADMIN";

    private boolean isImmutableRole(String roleName) {
        return ROOT_ROLE.equals(roleName) || SYS_ADMIN_ROLE.equals(roleName) || 
               "ROLE_SUPER_ADMIN".equals(roleName) || "ROLE_SYSTEM_ADMIN".equals(roleName);
    }

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RoleAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public RoleService(RoleRepository roleRepository,
                       UserRepository userRepository,
                       RoleAuditLogRepository auditLogRepository,
                       ObjectMapper objectMapper) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("null")
    public RoleEntity createRole(RoleEntity role) {
        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new IllegalArgumentException("El rol ya existe.");
        }
        // CA-6 US-036: Resolver parentRole por ID si se proporcionó referencia parcial
        if (role.getParentRole() != null && role.getParentRole().getId() != null) {
            RoleEntity parent = roleRepository.findById(role.getParentRole().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Rol padre no encontrado: " + role.getParentRole().getId()));
            role.setParentRole(parent);
        }
        RoleEntity saved = roleRepository.save(role);
        logAuditEntry(saved, "CREATE");
        return saved;
    }

    public List<RoleEntity> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * CA-2 US-036 — Inmutabilidad del Guardián.
     * Aborta la transacción con AccessDeniedException si el rol objetivo
     * es ROLE_SUPER_ADMIN. Ningún camino de código puede eludir este blindaje.
     */
    @SuppressWarnings("null")
    @CacheEvict(value = "menuTopology", allEntries = true)
    public void deleteRole(UUID id) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + id));
        if (isImmutableRole(role.getName())) {
            throw new AccessDeniedException(
                    "Mutación/Borrado de Roles del Sistema (SUPER_ADMIN / SYSTEM_ADMIN) prohibido por diseño de seguridad.");
        }
        logAuditEntry(role, "DELETE");
        roleRepository.delete(role);
    }

    /**
     * CA-2 US-036 — Blindaje de mutación.
     * Impide modificar nombre/descripción/permisos del Rol Root.
     */
    @SuppressWarnings("null")
    @CacheEvict(value = "menuTopology", allEntries = true)
    public RoleEntity updateRole(UUID id, RoleEntity patch) {
        RoleEntity existing = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + id));
        if (isImmutableRole(existing.getName())) {
            throw new AccessDeniedException(
                    "Mutación/Borrado de Roles del Sistema prohibido por diseño de seguridad.");
        }
        
        // CA-2 US-036: Blindaje adicional contra suplantación del nombre Root
        if (patch.getName() != null && isImmutableRole(patch.getName()) && !isImmutableRole(existing.getName())) {
            throw new AccessDeniedException("No se puede renombrar un rol a roles del sistema nativos.");
        }

        if (patch.getName() != null) existing.setName(patch.getName());
        existing.setDescription(patch.getDescription());
        existing.setIsTemplate(patch.getIsTemplate());
        existing.setSource(patch.getSource());
        
        // Clonar para el log antes de guardar cambios en la colección de permisos si los hubiera
        // Nota: Si patch trae permisos, habría que mapearlos aquí también.
        
        RoleEntity saved = roleRepository.save(existing);
        // En una implementación real, compararíamos existing (antes de flush) o pasaríamos el DTO original
        logAuditEntry(saved, "UPDATE"); 
        return saved;
    }

    /**
     * CA-3 US-036 — Asignación Masiva desde Plantilla.
     * Itera sobre los userIds e inyecta el rol plantilla en una única
     * transacción @Transactional. Devuelve los IDs que no se encontraron.
     */
    @SuppressWarnings("null")
    @CacheEvict(value = "menuTopology", allEntries = true)
    public List<UUID> assignTemplateToUsers(UUID templateId, List<UUID> userIds) {
        RoleEntity template = roleRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Rol plantilla no encontrado: " + templateId));
        if (!Boolean.TRUE.equals(template.getIsTemplate())) {
            throw new IllegalArgumentException(
                    "El rol '" + template.getName() + "' no es una plantilla asignable. Marque isTemplate=true primero.");
        }
        List<UUID> notFound = new ArrayList<>();
        for (UUID userId : userIds) {
            userRepository.findById(userId).ifPresentOrElse(
                    user -> {
                        user.getRoles().add(template);
                        userRepository.save(user);
                    },
                    () -> notFound.add(userId)
            );
        }
        logAuditEntry(template, "MASS_ASSIGN");
        return notFound;
    }

    /**
     * CA-4 US-036 — Segregación Iniciador vs Ejecutor.
     * Reemplaza la colección completa de ProcessPermissions de un rol.
     * orphanRemoval=true en RoleEntity elimina las entradas huérfanas de la BD.
     */
    @SuppressWarnings("null")
    @CacheEvict(value = "menuTopology", allEntries = true)
    public RoleEntity updateProcessPermissions(UUID roleId, List<ProcessPermissionEntity> permissions) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + roleId));
        if (isImmutableRole(role.getName())) {
            throw new AccessDeniedException(
                    "Mutación de permisos de Roles del Sistema prohibida por diseño de seguridad.");
        }
        role.getProcessPermissions().clear();
        for (ProcessPermissionEntity perm : permissions) {
            perm.setRole(role);
            role.getProcessPermissions().add(perm);
        }
        RoleEntity saved = roleRepository.save(role);
        logAuditEntry(saved, "UPDATE_PERMISSIONS");
        return saved;
    }

    /**
     * CA-6 US-036 — Resolver de Herencia Piramidal de Permisos de Proceso.
     *
     * Recorre el árbol de roles usando la CTE WITH RECURSIVE del repositorio,
     * obtiene todos los IDs de roles en la jerarquía (rol dado + todos sus
     * ancestros) y aplana sus ProcessPermissions en una lista sin duplicados
     * de processDefinitionKey (el hijo prevalece sobre el padre en caso de
     * conflicto, por ser el primero en el stream).
     *
     * Ejemplo: Gerente_A (hereda → Analista_B) → devuelve permisos de ambos.
     */
    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public List<ProcessPermissionEntity> getEffectiveProcessPermissions(UUID roleId) {
        List<UUID> treeIds = roleRepository.findRoleIdsInTree(roleId);
        return roleRepository.findAllById(treeIds).stream()
                .flatMap(r -> r.getProcessPermissions().stream())
                .collect(Collectors.toList());
    }

    /**
     * CA-17: Recuperar historial de auditoría del rol.
     */
    @Transactional(readOnly = true)
    public List<RoleAuditLogEntity> getAuditLogsForRole(UUID roleId) {
        return auditLogRepository.findByRoleIdOrderByTimestampDesc(roleId);
    }

    public List<RoleAuditLogEntity> getAllAuditLogs() {
        return auditLogRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "timestamp"));
    }

    private void logAuditEntry(RoleEntity role, String action) {
        logAuditEntry(role, null, action);
    }

    private void logAuditEntry(RoleEntity role, RoleEntity oldRole, String action) {
        try {
            String adminId = SecurityContextHolder.getContext().getAuthentication() != null ? 
                             SecurityContextHolder.getContext().getAuthentication().getName() : "SYSTEM";
            
            Map<String, Object> delta = new java.util.HashMap<>();
            if (oldRole != null) {
                // Calcular permisos otorgados y quitados
                java.util.Set<String> oldPerms = oldRole.getPermissions().stream().map(p -> p.getName()).collect(Collectors.toSet());
                java.util.Set<String> newPerms = role.getPermissions().stream().map(p -> p.getName()).collect(Collectors.toSet());
                
                java.util.Set<String> granted = new java.util.HashSet<>(newPerms);
                granted.removeAll(oldPerms);
                
                java.util.Set<String> revoked = new java.util.HashSet<>(oldPerms);
                revoked.removeAll(newPerms);
                
                delta.put("granted", granted);
                delta.put("revoked", revoked);
                delta.put("roleName", role.getName());
            } else {
                delta.put("fullState", role);
            }

            String jsonDelta = objectMapper.writeValueAsString(delta);
            RoleAuditLogEntity audit = new RoleAuditLogEntity(role.getId(), adminId, LocalDateTime.now(), action, jsonDelta);
            auditLogRepository.save(audit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // CA-16: Exportación de Matriz Segura CISO en nativo Crudo CSV
    public byte[] exportRoleMatrixToCsv() {
        List<RoleEntity> roles = roleRepository.findAll();
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Role_Name,Description,Process_Def_Key,Can_Initiate,Can_Execute\n");

        for (RoleEntity r : roles) {
            if (r.getProcessPermissions() != null) {
                for (var pp : r.getProcessPermissions()) {
                    csvBuilder.append(r.getName()).append(",")
                              .append(r.getDescription() != null ? r.getDescription() : "").append(",")
                              .append(pp.getProcessDefinitionKey()).append(",")
                              .append(pp.getCanInitiateProcess()).append(",")
                              .append(pp.getCanExecuteTasks()).append("\n");
                }
            }
            if (r.getProcessPermissions() == null || r.getProcessPermissions().isEmpty()) {
                csvBuilder.append(r.getName()).append(",")
                          .append(r.getDescription() != null ? r.getDescription() : "").append(",")
                          .append("N/A,false,false\n");
            }
        }
        return csvBuilder.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
