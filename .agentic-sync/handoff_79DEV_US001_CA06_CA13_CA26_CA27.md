# 📋 Handoff Arquitectónico — Iteración 79-DEV (US-001)
> **Fecha:** 2026-04-13 | **Arquitecto Líder:** Agente Antigravity  
> **Rama:** `sprint-3/informe_auditoriaSprint1y2`  
> **Protocolo aplicado:** `.agents/skills/architect_handoff_protocol/SKILL.md` v1.0.0

---

## 1. Metadatos y SSOT (Single Source of Truth)

| Campo | Valor |
|-------|-------|
| **Iteración** | 79-DEV |
| **Sprint** | Sprint 3 |
| **Rama Git** | `sprint-3/informe_auditoriaSprint1y2` |
| **User Story** | US-001 — Obtener Tareas Pendientes en el Workdesk |
| **Archivo SSOT** | `docs/requirements/epics/epic_A_motor_core.md` (líneas 91–315) |
| **CAs a desarrollar** | CA-06, CA-13, CA-26, CA-27 |
| **CAs excluidos (V2)** | Niveles de experticia en Skills (CA-21 nota V2), comportamientos de Heartbeat (CA-11), SLA Ticking Engine (CA-5) |
| **Flujo de ejecución** | **Secuencial estricto:** Backend → Frontend → QA |
| **Razón de secuencialidad** | El Frontend (CA-13, CA-26) depende del contrato DTO WebSocket emitido por el Backend (CA-06, CA-27). QA certifica ambas capas. |

---

## 2. Alineación Arquitectónica y ADRs

### ADRs que rigen esta iteración

| ADR / Restricción | Impacto en 79-DEV |
|--------------------|--------------------|
| **Tenant Isolation (CA-14)** | Los canales WebSocket STOMP deben estar segregados por tenant: `/topic/workdesk/{tenantId}`. Ningún tenant debe recibir eventos de otro. |
| **CQRS Read Model** | La tabla `ibpms_workdesk_projection` es la fuente de lectura. Los eventos WS se disparan en la capa de escritura (listeners), no en la capa de lectura (controller). |
| **Payload Sanitizado (CA-14, CA-20)** | Los payloads WebSocket DEBEN respetar el mismo DTO sanitizado del CA-20 (`WorkdeskGlobalItemDTO`). Prohibido exponer campos internos de Camunda en el payload WS. |
| **Zero-Trust SRE** | Compilación Backend vía `.agents/skills/backend_sre_compilation_audit/SKILL.md`. Prohibido usar `mvn clean install` como atajo. |
| **Zero-Trust UI** | Build Frontend vía `.agents/skills/frontend_build_audit/SKILL.md`. Prohibido usar `npm run build` como atajo. |
| **Grep Search Governance (RGL-001)** | Búsquedas de código deben seguir `.agents/skills/grep_search_governance/SKILL.md`. |

### Trazabilidad de la solución

El diseño de esta iteración respeta el modelo CQRS existente: los `TaskListener` de Camunda (`CamundaTaskSyncListener`) y los listeners de Kanban (`KanbanTaskSyncListener`) ya inyectan `SimpMessagingTemplate` para emitir eventos WS. La configuración del broker STOMP ya existe en `WebSocketConfig.java` con el simple broker `/topic` y endpoint `/ws-endpoint` con SockJS. El Frontend ya tiene `@stomp/stompjs` importado y una suscripción inicial en `useWorkdeskStore.ts`. **Esta iteración extiende lo existente sin crear infraestructura nueva desde cero.**

---

## 3. Rutas Exactas y Contexto Preexistente

### 3.1 Backend — Archivos a modificar

