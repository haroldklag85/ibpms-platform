# 🧠→🔧 Handoff: Arquitecto Líder → Infra/DB
# T-21: Verificar Completitud del Data Seeder Liquibase (US-001)

**Emitido por:** [🧠 ARQUITECTO LÍDER]
**Destinatario:** [🔧 INFRA/DB]
**Fecha:** 2026-05-11T22:18:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🔴 ALTA
**Dependencia:** Ninguna (Se puede ejecutar en paralelo con Backend T-04/T-05)

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Zero-Mock enforcement (ADR-010 requiere E2E real)
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 3. Políticas de Migración
cat docs/architecture/data_architecture_erd.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO changeset de Liquibase o SQL DEBE incluir un comentario `-- changeset agente:us001-caXX...` indicando la US. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

Para que el refactor Hexagonal (T-04, T-05, T-06) funcione y el QA certifique con éxito (Zero-Mock), la base de datos debe inicializarse con un estado inmutable en el `seed-e2e.sql` o los changelogs de Liquibase.

Los tests esperan:
1. Un Feature Toggle `FORCE_ROUTING` existente (para CA-08/CA-16).
2. Un delegado real configurado para el usuario E2E en `ibpms_security_delegation` (para CA-04).

### Análisis del estado actual
* Hemos detectado múltiples tablas de delegación en los changelogs (`user_delegation` vs `ibpms_security_delegation` vs `ibpms_sec_delegation_log`).
* El backend canónico ahora usa `ibpms_security_delegation` según el script `28-consolidate-delegation.sql`.

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Auditoría y Consolidación de `seed-e2e.sql`

**Archivo:** `backend/ibpms-core/src/main/resources/seed-e2e.sql` (o en su defecto, el changelog master que inyecta datos E2E).

Verifica o agrega los siguientes inserts inmutables (ajustando los UUIDs/IDs a los usuarios E2E estándar del proyecto):

**1. Feature Toggle (Para T-05):**
```sql
-- @Traceability: US-001, CA-08 (Feature Toggles)
INSERT INTO ibpms_feature_toggle (id, tenant_id, toggle_key, enabled, description)
VALUES ('ft_force_routing', 'default', 'FORCE_ROUTING', false, 'Toggle CA-08')
ON CONFLICT (tenant_id, toggle_key) DO NOTHING;
```

**2. Delegación Activa (Para T-06):**
```sql
-- @Traceability: US-001, CA-04 (Múltiples Delegantes)
-- Usuario E2E delega su bandeja a un asistente
INSERT INTO ibpms_security_delegation (id, delegator_id, substitute_id, start_date, end_date, is_active, reason)
VALUES (gen_random_uuid(), 'user-e2e-id', 'assistant-e2e-id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days', true, 'Vacaciones')
ON CONFLICT DO NOTHING;
```

*(Nota: Usa los UUIDs reales de los usuarios E2E definidos previamente en la base de datos)*.

### Paso 2: Validar el estado de la BD

Si es necesario, arranca el stack de infraestructura y levanta el backend solo para validar que Liquibase aplica correctamente sin colapsar.

```bash
cd backend/ibpms-core
docker-compose up -d postgres
mvn spring-boot:run -Dspring-boot.run.profiles=e2e
```

### Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | `FORCE_ROUTING` está en la DB E2E | Verificación SQL (`SELECT * FROM ibpms_feature_toggle`) |
| 2 | Delegado configurado para usuario E2E | Verificación SQL (`SELECT * FROM ibpms_security_delegation`) |
| 3 | Trazabilidad SQL | Comentarios `-- @Traceability` presentes |
| 4 | Liquibase ejecuta sin error | Log de `mvn spring-boot:run` |
| 5 | Commit de Infra en rama de sprint | `git log -1` |

---

**RECUERDA:** Estás blindando el ambiente E2E. Evita usar tablas legacy como `user_delegation`. El esquema oficial validado es `ibpms_security_delegation`.
