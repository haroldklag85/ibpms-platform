# Handoff Infra/BD — ARQ-028-04 | Segregación de Cohesión Mixta

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | Deuda Técnica y Refactorización (sprint-6) |
| **Rama Git** | `sprint-6` |
| **Deuda a Cerrar** | **ARQ-028-04:** Entidad con cohesión mixta (`ibpms_form_definitions`) |
| **Flujo de Trabajo** | **Infra/BD** → Backend → QA (secuencial estricto) |

---

## 2. Alineación Arquitectónica y ADRs

| ADR | Impacto |
|-----|---------|
| `adr-001-hexagonal-architecture.md` | La separación de tablas habilita que Backend cree entidades JPA segregadas por dominio (Diseño vs Certificación), respetando Alta Cohesión. |
| `adr_009_postgresql_pgvector_migration.md` | El DDL usa PostgreSQL nativo. Se prohíbe usar otro motor de BD. |

**Trazabilidad:** La tabla `ibpms_form_definitions` actualmente mezcla campos de versionado de esquema (`form_id`, `schema_content`, `hash_sha256`) con campos de certificación QA (`is_qa_certified`, `certified_by`, `certified_schema_hash`, `certified_at`). Esto viola el principio de Alta Cohesión y dificulta la evolución independiente de ambos dominios. La solución es extraer las columnas de certificación a una nueva tabla `ibpms_form_certifications` con FK hacia la original.

---

## 3. Rutas Exactas y Contexto Preexistente

| Archivo | Estado Actual |
|---------|--------------|
| `backend/ibpms-core/src/main/resources/db/changelog/25-us028-qa-certification-columns.sql` | Script que añadió las columnas de certificación (`is_qa_certified`, `certified_schema_hash`, `certified_by`, `certified_at`) a `ibpms_form_definitions`. |
| `backend/ibpms-core/src/main/resources/db/changelog/db.changelog-master.yaml` | Archivo maestro de Liquibase. Debe registrar el nuevo changeset. |

---

## 4. Snippets Prescriptivos — Changeset de Liquibase

Crea el archivo `backend/ibpms-core/src/main/resources/db/changelog/35-arq02804-split-certification.sql`:

```sql
-- liquibase formatted sql

-- changeset antigravity:35-arq02804-split-certification

-- Paso 1: Crear tabla segregada de certificaciones QA
CREATE TABLE ibpms_form_certifications (
    id UUID NOT NULL,
    form_definition_id UUID NOT NULL,
    is_qa_certified BOOLEAN NOT NULL DEFAULT FALSE,
    certified_schema_hash VARCHAR(64),
    certified_by VARCHAR(100),
    certified_at TIMESTAMP,
    CONSTRAINT pk_ibpms_form_certifications PRIMARY KEY (id),
    CONSTRAINT fk_fc_form_definition FOREIGN KEY (form_definition_id) REFERENCES ibpms_form_definitions(id) ON DELETE CASCADE
);

-- Paso 2: Migración de datos existentes
INSERT INTO ibpms_form_certifications (id, form_definition_id, is_qa_certified, certified_schema_hash, certified_by, certified_at)
SELECT gen_random_uuid(), id, is_qa_certified, certified_schema_hash, certified_by, certified_at
FROM ibpms_form_definitions;

-- Paso 3: Eliminar columnas migradas de la tabla original
ALTER TABLE ibpms_form_definitions
DROP COLUMN is_qa_certified,
DROP COLUMN certified_schema_hash,
DROP COLUMN certified_by,
DROP COLUMN certified_at;

-- rollback ALTER TABLE ibpms_form_definitions ADD COLUMN is_qa_certified BOOLEAN NOT NULL DEFAULT FALSE;
-- rollback ALTER TABLE ibpms_form_definitions ADD COLUMN certified_schema_hash VARCHAR(64);
-- rollback ALTER TABLE ibpms_form_definitions ADD COLUMN certified_by VARCHAR(100);
-- rollback ALTER TABLE ibpms_form_definitions ADD COLUMN certified_at TIMESTAMP;
-- rollback DROP TABLE ibpms_form_certifications;
```

Registra el nuevo script en `db.changelog-master.yaml`:
```yaml
- include:
    file: 35-arq02804-split-certification.sql
    relativeToChangelogFile: true
```

---

## 5. Criterios de Aceptación

- [ ] La tabla `ibpms_form_certifications` existe en PostgreSQL con FK hacia `ibpms_form_definitions`.
- [ ] Las columnas `is_qa_certified`, `certified_schema_hash`, `certified_by`, `certified_at` **NO** existen en `ibpms_form_definitions`.
- [ ] El levantamiento del entorno (`docker-compose up` + `mvn spring-boot:run`) ejecuta Liquibase sin errores de checksum.
- [ ] Los datos preexistentes de certificación se migraron correctamente a la nueva tabla.

---

## 6. Instrucciones Operativas y de Comunicación

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_infra.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_infra.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `sprint-6`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md`.

> ⚠️ Notifica tu finalización para que el Agente Backend pueda proceder con su código.