| Archivo | Ruta relativa | Estado actual |
|---------|---------------|---------------|
| **WebSocketConfig.java** | `backend/.../infrastructure/config/WebSocketConfig.java` | ✅ Existe. Simple broker en `/topic`, endpoint `/ws-endpoint` con SockJS. No tiene segregación por tenant. |
| **CamundaTaskSyncListener.java** | `backend/.../infrastructure/event/CamundaTaskSyncListener.java` | ✅ Existe (84 líneas). Ya inyecta `SimpMessagingTemplate`. Solo emite `TASK_CLAIMED` en evento `assignment`. **Falta:** vocabulario completo (REMOVE, ADD, UPDATE, PRIORITY_CHANGE) y segregación por tenantId. |
| **KanbanTaskSyncListener.java** | `backend/.../infrastructure/event/KanbanTaskSyncListener.java` | ✅ Existe. Análogo al Camunda listener. Debe recibir mismo tratamiento de vocabulario WS. |
| **WorkdeskProjectionEntity.java** | `backend/.../infrastructure/jpa/entity/WorkdeskProjectionEntity.java` | ✅ Existe (72 líneas). Campo `tenantId` presente (línea 55). No requiere cambios. |
| **WorkdeskProjectionRepository.java** | `backend/.../infrastructure/jpa/repository/WorkdeskProjectionRepository.java` | ✅ Existe. Query nativa con `tenant_id = :tenantId`. No requiere cambios. |
| **[NUEVO] WsWorkdeskEventDTO.java** | `backend/.../application/dto/WsWorkdeskEventDTO.java` | ❌ No existe. Debe crearse. |

### 3.2 Frontend — Archivos a modificar

| Archivo | Ruta relativa | Estado actual |
|---------|---------------|---------------|
| **useWorkdeskStore.ts** | `frontend/src/stores/useWorkdeskStore.ts` | ✅ Existe (147 líneas). Ya tiene `initWebSocket()` con suscripción a `/topic/workdesk.updates`. **Problemas actuales:** (1) Solo procesa `TASK_CLAIMED`; (2) No tiene throttling/debounce; (3) No tiene auto-relleno; (4) No segrega por tenant; (5) Elimina DOM inmediatamente sin animación CSS. |
| **apiClient.ts** | `frontend/src/services/apiClient.ts` | ✅ Existe (214 líneas). Interceptor 429 ya presente. No requiere cambios para esta iteración. |

### 3.3 QA — Archivos a modificar/crear

| Archivo | Ruta relativa | Estado actual |
|---------|---------------|---------------|
| **WorkdeskRepositoryTest.java** | `backend/.../test/.../repository/WorkdeskRepositoryTest.java` | ✅ Existe (268 líneas, 6 tests). Incluye test de tenant isolation (TEST 1), NULLS LAST (TEST 2), ILIKE search (TEST 3), progressPercent (TESTes 4-5), facets (TEST 6). **Falta:** test de perimetraje WS por tenant. |

---

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

### 4.1 Backend — DTO WebSocket (CA-27): `WsWorkdeskEventDTO.java` [NUEVO]

```java
package com.ibpms.poc.application.dto;

/**
 * CA-27: Vocabulario estandarizado de acciones WebSocket para el Workdesk.
 * Payload atómico (CA-13) — solo instrucción + ID + payload parcial opcional.
 */
public class WsWorkdeskEventDTO {

    public enum Action {
        REMOVE,            // Tarea reclamada o reasignada
        ADD,               // Nueva tarea asignada al usuario
        UPDATE,            // Campo visible cambió (status, SLA, etc.)
        PRIORITY_CHANGE    // Reordenamiento por impacto financiero
    }

    private Action action;
    private String taskId;
    private String tenantId;
    private WorkdeskGlobalItemDTO payload; // Parcial, solo para ADD y UPDATE

    // Getters y Setters (Lombok @Getter @Setter aceptado)
    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public WorkdeskGlobalItemDTO getPayload() { return payload; }
    public void setPayload(WorkdeskGlobalItemDTO payload) { this.payload = payload; }
}
```

### 4.2 Backend — Modificación de `CamundaTaskSyncListener.java` (CA-06, CA-27)

**Líneas 74–77 actuales** (solo emite `TASK_CLAIMED` a un topic genérico):
```java
if ("assignment".equals(eventName) && delegateTask.getAssignee() != null) {
    String payload = "{\"event\": \"TASK_CLAIMED\", \"taskId\": \"BPMN-" + taskId + "\"}";
    messagingTemplate.convertAndSend("/topic/workdesk.updates", payload);
}
```

