package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.ProcessPermissionEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.application.service.security.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/roles")
public class RoleAdminController {

    private final RoleService roleService;

    public RoleAdminController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<RoleEntity> createRole(@RequestBody RoleEntity request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(request));
    }

    @GetMapping
    public ResponseEntity<List<RoleEntity>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    // CA-2 US-036: Blindaje ROOT — AccessDeniedException se traduce a 403 via GlobalExceptionHandler
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    // CA-2 US-036: Blindaje de mutación ROOT en endpoint de actualización
    @PutMapping("/{id}")
    public ResponseEntity<RoleEntity> updateRole(@PathVariable UUID id, @RequestBody RoleEntity patch) {
        return ResponseEntity.ok(roleService.updateRole(id, patch));
    }

    /**
     * CA-3 US-036 — Asignación Masiva desde Plantilla.
     * POST /api/v1/admin/roles/{templateId}/assign-massively
     * Body: ["uuid-1", "uuid-2", ...]
     * Respuesta 200: { "assigned": N, "notFound": [...] }
     */
    @PostMapping("/{templateId}/assign-massively")
    public ResponseEntity<Map<String, Object>> assignMassively(
            @PathVariable UUID templateId,
            @RequestBody List<UUID> userIds) {
        List<UUID> notFound = roleService.assignTemplateToUsers(templateId, userIds);
        int assigned = userIds.size() - notFound.size();
        return ResponseEntity.ok(Map.of(
                "assigned", assigned,
                "notFound", notFound
        ));
    }

    /**
     * CA-6 US-036 — Permisos Efectivos con Herencia Piramidal.
     * GET /api/v1/admin/roles/{id}/effective-permissions
     * Devuelve la lista aplanada de ProcessPermissions del rol y todos sus ancestros.
     */
    @GetMapping("/{id}/effective-permissions")
    public ResponseEntity<List<ProcessPermissionEntity>> getEffectivePermissions(@PathVariable UUID id) {
        return ResponseEntity.ok(roleService.getEffectiveProcessPermissions(id));
    }

    // CA-16: Exportador de Informe Denso
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportRolesCsv() {
        byte[] csvData = roleService.exportRoleMatrixToCsv();
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=matriz_rbac.csv")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csvData);
    }

    /**
     * CA-1 US-036 — Soporte Doble Motor (EntraID SSO).
     * GET /api/v1/admin/roles/entraid-groups
     * Devuelve la lista de grupos sincronizados desde Microsoft Graph API (Mock).
     */
    @GetMapping("/entraid-groups")
    public ResponseEntity<List<Map<String, String>>> getEntraIdGroups() {
        return ResponseEntity.ok(List.of(
                Map.of("id", "1111-2222-3333-4444", "displayName", "GG_IBPMS_Admins_Prod"),
                Map.of("id", "5555-6666-7777-8888", "displayName", "GG_IBPMS_Compliance_Readonly"),
                Map.of("id", "9999-0000-AAAA-BBBB", "displayName", "GG_IBPMS_Operations_Managers")
        ));
    }
}
