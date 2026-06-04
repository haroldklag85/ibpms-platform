# 🎨 Handoff Frontend — US-002 (Claim/Unclaim de Tareas)

> **Sprint:** PM-01 | **Slot:** 1 | **Cadena:** 2 (Core Workdesk)
> **Rama de trabajo:** `sprint-8/pm-01/us-002-claim`
> **Emisor:** Arquitecto Líder | **Fecha:** 2026-06-03
> **Destinatario:** Agente Frontend
> **Prerrequisito:** Handoff Backend US-002 completado y compilado ✅

---

## Pre-Handoff Checklist — US-002

| # | Verificación | Estado | Evidencia |
|---|---|---|---|
| 1 | US en sprint actual (Roadmap PM-IA) | ✅ | Sprint PM-01, Cadena 2 (Core Workdesk), Prioridad P0 |
| 2 | Endpoints definidos en API_CONTRACTS.md | ✅ | Sección 5.2 Workdesk |
| 3 | Backend completado y compilado | ⏳ | Pendiente de handoff_backend_US002_PM01.md |
| 4 | Prerrequisitos completados | ✅ | US-001 (Workdesk base): ✅ 100% Completada |

**Resultado**: ✅ APROBADO para handoff (condicionado a completitud Backend)

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | PM01-S1 |
| **User Story** | US-002: Reclamar una Tarea de Grupo (Claim Task) |
| **CAs Objetivo** | CA-16, CA-18, CA-19, CA-20 + Tests Zero-Mock |
| **Rama Git** | `sprint-8/pm-01/us-002-claim` |
| **SSOT** | `docs/requirements/v1_user_stories_index.md` → `docs/requirements/epics/epic_A_motor_core.md` (líneas 359-588) |
| **Flujo** | Backend ✅ → **Frontend** → QA (aplazado) |

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
| **ADR-002** (Vue 3 + Composition API) | Todo componente DEBE usar `<script setup lang="ts">`. Prohibido Options API. |
| **ADR-003** (Pinia) | Todo estado global en stores Pinia. El estado local efímero (modales, tooltips) es aceptable en `ref()` dentro del componente. |
| **ADR-005** (WebSocket) | Los eventos WebSocket se procesan en el store centralizado. Los componentes ESCUCHAN cambios reactivos, NO se suscriben directamente al socket. |
| **ADR-010** (Testing Pyramid) | Los tests E2E DEBEN ejecutar contra backend real. **PROHIBIDO** usar `route.fulfill()` o cualquier mock de red. |
| **ADR-012** (UX Consistency) | Los colores deben usar las CSS variables del design system. NO hardcodear hex values. |

### Stack Tecnológico Confirmado
- **Vue 3.4+** / **TypeScript** / **Vite**
- **Pinia** para state management
- **Vitest** para unit tests / **Playwright** para E2E
- **Axios** con interceptors centralizados

### Patrones de Consumo de API

Todos los endpoints del Backend se consumen a través de la instancia centralizada de Axios. Los endpoints relevantes para este handoff son:

| Método | URL | Body | Response | CA |
|--------|-----|------|----------|-----|
| GET | `/api/v1/workbox/tasks/{id}/preview` | — | `{ ..., mensajeInterno?, mensajeInternoAuthor?, mensajeInternoAt? }` | CA-16 |
| GET | `/api/v1/workbox/tasks/{id}/audit-trail` | — | `{ events: [{ actionType, userId, reason, timestamp }] }` | CA-20 |
| POST | `/api/v1/workbox/tasks/{id}/extend-timeout` | `{}` | `200 OK` o `422` (límite alcanzado) | CA-19 |

---

## 3. Rutas Exactas y Contexto Preexistente

### Archivos a MODIFICAR

#### 3.1 `TaskPreviewModal.vue` — CA-16 + CA-18
- **Path:** `frontend/src/components/workdesk/TaskPreviewModal.vue`
- **Estado actual:**
  - Modal de preview read-only con ClaimAuditTrail integrado ✅
  - Botón Claim con detección de conflicto `isAlreadyClaimed` ✅
  - **FALTA CA-16:** Banner mostrando "Nota del operario anterior" cuando la tarea tiene `mensajeInterno`
  - **FALTA CA-18:** Banner reactivo cuando otro usuario reclama la tarea mientras se está en modo Solo Lectura

#### 3.2 `useWorkdeskStore.ts` (550 líneas) — CA-19
- **Path:** `frontend/src/stores/useWorkdeskStore.ts`
- **Estado actual:**
  - `initWebSocket()` maneja evento `GHOST_WARNING` ✅
  - **FALTA CA-19:** El handler del `GHOST_WARNING` debe exponer datos reactivos al componente para renderizar botones "[Necesito más tiempo]" y "[Guardar borrador]".
  - Debe tener una action `extendTimeout(taskId)` que llame `POST /api/v1/workbox/tasks/{id}/extend-timeout`.