**Debe reemplazarse por** (vocabulario completo + segregación tenant):
```java
// CA-06 + CA-27: Emisión de evento con vocabulario estandarizado y segregación por Tenant
String tenantId = (String) delegateTask.getVariable("tenantId");
if (tenantId == null) tenantId = "default";

WsWorkdeskEventDTO wsEvent = new WsWorkdeskEventDTO();
wsEvent.setTaskId("BPMN-" + taskId);
wsEvent.setTenantId(tenantId);

if ("assignment".equals(eventName) && delegateTask.getAssignee() != null) {
    wsEvent.setAction(WsWorkdeskEventDTO.Action.REMOVE); // CA-06: Ghost deletion para otros
    messagingTemplate.convertAndSend("/topic/workdesk/" + tenantId, wsEvent);
} else if (EVENTNAME_CREATE.equals(eventName)) {
    wsEvent.setAction(WsWorkdeskEventDTO.Action.ADD);
    // Payload parcial con campos sanitizados del CA-20
    messagingTemplate.convertAndSend("/topic/workdesk/" + tenantId, wsEvent);
} else if (EVENTNAME_UPDATE.equals(eventName)) {
    wsEvent.setAction(WsWorkdeskEventDTO.Action.UPDATE);
    messagingTemplate.convertAndSend("/topic/workdesk/" + tenantId, wsEvent);
}
```

### 4.3 Frontend — Modificación de `useWorkdeskStore.ts` (CA-06, CA-13, CA-26, CA-27)

**A. Suscripción con segregación por tenant (CA-27):**  
Reemplazar línea 116 `'/topic/workdesk.updates'` por:
```typescript
// CA-27: Suscripción segregada por Tenant
const tenantId = useAuthStore().tenantId || 'default';
this.stompClient?.subscribe(`/topic/workdesk/${tenantId}`, (message) => {
```

**B. Handler con vocabulario completo (CA-27) dentro del callback de suscripción:**
```typescript
const event = JSON.parse(message.body);
switch (event.action) {
    case 'REMOVE':
        this._handleWsRemove(event.taskId);
        break;
    case 'ADD':
        if (event.payload) this._handleWsAdd(event.payload);
        break;
    case 'UPDATE':
        if (event.payload) this._handleWsUpdate(event.taskId, event.payload);
        break;
    case 'PRIORITY_CHANGE':
        this._handleWsPriorityChange();
        break;
}
```

**C. Throttle/Debounce y Ghost Deletion con animación CSS (CA-13):**
```typescript
// Estado adicional en state():
_pendingRemovals: [] as string[],
_removalTimer: null as ReturnType<typeof setTimeout> | null,

// Acción privada:
_handleWsRemove(taskId: string) {
    // CA-13: Acumular remociones en ventana de 2 segundos
    this._pendingRemovals.push(taskId);
    
    if (this._removalTimer) return; // Timer ya activo
    
    this._removalTimer = setTimeout(() => {
        const idsToRemove = [...this._pendingRemovals];
        this._pendingRemovals = [];
        this._removalTimer = null;
        
        // CA-13: Aplicar opacity:0 con transición CSS (no eliminar de golpe)
        idsToRemove.forEach(id => {
            const idx = this.items.findIndex(i => i.unifiedId === id || i.originalTaskId === id);
            if (idx !== -1) {
                (this.items[idx] as any)._isGhost = true; // Flag para CSS transition
            }
        });
        
        // Remover del array 800ms después (duración de la animación CSS)
        setTimeout(() => {
            this.items = this.items.filter(i => !(i as any)._isGhost);
            this._checkAutoRefill(); // CA-26
        }, 800);
    }, 2000); // CA-13: Ventana de throttling de 2 segundos
},
```

