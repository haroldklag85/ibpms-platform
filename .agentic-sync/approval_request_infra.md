# Solicitud de Revisión — Agente Infra/BD (Iteración 09-REMEDIATION)

**Fecha:** 2026-05-08
**Agente:** Infra/BD  
**US:** US-036 (Identity Governance)  
**CAs:** CA-16, CA-24  
**Rama:** DevDavid  

---

## Diagnóstico y Alineación

He analizado la tabla `ibpms_audit_reports` y la entidad JPA actual.
Actualmente la tabla existe, pero **sus columnas no coinciden con la nomenclatura estricta** solicitada en tu Handoff.

Tengo: `generated_by`, `file_hash`, `metadata_json`.
Pides: `generated_by_user_id`, `sha256_hash`, `file_path_or_blob`.

## Plan de Acción Propuesto

1. **Liquibase Changeset (`46-us036-audit-reports.sql`)**: 
   Ejecutaré comandos `ALTER TABLE ... RENAME COLUMN` para cambiar los nombres actuales a la nomenclatura exacta exigida en el Handoff. También renombraré los índices correspondientes creados en la iteración anterior.
2. **Alineación JPA**: 
   Actualizaré las anotaciones `@Column(name="...")` en `AuditReportEntity.java` para que el backend no falle al arrancar (Hibernate DDL Validate pasará sin problemas).
3. **Validación**: 
   Compilaré el backend (`mvn clean compile`) para asegurar que todo cuadra antes de hacer push.

---

Arquitecto Líder: solicito tu **aprobación** para proceder con este plan quirúrgico y remediar la deuda técnica del reporte ISO 27001.

**Por favor responde:**
- ✅ **APROBADO** — para que pase a modo EXECUTION.
- ❌ **RECHAZADO + motivo** — para corregir antes de ejecutar.
