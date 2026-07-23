# 🔧 Handoff Backend — US-002 (Claim/Unclaim de Tareas)

> **Sprint:** PM-01 | **Slot:** 1 | **Cadena:** 2 (Core Workdesk)
> **Rama de trabajo:** `sprint-8/pm-01/us-002-claim`
> **Emisor:** Arquitecto Líder | **Fecha:** 2026-06-03
> **Destinatario:** Agente Backend

---

## Pre-Handoff Checklist — US-002

| # | Verificación | Estado | Evidencia |
|---|---|---|---|
| 1 | US en sprint actual (Roadmap PM-IA) | ✅ | Sprint PM-01, Cadena 2 (Core Workdesk), Prioridad P0 |
| 2 | Endpoints definidos en API_CONTRACTS.md | ✅ | Sección 5.2 Workdesk (claim, release, bulk-claim, force-unclaim, audit-trail) |
| 3 | Prerrequisitos completados | ✅ | US-001 (Workdesk base): ✅ 100% Completada |
| 4 | Matriz de cobertura actualizada | ⚠️ | Desactualizada — se reconciliará post-ejecución |

**Resultado**: ✅ APROBADO para handoff

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | PM01-S1 |
| **User Story** | US-002: Reclamar una Tarea de Grupo (Claim Task) |
| **CAs Objetivo** | CA-15, CA-17, CA-19, CA-20 + Código muerto + Tests |
| **Rama Git** | `sprint-8/pm-01/us-002-claim` |
| **SSOT** | `docs/requirements/v1_user_stories_index.md` → `docs/requirements/epics/epic_A_motor_core.md` (líneas 359-588) |
| **Flujo** | Backend → Frontend → QA (aplazado) |

> ⚠️ **POLÍTICA ANTIAMNESIA (OBLIGATORIA):**
> Antes de escribir una sola línea de código, DEBES leer:
> 1. **Arquitectura Core:** `docs/architecture/arquitecturar.md`
> 2. **Épica A (US-002):** `docs/requirements/epics/epic_A_motor_core.md` (líneas 359-588)
> 3. **Este documento completo** antes de planificar.

---

## 2. Alineación Arquitectónica y ADRs

### ADRs Aplicables

| ADR | Impacto en este handoff |
|-----|------------------------|
| **ADR-001** (Hexagonal) | La lógica del `GhostJobScheduler` y el cleanup de archivos DEBEN vivir en la capa `application/service/`, NO en `infrastructure/`. Los schedulers son use cases, no adaptadores. |
| **ADR-009** (PostgreSQL) | Toda query DEBE usar `tenantId` del JWT. Las consultas del scheduler deben agrupar por tenant para aplicar timeouts diferenciados. |
| **ADR-010** (Testing Pyramid) | Los tests de integración DEBEN usar Testcontainers (PostgreSQL real), NO H2. Los tests unitarios son para lógica de dominio pura. |
| **ADR-011** (Local CQRS) | El audit trail es un modelo de lectura. Las queries de audit pueden usar proyecciones DTO optimizadas. |

### Stack Tecnológico Confirmado
- **Java 17** / **Spring Boot 3.2.3** / **PostgreSQL 15+**
- **Liquibase** para migraciones (si aplica)
- **JUnit 5** + **Testcontainers** para tests
- **MapStruct** para DTOs

### Trazabilidad de la Solución
Los cambios propuestos respetan la Arquitectura Hexagonal: el `GhostJobScheduler` es un use case en `application/service/` que orquesta puertos. El cleanup de archivos transitorios sigue el mismo patrón. Los `@Scheduled` métodos son driving adapters que invocan lógica de aplicación. Toda persistencia se delega al `Repository` (driven adapter).

---

## 3. Rutas Exactas y Contexto Preexistente

### Archivos a MODIFICAR

