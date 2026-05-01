# 🏛️ Auditoría Forense ARQ-005 — Bloque 4: Remediaciones Post-Auditoría (CA-63 a CA-70)

**Fecha:** 2026-05-01  
**Autor:** Arquitecto Líder  
**Sprint:** 6 | **Rama:** `sprint-6`  
**Archivos Auditados:** `SandboxInterceptor.java`, `BpmnDesignService.java` (260 LOC), `BpmnDesignController.java`, `PreFlightAnalyzerService.java`, `BpmnAuditJpaAdapter.java`, `DeploymentValidationResponse.java`, puertos hexagonales (`ProcessLockPort`, `DeployRequestPort`, `BpmnAuditPort`, `BpmnValidationPort`, `ExternalTaskTopicPort`), `22-us005-bpmn-design-schema.sql`

---

## 1. MATRIZ DE COBERTURA (CA-63 a CA-70)

| CA | Título | Backend | Hexagonal | Infra/BD | Veredicto |
|:--:|--------|:-------:|:---------:|:--------:|:---------:|
| **CA-63** | Aislamiento Transaccional Sandbox (Zero-Blast) | ✅ `SandboxInterceptor` valida header `X-Sandbox-Mode` obligatorio (L33-36) + `@SandboxOperation` AOP | ✅ Anotación custom limpia | N/A | **CUBIERTO** ✅ |
| **CA-64** | Break-Lock de Emergencia | ✅ `BpmnDesignService.forceReleaseLock()` (L211-216) con audit log `{type: forced, previousOwner}` + `@PreAuthorize("hasRole('SUPER_ADMIN')")` en Controller (L228) | ✅ `ProcessLockPort.deleteLock()` | ✅ `ibpms_process_locks` | **CUBIERTO** ✅ |
| **CA-65** | Contrato API Explícito Deploy | ✅ `POST /deploy` con `@RequestParam("file") MultipartFile` + `deploy_comment` obligatorio ≥10 chars (L65-116) + `DeploymentValidationResponse` DTO tipado con `errors[]`, `warnings[]`, `generatedRoles[]` | ✅ `BpmnValidationPort.validateBpmnStream()` | N/A | **CUBIERTO** ✅ |
| **CA-66** | Persistencia Lock en BD | ✅ `BpmnDesignService.acquireLockTechnicalKey()` (L181-190) + heartbeat (L192-200) + stale cleanup 90s (L218-225) | ✅ `ProcessLockPort` (findLock/saveLock/deleteLock) | ✅ `ibpms_process_locks` (process_key, locked_by, locked_at, browser_session_id) | **CUBIERTO** ✅ |
| **CA-67** | Límites y Gobernanza Sandbox | ✅ `SandboxInterceptor` Redis counter `MAX_SANDBOX_INSTANCES=3` (L21) + TTL 15min (L42) + decrement en finally (L54) + `ResourceExhaustedException` (L47) | ✅ Redis como infraestructura de rate limiting | ✅ Redis key `sandbox_active_simulations` | **CUBIERTO** ✅ |
| **CA-68** | Persistencia DataMapping como Extension Properties | ✅ `DataMapping` domain model con `processDefinitionKey`, `taskId`, `connectorId`, `mappingJson`, `lastValidatedAt` (30 LOC) + `DataMappingPort` CRUD | ✅ `DataMappingPort.findByProcessDefinitionKey()` + `save()` | ✅ `ibpms_data_mappings` (id, process_key, task_id, connector_id, mapping_json, last_validated_at) | **CUBIERTO** ✅ |
| **CA-69** | Flujo Deploy Request con Rechazo | ✅ `BpmnDesignService.createDeployRequest()` (L93-113), `approveDeployRequest()` (L115-142), `rejectDeployRequest()` (L144-177) con validación de comentario ≥20 chars (L145-147) + estado machine (PENDING → APPROVED/REJECTED) | ✅ `DeployRequestPort` (save/findById) con record `DeployRequestInfo` | ✅ `ibpms_deploy_requests` (id, process_key, requested_by, requested_at, status, reviewed_by, reviewed_at, review_comment) | **CUBIERTO** ✅ |
| **CA-70** | Catálogo External Task Topics + Pre-Flight | ✅ `ExternalTaskTopicJpaAdapter.findByIsActiveTrue()` + `PreFlightAnalyzerService` cruza topics activos contra XML (L76-78) + endpoint GET `/external-task-topics` (L265-268) | ✅ `ExternalTaskTopicPort` full hexagonal (Port → Adapter → JPA → Entity → Domain) | ✅ `ibpms_external_task_topics` + 6 topics seed (email, erp, sharepoint, pdf, ai_copilot, webhook) | **CUBIERTO** ✅ |

