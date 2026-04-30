# 🗄️ Handoff Infra/BD — Remediación ARQ-005 (US-005 Core Deploy Pipeline)

## 1. Metadatos y SSOT
- **Iteración:** Remediación Arquitectónica Post-Auditoría US-005
- **Rama Git:** `sprint-6`
- **SSOT:** `docs/requirements/epics/epic_B_formularios_bpmn.md` → US-005
- **Hallazgos Origen:** `audit_arquitectura_US005.md`
- **Orden de Ejecución:** Infra/BD → Backend → QA → Frontend (informativo)

## 2. Alineación Arquitectónica

| ADR | Impacto |
|-----|---------|
| `adr_009_postgresql_pgvector_migration.md` | Todas las migraciones deben ser Liquibase sobre PostgreSQL. |
| `adr-001-hexagonal-architecture.md` | Las tablas nuevas deben tener entidades JPA y repositorios segregados. |

## 3. Acciones Requeridas

> [!NOTE]
> **El Bloque 1 de la auditoría NO detectó hallazgos de base de datos nuevos.** Las tablas necesarias ya fueron creadas previamente:
> - `ibpms_process_locks` ✅ (CA-66)
> - `ibpms_deploy_requests` ✅ (CA-69)
> - `ibpms_data_mappings` ✅ (CA-68)
> - `ibpms_external_task_topics` ✅ (CA-70)
> - `ibpms_bpmn_design_audit_log` ✅ (CA-42)

### Acción Única: Verificación de Integridad

El agente Infra/BD debe verificar que los changesets de Liquibase para las tablas listadas arriba están correctamente registrados en el master changelog:

```
backend/ibpms-core/src/main/resources/db/changelog/db.changelog-master.yaml
```

**Checklist de verificación:**
- [ ] Cada tabla tiene su changeset con `CREATE TABLE IF NOT EXISTS`.
- [ ] Los changesets tienen IDs únicos y no colisionan.
- [ ] Las FK constraints son coherentes con `data_architecture_erd.md`.
- [ ] No hay columnas huérfanas sin uso.

## 4. Mensaje de Despacho

> Este handoff es **verificativo, no constructivo**. No se requiere creación de tablas nuevas. Si la verificación detecta inconsistencias, reportarlas en `.agentic-sync/approval_request_infra.md`.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md`.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.**
3. Debes guardar tu solicitud de revisión en `.agentic-sync/approval_request_infra.md`.
4. Al grabar el archivo, detente y dile al Humano: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_infra.md`. Por favor, ve al chat del Arquitecto Líder."*
5. Espera el veredicto. Si aprobado, pasa a `EXECUTION`, ejecuta `git commit` y `git push` en la rama `sprint-6`.

> 📚 **SKILLS OBLIGATORIOS:**
> - Aplica `.agents/skills/tdd_first/SKILL.md` y `.agents/skills/clean_code_standards/SKILL.md`.
