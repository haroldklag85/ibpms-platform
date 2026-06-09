# 🎨 Handoff Frontend — Sprint 6 / Iteración 6.1

> **Iteración:** Sprint 6 — Iteración 6.1 (B-20 DMN↔BPMN Low-Code + Adaptación E2E Real)  
> **Rama de trabajo:** `sprint-6/uat-certification` (debe existir, creada por Backend)  
> **US objetivo:** US-005 (BPMN Designer), US-007 (DMN Viewer), US-008 (Kanban)  
> **Flujo:** Backend (PRIMERO) → **Frontend** (TÚ) → QA  
> **SSOT de referencia:** `docs/requirements/v1_user_stories_index.md` → `docs/requirements/epics/epic_B_formularios_bpmn.md`, `epic_A_orquestacion.md`  
> **Autor:** Arquitecto Líder SW  
> **Fecha:** 2026-04-19

---

## 1. Metadatos y SSOT

| Parámetro | Valor |
|-----------|-------|
| **Sprint** | 6 — Iteración 6.1 |
| **Rama Git** | `sprint-6/uat-certification` |
| **US** | US-005 (BPMN Designer decisionRef), US-007 (DMN catálogo), US-008 (Kanban real) |
| **Bloques de trabajo** | B1: Dropdown DMN en BpmnDesigner (B-20), B2: KanbanView real (US-008), B3: Playwright E2E config |
| **Dependencia Backend:** | ✅ Backend debe haber pusheado: endpoint `GET /api/v1/dmn-models/definitions` y el `docker-compose.e2e.yml` |
| **Exclusiones** | US-017 CQRS excluida. `<UniversalSlaTimer>` P2 no incluido. |

**Fuentes de verdad:**
- `docs/requirements/epics/epic_B_formularios_bpmn.md` → US-005 CA-12 (vinculación BPMN↔DMN/Form)
- `docs/sprints/sprint_plan_s6.md` → Sección "Detalle Técnico B-20"
- `frontend/src/views/BpmnDesigner.vue` → Vista del modelador BPMN (archivo principal)
- `frontend/src/views/kanban/KanbanView.vue` → Vista Kanban actual con mocks

---

## 2. Alineación Arquitectónica y ADRs

| ADR | Impacto en esta iteración |
|-----|---------------------------|
| `adr-002-vue3-microfrontends.md` | Composición Vue 3, Composition API, stores Pinia. Sin mixins. |
| `adr_010_testing_pyramid_governance.md` | Tests Vitest obligatorios por componente nuevo/modificado. |
| `adr-001-hexagonal-architecture.md` | Frontend consume API via `apiClient.ts` (Axios). PROHIBIDO lógica de negocio en componentes. |

**Principios de diseño:**
- Toda data se obtiene vía Axios → API real. PROHIBIDOS los mocks hardcodeados en código de producción.
- Sanitización XSS con `DOMPurify` para contenido rico (descripciones Kanban, DMN XML).
- TypeScript strict mode obligatorio. Zero `any`.

---

## 3. Rutas Exactas y Contexto Preexistente

### B1: Dropdown DMN en BpmnDesigner (B-20 — Deuda Técnica)

- **Archivo principal:** `frontend/src/views/BpmnDesigner.vue`
- **Contexto existente:**
  - Líneas ~255-272: Sidebar del BusinessRuleTask — actualmente NO tiene dropdown para `decisionRef`
  - Líneas ~1278+: Event listener `selection.changed` — rehidrata `selectedElement.props` con `formKey`, `candidateGroups`, etc.
  - El dropdown de `formKey` para UserTask YA EXISTE y funciona con 1 clic (dropdown que lista formularios publicados)
  - El `decisionRef` para BusinessRuleTask **NO tiene equivalente visual** — el Arquitecto debe editarlo manualmente en el XML
- **Endpoint Backend nuevo (creado en handoff Backend):** `GET /api/v1/dmn-models/definitions` → retorna `[{id, key, name, version, deploymentDate}]`

### B2: KanbanView Real (US-008)

- **Archivo:** `frontend/src/views/kanban/KanbanView.vue`
- **Estado actual:** 
  - Línea 44-45: `const board = ref<KanbanBoard>({...})` ← datos hardcodeados inline con mock
  - Línea 65: `handleItemMove` invoca `api.updateKanbanStatus(item.id, newStatus)` — pero el endpoint backend NO existe aún
  - Componente `KanbanColumn.vue` existe en `frontend/src/components/kanban/`
  - Tipo `KanbanBoard` / `KanbanItem` en `frontend/src/types/Kanban.ts`
