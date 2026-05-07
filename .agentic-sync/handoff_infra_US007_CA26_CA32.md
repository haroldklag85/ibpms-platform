---
name: "Handoff Infra/BD - US-007 (Modo Manual DMN) CA-26 a CA-32"
role: "Infra/BD"
---

# 1. Metadatos y SSOT (Single Source of Truth)
- **Iteración/Sprint:** Sprint-6
- **Rama de trabajo:** sprint-6
- **User Story:** US-007 (Generador Cognitivo de DMN)
- **Criterios de Aceptación (CAs) a desarrollar:** CA-32 (Alineación con BD)
- **Path del SSOT:** `docs/requirements/epics/epic_B_formularios_bpmn.md`
- **Flujo de Trabajo:** Infra/BD -> Backend -> Frontend -> QA

# 2. Alineación Arquitectónica y ADRs (Architecture Decision Records)
- **Validación de ADRs:**
  - `adr_009_postgresql_pgvector_migration.md`: Las modificaciones en base de datos deben realizarse obligatoriamente mediante un changelog de Liquibase para PostgreSQL.
- **Lineamientos Transversales:** Todo esquema debe soportar el control y auditoría de cambios manuales. Se mantiene el límite estricto de las 3 VMs.

# 3. Rutas Exactas y Contexto Preexistente
- **Liquibase Changelog:** `backend/ibpms-core/src/main/resources/db/changelog/38-us007-dmn-manual-edit-schema.sql` (Archivo a crear)

# 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

**1. Base de Datos (Liquibase):**
Debes crear el archivo `38-us007-dmn-manual-edit-schema.sql` para añadir la columna de rastreo manual en el esquema de DMN:
```sql
-- liquibase formatted sql
-- changeset antigravity:38-us007-dmn-manual-edit-schema

ALTER TABLE ibpms_dmn_definitions 
ADD COLUMN is_manual BOOLEAN DEFAULT FALSE;

-- En la tabla ibpms_audit_log (si es necesario) asegurarse de que soporta el source: MANUAL_EDIT.
```
*Asegúrate de agregar este archivo al master changelog (`db.changelog-master.yaml` o `.xml`).*

# 5. Matriz de QA y Testing Atómico
*No aplica pruebas unitarias para este agente. Sin embargo, su esquema es dependencia dura para el Agente Backend.*

# 6. Mensaje de Despacho (Comunicación al Agente Especialista)

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_Infra.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_Infra.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
> 
> "Validación de esquema obligatoria: Ejecuta verificaciones de sintaxis Liquibase o configuraciones docker-compose antes de hacer push."