#### 3.3 `ClaimAuditTrail.vue` (70 líneas) — CA-20
- **Path:** `frontend/src/components/workdesk/ClaimAuditTrail.vue`
- **Estado actual:**
  - Timeline con dots color-coded: FORCE_UNCLAIM=red, CLAIM=green ✅
  - **FALTA CA-20:** Agregar los tipos enriquecidos: 🟢 CLAIMED, 🔵 RELEASED, 🟠 FORCE_UNCLAIMED, 🔴 AUTO_UNCLAIMED, ⏰ TIMEOUT_EXTENDED, 📦 BULK_CLAIMED.

#### 3.4 `Workdesk.vue` (1268 líneas) — CA-19
- **Path:** `frontend/src/views/Workdesk.vue`
- **Estado actual:**
  - Vista principal con tabs "Mi Bandeja" / "Pool Disponible" ✅
  - **FALTA CA-19:** Cuando se recibe un `GHOST_WARNING` del WebSocket, mostrar un Toast persistente con los botones de acción. El Toast NO debe ser un simple `toast.warning()` sino un componente custom con slots para los botones.

---

## 4. Snippets Prescriptivos

### 4.1 CA-16 — Banner "Nota del operario anterior"

**Agregar dentro de `TaskPreviewModal.vue`, en la parte superior del contenido del modal:**

```vue
<!-- CA-16: Banner de nota interna del operario anterior -->
<div
  v-if="task?.mensajeInterno"
  class="internal-note-banner"
  data-testid="internal-note-banner"
>
  <span class="internal-note-banner__icon">📝</span>
  <div class="internal-note-banner__content">
    <strong>Nota del operario anterior:</strong>
    <p>{{ task.mensajeInterno }}</p>
    <span class="internal-note-banner__meta">
      — {{ task.mensajeInternoAuthor }}, hace {{ formatTimeAgo(task.mensajeInternoAt) }}
    </span>
  </div>
</div>
```

**Estilos CSS (añadir al `<style scoped>`):**
```css
.internal-note-banner {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  background: var(--color-info-soft, #e8f4fd);
  border-left: 4px solid var(--color-info, #2196f3);
  border-radius: 6px;
  margin-bottom: 1rem;
  animation: slideDown 0.3s ease-out;
}

.internal-note-banner__icon {
  font-size: 1.25rem;
  flex-shrink: 0;
}

.internal-note-banner__content p {
  margin: 0.25rem 0;
  color: var(--color-text-primary);
  font-style: italic;
}

.internal-note-banner__meta {
  font-size: 0.8rem;
  color: var(--color-text-secondary);
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}
```

### 4.2 CA-18 — Banner de "Tarea reclamada por otro" en modo lectura

**Agregar al `TaskPreviewModal.vue` un watcher reactivo sobre el WebSocket:**

```vue
<script setup lang="ts">
import { watch, ref } from 'vue';

const isClaimedByOther = ref(false);
const claimedByName = ref('');

// CA-18: Escuchar evento WebSocket 'TASK_CLAIMED' mientras estamos en preview
const workdeskStore = useWorkdeskStore();

watch(
  () => workdeskStore.lastWsEvent,
  (event) => {
    if (
      event?.action === 'REMOVE' &&
      event?.taskId === props.taskId &&
      event?.claimedBy !== currentUserId.value
    ) {
      isClaimedByOther.value = true;
      claimedByName.value = event.claimedByName || 'otro compañero';
    }
  }
);
</script>

<!-- Banner CA-18 -->
<div
  v-if="isClaimedByOther"
  class="claimed-warning-banner"
  data-testid="claimed-by-other-banner"
>
  <span>⚠️</span>
  <p>
    Esta tarea fue reclamada por <strong>{{ claimedByName }}</strong>
    y ya no está disponible.
  </p>
</div>

<!-- Botón Reclamar deshabilitado cuando otro ya reclamó -->
<button
  :disabled="isClaimedByOther || isAlreadyClaimed"
  :class="{ 'btn--disabled': isClaimedByOther }"
  data-testid="btn-claim-preview"
  @click="handleClaim"
>
  <span v-if="isClaimedByOther">🔒 Reclamar</span>
  <span v-else>Reclamar</span>
</button>
```

