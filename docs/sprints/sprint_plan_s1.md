# Sprint 1 — DUAL TRACK: Core Thread (J-04) + Validación QA

> **Sprint:** 1  
> **Duración:** 4 días laborales  
> **Prerequisito:** Gate Sprint 0 aprobado (ACP + Docker + Smoke E2E)  
> **Objetivo:** Completar el Journey J-04 (flujo diario del Operario) y validar las US completadas existentes.  
> **Journeys:** J-04 (Build) + J-02/J-03 (Validate existentes)

---

## Modelo de Roles Activos

| Rol | Actor | Scope en Sprint 1 |
|-----|-------|--------------------|
| **Jefe de Equipo** | Harolt | Prueba UAT manual J-04, firma Gate Final |
| **Agente PO** | Agente IA | Valida CAs implementados vs especificación, emite Gate Funcional |
| **Arquitecto Líder SW** | Agente IA Lead | Descompone US, genera handoffs, audita cada entrega, firma Gate Técnico |
| **Agente Backend** | Agente IA ejecutor | Implementa endpoints, dominio, tests unitarios |
| **Agente Frontend** | Agente IA ejecutor | Implementa stores, vistas, integración API |
| **Agente QA** | Agente IA ejecutor | Valida US existentes, escribe tests E2E Playwright |

---

## Pre-Sprint (Día 0 — 2-3 horas)

### 👤 Jefe de Equipo
- [ ] Confirma que las US seleccionadas son las correctas para este sprint
- [ ] Confirma prioridad: J-04 es el journey que debe funcionar al final del sprint

### 📋 Agente PO
- [ ] Verifica que los CAs de US-001 (restantes), US-002 y US-029 están completos en Gherkin
- [ ] Identifica gaps funcionales en los CAs del sprint

### ★ Arquitecto Lead
- [ ] Descompone US en tasks técnicos y genera sprint_plan_s1.json
- [ ] Genera handoffs estructurados para cada agente
- [ ] Define orden de ejecución y dependencias

---

## TRACK A — BUILD (Días 1-3)

### Objetivo
Construir los CAs pendientes del Journey J-04: **Workdesk → Reclamar Tarea → Formulario → CQRS**

### US en Scope (BUILD)

#### US-001: Bandeja de Entrada Unificada — CAs Pendientes (15 CAs)

> **Estado actual:** 15/30 CAs construidos (50%). Los 15 CAs restantes se priorizan por dominio funcional para este sprint.

**Grupo 1 — WebSocket / Tiempo Real (Alta prioridad para J-04)**

| CA | Título | Prioridad | Dependencias |
|----|--------|-----------|--------------|
| CA-6 | Ghost Deletion STOMP WebSocket | MUST | Requiere infraestructura WebSocket base |
| CA-13 | Minificación WebSocket Throttling | SHOULD | Depende de CA-6 |
| CA-26 | Relleno Automático Post-WebSocket | SHOULD | Depende de CA-6 |
| CA-27 | Vocabulario Completo WebSocket | SHOULD | Depende de CA-6 |

**Handoff Backend (T-S1-01):**
```
Implementar el módulo STOMP WebSocket completo:
- Broker: /topic/workdesk/{tenantId}
- Mensaje TASK_CLAIMED: { taskId, claimedBy, timestamp }
- Mensaje TASK_COMPLETED: { taskId, timestamp }
- Spring @MessageMapping + SimpMessagingTemplate
- Tests: WebSocketIntegrationTest con STOMP client
```

**Handoff Frontend (T-S1-02):**
```
Integrar store useWorkdeskRealtime:
- Conectar SockJS + stompjs al broker
- onTaskClaimed: eliminar tarea de la grilla (ghost deletion)
- onTaskCompleted: eliminar tarea y actualizar contadores
- Throttle visual (debounce 300ms para batch WebSocket)
- Tests: mock WebSocket con vi.fn()
```

**Grupo 2 — SLA / Semáforos (Alta prioridad para J-04)**

| CA | Título | Prioridad | Dependencias |
|----|--------|-----------|--------------|
| CA-5 | SLA Ticking Engine Vivo | MUST | Core de J-04 (semáforo urgente) |
| CA-11 | Heartbeat Store rAF | SHOULD | Depende de CA-5 |
| CA-24 | Umbrales Semáforo SLA Configurables | SHOULD | Depende de CA-5 + US-043 |
| CA-25 | Recálculo Semáforos Tab Inactiva | SHOULD | Depende de CA-5 |
| CA-31 | Auto-Refresco Pasivo Inactividad | COULD | Baja prioridad |

**Handoff Backend (T-S1-03):**
```
Endpoint SLA Configuration:
- GET /api/v1/sla/thresholds → umbrales por tenant (referencia US-043)
- El cálculo SLA es client-side (el Backend provee deadlines, el Frontend calcula countdown)
- Tests: SlaThresholdControllerTest
```

