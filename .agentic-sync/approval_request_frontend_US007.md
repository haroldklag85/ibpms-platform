# 📋 Solicitud de Aprobación — FRONTEND US-007 (Ejecución BPMN)

> **Agente**: 🎨 FRONTEND - VUE 3  
> **Fecha**: 2026-06-06  
> **Handoff de Origen**: `.agentic-sync/handoff_frontend_US007.md`  
> **Estado**: ⏳ PENDIENTE DE APROBACIÓN ARQUITECTO

---

## 1. RESUMEN EJECUTIVO

Integrar los endpoints reales de ejecución BPMN (`POST /api/bpmn/instances` y `POST /api/bpmn/tasks/{taskId}/complete`) en la UI de la plataforma, permitiendo:
1. **Iniciar un caso (proceso BPMN)** desde la interfaz operativa.
2. **Completar tareas pendientes BPMN** desde la bandeja Workdesk usando el endpoint específico de US-007.

Todas las llamadas serán **ZERO MOCKS** contra la API real del backend, gestionadas con **Axios + Pinia**.

---

## 2. HALLAZGOS DEL ESCANEO RAG

### 2.1 Contrato Backend Confirmado

| Endpoint | Método | Request Body | Response |
|---|---|---|---|
| `/api/bpmn/instances` | POST | `{ processDefinitionKey: string (REQUIRED), businessKey?: string, variables?: Record<string, unknown> }` | `201 Created` → `{ processInstanceId, processDefinitionKey, businessKey, startedAt, startedBy }` |
| `/api/bpmn/tasks/{taskId}/complete` | POST | `Record<string, unknown>` (variables opcionales) | `204 No Content` |
| `/api/v1/design/processes/catalog` | GET | — | Lista de `ProcessCatalogItem[]` con `key, name, version, deployDate, status, formPattern` |

**Errores**: RFC 7807 `ProblemDetail` (`type`, `title`, `status`, `detail`, `errors[]`).

### 2.2 Hallazgos Críticos del Frontend Actual

| # | Hallazgo | Impacto |
|---|---|---|
| H-01 | **No existe `useProcessStore`** ni ningún store de ejecución de procesos | Debe crearse desde cero |
| H-02 | **No existe método `startProcess`** en el objeto `api` de `apiClient.ts` | Debe agregarse |
| H-03 | **`apiClient` tiene `baseURL: '/api/v1'`** pero los endpoints de ejecución BPMN están en `/api/bpmn/` (sin `/v1`) | Requiere `baseURL` override por llamada |
| H-04 | El `completeTask` existente apunta a `/workbox/tasks/{id}/complete` (US-017), **NO** al endpoint US-007 `/api/bpmn/tasks/{taskId}/complete` | Agregar método separado `completeBpmnTask` |
| H-05 | El proxy Vite reenvía todo `/api` → `localhost:8080` | ✅ Compatible, no requiere cambios |
| H-06 | No existe ruta de UI para iniciar procesos para usuarios operativos | Integrar panel en Workdesk/Portal |
| H-07 | `getCatalogProcesses()` ya existe en `api` → `GET /design/processes/catalog` | ✅ Reutilizable directamente |
| H-08 | No existe `types/Process.ts` | Debe crearse con interfaces tipadas |

### 2.3 ⚠️ DISCREPANCIA SSOT DETECTADA

> [!WARNING]  
> En el SSOT (`docs/requirements/epics/epic_B_formularios_bpmn.md`), la US-007 corresponde a **"Generador Cognitivo de DMN (NLP a Tablas de Decisión)"**, NO a "Ejecución BPMN". Sin embargo, el **Handoff Arquitectónico** (`.agentic-sync/handoff_frontend_US007.md`) define explícitamente la misión como consumir endpoints de ejecución BPMN. **Procedo según el Handoff del Arquitecto Líder**, pero se solicita al Arquitecto confirmar la numeración correcta de la US para trazabilidad SSOT.

---

## 3. PLAN DE IMPLEMENTACIÓN

### Componente 1: Capa de Tipos — `types/Process.ts`

#### [NEW] `frontend/src/types/Process.ts`

Definir interfaces TypeScript que reflejan el contrato exacto del backend:

