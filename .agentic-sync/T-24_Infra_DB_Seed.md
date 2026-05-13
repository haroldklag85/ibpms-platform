# 🧠→🛠️ Handoff: Arquitecto → Infra/DB
# T-24-INFRA: Scripts Semilla E2E (Zero-Mock) J-02

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🛠️ INFRA/DB
**Fecha:** 2026-05-13T17:55:00-05:00
**Sprint:** 7 — Iteración 7.2
**Prioridad:** 🔴 Alta (Bloqueante para QA)
**Dependencia:** Ninguna

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (Leyes Globales)
cat .cursorrules

# 2. Skill principal del agente receptor
cat ibpms-platform/.agents/skills/database_migration_audit/SKILL.md  # O equivalente en tu contexto

# 3. Skills transversales aplicables
cat ibpms-platform/.agents/skills/zero_mock_enforcement/SKILL.md
cat ibpms-platform/.agents/skills/clean_code_standards/SKILL.md

# 4. ADRs relevantes
cat ibpms-platform/docs/architecture/adr_010_testing_pyramid_governance.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación `-- @Traceability: Semilla E2E J-02 (T-24)`. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

Para cumplir con ADR-010 (Zero-Mock), QA ejecutará sus pruebas E2E contra una Base de Datos real y vacía (sin historial transaccional). Sin embargo, para poder autenticarse y ejecutar los flujos de negocio (ej. validaciones Anti-Spoofing en DMN), se requieren datos maestros mínimos, tales como Tenants y Usuarios/Roles preconfigurados que coincidan con las necesidades de los journeys de prueba.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Falta de Perfiles E2E | `backend/ibpms-core/src/main/resources/db/migration/` | No existen usuarios con rol `sysadmin` o `analista` en las semillas de Liquibase/SQL que habiliten el testing RBAC de QA. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Creación de Script Semilla E2E

**Archivo:** `backend/ibpms-core/src/main/resources/db/migration/V1.5__Seed_E2E_J02.sql` (Verifica el número de versión correcto)

Debes crear un script de inserción (DML) seguro que garantice la existencia de perfiles de prueba requeridos por Playwright para validar flujos BPMN y reglas Anti-Spoofing en DMN:

```sql
-- Snippet prescriptivo — Estructura base requerida
-- @Traceability: Semilla E2E J-02 (T-24)

-- Insertar Tenant Base
INSERT INTO ibpms_tenant (id, name, status) 
VALUES ('tenant_alpha', 'Alpha Corp', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- Insertar Perfil Sysadmin para Anti-Spoofing DMN
INSERT INTO ibpms_user (username, tenant_id, role, password_hash) 
VALUES ('sysadmin@alpha.com', 'tenant_alpha', 'SYSADMIN', '$2a$10$dummyHash...')
ON CONFLICT (username) DO NOTHING;

-- Insertar Perfil Analista Base
INSERT INTO ibpms_user (username, tenant_id, role, password_hash) 
VALUES ('analista_n1@alpha.com', 'tenant_alpha', 'ANALISTA', '$2a$10$dummyHash...')
ON CONFLICT (username) DO NOTHING;
```

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Integridad SQL | El archivo `.sql` no tiene errores de sintaxis y usa `ON CONFLICT DO NOTHING` para evitar fallos en migraciones recurrentes. |
| 2 | Cobertura de Perfiles | Existen al menos dos perfiles (`sysadmin` y `analista`) bajo un Tenant válido. |
| 3 | Trazabilidad Inyectada | `-- @Traceability: Semilla E2E J-02 (T-24)` inyectado en el script SQL. |
| 4 | Migración Exitosa | El arranque de Spring Boot (`mvn spring-boot:run` o flyway/liquibase plugin) aplica la migración sin fallar. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Identificar la próxima versión disponible en `db/migration/`.
2. Crear archivo `V_X__Seed_E2E_J02.sql` e insertar la lógica prescriptiva.
3. Verificar validación de formato SQL.
4. Commit: `git add . && git commit -m "chore(db): script semilla E2E para perfiles J-02" && git push`

---

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 🛠️ INFRA/DB.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat ibpms-platform/.cursorrules
2. cat ibpms-platform/.agents/skills/zero_mock_enforcement/SKILL.md
3. cat ibpms-platform/.agents/skills/clean_code_standards/SKILL.md
4. cat ibpms-platform/docs/architecture/adr_010_testing_pyramid_governance.md
5. cat ibpms-platform/.agentic-sync/T-24_Infra_DB_Seed.md

TU MISIÓN:

1. Crear el script semilla SQL (`db/migration/V_X__Seed_E2E_J02.sql`) con los datos maestros mínimos (Tenants, Usuarios base `analista` y `sysadmin`) para que QA pueda operar las pruebas RBAC y Zero-Mock sobre J-02.
2. Commit: `git add . && git commit -m "chore(db): script semilla E2E para perfiles J-02" && git push`

REGLAS INQUEBRANTABLES:
- OBLIGATORIO usar `ON CONFLICT DO NOTHING` (o equivalente) para evitar fallos en reconstrucciones.
- OBLIGATORIO inyectar `-- @Traceability: Semilla E2E J-02 (T-24)` en el archivo SQL.
```
