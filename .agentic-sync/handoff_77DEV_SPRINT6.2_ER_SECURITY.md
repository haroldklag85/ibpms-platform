---
name: Handoff de Remediación ER y Seguridad (HO-04 a HO-10)
version: 1.0.0
---

# 🏗️ Protocolo de Handoff Arquitectónico (Zero-Hallucination)

## 📌 1. Metadatos y SSOT (Single Source of Truth)
- **Iteración/Sprint:** `sprint-6` / Iteración 6.2 (Cierre Obligatorio)
- **Criterios de Aceptación (CAs):** Remediación de deuda técnica en esquemas ER y seguridad (HO-04 a HO-10).
- **SSOT:** `docs/requirements/epics/epic_A_motor_core.md`
- **Flujo de Trabajo:** Backend (DB Migrations & JPA) -> QA (Integridad Referencial).

## 📌 2. Alineación Arquitectónica y ADRs
Este desarrollo rige bajo los siguientes principios documentados en `docs/architecture`:
- **ADR-001 (Hexagonal Architecture):** Todas las validaciones de base de datos (`domain`/`infrastructure`) deben mantenerse estrictamente desacopladas para prever la evolución a un esquema V2 con Kubernetes y aislar `ibpms_*` del motor Camunda.
- **Data Architecture ERD (`data_architecture_erd.md`):** Garantizar la eliminación de antípatrones (esquemas triplicados de roles, tablas huérfanas) validando el Patrón Dual-Schema y la referencialidad cruzada a entidades unificadas. Se debe perseguir inmutabilidad y normalización.
- **Zero-Trust Security:** Eliminación completa de roles hardcodeados (fallback) en endpoints críticos (`AuthSyncController`), transicionando hacia un *fail-fast* si un usuario carece de roles pre-existentes en base de datos. Enforzar restricción `UNIQUE` en tablas asociativas RBAC.

## 📌 3. Rutas Exactas y Contexto Preexistente
A continuación, se listan las 7 tareas críticas delegadas a los Agentes de Backend, detallando el path exacto y la situación actual. Estrictamente deben ejecutarse en `sprint-6.2`.

### HO-04: Consolidar esquema triple de roles
- **Contexto Actual:** Coexisten `ibpms_security_role` (JPA `RoleEntity`), `ibpms_roles` (`IbpmsRoleEntity`) y `sys_role` (`SysRoleEntity`). Solamente la primera está alineada funcionalmente.
- **Paths a modificar:**
  - `backend/ibpms-core/src/main/resources/db/changelog/29-consolidate-roles.sql` (Crear nuevo)
  - Clases JPA que referencean `IbpmsRoleEntity` y `SysRoleEntity`.

### HO-05: Integridad referencial de `tenant_id`
- **Contexto Actual:** `tenant_id` se usa como VARCHAR suelto sin FK (Antipatrón).
- **Paths a modificar:**
  - `backend/ibpms-core/src/main/resources/db/changelog/30-tenant-fk.sql` (Crear nuevo)

### HO-06: Entidades JPA faltantes para tablas huérfanas
- **Contexto Actual:** Existen tablas `kanban_column` y `dmn_definition` sin `@Entity` asociada, quedando huérfanas ante la vista JPA.
- **Paths a modificar:**
  - Crear `KanbanColumnEntity.java` en `com/ibpms/poc/infrastructure/jpa/entity/kanban/` o eliminar tablas si se confirma irrelevancia.

### HO-07: Eliminar fallback hardcodeado de `AuthSyncController`
- **Contexto Actual:** El método `emergencyLogin` inyecta `"ROLE_SUPER_ADMIN"` a cuentas sin roles. Fila de vulnerabilidad crítica IDOR.
- **Paths a modificar:**
  - `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/security/AuthSyncController.java`

### HO-08: Eliminar tablas legacy del seed
- **Contexto Actual:** `seed-e2e.sql` contiene `CREATE TABLE users`, `user_roles`, `user_delegation` de sprints tempranos (ahora muertas).
- **Paths a modificar:**
  - `backend/ibpms-core/src/main/resources/seed-e2e.sql`

### HO-09: Agregar constraint UNIQUE a `ibpms_security_user_roles`
- **Contexto Actual:** Hibernate genera la tabla pivote, pero el script de Liquibase/Seed carece de la constraint UNIQUE (PK compuesta) explícita que previene registros duplicados.
- **Paths a modificar:**
  - Crear `backend/ibpms-core/src/main/resources/db/changelog/31-ensure-pk-user-roles.sql`

### HO-10: Documentar esquema ER canónico post-consolidación
- **Contexto Actual:** Diagrama original desactualizado respecto a la limpieza que estamos realizando.
- **Paths a modificar:**
  - `docs/architecture/er_model_canonical.md`

## 📌 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

**1. Snippet para HO-07 (Backend / Fail-Fast Auth):**
En `AuthSyncController.java`, reemplazar el fallback por la siguiente excepción (Zero-Trust):
```java
// Reemplazar la asignación por defecto "ROLE_SUPER_ADMIN" con un fail-fast estricto
if (roles.isEmpty()) {
    log.error("Zero-Trust Violation: El usuario {} no posee roles asignados en DB.", username);
    throw new AccessDeniedException("User has no roles assigned in ibpms_security_user_roles");
}
```

**2. Snippet para HO-05 (Liquibase DDL tenant_id):**
```sql
-- liquibase formatted sql
-- changeset antigravity:30-tenant-fk

CREATE TABLE ibpms_tenant (
    slug VARCHAR(100) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO ibpms_tenant (slug, name) VALUES ('tenant_alpha', 'Tenant Primario de Operaciones');

-- Agregar FKs a todas las tablas base de dominio
ALTER TABLE ibpms_workdesk_projection 
ADD CONSTRAINT fk_workdesk_tenant FOREIGN KEY (tenant_id) REFERENCES ibpms_tenant(slug);
```

**3. Snippet para HO-09 (Liquibase DDL PK constraint):**
```sql
-- liquibase formatted sql
-- changeset antigravity:31-ensure-pk-user-roles

ALTER TABLE ibpms_security_user_roles 
ADD CONSTRAINT pk_ibpms_sec_user_roles PRIMARY KEY (user_id, role_id);
```

## 📌 5. Matriz de QA y Testing Atómico
**Script sugerido a crear:** `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/security/AuthSyncControllerTest.java`

| Test Name | CA Evaluado | Aserción Esperada |
| :--- | :--- | :--- |
| `testEmergencyLoginFailsWithoutRoles` | No escalar a SUPER_ADMIN | `assertThrows(AccessDeniedException.class)` sobre credenciales válidas sin rol |
| `testDatabaseNormalFormIntegration` | Consolidación de tablas | Context load exitoso sin advertencias de persistencia por `SysRoleEntity` faltante |

## 📌 6. Mensaje de Despacho
> Agentes Backend, inicien el trabajo asignado para la iteración 6.2.
> "Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B."

> Equipos de Arquitectura Documental: Apliquen el HO-10 y generen el diagrama ER validando contra el nuevo Liquibase changeset.
