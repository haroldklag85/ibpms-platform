# 🧠→🎨 Handoff: ARQUITECTO LÍDER → FRONTEND
# T-24-FE: Inyección de data-testid Canónicos para Certificación J-02

**Emitido por:** 🧠 ARQUITECTO LÍDER (Antigravity)
**Destinatario:** 🎨 FRONTEND - VUE
**Fecha:** 2026-05-14T03:34:00-05:00
**Sprint:** 6 — Iteración 7.2
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna (bloqueante para T-24-QA)

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre
cat .cursorrules

# 2. Skill principal del agente Frontend
cat .agents/skills/frontend_build_audit/SKILL.md

# 3. Skills transversales
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. Auditoría de brechas (contexto)
cat .agentic-sync/T-24_UAT_Gap_Analysis.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> `// @Traceability: Testabilidad J-02 (T-24)`. INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

Los tests E2E de QA requieren selectores deterministas (`data-testid`) para interactuar con componentes del Modeler BPMN/DMN y del Workdesk. Actualmente muchos componentes carecen de estos atributos.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Sin `data-testid` en panel propiedades BPMN | `BpmnDesigner.vue` | QA no puede hacer clic en sidebar formKey |
| Sin `data-testid` en grilla DMN | `DmnIntelligence.vue` | QA no puede interactuar con inputs/outputs |
| Sin `data-testid` en filtros Workdesk | `WorkdeskView.vue` | Filtros facetados (BPMN/Kanban) sin selector |
| Sin `data-testid` en paginación | `WorkdeskView.vue` | Botones prev/next sin selector estable |
| Sin `data-testid` en badges SLA | `SlaIndicator.vue` | Semáforos sin ID para aserción cromática |
| Sin `data-testid` en Delegation dropdown | `WorkdeskView.vue` | `toggle-delegation` existe pero inconsistente |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: BpmnDesigner.vue — Panel de Propiedades

**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`

Inyectar `data-testid` en:
```html
<!-- Sidebar de propiedades del elemento seleccionado -->
<div data-testid="bpmn-properties-panel">
  <input data-testid="prop-form-key" ... />      <!-- camunda:formKey -->
  <select data-testid="prop-decision-ref" ... />  <!-- camunda:decisionRef -->
  <input data-testid="prop-element-name" ... />   <!-- nombre del nodo -->
</div>
```

### Paso 2: DmnIntelligence.vue — Grilla de Reglas

**Archivo:** `frontend/src/views/admin/Modeler/DmnIntelligence.vue`

```html
<button data-testid="dmn-add-input">Agregar Input</button>
<button data-testid="dmn-add-output">Agregar Output</button>
<button data-testid="dmn-add-rule">Agregar Regla</button>
<tr data-testid="dmn-rule-row-{index}">...</tr>
<button data-testid="dmn-test-button">🧪 Probar DMN</button>
<button data-testid="dmn-save-button">💾 Guardar</button>
```

### Paso 3: WorkdeskView.vue — Filtros y Paginación

**Archivo:** `frontend/src/views/workdesk/WorkdeskView.vue`

```html
<button data-testid="filter-all">Todos</button>
<button data-testid="filter-bpmn">⚡ BPMN</button>
<button data-testid="filter-kanban">📋 Kanban</button>
<button data-testid="pagination-prev">Anterior</button>
<button data-testid="pagination-next">Siguiente</button>
<span data-testid="pagination-info">Página X de Y</span>
```

### Paso 4: SlaIndicator.vue — Badges

**Archivo:** `frontend/src/components/SlaIndicator.vue` (o componente equivalente)

```html
<span :data-testid="`sla-badge-${color}`" :class="badgeClass">
  {{ remainingTime }}
</span>
```

---

## ✅ Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | ≥15 nuevos `data-testid` inyectados en componentes BPMN/DMN/Workdesk | `grep -r "data-testid" frontend/src/ \| wc -l` |
| 2 | `npm run build` ejecuta sin errores | Exit code 0 |
| 3 | Todos los archivos modificados contienen `// @Traceability: Testabilidad J-02 (T-24)` | `grep -rL "@Traceability" [archivos_modificados]` → 0 |
| 4 | Commit en `sprint-6` | `git log -1 --oneline` |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Leer `.cursorrules` + Skills de Sección 2.
2. Leer `T-24_UAT_Gap_Analysis.md` (sección de violaciones Zero-Mock).
3. Inyectar `data-testid` en BpmnDesigner.vue, DmnIntelligence.vue, WorkdeskView.vue.
4. Compilar: `npm run build` → Build exitoso.
5. Commit: `git add . && git commit -m "feat(testability): inject data-testid for J-02 E2E certification // @Traceability: T-24" && git push origin sprint-6`.

---
