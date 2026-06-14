# Handoff Backend - US-036 Identity Governance (CA-01 a CA-06)

## 1. Metadatos y SSOT
- **Iteración:** 03-DEV-DAVID
- **Rama:** `DevDavid`
- **US:** US-036 (RBAC & Identity Governance)
- **CAs:** CA-01, CA-02, CA-03, CA-04, CA-05, CA-06
- **SSOT:** [epic_E_seguridad_identidad_config.md](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/docs/requirements/epics/epic_E_seguridad_identidad_config.md)
- **Orden:** Back -> Front -> QA

## 2. Alineación Arquitectónica
- **ADR-001 (Hexagonal):** Mantener lógica de negocio en `application/service` y entidades en `infrastructure/jpa`.
- **Zero-Trust:** El `ROLE_SUPER_ADMIN` es inmutable y no puede ser borrado ni modificado.
- **ISO 27001:** Cada cambio en roles debe disparar un `RoleAuditLogEntity`.

## 3. Rutas Exactas y Contexto
- **Controller:** [RoleAdminController.java](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/security/RoleAdminController.java)
- **Service:** [RoleService.java](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/security/RoleService.java)
- **Entidades:** 
    - [RoleEntity.java](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/security/RoleEntity.java)
    - [ProcessPermissionEntity.java](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/security/ProcessPermissionEntity.java)

## 4. Snippets Prescriptivos

### CA-01: Real-ish EntraID Mock Service
Actualmente `getEntraIdGroups` devuelve una lista hardcodeada. Crea `EntraIdSyncService` en `application/service/security` para simular la integración con Microsoft Graph.

```java
@Service
public class EntraIdSyncService {
    public List<Map<String, String>> fetchAvailableGroups() {
        // En V1, esto simula el fetch de grupos con prefijo GG_IBPMS
        return List.of(
            Map.of("id", "1111-2222", "displayName", "GG_IBPMS_Admins"),
            Map.of("id", "3333-4444", "displayName", "GG_IBPMS_Viewers")
        );
    }
}
```

### CA-02: Blindaje de Seguridad
Asegúrate de que `RoleService.deleteRole` y `updateRole` lancen `AccessDeniedException` si el rol es `ROLE_SUPER_ADMIN`. Esto ya está parcialmente implementado, pero valida que cubra todos los campos.

## 5. Matriz de QA (Backend)
| Test Name | CA Evaluado | Aserción Esperada |
|-----------|-------------|-------------------|
| `testDeleteSuperAdminFails` | CA-02 | Expect `AccessDeniedException` |
| `testMassAssignment` | CA-03 | Verify N users receive the role in 1 transaction |
| `testEffectivePermissionsInheritance` | CA-06 | Child role returns Child + Parent permissions |

## 6. Mensaje de Despacho
"Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B."