**Estilos:**
```css
.claimed-warning-banner {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: var(--color-warning-soft, #fff3e0);
  border-left: 4px solid var(--color-warning, #ff9800);
  border-radius: 6px;
  margin-bottom: 1rem;
  animation: slideDown 0.3s ease-out;
}

.btn--disabled {
  opacity: 0.5;
  cursor: not-allowed;
  filter: grayscale(0.8);
}
```

### 4.3 CA-19 — Ghost Warning Toast con botones de acción

**Agregar al store `useWorkdeskStore.ts`:**

```typescript
// Estado reactivo para Ghost Warning
const ghostWarning = ref<{
  taskId: string;
  taskName: string;
  remainingMinutes: number;
  visible: boolean;
} | null>(null);

// Action: Extend timeout
async function extendTimeout(taskId: string) {
  try {
    await api.post(`/workbox/tasks/${taskId}/extend-timeout`);
    ghostWarning.value = null;
    toast.success('⏰ Tiempo extendido. Tu tarea no será devuelta.');
  } catch (error: any) {
    if (error.response?.status === 422) {
      toast.error('Has alcanzado el límite de 2 extensiones. Completa la tarea para evitar el auto-unclaim.');
    } else {
      toast.error('Error al extender el tiempo.');
    }
  }
}

// En initWebSocket(), handler del GHOST_WARNING:
case 'GHOST_WARNING':
  ghostWarning.value = {
    taskId: data.taskId,
    taskName: data.taskName || 'Tarea',
    remainingMinutes: data.remainingMinutes,
    visible: true,
  };
  break;
```

**Agregar componente de Toast persistente en `Workdesk.vue`:**

```vue
<!-- CA-19: Ghost Warning Toast Persistente -->
<Teleport to="body">
  <Transition name="toast-slide">
    <div
      v-if="workdeskStore.ghostWarning?.visible"
      class="ghost-warning-toast"
      data-testid="ghost-warning-toast"
    >
      <div class="ghost-warning-toast__header">
        ⚠️ Tu tarea
        <strong>{{ workdeskStore.ghostWarning.taskName }}</strong>
        será devuelta a la cola grupal en
        <strong>{{ workdeskStore.ghostWarning.remainingMinutes }} minutos</strong>
        por inactividad.
      </div>
      <div class="ghost-warning-toast__actions">
        <button
          class="btn btn--primary"
          data-testid="btn-extend-timeout"
          @click="workdeskStore.extendTimeout(workdeskStore.ghostWarning!.taskId)"
        >
          ⏰ Necesito más tiempo
        </button>
        <button
          class="btn btn--secondary"
          data-testid="btn-save-draft"
          @click="handleSaveDraft(workdeskStore.ghostWarning!.taskId)"
        >
          💾 Guardar borrador
        </button>
      </div>
    </div>
  </Transition>
</Teleport>
```

**Estilos:**
```css
.ghost-warning-toast {
  position: fixed;
  bottom: 1.5rem;
  right: 1.5rem;
  z-index: 9999;
  background: var(--color-warning-bg, #fff8e1);
  border: 2px solid var(--color-warning, #ff9800);
  border-radius: 12px;
  padding: 1.25rem;
  max-width: 420px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.ghost-warning-toast__header {
  font-size: 0.95rem;
  line-height: 1.5;
  margin-bottom: 1rem;
}

.ghost-warning-toast__actions {
  display: flex;
  gap: 0.75rem;
}

.toast-slide-enter-active { animation: slideUp 0.4s ease-out; }
.toast-slide-leave-active { animation: slideUp 0.3s ease-in reverse; }

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
```

### 4.4 CA-20 — Timeline enriquecido en ClaimAuditTrail

**Reemplazar el mapeo de colores actual en `ClaimAuditTrail.vue`:**

```typescript
// CA-20: Mapeo completo de actionType a estilo visual
const ACTION_STYLE_MAP: Record<string, { icon: string; color: string; label: string }> = {
  CLAIMED:          { icon: '🟢', color: 'var(--color-success, #4caf50)',  label: 'Reclamada voluntariamente' },
  RELEASED:         { icon: '🔵', color: 'var(--color-info, #2196f3)',     label: 'Liberada por el operario' },
  FORCE_UNCLAIMED:  { icon: '🟠', color: 'var(--color-warning, #ff9800)',  label: 'Despojada por supervisor' },
  AUTO_UNCLAIMED:   { icon: '🔴', color: 'var(--color-danger, #f44336)',   label: 'Liberada por inactividad' },
  TIMEOUT_EXTENDED: { icon: '⏰', color: 'var(--color-info-light, #64b5f6)', label: 'Tiempo extendido' },
  BULK_CLAIMED:     { icon: '📦', color: 'var(--color-primary, #3f51b5)',  label: 'Reclamada en lote' },
};

function getEventStyle(actionType: string) {
  return ACTION_STYLE_MAP[actionType] || { icon: '⚪', color: '#999', label: actionType };
}
```

