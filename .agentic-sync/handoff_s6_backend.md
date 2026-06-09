# 🔧 Handoff Backend — Sprint 6 / Iteración 6.1

> **Iteración:** Sprint 6 — Iteración 6.1 (Hotfixes P0 + Infraestructura E2E + B-20)  
> **Rama de trabajo:** `sprint-6/uat-certification` (crear desde `main`)  
> **US objetivo:** US-007/US-027 (IDOR), US-004 (Webhook Legacy), US-005/US-007 (B-20 DMN↔BPMN)  
> **Flujo:** Backend → Frontend → QA  
> **SSOT de referencia:** `docs/requirements/v1_user_stories_index.md` → `docs/requirements/epics/epic_B_formularios_bpmn.md`, `epic_A_orquestacion.md`  
> **Autor:** Arquitecto Líder SW  
> **Fecha:** 2026-04-19

---

## 1. Metadatos y SSOT

| Parámetro | Valor |
|-----------|-------|
| **Sprint** | 6 — Iteración 6.1 |
| **Rama Git** | `sprint-6/uat-certification` |
| **US** | US-004 (Webhook), US-007 (DMN), US-027 (Copilot IA), US-005 (BPMN) |
| **Bloques de trabajo** | B1: IDOR Copilot (P0), B2: Deprecar EmailWebhook (P0), B3: Endpoint catálogo DMN (B-20), B4: Docker Compose E2E + Seed Data |
| **Exclusiones** | US-017 (CQRS) queda excluida del Sprint 6 — deuda V2 |

**Fuentes de verdad:**
- `docs/requirements/epics/epic_B_formularios_bpmn.md` → US-005, US-007
- `docs/requirements/epics/epic_A_orquestacion.md` → US-004, US-027
- `docs/sprints/sprint_plan_s6.md` → Plan del Sprint completo
- `.agentic-sync/coverage_matrix.md` → Matriz de cobertura global

---

## 2. Alineación Arquitectónica y ADRs

| ADR | Impacto en esta iteración |
|-----|---------------------------|
| `adr-001-hexagonal-architecture.md` | Todos los endpoints nuevos en `infrastructure/web/`. Lógica de negocio en `domain/` o `application/`. |
| `adr_009_postgresql_pgvector_migration.md` | Seed data E2E usa PostgreSQL puro con Liquibase. |
| `adr_010_testing_pyramid_governance.md` | Tests unitarios obligatorios para cada hotfix (JUnit/Mockito). |
| `adr_013_dual_rag_strategy.md` | B-02 (Copilot) toca el pipeline RAG — el tenant debe propagarse al `triggerRagSessionWipe`. |

**Principios Zero-Trust confirmados:**
- Todo acceso a tenant DEBE usar `SecurityContextUtils.getTenantId()` — NUNCA hardcodes.
- El `EmailWebhookController` bypasea el pipeline de seguridad OAuth2 (solo valida `ClientState` header) — viola Zero-Trust.
- El endpoint catalogo DMN (`B-20`) DEBE filtrar por tenant del JWT invoker.

---

## 3. Rutas Exactas y Contexto Preexistente

### B1: IDOR en BpmnCopilotController (P0 — CONFIRMADO)
- **Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/ai/BpmnCopilotController.java`
- **Línea 73:** `String tenantId = "tenant_hq_corp";` ← **HARDCODED, VIOLACIÓN IDOR**
- **Método afectado:** `wipeCopilotMemory(@RequestParam String sessionId)` (L69-L77)
- **Dependencia:** `SecurityContextUtils.java` en `application/util/` — ya existe, provee `getTenantId()` y `getAssignee()`.
- **UseCase afectado:** `BpmnCopilotUseCase.triggerRagSessionWipe(tenantId, sessionId)` — ya acepta tenant como parámetro.

### B2: Deprecar EmailWebhookController (P0 — CONFIRMADO)
- **Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/EmailWebhookController.java` (49 líneas)
- **Estado actual:** Acepta POST sin JWT, sin HMAC, sin ClamAV, sin domain whitelist. Solo valida `ClientState` header estático.
- **Ruta expuesta:** `POST /inbound/email-webhook`
- **Pipeline seguro existente:** `WebhookIntakeService` (US-004/US-034) con RabbitMQ, validación HMAC y ClamAV.

### B3: Endpoint catálogo DMN para B-20
- **Archivo a crear:** Nuevo endpoint en `DmnGovernanceController.java` (L52-63 ya tiene `/catalog` pero retorna lista vacía)
- **Motor Camunda:** API REST Camunda 7 en `http://localhost:8080/engine-rest/decision-definition` lista todas las DMN deployadas.
- **Contrato existente:** `DmnGovernanceUseCase.java` — necesita nuevo método `listDeployedDecisionDefinitions(tenantId)`.