#### 3.1 `GhostJobScheduler.java` (91 líneas) — CA-15
- **Path:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/GhostJobScheduler.java`
- **Estado actual:**
  - `@Scheduled(fixedRate = 900000)` — ejecuta cada 15 min ✅
  - Detecta tareas CLAIMED con `elapsedMinutes >= thresholdMins` ✅
  - Emite 75% pre-warning via WebSocket `GHOST_WARNING` ✅
  - **BUG:** Usa `claimProperties.getGhostTimeout()` (valor global 240 min) en TODAS las tareas, ignorando `getTimeoutForTenant(tenantId)`.

#### 3.2 `ClaimProperties.java` (36 líneas) — CA-15
- **Path:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/config/ClaimProperties.java`
- **Estado actual:**
  - `@ConfigurationProperties(prefix = "ibpms.claim")` ✅
  - `ghostTimeout = 240` (default) ✅
  - `Map<String, Integer> tenantOverrides` — **EXISTE pero es código muerto**
  - `getTimeoutForTenant(String tenantId)` — **EXISTE pero NUNCA se invoca**

#### 3.3 `AgileTaskService.java` (444 líneas) — CA-19
- **Path:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/AgileTaskService.java`
- **Estado actual:**
  - Contiene `claimTask()`, `claimNextTask()`, `rollbackClaim()`, `unclaimTask()`, `forceUnclaimTask()`, `bulkClaim()`, `forceUnclaimWithValidation()`
  - El endpoint `extend-timeout` existe en `TaskClaimApiController:L117-123` pero la lógica de **límite de 2 extensiones consecutivas** debe verificarse y garantizarse.

#### 3.4 `ClaimAuditService.java` — CA-20
- **Path:** Buscar en `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/ClaimAuditService.java`
- **Estado actual:** El servicio registra eventos de auditoría. Verificar que los `action_type` incluyen TODOS los valores enriquecidos del CA-20: `CLAIMED`, `RELEASED`, `FORCE_UNCLAIMED`, `AUTO_UNCLAIMED`, `TIMEOUT_EXTENDED`, `BULK_CLAIMED`.

#### 3.5 `TaskClaimApiController.java` — Código muerto / Traceability
- **Path:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/TaskClaimApiController.java`
- **Estado actual:**
  - Expone endpoints DUPLICADOS en `/api/v1/tasks/*` que solapan con `WorkboxTaskController` en `/api/v1/workbox/tasks/*`.
  - El Frontend SOLO consume `/api/v1/workbox/tasks/*`.
  - Anotado con `@Traceability(US="US-004")` — **ERROR**: debe ser `US-002`.
  - **ACCIÓN:** Marcar este controller con `@Deprecated` y agregar Javadoc: `"Use WorkboxTaskController instead. Scheduled for removal in V2."`. Corregir `@Traceability` a `US-002`. **NO eliminar** el controller para no romper posibles consumidores externos.

### Archivos a CREAR

