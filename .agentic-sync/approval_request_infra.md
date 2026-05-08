# Solicitud de Revisión — Agente Infra/BD

**Fecha:** 2026-05-08T08:50:00-05:00  
**Agente:** Infra/BD  
**US:** US-036 (Identity Governance)  
**CAs:** CA-23, CA-24, CA-25, CA-26, CA-27, CA-28  
**Rama:** DevDavid  

---

## Resumen del Plan

### Diagnóstico Ejecutado

He realizado una auditoría completa del esquema de base de datos PostgreSQL contra los changesets Liquibase y las entidades JPA, verificando empíricamente contra la BD en ejecución (`ibpms-postgres-uat`).

### Hallazgos

| Tabla | Estado | Acción |
|-------|--------|--------|
| `ibpms_audit_reports` (CA-24) | ✅ Existe. Entidad JPA alineada. | ⚠️ Limpiar 2 columnas legacy orphan (`requested_by`, `content_hash`) no mapeadas por JPA. Añadir índices de rendimiento. |
| `ibpms_security_delegation` (CA-23) | ✅ Existe con `start_date` + `end_date`. Entidad JPA alineada. FK correctas. | ✅ Sin cambios necesarios. |

### Cambio Propuesto

**Un único changeset idempotente:** `45-us036-ca23-ca28-infra.sql`

Operaciones:
1. `ALTER TABLE ibpms_audit_reports DROP COLUMN IF EXISTS requested_by;`
2. `ALTER TABLE ibpms_audit_reports DROP COLUMN IF EXISTS content_hash;`
3. `CREATE INDEX IF NOT EXISTS idx_audit_reports_generated_at ON ibpms_audit_reports(generated_at);`
4. `CREATE INDEX IF NOT EXISTS idx_audit_reports_file_hash ON ibpms_audit_reports(file_hash);`

### Justificación
- Las columnas `requested_by` y `content_hash` fueron creadas por el changeset original `20-us036-rbac-schema.sql`, pero la entidad JPA `AuditReportEntity.java` mapea `generated_by` y `file_hash` (del changeset `36-us036-ca12-ca16-reports.sql`).
- Ningún código Java referencia estas columnas legacy.
- Los índices optimizan las consultas de comparativa entre periodos (requisito explícito de CA-24).

### Impacto en funcionalidades existentes
**CERO.** Solo se eliminan columnas huérfanas y se añaden índices.

---

## Solicitud Formal

Arquitecto Líder: solicito su **aprobación** para proceder con la ejecución del changeset descrito. El cambio es quirúrgico, idempotente y no impacta ninguna funcionalidad existente.

**Responda con:**
- ✅ **APROBADO** — para que proceda a modo EXECUTION
- ❌ **RECHAZADO + motivo** — para corregir antes de ejecutar