### B4: Docker Compose E2E
- **Infraestructura existente:** `docker-compose.yml` en raíz del proyecto (backend dev).
- **Nuevo archivo:** `docker-compose.e2e.yml` — ambiente completo aislado.
- **Seed SQL:** `backend/ibpms-core/src/main/resources/db/seed/e2e_seed.sql` (nuevo).
- **Profile Spring:** `application-e2e.yml` (nuevo).

---

## 4. Snippets Prescriptivos

### B1: Fix IDOR — BpmnCopilotController (Cirugía de 2 líneas + import)

**Antes (L71-76):**
```java
@DeleteMapping("/session")
@PreAuthorize("hasAnyAuthority('ROLE_PROCESS_ARCHITECT', 'ROLE_BPMN_DESIGNER')")
public ResponseEntity<Void> wipeCopilotMemory(@RequestParam String sessionId) {
    // En V1 extraemos Tenant_ID asumiendo un Auth Context Mockeado
    String tenantId = "tenant_hq_corp"; 
    
    copilotUseCase.triggerRagSessionWipe(tenantId, sessionId);
    return ResponseEntity.ok().build();
}
```

**Después:**
```java
import com.ibpms.poc.application.util.SecurityContextUtils;  // AGREGAR IMPORT

@DeleteMapping("/session")
@PreAuthorize("hasAnyAuthority('ROLE_PROCESS_ARCHITECT', 'ROLE_BPMN_DESIGNER')")
public ResponseEntity<Void> wipeCopilotMemory(@RequestParam String sessionId) {
    String tenantId = SecurityContextUtils.getTenantId();
    
    copilotUseCase.triggerRagSessionWipe(tenantId, sessionId);
    return ResponseEntity.ok().build();
}
```

### B2: Deprecar EmailWebhookController (Opción Fencing)

```java
@Deprecated(since = "v1.0.0", forRemoval = true)
@RestController
@RequestMapping("/inbound/email-webhook")
public class EmailWebhookController {

    @PostMapping
    public ResponseEntity<Map<String, String>> receiveEmailNotification(
            @RequestHeader(value = "ClientState", required = false) String clientState,
            @RequestBody Map<String, Object> payload) {
        // SECURITY GATE: Legacy endpoint deprecado.
        // Todas las integraciones deben usar POST /api/v1/intake/webhook (WebhookIntakeService)
        return ResponseEntity.status(HttpStatus.GONE) // HTTP 410 Gone
                .body(Map.of(
                    "error", "ENDPOINT_DEPRECATED",
                    "message", "This endpoint has been deprecated. Use POST /api/v1/intake/webhook with HMAC validation.",
                    "migration", "/api/v1/intake/webhook"
                ));
    }
}
```

### B3: Endpoint catálogo DMN desplegadas

**Nuevo método en `DmnGovernanceController.java`:**
```java
/**
 * B-20: Lista las tablas DMN publicadas en el motor Camunda.
 * Consumido por BpmnDesigner (Frontend) para dropdown visual de decisionRef.
 */
@GetMapping("/definitions")
public ResponseEntity<?> listDeployedDmnDefinitions() {
    String invokerTenant = SecurityContextUtils.getTenantId();
    var definitions = dmnGovernanceUseCase.listDeployedDecisionDefinitions(invokerTenant);
    return ResponseEntity.ok(definitions);
}
```

**Nuevo método en `DmnGovernanceUseCase.java`:**
```java
/**
 * Consulta la API REST de Camunda 7 (/engine-rest/decision-definition)
 * filtrando por tenant y retornando id, key, name, version, deploymentId.
 */
public List<DmnDefinitionDto> listDeployedDecisionDefinitions(String tenantId) {
    // Integración con Camunda REST API
    // GET /engine-rest/decision-definition?tenantIdIn={tenantId}&latestVersion=true
    // Mapear a DmnDefinitionDto(id, key, name, version, deploymentDate)
}
```

**DTO nuevo:** `DmnDefinitionDto.java`
```java
public record DmnDefinitionDto(
    String id,
    String key,
    String name,
    int version,
    String deploymentId,
    String deploymentDate
) {}
```

### B4: Docker Compose E2E

