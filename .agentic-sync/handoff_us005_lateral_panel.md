# 🧠→🎨 Handoff: Lead Architect → Frontend Specialist
# T-US005-FE-LATERAL: Rediseño del Panel Lateral de Validación y Simulación (US-005)

**Emitido por:** [🧠 ARQUITECTO LÍDER]
**Destinatario:** [🎨 FRONTEND - VUE]
**Fecha:** 2026-06-06T02:40:00-05:00
**Sprint:** Sprint 6 — Iteración 3
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3, 7)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/frontend_build_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md
cat .agents/skills/addyosmani_sre_discipline/SKILL.md

# 4. ADRs relevantes
cat docs/architecture/adr-002-vue3-microfrontends.md
cat docs/architecture/adr_010_testing_pyramid_governance.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación o comentario `// @Traceability: US-005, CA-[XX]`.
> Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

El componente `BpmnDesigner.vue` debe actualizar su interfaz de validación y simulación:
1. Reemplazar el modal emergente central por un panel lateral derecho (Push Layout) deslizable y redimensionable, que mantiene visible e interactivo el modelador.
2. Ocultar la barra de propiedades de Camunda (`aside.w-80`) cuando el panel lateral de simulación esté visible.
3. Organizar las 3 fases en un acordeón vertical colapsable.
4. Implementar una grilla de variables interactiva CRUD en la sección del simulador para gestionar `localStorage` por `processId`.
5. Ejecutar la simulación con trazado progresivo de halos neón (`highlight-executed`) nodo por nodo con delay de 400ms.

**⚠️ ESTADO DE TESTS:**
He inyectado 5 pruebas unitarias en `BpmnDesigner.spec.ts` para validar estas especificaciones. Actualmente, los tests están **FALLANDO** debido a la falta de variables de estado y elementos DOM. Tu misión es codificar hasta que estas pruebas pasen en verde.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Modal emergente bloqueante | `BpmnDesigner.vue` | Se usa `<div v-if="showSandboxModal" class="fixed inset-0 ...">` que cubre la pantalla. |
| Superposición sobre Properties | `BpmnDesigner.vue` | El panel de propiedades de Camunda no se oculta y genera ruido visual. |
| Lógica de pestañas | `BpmnDesigner.vue` | Se usa navegación de pestañas horizontales en vez de acordeón vertical. |
| Sin grilla de variables | `BpmnDesigner.vue` | Falta una grilla interactiva para gestionar el `localStorage` de variables antes de simular. |
| Simulación no progresiva | `BpmnDesigner.vue` | Los halos se pintan de golpe al finalizar en lugar de ir trazándose en caliente. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Variables reactivas en BpmnDesigner.vue
Declara las siguientes variables reactivas y métodos en la sección setup de `BpmnDesigner.vue`:
```typescript
// @Traceability: US-005, CA-80 - Panel Lateral Resizable
const validationPanelWidth = ref(450);
const isResizingValidation = ref(false);

// @Traceability: US-005, CA-81 - Acordeón colapsable
const collapsedSections = ref({
  linter: false,
  preflight: false,
  simulator: false
});

const toggleSection = (section: 'linter' | 'preflight' | 'simulator') => {
  collapsedSections.value[section] = !collapsedSections.value[section];
};

// @Traceability: US-005, CA-83 - Grilla de variables reactiva
const newGridVarName = ref('');
const newGridVarType = ref('String');
const newGridVarValue = ref('');

const addGridVariable = () => {
  if (!newGridVarName.value) return;
  let val: any = newGridVarValue.value;
  if (newGridVarType.value === 'Number') {
    val = Number(val);
  } else if (newGridVarType.value === 'Boolean') {
    val = val === 'true' || val === true;
  }
  sandboxVariables.value[newGridVarName.value] = val;
  saveVariablesToLocalStorage();
  newGridVarName.value = '';
  newGridVarValue.value = '';
};

const editGridVariable = (key: string, value: any) => {
  sandboxVariables.value[key] = value;
  saveVariablesToLocalStorage();
};

const deleteGridVariable = (key: string) => {
  delete sandboxVariables.value[key];
  saveVariablesToLocalStorage();
};
```

### Paso 2: Resizer nativo (Mouse Drag)
Implementa la lógica del arrastre:
```typescript
// @Traceability: US-005, CA-80 - Resizer del panel lateral
const startResizing = (e: MouseEvent) => {
  isResizingValidation.value = true;
  document.addEventListener('mousemove', handleResizing);
  document.addEventListener('mouseup', stopResizing);
  e.preventDefault();
};

const handleResizing = (e: MouseEvent) => {
  if (!isResizingValidation.value) return;
  const newWidth = window.innerWidth - e.clientX;
  if (newWidth >= 400 && newWidth <= 700) {
    validationPanelWidth.value = newWidth;
  }
};

const stopResizing = () => {
  isResizingValidation.value = false;
  document.removeEventListener('mousemove', handleResizing);
  document.removeEventListener('mouseup', stopResizing);
};
```

