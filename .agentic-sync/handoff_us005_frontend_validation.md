# 🧠→🎨 Handoff: Lead Architect → Frontend Specialist
# T-US005-FE: Integración del Embudo de Validación de 3 Niveles y Simulación Interactiva

**Emitido por:** [🧠 ARQUITECTO LÍDER]
**Destinatario:** [🎨 FRONTEND - VUE]
**Fecha:** 2026-06-06T00:35:00-05:00
**Sprint:** Sprint 6 — Iteración 3
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna (El Backend ya implementa soporte para variables y retorno HTTP 422)

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
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
> la anotación @Traceability o comentario `// @Traceability: US-005, CA-XX`.
> Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto
El botón original "Probar en Sandbox" dispara una simulación directa en el motor sin pre-flight integrado ni validación estructurada. Se requiere unificar el Linter estructural del frontend, el Pre-Flight semántico del backend, y la Simulación interactiva en un solo flujo en cascada paralela resguardado por validaciones duras y soporte para captura de variables faltantes del proceso.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Simulación sin pre-flight | `BpmnDesigner.vue`:2832 | `runSandbox` ejecuta simulación en motor directamente ignorando fallos del pre-flight. |
| Sin captura de variables | `BpmnDesigner.vue`:2838 | Si el motor retorna variables faltantes, la simulación falla sin popup interactivo. |
| Sin halos de tokens | `BpmnDesigner.vue`:2840 | La simulación no resalta la trayectoria de ejecución en el lienzo de bpmn-js. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Configurar Variables Reactivas en el Modeler
**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`

Agregar en la sección de setup las siguientes referencias reactivas:
```typescript
// @Traceability: US-005, CA-80, CA-81, CA-82, CA-83, CA-84 - ADR-001
const showSandboxModal = ref(false);
const sandboxStage = ref('linter'); // 'linter' | 'preflight' | 'sandbox'
const preFlightErrors = ref<string[]>([]);
const preFlightWarnings = ref<string[]>([]);
const sandboxBlocked = ref(false);
const showVariablePopup = ref(false);
const missingVariableName = ref('');
const tempVariableValue = ref('');
const sandboxVariables = ref<Record<string, any>>({});
const executedNodes = ref<string[]>([]);
const isSimulating = ref(false);
const simulationLogs = ref<string[]>([]);
```

### Paso 2: Implementar Métodos de Flujo de Validación y Simulación
**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`