```typescript
/** Contrato de request para iniciar un proceso BPMN (POST /api/bpmn/instances) */
export interface StartProcessRequest {
  processDefinitionKey: string;
  businessKey?: string;
  variables?: Record<string, unknown>;
}

/** Contrato de response al iniciar un proceso BPMN (201 Created) */
export interface StartProcessResult {
  processInstanceId: string;
  processDefinitionKey: string;
  businessKey: string | null;
  startedAt: string; // ISO 8601
  startedBy: string;
}

/** Ítem del catálogo de procesos disponibles (GET /api/v1/design/processes/catalog) */
export interface ProcessCatalogItem {
  key: string;
  name: string;
  version: number;
  deployDate: string;
  status: string;
  formPattern?: string;
}

/** Estructura de error RFC 7807 ProblemDetail del backend */
export interface BpmnProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  errors?: Array<{ field: string; issue: string }>;
}
```

---

### Componente 2: Capa de API — `apiClient.ts`

#### [MODIFY] `frontend/src/services/apiClient.ts`

Agregar 2 métodos al objeto `api` exportado. **Punto crítico**: estos endpoints NO están bajo `/api/v1`, sino bajo `/api/bpmn/`, por lo que se requiere un override de `baseURL` en cada llamada individual.

```typescript
// US-007: Ejecución BPMN (Endpoints fuera de /api/v1)
startProcess: (payload: StartProcessRequest) =>
  apiClient.post('/bpmn/instances', payload, { baseURL: '/api' }),

completeBpmnTask: (taskId: string, variables?: Record<string, unknown>) =>
  apiClient.post(`/bpmn/tasks/${taskId}/complete`, variables ?? {}, {
    baseURL: '/api',
    headers: {
      'Idempotency-Key': crypto.randomUUID?.() ?? `${Math.random().toString(36).substring(2)}${Date.now().toString(36)}`
    }
  }),
```

**Justificación del `baseURL: '/api'`**: El `apiClient` tiene `baseURL: '/api/v1'`. Axios concatena `baseURL + url`, produciendo `/api/v1/bpmn/instances` si no se overridea. Con `baseURL: '/api'`, la URL resultante es `/api/bpmn/instances` — coincidente con el proxy Vite `/api → localhost:8080` y con el endpoint real del backend.

**No se modifica** el `completeTask` existente (línea 284) porque pertenece a US-017/US-003 con un contrato diferente (200 + `eventReference`).

---

### Componente 3: Pinia Store — `useProcessStore.ts`

#### [NEW] `frontend/src/stores/useProcessStore.ts`

Store Pinia dedicado para la ejecución de procesos BPMN con estado reactivo y error handling RFC 7807.

**Estado**:
- `catalog: ProcessCatalogItem[]` — Lista de procesos desplegados
- `isLoadingCatalog: boolean` — Loading spinner
- `isStartingProcess: boolean` — Loading del botón "Iniciar"
- `lastStartResult: StartProcessResult | null` — Resultado de la última instanciación
- `error: string | null` — Mensaje de error para la UI

**Acciones**:
- `fetchCatalog()` — Llama `api.getCatalogProcesses()` (ya existe, reutilizamos)
- `startProcess(request: StartProcessRequest)` — Llama `api.startProcess()`, maneja errores RFC 7807, retorna resultado
- `clearError()` — Limpia estado de error

**Características**:
- Parseo de errores RFC 7807 (`ProblemDetail`) para mensajes amigables
- Manejo explícito de `ProcessDefinitionNotFoundException` (404) con mensaje descriptivo
- Manejo de errores 400 (validación) con lista de campos inválidos
- `try/catch` real — Zero Mocks, Fail-Fast genuino
- Traceability tags en cada acción

---

### Componente 4: Componente UI — `ProcessCatalogPanel.vue`

#### [NEW] `frontend/src/components/workdesk/ProcessCatalogPanel.vue`

Componente tipo **panel lateral (drawer)** o **modal** que se activará desde un botón en la vista Workdesk. Diseño:

**Estructura funcional**:
1. **Trigger**: Botón `➕ Iniciar Caso` en el header de la vista Workdesk
2. **Panel**: Lista de procesos disponibles obtenidos del catálogo (`fetchCatalog`)
3. **Acción por proceso**: Botón "Iniciar" por cada definición de proceso
4. **Confirmación**: Modal de confirmación antes de iniciar (prevención de ejecuciones accidentales) — componente Vue, **NO** `confirm()` nativo (cumpliendo .cursorrules § 5)
5. **Feedback**: Toast DOM de éxito (consistente con el patrón existente del proyecto) mostrando `processInstanceId` y refresh automático del inbox
6. **Error**: Renderizado del `ProblemDetail` en un toast de error con detalles

**Campos opcionales para el usuario** (si el proceso lo requiere):
- `businessKey` — Campo de texto opcional
- `variables` — Se podría abrir un mini-formulario, pero para el MVP esto se omite y se envía vacío (el formulario dinámico vendrá con US-003/US-017)

