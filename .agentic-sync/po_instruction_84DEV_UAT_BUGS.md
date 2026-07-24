# 🐛 INFORME FORENSE UAT — 4 Bugs Detectados + Delegación Correctiva
## Iteración 84-DEV-LANE-ROLE (Ronda UAT Humano)
**Fecha:** 2026-07-15 | **Reporta:** PM/PO-IA | **Método:** Código Real (2 Investigadores Forenses)

---

> [!CAUTION]
> ## CLASIFICACIÓN DE BUGS
> De los 4 bugs reportados, **2 son REGRESIONES** causadas por el código de Lane-Role (B-01, B-02) y **2 son PRE-EXISTENTES** descubiertos durante la UAT pero NO causados por esta iteración (B-03, B-04). Los 4 deben corregirse.

---

## 1. DIAGNÓSTICO POR BUG

### 🔴 B-01: Lane Inputs Bloqueados — Re-render Loop (REGRESIÓN)
**Severidad:** CRÍTICA | **Origen:** Código Lane-Role (MS-4)

**Síntoma:** Al hacer clic en un Lane y tratar de escribir en "Nombre del Lane" o "Actor/Participante", el campo parece bloqueado, la página se refresca y el navegador muestra 159 warnings.

**Causa Raíz — TRIPLE BUG compuesto:**

