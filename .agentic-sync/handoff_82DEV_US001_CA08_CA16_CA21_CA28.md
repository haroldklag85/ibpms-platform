# 🏗️ Handoff Técnico: Iteración 82-DEV — US-001 (Cierre Deuda Técnica)
> **Fecha:** 2026-04-14 | **Rama:** `sprint-3/informe_auditoriaSprint1y2`
> **User Story:** US-001 — Obtener Tareas Pendientes en el Workdesk
> **CAs Objetivo:** CA-08, CA-16, CA-21, CA-28
> **Fuente SSOT:** `docs/requirements/epics/epic_A_motor_core.md`
> **Coverage:** `.agentic-sync/coverage_matrix.md`
> **NFR Aplicable:** NFR-OBS-01 (Trazabilidad Extrema / Audit Logging)

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| Iteración | 82-DEV |
| Sprint | Sprint-3 |
| Rama Git | `sprint-3/informe_auditoriaSprint1y2` |
| User Story | US-001: Obtener Tareas Pendientes en el Workdesk |
| CAs a desarrollar | CA-08, CA-16, CA-21, CA-28 |
| Flujo de Trabajo | **Backend → Frontend → QA (Secuencial Estricto)** |
| SSOT Requerimientos | `docs/requirements/epics/epic_A_motor_core.md` |

---

## 2. Alineación Arquitectónica y ADRs

### Pilares Innegociables Impactados
- **Tenant Isolation (CA-14 ya implementado):** Toda nueva query DEBE inyectar `tenantId = :myTenant`. Los endpoints de CA-08/CA-16/CA-21/CA-28 heredan este perímetro.
- **Zero-Trust / RBAC:** El Feature Toggle de CA-08 requiere validación de rol `ADMIN_GLOBAL` para encender/apagar. Los Skips de CA-21 generan Audit Log inmutable (NFR-OBS-01).
- **Concurrencia Pesimista (CA-28):** Se reutiliza el patrón `SELECT ... FOR UPDATE SKIP LOCKED` ya referenciado en US-002 CA-11.
- **CQRS Projection:** Las consultas operan sobre la proyección `ibpms_workdesk_projection`, NO sobre tablas transaccionales de Camunda.

### Trazabilidad de la Solución
El bloque CA-08 + CA-16 + CA-21 + CA-28 forma un **subsistema cohesivo de Enrutamiento Forzoso**:
1. CA-08 activa el modo (Feature Toggle Admin).
2. CA-16 define el algoritmo (Skill-Based Routing + Anti Cherry-Picking).
3. CA-21 define el Skipeo Justificado y los Skills del operario.
4. CA-28 previene la condición de carrera cuando 200 operarios presionan simultáneamente.

---

## 3. Análisis Funcional por CA (Gherkin Literal del SSOT)

### CA-08: Intervención Administrativa Anti Cherry-Picking
> Given que el Administrador Global detecta sesgos de selección
> When enciende la bandera del sistema de "Enrutamiento Forzoso" (Feature Toggle)
> Then la vista de Workdesk de los operarios oculta la tabla/lista selectiva
> And presenta un único call-to-action gigante: `[Atender Siguiente Tarea]`, forzando a resolver lo más crítico o antiguo.

### CA-16: Anti Cherry-Picking y Enrutamiento por Habilidades (Skill-Based)
> Given la activación del interruptor de "Atender Siguiente"
> When el operario oprime el botón
> Then el motor Backend cruza la tarea más antigua/crítica contra el "Array de Skills" del operario.
> And proveerá "Pausa / Skipeo Justificado" si la tarea exige contactar a un cliente que no responde.
> And este interruptor dejará huella inmutable en el Audit Log Central.

### CA-21: Definición del Skill-Based Routing y Skipeo Justificado
> 1. Las habilidades (skills) se administran en la Pantalla 14 (US-036) como un array de etiquetas simples. **V1 NO soporta niveles de experticia** (diferido a V2).
> 2. Algoritmo: cruzar etiqueta de categoría de la tarea más antigua/crítica contra array de skills del operario.
> 3. Fallback Universal: si NINGUNA tarea coincide, asignar la más antigua/crítica independientemente del skill + WARNING en Audit Log.
> 4. Skipeo Justificado: Dropdown con motivos predefinidos: "Cliente no responde", "Requiere documentación adicional", "Fuera de mi área de conocimiento", "Otro" (campo libre obligatorio, mín. 10 chars).
> 5. Cada Skip → asiento inmutable en Audit Log: `{userId, taskId, skip_reason, timestamp}`. Más de 3 Skips consecutivos → alerta al Supervisor.