**D. Auto-relleno post-remoción (CA-26):**
```typescript
_refillDebounce: null as ReturnType<typeof setTimeout> | null,

async _checkAutoRefill() {
    // CA-26: Si la página tiene menos de 15 tarjetas, pedir relleno silencioso
    if (this.items.length < 15 && this.pageInfo.totalElements > this.items.length) {
        // Debounce 5s para acumular múltiples remociones
        if (this._refillDebounce) clearTimeout(this._refillDebounce);
        this._refillDebounce = setTimeout(async () => {
            await this.fetchGlobalInbox(this.currentPage, 15); // Petición silenciosa
            this._refillDebounce = null;
        }, 5000);
    }
    
    // CA-12: Si la página queda completamente vacía → ir a Página 1
    if (this.items.length === 0 && this.currentPage > 0) {
        await this.fetchGlobalInbox(0, 15);
    }
},
```

**E. CSS complementario (en el componente Workdesk.vue o scope CSS):**
```css
/* CA-13: Transición Ghost Deletion */
.workdesk-row {
    transition: opacity 0.8s ease-out, transform 0.8s ease-out;
}
.workdesk-row.is-ghost {
    opacity: 0;
    transform: translateX(-20px);
    pointer-events: none;
}
/* CA-26: Fade-in para tarjetas nuevas */
.workdesk-row.is-new {
    animation: fadeIn 0.5s ease-in;
}
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}
```

---

## 5. Matriz de QA y Testing Atómico

### Tests del Repository Data Layer (JUnit / @SpringBootTest)

**Archivo destino:** `backend/.../test/.../repository/WorkdeskRepositoryTest.java` (extender el existente)

| # | Test Name | CA Evaluado | Aserción Esperada |
|---|-----------|-------------|-------------------|
| 7 | `testWsEventEmission_TenantIsolation` | CA-06, CA-27 | Mock de `SimpMessagingTemplate`. Verificar que al asignar una tarea de `tenantA`, el mensaje se envía a `/topic/workdesk/tenantA` y NO a `/topic/workdesk/tenantB`. |
| 8 | `testWsPayload_VocabularyActions` | CA-27 | Verificar que el `WsWorkdeskEventDTO` serializado contiene `action: "REMOVE"` al asignar tarea, `action: "ADD"` al crear tarea, `action: "UPDATE"` al modificar tarea. |
| 9 | `testTenantPerimeter_CrossTenantBlock` | CA-06 | Insertar 2 tareas (tenantA, tenantB). Invocar el listener con tenantA. Confirmar que el `/topic/workdesk/tenantB` NUNCA recibió invocación alguna en el mock. |

### Tests del Frontend Store (Vitest / Mock STOMP)

**Archivo destino:** `frontend/src/tests/useWorkdeskStore.spec.ts` (crear o extender)

| # | Test Name | CA Evaluado | Aserción Esperada |
|---|-----------|-------------|-------------------|
| 1 | `throttles_100_removes_into_single_batch` | CA-13 | Disparar 100 eventos `REMOVE` en 100ms. Verificar que solo se ejecuta 1 petición de refill y no 100 mutaciones individuales al DOM. |
| 2 | `ghost_deletion_sets_opacity_flag` | CA-13 | Disparar evento `REMOVE`. Verificar que el item recibe `_isGhost = true` y se elimina del array después de 800ms. |
| 3 | `auto_refill_fires_when_below_15` | CA-26 | Mockear un store con 14 items. Disparar 1 `REMOVE`. Verificar que `fetchGlobalInbox` se invoca con page actual. |
| 4 | `redirect_to_page1_when_empty` | CA-26, CA-12 | Mockear un store con 1 item en `currentPage: 3`. Disparar 1 `REMOVE`. Verificar que `fetchGlobalInbox(0, 15)` se invoca. |
| 5 | `handles_all_vocabulary_actions` | CA-27 | Disparar 4 eventos con actions `REMOVE`, `ADD`, `UPDATE`, `PRIORITY_CHANGE`. Verificar que cada handler interno se invoca correctamente. |

---

## 6. Mensajes de Despacho (Copy & Paste para el Humano)

### 🔄 Orden de ejecución: SECUENCIAL ESTRICTO
1. **Primero:** Backend
2. **Segundo:** Frontend (tras commit del Backend)
3. **Tercero:** QA (tras commit del Frontend)

---

### 1️⃣ Para el Agente Backend:

