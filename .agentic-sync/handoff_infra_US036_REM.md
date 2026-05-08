---
title: "Handoff Infraestructura/BD - US-036 Remediación (Iteración 09-DEV-REMEDIATION)"
agent: "Infra/BD"
branch: "DevDavid"
us: "US-036"
cas: "CA-16, CA-24"
---

# 1. Metadatos de la Delegación
*   **Rol Asignado:** Infra/BD
*   **Rama de Trabajo:** `DevDavid`
*   **US Objetivo:** US-036 (Identity Governance)
*   **Criterios a Validar:** CA-16, CA-24 (Persistencia de Reportes ISO 27001).
*   **Alineación Arquitectónica:** Cumplimiento estricto del ADR-009 (PostgreSQL + Liquibase).

# 2. Contexto Técnico
Debemos remediar la deuda técnica del reporte ISO 27001 (CA-16 y CA-24). Actualmente, el backend no puede persistir los reportes generados porque el esquema relacional en PostgreSQL es incompleto o no existe la entidad formal.

# 3. Entregables Esperados
1.  **Liquibase Changeset:** Crear un nuevo archivo en `db/changelog/` (Ej: `46-us036-audit-reports.sql` o `.xml`) que defina/asegure la tabla `ibpms_audit_reports`.
    *   **Estructura obligatoria:** `id` (UUID o BIGSERIAL), `generated_at` (TIMESTAMP), `generated_by_user_id` (VARCHAR/UUID), `report_type` (VARCHAR), `sha256_hash` (VARCHAR - 64 chars), `file_path_or_blob` (Opcional, según diseño).
2.  **Validación:** Ejecutar un test de validación de sintaxis de Liquibase (Ej: `mvn liquibase:updateTestingRollback` o arrancar el contenedor localmente) para asegurar que el DDL no quiebra el inicio.

⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **Validación de esquema obligatoria:** Ejecuta verificaciones de sintaxis Liquibase o configuraciones docker-compose antes de hacer push.

# 4. INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_infra.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_infra.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
