package com.ibpms.poc.application.service.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleAuditLogRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public RoleService(RoleRepository roleRepository, RoleAuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.roleRepository = roleRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public RoleEntity createRole(RoleEntity role) {
        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new IllegalArgumentException("El rol ya existe.");
        }
        RoleEntity saved = roleRepository.save(role);
        logAuditEntry(saved, "CREATE");
        return saved;
    }

    public List<RoleEntity> getAllRoles() {
        return roleRepository.findAll();
    }

    @SuppressWarnings("null")
    public void deleteRole(UUID id) {
        roleRepository.findById(id).ifPresent(r -> logAuditEntry(r, "DELETE"));
        roleRepository.deleteById(id);
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