**NO se crea una ruta nueva en el router**. El panel se integra como componente hijo de `Workdesk.vue`.

---

### Componente 5: Integración en Workdesk — Task Completion BPMN

#### [MODIFY] `frontend/src/views/Workdesk.vue`

**Cambios mínimos y quirúrgicos** (Regla de Integración Visual Conservadora):

1. **Importar** `ProcessCatalogPanel.vue` y agregarlo al template con `v-if` controlado por un `ref<boolean>`
2. **Botón "Iniciar Caso"** en la barra de herramientas superior del Workdesk
3. **Post-start**: Al iniciar exitosamente un proceso, cerrar el panel y llamar `workdeskStore.fetchGlobalInbox()` para refrescar la bandeja

**Respecto a "Completar Tarea"**: El Workdesk ya tiene `completeTask()` funcional vía `useWorkdeskStore` → `/workbox/tasks/{id}/complete`. El handoff indica "si no lo cubre US-029". **Evaluación**: La funcionalidad ya existe para el flujo operativo estándar. El endpoint `POST /api/bpmn/tasks/{taskId}/complete` (US-007) es una ruta alternativa más simple (204 sin body). Si el Arquitecto lo autoriza, puedo agregar un método alternativo `completeBpmnTaskDirect()` en el store que use este endpoint para tareas puramente BPMN, invocable desde el Workdesk cuando `sourceSystem === 'BPMN'`.

---

## 4. ARCHIVOS IMPACTADOS (RESUMEN)

| Acción | Archivo | Justificación |
|--------|---------|---------------|
| **[NEW]** | `frontend/src/types/Process.ts` | Interfaces TypeScript para contratos BPMN |
| **[MODIFY]** | `frontend/src/services/apiClient.ts` | +2 métodos API: `startProcess`, `completeBpmnTask` |
| **[NEW]** | `frontend/src/stores/useProcessStore.ts` | Pinia store de ejecución de procesos |
| **[NEW]** | `frontend/src/components/workdesk/ProcessCatalogPanel.vue` | Panel de catálogo con botón Iniciar |
| **[MODIFY]** | `frontend/src/views/Workdesk.vue` | Integrar botón + panel (cambio quirúrgico) |
| **[MODIFY]** | `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` | Registro de cambio no-técnico |

---

## 5. LO QUE NO SE TOCA (Límites Rojos)

- ❌ NO se refactorizan Stores Pinia globales existentes (`useWorkdeskStore`, `authStore`)
- ❌ NO se tocan interceptores Axios globales
- ❌ NO se crean rutas nuevas en el router (el panel es inline en Workdesk)
- ❌ NO se usan mocks, arrays estáticos ni `mockAdapter.ts`
- ❌ NO se usa `alert()` ni `confirm()` nativos del DOM
- ❌ NO se usa `git stash` — consolidación con `git commit` + `git push`

---

## 6. PLAN DE VERIFICACIÓN

1. **Build Zero-Trust**: `npm run build` desde `frontend/` — debe reportar `✓ built in Xs` sin errores
2. **Correspondencia API**: Verificar que URLs, métodos HTTP y DTOs coinciden con el backend desplegado
3. **Graceful Degradation**: Si backend no está arriba, la UI debe mostrar error real (Fail-Fast), NO datos estáticos
4. **Post-Build**: Commit en rama de sprint, push, y notificación al Humano Enrutador

---

## 7. PREGUNTAS ABIERTAS PARA EL ARQUITECTO

| # | Pregunta | Impacto |
|---|----------|---------|
| Q-01 | **Discrepancia de numeración**: La US-007 en el SSOT es "Generador Cognitivo de DMN", no "Ejecución BPMN". ¿Se debe actualizar la trazabilidad a otra US? | Tags `@Traceability` en código |
| Q-02 | **¿Usar el endpoint alternativo `completeBpmnTask` (204) para tareas BPMN en Workdesk?** El `completeTask` existente usa `/workbox/tasks/{id}/complete` (US-017). ¿Coexisten ambos o el US-007 reemplaza para tareas `sourceSystem === 'BPMN'`? | Lógica de bifurcación en `useWorkdeskStore` |
| Q-03 | **¿Variables de inicio de proceso?** Por ahora se envían vacías (`{}`). ¿Se necesita un mini-formulario de variables iniciales o se delega a US-003/US-017? | Complejidad del `ProcessCatalogPanel` |

---

> **Estado del Agente Frontend**: PLANNING COMPLETO. Esperando aprobación del Arquitecto Líder para pasar a EXECUTION.