```text
Actúa como Agente Backend.
Iniciamos la Iteración 79-DEV de la US-001 en la rama `sprint-3/informe_auditoriaSprint1y2`.

OBJETIVO: Extender la infraestructura STOMP WebSocket existente para soportar el vocabulario completo de eventos y la segregación por tenant.

ALCANCE (CAs):
- CA-06: Ghost Deletion — emitir evento REMOVE cuando una tarea es reclamada/reasignada.
- CA-27: Vocabulario estándar — implementar las 4 acciones: REMOVE, ADD, UPDATE, PRIORITY_CHANGE.
- Exclusiones V2: Niveles de experticia en Skills (CA-21 nota V2).

CONTEXTO PREEXISTENTE (lee estos archivos ANTES de codificar):
- `backend/.../infrastructure/config/WebSocketConfig.java` — Ya existe el broker `/topic` y endpoint `/ws-endpoint`.
- `backend/.../infrastructure/event/CamundaTaskSyncListener.java` — Ya inyecta SimpMessagingTemplate. Solo emite TASK_CLAIMED en líneas 74-77. DEBES extender con el vocabulario completo.
- `backend/.../infrastructure/event/KanbanTaskSyncListener.java` — Análogo. Debe recibir mismo tratamiento.

TAREAS PRESCRIPTIVAS:
1. CREAR `backend/.../application/dto/WsWorkdeskEventDTO.java` con Enum Action (REMOVE, ADD, UPDATE, PRIORITY_CHANGE), taskId, tenantId, payload opcional de tipo WorkdeskGlobalItemDTO.
2. MODIFICAR `CamundaTaskSyncListener.java` líneas 74-77: reemplazar el String JSON crudo por WsWorkdeskEventDTO. Segregar el canal a `/topic/workdesk/{tenantId}`. Emitir REMOVE en assignment, ADD en create, UPDATE en update.
3. MODIFICAR `KanbanTaskSyncListener.java`: aplicar idéntico patrón de emisión WS.
4. NO modificar WebSocketConfig.java (ya soporta prefijo /topic genérico).

Los snippets exactos están en `.agentic-sync/handoff_79DEV_US001_CA06_CA13_CA26_CA27.md` sección 4.1 y 4.2.

Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en .agents/skills/backend_sre_compilation_audit/SKILL.md (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.
Actualiza `.agentic-sync/coverage_matrix.md` marcando CA-06 y CA-27 en columna Back como ✅.
Haz commit y push en la rama `sprint-3/informe_auditoriaSprint1y2`.
```

---

### 2️⃣ Para el Agente Frontend (solo tras commit del Backend):

```text
Actúa como Agente Frontend.
Continuamos la Iteración 79-DEV de la US-001 en la rama `sprint-3/informe_auditoriaSprint1y2`.

OBJETIVO: Consumo reactivo del vocabulario STOMP completo, throttling visual DOM y auto-relleno preventivo de la grilla.

ALCANCE (CAs):
- CA-13: Throttling/Debounce de 2s para agrupar remociones. Animación CSS `opacity:0` (NO desaparición de golpe). Toast discreto.
- CA-26: Relleno automático — si los items bajan de 15 tras remociones WS, petición silenciosa con debounce de 5s. Si página queda vacía, redirigir a Página 1 (CA-12).
- CA-27: Procesar las 4 acciones del vocabulario (REMOVE, ADD, UPDATE, PRIORITY_CHANGE).
- Exclusiones V2: ninguna aplica a Frontend en esta iteración.

CONTEXTO PREEXISTENTE (lee estos archivos ANTES de codificar):
- `frontend/src/stores/useWorkdeskStore.ts` — Ya tiene `initWebSocket()` con STOMP client (líneas 96-137). Solo procesa TASK_CLAIMED. DEBES extender.
- `frontend/src/services/apiClient.ts` — No requiere cambios. Interceptor 429 ya presente.

TAREAS PRESCRIPTIVAS:
1. MODIFICAR `useWorkdeskStore.ts` línea 116: cambiar suscripción de `/topic/workdesk.updates` a `/topic/workdesk/${tenantId}` usando `useAuthStore().tenantId`.
2. REEMPLAZAR el handler de líneas 117-127 por un switch/case que procese REMOVE, ADD, UPDATE, PRIORITY_CHANGE.
3. AGREGAR al state: `_pendingRemovals: []`, `_removalTimer: null`, `_refillDebounce: null`.
4. IMPLEMENTAR `_handleWsRemove(taskId)`: acumular en ventana de 2s, aplicar flag `_isGhost`, eliminar del array tras 800ms de animación CSS.
5. IMPLEMENTAR `_checkAutoRefill()`: si items < 15 y hay más en el servidor, emitir 1 sola petición tras debounce de 5s. Si items === 0 y currentPage > 0, ir a página 0.
6. AGREGAR CSS en el componente Workdesk: `.workdesk-row` con transition opacity 0.8s, clase `.is-ghost` con opacity:0, clase `.is-new` con keyframe fadeIn.

Los snippets exactos están en `.agentic-sync/handoff_79DEV_US001_CA06_CA13_CA26_CA27.md` sección 4.3, 4.4 y 4.5.

Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en .agents/skills/frontend_build_audit/SKILL.md.
Actualiza `.agentic-sync/coverage_matrix.md` marcando CA-13 y CA-26 en columna Front como ✅, y CA-06/CA-27 en columna Front como ✅.
Haz commit y push en la rama `sprint-3/informe_auditoriaSprint1y2`.
```

