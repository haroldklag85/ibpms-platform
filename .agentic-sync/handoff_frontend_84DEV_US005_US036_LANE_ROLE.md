# 📦 Handoff Frontend — Iteración 84-DEV-LANE-ROLE
# Micro-Sprint 4 + 5: Panel de Propiedades Lane (BPMN Modeler) + Integración RBAC (Identity Governance)

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | `84-DEV-LANE-ROLE` |
| **US** | US-005 (Extensión: Lane Actor Assignment) + US-036 (Extensión: RBAC Lane Integration) |
| **CAs** | Extensión funcional aprobada por PO — No son CAs numerados existentes |
| **Rama Git** | `feature/lane-role-assignment` |
| **Agente** | Frontend |
| **Dependencias** | ✅ Backend completado y pusheado (MS-2 + MS-3 deben estar mergeados primero) |
| **SSOT** | `docs/requirements/v1_user_stories_index.md` → Epic B (US-005) + Epic E (US-036) |
| **Flujo de Trabajo** | Infra/BD → Backend → **Frontend** → QA |
| **API Contracts** | `docs/sprints/gobernanza_pm/API_CONTRACTS.md` — Sección 5.9 Lane Management |

---

## 2. Alineación Arquitectónica y ADRs

### ADRs Aplicables

| ADR | Impacto |
|-----|--------|
| ADR-002 (Vue 3 & Micro-Frontends) | Composition API + `<script setup>`. SFC `.vue`. No business logic en UI. |
| ADR-011 (Local CQRS) | DTOs de lectura (BpmnLaneDTO) separados de escritura (LaneRoleAssignmentRequest). |

### Confirmación de Stack
- **Frontend:** Vue 3 + Vite + Pinia + Axios + TypeScript
- **Prohibiciones:** No mocks, no mockAdapter, no JSON estáticos, no business logic en Vue

### Trazabilidad
MS-4 (panel de propiedades Lane) reutiliza la función existente `syncElementProperties(key, value)` en `BpmnDesigner.vue:4244`, cumpliendo el principio DRY. MS-5 (integración RBAC) extiende la sección existente "Definición BPMN" en `IdentityGovernance.vue` (líneas 617-640) sin reescribir la funcionalidad existente de I/E a nivel proceso. Decisiones de UX confirmadas por PO (Opción C para UI Lane, Opción B para I/E granular).

---

## 3. Rutas Exactas y Contexto Preexistente

### Archivos a MODIFICAR

| Archivo | Ruta | Líneas de Interés | Acción |
|---------|------|-------------------|--------|
| `BpmnDesigner.vue` | `frontend/src/views/admin/Modeler/BpmnDesigner.vue` | 504-775 (panel propiedades), 773 (fallback "No hay propiedades editables"), 3046 (selection.changed handler), 4244 (syncElementProperties) | Insertar bloque `v-else-if` ANTES de línea 773 |
| `IdentityGovernance.vue` | `frontend/src/views/admin/Security/IdentityGovernance.vue` | 583-710 (modal edición rol), 617-640 (tabla Concesiones Zod) | Extender sección con vista jerárquica |
| `rbacStore.js` | `frontend/src/stores/rbacStore.js` | 246 (fetchSystemProcesses), 253+ (zona de inserción) | Agregar 3 funciones nuevas |
| `api-schema.d.ts` | `frontend/src/types/api-schema.d.ts` | Final del archivo | Agregar tipos TS |

### Contexto Preexistente

**`BpmnDesigner.vue`** (4,468 líneas):
- **Línea 773:** `<div v-else>` con mensaje "No hay propiedades editables para este tipo de elemento." — Este es el fallback genérico. El nuevo panel de Lane debe ir ANTES de este fallback.
- **Línea 3046:** Handler `selection.changed` — ya captura `element.type`, `element.id` y `element.businessObject.name`. Reutilizar para detectar `bpmn:Lane` y `bpmn:Participant`.
- **Línea 4244:** `syncElementProperties(key, value)` — función genérica que escribe CUALQUIER propiedad al XML BPMN via la API de bpmn-js. LISTA PARA USAR con Lanes.

**`IdentityGovernance.vue`** (1,470 líneas):
- **Líneas 617-640:** Tabla plana "Matriz de Concesiones Zod (CA-4)" con procesos y checkboxes I/E por proceso. EXTENDER esta tabla para que los procesos sean expandibles y muestren sus lanes como filas hijas.

**`rbacStore.js`** (347 líneas):
- **Línea 246:** `fetchSystemProcesses()` — llama `GET /api/v1/design/processes`. Ya carga procesos del sistema.
- **roles** — verificar que existe un array reactivo de roles. Si `fetchRoles()` no existe, crearla.

---

## 4. Snippets Prescriptivos

