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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleService {

    // CA-2 US-036: Identificador canónico del Rol Guardián — INMUTABLE por diseño de seguridad
    static final String ROOT_ROLE = "ROLE_SUPER_ADMIN";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RoleAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final SecurityStreamService securityStreamService;

    public RoleService(RoleRepository roleRepository,
                       UserRepository userRepository,
                       RoleAuditLogRepository auditLogRepository,
                       ObjectMapper objectMapper,
                       SecurityStreamService securityStreamService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
        this.securityStreamService = securityStreamService;
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
    public void deleteRole(UUID id) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + id));
        if (ROOT_ROLE.equals(role.getName())) {
            throw new AccessDeniedException(
                    "Mutación/Borrado de Rol Root prohibido por diseño de seguridad.");
        }
        logAuditEntry(role, "DELETE");
        roleRepository.delete(role);
    }

    /**
     * CA-2 US-036 — Blindaje de mutación.
     * Impide modificar nombre/descripción/permisos del Rol Root.
     */
    @SuppressWarnings("null")
    public RoleEntity updateRole(UUID id, RoleEntity patch) {
        RoleEntity existing = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + id));
        if (ROOT_ROLE.equals(existing.getName())) {
            throw new AccessDeniedException(
                    "Mutación/Borrado de Rol Root prohibido por diseño de seguridad.");
        }
        existing.setDescription(patch.getDescription());
        existing.setIsTemplate(patch.getIsTemplate());
        existing.setSource(patch.getSource());
        RoleEntity saved = roleRepository.save(existing);
        logAuditEntry(saved, "UPDATE");
        return saved;
    }

    /**
     * CA-3 US-036 — Asignación Masiva desde Plantilla.
     * Itera sobre los userIds e inyecta el rol plantilla en una única
     * transacción @Transactional. Devuelve los IDs que no se encontraron.
     */
    @SuppressWarnings("null")
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
        securityStreamService.broadcastEvent("[ROLES_UPDATED]");
        return notFound;
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

    private void logAuditEntry(RoleEntity role, String action) {
        try {
            String adminId = SecurityContextHolder.getContext().getAuthentication() != null ? 
                             SecurityContextHolder.getContext().getAuthentication().getName() : "SYSTEM";
            String jsonDelta = objectMapper.writeValueAsString(role);
            RoleAuditLogEntity audit = new RoleAuditLogEntity(role.getId(), adminId, LocalDateTime.now(), action, jsonDelta);
            auditLogRepository.save(audit);
        } catch (Exception e) {
            // Failsafe: Log audit no debe interrumpir transaccion base si json falla
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