---

### 3️⃣ Para el Agente QA (solo tras commit del Frontend):

```text
Actúa como Agente QA Especialista.
Cerramos la Iteración 79-DEV de la US-001 en la rama `sprint-3/informe_auditoriaSprint1y2`.

OBJETIVO: Certificar el circuito WebSocket con pruebas unitarias al Repository Data Layer y perimetraje SQL.

NFR / QA STRATEGY: Pruebas unitarias al Repository Data Layer, asegurando perimetraje en consultas SQL.

CONTEXTO PREEXISTENTE:
- `backend/.../test/.../repository/WorkdeskRepositoryTest.java` — Ya tiene 6 tests (tenant isolation, NULLS LAST, ILIKE, progressPercent, facets). DEBES extender.

TAREAS PRESCRIPTIVAS (Backend / JUnit):
1. AGREGAR TEST 7 `testWsEventEmission_TenantIsolation`: Mockear SimpMessagingTemplate. Verificar que al asignar tarea de tenantA, el mensaje va a `/topic/workdesk/tenantA` y NUNCA a `/topic/workdesk/tenantB`.
2. AGREGAR TEST 8 `testWsPayload_VocabularyActions`: Verificar que WsWorkdeskEventDTO serializado contiene action correcta según el evento (assignment→REMOVE, create→ADD, update→UPDATE).
3. AGREGAR TEST 9 `testTenantPerimeter_CrossTenantBlock`: Insertar tareas de 2 tenants, disparar listener con tenantA, confirmar que tenantB nunca recibió invocación.

TAREAS PRESCRIPTIVAS (Frontend / Vitest):
4. CREAR o EXTENDER `frontend/src/tests/useWorkdeskStore.spec.ts`:
   - Test: 100 REMOVEs en 100ms → solo 1 batch de mutación (CA-13 throttle).
   - Test: REMOVE aplica _isGhost=true y limpia en 800ms (CA-13 animación).
   - Test: items < 15 tras REMOVE → fetchGlobalInbox se invoca (CA-26 refill).
   - Test: items === 0 en page > 0 → redirect a page 0 (CA-26 + CA-12).
   - Test: 4 acciones del vocabulario disparan handler correcto (CA-27).

La matriz completa de test-a-CA está en `.agentic-sync/handoff_79DEV_US001_CA06_CA13_CA26_CA27.md` sección 5.

Audita `.agentic-sync/coverage_matrix.md` y verifica que CA-06, CA-13, CA-26 y CA-27 estén marcados ✅ en Back, Front y QA.
Haz commit cerrando la iteración en la rama `sprint-3/informe_auditoriaSprint1y2`.
```