- **Store existente:** NO existe `useKanbanStore`. El `agileStore.ts` existe para proyectos ágiles pero NO cubre Kanban.

### B3: Playwright E2E Config

- **Config existente:** `frontend/playwright.config.ts` — configurado para tests con `page.route()` (mocks HTTP)
- **Tests existentes:** `frontend/e2e/` — 31 specs con interceptores HTTP
- **Necesidad:** Config alternativo para backend real (Docker Compose)

---

## 4. Snippets Prescriptivos

### B1: Dropdown DMN en Sidebar del BpmnDesigner

**4.1 Función de carga de definiciones DMN (en `<script setup>`):**
```typescript
import apiClient from '@/services/apiClient';

interface DmnDefinitionDto {
  id: string;
  key: string;
  name: string;
  version: number;
  deploymentId: string;
  deploymentDate: string;
}

const dmnDefinitions = ref<DmnDefinitionDto[]>([]);

const fetchDmnDefinitions = async () => {
  try {
    const { data } = await apiClient.get<DmnDefinitionDto[]>('/api/v1/dmn-models/definitions');
    dmnDefinitions.value = data;
  } catch (error) {
    console.error('[BpmnDesigner] Error cargando catálogo DMN:', error);
    dmnDefinitions.value = [];
  }
};
```

**4.2 Template del dropdown DMN (en el sidebar, junto al dropdown de FormKey):**
```html
<!-- Panel DMN Binding — Visible cuando selectedElement.type === 'bpmn:BusinessRuleTask' -->
<div v-if="selectedElement?.type === 'bpmn:BusinessRuleTask'" class="sidebar-section">
  <label class="sidebar-label">Tabla DMN vinculada</label>
  <select 
    v-model="selectedElement.props.decisionRef"
    @change="syncElementProperties('camunda:decisionRef', $event.target.value)"
    class="sidebar-select"
    @focus="fetchDmnDefinitions()"
  >
    <option value="">— Seleccionar tabla DMN —</option>
    <option 
      v-for="dmn in dmnDefinitions" 
      :key="dmn.key" 
      :value="dmn.key"
    >
      {{ dmn.name }} (v{{ dmn.version }})
    </option>
  </select>
  
  <!-- Estrategia de Binding (ya existente, agregar debajo) -->
  <label class="sidebar-label mt-2">Estrategia de versionamiento</label>
  <select 
    v-model="selectedElement.props.decisionRefBinding"
    @change="syncElementProperties('camunda:decisionRefBinding', $event.target.value)"
    class="sidebar-select"
  >
    <option value="latest">LATEST (última versión)</option>
    <option value="deployment">DEPLOYMENT (versión del deploy)</option>
  </select>
</div>
```

**4.3 Rehidratación en `selection.changed` (L1278+):**
```typescript
// AGREGAR dentro del handler existente de selection.changed:
if (bo.$type === 'bpmn:BusinessRuleTask') {
  selectedElement.value.props.decisionRef = bo.get('camunda:decisionRef') || '';
  selectedElement.value.props.decisionRefBinding = bo.get('camunda:decisionRefBinding') || 'latest';
}
```

### B2: useKanbanStore.ts (Pinia Store — NUEVO)

```typescript
// frontend/src/stores/kanbanStore.ts
import { defineStore } from 'pinia';
import apiClient from '@/services/apiClient';
import type { KanbanBoard, KanbanItem } from '@/types/Kanban';

interface KanbanState {
  board: KanbanBoard | null;
  loading: boolean;
  error: string | null;
}

export const useKanbanStore = defineStore('kanban', {
  state: (): KanbanState => ({
    board: null,
    loading: false,
    error: null,
  }),

  getters: {
    tasksByColumn: (state) => (columnId: string) => {
      return state.board?.columns.find(c => c.id === columnId)?.items ?? [];
    },
  },

  actions: {
    async fetchBoard(projectId: string) {
      this.loading = true;
      this.error = null;
      try {
        const { data } = await apiClient.get<KanbanBoard>(`/api/v1/projects/${projectId}/kanban`);
        this.board = data;
      } catch (err: unknown) {
        this.error = err instanceof Error ? err.message : 'Error al cargar tablero';
      } finally {
        this.loading = false;
      }
    },

    async moveTask(taskId: string, newStatus: string, blockReason?: string) {
      try {
        await apiClient.patch(`/api/v1/projects/kanban/tasks/${taskId}/status`, {
          status: newStatus,
          blockReason: blockReason ?? null,
        });
        // Actualización optimista local
        if (this.board) {
          for (const col of this.board.columns) {
            const idx = col.items.findIndex(i => i.id === taskId);
            if (idx !== -1) {
              const [task] = col.items.splice(idx, 1);
              task.status = newStatus;
              const targetCol = this.board.columns.find(c => c.id === newStatus);
              targetCol?.items.push(task);
              break;
            }
          }
        }
      } catch (err: unknown) {
        this.error = err instanceof Error ? err.message : 'Error al mover tarea';
        throw err; // Propagar para rollback UI
      }
    },
  },
});
```

