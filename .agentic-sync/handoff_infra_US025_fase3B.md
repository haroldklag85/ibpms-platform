# 🟠 Handoff Infra/BD — US-025 Fase 3B: Liquibase Impersonación Audit Log

> **Emitido por:** Arquitecto Líder  
> **Fecha:** 2026-05-03  
> **Agente Destino:** Infra/BD  
> **Prioridad:** 🟡 P1 — **APROBADO POR PO PARA V1**  
> **Rama Git:** sprint-6  
> **Pre-requisito:** Fase 1B completada (changeset de `ibpms_menu_topology` ya aplicado)  
> **Gate de Salida:** Liquibase `update` exitoso + verificación SQL

---

## 1. Contexto

El PO aprobó CA-9 (Impersonación) y CA-31 (Trazabilidad) para V1. Se requiere la tabla `ibpms_impersonation_audit_log` para registrar todas las acciones de impersonación (START/EXIT) con trazabilidad completa.

---

## 2. Alineación Arquitectónica

| ADR | Impacto |
|-----|---------|
| ADR-009 (PostgreSQL + Liquibase) | Changeset obligatorio. Prohibido DDL manual |
| ARQ-025-08 | Esta tarea cierra la violación C4-Model identificada en la auditoría |

---

## 3. Tareas

### Tarea 3B.I1 — Changeset Liquibase `ibpms_impersonation_audit_log`

**Crear:** `backend/ibpms-core/src/main/resources/db/changelog/changes/XX-us025-impersonation-audit.sql`

```sql
--liquibase formatted sql
--changeset architect:us025-impersonation-audit-create

CREATE TABLE ibpms_impersonation_audit_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id        UUID NOT NULL,
    target_user_id  UUID NOT NULL,
    action          VARCHAR(20) NOT NULL CHECK (action IN ('START', 'EXIT', 'TIMEOUT', 'REVOKED')),
    metadata        JSONB,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_impersonation_admin ON ibpms_impersonation_audit_log(admin_id);
CREATE INDEX idx_impersonation_target ON ibpms_impersonation_audit_log(target_user_id);
CREATE INDEX idx_impersonation_created ON ibpms_impersonation_audit_log(created_at DESC);

COMMENT ON TABLE ibpms_impersonation_audit_log IS 'US-025 CA-31: Registro de auditoría de todas las acciones de impersonación. Inmutable (append-only).';
COMMENT ON COLUMN ibpms_impersonation_audit_log.action IS 'Tipo de acción: START (inicio), EXIT (fin voluntario), TIMEOUT (TTL expirado), REVOKED (admin forzó cierre)';
COMMENT ON COLUMN ibpms_impersonation_audit_log.metadata IS 'Datos adicionales en JSONB (ej. roles del target, módulos accedidos durante impersonación)';
```

---

## 4. Registro del Changeset

Agregar al `db.changelog-master.yaml`:
```yaml
- include:
    file: changes/XX-us025-impersonation-audit.sql
```

**IMPORTANTE:** Este changeset debe ir DESPUÉS del changeset de `ibpms_menu_topology` (Fase 1B).

---

## 5. Criterios de Aceptación del Gate

- [ ] `liquibase update` ejecuta sin errores
- [ ] `SELECT COUNT(*) FROM ibpms_impersonation_audit_log` retorna 0 (tabla vacía, sin seed)
- [ ] Los 3 índices existen: `admin_id`, `target_user_id`, `created_at`
- [ ] El constraint CHECK sobre `action` rechaza valores inválidos: `INSERT INTO ibpms_impersonation_audit_log (admin_id, target_user_id, action) VALUES (gen_random_uuid(), gen_random_uuid(), 'INVALID')` → ERROR

## 6. Archivos Impactados

| Archivo | Acción |
|---------|--------|
| `db/changelog/changes/XX-us025-impersonation-audit.sql` | Crear |
| `db/changelog/db.changelog-master.yaml` | Modificar (agregar include) |