**Handoff Frontend (T-S1-04):**
```
SLA Ticking Engine:
- Store useSlaEngine con requestAnimationFrame
- Cálculo: timeRemaining = task.slaDeadline - Date.now()
- Semáforo: verde (>50%), amarillo (25-50%), rojo (<25%)
- Refresco al cambiar de tab (visibilitychange API)
- Heartbeat para recálculo periódico (1 min)
```

**Grupo 3 — Delegación / RBAC (Necesario para J-04)**

| CA | Título | Prioridad | Dependencias |
|----|--------|-----------|--------------|
| CA-4 | Toggle Delegación Mis Tareas/Equipo | MUST | Core UX del Workdesk |
| CA-15 | Delegación Segura Anti-IDOR | MUST | Seguridad |

**Handoff Backend (T-S1-05):**
```
Endpoint de Delegación:
- GET /api/v1/workdesk/tasks?scope=MY|TEAM
- IDOR protection: Spring @PreAuthorize("isAssigneeOrTeamLead(#taskId)")
- Tests: DelegationSecurityTest con mock JWT roles
```

**Handoff Frontend (T-S1-06):**
```
Toggle UI "Mis Tareas" / "Equipo":
- Switch/Toggle-button en header del Workdesk
- Filtro reactivo en useWorkdeskStore (query param scope)
- UX: cuando scope=TEAM, mostrar columna "Asignado a"
```

**Grupo 4 — Routing / Anti-Abuse (Hardening)**

| CA | Título | Prioridad | Dependencias |
|----|--------|-----------|--------------|
| CA-8 | Anti-Cherry Picking Feature Flag | SHOULD | Requiere Feature Flag infra |
| CA-16 | Skill-Based Routing | COULD | Complejidad alta, puede posponerse |
| CA-21 | Skill-Based Skipeo Justificado | COULD | Depende de CA-16 |
| CA-28 | Prevención Race Condition Atender | MUST | Redis lock (US-034 depende) |

**Handoff Backend (T-S1-07):**
```
Anti-Cherry-Picking:
- Feature flag: workdesk.anti-cherry-picking.enabled (application.yml)
- Si activo: las tareas se asignan por FIFO, no por selección del usuario
- Race Condition: Redis SETNX lock de 5s antes de claim
- Tests: ConcurrencyClaimTest con CountDownLatch
```

---

#### US-002: Task Claiming (Reclamar Tarea) — NUEVA

> **Estado actual:** 0% — No tiene CAs implementados. Es BLOQUEANTE para J-04 (paso 3-4).

| CA | Título | Prioridad |
|----|--------|-----------|
| CA-1 | Reclamar tarea individual de cola | MUST |
| CA-2 | Liberar tarea reclamada | MUST |
| CA-3 | Validar que solo el asignado puede completar | MUST |
| CA-4 | Notificación WebSocket a compañeros (ghost) | MUST |
| CA-5-12 | CAs restantes según v1_user_stories.md | SHOULD |

**Handoff Backend (T-S1-08):**
```
Endpoints Task Claiming:
- POST /api/v1/tasks/{taskId}/claim → asigna task.assigneeId = currentUser
- POST /api/v1/tasks/{taskId}/unclaim → libera (solo owner)
- Validación: tarea debe estar en estado PENDING
- Redis lock para evitar doble claim
- WebSocket publish al topic /workdesk/{tenantId}: TASK_CLAIMED
- Tests: TaskClaimControllerTest, TaskClaimConcurrencyTest
```

**Handoff Frontend (T-S1-09):**
```
Botón "Atender" en card/row del Workdesk:
- Click → POST /claim → optimistic update (quitar de lista)
- WebSocket listener: si otra instancia reclama → quitar de mi vista
- Botón "Liberar" visible solo para el owner
- Loading state + error handling (409 si ya reclamada)
```

---

#### US-029: Ejecución de Formulario (Refactored → US-017)

> **Nota:** US-029 fue refactorizada. Los CAs de ejecución de formulario ahora viven en US-017 (CQRS). Para Sprint 1 nos enfocamos en los CAs de UI de formulario que NO son CQRS.

**Decisión:** Posponer US-017 (CQRS completo) al Sprint 2. En Sprint 1 implementar solo la UI de formulario necesaria para J-04:
- Abrir formulario desde el Workdesk (navegación)
- Renderizar iForm ya creado (US-003 ya construida)
- Enviar datos (POST simple, sin Event Sourcing aún)

---

### Orden de Ejecución TRACK A

```
Día 1:
  Backend(US-001 WebSocket CA-6) → ★Lead audita
  Backend(US-002 Claiming CA-1..4) → ★Lead audita

Día 2:
  Frontend(US-001 WebSocket CA-6,13) → ★Lead audita
  Frontend(US-002 Claiming UI) → ★Lead audita
  Backend(US-001 SLA CA-5,24) → ★Lead audita

Día 3:
  Frontend(US-001 SLA CA-5,11,24,25) → ★Lead audita
  Backend(US-001 Delegación CA-4,15) → ★Lead audita
  Frontend(US-001 Delegación CA-4,15) → ★Lead audita
  Backend(US-001 Anti-Abuse CA-28) → ★Lead audita
```