### Paso 3: Renderizado Progresivo
Modifica el método `renderTrajectoryHalos` en `BpmnDesigner.vue` para que realice un trazado progresivo:
```typescript
// @Traceability: US-005, CA-84 - Trayectorias progresivas
const renderTrajectoryHalos = async () => {
  if (!modelerInstance) return;
  const canvas = modelerInstance.get('canvas');
  clearTrajectory();
  
  const isTestEnv = typeof window !== 'undefined' && (window as any).__vitest_worker__;
  
  for (const nodeId of executedNodes.value) {
    try {
      canvas.addMarker(nodeId, 'highlight-executed');
      if (!isTestEnv) {
        await new Promise(resolve => setTimeout(resolve, 400));
      }
    } catch (e) {}
  }
};
```

### Paso 4: Plantilla HTML y Estilos
Añadir el panel lateral `<aside>` en `BpmnDesigner.vue` controlado por `showSandboxModal` que oculte el panel de propiedades nativo (`v-show="!showSandboxModal"`).
Asegurar que existan los siguientes identificadores `data-testid` para mantener retrocompatibilidad con tests:
- `data-testid="sandbox-glass-modal"` sobre el lateral panel.
- `data-testid="validation-resizer"` sobre la barra vertical del resizer.
- `data-testid="linter-header"` sobre el botón de colapsar Linter.
- `data-testid="preflight-header"` sobre el botón de colapsar Preflight.
- `data-testid="simulator-header"` sobre el botón de colapsar Simulator.
- `data-testid="linter-level"` sobre el contenido del Linter.
- `data-testid="preflight-level"` sobre el contenido de Preflight.
- `data-testid="sandbox-level"` sobre el contenido del Simulator.
- Inputs de nueva variable grid con v-models a `newGridVarName`, `newGridVarType`, `newGridVarValue`.
- Botón añadir variable con `data-testid="btn-grid-add-variable"`.
- Botón borrar variable con `data-testid="btn-grid-delete-[name]"`.

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Toggle de barra lateral | Al presionar "Validar y simular", el panel lateral derecho se abre empujando el lienzo BPMN. El panel de propiedades de Camunda se oculta. |
| 2 | Resizer reactivo (400px - 700px) | Al arrastrar el borde izquierdo del panel lateral, este cambia su ancho dinámicamente sin congelar el navegador. |
| 3 | Acordeón vertical colapsable | Las secciones de Linter, Pre-Flight y Simulator se colapsan/expanden verticalmente de forma independiente. |
| 4 | Grilla interactiva de variables | Permite ver, añadir, editar y eliminar variables persistiendo en `localStorage` con la clave del proceso. |
| 5 | Trazado progresivo nodo a nodo | Al simular con éxito, los halos verdes se van encendiendo uno a uno secuencialmente en el canvas. |
| 6 | Compilación sin warnings | Ejecutar `npm run build` en la subcarpeta `frontend` en WSL y comprobar éxito. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Ejecutar tests unitarios en WSL para verificar el fallo inicial:
   `npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts`
2. Modificar `BpmnDesigner.vue` de acuerdo a las directrices de panel lateral, resizer, acordeón y grilla.
3. Ejecutar los tests unitarios en WSL para certificar luz verde.
4. Ejecutar el build en WSL (`npm run build`) para verificar la compilación limpia.
5. Commit convencional de tus cambios y push correspondientes.

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de [🎨 FRONTEND - VUE].

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/frontend_build_audit/SKILL.md
3. cat .agents/skills/addyosmani_sre_discipline/SKILL.md
4. cat .agentic-sync/handoff_us005_lateral_panel.md

TU MISIÓN:
1. Ejecuta primero los tests en WSL para confirmar el fallo inicial de los 5 casos de prueba añadidos:
   npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts
2. Reemplazar el modal consolidado por el panel lateral derecho interactivo (Push Layout) con resizing nativo de ratón (400px - 700px) en BpmnDesigner.vue.
3. Ocultar automáticamente el panel de propiedades nativo de Camunda al abrirse y restaurarlo al cerrarse.
4. Organizar las tres capas de validación en acordeón vertical.
5. Implementar la grilla interactiva de variables del LocalStorage y la animación secuencial nodo-a-nodo del token (halos verdes).
6. Certificar que el build compila limpiamente (npm run build) y que la suite BpmnDesigner.spec.ts pasa al 100% en verde.
7. Consolidar mediante git commit y push a la rama del sprint.

REGLAS INQUEBRANTABLES:
- Prohibido usar alert() o confirm() del DOM.
- Inyectar // @Traceability en cada fragmento.
- No utilizar git stash.
```
