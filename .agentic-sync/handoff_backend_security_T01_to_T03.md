# 🧠→⚙️ Handoff: Arquitecto Líder → Backend
# Auditoría y Remediación de Seguridad (T-01, T-02, T-03)

**Emitido por:** [🧠 ARQUITECTO LÍDER]
**Destinatario:** [⚙️ BACKEND - JAVA]
**Fecha:** 2026-05-11T23:15:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🔴 ALTA (Bloqueantes de Seguridad Zero-Trust)

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Tu skill de SRE/Compilación Nativa
cat .agents/skills/backend_sre_compilation_audit/SKILL.md

# 3. Documentación Arquitectónica (ADR 001, ADR 010)
cat docs/architecture/adr-001-hexagonal-architecture.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código o configuración modificada DEBE incluir el comentario `// @Traceability: US-XXX, CA-YY`. Usa las US referenciadas abajo.

---

## 🔬 Diagnóstico del Arquitecto (Análisis de Código Actual)

He completado la auditoría pre-vuelo de las tareas T-01, T-02 y T-03. El diagnóstico revela que hay mitigaciones parciales, pero falta el cierre riguroso:

### T-01: IDOR en Destructor de Sesión RAG (US-027)
- **Estado Actual:** En `BpmnCopilotController.wipeCopilotMemory`, el `tenantId` ya se extrae de `SecurityContextUtils.getTenantId()`, lo cual mitiga el IDOR en el Controller.
- **Acción Requerida:** Verifica el interior de `RagSessionCleanerUseCase.wipeSessionFootprint()`. Asegúrate de que no existan bypasses en las queries a la base de datos de vectores (PgVectorAdapter/Storage) y que el `tenantId` actúe como una restricción WHERE estricta (Row-Level Security manual). Si está correcto, agrega las pruebas unitarias que certifiquen el 403 o la no-visibilidad de sesiones ajenas.

### T-02: Pipeline Seguridad Webhook (US-004)
- **Estado Actual:** `WebhookIntakeService` ya posee el esqueleto de HMAC (`validateHmacSignature`), ClamAV y Whitelist. Sin embargo, falta certificar el "Pipeline Completo".
- **Acción Requerida:** 
  1. Revisa `application.yml` (y sus perfiles) para verificar que las `webhookProperties` y la secret key del HMAC no estén vacías o hardcodeadas en texto plano sin soporte para variables de entorno (Vault/Env).
  2. Cerciórate de que el fallo de `ClamAvScanner` maneje un "Fail-Secure" real y que el IntegrationEventPublisher no filtre datos sensibles en el payload de emergencia.

### T-03: Purga de Hardcodes Zero-Trust (Varios)
- **Estado Actual:** 
  - Encontramos un SLA estático hardcodeado en `WebhookIntakeService` línea 184: `.slaDeadline(ZonedDateTime.now().plusHours(4)) // Enforced baseline 4-hour SLA`.
  - Los WebSockets parecen usar `"/topic/workdesk/" + tenantId`.
- **Acción Requerida:**
  1. Modifica la creación del `TriageTask` en `WebhookIntakeService` para que el SLA no sea `plusHours(4)` hardcodeado. Debe leer un parámetro de configuración (SLA por Tenant) o de las `webhookProperties`.
  2. Verifica que no existan más strings quemados (como el viejo `assistantId = 101edfe` o "tenant_alpha") en `WorkdeskQueryController` y `AuthSyncController` (viñetas sospechosas en líneas 104 y 130).

---

## 🎯 Criterios de Aceptación (DoD)

| Tarea | Criterio | Evidencia Esperada |
|-------|----------|--------------------|
| **T-01** | Vector Storage protegido por TenantId | Código de `RagSessionCleanerUseCase` validando RLS estricto y/o test de seguridad. |
| **T-02** | Webhook Sec Properties paramétricas | `application.yml` usando `${HMAC_SECRET}` y `application-e2e.yml` con valores mockeados seguros. |
| **T-03** | Erradicación de SLA `plusHours(4)` | Lógica inyectada vía Properties o Base de Datos por Tenant. Cero Hardcodes. |
| **Global**| GREEN BUILD nativo | `mvn spring-boot:run` arranca Tomcat sin errores de Dependency Injection. |

---

## 🚦 Instrucciones para el Agente Backend (Copiar y Pegar)

```text
Asume el rol de [⚙️ BACKEND - JAVA].

ANTES DE CODIFICAR, lee obligatoriamente estos archivos en este orden:
1. cat .cursorrules
2. cat .agents/skills/backend_sre_compilation_audit/SKILL.md
3. cat .agentic-sync/handoff_backend_security_T01_to_T03.md

TU MISIÓN ES RESOLVER LAS 3 TAREAS DE SEGURIDAD PENDIENTES DE LA ITERACIÓN 7.1:

1. T-01 (US-027): Revisa `RagSessionCleanerUseCase` y asegura que el borrado de vectores exige estricta coincidencia del `tenantId` extraído del JWT. Agrega pruebas o refactoriza para prevenir IDORs profundos.
2. T-02 (US-004): Revisa `application.yml` y parametriza todas las propiedades de seguridad de Webhooks (HMAC Secret, ClamAV URIs) para que no estén hardcodeadas.
3. T-03: Abre `WebhookIntakeService.java` línea ~184 y elimina el `.plusHours(4)` hardcodeado. Reemplázalo por una lectura de SLA configurado por el tenant o properties (`webhookProperties.getDefaultSlaHours()`). También purga los hardcodes `"tenant_alpha"` y `"tenant_beta"` en `AuthSyncController` línea ~130.

REGLAS INQUEBRANTABLES:
- Cero Mocks: Ejecuta validaciones contra Spring Security real.
- Inyecta la trazabilidad // @Traceability: US-XXX en todo lo que cambies.
- CIERRE DE MISIÓN: Estás obligado a ejecutar `mvn compile` y arrancar el servidor con `mvn spring-boot:run` para comprobar que Tomcat levanta. ¡Cero Stash! Commit directo y Push.
```