---

## TRACK B — VALIDATE (Días 1-3)

### Objetivo
Validar las US ya construidas que tienen QA al 0%. El QA Agent escribe tests E2E Playwright y valida funcionalidad.

### US a Validar

| US | Estado Actual | QA Actual | Acción Sprint 1 |
|----|---------------|-----------|-----------------|
| US-005 | ✅ Completada (97%) | CA-12 ✅ (1/70) | Validar 10 CAs core + cerrar OBS-1 y OBS-2 |
| US-003 | ✅ Completada | ❌ 0% | Validar 10 CAs core del IDE de formularios |
| US-036 | ✅ Completada | ✅ Parcial | Validar CAs de frontend (CA-6, CA-22, CA-24) |
| US-038 | ✅ Completada | ❌ 0% | Validar integración multi-rol + EntraID |
| US-048 | ✅ Completada | ❌ 0% | Validar IdP interno |

### Handoff QA (T-S1-10)

```
Validación Funcional:
1. US-005: Abrir modeler BPMN → crear proceso → guardar → verificar persistencia
2. US-003: Abrir IDE formularios → arrastar campo → Zod schema → preview
3. US-036: Login con rol limitado → verificar que sidebar filtra → intentar URL prohibida → 404
4. US-038: Crear usuario → asignar multi-rol → verificar permisos heredados
5. US-048: Crear usuario interno → reset password → login exitoso

Tests E2E Playwright:
- e2e/j02/bpmn-modeler-basic.spec.ts (US-005)
- e2e/j02/form-builder-basic.spec.ts (US-003)
- e2e/j03/rbac-access-control.spec.ts (US-036)
```

### ★ Arquitecto Lead — Checkpoints TRACK B

- [ ] Revisar calidad de scripts E2E (selectores, assertions)
- [ ] Validar que tests cubren los CAs especificados
- [ ] Clasificar cada US: ✅ Funcional / 🔧 Parcial / 💀 Fantasma
- [ ] Si US es fantasma → crear ticket de remediación para Sprint 2

---

## Día 4 — Integración y Gate

### ★ Arquitecto Lead — Gate Técnico

| # | Criterio | Meta | Estado |
|---|----------|------|--------|
| 1 | CAs US-001 construidos en sprint | ≥10/15 restantes | ⬜ |
| 2 | US-002 implementada (claim/unclaim) | ≥4 CAs core | ⬜ |
| 3 | WebSocket funcional en Workdesk | Ghost deletion visible | ⬜ |
| 4 | SLA Ticking Engine activo | Semáforo se mueve en real-time | ⬜ |
| 5 | Tests E2E Sprint 1 | ≥5 specs verdes | ⬜ |
| 6 | US validadas TRACK B | ≥3/5 clasificadas | ⬜ |
| 7 | Coverage matrix actualizada | Todos los CAs nuevos registrados | ⬜ |

### 📋 Agente PO — Gate Funcional

| # | Criterio | Meta | Estado |
|---|----------|------|--------|
| 1 | CAs implementados coinciden con especificación | Sin gaps funcionales | ⬜ |
| 2 | UAT J-04 escenarios cubiertos | ≥8/12 escenarios passing | ⬜ |
| 3 | US validadas sin hallazgos fantasma | ≤1 US fantasma | ⬜ |

### 👤 Jefe de Equipo — Gate Final

| # | Criterio | Meta | Estado |
|---|----------|------|--------|
| 1 | Demo J-04 funcional | Operario puede ver tarea → reclamar → (abrir form) | ⬜ |
| 2 | UAT manual J-04 | Flujo completo sin errores bloqueantes | ⬜ |
| 3 | Confianza para Sprint 2 | Equipo entrega a velocidad esperada | ⬜ |

### Definición de Listo

```
GATE S1 = Gate Técnico (≥10 CAs + WebSocket + SLA + ≥5 E2E)
         ∧ Gate Funcional (UAT J-04 ≥8/12 + ≤1 fantasma)
         ∧ Gate Final (Demo J-04 + UAT manual OK)
```

---

## Riesgos y Mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|:---:|:---:|------------|
| WebSocket STOMP no funciona con proxy Docker | Media | Alto | Fallback: polling con setInterval(30s) |
| US-002 tiene más CAs de los estimados | Baja | Medio | Limitar a CA-1..4 core + posponer extras |
| QA TRACK B descubre US fantasma | Media | Alto | Registrar en sprint report + remediar en S2 |
| Race condition en claim no se resuelve | Baja | Alto | Redis SETNX con TTL 5s |

---

## Historial de Cambios

| Fecha | Cambio | Autor |
|-------|--------|-------|
| 2026-04-10 | Creación del Sprint 1 plan | Arquitecto Lead |