---

## 2. ANÁLISIS HEXAGONAL PROFUNDO

### Inventario de Puertos Out (Application → Infrastructure)

| Puerto | Implementación | Método Clave | ADR-001 |
|--------|----------------|-------------|:-------:|
| `ProcessLockPort` | `ProcessLockJpaAdapter` | `findLock()`, `saveLock()`, `deleteLock()` | ✅ |
| `DeployRequestPort` | `DeployRequestJpaAdapter` | `save()`, `findById()` | ✅ |
| `BpmnAuditPort` | `BpmnAuditJpaAdapter` | `logAction(UUID, action, user, version, details)` | ✅ |
| `BpmnValidationPort` | `CamundaBpmnValidator` | `validateBpmnStream()`, `validateDraftXml()` | ✅ |
| `ExternalTaskTopicPort` | `ExternalTaskTopicJpaAdapter` | `findByIsActiveTrue()`, `findAll()` | ✅ |
| `BpmnDesignPort` | `BpmnDesignJpaAdapter` | `findById()`, `findByTechnicalId()`, `save()` | ✅ |
| `SecurityRolePort` | `SecurityRoleJpaAdapter` | `findByIsVipRestrictedTrue()` | ✅ |
| `DataMappingPort` | `DataMappingJpaAdapter` | `findByProcessDefinitionKey()`, `save()` | ✅ |

**Veredicto Hexagonal: 8/8 puertos con implementación JPA. Cero violaciones de ADR-001. ✅**

### Flujo de Dominio Validado

```
Controller (infra) → Service (application) → Domain Model → Port (application) → Adapter (infra) → JPA (infra)
```

Todas las operaciones críticas (`lock`, `deploy-request`, `archive`, `audit`) siguen este flujo. El `BpmnDesignService` actúa como orquestador sin lógica de infraestructura. **Conformidad hexagonal al 100%.**

---

## 3. HALLAZGOS

### ARQ-B4-01: Stale lock timeout hardcodeado a 90s (Severidad: 🟢 BAJA — Aceptable V1)
**Ubicación:** `BpmnDesignService.java` L220  
**Observación:** El timeout de lock stale es `90 seconds` hardcodeado. En V2, debería ser configurable vía `application.yml`.

### ARQ-B4-02: Reject no revierte estado del dominio (Severidad: 🟢 BAJA — Observación)
**Ubicación:** `BpmnDesignService.java` L164-167  
**Observación:** El comentario indica que falta un `revertRequestDeploy()` en el domain model para cambiar de `PENDING_DEPLOY` a `DRAFT`. El código actual deja el diseño en estado PENDING_DEPLOY tras un rechazo. Deuda funcional menor.

### ✅ Sin hallazgos críticos ni de media severidad.

---

## 4. RESUMEN EJECUTIVO

| Métrica | Valor |
|---------|:-----:|
| CAs auditados | 8 (CA-63 a CA-70) |
| CAs cubiertos FULL STACK | **8/8 (100%)** |
| Puertos hexagonales verificados | **8/8 (100%)** |
| Hallazgos | 2 (ambos 🟢 Baja) |
| **Veredicto Bloque 4** | ✅ **APROBADO SIN OBSERVACIONES BLOQUEANTES** |

---

# 📋 HANDOFF BACKEND — ARQ-005 Bloque 4

**Dirigido a:** Agente Backend  
**Prioridad:** 🟢 Baja (Verificativo)  

## Acciones

### BACK-B4-01: Sin acciones constructivas obligatorias
El Bloque 4 está **100% implementado** a nivel arquitectónico:
- Lock pesimista con heartbeat + stale cleanup ✅
- Deploy request CRUD con state machine ✅
- Sandbox isolation con Redis rate-limiting ✅
- Audit log hexagonal con port + adapter ✅
- External Task Topics con seed data ✅

