# 🧠→🔧 Handoff: Arquitecto Líder → Infraestructura/BD
# T-21: Auditoría y Completitud del Data Seeder Liquibase (E2E)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🔧 INFRA/DB
**Fecha:** 2026-05-12T15:00:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/db_infrastructure_management/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes (Hexagonal y Testing)
cat docs/architecture/adr-001-hexagonal-architecture.md
cat docs/architecture/adr_010_testing_pyramid_governance.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `// @Traceability: US-XXX, CA-XX`.
> Esto es INNEGOCIABLE. Especialmente en los archivos `.sql` y `.yaml` de Liquibase.

---

## 🔬 Diagnóstico del Arquitecto

Durante la auditoría forense para certificar los entornos E2E, se identificó que el `seed-e2e.sql` actual y el changelog de Liquibase no cubren todos los escenarios para que la Suite E2E de Playwright funcione sin mocks. Específicamente, el "Prefill US-029" (Borradores/Formularios vivos) no está instanciado, y las tareas del Workdesk no están asociadas completamente con la delegación `director_1 -> analista`.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Ausencia de Prefill Form Draft (US-029) | `db/changelog/changes/51-us029-form-definitions-seed.sql` | Se inyecta el esquema JSON pero no hay registros en `ibpms_task_drafts` (Borradores) para simular el Prefill (CA-05). |
| Workdesk Projection sin Tarea Delegada | `seed-e2e.sql:140` | La tarea del director (`wd_task_5`) existe, pero no está conectada a la delegación para que `analista` la vea en el UI. |
| Feature Toggle | `seed-e2e.sql:21` | El Feature Toggle existe, pero carece de la anotación de @Traceability en el archivo SQL estructurado y no hay Toggles de seguridad. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Completar Data Seeding para Prefill US-029 (Borradores)

**Archivo:** `ibpms-platform/backend/ibpms-core/src/main/resources/db/changelog/changes/51-us029-form-definitions-seed.sql`

Añadir al final del archivo los registros de Drafts para validar el CA-05 (Amnesia Transaccional y Prefill).

```sql
-- @Traceability: US-029, CA-05 (Form Wizard Prefill & Draft)
INSERT INTO ibpms_task_drafts (id, task_id, tenant_id, form_data, created_by, created_at, expires_at)
VALUES (
    'dr000000-0000-4000-8000-000000000001',
    'wd_task_2', 
    'tenant_alpha',
    '{"applicantName": "Juan Perez", "applicantAge": 30}'::jsonb,
    'analista',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '24 hours'
) ON CONFLICT DO NOTHING;
```

### Paso 2: Fortalecer semilla del Workdesk y Trazabilidad

**Archivo:** `ibpms-platform/backend/ibpms-core/src/main/resources/seed-e2e.sql`

Modificar la sección de Feature Toggles y Workdesk para inyectar trazabilidad e incluir una tarea de prueba de routing.

```sql
-- @Traceability: US-001, CA-08 (Feature Toggles Anti Cherry-Picking)
INSERT INTO ibpms_feature_toggles (id, tenant_id, toggle_key, enabled, changed_by, description)
VALUES (gen_random_uuid(), 'tenant_alpha', 'FORCE_ROUTING', false, 'admin', 'Toggle CA-08')
ON CONFLICT (tenant_id, toggle_key) DO UPDATE SET enabled = false;

-- @Traceability: US-002, CA-08 (Despojo Forzoso) y US-001 CA-04 (Delegación)
-- Verificar que wd_task_5 asigne candidate_group = 'ROLE_SUPERVISOR'
```

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Los scripts de BD incluyen explícitamente `@Traceability: US-029, CA-05` y `US-001, CA-08`. | `grep "@Traceability" ibpms-platform/backend/ibpms-core/src/main/resources/db/changelog/changes/51-us029-form-definitions-seed.sql` retorna resultados. |
| 2 | La base de datos se hidrata sin errores de sintaxis o llaves foráneas. | Ejecución limpia de la inicialización de Liquibase al levantar la app. |
| 3 | Build y compilación exitosa del backend. | `mvn clean compile` sin errores. |
| 4 | Commit estructurado en la rama correcta. | `git commit -m "chore(db): T-21 completar data seeding US-029 prefill y trazabilidad"` |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Edita el archivo `ibpms-platform/backend/ibpms-core/src/main/resources/db/changelog/changes/51-us029-form-definitions-seed.sql` inyectando el Paso 1.
2. Edita `ibpms-platform/backend/ibpms-core/src/main/resources/seed-e2e.sql` aplicando trazabilidad y revisión de toggles.
3. Compila el backend: `cd ibpms-platform/backend && .\compile_only.bat` (o el equivalente Maven nativo `mvn clean compile`).
4. Commit: `git add . && git commit -m "chore(db): T-21 completar data seeding US-029 prefill y trazabilidad" && git push`

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de 🔧 INFRA/DB.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/db_infrastructure_management/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat docs/architecture/adr-001-hexagonal-architecture.md
5. cat .agentic-sync/T-21_Infra_DataSeeding_Handoff.md

TU MISIÓN:

1. Ejecuta el Paso 1 añadiendo los scripts `INSERT` de Liquibase para los Borradores de Formularios (`ibpms_task_drafts`) asegurando que el seed contenga datos para el CA-05 de la US-029.
2. Ejecuta el Paso 2 asegurando las etiquetas de `@Traceability` requeridas por la Ley Global 3.
3. Build/Compile: `cd ibpms-platform/backend && mvn clean compile`
4. Commit: `git add . && git commit -m "chore(db): T-21 completar data seeding US-029 prefill y trazabilidad" && git push`

REGLAS INQUEBRANTABLES:
- DEBES añadir explícitamente los comentarios `-- @Traceability: US-XXX, CA-XX` (Ley Global 3) en cualquier SQL modificado.
- PROHIBIDO modificar el esquema DDL; solo puedes insertar (`INSERT INTO ... ON CONFLICT DO NOTHING`) DML para seeding.
- PROHIBIDO romper la compatibilidad de Liquibase en E2E.
```