#### 3.6 Nuevo: `TransitoryFileCleanupScheduler.java` — CA-17
- **Path propuesto:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/TransitoryFileCleanupScheduler.java`
- **Propósito:** Job programado que elimina archivos con estado `orphaned` después de 24 horas.
- **Frecuencia:** `@Scheduled(cron = "0 0 3 * * ?")` — ejecuta diariamente a las 3:00 AM.

---

## 4. Snippets Prescriptivos

### 4.1 Fix CA-15 — GhostJobScheduler per-tenant timeout

**Lógica actual (INCORRECTO):**
```java
// GhostJobScheduler.java — línea ~52
int thresholdMins = claimProperties.getGhostTimeout(); // ← SIEMPRE 240
List<AgileTaskEntity> ghostTasks = taskRepository.findClaimedTasksOlderThan(thresholdMins);
```

**Lógica prescrita (CORRECTO):**
```java
@Scheduled(fixedRate = 900_000) // cada 15 min
@Transactional
public void detectGhostJobs() {
    // Agrupar tareas claimed por tenant para aplicar threshold diferenciado
    List<AgileTaskEntity> allClaimedTasks = taskRepository.findAllByStatus(TaskStatus.CLAIMED);

    Map<String, List<AgileTaskEntity>> tasksByTenant = allClaimedTasks.stream()
        .collect(Collectors.groupingBy(AgileTaskEntity::getTenantId));

    for (Map.Entry<String, List<AgileTaskEntity>> entry : tasksByTenant.entrySet()) {
        String tenantId = entry.getKey();
        int thresholdMins = claimProperties.getTimeoutForTenant(tenantId); // ← FIX: per-tenant
        int warningThresholdMins = (int) (thresholdMins * 0.75);

        for (AgileTaskEntity task : entry.getValue()) {
            long elapsedMinutes = Duration.between(task.getLastActivityAt(), Instant.now()).toMinutes();

            if (elapsedMinutes >= thresholdMins) {
                executeAutoUnclaim(task, thresholdMins);
            } else if (elapsedMinutes >= warningThresholdMins && !task.isGhostWarningEmitted()) {
                emitGhostWarning(task, thresholdMins - elapsedMinutes);
            }
        }
    }
}
```

### 4.2 Fix CA-19 — Límite de 2 extensiones consecutivas

**Verificar/agregar en la lógica de extend-timeout:**
```java
public void extendTimeout(UUID taskId, String userId) {
    AgileTaskEntity task = taskRepository.findByIdForUpdate(taskId)
        .orElseThrow(() -> new TaskNotFoundException(taskId));

    // Validar que el solicitante es el assignee actual
    if (!task.getAssignee().equals(userId)) {
        throw new ForbiddenOperationException("Solo el asignado puede extender el timeout");
    }

    // CA-19: Máximo 2 extensiones consecutivas
    int currentExtensions = task.getConsecutiveExtensions() != null ? task.getConsecutiveExtensions() : 0;
    if (currentExtensions >= 2) {
        throw new BusinessRuleViolationException(
            "Se alcanzó el límite de 2 extensiones consecutivas. " +
            "El auto-unclaim se ejecutará al cumplirse el timeout."
        );
    }

    // Reiniciar lastActivityAt y registrar extensión
    task.setLastActivityAt(Instant.now());
    task.setConsecutiveExtensions(currentExtensions + 1);
    task.setGhostWarningEmitted(false); // Reiniciar flag de pre-aviso
    taskRepository.save(task);

    // Auditoría CA-20
    claimAuditService.recordEvent(taskId, userId, ClaimActionType.TIMEOUT_EXTENDED,
        "Extensión " + (currentExtensions + 1) + " de 2");

    // Notificar al supervisor
    if (currentExtensions + 1 > 0) {
        notificationService.notifySupervisor(task.getTeamId(),
            "El operario " + userId + " ha solicitado extensión de timeout para tarea " + taskId);
    }
}
```

> **NOTA:** Si el campo `consecutiveExtensions` no existe en `AgileTaskEntity`, DEBES agregarlo como `Integer` con default 0. Si se necesita changelog Liquibase, crear uno con el nombre `db/changelog/changes/YYYYMMDD-add-consecutive-extensions-to-agile-task.xml`.

### 4.3 Fix CA-20 — Action Types enriquecidos

**Verificar/crear Enum `ClaimActionType`:**
```java
public enum ClaimActionType {
    CLAIMED,           // Reclamo voluntario (CA-01/CA-02)
    RELEASED,          // Liberado por el operario (CA-04)
    FORCE_UNCLAIMED,   // Despojo forzoso por supervisor (CA-08/CA-13)
    AUTO_UNCLAIMED,    // Auto-unclaim por inactividad (CA-06/CA-15)
    TIMEOUT_EXTENDED,  // Extensión de timeout solicitada (CA-19)
    BULK_CLAIMED       // Reclamada como parte de lote (CA-02)
}
```

**Verificar en `ClaimAuditService`** que TODOS los métodos de `AgileTaskService` usen el enum correcto al registrar auditoría:

| Método en AgileTaskService | ClaimActionType esperado |
|---|---|
| `claimTask()` | `CLAIMED` |
| `bulkClaim()` → `claimSingleTaskIsolated()` | `BULK_CLAIMED` |
| `unclaimTask()` | `RELEASED` |
| `forceUnclaimTask()` / `forceUnclaimWithValidation()` | `FORCE_UNCLAIMED` |
| `GhostJobScheduler.executeAutoUnclaim()` | `AUTO_UNCLAIMED` |
| `extendTimeout()` | `TIMEOUT_EXTENDED` |

### 4.4 Nuevo CA-17 — TransitoryFileCleanupScheduler

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class TransitoryFileCleanupScheduler {

    private final DocumentRepository documentRepository;

    /**
     * CA-17: Limpieza diaria de archivos transitorios (orphaned) con más de 24 horas.
     * Los archivos quedan orphaned cuando un operario sube adjuntos y luego libera
     * la tarea sin completar (Amnesia Transaccional CA-07).
     */
    @Scheduled(cron = "0 0 3 * * ?") // 3:00 AM diario
    @Transactional
    public void cleanupOrphanedFiles() {
        Instant cutoffTime = Instant.now().minus(Duration.ofHours(24));
        int deletedCount = documentRepository.deleteByStatusAndCreatedAtBefore("orphaned", cutoffTime);

        if (deletedCount > 0) {
            log.info("[CA-17] Limpieza de archivos transitorios completada. {} archivos eliminados (cutoff: {})",
                deletedCount, cutoffTime);
        }
    }
}
```