| # | Problema | Archivo | Línea | Detalle |
|---|---------|---------|:-----:|---------|
| 1 | `:value` lee de propiedad **INEXISTENTE** | [BpmnDesigner.vue](file:///c:/Users/USER/Desktop/Proyectos/Harold Ibpms/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.vue) | L787, L802 | `:value="selectedElement.businessObject?.name"` → `selectedElement` es un objeto plano `{id, type, name, props}` — **NO tiene `businessObject`** → siempre resuelve a `''` → el campo se vacía en cada re-render |
| 2 | `@input` dispara en **cada tecla** | BpmnDesigner.vue | L788, L803 | `@input="syncElementProperties('name', ...)"` → llama `modeling.updateProperties()` → dispara `commandStack.changed` → ejecuta linter, validador, scanner en CADA TECLA |
| 3 | Sin **buffer de estado local** | BpmnDesigner.vue | L787-805 | Usa `:value` (one-way binding) en vez de `v-model` (two-way). Los otros paneles (Task, Gateway) usan `v-model="selectedElement.name"` que persiste el valor localmente |

**Contraste con el panel de Task (QUE FUNCIONA BIEN):**
```html
<!-- Task panel (L510) — FUNCIONA ✅ -->
<input type="text"
  v-model="selectedElement.name"
  @input="syncElementProperties('name', selectedElement.name)"
  class="w-full text-xs border-gray-300 ..." />

<!-- Lane panel (L787) — ROTO ❌ -->
<input type="text"
  :value="selectedElement.businessObject?.name || ''"
  @input="syncElementProperties('name', $event.target.value)" />
```

**Corrección exacta:**

**Archivo:** `BpmnDesigner.vue`

**Para el campo Nombre del Lane (L783-791):**
```html
<!-- ANTES (ROTO) -->
<input id="lane-name" type="text" class="form-control"
  :value="selectedElement.businessObject?.name || ''"
  @input="syncElementProperties('name', $event.target.value)" />

<!-- DESPUÉS (CORREGIDO) -->
<input id="lane-name" type="text"
  class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded focus:ring-indigo-500 focus:border-indigo-500 p-2 border"
  v-model="selectedElement.name"
  @change="syncElementProperties('name', selectedElement.name)"
  data-testid="lane-name-input" />
```

**Para el campo Actor/Participante (L797-805):** Se necesita:
1. En el handler `selection.changed` (L3118-3150), almacenar `camunda:assignee` en `selectedElement.props`:
   ```typescript
   selectedElement.value = {
     id: shape.id, type: shape.type, name: bo.name || '',
     props: {
       ...existingProps,
       assignee: bo.get('camunda:assignee') || '',     // ← AGREGAR
       candidateGroups: bo.get('camunda:candidateGroups') || '' // ← AGREGAR
     }
   };
   ```
2. Cambiar el input a:
   ```html
   <input id="lane-actor" type="text"
     class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border"
     v-model="selectedElement.props.assignee"
     @change="syncElementProperties('camunda:assignee', selectedElement.props.assignee)"
     data-testid="lane-actor-input" />
   ```

**Para el dropdown de Rol RBAC (L808-826):**
   ```html
   <select id="lane-linked-role"
     class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border"
     v-model="selectedElement.props.candidateGroups"
     @change="syncElementProperties('camunda:candidateGroups', selectedElement.props.candidateGroups)"
     data-testid="lane-linked-role-select">
   ```

---

### 🟡 B-02: Panel Lane con Estilos Rotos (REGRESIÓN)
**Severidad:** ALTA | **Origen:** Código Lane-Role (MS-4)

**Síntoma:** Panel de propiedades del Lane se ve desalineado, desorganizado, sin padding, sin bordes.

**Causa Raíz:** El panel fue codificado con **clases CSS de Bootstrap** (`form-group`, `form-control`, `badge badge-success`) en un proyecto que usa **Tailwind CSS exclusivamente**. Ninguna de estas clases existe en el proyecto → **CERO estilos aplicados**.

| Clase Usada (Bootstrap) | ¿Existe en el proyecto? | Clase Correcta (Tailwind) |
|--------------------------|:-----------------------:|--------------------------|
| `properties-panel-content` | ❌ NO | `space-y-5` |
| `panel-section-title` | ❌ NO | `text-sm font-bold text-gray-800 dark:text-gray-200 mb-3` |
| `form-group` | ❌ NO | `p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow-sm` |
| `form-control` | ❌ NO | `w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border` |
| `badge badge-success` | ❌ NO | `px-2 py-1 text-xs rounded-full bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300` |
| `badge badge-warning` | ❌ NO | `px-2 py-1 text-xs rounded-full bg-amber-100 text-amber-800 dark:bg-amber-900 dark:text-amber-300` |
| `lane-link-badge` | ❌ NO | `mt-2 flex items-center gap-2` |

**Corrección:** Reescribir TODO el HTML del panel Lane (L774-838) usando las mismas clases Tailwind que usan los otros paneles (Task L507-540, ServiceTask L651-700, DMN L718-760). El código exacto está en la corrección de B-01 (las clases CSS correctas ya están incluidas arriba).

---

### 🟡 B-03: Errores API 404 (`/instances`, `/external-task-topics`) (PRE-EXISTENTE)
**Severidad:** MEDIA | **Origen:** Bug pre-existente — NO es regresión de Lane-Role

**Causa Raíz — Mismatch de URLs entre Frontend y Backend:**

| Error | Frontend Llama | Backend Expone | Problema |
|-------|---------------|----------------|----------|
| `/instances` 404 | `GET /api/v1/design/processes/{id}/instances` | `GET /api/v1/design/processes/{key}/instances/migratable` | Falta sufijo `/migratable` + query params |
| `/external-task-topics` 404 | `GET /api/v1/design/external-task-topics` | `GET /api/v1/design/processes/external-task-topics` | Falta segmento `/processes` |

**Corrección:**

| Archivo | Línea | Cambio |
|---------|:-----:|--------|
| [InstancesManager.vue](file:///c:/Users/USER/Desktop/Proyectos/Harold Ibpms/ibpms-platform/frontend/src/views/admin/Modeler/InstancesManager.vue) | L114 | Cambiar URL a incluir `/migratable` y agregar query params `sourceVersion`/`targetVersion` |
| [apiClient.ts](file:///c:/Users/USER/Desktop/Proyectos/Harold Ibpms/ibpms-platform/frontend/src/services/apiClient.ts) | L333 | Cambiar `/design/external-task-topics` → `/design/processes/external-task-topics` |
| [useIntegrationStore.ts](file:///c:/Users/USER/Desktop/Proyectos/Harold Ibpms/ibpms-platform/frontend/src/stores/useIntegrationStore.ts) | L86 | Cambiar `/design/external-task-topics` → `/design/processes/external-task-topics` |

---

### 🔴 B-04: FormDesigner Lienzo en Blanco (PRE-EXISTENTE)
**Severidad:** CRÍTICA | **Origen:** Bug pre-existente — NO es regresión de Lane-Role

**Síntoma:** Al abrir `/admin/modeler/forms/designer?id=qa_form_complex_schema`, el formulario guardado no se carga, muestra "Crear Nuevo Formulario" y toast "Error cargando formulario remoto desde API".

**Causa Raíz — DOBLE BUG:**

| # | Problema | Archivo | Línea | Detalle |
|---|---------|---------|:-----:|---------|
| 1 | **No existe endpoint `GET /{formId}`** en el backend | [FormDesignController.java](file:///c:/Users/USER/Desktop/Proyectos/Harold Ibpms/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/FormDesignController.java) | L28 | El controller tiene `POST /{id}` (update) pero NO `GET /{id}` (read). Spring devuelve **405 Method Not Allowed** porque reconoce el path `/{id}` pero solo acepta POST |
| 2 | `showPatternModal` nunca se cierra en modo edición | [FormDesigner.vue](file:///c:/Users/USER/Desktop/Proyectos/Harold Ibpms/ibpms-platform/frontend/src/views/admin/Modeler/FormDesigner.vue) | L1104 | `showPatternModal = ref(true)` — inicializado como `true`. No hay código que lo ponga a `false` cuando el form carga exitosamente. Solo se pone `false` dentro de `selectPattern()` (L1503) |

**Endpoints que SÍ existen en FormDesignController:**
| Método | Ruta | Función |
|--------|------|---------|
| `GET /{technicalName}/versions/{version}` | L60 | Obtener versión específica |
| `GET /{id}/versions` | L70 | Listar versiones |
| `POST` (root) | L84 | Crear formulario |
| `POST /{id}` | L95 | Actualizar formulario |
| `DELETE /{id}` | L109 | Eliminar formulario |
| ❌ `GET /{id}` | — | **NO EXISTE** — ESTE ES EL BUG |

**Corrección:**

1. **Backend:** Agregar endpoint `GET /{technicalName}` en `FormDesignController.java`:
   ```java
   @GetMapping("/{technicalName}")
   public ResponseEntity<FormDesignDTO> getForm(@PathVariable String technicalName) {
       // Buscar por technicalName, retornar la versión más reciente
   }
   ```

2. **Frontend:** En `FormDesigner.vue`, después de un `fetchForm` exitoso (L1258-1269), agregar:
   ```typescript
   if (res.success) {
       showPatternModal.value = false; // Cerrar modal en modo edición
   }
   ```

**El error 409 del heartbeat** es ruido: es un heartbeat del BpmnDesigner que corre en background de un tab previo. No está relacionado con FormDesigner.

---

## 2. RESUMEN Y PRIORIZACIÓN

| Bug | Tipo | Severidad | Archivos a Modificar | Complejidad |
|-----|------|:---------:|---------------------|:-----------:|
| **B-01** | 🔴 REGRESIÓN | CRÍTICA | `BpmnDesigner.vue` (Lane panel + selection handler) | Media |
| **B-02** | 🟡 REGRESIÓN | ALTA | `BpmnDesigner.vue` (mismo panel, solo HTML/CSS) | Baja |
| **B-03** | ⚠️ PRE-EXISTENTE | MEDIA | `InstancesManager.vue`, `apiClient.ts`, `useIntegrationStore.ts` | Baja |
| **B-04** | ⚠️ PRE-EXISTENTE CRÍTICO | CRÍTICA | `FormDesignController.java` (backend) + `FormDesigner.vue` (frontend) | Media |

### Secuencia de Ejecución Recomendada

```
MC-A: Frontend (B-01 + B-02) ─────────┐
                                        ├──→ Verificación → UAT Humano Ronda 2
MC-B: Frontend (B-03) ────────────────┘
MC-C: Backend + Frontend (B-04) ──────┘
[MC-A y MC-B en PARALELO, MC-C puede ser paralelo si toca archivos distintos]
```

---

## 3. BLAST RADIUS ESTRICTO

### ✅ AUTORIZADO MODIFICAR

| Archivo | Bugs | Zona Exacta |
|---------|------|-------------|
| `BpmnDesigner.vue` | B-01, B-02 | L773-838 (panel Lane), L3118-3150 (selection handler) |
| `InstancesManager.vue` | B-03 | L114 (URL de API) |
| `apiClient.ts` | B-03 | L333 (URL external-task-topics) |
| `useIntegrationStore.ts` | B-03 | L86 (URL external-task-topics) |
| `FormDesignController.java` | B-04 | Agregar método GET después de L60 |
| `FormDesigner.vue` | B-04 | L1258-1269 (onMounted) |

### 🚫 INTOCABLE (Prohibido modificar)

| Archivo | Razón |
|---------|-------|
| `syncElementProperties()` en BpmnDesigner.vue | Función genérica que funciona bien — el problema es cómo se LLAMA, no la función en sí |
| Todas las entidades JPA existentes | Sin impacto en BD |
| `router/index.ts` | Sin rutas nuevas |
| Todos los archivos de Lane-Role backend | No son la causa de estos bugs |