### CA-28: Prevención de Condición de Carrera en "Atender Siguiente"
> Given 200 operarios presionando "Atender Siguiente" simultáneamente
> Then el Backend garantizará asignación atómica usando `SELECT ... FOR UPDATE SKIP LOCKED`.
> And si la tarea ya fue asignada a otro, el Backend seleccionará la SIGUIENTE tarea disponible.
> And el operario NUNCA recibirá error "Tarea ya asignada" — el Backend resuelve la colisión internamente.

---

## 4. Rutas Exactas y Contexto Preexistente

### Backend (Java/Spring Boot)

| Archivo | Ruta | Estado Actual |
|---------|------|---------------|
| **WorkdeskProjectionEntity** | `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/WorkdeskProjectionEntity.java` | Tiene `tenantId`, `impactLevel`. **Necesita:** campo `categoryTag` (para match de skill). |
| **WorkdeskProjectionRepository** | `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/repository/WorkdeskProjectionRepository.java` | Tiene query JPQL con tenant isolation. **Necesita:** query con `FOR UPDATE SKIP LOCKED` para asignación atómica. |
| **WorkdeskQueryController** | `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/WorkdeskQueryController.java` | Existe. **Necesita:** nuevo endpoint `POST /api/v1/workdesk/attend-next`. |
| **DDL Liquibase** | `backend/ibpms-core/src/main/resources/db/changelog/` | **Necesita:** nuevo script para: tabla `ibpms_feature_toggles`, tabla `ibpms_task_skips`, columna `category_tag` en projection. |
| **RoleEntity / SkillEntity** | `backend/ibpms-core/src/main/java/com/ibpms/poc/` (buscar en domain/infrastructure) | US-036 ya implementó roles. **Necesita:** campo `skills` (array JSON) en la entidad de usuario o tabla pivote. |

### Frontend (Vue 3 / Pinia)

| Archivo | Ruta | Estado Actual |
|---------|------|---------------|
| **Workdesk.vue** | `frontend/src/views/Workdesk.vue` | Tiene grilla, filtros, paginación. **Necesita:** modo "Atender Siguiente" con CTA gigante. |
| **useWorkdeskStore.ts** | `frontend/src/stores/useWorkdeskStore.ts` | Tiene actions para fetch, filtros. **Necesita:** action `attendNext()`, estado `forceRoutingEnabled`. |

---

## 5. Snippets Prescriptivos

### 5.A — DDL (Liquibase Migration)

```sql
-- 27-us001-attend-next-schema.sql

-- Feature Toggles Table
CREATE TABLE IF NOT EXISTS ibpms_feature_toggles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    toggle_key VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT false,
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_toggle_tenant_key UNIQUE (tenant_id, toggle_key)
);

-- Task Skip Audit Log
CREATE TABLE IF NOT EXISTS ibpms_task_skips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    task_id VARCHAR(100) NOT NULL,
    skip_reason VARCHAR(50) NOT NULL,
    skip_reason_detail TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_task_skips_user ON ibpms_task_skips (user_id, created_at DESC);

-- Category tag en la proyección (para match de skills)
ALTER TABLE ibpms_workdesk_projection
    ADD COLUMN IF NOT EXISTS category_tag VARCHAR(100);
```

### 5.B — Backend: Endpoint Attend-Next (Firma)

```java
// WorkdeskAttendNextController.java
@RestController
@RequestMapping("/api/v1/workdesk")
public class WorkdeskAttendNextController {

    @PostMapping("/attend-next")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkdeskTaskDTO> attendNext(
            Authentication authentication) {
        // 1. Extraer tenantId + userId del JWT
        // 2. Verificar que el Feature Toggle "FORCE_ROUTING" esté ON para el tenant
        // 3. Obtener skills[] del usuario (US-036)
        // 4. SELECT ... FOR UPDATE SKIP LOCKED contra ibpms_workdesk_projection
        //    WHERE tenant_id = :t AND assignee IS NULL
        //    AND category_tag IN (:skills)
        //    ORDER BY impact_level DESC, sla_expiration_date ASC NULLS LAST
        //    LIMIT 1
        // 5. Si no hay match de skills → Fallback Universal (sin filtro de skill)
        //    + WARNING en Audit Log
        // 6. UPDATE assignee = userId → commit atómico
        // 7. Emitir WebSocket REMOVE al canal grupal (CA-27)
        // 8. Retornar DTO sanitizado (CA-14)
    }

    @PostMapping("/attend-next/skip")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkdeskTaskDTO> skipAndNext(
            @RequestBody @Valid SkipReasonDTO skipReason,
            Authentication authentication) {
        // 1. Persistir Skip en ibpms_task_skips (NFR-OBS-01)
        // 2. Verificar si el usuario acumula > 3 skips consecutivos
        //    → Si sí: emitir alerta al Supervisor (log + notificación)
        // 3. Ejecutar attendNext() con la siguiente tarea
    }
}
```

