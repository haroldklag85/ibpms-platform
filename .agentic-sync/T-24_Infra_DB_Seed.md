# 🧠→🛠️ Handoff: ARQUITECTO LÍDER → INFRA/DB
# T-24-DB: Seeds E2E Exhaustivos para Certificación J-02 (57 CUs)

**Emitido por:** 🧠 ARQUITECTO LÍDER (Antigravity)
**Destinatario:** 🛠️ INFRA/DB
**Fecha:** 2026-05-14T03:34:00-05:00
**Sprint:** 6 — Iteración 7.2
**Prioridad:** 🔴 Alta (Bloqueante para T-24-QA)
**Dependencia:** Ninguna

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (Leyes Globales)
cat .cursorrules

# 2. Skills transversales
cat .agents/skills/zero_mock_enforcement/SKILL.md
cat .agents/skills/clean_code_standards/SKILL.md

# 3. UAT (precondiciones de cada fase)
cat docs/uat/casos_uso_uat_j02.md

# 4. Auditoría de brechas
cat .agentic-sync/T-24_UAT_Gap_Analysis.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> `-- @Traceability: Semilla E2E J-02 (T-24)`. INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

La auditoría v2 reveló que los seeds actuales solo cubren usuarios básicos (sysadmin, analista). Las Fases 7A/7B/7C del UAT requieren **datos operativos completos**:

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Faltan operarios del equipo Adjusters | `seed-e2e.sql` | CU-J02-C01 a C08 requieren `operario_a` y `operario_b` en mismo `team_id` |
| Falta supervisor | `seed-e2e.sql` | CU-J02-C06, W09, W10 requieren `supervisor_e2e` en grupo Directors |
| Falta tablero Kanban | `seed-e2e.sql` | CU-J02-A01 a A07 requieren board "QA Sprint E2E" con 5 columnas |
| Faltan tarjetas Kanban | `seed-e2e.sql` | PRE-11 exige 3 tarjetas en TODO |
| Faltan ≥20 tareas mixtas | `seed-e2e.sql` | PRE-14 exige 20+ tareas BPMN+Kanban en Cola del Equipo |
| Falta feature_toggle | `seed-e2e.sql` | CU-W10 exige `FORCE_ROUTING` en tabla de toggles |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Extender el Script de Semilla

**Archivo:** `backend/ibpms-core/src/main/resources/db/changelog/056-seed-e2e-j02.sql` (o siguiente disponible)

```sql
-- @Traceability: Semilla E2E J-02 (T-24)
-- Precondiciones UAT Fase 7 (PRE-10 a PRE-14)

-- PRE-12: Operarios del equipo Adjusters
INSERT INTO ibpms_user (id, username, tenant_id, role, team_id)
VALUES 
  ('op-a-uuid', 'operario_a@alpha.com', 'tenant_alpha', 'OPERARIO', 'team-adjusters'),
  ('op-b-uuid', 'operario_b@alpha.com', 'tenant_alpha', 'OPERARIO', 'team-adjusters')
ON CONFLICT (username) DO NOTHING;

-- PRE-13: Supervisor
INSERT INTO ibpms_user (id, username, tenant_id, role, team_id)
VALUES ('sup-uuid', 'supervisor_e2e@alpha.com', 'tenant_alpha', 'SUPERVISOR', 'team-adjusters')
ON CONFLICT (username) DO NOTHING;

-- PRE-10: Tablero Kanban "QA Sprint E2E"
INSERT INTO kanban_boards (id, name, tenant_id, created_by)
VALUES ('board-e2e', 'QA Sprint E2E', 'tenant_alpha', 'sysadmin')
ON CONFLICT (id) DO NOTHING;

-- Columnas del tablero (5 columnas, hard-limit 7)
INSERT INTO kanban_columns (id, board_id, name, position) VALUES
  ('col-todo', 'board-e2e', 'TODO', 1),
  ('col-doing', 'board-e2e', 'DOING', 2),
  ('col-review', 'board-e2e', 'REVIEW', 3),
  ('col-blocked', 'board-e2e', 'BLOCKED', 4),
  ('col-done', 'board-e2e', 'DONE', 5)
ON CONFLICT (id) DO NOTHING;

-- PRE-11: 3 tarjetas en TODO
INSERT INTO ibpms_task (id, title, board_id, status, assignee, tenant_id) VALUES
  ('kt-001', 'Tarea Kanban E2E 1', 'board-e2e', 'TODO', NULL, 'tenant_alpha'),
  ('kt-002', 'Tarea Kanban E2E 2', 'board-e2e', 'TODO', NULL, 'tenant_alpha'),
  ('kt-003', 'Tarea Kanban E2E 3', 'board-e2e', 'TODO', NULL, 'tenant_alpha')
ON CONFLICT (id) DO NOTHING;

-- Feature Toggle: FORCE_ROUTING
INSERT INTO feature_toggles (key, enabled, tenant_id)
VALUES ('FORCE_ROUTING', false, 'tenant_alpha')
ON CONFLICT (key, tenant_id) DO NOTHING;
```