### 4.5 Fix Traceability — TaskClaimApiController

```java
// ANTES (INCORRECTO):
@Traceability(US = "US-004")

// DESPUÉS (CORRECTO):
@Deprecated(since = "PM-01", forRemoval = false)
@Traceability(US = "US-002")
// Javadoc: "⚠️ DEPRECATED: Use WorkboxTaskController (/api/v1/workbox/tasks/*) instead.
//           Este controller se mantiene por compatibilidad pero NO es consumido por el Frontend.
//           Scheduled for consolidation in V2."
```

---

## 5. Matriz de QA y Testing Atómico

### 5.1 Tests Backend Requeridos

> **NOTA:** El test `TaskClaimControllerTest.java.disabled` está MUERTO (referencia clase inexistente `TaskClaimController`). **Eliminarlo** limpiamente.

#### Archivo: `backend/ibpms-core/src/test/java/com/ibpms/poc/application/service/GhostJobSchedulerTest.java` [NUEVO]

| Test Name | CA | Aserción Esperada |
|-----------|-----|-------------------|
| `shouldAutoUnclaimAfterTenantSpecificTimeout` | CA-15 | Dado un tenant con override de 120 min, una tarea claimed con 130 min de inactividad debe ser auto-unclaimed |
| `shouldUseDefaultTimeoutWhenNoTenantOverride` | CA-15 | Dado un tenant SIN override, debe usar 240 min (default global) |
| `shouldEmitGhostWarningAt75Percent` | CA-15 | A los 180 min (75% de 240), debe emitir GHOST_WARNING |
| `shouldNotAutoUnclaimBeforeThreshold` | CA-15 | A los 200 min (< 240), NO debe auto-unclaim |

#### Archivo: `backend/ibpms-core/src/test/java/com/ibpms/poc/application/service/AgileTaskServiceTest.java` [NUEVO o AMPLIAR]

| Test Name | CA | Aserción Esperada |
|-----------|-----|-------------------|
| `shouldLimitExtensionsToMaxTwo` | CA-19 | Tarea con 2 extensiones previas → `BusinessRuleViolationException` |
| `shouldResetLastActivityOnExtend` | CA-19 | `lastActivityAt` actualizado a `Instant.now()` tras extensión |
| `shouldRecordEnrichedAuditTypes` | CA-20 | Cada operación registra su `ClaimActionType` correcto |
| `shouldRecordBulkClaimedType` | CA-20 | `bulkClaim()` registra `BULK_CLAIMED` (no `CLAIMED`) |
| `shouldClaimWithSkipLocked` | CA-11 | Claim concurrente: solo 1 gana, otro recibe 409 |
| `shouldBulkClaimWithPartialFailure` | CA-2 | 3 de 5 ya tomadas → response `{claimed: 2, conflicts: 3}` |
| `shouldForceUnclaimValidateTeamId` | CA-13 | Supervisor de otro equipo → 403 Forbidden |

#### Archivo: `backend/ibpms-core/src/test/java/com/ibpms/poc/application/service/TransitoryFileCleanupSchedulerTest.java` [NUEVO]

| Test Name | CA | Aserción Esperada |
|-----------|-----|-------------------|
| `shouldDeleteOrphanedFilesOlderThan24Hours` | CA-17 | Archivos orphaned con >24h eliminados, <24h intactos |
| `shouldLogCleanupCount` | CA-17 | Log INFO con count correcto |

---

## 6. Directivas Obligatorias

### 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA

El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
**PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

### ⚠️ PRECISIÓN QUIRÚRGICA

Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

### 📚 SKILLS DE CODIFICACIÓN OBLIGATORIOS
- Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).

### 🔨 COMPILACIÓN OBLIGATORIA
Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

---

## INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN

1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_BACKEND.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_BACKEND.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `sprint-8/pm-01/us-002-claim`. Queda estrictamente prohibido usar git stash.