Reemplazar `runSandbox` y añadir métodos de persistencia, halos y cascada:
```typescript
// @Traceability: US-005, CA-80, CA-81, CA-82, CA-83, CA-84 - Add validation funnel
const openValidationAndSimulation = () => {
  showSandboxModal.value = true;
  sandboxStage.value = 'linter';
  preFlightErrors.value = [];
  preFlightWarnings.value = [];
  sandboxBlocked.value = false;
  simulationLogs.value = [];
  loadVariablesFromLocalStorage();
  runValidationFunnel();
};

const runPreFlightBackend = async () => {
  if (!modelerInstance) return;
  try {
    const { xml } = await modelerInstance.saveXML({ format: true });
    const { data } = await integrationStore.validateProcess({ xml });
    if (data && data.errors && data.errors.length > 0) {
      preFlightErrors.value = data.errors.map((e: any) => e.message || e);
    }
    if (data && data.warnings && data.warnings.length > 0) {
      preFlightWarnings.value = data.warnings.map((w: any) => w.message || w);
    }
  } catch (err: any) {
    if (err.response && err.response.data && err.response.data.errors) {
      preFlightErrors.value = err.response.data.errors.map((e: any) => e.message || e);
    } else {
      preFlightErrors.value = ['Error al conectar con el Pre-Flight Analyzer en backend.'];
    }
  }
};

const runValidationFunnel = async () => {
  isSimulating.value = true;
  simulationLogs.value.push('⏳ Iniciando embudo de validaciones...');
  
  // Ejecución en paralelo
  await Promise.all([
    runClientLinter(),
    runPreFlightBackend()
  ]);
  
  evaluateBlockingSelectivo();
  isSimulating.value = false;
};

const evaluateBlockingSelectivo = () => {
  const hasFatalLinter = linterErrors.value.some(err => err.toLowerCase().includes('error') || err.toLowerCase().includes('linter:'));
  const hasFatalPreFlight = preFlightErrors.value.length > 0;
  
  if (hasFatalLinter || hasFatalPreFlight) {
    sandboxBlocked.value = true;
    simulationLogs.value.push('❌ Simulación bloqueada: Corrige los errores estructurales fatales.');
  } else {
    sandboxBlocked.value = false;
    simulationLogs.value.push('✅ Validación superada. Sandbox listo para simulación.');
  }
};

const startSimulation = async () => {
  if (sandboxBlocked.value) return;
  isSimulating.value = true;
  simulationLogs.value.push('🚀 Spawning sandbox instance...');
  
  try {
    const { xml } = await modelerInstance.saveXML({ format: true });
    const response = await integrationStore.spawnSandbox({
      xml,
      variables: sandboxVariables.value
    });
    
    const data = response.data;
    if (data && data.status === 'SIMULATION_COMPLETE') {
      executedNodes.value = data.executedNodeIds || [];
      simulationLogs.value.push('✅ Simulación completada exitosamente.');
      showToast('✅ Sandbox (CA-41): Simulación completada exitosamente.', 'success');
      showSandboxModal.value = false;
      renderTrajectoryHalos();
    }
  } catch (err: any) {
    isSimulating.value = false;
    if (err.response && err.response.status === 422) {
      const errorData = err.response.data;
      if (errorData.error === 'MISSING_VARIABLE') {
        missingVariableName.value = errorData.variableName;
        showVariablePopup.value = true;
        simulationLogs.value.push(`⚠️ Simulación pausada: falta la variable '${errorData.variableName}'`);
        return;
      }
    }
    const msg = err.response?.data?.message || err.response?.data?.detail || 'Fallo en la simulación del Sandbox';
    showToast(msg, 'error');
    simulationLogs.value.push(`❌ Error: ${msg}`);
  }
  isSimulating.value = false;
};

const submitVariable = async () => {
  if (missingVariableName.value && tempVariableValue.value) {
    sandboxVariables.value[missingVariableName.value] = tempVariableValue.value;
    saveVariablesToLocalStorage();
    showVariablePopup.value = false;
    tempVariableValue.value = '';
    // Reintentar simulación
    await startSimulation();
  }
};

const saveVariablesToLocalStorage = () => {
  const key = `ibpms_sandbox_variables_${processId.value}`;
  localStorage.setItem(key, JSON.stringify(sandboxVariables.value));
};

const loadVariablesFromLocalStorage = () => {
  const key = `ibpms_sandbox_variables_${processId.value}`;
  const saved = localStorage.getItem(key);
  if (saved) {
    try {
      sandboxVariables.value = JSON.parse(saved);
    } catch (e) {
      sandboxVariables.value = {};
    }
  } else {
    sandboxVariables.value = {};
  }
};

const renderTrajectoryHalos = () => {
  if (!modelerInstance) return;
  const canvas = modelerInstance.get('canvas');
  executedNodes.value.forEach(id => {
    try {
      canvas.addMarker(id, 'highlight-executed');
    } catch (e) {}
  });
};

const clearTrajectory = () => {
  if (!modelerInstance) return;
  const canvas = modelerInstance.get('canvas');
  executedNodes.value.forEach(id => {
    try {
      canvas.removeMarker(id, 'highlight-executed');
    } catch (e) {}
  });
  executedNodes.value = [];
  showToast('🧹 Trayectoria del sandbox limpia.', 'success');
};
```

