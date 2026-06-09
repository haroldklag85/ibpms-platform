# 🧠→🗄️ Handoff: Arquitecto Líder → Infraestructura/DB
# T-02 & T-03: Esquema Dinámico para SLA por Tenant y Whitelist de Webhooks

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🗄️ INFRA / DB
**Fecha:** 2026-05-12T08:30:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/db_liquibase_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes
cat docs/architecture/adr-010-testing-pyramid-and-qa-governance.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `-- @Traceability: US-004, CA-04`.
> Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

El Backend tiene hardcodes criminales estipulando SLAs estáticos de 48h o 4h para Webhooks, evadiendo la capacidad empresarial de parametrizar por cliente (Tenant). A su vez, para el CA-04 de Whitelists de dominios (T-02), no existe una hidratación predeterminada (Seed) en el entorno E2E que permita que los tests de QA pasen. Es imperativo crear el esquema SQL.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Ausencia de Entidad de Config | `ibpms-core/src/main/resources/db/changelog/` | Falla estructural: No existe la tabla `ibpms_tenant_config` para parametrizar SLA en caliente. |
| Datos Seed Ausentes | `seed-e2e.sql` (Hipótesis) | Los tests de `WebhookIntakeService` fallarán con HTTP 403 porque no hay un dominio de prueba en la tabla `ibpms_allowed_domain`. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Crear Liquibase Changelog para Tenant Config

**Archivo:** `C:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/resources/db/changelog/changes/015-create-tenant-config.sql`

Crea el script DDL de la tabla y define sus restricciones (Primary Key, Not Null).

```sql
-- liquibase formatted sql
-- changeset liquibase:015-create-tenant-config
-- @Traceability: US-004, CA-18

CREATE TABLE ibpms_tenant_config (
    tenant_id VARCHAR(50) PRIMARY KEY,
    webhook_sla_hours INT NOT NULL DEFAULT 48,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- rollback DROP TABLE ibpms_tenant_config;
```

Asegúrate de registrar este changelog en el `db.changelog-master.yaml` si no está auto-detectado.

### Paso 2: Hidratación E2E para Whitelists (Dominio Seguro)

**Archivo:** `C:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/resources/db/changelog/data/seed-e2e.sql`

Inserta un dominio de prueba para que los Webhooks E2E no reboten (Fail-Open/403). Busca si el archivo ya existe y añade:

```sql
-- @Traceability: US-004, CA-04 (T-02)
-- Whitelist domain para entorno E2E
INSERT INTO ibpms_allowed_domain (id, domain, tenant_id, is_active, created_at)
VALUES ('00000000-0000-0000-0000-000000000001', '@e2e.domain.com', 'tenant_alpha', true, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Configuración por defecto para tenant_alpha (T-03)
INSERT INTO ibpms_tenant_config (tenant_id, webhook_sla_hours)
VALUES ('tenant_alpha', 24)
ON CONFLICT DO NOTHING;
```

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Creación exitosa de `ibpms_tenant_config` | `mvn liquibase:status` o levantar DB en test. |
| 2 | Inserción exitosa de dominio de prueba E2E | Verificación en PGAdmin/psql sobre el data seed E2E. |
| 3 | Inyección de `-- @Traceability` | Revisión directa en los `.sql` generados. |
| 4 | Construcción Exitosa / Commit | Build verde de la BBDD + Commit subido. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Crea o modifica `015-create-tenant-config.sql` con el SQL especificado.
2. Añade los inserts en `seed-e2e.sql`.
3. Valida la sintaxis (no uses `cd` a carpetas que no existen, corre Maven en el pom de backend): `mvn clean compile`
4. Commit: `git add . && git commit -m "feat(infra): create tenant_config and e2e seeds [T-02, T-03]" && git push`

---

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 🗄️ INFRA / DB.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/db_liquibase_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat docs/architecture/adr-010-testing-pyramid-and-qa-governance.md
5. cat .agentic-sync/handoff_infra_security_T02_T03.md

TU MISIÓN:

1. Crear el DDL `015-create-tenant-config.sql` en la carpeta `backend/ibpms-core/src/main/resources/db/changelog/changes/` definiendo SLA en caliente.
2. Modificar el `seed-e2e.sql` inyectando un dominio base en `ibpms_allowed_domain` para que las pruebas E2E Webhook funcionen, y añadiendo configuración de SLA al `tenant_alpha`.
3. Build/Compile: Ejecutar verificación de Maven o levantar docker-compose si aplica.
4. Commit: `git add . && git commit -m "feat(infra): create tenant_config and e2e seeds [T-02, T-03]" && git push`

REGLAS INQUEBRANTABLES:
- Inyectar compulsoriamente los comentarios `-- @Traceability: US-004, CA-18`.
- Cero Mocks: Usar sintaxis SQL compatible con Postgres 15+ (liquibase formatted sql).
- Si el Liquibase Master `db.changelog-master.yaml` requiere incluir `015-create-tenant-config.sql` a mano, OBLIGATORIAMENTE debes editarlo.
```