### 5.C — Backend: SkipReasonDTO

```java
public record SkipReasonDTO(
    @NotBlank String taskId,
    @NotBlank String skipReason, // enum: CLIENTE_NO_RESPONDE, REQUIERE_DOCUMENTACION, FUERA_DE_AREA, OTRO
    @Size(min = 10, message = "El detalle debe tener al menos 10 caracteres")
    String skipReasonDetail      // obligatorio solo si skipReason = OTRO
) {}
```

### 5.D — Backend: Repository (FOR UPDATE SKIP LOCKED)

```java
// En WorkdeskProjectionRepository.java
@Query(value = """
    SELECT * FROM ibpms_workdesk_projection w
    WHERE w.tenant_id = :tenantId
      AND w.assignee IS NULL
      AND (:skills IS NULL OR w.category_tag = ANY(CAST(:skills AS VARCHAR[])))
    ORDER BY w.impact_level DESC, w.sla_expiration_date ASC NULLS LAST
    LIMIT 1
    FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
Optional<WorkdeskProjectionEntity> findNextAvailableTask(
    @Param("tenantId") String tenantId,
    @Param("skills") String[] skills
);
```

### 5.E — Frontend: Modo "Atender Siguiente" (Estructura Vue)

```vue
<!-- Dentro de Workdesk.vue — sección condicional -->
<template>
  <div v-if="workdeskStore.forceRoutingEnabled" class="attend-next-mode">
    <div class="attend-next-hero">
      <p class="attend-next-label">Modo Enrutamiento Forzoso Activo</p>
      <button
        class="attend-next-cta"
        :disabled="workdeskStore.isAttending"
        @click="workdeskStore.attendNext()"
      >
        ⚡ Atender Siguiente Tarea
      </button>
    </div>
  </div>
  <div v-else>
    <!-- Grilla normal existente -->
  </div>
</template>
```

### 5.F — Frontend: Store Action

```typescript
// En useWorkdeskStore.ts
forceRoutingEnabled: false,

async checkForceRouting() {
  const { data } = await api.get('/api/v1/workdesk/feature-toggles/FORCE_ROUTING');
  this.forceRoutingEnabled = data.enabled;
},

async attendNext() {
  this.isAttending = true;
  try {
    const { data } = await api.post('/api/v1/workdesk/attend-next');
    // Navegar al formulario de la tarea asignada
    router.push(`/tasks/${data.id}`);
  } catch (err) {
    if (err.response?.status === 404) {
      // No hay tareas disponibles
      showToast('No hay tareas disponibles en este momento.');
    }
  } finally {
    this.isAttending = false;
  }
},

async skipAndNext(taskId: string, reason: string, detail?: string) {
  const { data } = await api.post('/api/v1/workdesk/attend-next/skip', {
    taskId, skipReason: reason, skipReasonDetail: detail
  });
  router.push(`/tasks/${data.id}`);
}
```

---

## 6. Matriz de QA y Testing Atómico