Exponer en el return de Vue:
```typescript
return {
  // ...
  showSandboxModal,
  sandboxStage,
  preFlightErrors,
  preFlightWarnings,
  sandboxBlocked,
  showVariablePopup,
  missingVariableName,
  tempVariableValue,
  sandboxVariables,
  executedNodes,
  openValidationAndSimulation,
  runValidationFunnel,
  runClientLinter,
  runPreFlightBackend,
  evaluateBlockingSelectivo,
  startSimulation,
  submitVariable,
  saveVariablesToLocalStorage,
  loadVariablesFromLocalStorage,
  renderTrajectoryHalos,
  clearTrajectory
};
```

### Paso 3: Renderizar HTML de Modales y Botón Limpiar
**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`

1. Cambiar el botón "Probar en Sandbox" por "Validar y Simular" y agregar el botón de Limpiar:
```html
        <!-- Sandbox CA-80 -->
        <button data-testid="btn-test-sandbox" @click="openValidationAndSimulation" class="bg-amber-500 text-white px-3 py-1.5 rounded-md shadow text-xs font-medium hover:bg-amber-600 flex items-center gap-1 transition">
          🧪 Validar y Simular
        </button>
        <!-- Botón Limpiar CA-84 -->
        <button v-if="executedNodes.length > 0" data-testid="btn-clear-trajectory" @click="clearTrajectory" class="bg-red-500 text-white px-3 py-1.5 rounded-md shadow text-xs font-medium hover:bg-red-600 flex items-center gap-1 transition">
          🗑️ Limpiar Trayectoria
        </button>