### MS-4: Panel de Propiedades Lane en BpmnDesigner.vue

**T4.1 — Bloque `v-else-if` para Lane/Participant** (insertar ANTES de línea 773):

```vue
<!-- INICIO: Panel de Propiedades Lane (US-005/US-036 Extension) -->
<div v-else-if="selectedElement && (selectedElement.type === 'bpmn:Lane' || selectedElement.type === 'bpmn:Participant')" class="properties-panel-content">
  <h4 class="panel-section-title">
    <i class="pi pi-users" style="margin-right: 6px;"></i>
    Propiedades del {{ selectedElement.type === 'bpmn:Lane' ? 'Lane' : 'Participante' }}
  </h4>

  <!-- 1. Nombre del Lane -->
  <div class="form-group">
    <label for="lane-name">Nombre del Lane</label>
    <input
      id="lane-name"
      type="text"
      class="form-control"
      :value="selectedElement.businessObject?.name || ''"
      @input="syncElementProperties('name', $event.target.value)"
      placeholder="Ej: Departamento de Contabilidad"
      data-testid="lane-name-input"
    />
  </div>

  <!-- 2. Actor / Participante (descripción libre) -->
  <div class="form-group">
    <label for="lane-actor">Actor / Participante</label>
    <input
      id="lane-actor"
      type="text"
      class="form-control"
      :value="selectedElement.businessObject?.get('camunda:assignee') || ''"
      @input="syncElementProperties('camunda:assignee', $event.target.value)"
      placeholder="Ej: Departamento de Contabilidad"
      data-testid="lane-actor-input"
    />
  </div>

  <!-- 3. Rol Vinculado (Dropdown de roles RBAC existentes) -->
  <div class="form-group">
    <label for="lane-linked-role">Rol RBAC Vinculado</label>
    <select
      id="lane-linked-role"
      class="form-control"
      :value="selectedElement.businessObject?.get('camunda:candidateGroups') || ''"
      @change="syncElementProperties('camunda:candidateGroups', $event.target.value)"
      data-testid="lane-linked-role-select"
    >
      <option value="">— Sin rol vinculado —</option>
      <option
        v-for="role in rbacStore.roles"
        :key="role.id"
        :value="role.name"
      >
        {{ role.name }}
      </option>
    </select>
  </div>

  <!-- 4. Indicador visual de vinculación -->
  <div class="lane-link-badge" data-testid="lane-link-badge">
    <span v-if="selectedElement.businessObject?.get('camunda:candidateGroups')" class="badge badge-success">
      ✅ Rol vinculado: {{ selectedElement.businessObject.get('camunda:candidateGroups') }}
    </span>
    <span v-else class="badge badge-warning">
      ⚠️ Sin rol RBAC vinculado
    </span>
  </div>
</div>
<!-- FIN: Panel de Propiedades Lane -->
```

**T4.2 — Verificación de rbacStore:**
- Verificar que `rbacStore.roles` está disponible como array reactivo.
- Si `fetchRoles()` no existe en rbacStore, agregarlo usando `GET /api/v1/admin/roles` (o el endpoint equivalente existente).
- Invocar `rbacStore.fetchRoles()` al montar el componente si aún no se han cargado.

---

### MS-5: Integración RBAC en IdentityGovernance.vue

**T5.1 — Funciones en `rbacStore.js`** (insertar DESPUÉS de `fetchSystemProcesses()`, línea 253):

```javascript
// US-005/US-036 Extension: Lane-Role Assignment
async function fetchLanesByProcess(processKey) {
    const response = await apiClient.get(`/api/v1/admin/lanes`, {
        params: { processKey }
    });
    return response.data; // List<BpmnLaneDTO>
}

async function saveLaneRoleAssignments(roleId, assignments) {
    await apiClient.put(`/api/v1/admin/roles/${roleId}/lane-assignments`, assignments);
}

async function fetchLaneAssignmentsByRole(roleId) {
    const response = await apiClient.get(`/api/v1/admin/roles/${roleId}/lane-assignments`);
    return response.data; // List<LaneRoleAssignmentDTO>
}
```

> ⚠️ Asegúrate de que estas funciones se expongan en el `return` del store (si usa Composition API con `defineStore` + `setup`).

**T5.2 — Vista Jerárquica Proceso→Lanes en el modal de edición de rol:**

Extender la sección "Definición BPMN" (líneas 617-640) para que sea una tabla jerárquica expandible:

```
┌───────────────────────┬───────────┬───────────┐
│ DEFINICIÓN BPMN       │ I (INIT)  │ E (EXEC)  │
├───────────────────────┼───────────┼───────────┤
│ ▸ Proceso_Siniestros  │    ☑      │    ☑      │  ← proceso (existente)
│   └ Contabilidad      │    ☐      │    ☑      │  ← lane (NUEVO)
│   └ Aprobación        │    ☑      │    ☐      │  ← lane (NUEVO)
│   └ Archivo           │    ☐      │    ☑      │  ← lane (NUEVO)
│ ▸ Crédito_Hipotecario │    ☑      │    ☐      │  ← proceso
│   └ Analista_Riesgos  │    ☐      │    ☑      │  ← lane (NUEVO)
│   └ Gerencia          │    ☑      │    ☑      │  ← lane (NUEVO)
└───────────────────────┴───────────┴───────────┘
```

**Reglas de UX (Decisiones PO — NO negociables):**
- Los procesos son filas principales con icono ▸/▾ para expandir/colapsar
- Los lanes son filas hijas, indentadas, con estilo visual diferenciado (fondo más claro, borde izquierdo coloreado)
- Los checkboxes I/E de los lanes son INDEPENDIENTES de los del proceso padre
- Agregar un icono de lane (≡ o similar) junto al nombre de cada lane
- Si un proceso no tiene lanes, mostrar texto sutil: "Sin lanes definidos"
- Al abrir el modal en modo edición, cargar las asignaciones existentes del rol via `fetchLaneAssignmentsByRole(roleId)`

**T5.3 — Guardado:**
En la función de guardado del modal (`consolidarRol` o equivalente):
1. Recopilar las asignaciones lane-rol del formulario
2. Llamar `saveLaneRoleAssignments(roleId, assignments)` junto con el guardado existente de `process-permissions`
3. NO romper la funcionalidad existente de I/E a nivel proceso

**T5.4 — Tipos TypeScript** (agregar al final de `api-schema.d.ts`):

```typescript
// US-005/US-036 Extension: Lane-Role Assignment Types
export interface BpmnLaneDTO {
  id: string; // UUID
  processKey: string;
  laneXmlId: string;
  laneName: string;
  actorDescription: string | null;
  linkedRoleName: string | null;
}

export interface LaneRoleAssignmentDTO {
  laneId: string; // UUID
  laneName: string;
  processKey: string;
  canInitiate: boolean;
  canExecute: boolean;
}

export interface LaneRoleAssignmentRequest {
  laneId: string; // UUID
  canInitiate: boolean;
  canExecute: boolean;
}
```

---

## 5. Matriz de QA y Testing Atómico

| Test ID | Validación | CA | Aserción Esperada |
|---------|-----------|-----|-------------------|
| FE-01 | `npm run build` exitoso | — | Build sin errores |
| FE-02 | Seleccionar Lane en canvas → panel derecho muestra 3 campos | MS-4 | Panel renderiza con nombre, actor, dropdown |
| FE-03 | Escribir nombre de Lane → se actualiza en XML BPMN | MS-4 | `syncElementProperties('name', value)` invocado |
| FE-04 | Dropdown carga roles reales del sistema | MS-4 | No hay datos mocked, roles vienen de rbacStore |
| FE-05 | Editar rol → sección BPMN muestra procesos expandibles con lanes | MS-5 | Lanes visibles al expandir proceso |
| FE-06 | Checkboxes I/E por lane funcionan y persisten al guardar | MS-5 | PUT /lane-assignments se invoca al guardar |
| FE-07 | I/E a nivel proceso NO se rompe | Regresión | Funcionalidad existente intacta |

Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

---

## 6. Mensaje de Despacho

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes.

> 📝 **POLÍTICA ANTIAMNESIA:** Antes de iniciar, lee `docs/architecture/arquitecturar.md` y `docs/architecture/adr-002-vue3-microfrontends.md`. NO asumas cómo funciona el frontend — léelo.

> 📋 **DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:**
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md`.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.**
> 3. Guarda tu solicitud de revisión en `.agentic-sync/approval_request_FRONTEND.md`.
> 4. Dile al Humano: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_FRONTEND.md`. Ve al chat del Arquitecto Líder y regrésame su respuesta formal."*
> 5. Espera el veredicto del Arquitecto. Si aprueba, pasa a `EXECUTION`.
> 6. Actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` antes del commit final.
> 7. `git commit` y `git push` en la rama `feature/lane-role-assignment`. PROHIBIDO git stash.

> 📚 **SKILLS OBLIGATORIOS:**
> - `.agents/skills/tdd_first/SKILL.md`
> - `.agents/skills/clean_code_standards/SKILL.md`

### Archivos INTOCABLES (Blast Radius = 0)
- `Workdesk.vue`, `Login.vue`, `FormDesigner.vue`, `FormRenderer`
- `router/index.ts`, `docker-compose.yml`
- Todas las migraciones Liquibase existentes
- Tests E2E existentes (J-02, J-04)
- Funcionalidad existente de I/E a nivel proceso en IdentityGovernance.vue