**Template actualizado del timeline:**
```vue
<div class="audit-timeline" data-testid="claim-audit-timeline">
  <div
    v-for="event in auditEvents"
    :key="event.id"
    class="audit-timeline__event"
  >
    <span
      class="audit-timeline__dot"
      :style="{ backgroundColor: getEventStyle(event.actionType).color }"
      :title="getEventStyle(event.actionType).label"
    >
      {{ getEventStyle(event.actionType).icon }}
    </span>
    <div class="audit-timeline__content">
      <strong>{{ getEventStyle(event.actionType).label }}</strong>
      <span v-if="event.reason"> — {{ event.reason }}</span>
      <div class="audit-timeline__meta">
        {{ event.userName }} · {{ formatDate(event.timestamp) }}
      </div>
    </div>
  </div>
</div>
```

---

## 5. Matriz de QA y Testing Atómico

### 5.1 Tests Requeridos

> ⚠️ **PROHIBICIÓN ZERO-MOCK:** El test existente `us002-workbox-kanban.spec.ts` usa `route.fulfill()` para mockear respuestas. Esto viola la política zero-mock (ADR-010). **NO** lo modifiques. Se abordará en el handoff de QA.

#### Archivo: `frontend/src/tests/components/workdesk/TaskPreviewModal.spec.ts` [NUEVO o AMPLIAR]

| Test Name | CA | Aserción Esperada |
|-----------|-----|-------------------|
| `should show internal note banner when mensajeInterno exists` | CA-16 | `data-testid="internal-note-banner"` visible con texto del mensaje |
| `should not show internal note banner when no mensajeInterno` | CA-16 | `data-testid="internal-note-banner"` NO presente en DOM |
| `should show claimed-by-other banner on REMOVE ws event` | CA-18 | `data-testid="claimed-by-other-banner"` visible tras evento |
| `should disable claim button when claimed by other` | CA-18 | `data-testid="btn-claim-preview"` deshabilitado |

#### Archivo: `frontend/src/tests/components/workdesk/ClaimAuditTrail.spec.ts` [NUEVO o AMPLIAR]

| Test Name | CA | Aserción Esperada |
|-----------|-----|-------------------|
| `should render all 6 action types with correct icons` | CA-20 | Cada tipo mapea al emoji correcto |
| `should show correct label for AUTO_UNCLAIMED` | CA-20 | "Liberada por inactividad" visible |
| `should show correct label for BULK_CLAIMED` | CA-20 | "Reclamada en lote" visible |

#### Archivo: `frontend/src/tests/stores/workdeskStore.spec.ts` [AMPLIAR]

| Test Name | CA | Aserción Esperada |
|-----------|-----|-------------------|
| `should expose ghostWarning state on GHOST_WARNING ws event` | CA-19 | `ghostWarning.value` no null, tiene taskId y remainingMinutes |
| `should call extend-timeout endpoint and clear warning` | CA-19 | POST a `/extend-timeout`, ghostWarning reset |
| `should handle 422 on third extension attempt` | CA-19 | Error toast con mensaje de límite |

---

## 6. Directivas Obligatorias

### 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA

El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. El frontend Vite corre en `http://localhost:5173` con proxy a `http://localhost:8080`.
1. Antes de iniciar: `curl -s http://localhost:8080/actuator/health` → `{"status":"UP"}`.
2. Para el frontend: `cd frontend && npm run dev` → Debe arrancar sin errores.
3. Si necesitas el backend pero no está corriendo, comunícalo al Arquitecto Líder.
**PROHIBIDO** modificar `vite.config.ts` para apuntar a otro backend.

### ⚠️ PRECISIÓN QUIRÚRGICA

Todo desarrollo debe ser **QUIRÚRGICO**. Los componentes afectados son delicados — `Workdesk.vue` tiene 1268 líneas y `useWorkdeskStore.ts` tiene 550 líneas. Cualquier cambio debe ser localizado y no romper la funcionalidad existente. Usa `git diff --stat` frecuentemente para verificar el alcance de tus cambios.

### 📚 SKILLS DE CODIFICACIÓN OBLIGATORIOS
- Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md`.

### 🔨 BUILD OBLIGATORIO
Build obligatorio: Ejecuta el protocolo documentado en `.agents/skills/frontend_build_audit/SKILL.md`. Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

---

## INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN

1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_FRONTEND.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_FRONTEND.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `sprint-8/pm-01/us-002-claim`. Queda estrictamente prohibido usar git stash.