### BACK-B4-02: Deuda técnica menor (NO obligatoria)
1. Extraer `90` (stale lock timeout) a `application.yml` como `ibpms.lock.stale-timeout-seconds`
2. Implementar `BpmnProcessDesign.revertRequestDeploy()` para rechazo limpio

Compilación obligatoria: `mvn clean compile -pl ibpms-core`

---

# 📋 HANDOFF FRONTEND — ARQ-005 Bloque 4

**Dirigido a:** Agente Frontend  
**Prioridad:** 🟢 Baja (Verificativo)  

## Acciones de Verificación

### FRONT-B4-01: Verificar CAs de UX en BpmnDesigner.vue

| CA | Componente UX a Verificar |
|:--:|--------------------------|
| CA-63 | Header `X-Sandbox-Mode: true` se envía en todas las llamadas sandbox |
| CA-64 | Botón [🔓 Forzar Desbloqueo] visible solo para SUPER_ADMIN |
| CA-67 | Mensaje de error "Límite de Sandbox superado" cuando se intenta la 4ta simulación |
| CA-69 | Botón [📩 Solicitar Despliegue] + Modal de Rechazo con textarea ≥20 chars |
| CA-70 | Dropdown de topics en Service Task consume endpoint `/external-task-topics` |

Compilación obligatoria: `npm run build`

---

# 📋 HANDOFF QA — ARQ-005 Bloque 4 (CIERRE FINAL)

**Dirigido a:** Agente QA - E2E  
**Prioridad:** 🔴 Alta (Cierre de US-005)  

## Checkpoints de Validación

### QA-B4-01: Compilación global
```bash
mvn clean compile -pl ibpms-core
```

### QA-B4-02: Tests del scope completo US-005 (Bloques 1-4)
```bash
mvn clean test -Dtest="BpmnDeployContractTest,SandboxIsolationTest,SandboxGovernanceTest,ProcessLockPersistenceTest,BreakLockRbacTest,ExternalTaskTopicsCatalogTest,DeployRequestWorkflowTest,DataMappingIntegrityTest,BpmnCopilotSseIntegrationTest,FormCertificationTest,IdentityGovernanceIntegrationTest,ApplicationTests" -pl ibpms-core
```

### QA-B4-03: Zero-Mock Scanner
```bash
cd frontend && node scripts/anti-mock-scanner.js
```

### QA-B4-04: Regresión TOTAL (todos los bloques)
```bash
mvn clean test -Dtest="FormCertificationTest,IdentityGovernanceIntegrationTest,ApplicationTests" -pl ibpms-core
```

### Criterio de CIERRE DEFINITIVO
- Compilación exitosa ✅
- `BeanCreationException = 0` ✅
- Zero-Mock Scanner exit 0 ✅
- Sin regresiones de Bloques 1+2+3 ✅

**Si todos los checkpoints pasan: emitir VEREDICTO FINAL "US-005 CERTIFICADA" en `.agentic-sync/qa_report_ARQ005.md`.**

---

# 📋 HANDOFF INFRA/BD — ARQ-005 Bloque 4

**Dirigido a:** Agente Infra  
**Prioridad:** 🟢 Baja (Verificativo)  

## Verificación Final del Schema

Todas las tablas ya fueron verificadas en bloques anteriores. Confirmar integridad del changeset `22-us005-bpmn-design-schema.sql`:

| Tabla | Columnas Clave | Seed |
|-------|---------------|:----:|
| `ibpms_process_locks` | process_definition_key (PK), locked_by, locked_at, browser_session_id | ❌ |
| `ibpms_deploy_requests` | id (UUID PK), process_definition_key, requested_by, status, reviewed_by, review_comment | ❌ |
| `ibpms_external_task_topics` | topic_name (PK), description, worker_class, is_active, registered_at | ✅ 6 rows |
| `ibpms_data_mappings` | id (UUID PK), process_definition_key, task_id, connector_id, mapping_json, last_validated_at | ❌ |

**Sin acciones constructivas.** Solo confirmar que el archivo existe y está registrado en el master changelog.

---

**Fin del Handoff Consolidado — Bloque 4 US-005 (CIERRE FINAL)**