```

2. Agregar el Modal Glassmorphic de Validación y Simulación al final de la sección template:
```html
    <!-- ═══════ Modal Consolidado Validación y Sandbox (CA-80) ═══════ -->
    <div v-if="showSandboxModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm" data-testid="sandbox-glass-modal">
      <div class="w-full max-w-2xl bg-white/70 dark:bg-gray-800/70 backdrop-blur-md border border-white/20 dark:border-gray-700/30 rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[85vh]">
        <!-- Cabecera -->
        <div class="px-6 py-4 border-b border-gray-200/50 dark:border-gray-700/50 flex justify-between items-center bg-gray-50/50 dark:bg-gray-900/50">
          <h3 class="text-sm font-bold text-gray-800 dark:text-white flex items-center gap-2">
            🧪 Dashboard de Validación y Simulación
          </h3>
          <button @click="showSandboxModal = false" class="text-gray-500 hover:text-gray-800 dark:text-gray-400 dark:hover:text-white font-bold text-lg">&times;</button>
        </div>

        <!-- Pestañas Navegación Niveles -->
        <div class="flex border-b border-gray-200 dark:border-gray-700 bg-gray-50/30">
          <button @click="sandboxStage = 'linter'" :class="{'border-amber-500 text-amber-600 font-bold bg-white/40': sandboxStage === 'linter'}" class="flex-1 py-3 text-xs text-center border-b-2 border-transparent hover:bg-gray-100/50 transition">
            Nivel 1: Linter Local
          </button>
          <button @click="sandboxStage = 'preflight'" :class="{'border-indigo-500 text-indigo-600 font-bold bg-white/40': sandboxStage === 'preflight'}" class="flex-1 py-3 text-xs text-center border-b-2 border-transparent hover:bg-gray-100/50 transition">
            Nivel 2: Pre-Flight Analyzer
          </button>
          <button @click="sandboxStage = 'sandbox'" :class="{'border-purple-500 text-purple-600 font-bold bg-white/40': sandboxStage === 'sandbox'}" class="flex-1 py-3 text-xs text-center border-b-2 border-transparent hover:bg-gray-100/50 transition">
            Nivel 3: Sandbox Simulator
          </button>
        </div>

        <!-- Contenido Niveles -->
        <div class="p-6 flex-1 overflow-y-auto min-h-0 space-y-4">
          <!-- Nivel 1: Linter -->
          <div v-show="sandboxStage === 'linter'" data-testid="linter-level" class="space-y-3">
            <h4 class="text-xs font-bold uppercase tracking-wider text-gray-400">Indicadores de Diseño Estructural</h4>
            <div v-if="linterErrors.length > 0" class="p-4 bg-red-50/55 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl">
              <ul class="list-disc list-inside text-xs text-red-700 dark:text-red-300 space-y-1">
                <li v-for="(err, i) in linterErrors" :key="i">{{ err }}</li>
              </ul>
            </div>
            <div v-else class="p-4 bg-green-50/50 border border-green-200 rounded-xl text-xs text-green-700">
              ✅ No se detectaron errores estructurales de diseño.
            </div>
          </div>

          <!-- Nivel 2: Pre-Flight -->
          <div v-show="sandboxStage === 'preflight'" data-testid="preflight-level" class="space-y-3">
            <h4 class="text-xs font-bold uppercase tracking-wider text-gray-400">Reglas de Gobernanza en Caliente</h4>
            <div v-if="preFlightErrors.length > 0" class="p-4 bg-red-50/55 border border-red-200 rounded-xl">
              <ul class="list-disc list-inside text-xs text-red-700 space-y-1">
                <li v-for="(err, i) in preFlightErrors" :key="i">{{ err }}</li>
              </ul>
            </div>
            <div v-if="preFlightWarnings.length > 0" class="p-4 bg-amber-50 border border-amber-200 rounded-xl">
              <ul class="list-disc list-inside text-xs text-amber-700 space-y-1">
                <li v-for="(warn, i) in preFlightWarnings" :key="i">{{ warn }}</li>
              </ul>
            </div>
            <div v-if="preFlightErrors.length === 0 && preFlightWarnings.length === 0" class="p-4 bg-green-50/50 border border-green-200 rounded-xl text-xs text-green-700">
              ✅ Validación de políticas de gobernanza superada.
            </div>
          </div>

          <!-- Nivel 3: Sandbox -->
          <div v-show="sandboxStage === 'sandbox'" data-testid="sandbox-level" class="space-y-4">
            <h4 class="text-xs font-bold uppercase tracking-wider text-gray-400">Simulación del Proceso (Runtime)</h4>
            
            <div v-if="sandboxBlocked" class="p-4 bg-red-100 text-red-800 rounded-xl text-xs font-semibold">
              ⚠️ Simulación Inhabilitada: Se detectaron errores fatales en el linter estructural o el pre-flight de gobernanza.
            </div>
            <div v-else class="space-y-3">
              <div class="flex justify-between items-center">
                <span class="text-xs text-gray-600 dark:text-gray-400">El proceso está listo para simulación temporal.</span>
                <button data-testid="btn-run-simulation" @click="startSimulation" :disabled="isSimulating" class="bg-purple-600 hover:bg-purple-700 text-white text-xs font-bold px-4 py-2 rounded-xl transition shadow disabled:opacity-50">
                  ⚡ Iniciar Simulación
                </button>
              </div>

              <!-- Log Consola -->
              <div class="bg-gray-950 dark:bg-black text-gray-200 p-4 rounded-xl font-mono text-[11px] h-40 overflow-y-auto space-y-1 shadow-inner">
                <div v-for="(log, i) in simulationLogs" :key="i">{{ log }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Popup superpuesto de variables faltantes (CA-82) -->
    <div v-if="showVariablePopup" class="fixed inset-0 z-[60] flex items-center justify-center bg-black/60 backdrop-blur-sm">
      <div class="w-full max-w-sm bg-white border border-gray-200 rounded-xl shadow-2xl p-6 space-y-4">
        <h4 class="text-sm font-bold text-gray-800 flex items-center gap-1">
          🔑 Variable Requerida
        </h4>
        <p class="text-xs text-gray-600">
          La compuerta lógica requiere ingresar el valor para: <strong class="font-mono text-purple-700">{{ missingVariableName }}</strong>
        </p>
        <input type="text" v-model="tempVariableValue" class="w-full text-xs border border-gray-300 rounded p-2 outline-none" placeholder="Valor de variable" />
        <div class="flex justify-end gap-2 text-xs font-bold">
          <button @click="showVariablePopup = false" class="bg-gray-200 hover:bg-gray-300 px-3 py-2 rounded-lg transition">Cancelar</button>
          <button data-testid="btn-submit-variable" @click="submitVariable" class="bg-purple-600 hover:bg-purple-700 text-white px-3 py-2 rounded-lg transition">Confirmar y Enviar</button>
        </div>
      </div>
    </div>
```

3. Estilos CSS para el Halo de Trayectoria en `BpmnDesigner.vue`:
```css
/* Trayectoria de Sandbox CA-84 */
.highlight-executed:not(.djs-connection) .djs-visual > :first-child {
  stroke: #10B981 !important;
  stroke-width: 4px !important;
  filter: drop-shadow(0 0 8px #10B981) !important;
  animation: highlight-pulse 2s infinite !important;
}

@keyframes highlight-pulse {
  0% {
    filter: drop-shadow(0 0 2px #10B981);
  }
  50% {
    filter: drop-shadow(0 0 10px #10B981);
  }
  100% {
    filter: drop-shadow(0 0 2px #10B981);
  }
}
```

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Existencia del Botón Unificado y Modal Glassmorphic | Ejecutar test CA-80 en Vitest y corroborar éxito en aserciones de modal. |
| 2 | Ejecución Paralela y Bloqueo Selectivo | Ejecutar test CA-81 en Vitest. Verificar que advertencias de Pre-Flight no bloquean. |
| 3 | Captura de variables HTTP 422 y Re-intento | Ejecutar test CA-82. Comprobar que spawnSandbox se invoca de nuevo con variables. |
| 4 | Persistencia en localStorage | Ejecutar test CA-83. Comprobar que localStorage persiste bajo la clave processKey. |
| 5 | Trazado de Trayectorias (Halos) y Limpieza | Ejecutar test CA-84. Comprobar que se añade/remueve el marcador `highlight-executed`. |
| 6 | Compilación Exitosa del Proyecto | WSL: `npm run build` en carpeta `frontend` termina sin errores de TypeScript/Vite. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Modificar `BpmnDesigner.vue` con la lógica y HTML indicados.
2. Ejecutar la suite de pruebas del frontend en WSL:
   ```bash
   wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend -e npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts
   ```
3. Ejecutar compilación de producción del frontend en WSL:
   ```bash
   wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend -e npm run build
   ```
4. Commit: `git add . && git commit -m "feat(design): implement unified 3-level validation funnel and interactive sandbox simulation (US-005)" && git push`

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de [🎨 FRONTEND - VUE].

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/frontend_build_audit/SKILL.md
3. cat .agents/skills/addyosmani_sre_discipline/SKILL.md
4. cat .agentic-sync/handoff_us005_frontend_validation.md

TU MISIÓN:
1. Implementar la interfaz, lógica reactiva y estilos del modal consolidado glassmorphic de validación y simulación (CA-80 a CA-84) en BpmnDesigner.vue.
2. Asegurar que las variables de simulación se persistan en localStorage por processKey.
3. Ejecutar y pasar la suite de pruebas unitarias BpmnDesigner.spec.ts en Vitest y la compilación exitosa (npm run build).
4. Realizar commit convencional del sprint y subir a origin.

REGLAS INQUEBRANTABLES:
- Prohibido usar alert() o confirm() nativos del DOM (usar variables reactivas y modales Vue).
- Inyectar las etiquetas de trazabilidad // @Traceability en cada fragmento.
- No romper regresión de tests anteriores en BpmnDesigner.spec.ts.
```