### Paso 2: Registrar en changelog-master.yaml

Agregar la referencia al nuevo script en `db.changelog-master.yaml` bajo contexto `dev,test`.

---

## ✅ Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Script SQL idempotente (ON CONFLICT DO NOTHING) | Revisión manual del archivo |
| 2 | ≥5 usuarios seed (sysadmin, analista, operario_a, operario_b, supervisor) | `SELECT count(*) FROM ibpms_user WHERE username LIKE '%e2e%'` ≥ 5 |
| 3 | 1 tablero Kanban con 5 columnas y 3 tarjetas | `SELECT count(*) FROM kanban_boards` ≥ 1 |
| 4 | Feature toggle FORCE_ROUTING existe | `SELECT * FROM feature_toggles WHERE key='FORCE_ROUTING'` |
| 5 | `-- @Traceability` presente en script | `grep "@Traceability" [archivo.sql]` |
| 6 | Migración exitosa con `mvn spring-boot:run` | Sin errores de Liquibase/Flyway |
| 7 | Commit en `sprint-6` | `git log -1 --oneline` |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Leer `.cursorrules` + Skills de Sección 2.
2. Leer `docs/uat/casos_uso_uat_j02.md` líneas 640-660 (precondiciones PRE-10 a PRE-14).
3. Crear/extender script SQL con los datos requeridos.
4. Registrar en `db.changelog-master.yaml`.
5. Compilar: `mvn clean compile -f backend/ibpms-core/pom.xml`.
6. Commit: `git add . && git commit -m "chore(db): extend seed-e2e for J-02 Phase 7 (57 CUs) // @Traceability: T-24" && git push origin sprint-6`.

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de [🛠️ INFRA/DB].

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos:

1. cat .cursorrules
2. cat .agents/skills/zero_mock_enforcement/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat docs/uat/casos_uso_uat_j02.md (líneas 640-660: precondiciones Fase 7)
5. cat .agentic-sync/T-24_Infra_DB_Seed.md

TU MISIÓN:

1. Extender seed-e2e.sql con: operario_a, operario_b, supervisor, tablero Kanban (5 columnas, 3 tarjetas), feature_toggle FORCE_ROUTING, ≥20 tareas mixtas.
2. Registrar en db.changelog-master.yaml bajo contexto dev,test.
3. Compilar: mvn clean compile -f backend/ibpms-core/pom.xml
4. Commit: git add . && git commit -m "chore(db): extend seed-e2e for J-02" && git push origin sprint-6

REGLAS INQUEBRANTABLES:
- OBLIGATORIO usar ON CONFLICT DO NOTHING para idempotencia.
- OBLIGATORIO inyectar -- @Traceability: Semilla E2E J-02 (T-24).
- PROHIBIDO usar INSERT sin ON CONFLICT (rompe re-ejecuciones).
- Los UUIDs deben ser deterministas para que QA los referencie en fixtures.
```

---

> // @Traceability: Semilla E2E J-02 (T-24)