**`docker-compose.e2e.yml` (raíz del proyecto):**
```yaml
version: '3.8'
services:
  postgres-e2e:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: ibpms_e2e
      POSTGRES_USER: ibpms
      POSTGRES_PASSWORD: ibpms_e2e_pass
    ports:
      - "5433:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ibpms -d ibpms_e2e"]
      interval: 5s
      retries: 5

  redis-e2e:
    image: redis:7-alpine
    ports:
      - "6380:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      retries: 5

  camunda-e2e:
    image: camunda/camunda-bpm-platform:run-7.21.0
    ports:
      - "8085:8080"
    healthcheck:
      test: ["CMD-SHELL", "curl -sf http://localhost:8080/engine-rest/version || exit 1"]
      interval: 10s
      retries: 10

  rabbitmq-e2e:
    image: rabbitmq:3.12-management-alpine
    ports:
      - "5673:5672"
      - "15673:15672"
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 10s
      retries: 5
```

**`application-e2e.yml`:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/ibpms_e2e
    username: ibpms
    password: ibpms_e2e_pass
  redis:
    host: localhost
    port: 6380
  rabbitmq:
    host: localhost
    port: 5673

camunda:
  bpm:
    client:
      base-url: http://localhost:8085/engine-rest
```

**`e2e_seed.sql` (idempotente):**
```sql
-- Tenants E2E
INSERT INTO tenants (id, name, domain) VALUES 
  ('tenant_alpha', 'Alpha Corp', 'alpha.com'),
  ('tenant_beta', 'Beta Inc', 'beta.com')
ON CONFLICT (id) DO NOTHING;

-- Usuarios E2E (passwords: BCrypt de 'Test1234!')
INSERT INTO users (id, email, password_hash, tenant_id, display_name) VALUES
  ('usr_admin_alpha', 'admin@alpha.com', '$2a$10$...', 'tenant_alpha', 'Admin Alpha'),
  ('usr_oper_alpha', 'operario@alpha.com', '$2a$10$...', 'tenant_alpha', 'Operario Alpha'),
  ('usr_arch_alpha', 'arquitecto@alpha.com', '$2a$10$...', 'tenant_alpha', 'Arquitecto Alpha'),
  ('usr_admin_beta', 'admin@beta.com', '$2a$10$...', 'tenant_beta', 'Admin Beta'),
  ('usr_oper_beta', 'operario@beta.com', '$2a$10$...', 'tenant_beta', 'Operario Beta')
ON CONFLICT (id) DO NOTHING;

-- Roles RBAC
INSERT INTO user_roles (user_id, role) VALUES
  ('usr_admin_alpha', 'ROLE_SUPER_ADMIN'),
  ('usr_oper_alpha', 'ROLE_OPERARIO'),
  ('usr_arch_alpha', 'ROLE_PROCESS_ARCHITECT'),
  ('usr_admin_beta', 'ROLE_SUPER_ADMIN'),
  ('usr_oper_beta', 'ROLE_OPERARIO')
ON CONFLICT DO NOTHING;
```

---

## 5. Matriz de QA y Testing Atómico

| Test Name | Bloque | Aserción Esperada |
|-----------|:------:|-------------------|
| `BpmnCopilotControllerTest.wipeCopilotMemory_usesTenantFromJwt` | B1 | `verify(copilotUseCase).triggerRagSessionWipe("tenant_from_jwt", "session123")` — NUNCA `tenant_hq_corp` |
| `BpmnCopilotControllerTest.wipeCopilotMemory_rejectsCrossTenant` | B1 | HTTP 403 cuando el session no pertenece al tenant del JWT |
| `EmailWebhookControllerTest.receiveEmail_returnsGone` | B2 | `status().isGone()` — HTTP 410 con body `ENDPOINT_DEPRECATED` |
| `DmnGovernanceControllerTest.listDefinitions_filtersByTenant` | B3 | Retorna solo DMN del tenant del invoker, NUNCA de otro tenant |
| `DmnGovernanceControllerTest.listDefinitions_emptyWhenNoDmn` | B3 | Retorna `[]` cuando no hay DMN desplegadas para el tenant |

---

## 6. Mensaje de Despacho

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `sprint-6/uat-certification`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).

> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

> 📚 **WORKFLOW DE GOBERNANZA OBLIGATORIO:**
> - Al finalizar, actualiza `.agentic-sync/coverage_matrix.md` según el workflow `.agent/workflows/reconciliacionCoberturaCa.md` — cruzando SSOT, handoff, commit y matriz.
> - Aplica la estrategia QA dictada por `.agent/workflows/router_certificacion_qa.md` — para esta iteración selecciona el path "Certificación Exclusiva Backend (APIs sin interfaz gráfica)" con REST Assured para B1/B2/B3.
> - Todo cierre se documenta según `.agent/workflows/cierreDeudaTecCriteriosAceptacion.md` — Fase 5 (trazabilidad) y Fase 6 (resumen ejecutivo).
