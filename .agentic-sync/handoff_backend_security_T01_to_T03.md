# 🧠→⚙️ Handoff: Arquitecto Líder → Backend
# T-01, T-02, T-03: Auditoría y Remediación de Seguridad (Zero-Trust SLA)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** ⚙️ BACKEND - JAVA
**Fecha:** 2026-05-12T08:35:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🔴 Alta
**Dependencia:** T-02, T-03 Infra/DB (Creación de `ibpms_tenant_config` ya ejecutada/paralela)

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/backend_sre_compilation_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes
cat docs/architecture/adr-001-hexagonal-architecture.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `// @Traceability: US-027, CA-04`.
> Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

El diagnóstico profundo revela brechas operativas en el manejo de sesiones de IA y hardcodes inaceptables que evitan el aislamiento multi-tenant exigido por el ADR-001.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| IDOR Residual (T-01) | `RagSessionCleanerUseCase.java` | Aunque el Controller captura el Tenant, se debe confirmar que `wipeSessionFootprint` aplique WHERE Tenant. |
| Secrets en Texto Plano (T-02) | `application.yml` | Webhook security properties (HMAC secret) pueden carecer de inyección via variables de entorno. |
| SLA Hardcodeado (T-03) | `WebhookIntakeService.java:184` | `.plusHours(4)` inyectado directo. Debe leer desde la tabla de BD `ibpms_tenant_config` creada por Infra. |
| Tenencia Hardcodeada (T-03) | `AuthSyncController.java:130` | Strings `tenant_alpha` / `tenant_beta` forzados en la sincronización. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Refactorización Dinámica SLA en Caliente

**Archivo:** `C:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/WebhookIntakeService.java`

Elimina el hardcode `.plusHours(4)` y sustitúyelo por una lectura dinámica a la base de datos a través de una nueva entidad y repositorio (`TenantConfigRepository`).

```java
// @Traceability: US-004, CA-18
// Pseudo-implementación, deberás crear la Entity TenantConfig y su Repo.
int slaHours = tenantConfigRepository.findById(tenantId)
                                     .map(TenantConfig::getWebhookSlaHours)
                                     .orElse(48); // Fallback base seguro

// Línea ~184 de WebhookIntakeService
.slaDeadline(ZonedDateTime.now().plusHours(slaHours))
```

### Paso 2: Auditoría RLS en RagSessionCleanerUseCase

**Archivo:** `C:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/application/usecase/ai/RagSessionCleanerUseCase.java`

Verifica que el borrado exija `tenantId` (y refactoriza la SQL/JPQL en PgVectorAdapter si no lo hace).
```java
// Snippet sugerido
// @Traceability: US-027, CA-04
pgVectorRepository.deleteBySessionIdAndTenantId(sessionId, tenantId);
```

### Paso 3: Parametrización y Limpieza General

1. Crea las entidades/repositorios faltantes `TenantConfigEntity` y `TenantConfigRepository`.
2. En `application.yml`, mapea la clave secreta HMAC usando `${HMAC_SECRET:mock-secret-for-local}`.
3. En `AuthSyncController.java`, purga las viñetas sospechosas con `tenant_alpha`.

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Erradicación de `.plusHours(4)` | Inspección visual en `WebhookIntakeService.java` (0 resultados `plusHours(4)`). |
| 2 | Entidad `TenantConfigEntity` activa | Archivo creado y mapeando `ibpms_tenant_config`. |
| 3 | Build/Compilación Exitosa + Push | `mvn clean compile` pasa sin fallos de inyección de dependencia. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Crea `TenantConfigEntity` y `TenantConfigRepository`.
2. Inyecta el repo en `WebhookIntakeService` y aplica la lógica de lectura "en caliente".
3. Audita `RagSessionCleanerUseCase` y `AuthSyncController`.
4. Compila `mvn clean compile`.
5. Commit: `git add . && git commit -m "feat(security): dynamic SLA config and strict RLS tenant isolation [T-01, T-03]" && git push`

---

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de ⚙️ BACKEND - JAVA.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/backend_sre_compilation_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat docs/architecture/adr-001-hexagonal-architecture.md
5. cat .agentic-sync/handoff_backend_security_T01_to_T03.md

TU MISIÓN:

1. Revisa `RagSessionCleanerUseCase` y asegura que el borrado de vectores exige estricta coincidencia del `tenantId` extraído del JWT. Agrega la restricción JPA si falta.
2. Crea una entidad JPA `TenantConfigEntity` (mapeada a la tabla `ibpms_tenant_config` con `tenant_id` y `webhook_sla_hours`).
3. En `WebhookIntakeService.java` línea ~184, elimina el `.plusHours(4)` hardcodeado y lee el SLA por tenant inyectando el nuevo repositorio. Usa 48 como default fallback.
4. Purga los strings quemados `"tenant_alpha"` y `"tenant_beta"` en `AuthSyncController` línea ~130.
5. Build/Compile: OBLIGATORIO validar dependencias con `mvn clean compile` o arrancando el contexto.
6. Commit: `git add . && git commit -m "feat(security): dynamic SLA config and strict RLS tenant isolation [T-01, T-03]" && git push`

REGLAS INQUEBRANTABLES:
- Cero Mocks: Ejecuta validaciones contra Spring Security real.
- Inyectar compulsoriamente el comentario `// @Traceability: US-XXX, CA-YY`.
- Prohibido dejar el código sin compilar.
```