| # | Test Name | CA Evaluado | Aserción Esperada |
|---|-----------|-------------|-------------------|
| 1 | `should_assign_next_task_with_skill_match` | CA-16, CA-21 | La tarea retornada tiene `category_tag` dentro del array de skills del usuario |
| 2 | `should_fallback_universal_when_no_skill_match` | CA-21.3 | Si no hay match, asigna la tarea más antigua/crítica + genera WARNING en audit log |
| 3 | `should_use_for_update_skip_locked_atomicity` | CA-28 | Dos threads concurrentes obtienen tareas DISTINTAS (nunca la misma) |
| 4 | `should_block_attend_next_when_toggle_off` | CA-08 | Retorna 403 si el toggle FORCE_ROUTING está desactivado |
| 5 | `should_persist_skip_reason_in_audit_log` | CA-21.5, NFR-OBS-01 | Registro insertado en `ibpms_task_skips` con userId, taskId, skip_reason, timestamp |
| 6 | `should_alert_supervisor_after_3_consecutive_skips` | CA-21.5 | Tras 3 skips consecutivos del mismo usuario, se genera alerta |
| 7 | `should_reject_skip_other_without_detail` | CA-21.4 | Si skipReason="OTRO" y skipReasonDetail < 10 chars → HTTP 400 |
| 8 | `should_enforce_tenant_isolation_on_attend_next` | CA-14 heredado | El query SIEMPRE filtra por tenantId, nunca expone tareas de otro tenant |
| 9 | `should_emit_ws_remove_on_successful_assign` | CA-27 heredado | WebSocket emite `{action: 'REMOVE', taskId}` al canal grupal |
| 10 | `should_log_toggle_change_immutably` | CA-16, NFR-OBS-01 | Cada encendido/apagado del toggle queda en audit log con `changed_by` y `timestamp` |
| 11 | `should_return_next_task_on_race_condition` | CA-28 | Si la tarea fue tomada en la fracción de segundo, el operario recibe la SIGUIENTE tarea válida sin error |
| 12 | `should_order_by_impact_then_sla` | CA-16, CA-17 | El ordenamiento respeta `impact_level DESC, sla_expiration_date ASC NULLS LAST` |

**Framework de Tests:**
- **Backend:** JUnit 5 + Spring Boot Test + Testcontainers (PostgreSQL). Ruta: `backend/ibpms-core/src/test/java/com/ibpms/poc/`
- **Frontend:** Vitest + Vue Test Utils. Ruta: `frontend/src/tests/`

---

## 7. NFR-OBS-01: Trazabilidad Extrema (Audit Logging)

Todo bypass de usuario (Skip, Feature Toggle ON/OFF) DEBE generar un asiento inmutable en el sistema de auditoría:

| Evento | Campos Obligatorios |
|--------|--------------------|
| Toggle ON/OFF | `{tenantId, toggleKey, enabled, changedBy, changedAt}` |
| Skip de tarea | `{tenantId, userId, taskId, skipReason, skipReasonDetail?, createdAt}` |
| Asignación forzosa | `{tenantId, userId, taskId, assignedBy: 'SYSTEM_FORCE_ROUTING', assignedAt}` |
| Fallback Universal | `{tenantId, userId, taskId, warning: 'NO_SKILL_MATCH', assignedAt}` |
| Alerta 3+ Skips | `{tenantId, userId, consecutiveSkips, supervisorNotified, alertAt}` |

---

## 8. Mitigación de Riesgo E2E (Stress Testing)

> [!WARNING]
> **Riesgo Identificado:** 200 operarios presionando "Atender Siguiente" simultáneamente a las 8:00 AM (Thundering Herd).
> **Mitigación:** Al aislar esta funcionalidad al final de la US-001, se permiten pruebas de estrés puro (JMeter/K6) contra el endpoint `/attend-next`, validando que NO se corrompa el Thread-Pool del contenedor backend sin afectar la grilla básica ya construida y certificada.
> **Criterio de Aceptación del Stress Test:** 200 requests concurrentes al endpoint `/attend-next` → 200 tareas distintas asignadas, 0 duplicados, 0 errores 500.

---

## 9. Mensaje de Despacho (Comunicación al Agente Especialista)

### 🔧 Para el Agente Backend:
> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B. Rama: `sprint-3/informe_auditoriaSprint1y2`.

### 🎨 Para el Agente Frontend:
> **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`. Rama: `sprint-3/informe_auditoriaSprint1y2`.

### 🕵️ Para el Agente QA:
> **Validación obligatoria:** Ejecuta la doctrina de testing documentada en `.agents/skills/qa_e2e_validation_audit/SKILL.md`. Rama: `sprint-3/informe_auditoriaSprint1y2`. Foco: Unit Tests al Repository Data Layer con Testcontainers PostgreSQL. Validar perimetraje SQL (tenant isolation) y atomicidad `FOR UPDATE SKIP LOCKED`.

---

## 10. Post-Ejecución: Trazabilidad Coverage

Al finalizar la ejecución y push a la rama, TODOS los agentes DEBEN actualizar `.agentic-sync/coverage_matrix.md`:
- CA-08 → ✅ con Sprint 82-DEV y referencia a este handoff
- CA-16 → ✅ con Sprint 82-DEV y referencia a este handoff
- CA-21 → ✅ con Sprint 82-DEV y referencia a este handoff
- CA-28 → ✅ con Sprint 82-DEV y referencia a este handoff