### B2: Refactorizar KanbanView.vue

**ELIMINAR** la constante `board = ref<KanbanBoard>({...})` hardcodeada (L44-62).
**REEMPLAZAR** por:
```typescript
import { useKanbanStore } from '@/stores/kanbanStore';
import { useRoute } from 'vue-router';

const route = useRoute();
const kanbanStore = useKanbanStore();
const projectId = computed(() => route.params.projectId as string);

onMounted(() => {
  kanbanStore.fetchBoard(projectId.value);
});
```

**Modal obligatorio para BLOCKED:**
```html
<Dialog v-model:visible="showBlockedModal" header="Motivo del Bloqueo" :modal="true">
  <Textarea 
    v-model="blockReason" 
    rows="3" 
    placeholder="Describe el motivo del bloqueo (mínimo 10 caracteres)"
    :class="{ 'p-invalid': blockReason.length < 10 && blockReason.length > 0 }"
  />
  <small v-if="blockReason.length > 0 && blockReason.length < 10" class="p-error">
    Mínimo 10 caracteres requeridos
  </small>
  <template #footer>
    <Button label="Cancelar" @click="cancelBlock" severity="secondary" />
    <Button label="Confirmar Bloqueo" @click="confirmBlock" :disabled="blockReason.length < 10" />
  </template>
</Dialog>
```

### B3: Playwright E2E Config

```typescript
// frontend/playwright.e2e.config.ts
import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './e2e/certification',
  timeout: 60000,
  use: {
    baseURL: 'http://localhost:5173',
    storageState: './e2e/auth/storageState.json',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'e2e-auth-setup',
      testMatch: /auth\.setup\.ts/,
    },
    {
      name: 'e2e-certification',
      dependencies: ['e2e-auth-setup'],
      testMatch: /.*\.e2e\.spec\.ts/,
    },
  ],
});
```

---

## 5. Matriz de QA y Testing Atómico (Vitest)

| Test Name | Bloque | CA | Aserción Esperada |
|-----------|:------:|:--:|-------------------|
| `BpmnDesigner.spec.ts: renderiza dropdown DMN al seleccionar BusinessRuleTask` | B1 | US-005/B-20 | El select con opciones DMN aparece cuando `selectedElement.type === 'bpmn:BusinessRuleTask'` |
| `BpmnDesigner.spec.ts: seleccionar DMN invoca syncElementProperties con decisionRef` | B1 | US-005/B-20 | `syncElementProperties('camunda:decisionRef', 'dmn_key_selected')` es llamado |
| `BpmnDesigner.spec.ts: rehidrata decisionRef en selection.changed` | B1 | US-005/B-20 | `selectedElement.props.decisionRef === 'my_dmn_key'` tras cambiar selección a un BusinessRuleTask |
| `KanbanView.spec.ts: carga datos desde store, no mocks hardcodeados` | B2 | US-008 | `kanbanStore.fetchBoard()` se invoca en `onMounted` |
| `KanbanView.spec.ts: drag & drop invoca moveTask del store` | B2 | US-008 | `kanbanStore.moveTask(taskId, newStatus)` es llamado al soltar tarjeta |
| `KanbanView.spec.ts: mover a BLOCKED abre modal con textarea` | B2 | US-008 | Modal visible con textarea validada (min 10 chars) |
| `KanbanView.spec.ts: tarjetas DONE son solo lectura` | B2 | US-008 | Botones deshabilitados en tarjetas con status `DONE` |

---

## 6. Mensaje de Despacho

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `sprint-6/uat-certification`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor).
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md`.

> **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

> 📚 **WORKFLOW DE GOBERNANZA OBLIGATORIO:**
> - Al finalizar, actualiza `.agentic-sync/coverage_matrix.md` según el workflow `.agent/workflows/reconciliacionCoberturaCa.md` — cruzando SSOT, handoff, commit y matriz.
> - Aplica la verificación de correspondencia Gherkin dictada por `.agents/skills/qa_e2e_validation_audit/SKILL.md` §4 — todo componente nuevo debe tener test Vitest correspondiente.
> - Todo cierre se documenta según `.agent/workflows/cierreDeudaTecCriteriosAceptacion.md` — Fase 5 (trazabilidad) y Fase 6 (resumen ejecutivo).
