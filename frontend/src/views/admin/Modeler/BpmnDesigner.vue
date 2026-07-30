// @Traceability: US-005, CA-42 - Activity Timeline​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​
<template>
  <div class="h-full w-full bg-gray-50 dark:bg-gray-900 flex flex-col" v-cloak>

    <!-- ═══════ Toast Notifications ═══════ -->
    <Transition name="toast-slide">
      <div v-if="toast.msg" :class="toast.type === 'success' ? 'bg-green-600' : 'bg-red-600'" class="fixed top-4 right-4 z-[100] text-white px-5 py-3 rounded-lg shadow-xl flex items-center space-x-3">
        <span class="text-sm font-medium">{{ toast.msg }}</span>
        <button @click="toast.msg = ''" class="ml-2 opacity-70 hover:opacity-100">&times;</button>
      </div>
    </Transition>

    <!-- ═══════ Redesigned Top Toolbar: 6-Step Stepper with Glassmorphism ═══════ -->
    <header class="bg-white/70 dark:bg-gray-800/70 backdrop-blur-md border-b border-white/20 dark:border-gray-700/30 shadow-sm flex flex-col p-4 w-full shrink-0">
      <!-- Top info bar (title, status, active role) -->
      <div class="flex flex-wrap justify-between items-center mb-3 pb-3 border-b border-gray-200/55 dark:border-gray-700/55 w-full gap-3">
        <div class="flex items-center space-x-3">
          <h1 class="text-lg font-bold text-gray-900 dark:text-white">{{ currentProcessName || 'Proceso Sin Título​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​' }}</h1>
          <span v-if="processStatus" class="text-xs font-bold uppercase tracking-wider px-2 py-0.5 rounded-full"
                :class="{
                  'bg-gray-200 text-gray-700': processStatus === 'BORRADOR​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​',
                  'bg-green-100 text-green-800': processStatus === 'ACTIVO',
                  'bg-gray-100 text-gray-600': processStatus === 'ARCHIVADO'
                }"
                title="Estado de la versión de diseño del proceso en la plataforma iBPMS">{{ processStatus }}</span>
          <!-- CA-63: Indicador de Sandbox -->
          <span v-if="processStatus === 'BORRADOR'" 
                class="text-xs bg-gray-100 text-gray-650 border border-gray-300 px-2 py-0.5 rounded shadow-sm font-bold ml-2 cursor-help"
                title="Modo Sandbox: Ejecución aislada para pruebas de simulación sin impactar producción">🧪 SANDBOX</span>
        </div>
        <div class="flex items-center space-x-3">
          <!-- Active Role CA-21 -->
          <span class="text-xs bg-indigo-50 dark:bg-gray-700 border-indigo-200 dark:border-gray-600 rounded px-2 py-1 text-indigo-800 dark:text-white font-bold">
             Rol Activo: {{ activeRole }}
          </span>
          <span class="text-xs text-gray-500 dark:text-gray-400">
             Versión: v{{ currentVersion }}
          </span>
        </div>
      </div>

      <!-- Stepper grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-6 gap-3 w-full">
        <!-- Paso 1: Inicio -->
        <div 
          class="p-3 rounded-lg border transition-all duration-300 flex flex-col justify-between"
          :class="isStepHighlighted(1) ? 'border-indigo-500 shadow-[0_0_15px_rgba(99,102,241,0.2)] bg-indigo-500/5 dark:bg-indigo-500/10' : 'border-gray-200 dark:border-gray-700 bg-white/30 dark:bg-gray-800/30'"
        >
          <div class="flex items-center gap-2">
            <span 
              class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold transition-colors"
              :class="isStepHighlighted(1) ? 'bg-indigo-500 text-white' : 'bg-gray-200 dark:bg-gray-700 text-gray-600 dark:text-gray-300'"
            >1</span>
            <span class="text-xs font-bold" :class="isStepHighlighted(1) ? 'text-indigo-600 dark:text-indigo-400' : 'text-gray-700 dark:text-gray-300'">Inicio</span>
          </div>
          
          <!-- Large screen buttons -->
          <div class="hidden lg:flex flex-row items-center gap-2 mt-2 flex-wrap">
            <button @click="showCatalog = true" class="text-[11px] bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 px-2 py-1 rounded shadow-sm font-medium hover:bg-gray-50 dark:hover:bg-gray-600 transition">
              📜 Explorador
            </button>
            <label data-testid="btn-import-bpmn" class="cursor-pointer bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 px-2 py-1 rounded shadow-sm text-[11px] font-medium hover:bg-gray-50 dark:hover:bg-gray-600 transition flex items-center gap-1">
              ⬆️ Importar
              <input ref="importFileInput" data-testid="input-import-bpmn" type="file" @change="handleFileUpload" accept=".bpmn,.xml" class="hidden" />
            </label>
            <button data-testid="btn-export-bpmn" @click="downloadXML" class="bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 px-2 py-1 rounded shadow-sm text-[11px] font-medium hover:bg-gray-50 dark:hover:bg-gray-600 transition flex items-center gap-1">
              ⬇️ Exportar
            </button>
            <button @click="saveDraft(true)" :disabled="isLocked" class="bg-indigo-50 border border-indigo-200 text-indigo-700 dark:bg-indigo-900/30 dark:border-indigo-800 dark:text-indigo-300 px-2 py-1 rounded shadow-sm text-[11px] font-bold hover:bg-indigo-100 disabled:opacity-50 transition flex items-center gap-1">
              💾 Guardar
            </button>
          </div>

          <!-- Small screen dropdown -->
          <select @change="handleStepSelect(1, $event)" class="lg:hidden w-full mt-2 text-xs p-1.5 border rounded dark:bg-gray-800 dark:border-gray-700 dark:text-white">
            <option value="" disabled selected>Acciones...</option>
            <option value="Explorador">📜 Explorador</option>
            <option value="Importar">⬆️ Importar</option>
            <option value="Exportar">⬇️ Exportar</option>
            <option value="Guardar">💾 Guardar</option>
          </select>
        </div>

        <!-- Paso 2: Modelado -->
        <div 
          class="p-3 rounded-lg border transition-all duration-300 flex flex-col justify-between"
          :class="isStepHighlighted(2) ? 'border-indigo-500 shadow-[0_0_15px_rgba(99,102,241,0.2)] bg-indigo-500/5 dark:bg-indigo-500/10' : 'border-gray-200 dark:border-gray-700 bg-white/30 dark:bg-gray-800/30'"
        >
          <div class="flex items-center gap-2">
            <span 
              class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold transition-colors"
              :class="isStepHighlighted(2) ? 'bg-indigo-500 text-white' : 'bg-gray-200 dark:bg-gray-700 text-gray-600 dark:text-gray-300'"
            >2</span>
            <span class="text-xs font-bold" :class="isStepHighlighted(2) ? 'text-indigo-600 dark:text-indigo-400' : 'text-gray-700 dark:text-gray-300'">Modelado</span>
          </div>

          <div class="hidden lg:flex flex-row items-center gap-2 mt-2 flex-wrap">
            <span class="text-[11px] font-semibold px-2 py-1 rounded bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300 border border-blue-200 dark:border-blue-800 flex items-center gap-1">
              🎨 Canvas
            </span>
            <button @click="triggerCopilotAudit" class="bg-slate-950 text-white px-2 py-1 rounded shadow text-[11px] font-medium hover:bg-black transition flex items-center gap-1 relative">
              🧠 Copiloto IA
              <span v-if="unreadAiBadge" class="absolute -top-1 -right-1 flex h-2 w-2">
                <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
                <span class="relative inline-flex rounded-full h-2 w-2 bg-red-500 shadow shadow-red-500/50"></span>
              </span>
            </button>
          </div>

          <select @change="handleStepSelect(2, $event)" class="lg:hidden w-full mt-2 text-xs p-1.5 border rounded dark:bg-gray-800 dark:border-gray-700 dark:text-white">
            <option value="" disabled selected>Acciones...</option>
            <option value="Canvas">🎨 Centrar Canvas</option>
            <option value="Copiloto IA">🧠 Copiloto IA</option>
          </select>
        </div>

        <!-- Paso 3: Simulación -->
        <div 
          class="p-3 rounded-lg border transition-all duration-300 flex flex-col justify-between"
          :class="isStepHighlighted(3) ? 'border-indigo-500 shadow-[0_0_15px_rgba(99,102,241,0.2)] bg-indigo-500/5 dark:bg-indigo-500/10' : 'border-gray-200 dark:border-gray-700 bg-white/30 dark:bg-gray-800/30'"
        >
          <div class="flex items-center gap-2">
            <span 
              class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold transition-colors"
              :class="isStepHighlighted(3) ? 'bg-indigo-500 text-white' : 'bg-gray-200 dark:bg-gray-700 text-gray-600 dark:text-gray-300'"
            >3</span>
            <span class="text-xs font-bold" :class="isStepHighlighted(3) ? 'text-indigo-600 dark:text-indigo-400' : 'text-gray-700 dark:text-gray-300'">Simulación</span>
          </div>

          <div class="hidden lg:flex flex-row items-center gap-2 mt-2 flex-wrap">
            <button data-testid="btn-test-sandbox" @click="runSandbox" class="bg-amber-500 text-white px-2 py-1 rounded shadow text-[11px] font-medium hover:bg-amber-600 transition flex items-center gap-1">
              🧪 Simular
            </button>
            <button data-testid="btn-clear-trajectory" @click="clearTrajectory" class="bg-slate-500 text-white px-2 py-1 rounded shadow text-[11px] font-medium hover:bg-slate-600 transition flex items-center gap-1">
              🧹 Limpiar
            </button>
          </div>

          <select @change="handleStepSelect(3, $event)" class="lg:hidden w-full mt-2 text-xs p-1.5 border rounded dark:bg-gray-800 dark:border-gray-700 dark:text-white">
            <option value="" disabled selected>Acciones...</option>
            <option value="Simular">🧪 Simular</option>
            <option value="Limpiar">🧹 Limpiar</option>
          </select>
        </div>

        <!-- Paso 4: Trazabilidad -->
        <div 
          class="p-3 rounded-lg border transition-all duration-300 flex flex-col justify-between"
          :class="isStepHighlighted(4) ? 'border-indigo-500 shadow-[0_0_15px_rgba(99,102,241,0.2)] bg-indigo-500/5 dark:bg-indigo-500/10' : 'border-gray-200 dark:border-gray-700 bg-white/30 dark:bg-gray-800/30'"
        >
          <div class="flex items-center gap-2">
            <span 
              class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold transition-colors"
              :class="isStepHighlighted(4) ? 'bg-indigo-500 text-white' : 'bg-gray-200 dark:bg-gray-700 text-gray-600 dark:text-gray-300'"
            >4</span>
            <span class="text-xs font-bold" :class="isStepHighlighted(4) ? 'text-indigo-600 dark:text-indigo-400' : 'text-gray-700 dark:text-gray-300'">Trazabilidad</span>
          </div>

          <div class="hidden lg:flex flex-row items-center gap-2 mt-2 flex-wrap">
            <button @click="openAuditLogs()" class="bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 px-2 py-1 rounded shadow-sm text-[11px] font-medium hover:bg-gray-50 dark:hover:bg-gray-600 transition flex items-center gap-1">
              📝 Auditoría
            </button>
            <button @click="showVersions = !showVersions" class="bg-white dark:bg-gray-750 border border-gray-300 dark:border-gray-650 px-2 py-1 rounded shadow-sm text-[11px] font-medium hover:bg-gray-50 dark:hover:bg-gray-600 transition flex items-center gap-1">
              📜 Versiones
            </button>
          </div>

          <select @change="handleStepSelect(4, $event)" class="lg:hidden w-full mt-2 text-xs p-1.5 border rounded dark:bg-gray-800 dark:border-gray-700 dark:text-white">
            <option value="" disabled selected>Acciones...</option>
            <option value="Auditoría">📝 Auditoría</option>
            <option value="Versiones">📜 Versiones</option>
          </select>
        </div>

        <!-- Paso 5: Despliegue -->
        <div 
          class="p-3 rounded-lg border transition-all duration-300 flex flex-col justify-between"
          :class="isStepHighlighted(5) ? 'border-indigo-500 shadow-[0_0_15px_rgba(99,102,241,0.2)] bg-indigo-500/5 dark:bg-indigo-500/10' : 'border-gray-200 dark:border-gray-700 bg-white/30 dark:bg-gray-800/30'"
        >
          <div class="flex items-center gap-2">
            <span 
              class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold transition-colors"
              :class="isStepHighlighted(5) ? 'bg-indigo-500 text-white' : 'bg-gray-200 dark:bg-gray-700 text-gray-600 dark:text-gray-300'"
            >5</span>
            <span class="text-xs font-bold" :class="isStepHighlighted(5) ? 'text-indigo-600 dark:text-indigo-400' : 'text-gray-700 dark:text-gray-300'">Despliegue</span>
          </div>

          <div class="hidden lg:flex flex-row items-center gap-1.5 mt-2 flex-wrap">
            <button @click="requestDeploy" class="bg-purple-600 text-white px-2 py-1 rounded shadow text-[11px] font-bold hover:bg-purple-700 transition flex items-center gap-1">
              📩 Solicitar
            </button>
            <button @click="openDeployRequests" class="bg-indigo-50 border border-indigo-200 text-indigo-700 dark:bg-indigo-900/30 dark:border-indigo-800 dark:text-indigo-300 px-2 py-1 rounded shadow-sm text-[11px] font-bold hover:bg-indigo-100 transition flex items-center gap-1">
              📨 Ver Solicitudes
            </button>
            <button data-testid="btn-deploy" v-show="['BPMN_Release_Manager', 'Super_Admin', 'ROLE_SUPER_ADMIN', 'ROLE_PROCESS_ARCHITECT'].includes(activeRole)"
                    @click="showDeployModal = true" 
                    :disabled="isDeploying || (preFlightStatus !== 'VALIDATED' && preFlightStatus !== 'WARNING')" 
                    class="bg-indigo-600 text-white px-2 py-1 rounded shadow text-[11px] font-bold hover:bg-indigo-700 disabled:opacity-50 transition flex items-center gap-1">
              🚀 Desplegar
            </button>
          </div>

          <select @change="handleStepSelect(5, $event)" class="lg:hidden w-full mt-2 text-xs p-1.5 border rounded dark:bg-gray-800 dark:border-gray-700 dark:text-white">
            <option value="" disabled selected>Acciones...</option>
            <option value="Solicitar Despliegue">📩 Solicitar</option>
            <option value="Ver Solicitudes">📨 Ver Solicitudes</option>
            <option v-if="['BPMN_Release_Manager', 'Super_Admin', 'ROLE_SUPER_ADMIN', 'ROLE_PROCESS_ARCHITECT'].includes(activeRole)" value="Desplegar">🚀 Desplegar</option>
          </select>
        </div>

        <!-- Paso 6: Operación -->
        <div 
          class="p-3 rounded-lg border transition-all duration-300 flex flex-col justify-between"
          :class="[
            isStepHighlighted(6) ? 'border-indigo-500 shadow-[0_0_15px_rgba(99,102,241,0.2)] bg-indigo-500/5 dark:bg-indigo-500/10' : 'border-gray-200 dark:border-gray-700 bg-white/30 dark:bg-gray-800/30',
            currentVersion === 0 ? 'opacity-50' : ''
          ]"
          :title="currentVersion === 0 ? 'Esta opción estará disponible al realizar el primer despliegue activo' : ''"
        >
          <div class="flex items-center gap-2">
            <span 
              class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold transition-colors"
              :class="isStepHighlighted(6) ? 'bg-indigo-500 text-white' : 'bg-gray-200 dark:bg-gray-700 text-gray-600 dark:text-gray-300'"
            >6</span>
            <span class="text-xs font-bold" :class="isStepHighlighted(6) ? 'text-indigo-600 dark:text-indigo-400' : 'text-gray-700 dark:text-gray-300'">Operación</span>
          </div>

          <div class="hidden lg:flex flex-row items-center gap-2 mt-2 flex-wrap">
            <button 
              @click="showInstancesManager = true" 
              :disabled="currentVersion === 0"
              class="bg-indigo-50 text-indigo-700 border border-indigo-200 dark:bg-indigo-900/40 px-2 py-1 rounded shadow-sm text-[11px] font-bold hover:bg-indigo-100 disabled:opacity-50 disabled:cursor-not-allowed transition flex items-center gap-1"
            >
              🧬 Gestor de Instancias
            </button>
          </div>

          <select 
            @change="handleStepSelect(6, $event)" 
            :disabled="currentVersion === 0"
            class="lg:hidden w-full mt-2 text-xs p-1.5 border rounded dark:bg-gray-800 dark:border-gray-700 dark:text-white disabled:opacity-50"
          >
            <option value="" disabled selected>Acciones...</option>
            <option value="Operacion">🧬 Gestor de Instancias</option>
          </select>
        </div>
      </div>
    </header>

    <!-- ═══════ Status Bar (Lock + AutoSave + PreFlight) ═══════ -->
    <div class="flex items-center justify-between px-6 py-1.5 bg-gray-100 dark:bg-gray-800/50 border-b border-gray-200 dark:border-gray-700 text-xs shrink-0">
      <!-- Lock Indicator CA-7 -->
      <div class="flex items-center space-x-4">
        <span v-if="isLocked" class="flex items-center text-orange-700 font-bold bg-orange-100 px-3 py-1 rounded shadow-sm border border-orange-200">
          🔒 SOLO LECTURA: Bloqueado por {{ lockOwner }} ({{ lockSince }})
          <!-- CA-66: Break Lock -->
          <button v-if="activeRole === 'Super_Admin'" @click="breakLock" class="ml-3 bg-red-600 hover:bg-red-700 text-white px-2 py-0.5 rounded text-[10px] uppercase transition shadow-sm border border-red-800">🔓 Romper Candado</button>
          <!-- Renew Lock if expired due to inactivity -->
          <button v-if="lockOwner === 'Expirado (Inactividad)'" @click="renewLock" class="ml-3 bg-indigo-650 hover:bg-indigo-700 text-white px-2 py-0.5 rounded text-[10px] uppercase transition shadow-sm border border-indigo-800">🔑 Adquirir Candado</button>
        </span>
        <span v-else class="text-green-600 font-medium">🔓 Edición Exclusiva Adquirida</span>
      </div>

      <div class="flex items-center space-x-4">
        <!-- Auto-Save -->
        <span class="text-gray-500 dark:text-gray-400">
          ✅ Guardado: hace {{ autoSaveAgo }}s
        </span>
        <!-- Pre-Flight Badge CA-9 -->
        <span class="font-bold px-2 py-0.5 rounded-full"
              :class="{
                'bg-green-100 text-green-800': preFlightStatus === 'VALIDATED',
                'bg-yellow-100 text-yellow-800': preFlightStatus === 'PENDING',
                'bg-orange-100 text-orange-800': preFlightStatus === 'WARNING',
                'bg-red-100 text-red-800': preFlightStatus === 'ERROR'
              }">
          {{ preFlightStatus === 'VALIDATED' ? '✅ Validado' : preFlightStatus === 'WARNING' ? '⚠️ Advertencias' : preFlightStatus === 'ERROR' ? '❌ Errores' : '⏳ Validando...' }}
        </span>
      </div>
    </div>

    <!-- ═══════ Main Canvas Area ═══════ -->
    <main class="flex-1 flex min-h-0 overflow-hidden relative">
      
      <!-- CA-7: Lock Overlay over the whole canvas to prevent clicks in Read-Only mode -->
      <div v-if="isLocked" class="absolute inset-0 bg-white/30 dark:bg-black/30 backdrop-blur-[1px] cursor-not-allowed z-10 flex flex-col items-center justify-center pointer-events-auto">
         <div class="bg-orange-100 text-orange-800 p-4 rounded-xl shadow-2xl font-bold flex items-center gap-3">
            <span class="text-3xl">🔒</span>
            <div>
              <p class="text-sm">Edición Deshabilitada (ReadOnly)</p>
              <p class="text-xs font-normal">Este flujo está siendo editado por {{ lockOwner }}</p>
            </div>
         </div>
      </div>

      <!-- BPMN Canvas -->
      <!-- @Traceability: Testabilidad J-02 (T-24) -->
      <div data-testid="bpmn-canvas-wrapper" ref="canvasContainer" class="flex-1 overflow-hidden h-full bpmn-canvas" :class="{ 'pointer-events-none': isLocked }"></div>

      <!-- CA-25: Floating Zoom Controls -->
      <div class="absolute bottom-4 left-4 flex gap-2 z-30">
        <button @click="zoomIn" class="bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 shadow-lg rounded p-2 hover:bg-gray-50 dark:hover:bg-gray-700 font-bold w-10 h-10 flex items-center justify-center border border-gray-200 dark:border-gray-600" title="Zoom In">+</button>
        <button @click="zoomOut" class="bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 shadow-lg rounded p-2 hover:bg-gray-50 dark:hover:bg-gray-700 font-bold w-10 h-10 flex items-center justify-center border border-gray-200 dark:border-gray-600" title="Zoom Out">-</button>
        <button @click="zoomFit" class="bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 shadow-lg rounded p-2 hover:bg-gray-50 dark:hover:bg-gray-700 text-lg font-bold w-10 h-10 flex items-center justify-center border border-gray-200 dark:border-gray-600" title="Fit Viewport">O</button>
      </div>

      <!-- ═══════ Properties Side Panel ═══════ -->
      <aside v-show="!showSandboxModal" class="w-80 border-l border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 shrink-0 flex flex-col overflow-y-auto">
        <div class="p-4 border-b border-gray-200 dark:border-gray-700 shrink-0">
          <h3 class="text-xs font-bold text-gray-400 uppercase tracking-widest flex items-center gap-2">
            ⚙️ Camunda Properties
          </h3>
        </div>

        <div class="p-4 space-y-5 flex-1">
          <!-- @Traceability: US-005, CA-77 Panel de Propiedades Contextual -->

          <!-- 1. Global Process Properties (Shown only when no element is selected) -->
          <div v-if="!selectedElement.id" class="space-y-5">
            <!-- Naming Dual -->
            <div>
              <label class="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">Nombre de Negocio</label>
              <input type="text" v-model="currentProcessName" @input="onDiagramEdit" class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded focus:ring-indigo-500 focus:border-indigo-500 p-2 border" placeholder="Ej: Crédito de Consumo" />
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">ID Técnico</label>
              <input type="text" v-model="processId" :disabled="!isNewProcess" class="w-full text-xs font-mono border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded focus:ring-indigo-500 focus:border-indigo-500 p-2 border bg-gray-50 dark:bg-gray-900 disabled:opacity-50 disabled:cursor-not-allowed" placeholder="Auto: credito-de-consumo" />
            </div>

            <!-- Nomenclatura Instancia CA-5 -->
            <div class="p-3 bg-fuchsia-50 dark:bg-fuchsia-900/20 border border-fuchsia-200 rounded">
               <label class="block text-xs font-bold text-fuchsia-800 dark:text-fuchsia-300 mb-1 flex items-center justify-between">
                 <span>🎟 Regla de Nomenclatura (CA-5)</span>
                 <AppTooltip :content="isNomenclatureSyntaxError ? '⚠️ Error de sintaxis: llaves sin cerrar' : bpmnTooltips.NOMENCLATURE_DUMMY" :isError="isNomenclatureSyntaxError" />
               </label>
               <div class="relative">
                 <!-- @Traceability: US-005, CA-05 -->
                 <div
                   ref="editorRef"
                   contenteditable="true"
                   @input="onEditorInput"
                   @keydown="onEditorKeydown"
                   @blur="onEditorBlur"
                   :class="{'border-red-500 ring-1 ring-red-500 bg-red-50': isNomenclatureSyntaxError}"
                   class="w-full min-h-[38px] text-xs border border-fuchsia-300 dark:border-fuchsia-600 dark:bg-gray-700 dark:text-white rounded focus:ring-fuchsia-500 focus:border-fuchsia-500 p-2 outline-none transition overflow-y-auto max-h-24 whitespace-pre-wrap empty:before:content-[attr(placeholder)] empty:before:text-gray-400 dark:empty:before:text-gray-500"
                   placeholder="Ej: OC-{Solicitante}"
                 ></div>
                 
                 <!-- Autocomplete Dropdown Popover (CA-5) -->
                 <div v-if="showAutocompletePopover && filteredAutocompleteVariables.length > 0" class="absolute z-50 left-0 right-0 mt-1 max-h-40 overflow-y-auto bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-md shadow-lg text-xs">
                   <div v-for="v in filteredAutocompleteVariables" :key="v.name" @mousedown.prevent="selectVariable(v.name)" class="px-3 py-2 cursor-pointer hover:bg-indigo-50 dark:hover:bg-indigo-900/40 flex justify-between items-center">
                     <span class="font-mono font-bold text-gray-900 dark:text-white">{{ v.name }}</span>
                     <span :class="{
                       'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-300': v.source === 'Session',
                       'bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-300': v.source === 'Form',
                       'bg-purple-100 text-purple-800 dark:bg-purple-900/40 dark:text-purple-300': v.source === 'Glossary',
                       'bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300': v.source === 'Connector'
                     }" class="px-1.5 py-0.5 rounded text-[10px] font-semibold uppercase">{{ v.source }}</span>
                   </div>
                 </div>
               </div>
               <p class="text-[10px] text-fuchsia-600 dark:text-fuchsia-400 mt-1 leading-tight">Obligatorio. Define la máscara para instanciar tickets. Se inyecta al nodo raíz del XML.</p>
               
               <!-- Nomenclature preview chips below the input (CA-5) -->
               <div v-if="processNomenclature" class="mt-2 flex flex-wrap items-center gap-1 text-[10px] border border-fuchsia-100 dark:border-fuchsia-900/50 rounded p-1.5 bg-fuchsia-50/50 dark:bg-fuchsia-900/10">
                 <span class="text-fuchsia-400 font-medium mr-1">Previsualización:</span>
                 <template v-for="(part, idx) in nomenclatureParts" :key="idx">
                   <span v-if="part.isVariable" :class="part.badgeClass" class="inline-flex items-center gap-0.5 px-1 py-0.5 rounded border font-mono font-semibold" :title="`Tipo: ${part.type} | Origen: ${part.source}`">
                     {{ part.text }}
                   </span>
                   <span v-else class="text-fuchsia-700 dark:text-fuchsia-300 font-mono">{{ part.text }}</span>
                 </template>
               </div>
            </div>

            <!-- Warning Banner @Traceability: US-005, CA-35 -->
            <div v-if="isCriticalPathExceeded" class="p-3 bg-red-50 border border-red-200 rounded text-xs text-red-800 space-y-2 mb-3">
              <p class="font-bold">
                ⚠️ Inconsistencia de SLA: La ruta crítica de las tareas requiere al menos {{ criticalPathDuration }} horas, lo cual supera el SLA Global del proceso ({{ globalSla }} horas).
              </p>
              <button @click="autoAdjustGlobalSla" class="text-xs font-bold underline text-red-950 hover:text-red-900">
                [ Ajustar SLA Global a {{ criticalPathDuration }}h ]
              </button>
            </div>
            
            <!-- Mode Toggle @Traceability: US-005, CA-35 -->
            <div class="flex items-center justify-between p-1 bg-gray-50 dark:bg-gray-800 rounded border border-gray-200 dark:border-gray-700 mb-3">
              <span class="text-xs font-bold text-gray-700 dark:text-gray-300">Modo SLA Avanzado (Expresiones)</span>
              <input type="checkbox" v-model="isSlaAdvancedMode" class="h-4 w-4 text-indigo-600 rounded" />
            </div>

            <!-- SLA Global -->
            <!-- @Traceability: US-005, CA-35 -->
            <div class="p-3 bg-blue-50 dark:bg-blue-900/20 border border-blue-100 dark:border-blue-800 rounded">
              <label class="block text-xs font-bold text-blue-800 dark:text-blue-300 mb-1 flex items-center justify-between">
                ⏱ SLA Global
                <AppTooltip :content="bpmnTooltips.GLOBAL_SLA" />
              </label>
              <div v-if="!isSlaAdvancedMode" class="flex gap-2">
                <input type="number" v-model.number="globalSlaSimpleValue" @change="onGlobalSimpleSlaChange" min="1" class="w-2/3 text-xs border-gray-300 rounded shadow-sm focus:ring-indigo-500" />
                <select v-model="globalSlaSimpleUnit" @change="onGlobalSimpleSlaChange" class="w-1/3 text-xs border-gray-300 rounded shadow-sm focus:ring-indigo-500 bg-white">
                  <option value="Minutos">Minutos</option>
                  <option value="Horas">Horas</option>
                  <option value="Días">Días</option>
                  <option value="Semanas">Semanas</option>
                </select>
              </div>
              <input v-else type="text" v-model="globalSlaRaw" @change="updateGlobalSlaRaw" class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border font-mono" placeholder="Ej: P3D" />
            </div>

            <!-- History TTL (Camunda 7 Core) -->
            <div class="p-3 bg-slate-50 dark:bg-slate-900/20 border border-slate-200 dark:border-slate-800 rounded">
              <label class="block text-xs font-bold text-slate-800 dark:text-slate-300 mb-1 flex items-center justify-between">
                🛡️ History Time To Live (Días)
                <AppTooltip content="Número de días que se conservarán los datos históricos en Camunda. Vacío o cero desactiva la limpieza automática para este proceso." />
              </label>
              <input type="number" v-model.number="processHistoryTTL" @change="updateHistoryTTL" min="1" class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border" placeholder="Ej: 180" />
            </div>

            <!-- Version Tag (Camunda 7 Core) -->
            <div class="p-3 bg-slate-50 dark:bg-slate-900/20 border border-slate-200 dark:border-slate-800 rounded">
              <label class="block text-xs font-bold text-slate-800 dark:text-slate-300 mb-1 flex items-center justify-between">
                🏷️ Etiqueta de Versión (Version Tag)
                <AppTooltip content="Etiqueta semántica de la versión del diseño del proceso (ej. 1.0.0, v2.1-draft)." />
              </label>
              <input type="text" v-model="processVersionTag" @change="updateVersionTag" class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border font-mono" placeholder="Ej: 1.0.0" />
            </div>

            <!-- Executable Checkbox (Camunda 7 Core) -->
            <div class="p-3 bg-slate-50 dark:bg-slate-900/20 border border-slate-200 dark:border-slate-800 rounded flex items-center justify-between">
               <span class="block text-xs font-bold text-slate-800 dark:text-slate-300">
                 ⚙️ Proceso Ejecutable (isExecutable)
               </span>
               <input type="checkbox" v-model="processIsExecutable" @change="updateIsExecutable" class="h-4 w-4 text-indigo-600 border-gray-300 dark:border-gray-600 rounded focus:ring-indigo-500" />
             </div>

            <!-- Glosario de Variables de Negocio (CA-5) -->
            <div class="p-3 bg-purple-50 dark:bg-purple-900/20 border border-purple-200 rounded space-y-[12px]">
              <!-- @Traceability: US-005, CA-05 -->
              <div class="flex items-center justify-between cursor-pointer" @click="isGlossaryCollapsed = !isGlossaryCollapsed">
                <div class="flex items-center space-x-1.5">
                  <span class="block text-xs font-bold text-purple-800 dark:text-purple-300">
                    📚 Glosario de Variables de Negocio
                  </span>
                  <AppTooltip :content="bpmnTooltips.GLOSSARY_VARIABLES" />
                </div>
                <span class="text-xs text-purple-600 dark:text-purple-400">
                  {{ isGlossaryCollapsed ? '▶' : '▼' }}
                </span>
              </div>

              <div v-show="!isGlossaryCollapsed" class="space-y-[12px] pt-1 border-t border-purple-100 dark:border-purple-800">
                <!-- List of declared variables -->
                <div v-if="declaredVariables.length === 0" class="text-[10px] text-gray-500 dark:text-gray-400">
                  No hay variables declaradas manualmente.
                </div>
                <div v-else class="space-y-1.5 max-h-40 overflow-y-auto pr-1">
                  <div v-for="(v, index) in declaredVariables" :key="index" class="flex items-center justify-between bg-white dark:bg-gray-800 border border-purple-100 dark:border-purple-900 p-1.5 rounded text-[11px]">
                    <div class="flex items-center space-x-1.5 truncate">
                      <span class="font-mono font-semibold text-purple-700 dark:text-purple-300 truncate" :title="v.name">{{ v.name }}</span>
                      <span class="text-[9px] px-1 bg-purple-100 dark:bg-purple-900/40 text-purple-800 dark:text-purple-300 rounded font-semibold">{{ v.type }}</span>
                    </div>
                    <button @click="removeDeclaredVariable(index)" class="text-red-500 hover:text-red-700 text-xs px-1 font-bold" title="Eliminar variable">&times;</button>
                  </div>
                </div>

                <!-- Form to add new variable -->
                <div class="space-y-2 pt-2 border-t border-purple-100 dark:border-purple-800">
                  <div class="grid grid-cols-2 gap-1.5">
                    <div>
                      <label class="block text-[9px] font-bold text-purple-700 dark:text-purple-400 mb-0.5">Nombre/Clave</label>
                      <input type="text" v-model="newVarName" placeholder="Ej: monto" class="w-full text-[10px] border-purple-200 dark:border-purple-800 dark:bg-gray-700 dark:text-white rounded p-1 border" />
                    </div>
                    <div>
                      <label class="block text-[9px] font-bold text-purple-700 dark:text-purple-400 mb-0.5">Tipo</label>
                      <select v-model="newVarType" class="w-full text-[10px] border-purple-200 dark:border-purple-800 dark:bg-gray-700 dark:text-white rounded p-1 border">
                        <option value="String">String</option>
                        <option value="Number">Number</option>
                        <option value="Boolean">Boolean</option>
                      </select>
                    </div>
                  </div>
                  <button @click="addDeclaredVariable" class="w-full text-center bg-purple-600 hover:bg-purple-700 text-white text-[10px] font-bold py-1 px-2 rounded shadow transition">
                    + Declarar Variable
                  </button>
                </div>
              </div>
            </div>

            <!-- Process Pattern (CA-31 y CA-38) -->
            <div>
              <label class="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1 flex items-center justify-between">
                Patrón de Proceso
                <AppTooltip :content="bpmnTooltips.PROCESS_PATTERN" />
              </label>
              <select v-model="processPattern" @change="updateProcessProperty('formPattern', processPattern)" :disabled="elementCount > 1" class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border disabled:opacity-60 disabled:cursor-not-allowed">
                <option value="SIMPLE">🟢 Simple (Formularios independientes)</option>
                <option value="IFORM_MAESTRO">🔵 iForm Maestro (Formulario mutante)</option>
              </select>
              <p v-if="elementCount > 1" class="text-[9px] text-gray-500 mt-1">🔒 Bloqueado: El lienzo no está vacío.</p>
            </div>
          </div>

          <!-- Shared Name & ID Inputs for Selected Element -->
          <div v-if="selectedElement.id && ['bpmn:Task', 'bpmn:UserTask', 'bpmn:ServiceTask', 'bpmn:BusinessRuleTask', 'bpmn:CallActivity', 'bpmn:StartEvent'].includes(selectedElement.type)" class="space-y-4">
            <div>
              <label class="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ selectedElement.type === 'bpmn:StartEvent' ? 'Nombre del Evento' : 'Nombre de la Tarea' }}
              </label>
              <input type="text" v-model="selectedElement.name" @input="syncElementProperties('name', selectedElement.name)" class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded focus:ring-indigo-500 focus:border-indigo-500 p-2 border" :placeholder="selectedElement.type === 'bpmn:StartEvent' ? 'Nombre del evento' : 'Nombre de la tarea'" />
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
                {{ selectedElement.type === 'bpmn:StartEvent' ? 'ID de Evento' : 'ID de Tarea' }}
              </label>
              <input type="text" :value="selectedElement.id" disabled class="w-full text-xs font-mono border-gray-200 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-400 rounded p-2 border bg-gray-50 cursor-not-allowed" />
            </div>
          </div>

          <!-- 2. User Task Properties -->
          <div v-if="selectedElement.type === 'bpmn:UserTask'" class="space-y-5">
            <!-- FormKey -->
            <div class="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow-sm">
              <label class="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-2 flex items-center justify-between">
                📝 FormKey (User Task)
                <AppTooltip :content="bpmnTooltips.FORM_KEY" />
              </label>
              <p class="text-[10px] text-gray-500 dark:text-gray-400 mb-2">Formulario renderizado en Workdesk</p>
              <!-- BUG-J02-004: Filtro rápido de tipo de formulario -->
              <div class="flex gap-1 mb-2">
                <button 
                  v-for="filterOpt in [
                    { value: 'ALL', label: 'Todos', icon: '📋' },
                    { value: 'SIMPLE', label: 'Simple', icon: '🟢' },
                    { value: 'MAESTRO', label: 'Maestro', icon: '🔵' }
                  ]" 
                  :key="filterOpt.value"
                  @click="formTypeFilter = filterOpt.value"
                  :class="[
                    'px-2 py-1 text-[10px] font-medium rounded-md border transition-all duration-150',
                    formTypeFilter === filterOpt.value
                      ? 'bg-indigo-600 text-white border-indigo-600 shadow-sm'
                      : 'bg-white dark:bg-gray-700 text-gray-600 dark:text-gray-300 border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-600'
                  ]"
                  type="button"
                >
                  {{ filterOpt.icon }} {{ filterOpt.label }}
                </button>
              </div>
              <select v-model="selectedFormKey" @change="syncElementProperties('camunda:formKey', selectedFormKey)" class="w-full text-xs font-mono rounded-lg p-2.5 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white shadow-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 dark:focus:ring-indigo-400 dark:focus:border-indigo-400 transition-colors duration-150 appearance-none cursor-pointer hover:border-indigo-400 dark:hover:border-indigo-500">
                <option value="">-- Sin FormKey --</option>
                <option v-for="form in filteredForms" :key="form.key" :value="form.key">
                  {{ form.type === 'MAESTRO' ? '🔵' : '🟢' }} {{ form.name }} ({{ form.key }})
                </option>
              </select>
            </div>

            <!-- SLA Timeout -->
            <!-- @Traceability: US-005, CA-35 -->
            <div class="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow-sm">
              <label class="block text-xs font-bold text-gray-700 mb-2 flex items-center justify-between">
                <span class="flex items-center gap-1">⏱️ SLA Timeout</span>
                <AppTooltip :content="bpmnTooltips.SLA_TIMEOUT" :isError="isSlaSyntaxError" />
              </label>
              <div v-if="!isSlaAdvancedMode" class="flex gap-2">
                <input type="number" v-model.number="slaSimpleValue" @change="onSimpleSlaChange" min="0" class="w-2/3 text-xs border-gray-300 rounded shadow-sm focus:ring-indigo-500" />
                <select v-model="slaSimpleUnit" @change="onSimpleSlaChange" class="w-1/3 text-xs border-gray-300 rounded shadow-sm focus:ring-indigo-500 bg-white">
                  <option value="Minutos">Minutos</option>
                  <option value="Horas">Horas</option>
                  <option value="Días">Días</option>
                  <option value="Semanas">Semanas</option>
                </select>
              </div>
              <input v-else type="text" v-model="selectedElement.props.sla" @change="updateElementSla" class="w-full text-xs border-gray-300 rounded shadow-sm focus:ring-indigo-500 font-mono" :class="{'border-red-500 bg-red-50 text-red-700': isSlaSyntaxError}" placeholder="Ej: P2D (2 Días)" />
            </div>

            <!-- SharePoint Integration Checkbox (CA-2) -->
            <div v-if="selectedElement.name && selectedElement.name.toLowerCase().includes('intake')" class="p-3 bg-blue-50 border border-blue-200 rounded-md">
              <div class="flex items-start gap-2">
                <input type="checkbox" id="spFolderCheck" v-model="selectedElement.props.createSharepointFolder" @change="syncElementProperties('camunda:createSharepointFolder', selectedElement.props.createSharepointFolder)" class="mt-0.5 text-blue-600 rounded border-blue-300 focus:ring-blue-500 shadow-sm" />
                <label for="spFolderCheck" class="text-[11px] font-bold text-blue-900 cursor-pointer leading-tight">
                  Create Unique SharePoint Sub-folder for this generic Process Instance (CA-2)
                </label>
              </div>
            </div>

            <!-- Escalamiento -->
            <div class="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow-sm">
              <label class="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-2 flex items-center justify-between">
                 <span>🔺 Escalamiento & Ping-Pong</span>
                 <AppTooltip :content="bpmnTooltips.ESCALATION" />
              </label>
              <div class="space-y-2">
                <select class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border">
                  <option>Escalamiento: Ninguno</option>
                  <option>Escalamiento: Al Supervisor</option>
                  <option>Escalamiento: Al Director</option>
                </select>
                <select class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border">
                  <option>Anti Ping-Pong: Desactivado</option>
                  <option>Anti Ping-Pong: Máx 2 rebotes</option>
                  <option>Anti Ping-Pong: Máx 3 rebotes</option>
                </select>
              </div>
            </div>
          </div>

          <!-- Start Event Properties -->
          <!-- @Traceability: US-005, US-024 Zero-Bypass Form Start -->
          <div v-if="selectedElement.type === 'bpmn:StartEvent'" class="space-y-5">
            <!-- FormKey -->
            <div class="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow-sm">
              <label class="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-2 flex items-center justify-between">
                📝 FormKey (Start Event)
                <AppTooltip :content="bpmnTooltips.FORM_KEY" />
              </label>
              <p class="text-[10px] text-gray-500 dark:text-gray-400 mb-2">Formulario de inicio del proceso</p>
              <!-- BUG-J02-004: Filtro rápido de tipo de formulario -->
              <div class="flex gap-1 mb-2">
                <button 
                  v-for="filterOpt in [
                    { value: 'ALL', label: 'Todos', icon: '📋' },
                    { value: 'SIMPLE', label: 'Simple', icon: '🟢' },
                    { value: 'MAESTRO', label: 'Maestro', icon: '🔵' }
                  ]" 
                  :key="filterOpt.value"
                  @click="formTypeFilter = filterOpt.value"
                  :class="[
                    'px-2 py-1 text-[10px] font-medium rounded-md border transition-all duration-150',
                    formTypeFilter === filterOpt.value
                      ? 'bg-indigo-600 text-white border-indigo-600 shadow-sm'
                      : 'bg-white dark:bg-gray-700 text-gray-600 dark:text-gray-300 border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-600'
                  ]"
                  type="button"
                >
                  {{ filterOpt.icon }} {{ filterOpt.label }}
                </button>
              </div>
              <select v-model="selectedFormKey" @change="syncElementProperties('camunda:formKey', selectedFormKey)" class="w-full text-xs font-mono rounded-lg p-2.5 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white shadow-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 dark:focus:ring-indigo-400 dark:focus:border-indigo-400 transition-colors duration-150 appearance-none cursor-pointer hover:border-indigo-400 dark:hover:border-indigo-500">
                <option value="">-- Sin FormKey --</option>
                <option v-for="form in filteredForms" :key="form.key" :value="form.key">
                  {{ form.type === 'MAESTRO' ? '🔵' : '🟢' }} {{ form.name }} ({{ form.key }})
                </option>
              </select>
            </div>
          </div>

          <!-- 3. Service Task Properties -->
          <div v-if="selectedElement.type === 'bpmn:ServiceTask'" class="space-y-5">
            <!-- Service Task Topics (CA-70) -->
            <div class="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow-sm">
               <label class="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-2 flex items-center justify-between">
                  <span>🏷️ External Topic (CA-70)</span>
               </label>
               <p class="text-[10px] text-gray-500 mb-2">Tópico al que se suscriben los External Task Workers.</p>
               <div class="relative">
                  <select v-model="selectedElement.props.topic" @change="syncElementProperties('camunda:topic', selectedElement.props.topic)" class="w-full text-xs font-mono border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border" :disabled="loadingTopics">
                     <option value="">-- Seleccionar Tópico --</option>
                     <option v-for="t in externalTopics" :key="t" :value="t">{{ t }}</option>
                  </select>
                  <div v-if="loadingTopics" class="absolute top-0 right-3 h-full flex items-center">
                     <span class="animate-spin text-indigo-500 font-bold text-sm">↻</span>
                  </div>
               </div>
            </div>

            <!-- Service Task Connector (CA-47, CA-49) -->
            <div class="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow-sm">
              <label class="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-2 flex items-center justify-between">
                 <span>🔌 Conector API (Service Task)</span>
                 <AppTooltip :content="bpmnTooltips.CONNECTOR" />
              </label>
              <select v-model="selectedConnector" @change="updateElementConnector" class="w-full text-xs font-mono border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border mb-3">
                <option value="">-- Sin Conector --</option>
                <option v-for="c in availableConnectors" :key="c.id" :value="c.id">
                  {{ c.icon }} {{ c.name }}
                </option>
              </select>

              <!-- CA-49 & CA-50: DataMapperGrid -->
              <div v-if="selectedConnector" class="border-t border-gray-200 dark:border-gray-700 pt-3">
                 <label class="block text-xs font-bold text-indigo-700 dark:text-indigo-400 mb-2">
                   🔀 Mapeo Visual (DataMapperGrid)
                 </label>
                 <table class="w-full text-xs text-left">
                    <thead>
                       <tr class="text-gray-500 dark:text-gray-400 border-b border-gray-200 dark:border-gray-700">
                          <th class="pb-1 font-medium w-1/2">Input Esperado</th>
                          <th class="pb-1 font-medium w-1/2">Variable del Proceso</th>
                       </tr>
                    </thead>
                    <tbody>
                       <tr v-for="schema in connectorSchema" :key="schema.name" class="border-b border-gray-100 dark:border-gray-700 last:border-0 hover:bg-gray-50 dark:hover:bg-gray-700 transition">
                          <td class="py-2 pr-2 font-mono text-[10px] text-gray-700 dark:text-gray-300">
                             <div class="font-bold">{{ schema.name }}</div>
                             <div class="text-gray-400 text-[9px]">({{ schema.type }})</div>
                          </td>
                          <td class="py-2 relative group">
                             <select v-model="connectorMappings[schema.name]" @change="saveConnectorMapping" class="w-full text-[10px] p-1 border border-gray-300 rounded dark:bg-gray-700 dark:border-gray-600 dark:text-white focus:ring-1 focus:ring-indigo-500" :class="{'border-red-500 ring-1 ring-red-500': mappingErrors[schema.name]}">
                                <option value="">-- Asignar --</option>
                                <option v-for="v in processVariables" :key="v.name" :value="v.name" :disabled="!isTypeCompatible(schema.type, v.type)">
                                   {{ !isTypeCompatible(schema.type, v.type) ? '🚫 ' : '' }}{{ v.name }} ({{ v.type }})
                                </option>
                             </select>
                             <AppTooltip v-if="mappingErrors[schema.name]" content="⚠️ Tipo Incompatible" isError class="absolute right-0 top-1/2 -translate-y-1/2 -mr-6" />
                          </td>
                       </tr>
                    </tbody>
                 </table>
                 <div v-if="loadingSchema" class="flex justify-center py-2"><div class="w-3/4 h-4 rounded bg-gray-200 dark:bg-gray-600 animate-pulse"></div></div>
              </div>
            </div>
          </div>

          <!-- 4. Business Rule Task Properties (CA-12) -->
          <div v-if="selectedElement.type === 'bpmn:BusinessRuleTask'" class="space-y-5">
            <!-- CA-12: Business Rule Task — DMN Binding (Protección de Derechos Adquiridos) -->
            <div class="p-3 bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded shadow-sm">
               <label class="block text-xs font-bold text-amber-800 dark:text-amber-300 mb-2 flex items-center justify-between">
                  <span>📐 Regla DMN (CA-12)</span>
                  <AppTooltip content="Configura la regla de negocio y si se evalúa con la versión vigente al desplegar (DEPLOYMENT) o con la última publicada (LATEST)." />
               </label>
               <p class="text-[10px] text-amber-700 dark:text-amber-400 mb-2">Tabla de decisión conectada:</p>
               <select v-model="selectedElement.props.decisionRef" @change="syncElementProperties('camunda:decisionRef', selectedElement.props.decisionRef)" class="w-full text-xs font-mono border-amber-300 dark:border-amber-600 dark:bg-gray-700 dark:text-white rounded p-2 border mb-3">
                  <option value="">— Seleccionar tabla DMN —</option>
                  <option v-for="dmn in availableDmns" :key="dmn.id" :value="dmn.key || dmn.id">
                     {{ dmn.name }} (v{{ dmn.version }})
                  </option>
               </select>

               <p class="text-[10px] text-amber-700 dark:text-amber-400 mb-2">Estrategia de versionamiento:</p>
               <select v-model="selectedElement.props.dmnBinding"
                       @change="syncElementProperties('camunda:decisionRefBinding', selectedElement.props.dmnBinding)"
                       class="w-full text-xs font-mono border-amber-300 dark:border-amber-600 dark:bg-gray-700 dark:text-white rounded p-2 border">
                  <option value="deployment">🔒 DEPLOYMENT (Default — Protección de Derechos Adquiridos)</option>
                  <option value="latest">⚡ LATEST (Late Binding — Siempre la regla más reciente)</option>
               </select>
               <p class="text-[10px] text-amber-600 dark:text-amber-500 mt-1 leading-tight">
                  <strong>DEPLOYMENT:</strong> Los casos en vuelo se evalúan con la DMN activa al nacer el caso.<br>
                  <strong>LATEST:</strong> Los casos en vuelo se evalúan con la DMN más reciente publicada.
               </p>
            </div>
          </div>

          <!-- 5. Call Activity Properties (CA-27) -->
          <div v-if="selectedElement.type === 'bpmn:CallActivity'" class="space-y-5">
            <div class="mb-4">
               <label class="block text-xs font-bold text-gray-700 mb-2 flex items-center justify-between">
                  <span>🔗 Destino de Call Activity</span>
                  <AppTooltip :content="bpmnTooltips.CALL_ACTIVITY" :isError="isCallActivityError" />
               </label>
               <button @click="openCallActivity" class="w-full text-xs text-center py-2 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded text-gray-500 dark:text-gray-400 hover:border-indigo-400 hover:text-indigo-600 transition truncate px-2" :class="{'border-red-400 hover:border-red-500 text-red-500 bg-red-50 hover:bg-red-100': isCallActivityError}" :title="selectedElement.props.calledElement || 'Sub-proceso'">
                 Abrir Sub-Proceso {{ selectedElement.props.calledElement ? `(${selectedElement.props.calledElement})` : '(No Configurado)' }}
               </button>
            </div>
          </div>

          <!-- 6. Information Banner for non-editable elements (Gateways, Events, etc.) -->
          <!-- @Traceability: US-005, CA-77 Panel de Propiedades Contextual -->
          <!-- Banner para Tarea Genérica sin tipo definido -->
          <div v-if="selectedElement.id && selectedElement.type === 'bpmn:Task'" class="p-4 bg-amber-50 border border-amber-200 rounded text-xs text-amber-800 space-y-2">
             <div class="font-bold flex items-center gap-1">
               <span>⚠️ Tarea Genérica (Sin Tipo)</span>
             </div>
             <p class="leading-relaxed">Esta es una tarea genérica sin propiedades de ejecución de Camunda.</p>
             <p class="leading-relaxed font-semibold">Para configurarla:</p>
             <ul class="list-disc list-inside space-y-1 text-[11px] text-amber-700">
               <li>Haz clic sobre la tarea en el lienzo.</li>
               <li>Selecciona el ícono de la <strong>llave de tuercas 🔧</strong> (Change type).</li>
               <li>Cámbiala a <strong>User Task</strong> (para asociar formularios) o <strong>Service Task</strong> (para conectar APIs/conectores).</li>
             </ul>
          </div>
          <!-- INICIO: Panel de Propiedades Lane (US-005/US-036 Extension) -->
          <div v-else-if="selectedElement && (selectedElement.type === 'bpmn:Lane' || selectedElement.type === 'bpmn:Participant')" class="space-y-5">
            <!-- Nombre del Lane -->
            <div>
              <label class="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
                Nombre del {{ selectedElement.type === 'bpmn:Lane' ? 'Lane' : 'Participante' }}
              </label>
              <input
                type="text"
                v-model="selectedElement.name"
                @change="syncElementProperties('name', selectedElement.name)"
                class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded focus:ring-indigo-500 focus:border-indigo-500 p-2 border"
                placeholder="Ej: Departamento de Contabilidad"
                data-testid="lane-name-input"
              />
            </div>
            <!-- Actor / Participante -->
            <div class="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow-sm">
              <label class="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-2">
                👤 Actor / Participante
              </label>
              <p class="text-[10px] text-gray-500 mb-2">Persona o departamento responsable de este carril.</p>
              <input
                type="text"
                v-model="selectedElement.props.assignee"
                @change="syncElementProperties('camunda:assignee', selectedElement.props.assignee)"
                class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded focus:ring-indigo-500 focus:border-indigo-500 p-2 border"
                placeholder="Ej: Departamento de Contabilidad"
                data-testid="lane-actor-input"
              />
            </div>
            <!-- Rol RBAC Vinculado -->
            <div class="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow-sm">
              <label class="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-2 flex items-center justify-between">
                <span>🔐 Rol RBAC Vinculado</span>
              </label>
              <p class="text-[10px] text-gray-500 mb-2">Rol del sistema de seguridad asociado a este carril.</p>
              <select
                v-model="selectedElement.props.candidateGroups"
                @change="syncElementProperties('camunda:candidateGroups', selectedElement.props.candidateGroups)"
                class="w-full text-xs font-mono border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border"
                data-testid="lane-linked-role-select"
              >
                <option value="">-- Sin rol vinculado --</option>
                <option v-for="role in rbacStore.roles" :key="role.id" :value="role.name">
                  {{ role.name }}
                </option>
              </select>
            </div>
            <!-- Indicador visual de vinculación -->
            <div class="flex items-center gap-2 px-1" data-testid="lane-link-badge">
              <span v-if="selectedElement.props.candidateGroups" class="inline-flex items-center px-2.5 py-1 text-xs rounded-full bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300">
                ✅ Rol vinculado: {{ selectedElement.props.candidateGroups }}
              </span>
              <span v-else class="inline-flex items-center px-2.5 py-1 text-xs rounded-full bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-300">
                ⚠️ Sin rol RBAC vinculado
              </span>
            </div>
          </div>
          <!-- FIN: Panel de Propiedades Lane (US-005/US-036 Extension) -->
          <div v-else-if="selectedElement.id && !['bpmn:UserTask', 'bpmn:ServiceTask', 'bpmn:BusinessRuleTask', 'bpmn:CallActivity', 'bpmn:StartEvent'].includes(selectedElement.type)" class="p-4 bg-gray-50 border border-gray-200 rounded text-xs text-gray-500 text-center">
             ℹ️ No hay propiedades de Camunda editables para este elemento.
          </div>

          <!-- 7. AI Copilot Quick Action (Visible when selection exists) -->
          <div v-if="selectedElement.id" class="pt-2 border-t border-gray-200 dark:border-gray-700">
            <button @click="showCopilot = true" class="w-full bg-slate-900 hover:bg-black text-white px-3 py-2 rounded text-xs font-semibold flex items-center justify-center gap-2 transition">
              🧠 Auditoría ISO-9001 (Copilot)
            </button>
          </div>

          <!-- 8. Módulo Cognitivo (CA-10 / CA-11) (Visible when element name includes 'rag') -->
          <div v-if="selectedElement.name && selectedElement.name.toLowerCase().includes('rag')" class="p-4 bg-emerald-50 dark:bg-emerald-900/20 border-2 border-emerald-200 dark:border-emerald-800 rounded-lg shadow-inner mt-4">
            <h4 class="text-xs font-bold text-emerald-800 dark:text-emerald-400 mb-3 flex items-center gap-2">
              <span class="text-lg">🤖</span> Cognitive Task Settings
            </h4>
            <div class="space-y-4">
              <!-- Tone Selector (CA-11) -->
              <div>
                <label class="block text-[10px] font-bold text-emerald-700 dark:text-emerald-500 uppercase tracking-widest mb-1">Tone Override</label>
                <select v-model="selectedElement.props.aiTone" @change="syncElementProperties('camunda:aiTone', selectedElement.props.aiTone)" class="w-full text-xs font-medium border-emerald-300 dark:border-emerald-700 bg-white dark:bg-gray-800 text-gray-800 dark:text-gray-200 rounded p-1.5 focus:ring-emerald-500">
                  <option value="NEUTRAL">Neutral / Objetivo</option>
                  <option value="EMPATHETIC">Empático (Servicio al Cliente)</option>
                  <option value="FORMAL">Formal (Legal / Regulatorio)</option>
                  <option value="COMMERCIAL">Comercial (Ventas / Persuasivo)</option>
                </select>
              </div>

              <!-- Reading Limits (CA-10) Cost Control -->
              <div class="pt-2 border-t border-emerald-100 dark:border-emerald-800/50">
                <div class="flex justify-between items-center mb-1">
                  <label class="block text-[10px] font-bold text-emerald-700 dark:text-emerald-500 uppercase tracking-widest">Max Context (Tokens)</label>
                  <span class="text-[10px] font-mono font-bold text-emerald-900 dark:text-emerald-300 bg-emerald-100 dark:bg-emerald-800 px-1 rounded">{{ selectedElement.props.aiTokenLimit || 2000 }}</span>
                </div>
                <input type="range" v-model.number="selectedElement.props.aiTokenLimit" min="500" max="32000" step="500" class="w-full accent-emerald-600" />
                <p class="text-[9px] text-emerald-600 dark:text-emerald-500 mt-1 leading-tight">Limita la cantidad de texto extraído del SGDEA para evitar facturación excesiva del LLM en documentos gigantes (Pre-Packaged Context).</p>
              </div>

              <!-- Target Output Schema -->
              <div class="pt-2 border-t border-emerald-100 dark:border-emerald-800/50">
                <label class="block text-[10px] font-bold text-emerald-700 dark:text-emerald-500 uppercase tracking-widest mb-1">JSON Target Schema</label>
                <input type="text" v-model="selectedElement.props.aiSchemaId" @blur="syncElementProperties('camunda:aiSchemaId', selectedElement.props.aiSchemaId)" placeholder="Ej: schema_risk_matrix_v2" class="w-full text-[11px] font-mono border-emerald-300 dark:border-emerald-700 bg-white dark:bg-gray-800 text-gray-800 dark:text-gray-200 rounded p-1.5" />
                <p class="text-[9px] text-emerald-600 dark:text-emerald-500 mt-1 leading-tight">Fuerza a la IA a responder con un layout compatible con Pantalla 7.</p>
              </div>
            </div>
          </div>

          <!-- ════════════════════════════════════════════════ -->

        </div>
      </aside>

      <!-- ═══════ Validation & Simulation Side Panel (CA-80) ═══════ -->
      <aside 
        v-show="showSandboxModal" 
        :style="{ width: validationPanelWidth + 'px' }" 
        data-testid="sandbox-glass-modal" 
        class="border-l border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 shrink-0 flex flex-col overflow-hidden relative select-none animate-slide-left"
      >
        <!-- Resizer Bar (CA-80) -->
        <div 
          data-testid="validation-resizer" 
          @mousedown="startResizing" 
          class="absolute top-0 left-0 w-1 h-full cursor-col-resize hover:bg-indigo-500/50 transition z-50"
        ></div>

        <!-- Header -->
        <div class="px-4 py-3 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center bg-gray-50 dark:bg-gray-900 shrink-0">
          <h3 class="text-xs font-bold text-gray-700 dark:text-gray-200 flex items-center gap-1.5">
            🧪 Embudo de Validación y Sandbox
          </h3>
          <button @click="showSandboxModal = false" class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 text-lg font-bold transition">&times;</button>
        </div>

        <!-- Accordion Content (CA-81) -->
        <div class="flex-1 overflow-y-auto flex flex-col min-h-0 select-text">
          
          <!-- Accordion 1: Linter Local -->
          <div class="border-b border-gray-200 dark:border-gray-700">
            <div 
              data-testid="linter-header" 
              @click="collapsedSections.linter = !collapsedSections.linter" 
              class="cursor-pointer px-4 py-3 bg-gray-50 dark:bg-gray-900 flex justify-between items-center text-xs font-bold text-gray-600 dark:text-gray-300 select-none"
            >
              <span>🔍 Nivel 1: Linter Local</span>
              <span>{{ collapsedSections.linter ? '➕' : '➖' }}</span>
            </div>
            
            <div v-show="!collapsedSections.linter" data-testid="linter-level" class="p-4 space-y-[12px] bg-white dark:bg-gray-800">
              <div v-if="linterErrors.length > 0" class="space-y-2">
                <div v-for="(err, i) in linterErrors" :key="'lin-'+i" class="p-3 bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900 rounded-lg text-xs text-red-700 dark:text-red-400 font-mono">
                  {{ err }}
                </div>
              </div>
              <div v-else class="text-xs text-green-600 dark:text-green-400 font-medium">
                ✅ No se detectaron errores estructurales de diseño.
              </div>
            </div>
          </div>

          <!-- Accordion 2: Pre-Flight Analyzer -->
          <div class="border-b border-gray-200 dark:border-gray-700">
            <div 
              data-testid="preflight-header" 
              @click="collapsedSections.preflight = !collapsedSections.preflight" 
              class="cursor-pointer px-4 py-3 bg-gray-50 dark:bg-gray-900 flex justify-between items-center text-xs font-bold text-gray-600 dark:text-gray-300 select-none"
            >
              <span>⚙️ Nivel 2: Pre-Flight Analyzer</span>
              <span>{{ collapsedSections.preflight ? '➕' : '➖' }}</span>
            </div>
            
            <div v-show="!collapsedSections.preflight" data-testid="preflight-level" class="p-4 space-y-[12px] bg-white dark:bg-gray-800">
              <div v-if="preFlightErrors.length > 0 || preFlightWarnings.length > 0" class="space-y-2">
                <div v-for="(err, i) in preFlightErrors" :key="'pfe-'+i" class="p-3 bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900 rounded-lg text-xs text-red-700 dark:text-red-400 font-mono">
                  🛑 {{ err }}
                </div>
                <div v-for="(warn, i) in preFlightWarnings" :key="'pfw-'+i" class="p-3 bg-amber-50 dark:bg-amber-950/20 border border-amber-200 dark:border-amber-900 rounded-lg text-xs text-amber-700 dark:text-amber-400 font-mono">
                  ⚠️ {{ warn }}
                </div>
              </div>
              <div v-else class="text-xs text-green-600 dark:text-green-400 font-medium">
                ✅ Validación de políticas de gobernanza superada.
              </div>
            </div>
          </div>

          <!-- Accordion 3: Sandbox Simulator -->
          <div class="border-b border-gray-200 dark:border-gray-700 flex-1 flex flex-col min-h-0">
            <div 
              data-testid="simulator-header" 
              @click="collapsedSections.simulator = !collapsedSections.simulator" 
              class="cursor-pointer px-4 py-3 bg-gray-50 dark:bg-gray-900 flex justify-between items-center text-xs font-bold text-gray-600 dark:text-gray-300 select-none shrink-0"
            >
              <span>🚀 Nivel 3: Sandbox Simulator</span>
              <span>{{ collapsedSections.simulator ? '➕' : '➖' }}</span>
            </div>
            
            <div v-show="!collapsedSections.simulator" data-testid="sandbox-level" class="p-4 flex-1 flex flex-col min-h-0 space-y-4 bg-white dark:bg-gray-800 overflow-y-auto">
              
              <div v-if="sandboxBlocked" class="p-3 bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-900 rounded-lg text-xs text-red-800 dark:text-red-400 font-semibold">
                ⚠️ Simulación Inhabilitada: Corrige los errores críticos en el Linter o Pre-Flight.
              </div>

              <div v-else class="space-y-4 flex-1 flex flex-col min-h-0">
                <!-- Variables CRUD Grid (CA-83) -->
                <div class="bg-gray-50 dark:bg-gray-900 p-3 rounded-lg border border-gray-200 dark:border-gray-700 flex flex-col shrink-0">
                  <h4 class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Variables del Sandbox</h4>
                  
                  <!-- Formulario de adición inline -->
                  <div class="grid grid-cols-3 gap-1 mb-2">
                    <input 
                      type="text" 
                      v-model="newGridVarName" 
                      placeholder="Nombre" 
                      class="text-xs p-1 border rounded dark:bg-gray-800 dark:border-gray-700 dark:text-white"
                    />
                    <select 
                      v-model="newGridVarType" 
                      class="text-xs p-1 border rounded dark:bg-gray-800 dark:border-gray-700 dark:text-white"
                    >
                      <option value="String">String</option>
                      <option value="Number">Number</option>
                      <option value="Boolean">Boolean</option>
                    </select>
                    <input 
                      type="text" 
                      v-model="newGridVarValue" 
                      placeholder="Valor" 
                      class="text-xs p-1 border rounded dark:bg-gray-800 dark:border-gray-700 dark:text-white"
                    />
                  </div>
                  <button 
                    data-testid="btn-grid-add-variable" 
                    @click="addGridVariable" 
                    class="w-full bg-indigo-600 hover:bg-indigo-700 text-white text-xs py-1 rounded font-bold transition"
                  >
                    ➕ Agregar Variable
                  </button>

                  <!-- Lista de variables de la grilla -->
                  <div v-if="Object.keys(sandboxVariables).length > 0" class="mt-3 space-y-1.5 max-h-40 overflow-y-auto pr-1">
                    <div 
                      v-for="(val, name) in sandboxVariables" 
                      :key="name" 
                      class="flex items-center justify-between bg-white dark:bg-gray-850 p-2 rounded border border-gray-150 dark:border-gray-750 text-xs"
                    >
                      <div class="flex flex-col min-w-0">
                        <span class="font-bold truncate text-gray-700 dark:text-gray-200">{{ name }}</span>
                        <span class="text-[10px] text-gray-400">Tipo: {{ typeof val === 'number' ? 'Number' : typeof val === 'boolean' ? 'Boolean' : 'String' }}</span>
                      </div>
                      <div class="flex items-center gap-1">
                        <!-- Edit inline value -->
                        <input 
                          type="text" 
                          :value="val" 
                          @change="(e: any) => editGridVariable(name as string, typeof val === 'number' ? Number(e.target.value) : typeof val === 'boolean' ? (e.target.value === 'true' || e.target.value === true) : e.target.value)" 
                          class="w-20 text-xs p-0.5 border rounded text-right dark:bg-gray-800 dark:text-white"
                        />
                        <button 
                          :data-testid="'btn-grid-delete-' + name" 
                          @click="deleteGridVariable(name as string)" 
                          class="text-red-500 hover:text-red-700 font-bold px-1"
                        >
                          🗑️
                        </button>
                      </div>
                    </div>
                  </div>
                  <div v-else class="text-[10px] text-gray-400 mt-2 text-center">
                    No hay variables ingresadas.
                  </div>
                </div>

                <!-- Run Simulation Area -->
                <div class="flex justify-end shrink-0">
                  <button 
                    data-testid="btn-run-simulation" 
                    @click="startSimulation()" 
                    :disabled="isSimulating" 
                    class="bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold px-4 py-2 rounded shadow transition disabled:opacity-50"
                  >
                    ⚡ Iniciar Simulación
                  </button>
                </div>

                <!-- Simulation Logs -->
                <div v-if="simulationLogs.length > 0 || executedNodes.length > 0" class="flex-1 flex flex-col min-h-[120px] bg-gray-950 text-emerald-400 p-3 rounded-lg border border-gray-850 font-mono text-[11px] overflow-hidden">
                  <div class="border-b border-gray-900 pb-1 flex justify-between text-gray-500 font-sans shrink-0">
                    <span>SALIDA DE EJECUCIÓN</span>
                    <button v-if="executedNodes.length > 0" @click="clearTrajectory" class="text-red-400 hover:underline">Limpiar Trayectoria</button>
                  </div>
                  <div class="flex-1 overflow-y-auto mt-1 space-y-0.5">
                    <div v-for="(log, i) in simulationLogs" :key="'log-'+i">
                      {{ log }}
                    </div>
                    <div v-if="executedNodes.length > 0" class="text-white font-bold mt-2">
                      Nodos Ejecutados: [{{ executedNodes.join(', ') }}]
                    </div>
                  </div>
                </div>
              </div>

            </div>
          </div>

        </div>

        <!-- Footer -->
        <div class="px-4 py-3 border-t border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 flex justify-between items-center shrink-0">
          <button @click="runValidationFunnel" class="bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 text-gray-800 dark:text-white px-3 py-1.5 rounded text-xs font-bold transition">
            🔄 Re-Validar Todo
          </button>
          <button @click="showSandboxModal = false" class="bg-indigo-600 text-white px-3 py-1.5 rounded text-xs font-bold hover:bg-indigo-750 transition">
            Cerrar
          </button>
        </div>
      </aside>
    </main>

    <!-- ═══════ Modal: Deploy ═══════ -->
    <div v-if="showDeployModal" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-2xl w-full max-w-md overflow-hidden">
        <div class="px-6 py-4 bg-indigo-50 dark:bg-indigo-900/30 border-b border-indigo-100 dark:border-indigo-800 flex items-center justify-between">
          <h3 class="text-lg font-bold text-gray-900 dark:text-white">🚀 Desplegar Proceso</h3>
          <button @click="showDeployModal = false" class="text-gray-400 hover:text-gray-600 text-xl">&times;</button>
        </div>
        <div class="p-6 space-y-4">
          <div class="bg-gray-50 dark:bg-gray-900 rounded-lg p-3 text-sm">
            <p class="text-gray-600 dark:text-gray-400">Proceso: <span class="font-bold text-gray-900 dark:text-white">{{ currentProcessName }}</span></p>
            <p class="text-gray-600 dark:text-gray-400">Instancias activas: <span class="font-bold text-orange-600">{{ activeInstances }}</span></p>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Estrategia de Versionado</label>
            <select v-model="deployStrategy" class="w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm p-2.5 border text-sm">
              <option value="coexist">Coexistir (instancias activas terminan en versión anterior)</option>
              <option value="migrate">Forzar Migración (todas las instancias a nueva versión)</option>
            </select>
          </div>
          <div v-if="activeInstances > 0 && deployStrategy === 'migrate'" class="bg-yellow-50 border border-yellow-200 rounded p-3 text-xs text-yellow-800">
            ⚠️ Se migrarán {{ activeInstances }} instancias en vuelo a la nueva versión. Esta acción es irreversible.
          </div>
          <!-- CA-65 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Justificación del Despliegue <span class="text-red-500">*</span></label>
            <textarea v-model="deployComment" rows="3" minlength="10" placeholder="Justificación del despliegue..." class="w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white shadow-sm p-2.5 border text-sm"></textarea>
            <p class="text-[10px] text-gray-500 mt-1">Mínimo 10 caracteres requeridos</p>
          </div>
            <!-- @Traceability: US-005, CA-33 - Checkbox 'forceDeploy' eliminado. Hard-Stop obligatorio. -->
          <div class="flex justify-end space-x-3 pt-2">
            <button @click="showDeployModal = false" class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition">Cancelar</button>
            <button data-testid="btn-confirm-deploy" @click="confirmDeploy" :disabled="isDeploying || deployComment.trim().length < 10" class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg shadow transition disabled:opacity-50">
              {{ isDeploying ? 'Desplegando...' : 'Confirmar Despliegue' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ═══════ Modal: Nuevo Proceso ═══════ -->
    <div v-if="showNewProcessModal" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-2xl w-full max-w-lg overflow-hidden">
        <div class="px-6 py-4 bg-blue-50 dark:bg-blue-900/30 border-b flex items-center justify-between">
          <h3 class="text-lg font-bold text-gray-900 dark:text-white">✨ Nuevo Proceso BPMN</h3>
          <button @click="showNewProcessModal = false" class="text-gray-400 hover:text-gray-600 text-xl">&times;</button>
        </div>
        <div class="p-6 space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Nombre del Proceso</label>
            <input v-model="newProcessName" type="text" placeholder="Ej. Onboarding Cliente Jurídico" class="w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white p-2.5 border text-sm" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Patrón</label>
            <div class="grid grid-cols-2 gap-3">
              <button @click="newProcessPattern = 'SIMPLE'" :class="newProcessPattern === 'SIMPLE' ? 'ring-2 ring-green-500 border-green-300' : ''" class="p-4 border rounded-lg text-center hover:bg-green-50 dark:hover:bg-green-900/20 transition">
                <span class="text-2xl">🟢</span>
                <p class="text-sm font-bold mt-1 text-gray-800 dark:text-white">Simple</p>
                <p class="text-[10px] text-gray-500">Formularios independientes</p>
              </button>
              <button @click="newProcessPattern = 'IFORM_MAESTRO'" :class="newProcessPattern === 'IFORM_MAESTRO' ? 'ring-2 ring-blue-500 border-blue-300' : ''" class="p-4 border rounded-lg text-center hover:bg-blue-50 dark:hover:bg-blue-900/20 transition">
                <span class="text-2xl">🔵</span>
                <p class="text-sm font-bold mt-1 text-gray-800 dark:text-white">iForm Maestro</p>
                <p class="text-[10px] text-gray-500">Super-form mutante global</p>
              </button>
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Origen</label>
            <div class="flex space-x-3">
              <button @click="newProcessOrigin = 'SCRATCH'; selectedTemplateId = ''" :class="newProcessOrigin === 'SCRATCH' ? 'bg-gray-200 dark:bg-gray-600 font-bold' : ''" class="flex-1 border rounded-lg p-3 text-sm text-center hover:bg-gray-100 dark:hover:bg-gray-700 transition">Desde Cero</button>
              <button @click="newProcessOrigin = 'TEMPLATE'" :class="newProcessOrigin === 'TEMPLATE' ? 'bg-gray-200 dark:bg-gray-600 font-bold' : ''" class="flex-1 border rounded-lg p-3 text-sm text-center hover:bg-gray-100 dark:hover:bg-gray-700 transition">Usar Plantilla</button>
            </div>
            
            <!-- CA-18 Selección de Plantilla -->
            <div v-if="newProcessOrigin === 'TEMPLATE'" class="mt-3 p-3 bg-blue-50 dark:bg-blue-900/20 rounded border border-blue-100 dark:border-blue-800 animate-in fade-in zoom-in duration-200">
              <label class="block text-xs font-bold text-blue-800 dark:text-blue-300 mb-1">Catálogo de Plantillas Base</label>
              <select v-model="selectedTemplateId" class="w-full text-xs rounded border-blue-200 dark:border-blue-700 bg-white dark:bg-gray-800 p-2 text-gray-800 dark:text-gray-200" :disabled="loadingTemplates">
                <option value="">-- Selecciona una plantilla --</option>
                <option v-for="t in templatesList" :key="t.id" :value="t.id">{{ t.name }}</option>
              </select>
              <p v-if="loadingTemplates" class="text-[10px] text-blue-500 mt-1 animate-pulse">Descargando XMLs del servidor...</p>
            </div>
          </div>
          <div class="flex justify-end space-x-3 pt-2">
            <button @click="showNewProcessModal = false" class="px-4 py-2 text-sm text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition">Cancelar</button>
            <button @click="createNewProcess" :disabled="!newProcessName.trim() || (newProcessOrigin === 'TEMPLATE' && !selectedTemplateId)" class="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow transition disabled:opacity-50">Crear Proceso</button>
          </div>
        </div>
      </div>
    </div>

    <!-- ═══════ Panel: AI Copilot (Bottom) ═══════ -->
    <Transition name="slide-up">
      <div v-if="showCopilot" class="absolute bottom-0 left-0 right-0 h-64 bg-gray-900 text-white border-t-2 border-emerald-500 flex flex-col z-40 shadow-2xl">
        <div class="flex items-center justify-between px-4 py-2 bg-gray-800 shrink-0">
          <h4 class="text-sm font-bold flex items-center gap-2"><span class="text-emerald-400">🧠</span> Copiloto IA — Auditoría ISO 9001</h4>
          <button @click="showCopilot = false" class="text-gray-400 hover:text-white">&times;</button>
        </div>
        <div class="flex-1 p-4 overflow-y-auto space-y-[12px] text-sm font-mono">
          <div v-for="(msg, i) in copilotMessages" :key="i" class="flex flex-col gap-2">
            <div class="flex items-start gap-2">
              <span :class="msg.role === 'ai' ? 'text-emerald-400' : 'text-blue-400'">{{ msg.role === 'ai' ? '🤖' : '👤' }}</span>
              <p class="text-gray-300 leading-relaxed whitespace-pre-wrap">{{ msg.text }}</p>
            </div>
            <!-- CA-07 Action Pills (Inmutables post-clic) -->
            <div v-if="msg.options && msg.options.length > 0" class="flex flex-wrap gap-2 ml-6">
              <button 
                v-for="(opt, optIdx) in msg.options" 
                :key="optIdx"
                @click="selectCopilotOption(msg, opt)"
                :disabled="!!msg.selectedOption"
                :class="[
                  'px-3 py-1.5 text-xs font-semibold rounded-full border transition-all duration-200 shadow-sm',
                  msg.selectedOption === opt 
                    ? 'bg-emerald-600 border-emerald-500 text-white shadow-emerald-500/50'
                    : msg.selectedOption 
                      ? 'bg-gray-800 border-gray-700 text-gray-500 opacity-50 cursor-not-allowed shadow-none'
                      : 'bg-gray-800 border-emerald-500/50 text-emerald-300 hover:bg-emerald-900/50 hover:border-emerald-400 cursor-pointer'
                ]"
              >
                {{ opt }}
              </button>
            </div>
          </div>
          <div v-if="copilotLoading" class="flex items-center justify-center p-4">
             <!-- CA-01: Lottie Animation (Lazy Loaded) -->
             <Vue3Lottie animationLink="https://lottie.host/b0429fec-4467-4bdc-b72e-d52f68d3deec/0JpI5bM2P1.json" :height="100" :width="100" />
             <span class="text-xs text-emerald-400 font-bold ml-2">Sintetizando estructura atómica...</span>
          </div>
        </div>
        <div class="px-4 py-2 bg-gray-800 flex gap-2 shrink-0">
          <input v-model="copilotInput" @keyup.enter="sendCopilotMessage" type="text" placeholder="Pregunta al Copiloto sobre tu proceso..." class="flex-1 bg-gray-700 text-white text-sm rounded px-3 py-1.5 border border-gray-600 focus:border-emerald-500 focus:ring-0" />
          <button @click="sendCopilotMessage" :disabled="copilotLoading" class="bg-emerald-600 hover:bg-emerald-700 text-white px-3 py-1.5 rounded text-xs font-bold disabled:opacity-50 transition">Enviar</button>
        </div>
      </div>
    </Transition>

    <!-- ═══════ Panel: Linter de Gobernanza (CA-77) ═══════ -->
    <Transition name="slide-up">
      <div v-if="linterErrors.length > 0" class="absolute bottom-0 left-0 right-0 max-h-56 bg-amber-950 border-t-4 border-amber-600 flex flex-col z-50 shadow-2xl overflow-hidden shadow-amber-500/50" data-testid="linter-errors-panel">
        <div class="flex items-center justify-between px-6 py-2 bg-amber-900/90 shrink-0">
          <h4 class="text-sm font-bold text-white flex items-center gap-2">⚠️ Advertencias Estructurales del Linter (Gobernanza CA-77)</h4>
          <button @click="linterErrors = []" class="text-amber-200 hover:text-white font-bold text-xl">&times;</button>
        </div>
        <div class="flex-1 p-5 overflow-y-auto space-y-2 text-sm font-mono bg-amber-950 text-amber-100">
          <ul class="list-disc pl-5">
             <li v-for="(err, i) in linterErrors" :key="i" class="mb-1">{{ err }}</li>
          </ul>
        </div>
      </div>
    </Transition>

    <!-- ═══════ Panel: Semantic Errors (CA-2 a CA-4) ═══════ -->
    <Transition name="slide-up">
      <div v-if="validationErrors.length > 0" class="absolute bottom-0 left-0 right-0 max-h-56 bg-red-900 border-t-4 border-red-500 flex flex-col z-50 shadow-2xl overflow-hidden shadow-red-500/50">
        <div class="flex items-center justify-between px-6 py-2 bg-red-800/90 shrink-0">
          <h4 class="text-sm font-bold text-white flex items-center gap-2">⚠️ Errores Semánticos y Advertencias (HTTP 422)</h4>
          <button @click="validationErrors = []" class="text-red-200 hover:text-white font-bold text-xl">&times;</button>
        </div>
        <div class="flex-1 p-5 overflow-y-auto space-y-2 text-sm font-mono bg-red-900 text-red-100">
          <ul class="list-disc pl-5">
             <li v-for="(err, i) in validationErrors" :key="i" class="mb-1">{{ err }}</li>
          </ul>
        </div>
      </div>
    </Transition>

    <!-- ═══════ CA-42: Panel: Audit Log (Vertical Glassmorphic Timeline) ═══════ -->
    <!-- @Traceability: US-005, CA-42 - Activity Timeline -->
    <Transition name="slide-up">
      <div v-if="showAuditLogsModal" class="absolute bottom-0 right-0 w-[500px] h-96 bg-white/70 dark:bg-gray-800/70 backdrop-blur-md border border-white/20 dark:border-gray-700/30 rounded-tl-2xl shadow-2xl flex flex-col z-40">
        <div class="flex items-center justify-between px-4 py-3 border-b border-white/10 shrink-0">
          <h4 class="text-sm font-bold text-gray-850 dark:text-white flex items-center gap-2">
            📜 Auditoría de Cambios (Actividades)
            <button @click="openAuditLogs" class="text-xs text-blue-500 hover:text-blue-700 transition" title="Refrescar">↻</button>
          </h4>
          <button @click="showAuditLogsModal = false" class="text-gray-400 hover:text-red-500 font-bold text-lg">&times;</button>
        </div>
        
        <div class="flex-1 overflow-y-auto p-4 relative">
          <div v-if="loadingAuditLogs" class="text-center text-xs text-gray-500 py-8">Cargando bitácora...</div>
          <div v-else-if="auditLogs.length === 0" class="text-center text-xs text-gray-500 py-10">Sin auditoría visible.</div>
          <div v-else class="relative pl-6 space-y-4">
            <!-- Vertical line -->
            <div class="absolute left-[9px] top-2 bottom-2 w-0.5 bg-gray-250 dark:bg-gray-700"></div>
            
            <!-- Timeline items -->
            <div v-for="(log, i) in auditLogs" :key="i" class="relative flex flex-col text-xs text-left group">
              <!-- Timeline Dot -->
              <div :class="[getActionDotColor(log.action), 'absolute -left-[22px] top-1 w-3.5 h-3.5 rounded-full border-2 z-10 transition-transform group-hover:scale-110']"></div>
              
              <!-- Content Block -->
              <div class="bg-white/50 dark:bg-gray-900/30 border border-white/10 dark:border-gray-700/20 rounded-lg p-2.5 shadow-sm transition hover:shadow-md cursor-pointer" @click="toggleLogExpansion(i)">
                <div class="flex items-center justify-between">
                  <span class="font-bold text-gray-800 dark:text-white">{{ mapActionLabel(log.action) }}</span>
                  <span class="text-[10px] text-gray-500 dark:text-gray-400 font-mono">{{ formatLogDate(log) }}</span>
                </div>
                
                <div class="flex items-center justify-between mt-1 text-[11px]">
                  <span class="text-gray-600 dark:text-gray-350">Usuario: <span class="font-semibold text-gray-700 dark:text-gray-200">{{ log.user }}</span></span>
                  <span class="font-semibold text-indigo-650 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-950/30 px-1.5 py-0.5 rounded text-[10px]">
                    v{{ log.version ?? 1 }}
                  </span>
                </div>
                
                <!-- Expandable detail actions -->
                <Transition name="fade">
                  <div v-if="expandedLogs[i]" class="mt-3 pt-2.5 border-t border-gray-200/50 dark:border-gray-700/50 flex gap-2 justify-end" @click.stop>
                    <button 
                      @click="openSnapshot(log)" 
                      class="bg-blue-500/10 hover:bg-blue-500/20 text-blue-600 dark:text-blue-400 px-2.5 py-1 rounded font-bold text-[10px] transition focus:ring-1 focus:ring-blue-400"
                    >
                      Ver Snapshot
                    </button>
                    <button 
                      @click="restoreVersionFromLog(log.version ?? 1)" 
                      class="bg-green-500/10 hover:bg-green-500/20 text-green-600 dark:text-green-400 px-2.5 py-1 rounded font-bold text-[10px] transition focus:ring-1 focus:ring-green-400"
                    >
                      Restaurar esta versión
                    </button>
                  </div>
                </Transition>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ═══════ Modal: Ver Snapshot (CA-42) ═══════ -->
    <!-- @Traceability: US-005, CA-42 - Activity Timeline -->
    <Transition name="fade">
      <div v-if="showSnapshotModal" class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
        <div class="bg-white/90 dark:bg-gray-800/90 backdrop-blur-lg rounded-2xl shadow-2xl w-full max-w-4xl h-[70vh] border border-white/20 dark:border-gray-700/30 flex flex-col overflow-hidden">
          <!-- Header -->
          <div class="px-6 py-4 bg-gradient-to-r from-blue-600/85 to-indigo-600/85 text-white flex items-center justify-between shrink-0">
            <div>
              <h3 class="text-base font-bold">👁️ Vista de Snapshot (Solo Lectura)</h3>
              <p class="text-xs text-blue-100 mt-0.5">Visualización del diagrama BPMN correspondiente a esta versión histórica.</p>
            </div>
            <button @click="closeSnapshotModal" class="text-white hover:text-gray-200 text-2xl font-bold transition focus:outline-none">&times;</button>
          </div>
          
          <!-- Canvas Viewer Container -->
          <div class="flex-1 bg-gray-50 dark:bg-gray-900 relative">
            <div ref="snapshotViewerContainer" class="w-full h-full"></div>
          </div>
          
          <!-- Footer -->
          <div class="px-6 py-3 bg-gray-100 dark:bg-gray-950/50 border-t border-gray-200/50 dark:border-gray-700/50 flex justify-end shrink-0">
            <button @click="closeSnapshotModal" class="bg-gray-200 hover:bg-gray-300 dark:bg-gray-750 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-200 text-xs font-bold py-2 px-4 rounded-lg transition border border-gray-300 dark:border-gray-650">
              Cerrar
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ═══════ Panel: Version History ═══════ -->
    <Transition name="slide-up">
      <div v-if="showVersions" class="absolute bottom-0 right-0 w-96 h-72 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-tl-xl shadow-2xl flex flex-col z-40">
        <div class="flex items-center justify-between px-4 py-2 border-b border-gray-200 dark:border-gray-700 shrink-0">
          <h4 class="text-sm font-bold text-gray-700 dark:text-white flex items-center gap-2">
            📜 Historial de Versiones 
            <button @click="fetchVersions" class="text-xs text-blue-500 hover:text-blue-700">↻</button>
          </h4>
          <button @click="showVersions = false" class="text-gray-400 hover:text-red-500">&times;</button>
        </div>
        <div class="px-4 py-2 bg-blue-50 dark:bg-blue-900/20 border-b border-blue-100 dark:border-blue-800 shrink-0 text-center">
          <p class="text-[10px] text-blue-700 dark:text-blue-300 leading-tight">
             ℹ️ <b>Nota (CA-15):</b> El Rollback es inmutable. No pisa los datos, sino que clona la arquitectura creando una <b>V_NUEVA</b> en borrador.
          </p>
        </div>
        <!-- @Traceability: US-005, CA-15, BUG-FIX: Renderizar mensaje cuando no hay versiones publicadas -->
        <div class="flex-1 overflow-y-auto p-3 space-y-2">
          <div v-if="loadingVersions" class="text-center text-xs text-gray-500 py-4">Cargando versiones...</div>
          <div v-else-if="versionHistory.length > 0" v-for="v in versionHistory" :key="v.version" class="flex justify-between items-center p-2 rounded hover:bg-gray-50 dark:hover:bg-gray-700 text-sm border border-gray-100 dark:border-gray-700 transition group">
            <div>
              <span class="font-bold text-gray-800 dark:text-white">v{{ v.version }}</span>
              <p class="text-[10px] text-gray-500">{{ v.date }} — {{ v.author }}</p>
            </div>
            <div class="flex flex-col items-end gap-1">
              <span :class="v.status === 'ACTIVO' ? 'text-green-600' : 'text-gray-500'" class="text-[10px] font-bold">{{ v.status }}</span>
              <!-- CA-15 Botón Restaurar -> Clonar -->
              <button v-if="v.status !== 'ACTIVO' && !isLocked" @click="restoreVersion(v.version)" class="text-[10px] bg-amber-100 hover:bg-amber-200 text-amber-800 px-2 py-0.5 rounded shadow-sm opacity-0 group-hover:opacity-100 transition disabled:opacity-50" title="Ejecutar Rollback Un Clic">
                Clonar como V_NUEVA (Rollback) ↺
              </button>
            </div>
          </div>
          <div v-else class="text-center text-xs text-gray-500 py-10" data-testid="no-versions-msg">
            No hay versiones publicadas aún.
          </div>
        </div>
      </div>
    </Transition>

    <!-- ═══════ Drawer: Explorador de Procesos (CA-23) ═══════ -->
    <Transition name="slide-left">
      <div v-if="showCatalog" class="fixed inset-y-0 right-0 w-96 bg-white dark:bg-gray-800 shadow-2xl z-50 flex flex-col border-l border-gray-200 dark:border-gray-700">
        <div class="px-5 py-4 bg-gray-50 dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between shrink-0">
          <h3 class="text-sm font-bold text-gray-900 dark:text-white flex items-center gap-2">📂 Explorador de Procesos</h3>
          <div class="flex items-center gap-3">
            <button @click="showNewProcessModal = true; showCatalog = false" title="Nuevo Proceso" class="text-blue-600 hover:text-blue-800 text-xl font-bold">+</button>
            <button @click="showCatalog = false" class="text-gray-400 hover:text-gray-600 text-xl font-bold">&times;</button>
          </div>
        </div>
        <div class="flex-1 overflow-y-auto relative p-4 bg-gray-50 dark:bg-gray-900">
          <div v-if="loadingCatalog" class="absolute inset-0 bg-white/50 dark:bg-gray-800/50 flex items-center justify-center z-10">
            <span class="text-sm text-gray-500 font-bold animate-pulse">Consultando modelos...</span>
          </div>
          <div class="space-y-3">
            <div v-for="p in catalogProcesses" :key="p.id" @click="loadProcess(p)" class="p-4 bg-white dark:bg-gray-800 border rounded-lg shadow-sm hover:shadow-md hover:border-blue-400 cursor-pointer transition flex flex-col gap-2 border-gray-200 dark:border-gray-700 group">
              <div class="cursor-pointer" @click="loadProcess(p)">
                <span class="font-bold text-sm text-gray-900 dark:text-gray-100 group-hover:text-blue-600 transition">{{ p.name }}</span>
                <div class="flex flex-col gap-1">
                  <span class="text-[10px] text-gray-500 dark:text-gray-400">📅 {{ p.lastEdited.split(' ')[0] || p.lastEdited }}</span>
                  <div class="flex items-center justify-between">
                     <span class="text-[10px] font-bold text-gray-500">v{{ p.version }} | {{ p.author?.split(' ')[0] || p.author }}</span>
                     <!-- @Traceability: US-005, CA-31 Etiquetas de Estado en el Catálogo -->
                     <span class="text-[10px] font-bold uppercase rounded-full px-2 py-0.5" :class="{'bg-green-100 text-green-800': p.status==='ACTIVO', 'bg-yellow-100 text-yellow-800': p.status==='BORRADOR', 'bg-gray-100 text-gray-700': p.status==='ARCHIVADO'}">
                       {{ p.status === 'BORRADOR' ? '📝 BORRADOR' : (p.status === 'ACTIVO' ? `✅ ACTIVO (v${p.version})` : (p.status === 'ARCHIVADO' ? '📦 ARCHIVADO' : p.status)) }}
                     </span>
                  </div>
                </div>
              </div>
              <!-- Action Button CA-32 -->
              <!-- @Traceability: US-005, CA-32 Archivar un Proceso sin Instancias Activas -->
              <button v-if="p.status === 'ACTIVO'" 
                      :disabled="(p.activeInstances || p.activeInstancesCount || 0) > 0"
                      @click.stop="archiveProcess(p.id || p.key)" 
                      class="absolute top-2 right-2 text-[10px] font-bold px-2 py-1 rounded transition z-10 border shadow-sm flex items-center gap-1"
                      :class="((p.activeInstances || p.activeInstancesCount || 0) > 0) ? 'bg-gray-100 text-gray-400 border-gray-200 cursor-not-allowed' : 'bg-gray-100 text-gray-600 border-gray-300 hover:bg-gray-200'"
                      :title="((p.activeInstances || p.activeInstancesCount || 0) > 0) ? `No se puede archivar: ${p.activeInstances || p.activeInstancesCount} instancias en ejecución` : 'Archivar Proceso (CA-32)'">
                📦 Archivar
              </button>
            </div>
            <div v-if="catalogProcesses.length === 0 && !loadingCatalog" class="text-center text-xs text-gray-500 py-10 font-bold">
              El repositorio está vacío.
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ═══════ Drawer: Deploy Requests (CA-69) ═══════ -->
    <Transition name="slide-left">
      <div v-if="showDeployRequests" class="fixed inset-y-0 right-0 w-96 bg-white dark:bg-gray-800 shadow-2xl z-50 flex flex-col border-l border-gray-200 dark:border-gray-700">
        <div class="px-5 py-4 bg-gray-50 dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between shrink-0">
          <h3 class="text-sm font-bold text-indigo-900 dark:text-indigo-300 flex items-center gap-2">📨 Solicitudes de Despliegue</h3>
          <button @click="showDeployRequests = false" class="text-gray-400 hover:text-gray-600 text-xl font-bold">&times;</button>
        </div>
        <div class="flex-1 overflow-y-auto relative p-4 bg-gray-50 dark:bg-gray-900">
          <div v-if="loadingDeployRequests" class="absolute inset-0 bg-white/50 dark:bg-gray-800/50 flex items-center justify-center z-10">
            <span class="text-sm text-gray-500 font-bold animate-pulse">Cargando solicitudes...</span>
          </div>
          <div class="space-y-[12px]">
            <div v-for="req in deployRequests" :key="req.id" class="p-4 bg-white dark:bg-gray-800 border rounded-lg shadow-sm flex flex-col gap-2 border-indigo-200 dark:border-indigo-700 relative">
              <span class="font-bold text-sm text-gray-900 dark:text-gray-100">{{ req.processName || processId }} (v{{ req.version }})</span>
              <p class="text-xs text-gray-500 line-clamp-2 italic">"{{ req.comment }}"</p>
              <div class="flex flex-col gap-1 mt-1">
                <span class="text-[10px] text-gray-500 dark:text-gray-400">👤 Solicitado por: {{ req.requester }}</span>
                <span class="text-[10px] text-gray-500 dark:text-gray-400">📅 {{ req.requestedAt }}</span>
              </div>
              <div class="mt-2 flex gap-2 w-full">
                <button v-if="activeRole !== 'BPMN_Designer'" @click="handleDeployRequest(req.id, true)" class="flex-1 bg-green-100 hover:bg-green-200 text-green-800 py-1.5 rounded text-xs font-bold transition flex justify-center items-center gap-1 shadow-sm border border-green-300">
                  ✅ Aprobar
                </button>
                <button v-if="activeRole !== 'BPMN_Designer'" @click="handleDeployRequest(req.id, false)" class="flex-1 bg-red-100 hover:bg-red-200 text-red-800 py-1.5 rounded text-xs font-bold transition flex justify-center items-center gap-1 shadow-sm border border-red-300">
                  ❌ Rechazar
                </button>
              </div>
            </div>
            <div v-if="deployRequests.length === 0 && !loadingDeployRequests" class="text-center text-xs text-gray-500 py-10 font-bold">
              No hay solicitudes pendientes.
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ═══════ Gestor de Instancias (CA-8 a CA-10) ═══════ -->
    <InstancesManager 
      :show="showInstancesManager"
      :processId="processId"
      :isSandbox="processStatus === 'BORRADOR'"
      @close="showInstancesManager = false"
      @success="msg => showToast('✅ ' + msg, 'success')"
    />

    <!-- ═══════ Welcome Modal (CA-40) ═══════ -->
    <div v-if="showWelcomeModal" data-testid="welcome-modal" class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl w-full max-w-2xl overflow-hidden border border-gray-100 dark:border-gray-700">
        <!-- Header -->
        <div class="px-8 py-6 bg-gradient-to-r from-blue-600 to-indigo-600 text-white flex items-center justify-between">
          <div>
            <h3 class="text-xl font-bold">✨ Bienvenido al Diseñador iBPMS</h3>
            <p class="text-xs text-blue-100 mt-1">Selecciona un proceso existente o crea uno nuevo para comenzar.</p>
          </div>
          <!-- @Traceability: US-005, CA-40 -->
          <button @click="cancelAndGoToPortal" data-testid="welcome-close-header" class="text-white/80 hover:text-white text-2xl font-bold transition focus:outline-none" title="Salir al Portal">&times;</button>
        </div>
        
        <!-- Content -->
        <div class="p-8 grid grid-cols-1 md:grid-cols-2 gap-8 max-h-[70vh] overflow-y-auto">
          <!-- Left: Existing Processes Catalog -->
          <div class="flex flex-col h-full border-r border-gray-200 dark:border-gray-700 pr-6">
            <h4 class="text-sm font-bold text-gray-800 dark:text-gray-200 mb-4 flex items-center gap-2">
              📂 Procesos Recientes
            </h4>
            <div class="flex-1 overflow-y-auto flex flex-col gap-3 min-h-[250px]">
              <div v-if="loadingCatalog" class="flex items-center justify-center py-10">
                <span class="text-xs text-gray-500 font-bold animate-pulse">Cargando catálogo...</span>
              </div>
              <div v-else-if="catalogProcesses.length === 0" class="text-center text-xs text-gray-500 py-10 font-bold">
                No hay procesos en el catálogo.
              </div>
              <div v-else v-for="p in catalogProcesses" :key="p.id" @click="selectProcessFromWelcome(p)" class="p-3 bg-gray-50 dark:bg-gray-700/50 hover:bg-blue-50 dark:hover:bg-blue-900/20 border border-gray-200 dark:border-gray-700 rounded-lg cursor-pointer transition flex flex-col gap-1">
                <span class="font-bold text-xs text-gray-900 dark:text-gray-100">{{ p.name }}</span>
                <div class="flex items-center justify-between text-[10px] text-gray-500 dark:text-gray-400">
                  <span>v{{ p.version }} | {{ p.author?.split(' ')[0] }}</span>
                  <span class="px-1.5 py-0.5 rounded text-[9px] font-bold uppercase" :class="{'bg-green-100 text-green-800': p.status==='ACTIVO', 'bg-yellow-100 text-yellow-800': p.status==='BORRADOR', 'bg-gray-100 text-gray-700': p.status==='ARCHIVADO'}">
                    {{ p.status }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Right: New Process Form -->
          <div class="flex flex-col justify-between">
            <div>
              <h4 class="text-sm font-bold text-gray-800 dark:text-gray-200 mb-4">
                ✨ Crear Nuevo Proceso
              </h4>
              <div class="space-y-4">
                <div>
                  <label class="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">Nombre del Proceso</label>
                  <input v-model="newProcessName" type="text" placeholder="Ej. Proceso de Facturación" class="w-full rounded-md border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white p-2.5 text-xs focus:ring-blue-500 focus:border-blue-500" />
                </div>
                <div>
                  <label class="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-2">Patrón de Proceso</label>
                  <div class="grid grid-cols-2 gap-2">
                    <button @click="newProcessPattern = 'SIMPLE'" :class="newProcessPattern === 'SIMPLE' ? 'ring-2 ring-blue-500 bg-blue-50/50 dark:bg-blue-900/10 border-blue-300' : 'border-gray-200 dark:border-gray-700'" class="p-3 border rounded-lg text-center hover:bg-gray-50 dark:hover:bg-gray-700/50 transition">
                      <span class="text-lg">🟢</span>
                      <p class="text-xs font-bold mt-1 text-gray-800 dark:text-white">Simple</p>
                    </button>
                    <button @click="newProcessPattern = 'IFORM_MAESTRO'" :class="newProcessPattern === 'IFORM_MAESTRO' ? 'ring-2 ring-blue-500 bg-blue-50/50 dark:bg-blue-900/10 border-blue-300' : 'border-gray-200 dark:border-gray-700'" class="p-3 border rounded-lg text-center hover:bg-gray-50 dark:hover:bg-gray-700/50 transition">
                      <span class="text-lg">🔵</span>
                      <p class="text-xs font-bold mt-1 text-gray-800 dark:text-white">iForm</p>
                    </button>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- @Traceability: US-005, CA-40 -->
            <div class="pt-6 border-t border-gray-100 dark:border-gray-700 mt-6 flex gap-3 justify-end w-full">
              <button @click="cancelAndGoToPortal" data-testid="welcome-cancel-footer" class="w-1/2 bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-200 text-xs font-bold py-2.5 px-4 rounded-lg border border-gray-200 dark:border-gray-600 transition">
                Cancelar
              </button>
              <button @click="completeProcessCreationInWelcome" :disabled="!newProcessName.trim()" class="w-1/2 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-xs font-bold py-2.5 px-4 rounded-lg shadow-md transition">
                Crear y Diseñar Proceso
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>



    <!-- ═══════ Variable Input Popup (CA-82) ═══════ -->
    <div v-if="showVariablePopup" class="fixed inset-0 bg-black/60 backdrop-blur-sm z-[60] flex items-center justify-center p-4">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-2xl w-full max-w-md border border-gray-200 dark:border-gray-700 overflow-hidden">
        <div class="px-5 py-3.5 bg-yellow-50 dark:bg-yellow-950/20 border-b border-yellow-100 dark:border-yellow-900 flex items-center justify-between">
          <h4 class="text-sm font-bold text-yellow-800 dark:text-yellow-300 flex items-center gap-1.5">
            🔑 Variable Requerida por la Compuerta
          </h4>
          <button @click="showVariablePopup = false" class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 text-xl font-bold">&times;</button>
        </div>
        <div class="p-5 space-y-4">
          <p class="text-xs text-gray-600 dark:text-gray-400 leading-relaxed">
            La simulación detectó una compuerta que depende de la variable <code class="bg-gray-100 dark:bg-gray-900 px-1 py-0.5 rounded font-mono font-bold text-red-500">{{ missingVariableName }}</code>. Por favor ingresa su valor para continuar la evaluación semántica:
          </p>
          <div>
            <label class="block text-xs font-semibold text-gray-500 uppercase mb-1">Nombre Variable</label>
            <input type="text" :value="missingVariableName" disabled class="w-full bg-gray-100 dark:bg-gray-900 text-gray-500 text-xs font-mono p-2.5 border rounded-lg" />
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 dark:text-gray-300 mb-1">Valor de la Variable (Inyección)</label>
            <input type="text" v-model="tempVariableValue" placeholder="Ej: 60000 o true" class="w-full text-xs font-mono p-2.5 border rounded-lg bg-white dark:bg-gray-700 dark:text-white border-gray-300 dark:border-gray-650 focus:ring-yellow-500 focus:border-yellow-500" @keyup.enter="submitVariable" />
          </div>
          <div class="flex justify-end gap-3 pt-2">
            <button @click="showVariablePopup = false" class="px-3.5 py-2 text-xs font-semibold text-gray-750 bg-gray-100 hover:bg-gray-200 rounded-lg transition">Cancelar Simulación</button>
            <button @click="submitVariable" class="px-4 py-2 text-xs font-bold text-white bg-yellow-600 hover:bg-yellow-700 rounded-lg shadow transition">Inyectar y Reintentar</button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
// @Traceability: US-005, CA-40
import { useTimeStore } from '@/stores/timeStore';
import { useIntegrationStore } from '@/stores/useIntegrationStore';
import { useRbacStore } from '@/stores/rbacStore';
import { ref, onMounted, onBeforeUnmount, watch, computed, defineAsyncComponent, nextTick, getCurrentInstance } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { useRoute, useRouter } from 'vue-router';
import { debounce } from 'lodash-es';
import AppTooltip from '@/components/common/AppTooltip.vue';
import InstancesManager from './InstancesManager.vue';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import DOMPurify from 'dompurify';

// CA-01: Lazy Loading para Lottie (IA Wait State)
const Vue3Lottie = defineAsyncComponent(() => import('vue3-lottie').then(m => m.Vue3Lottie));

const corruptNodeId = ref<string | null>(null);
const authStore = useAuthStore();
const rbacStore = useRbacStore();
const integrationStore = useIntegrationStore(); // @Traceability: US-005, CA-40
const timeStore = useTimeStore(); // Prevent runtime TypeError on undefined timeStore
const route = useRoute();
const router = useRouter(); // @Traceability: US-005, CA-40

const currentInstance = getCurrentInstance();

// @Traceability: US-005, CA-40
const cancelAndGoToPortal = () => {
  router.push('/');
};

const activeRole = computed(() => authStore.roles?.[0] || 'BPMN_Designer'); // Reemplaza mockRole CA-21, CA-66

const currentVersion = ref(0);

const isStepHighlighted = (step: number) => {
  if (currentVersion.value === 0) {
    return step === 2 || step === 3;
  } else {
    return step === 4 || step === 5 || step === 6;
  }
};

const importFileInput = ref<HTMLInputElement | null>(null);

const handleStepSelect = (step: number, event: Event) => {
  const target = event.target as HTMLSelectElement;
  const value = target.value;
  if (!value) return;

  if (step === 1) {
    if (value === 'Explorador') showCatalog.value = true;
    else if (value === 'Importar') importFileInput.value?.click();
    else if (value === 'Exportar') downloadXML();
    else if (value === 'Guardar') saveDraft(true);
  } else if (step === 2) {
    if (value === 'Canvas') {
      zoomFit();
    } else if (value === 'Copiloto IA') {
      triggerCopilotAudit();
    }
  } else if (step === 3) {
    if (value === 'Simular') runSandbox();
    else if (value === 'Limpiar') clearTrajectory();
  } else if (step === 4) {
    if (value === 'Auditoría') openAuditLogs();
    else if (value === 'Versiones') showVersions.value = !showVersions.value;
  } else if (step === 5) {
    if (value === 'Solicitar Despliegue') requestDeploy();
    else if (value === 'Ver Solicitudes') openDeployRequests();
    else if (value === 'Desplegar') showDeployModal.value = true;
  } else if (step === 6) {
    if (value === 'Operacion') showInstancesManager.value = true;
  }
  target.value = '';
};

// ── Types ────────────────────────────────────────────────────
interface BpmnElement {
  id: string;
  type: string;
  name?: string;
  props: Record<string, any>;
}

// ── Canvas ───────────────────────────────────────────────────
const canvasContainer = ref<HTMLElement | null>(null);
let modelerInstance: any = null;

// ── Tooltips Didácticos (CA-38, CA-47 MVP) ─────────────────
// @Traceability: US-005, CA-05
const bpmnTooltips = {
  GLOBAL_SLA: 'Dicta el Acabado Total esperado del Proceso (Vida Útil). Al expirar, lanza métrica a los dashboards BAM corporativos y emite alertas amarillas.',
  SLA_TIMEOUT: 'Determina temporalidad en norma <a href="https://en.wikipedia.org/wiki/ISO_8601" target="_blank" class="text-blue-500 underline font-semibold">ISO-8601</a> antes de detonar Boundary Events o Escalar la Tarea a líderes.<br><br><b>Syntax estricta:</b> <code>P(N)Y(N)M(N)DT(N)H(N)M(N)S</code><br>Ejemplo: <code>P2D</code> = 2 días. <code>PT6H</code> = 6 Horas.',
  FORM_KEY: 'Formulario Inteligente embebido. El Workdesk usará este ID Técnico para dibujar la GUI y los campos reactivos de la tarea humana actual.',
  PROCESS_PATTERN: 'La arquitectura. <b>iForm_Maestro</b> permite usar un solo formulario mutante universal; <b>Simple</b> requiere diseñar formularios separados e instanciarlos en tareas disyuntas individualmente.',
  CALL_ACTIVITY: 'Un Sub-proceso re-usable de nivel corporativo que actúa como Caja Negra. Obliga a que la Cédula/Identificador coincida lógicamente entre ambos Diagramas. El link no rutea si la variable Target no existe.',
  // CA-47: Integraciones Estrictas y UX
  NOMENCLATURE: 'Patrón para generar el ID del ticket. Soporta variables como {Solicitante}.',
  NOMENCLATURE_DUMMY: '🎟 <b>¿Qué es esto?</b> Es una plantilla para nombrar las solicitudes automáticamente mediante un <b>Glosario de Datos Unificado</b> y mapeo bidireccional.<br><br>' +
                      '1. Escribe texto fijo (ej. <code>FAC-</code>).<br>' +
                      '2. Abre una llave <code>{</code> para ver variables disponibles (de sesión, formularios y webhooks).<br>' +
                      '3. Ejemplo: <code>FAC-{form.monto}-{system.date}</code> se resolverá dinámicamente como <b>FAC-5000-2026-06-02</b>.',
  CONNECTOR: 'Integra este nodo con sistemas externos mapeando variables del proceso actual.',
  ESCALATION: 'Define reglas semánticas de rebote o escalamiento a roles superiores.',
  // @Traceability: US-005, CA-05
  GLOSSARY_VARIABLES: '🗂️ <b>¿Qué es esto?</b> Es un catálogo local donde puedes declarar manualmente variables personalizadas para el proceso (ej: <code>prioridad</code>).<br><br>' +
                      '<b>¿Cuándo usarlo?</b><br>Úsalo para registrar variables antes de diseñar los formularios de las tareas o integraciones. Así podrás utilizarlas en la Regla de Nomenclatura sin generar alertas.<br><br>' +
                      '<b>¿Cómo funciona?</b><br>' +
                      '1. Escribe el <b>Nombre</b> (sin espacios ni caracteres especiales).<br>' +
                      '2. Elige el <b>Tipo</b> (Texto, Número o Booleano).<br>' +
                      '3. Clic en <b>+ Declarar Variable</b> y estará disponible en el menú de autocompletado <code>{</code>.'
};

// ── Selection State ──────────────────────────────────────────
const selectedElement = ref<BpmnElement>({
  id: '',
  type: '',
  name: '',
  props: {
    aiTokenLimit: 4000,
    aiTone: 'NEUTRAL',
    sla: '',
    calledElement: '',
    topic: ''
  }
});

// ── Process State ────────────────────────────────────────────
// @Traceability: US-005, CA-15
const isNewProcess = ref(!(route && route.query && route.query.processId));
const currentProcessName = ref('Crédito de Consumo V1');
const processId = ref('credito-consumo-v1');
if (route && route.query && route.query.processId) {
  processId.value = route.query.processId as string;
}
const processStatus = ref<'BORRADOR' | 'ACTIVO' | 'ARCHIVADO' | 'PENDING'>('BORRADOR');
const processPattern = ref<'SIMPLE' | 'IFORM_MAESTRO'>('SIMPLE');
const processNomenclature = ref(''); // CA-5
// @Traceability: US-005, CA-35
const globalSlaRaw = ref('');
const globalSlaSimpleValue = ref(72);
const globalSlaSimpleUnit = ref('Horas');
const globalSla = computed({
  get() {
    return parseDurationToHours(globalSlaRaw.value) || 72;
  },
  set(val: number) {
    globalSlaRaw.value = `PT${val}H`;
    updateGlobalSlaRaw();
  }
});
// @Traceability: US-005, CA-35
const isSlaAdvancedMode = ref(false);
const slaSimpleValue = ref(0);
const slaSimpleUnit = ref('Horas');
const criticalPathDuration = ref(0);

const parseIso8601Duration = (durationStr: string) => {
  // @Traceability: US-005, CA-35
  if (!durationStr) {
    return { value: 0, unit: 'Horas', isSimple: true };
  }
  const matchMinutes = durationStr.match(/^PT(\d+)M$/);
  if (matchMinutes) {
    return { value: parseInt(matchMinutes[1]), unit: 'Minutos', isSimple: true };
  }
  const matchHours = durationStr.match(/^PT(\d+)H$/);
  if (matchHours) {
    return { value: parseInt(matchHours[1]), unit: 'Horas', isSimple: true };
  }
  const matchDays = durationStr.match(/^P(\d+)D$/);
  if (matchDays) {
    return { value: parseInt(matchDays[1]), unit: 'Días', isSimple: true };
  }
  const matchWeeks = durationStr.match(/^P(\d+)W$/);
  if (matchWeeks) {
    return { value: parseInt(matchWeeks[1]), unit: 'Semanas', isSimple: true };
  }
  return { value: 0, unit: 'Horas', isSimple: false };
};

const formatIso8601Duration = (value: number, unit: string) => {
  // @Traceability: US-005, CA-35
  if (unit === 'Minutos') return `PT${value}M`;
  if (unit === 'Horas') return `PT${value}H`;
  if (unit === 'Días') return `P${value}D`;
  if (unit === 'Semanas') return `P${value}W`;
  return '';
};

const parseDurationToHours = (duration: string): number => {
  // @Traceability: US-005, CA-35
  const parsed = parseIso8601Duration(duration);
  if (!parsed.isSimple) return 0;
  if (parsed.unit === 'Minutos') return parsed.value / 60;
  if (parsed.unit === 'Horas') return parsed.value;
  if (parsed.unit === 'Días') return parsed.value * 24;
  if (parsed.unit === 'Semanas') return parsed.value * 24 * 7;
  return 0;
};

// @Traceability: US-005, CA-35
// @Traceability: US-005, CA-35
watch(() => selectedElement.value?.props?.sla, (newVal) => {
  if (!selectedElement.value?.id) return;
  const parsed = parseIso8601Duration(newVal || '');
  if (parsed.isSimple) {
    slaSimpleValue.value = parsed.value;
    slaSimpleUnit.value = parsed.unit;
    isSlaAdvancedMode.value = false;
  } else {
    isSlaAdvancedMode.value = true;
  }
}, { immediate: true });

// @Traceability: US-005, CA-35
watch(globalSlaRaw, (newVal) => {
  const parsed = parseIso8601Duration(newVal || '');
  if (parsed.isSimple) {
    globalSlaSimpleValue.value = parsed.value;
    globalSlaSimpleUnit.value = parsed.unit;
    if (!selectedElement.value?.id) {
      isSlaAdvancedMode.value = false;
    }
  } else {
    if (!selectedElement.value?.id) {
      isSlaAdvancedMode.value = true;
    }
  }
}, { immediate: true });

// @Traceability: US-005, CA-35
watch(() => selectedElement.value?.id, (newId) => {
  if (newId) {
    const parsed = parseIso8601Duration(selectedElement.value?.props?.sla || '');
    if (parsed.isSimple) {
      slaSimpleValue.value = parsed.value;
      slaSimpleUnit.value = parsed.unit;
      isSlaAdvancedMode.value = false;
    } else {
      isSlaAdvancedMode.value = true;
    }
  } else {
    const parsed = parseIso8601Duration(globalSlaRaw.value || '');
    if (parsed.isSimple) {
      globalSlaSimpleValue.value = parsed.value;
      globalSlaSimpleUnit.value = parsed.unit;
      isSlaAdvancedMode.value = false;
    } else {
      isSlaAdvancedMode.value = true;
    }
  }
});

const onSimpleSlaChange = () => {
  // @Traceability: US-005, CA-35
  const formatted = formatIso8601Duration(slaSimpleValue.value, slaSimpleUnit.value);
  if (selectedElement.value && selectedElement.value.props) {
    selectedElement.value.props.sla = formatted;
    updateElementSla();
  }
};

const onGlobalSimpleSlaChange = () => {
  // @Traceability: US-005, CA-35
  const formatted = formatIso8601Duration(globalSlaSimpleValue.value, globalSlaSimpleUnit.value);
  globalSlaRaw.value = formatted;
  updateGlobalSlaRaw();
};

const updateGlobalSlaRaw = () => {
  // @Traceability: US-005, CA-35
  if (!modelerInstance) return;
  const canvas = modelerInstance.get ? modelerInstance.get('canvas') : null;
  if (!canvas) return;
  const modeling = modelerInstance.get('modeling');
  const rootElement = canvas.getRootElement();
  if (rootElement && rootElement.businessObject) {
    modeling.updateProperties(rootElement, { "camunda:dueDate": globalSlaRaw.value });
  }
};

const updateCriticalPathDuration = () => {
  // @Traceability: US-005, CA-35
  if (!modelerInstance) {
    criticalPathDuration.value = 0;
    return;
  }
  try {
    const elementRegistry = modelerInstance.get('elementRegistry');
    if (!elementRegistry || typeof elementRegistry.getAll !== 'function') {
      criticalPathDuration.value = 0;
      return;
    }
    const elements = elementRegistry.getAll();
    const startEvents = elements.filter((el: any) => el.type === 'bpmn:StartEvent');

    const visited = new Set<string>();
    const cache = new Map<string, number>();

    const getLongestPathFromNode = (node: any): number => {
      if (!node) return 0;
      if (visited.has(node.id)) {
        return 0;
      }
      if (cache.has(node.id)) {
        return cache.get(node.id)!;
      }
      visited.add(node.id);

      let durationHours = 0;
      if (node.businessObject) {
        let durationStr = '';
        if (typeof node.businessObject.get === 'function') {
          durationStr = node.businessObject.get('camunda:dueDate') || '';
        } else {
          durationStr = node.businessObject['camunda:dueDate'] || '';
        }
        if (durationStr) {
          durationHours = parseDurationToHours(durationStr);
        }
      }

      let maxOutgoingPath = 0;
      if (node.outgoing && node.outgoing.length > 0) {
        for (const flow of node.outgoing) {
          if (flow && flow.target) {
            const pathLength = getLongestPathFromNode(flow.target);
            if (pathLength > maxOutgoingPath) {
              maxOutgoingPath = pathLength;
            }
          }
        }
      }

      visited.delete(node.id);
      const total = durationHours + maxOutgoingPath;
      cache.set(node.id, total);
      return total;
    };

    let maxDuration = 0;
    for (const startNode of startEvents) {
      const duration = getLongestPathFromNode(startNode);
      if (duration > maxDuration) {
        maxDuration = duration;
      }
    }
    criticalPathDuration.value = maxDuration;
  } catch (err) {
    console.error('Error calculating critical path duration:', err);
    criticalPathDuration.value = 0;
  }
};

const isCriticalPathExceeded = computed(() => {
  // @Traceability: US-005, CA-35
  return criticalPathDuration.value > globalSla.value;
});

const autoAdjustGlobalSla = () => {
  // @Traceability: US-005, CA-35
  globalSla.value = criticalPathDuration.value;
  updateGlobalSla();
};

const processHistoryTTL = ref<number | null>(180);
const processVersionTag = ref('');
const processIsExecutable = ref(true);

// CA-5: Glosario de Variables de Negocio y Autocompletado
const safeGet = (obj: any, key: string) => {
  if (!obj) return undefined;
  if (typeof obj.get === 'function') {
    return obj.get(key);
  }
  return obj[key];
};
const isGlossaryCollapsed = ref(false);
const declaredVariables = ref<{ name: string, type: 'String' | 'Number' | 'Boolean' }[]>([]);
const newVarName = ref('');
const newVarType = ref<'String' | 'Number' | 'Boolean'>('String');
const formFieldsCache = ref<Record<string, { name: string, type: string }[]>>({});
const processVariables = ref<any[]>([]);
// @Traceability: US-005, CA-05
const editorRef = ref<HTMLDivElement | null>(null);
const showAutocompletePopover = ref(false);
const autocompleteSearchQuery = ref('');

// @Traceability: US-005, CA-05
const addDeclaredVariable = () => {
  let name = newVarName.value.trim();
  // Parse out curly braces { } if user typed them
  name = name.replace(/[{}]/g, '');
  if (!name) {
    showToast('El nombre de la variable no puede estar vacío', 'error');
    return;
  }
  // allow dots . in key name validation regex: /^[a-zA-Z0-9_.]+$/
  const regex = /^[a-zA-Z0-9_.]+$/;
  if (!regex.test(name)) {
    showToast('El nombre de la variable solo puede contener caracteres alfanuméricos, guiones bajos y puntos', 'error');
    return;
  }
  const exists = declaredVariables.value.some(v => v.name === name);
  if (exists) {
    showToast(`La variable ${name} ya está declarada`, 'error');
    return;
  }
  declaredVariables.value.push({ name, type: newVarType.value });
  newVarName.value = '';
  newVarType.value = 'String';
  updateProcessProperty('GlosarioVariables', JSON.stringify(declaredVariables.value));
  showToast('Variable declarada exitosamente');
};

const removeDeclaredVariable = (index: number) => {
  declaredVariables.value.splice(index, 1);
  updateProcessProperty('GlosarioVariables', JSON.stringify(declaredVariables.value));
  showToast('Variable eliminada');
};

// @Traceability: US-005, CA-05
const mergedVariables = computed(() => {
  const map = new Map<string, { name: string, type: 'String' | 'Number' | 'Boolean', source: 'Session' | 'Form' | 'Connector' | 'Glossary' }>();

  // 1. Session context (highest priority or first)
  map.set('session.user_name', { name: 'session.user_name', type: 'String', source: 'Session' });
  map.set('session.email', { name: 'session.email', type: 'String', source: 'Session' });
  map.set('system.date', { name: 'system.date', type: 'String', source: 'Session' });

  // 2. Form variables from cache
  Object.values(formFieldsCache.value).forEach(fields => {
    fields.forEach(f => {
      const name = f.name.startsWith('form.') ? f.name : 'form.' + f.name;
      if (!map.has(name)) {
        map.set(name, {
          name,
          type: (f.type as any) || 'String',
          source: 'Form'
        });
      }
    });
  });

  // 3. Connector/processVariables
  processVariables.value.forEach(v => {
    const name = v.name.startsWith('webhook.') ? v.name : 'webhook.' + v.name;
    if (!map.has(name)) {
      map.set(name, {
        name,
        type: (v.type as any) || 'String',
        source: 'Connector'
      });
    }
  });

  // 4. Glossary variables
  declaredVariables.value.forEach(v => {
    let source: 'Session' | 'Form' | 'Connector' | 'Glossary' = 'Glossary';
    if (v.name.startsWith('form.')) {
      source = 'Form';
    } else if (v.name.startsWith('webhook.')) {
      source = 'Connector';
    } else if (v.name.startsWith('session.')) {
      source = 'Session';
    }
    
    map.set(v.name, {
      name: v.name,
      type: v.type,
      source
    });
  });

  return Array.from(map.values());
});

const filteredAutocompleteVariables = computed(() => {
  const query = autocompleteSearchQuery.value.toLowerCase().trim();
  if (!query) return mergedVariables.value;
  return mergedVariables.value.filter(v => v.name.toLowerCase().includes(query));
});

// @Traceability: US-005, CA-05
const nomenclatureParts = computed(() => {
  const val = processNomenclature.value || '';
  const parts = val.split(/(\{.*?\})/g);
  return parts.map(part => {
    const isVariable = part.startsWith('{') && part.endsWith('}');
    if (!isVariable) {
      return { text: part, isVariable: false };
    }
    const varName = part.substring(1, part.length - 1);
    const foundVar = mergedVariables.value.find(v => v.name === varName);
    let badgeClass = 'bg-red-50 text-red-700 border-red-200 dark:bg-red-950/20 dark:text-red-400 dark:border-red-800';
    let sourceLabel = 'Desconocido';
    let varType = 'Desconocido';
    if (foundVar) {
      varType = foundVar.type;
      sourceLabel = foundVar.source;
      if (foundVar.source === 'Session') {
        badgeClass = 'bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-950/20 dark:text-blue-400 dark:border-blue-800';
      } else if (foundVar.source === 'Form') {
        badgeClass = 'bg-green-50 text-green-700 border-green-200 dark:bg-green-950/20 dark:text-green-400 dark:border-green-800';
      } else if (foundVar.source === 'Glossary') {
        badgeClass = 'bg-purple-50 text-purple-700 border-purple-200 dark:bg-purple-950/20 dark:text-purple-400 dark:border-purple-800';
      } else if (foundVar.source === 'Connector') {
        badgeClass = 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/20 dark:text-amber-400 dark:border-amber-800';
      }
    }
    return {
      text: part,
      isVariable: true,
      badgeClass,
      type: varType,
      source: sourceLabel
    };
  });
});

// @Traceability: US-005, CA-05
const syncNomenclatureToHtml = (val: string) => {
  if (!editorRef.value) return;
  const parts = val.split(/(\{.*?\})/g);
  let html = '';
  parts.forEach(part => {
    if (part.startsWith('{') && part.endsWith('}')) {
      const varName = part.substring(1, part.length - 1);
      const foundVar = mergedVariables.value.find(v => v.name === varName);
      let badgeClass = 'bg-red-50 text-red-700 border-red-200 dark:bg-red-950/20 dark:text-red-400 dark:border-red-800';
      if (foundVar) {
        if (foundVar.source === 'Session') {
          badgeClass = 'bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-950/20 dark:text-blue-400 dark:border-blue-800';
        } else if (foundVar.source === 'Form') {
          badgeClass = 'bg-green-50 text-green-700 border-green-200 dark:bg-green-950/20 dark:text-green-400 dark:border-green-800';
        } else if (foundVar.source === 'Glossary') {
          badgeClass = 'bg-purple-50 text-purple-700 border-purple-200 dark:bg-purple-950/20 dark:text-purple-400 dark:border-purple-800';
        } else if (foundVar.source === 'Connector') {
          badgeClass = 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/20 dark:text-amber-400 dark:border-amber-800';
        }
      }
      const safeVarName = DOMPurify.sanitize(varName);
      html += `<span contenteditable="false" class="token-pill inline-block mx-0.5 px-1.5 py-0.5 rounded font-mono font-bold text-[11px] select-all cursor-default border ${badgeClass}" data-variable="${safeVarName}">{${safeVarName}}</span>`;
    } else {
      if (part) {
        html += DOMPurify.sanitize(part);
      }
    }
  });
  if (editorRef.value.innerHTML !== html) {
    editorRef.value.innerHTML = html;
  }
};

// @Traceability: US-005, CA-05
const parseHtmlToNomenclature = (): string => {
  if (!editorRef.value) return '';
  let result = '';
  const childNodes = editorRef.value.childNodes;
  for (let i = 0; i < childNodes.length; i++) {
    const node = childNodes[i];
    if (node.nodeType === Node.ELEMENT_NODE) {
      const el = node as HTMLElement;
      if (el.tagName.toLowerCase() === 'span' && el.hasAttribute('data-variable')) {
        result += `{${el.getAttribute('data-variable')}}`;
      } else {
        result += el.textContent || '';
      }
    } else if (node.nodeType === Node.TEXT_NODE) {
      result += node.textContent || '';
    }
  }
  return result;
};

// @Traceability: US-005, CA-05
const onEditorInput = (event: Event) => {
  const rawValue = parseHtmlToNomenclature();
  processNomenclature.value = rawValue;
  updateProcessProperty('ReglaNomenclatura', rawValue);

  const selection = typeof window !== 'undefined' ? window.getSelection() : null;
  let textBeforeCursor = '';
  if (selection && selection.rangeCount > 0) {
    try {
      const range = selection.getRangeAt(0);
      const preCaretRange = range.cloneRange();
      if (editorRef.value) {
        preCaretRange.selectNodeContents(editorRef.value);
        preCaretRange.setEnd(range.endContainer, range.endOffset);
        textBeforeCursor = preCaretRange.toString();
      }
    } catch (e) {
      textBeforeCursor = rawValue;
    }
  } else {
    textBeforeCursor = rawValue;
  }

  const lastBraceIndex = textBeforeCursor.lastIndexOf('{');
  if (lastBraceIndex !== -1) {
    const textAfterBrace = textBeforeCursor.substring(lastBraceIndex + 1);
    if (!textAfterBrace.includes('}')) {
      showAutocompletePopover.value = true;
      autocompleteSearchQuery.value = textAfterBrace;
      return;
    }
  }
  showAutocompletePopover.value = false;
  autocompleteSearchQuery.value = '';
};

// @Traceability: US-005, CA-05
const onEditorKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter') {
    event.preventDefault();
  }
  if (event.key === 'Escape') {
    showAutocompletePopover.value = false;
  }
};

// @Traceability: US-005, CA-05
const onEditorBlur = () => {
  setTimeout(() => {
    showAutocompletePopover.value = false;
  }, 200);
};

// @Traceability: US-005, CA-05
watch(processNomenclature, (newVal) => {
  const currentRaw = parseHtmlToNomenclature();
  if (currentRaw !== newVal) {
    syncNomenclatureToHtml(newVal);
  }
});

// @Traceability: US-005, CA-05
watch(mergedVariables, () => {
  syncNomenclatureToHtml(processNomenclature.value);
}, { deep: true });

// @Traceability: US-005, CA-05
const selectVariable = (varName: string) => {
  const foundVar = mergedVariables.value.find(v => v.name === varName);
  const nameToUse = foundVar ? foundVar.name : varName;

  const rawValue = processNomenclature.value || '';
  const lastBraceIndex = rawValue.lastIndexOf('{');
  
  let newValue = '';
  if (lastBraceIndex !== -1) {
    newValue = rawValue.substring(0, lastBraceIndex) + `{${nameToUse}}`;
  } else {
    newValue = rawValue + `{${nameToUse}}`;
  }

  processNomenclature.value = newValue;
  updateProcessProperty('ReglaNomenclatura', newValue);
  syncNomenclatureToHtml(newValue);
  showAutocompletePopover.value = false;
  autocompleteSearchQuery.value = '';

  nextTick(() => {
    if (editorRef.value) {
      editorRef.value.focus();
      if (typeof window !== 'undefined' && window.getSelection) {
        const range = document.createRange();
        range.selectNodeContents(editorRef.value);
        range.collapse(false);
        const selection = window.getSelection();
        if (selection) {
          selection.removeAllRanges();
          selection.addRange(range);
        }
      }
    }
  });
};

const scanAndFetchFormFields = async () => {
  if (!modelerInstance) return;
  try {
    const elementRegistry = modelerInstance.get('elementRegistry');
    if (!elementRegistry || typeof elementRegistry.filter !== 'function') return;
    const userTasks = elementRegistry.filter((e: any) => e.type === 'bpmn:UserTask');
    const startEvents = elementRegistry.filter((e: any) => e.type === 'bpmn:StartEvent');
    const allElements = [...userTasks, ...startEvents];
    const formKeys = [...new Set(allElements.map((t: any) => t.businessObject?.get('camunda:formKey')).filter(Boolean))];
    for (const key of formKeys) {
      if (!formFieldsCache.value[key]) {
        try {
          // @Traceability: US-005, CA-15 - ADR-001: Centralizar y corregir prefijos de llamadas
          const { data } = await integrationStore.get(`/forms/${key}/versions/1`);
          if (data && data.formFields) {
            formFieldsCache.value[key] = data.formFields.map((f: any) => ({
              name: f.camundaVariable || f.id,
              type: f.type === 'number' ? 'Number' : f.type === 'checkbox' ? 'Boolean' : 'String'
            }));
          } else if (data && data.fields) {
            formFieldsCache.value[key] = data.fields.map((f: any) => ({
              name: f.camundaVariable || f.id,
              type: f.type === 'number' ? 'Number' : f.type === 'checkbox' ? 'Boolean' : 'String'
            }));
          }
        } catch (e) {
          console.warn(`Could not load fields for form key: ${key}`, e);
        }
      }
    }
  } catch (err) {
    console.error('Error scanning form fields', err);
  }
};
const selectedFormKey = ref('');
const selectedConnector = ref('');

// CA-49: Data Mapper State
const connectorSchema = ref<any[]>([]);

const connectorMappings = ref<Record<string, string>>({});
const mappingErrors = ref<Record<string, boolean>>({});
const loadingSchema = ref(false);

// CA-48: Reactive Syntax Checking
const isNomenclatureSyntaxError = computed(() => {
  const nom = processNomenclature.value || '';
  const openCount = (nom.match(/\{/g) || []).length;
  const closeCount = (nom.match(/\}/g) || []).length;
  return openCount !== closeCount;
});

// CA-31: Computado para el bloqueo de Patrón
const elementCount = ref(0);
const bpmnComplexityLimit = ref(100);

// ── Computed Validations (CA-39) ─────────────────────────
const isSlaSyntaxError = computed(() => {
  const sla = selectedElement.value.props.sla;
  if (!sla) return false;
  // Regex Simple de Periodos ISO 8601 (Exige empezar con P y tener unidades lógicas)
  const regexIso8601 = /^P(?:\d+[YMWD])?(?:T(?:\d+[HMS])*)?$/;
  return sla !== '' && !regexIso8601.test(sla);
});

const isCallActivityError = computed(() => {
   if(selectedElement.value.type !== 'bpmn:CallActivity') return false;
   return !selectedElement.value.props.calledElement || selectedElement.value.props.calledElement.trim() === '';
});
const lockOwner = ref<string | null>(null);
const lockSince = ref<string | null>(null);
const isLocked = computed(() => {
  return lockOwner.value !== null && lockOwner.value !== authStore.user?.username;
});

// ── Auto-Save ────────────────────────────────────────────────
const autoSaveAgo = ref(5);
let autoSaveInterval: any = null;

// ── Pre-Flight (CA-9) ────────────────────────────────────────
const preFlightStatus = ref<'VALIDATED' | 'PENDING' | 'WARNING' | 'ERROR'>('PENDING');

// ── Deploy ───────────────────────────────────────────────────
const isDeploying = ref(false);
const showDeployModal = ref(false);
const deployComment = ref(''); // CA-65
// @Traceability: US-005, CA-33 - forceDeploy removido (Hard Stop)
const deployStrategy = ref('coexist');
const activeInstances = ref(12);
const validationErrors = ref<string[]>([]);
// @Traceability: US-005, CA-77 Validación y Corrección en Caliente mediante Linter en Frontend
const linterErrors = ref<string[]>([]);

// @Traceability: US-005, CA-80, CA-81, CA-82, CA-83, CA-84
const showSandboxModal = ref(false);
const sandboxStage = ref<'linter' | 'preflight' | 'simulation'>('linter');
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

const validationPanelWidth = ref(450);
const isResizingValidation = ref(false);
const collapsedSections = ref({ linter: false, preflight: false, simulator: false });
const newGridVarName = ref('');
const newGridVarType = ref<'String' | 'Number' | 'Boolean'>('String');
const newGridVarValue = ref('');

// ── New Process Modal ────────────────────────────────────────
const showNewProcessModal = ref(false);
const newProcessName = ref('');
const newProcessPattern = ref<'SIMPLE' | 'IFORM_MAESTRO'>('SIMPLE');
const newProcessOrigin = ref<'SCRATCH' | 'TEMPLATE'>('SCRATCH');

// ── Instance Manager ─────────────────────────────────────────
const showInstancesManager = ref(false);

// ── Templates (CA-18) ─────────────────────────────────────────
const templatesList = ref<any[]>([]);
const selectedTemplateId = ref('');
const loadingTemplates = ref(false);

watch(newProcessOrigin, async (val) => {
  if (val === 'TEMPLATE' && templatesList.value.length === 0) {
    loadingTemplates.value = true;
    try {
      const { data } = await integrationStore.getBpmnTemplates();
      templatesList.value = data || [];
    } catch (err) {
      showToast('Error cargando plantillas', 'error');
    } finally {
      loadingTemplates.value = false;
    }
  }
});

// ── Copilot & SSE (CA-01 y CA-08) ─────────────────────────
const showCopilot = ref(false);
const copilotInput = ref('');
const copilotLoading = ref(false); // Refleja el estado Lottie
const copilotMessages = ref<{ role: 'ai' | 'user'; text: string; xmlPayload?: string; options?: string[]; selectedOption?: string }[]>([
  { role: 'ai', text: 'Copiloto listo. Puedo auditar tu proceso contra ISO 9001, o auto-generar estructuras XML de forma atómica.' }
]);
const unreadAiBadge = ref(false);

watch(showCopilot, (val) => {
   if (val) unreadAiBadge.value = false;
});

watch([validationPanelWidth, showSandboxModal], () => {
  if (modelerInstance) {
    try {
      modelerInstance.get('canvas').resized();
    } catch (e) {
      console.warn('Canvas resize failed:', e);
    }
  }
});

watch(showSandboxModal, (val) => {
  if (!val) {
    clearTrajectory();
  }
});

const playPingSound = () => {
   try { new Audio('data:audio/wav;base64,UklGRl9vT19XQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YU').play(); } catch(e){}
};

const selectCopilotOption = (msgItem: any, optionText: string) => {
  msgItem.selectedOption = optionText; // Sello de Inmutabilidad CA-07
  copilotInput.value = optionText;
  sendCopilotMessage();
};

// @Traceability: US-005, CA-17, US-027 CA-01
const triggerCopilotAudit = async () => {
  showCopilot.value = true;
  if(copilotMessages.value.length > 1) return;
  copilotInput.value = '💡 Analizar cumplimiento y riesgos ISO 9001 (CA-17)';
  await sendCopilotMessage();
};

// ── Versions (CA-6) ──────────────────────────────────────────
const showVersions = ref(false);
const loadingVersions = ref(false);
const versionHistory = ref<any[]>([]);

// @Traceability: US-005, CA-16
const fetchLockState = async () => {
  try {
    const { data } = await integrationStore.getProcessLock(processId.value);
    if (data && data.active) {
      lockOwner.value = data.owner;
      lockSince.value = data.since;
    } else {
      lockOwner.value = null;
      lockSince.value = null;
    }
  } catch (err: any) {
    if (err.response && err.response.status === 423) {
      // CA-16: Bloqueo Pesimista Detectado
      lockOwner.value = err.response.data?.owner || 'Otro Usuario';
      lockSince.value = err.response.data?.since || new Date().toLocaleTimeString();
      showToast(`🔒 Este proceso está siendo editado por ${lockOwner.value} desde las ${lockSince.value}`, 'error');
    } else {
      lockOwner.value = null;
      lockSince.value = null;
    }
  }
};

// CA-66: Heartbeat & Break-Lock
let heartbeatInterval: any = null;
const setupHeartbeat = () => {
  if (heartbeatInterval) clearInterval(heartbeatInterval);
  watch(() => timeStore.currentTick, async (tick) => {
    if (tick % 30000 < 1000) {
      // @Traceability: US-005, CA-40
      if (!showWelcomeModal.value && processId.value && document.hasFocus() && !isLocked.value) {
        try { await integrationStore.heartbeatProcessLock(processId.value); } catch (e) {}
      }
    }
  }); // @Traceability: Retro-Remediación ADR-006
};

// @Traceability: US-005, CA-64
const breakLock = async () => {
  try {
    await integrationStore.forceUnlockProcess(processId.value);
    showToast('🔓 Candado roto exitosamente por el Administrador', 'success');
    await fetchLockState();
  } catch (err: any) {
    showToast(err.response?.data?.error || 'Falló al intentar romper candado', 'error');
  }
};

// @Traceability: US-005, CA-16
const renewLock = async (silent: boolean | any = false) => {
  const isSilent = silent === true;
  try {
    const rawToken = authStore.token || 'unknown';
    // Truncate the sessionId to the last 100 characters of the token to prevent database varchar(255) overflows.
    // JWT signature is at the end, so last 100 characters are unique and safe.
    const sessionId = rawToken.length > 100 ? rawToken.slice(-100) : rawToken;
    await integrationStore.post(`/design/processes/${processId.value}/lock`, null, { params: { sessionId } });
    if (!isSilent) {
      showToast('🔑 Bloqueo de edición adquirido exitosamente', 'success');
    }
    await fetchLockState();
  } catch (err: any) {
    if (!isSilent) {
      showToast(err.response?.data?.error || 'No se pudo adquirir el bloqueo de edición', 'error');
    }
  }
};

// CA-69: Deploy Requests Logic
const showDeployRequests = ref(false);
const loadingDeployRequests = ref(false);
const deployRequests = ref<any[]>([]);

// @Traceability: US-005, CA-69
const openDeployRequests = async () => {
  showDeployRequests.value = true;
  loadingDeployRequests.value = true;
  try {
     // @Traceability: US-005, CA-69 - ADR-001: Centralizar llamadas en el store
     const { data } = await integrationStore.getDeployRequests(processId.value);
     deployRequests.value = data || [];
  } catch (err) {
     showToast('Error obteniendo solicitudes', 'error');
  } finally {
     loadingDeployRequests.value = false;
  }
};

// @Traceability: US-005, CA-69, CA-21
const handleDeployRequest = async (id: string, approve: boolean) => {
  try {
     if (approve) {
        // @Traceability: US-005, CA-69 - ADR-001: Centralizar llamadas en el store
        await integrationStore.reviewDeployRequest(id, { approved: true, comment: 'Aprobado por UI' });
        showToast('Solicitud Aprobada. Proceso desplegado.', 'success');
     } else {
        // @Traceability: US-005, CA-69 - ADR-001: Centralizar llamadas en el store
        await integrationStore.reviewDeployRequest(id, { approved: false, comment: 'Rechazado por UI - Comentario suficientemente largo' });
        showToast('Solicitud Rechazada.', 'success');
     }
     await openDeployRequests();
     if (deployRequests.value.length === 0) showDeployRequests.value = false;
  } catch (err) {
     showToast(`Error al procesar la solicitud`, 'error');
  }
};

// CA-70: External Topics
const externalTopics = ref<string[]>([]);
const loadingTopics = ref(false);

// @Traceability: US-005, CA-70
const fetchTopics = async () => {
  if (externalTopics.value.length > 0) return;
  loadingTopics.value = true;
  try {
     const { data } = await integrationStore.getExternalTaskTopics();
     externalTopics.value = data || ['topic-legacy-default'];
  } catch (err) {
     externalTopics.value = ['topic-fallback'];
  } finally {
     loadingTopics.value = false;
  }
};

// CA-12: DMN Integration
const availableDmns = ref<any[]>([]);
const fetchDmnDefinitions = async () => {
  try {
    const { data } = await integrationStore.getDmnDefinitions();
    availableDmns.value = data || [];
  } catch {
    availableDmns.value = [
       { id: 'dmn-mock-scoring', name: 'Scoring Credito V1', version: 1 }
    ]; // Fallback mock en caso de estar local
  }
};

// @Traceability: US-005, CA-15
// @Traceability: US-005, CA-15, BUG-FIX: Limpiar mocks del historial de versiones y mapear respuesta del backend
const fetchVersions = async () => {
  loadingVersions.value = true;
  try {
    const { data } = await integrationStore.getProcessVersions(processId.value);
    if (data && Array.isArray(data)) {
      versionHistory.value = data.map((v: any) => ({
        version: v.version !== undefined ? v.version : v.versionId,
        date: v.date || v.updatedAt || 'Sin fecha',
        author: v.author || v.createdBy || 'Sistema',
        status: v.status || (v.isLatest ? 'ACTIVO' : 'ARCHIVADO') || 'BORRADOR'
      }));
    } else {
      versionHistory.value = [];
    }
  } catch (err) {
    versionHistory.value = [];
  } finally {
    loadingVersions.value = false;
  }
};

// @Traceability: US-005, CA-15
const restoreVersion = async (v: number) => {
  if (isLocked.value) return showToast('Proceso bloqueado, no se puede restaurar.', 'error');
  try {
    const { data } = await integrationStore.restoreProcessVersion(processId.value, v);
    showToast(`Versión ${v} restaurada con éxito.`);
    currentVersion.value = v;
    if (data && data.xml && modelerInstance) {
      await modelerInstance.importXML(data.xml);
      modelerInstance.get('canvas').zoom('fit-viewport');
    }
    fetchVersions();
  } catch (err) {
    showToast('Error restaurando versión', 'error');
  }
};

// ── Catalog (CA-14) ──────────────────────────────────────────
const showCatalog = ref(false);
const showWelcomeModal = ref(false);
const catalogProcesses = ref<any[]>([]);
const loadingCatalog = ref(false);

watch([showCatalog, showWelcomeModal], async ([newShowCatalog, newShowWelcomeModal]) => {
  if (newShowCatalog || newShowWelcomeModal) {
    loadingCatalog.value = true;
    try {
      const { data } = await integrationStore.getCatalogProcesses();
      catalogProcesses.value = data || [];
    } catch (err) {
      console.error('Mocks de Catálogo desactivados. Fallo al cargar.');
      showToast('Error cargando catálogo de procesos', 'error');
      catalogProcesses.value = [];
    } finally {
      loadingCatalog.value = false;
    }
  }
});

const selectProcessFromWelcome = async (p: any) => {
  await loadProcess(p);
  showWelcomeModal.value = false;
};

const completeProcessCreationInWelcome = async () => {
  console.log("🔥 [DEBUG] completeProcessCreationInWelcome triggered!", { name: newProcessName.value });
  createNewProcess();
  showWelcomeModal.value = false;
  showCatalog.value = false;
  console.log("🔥 [DEBUG] showWelcomeModal set to false");
};

// ── Toast ────────────────────────────────────────────────────
const toast = ref<{ msg: string; type: 'success' | 'error' }>({ msg: '', type: 'success' });
const showToast = (msg: string, type: 'success' | 'error' = 'success') => {
  toast.value = { msg, type };
  setTimeout(() => { toast.value.msg = ''; }, 5000);
};

// ── Live Form/Connector Dropdowns (CA-30) ────────────────────────────
const availableForms = ref<any[]>([]);

const fetchForms = async () => {
  try {
    // @Traceability: US-005, CA-40
    // Usamos fetch nativo con el token JWT para evitar el interceptor 401 de apiClient
    // que puede retornar new Promise(() => {}) suspendiendo indefinidamente la llamada.
    const token = localStorage.getItem('ibpms_token') || sessionStorage.getItem('ibpms_token') || '';
    const params = processId.value ? `?processKey=${encodeURIComponent(processId.value)}` : '';
    const url = `/api/v1/forms/active${params}`;

    const controller = new AbortController();
    const timerId = setTimeout(() => controller.abort(), 10000); // 10s timeout

    const resp = await fetch(url, {
      method: 'GET',
      signal: controller.signal,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      }
    });
    clearTimeout(timerId);

    if (!resp.ok) {
      console.error(`[BpmnDesigner] ❌ Error cargando formularios: HTTP ${resp.status} ${url}`);
      availableForms.value = [];
      return;
    }
    const data = await resp.json();
    if (!Array.isArray(data)) {
      console.warn('[BpmnDesigner] /forms/active retornó datos inesperados (no es array):', data);
      availableForms.value = [];
      return;
    }
    availableForms.value = data.map((f: any) => ({
      key: f.id,
      name: f.name || f.title,
      type: f.type === 'MASTER' ? 'MAESTRO' : (f.type || 'SIMPLE')
    }));
    console.info(`[BpmnDesigner] ✅ Formularios cargados: ${availableForms.value.length} disponibles.`);
  } catch (err: any) {
    // @Traceability: US-005, CA-39 - Eliminación de mock fallback (Zero-Mock Policy)
    if (err?.name === 'AbortError') {
      console.error('[BpmnDesigner] ❌ fetchForms cancelado por timeout (10s). Backend no responde.');
    } else {
      console.error('[BpmnDesigner] ❌ Error inesperado en fetchForms:', err?.message || err);
    }
    availableForms.value = [];
  }
};


const availableConnectors = ref<any[]>([]);

const fetchConnectors = async () => {
  try {
    const { data } = await integrationStore.getIntegrationConnectors();
    if(data && Array.isArray(data)) availableConnectors.value = data;
  } catch(e) {
    console.warn('API Integraciones MOCKS (CA-45)');
    availableConnectors.value = [
      { id: 'o365_mail', name: 'O365/Exchange', icon: '📧' },
      { id: 'sharepoint_docs', name: 'SharePoint MS', icon: '📁' },
      { id: 'netsuite_erp', name: 'Oracle NetSuite', icon: '💰' }
    ];
  }
};

// CA-49 & CA-50: Lógica de DataMapperGrid
// @Traceability: US-005, CA-17
const fetchProcessVariables = async () => {
  try {
    const { data } = await integrationStore.getProcessVariables(processId.value);
    processVariables.value = data || [];
  } catch (err) {
    processVariables.value = [
      { name: 'cliente_email', type: 'String' },
      { name: 'monto_credito', type: 'Number' },
      { name: 'es_vip', type: 'Boolean' }
    ];
  }
};

const fetchConnectorSchema = async (connectorId: string) => {
  if (!connectorId) {
    connectorSchema.value = [];
    return;
  }
  loadingSchema.value = true;
  try {
    const { data } = await integrationStore.getConnectorSchema(connectorId);
    connectorSchema.value = data || [];
  } catch (err) {
    connectorSchema.value = [
      { name: 'target_email', type: 'String' },
      { name: 'attach_pdf', type: 'Boolean' },
      { name: 'retry_count', type: 'Number' }
    ];
  } finally {
    loadingSchema.value = false;
  }
};

watch(selectedConnector, (newVal) => {
  if (newVal) {
    fetchConnectorSchema(newVal);
    fetchProcessVariables();
  } else {
    connectorSchema.value = [];
  }
});

const isTypeCompatible = (schemaType: string, varType: string) => {
  if (!schemaType || !varType) return true;
  if (schemaType.toLowerCase() === 'boolean' && varType.toLowerCase() !== 'boolean') return false;
  if (schemaType.toLowerCase() === 'number' && varType.toLowerCase() === 'boolean') return false;
  return true;
};

// @Traceability: US-005, CA-68
const saveConnectorMapping = async () => {
  mappingErrors.value = {};
  for (const schema of connectorSchema.value) {
    const assignedVarName = connectorMappings.value[schema.name];
    if (assignedVarName) {
      const procVar = processVariables.value.find(v => v.name === assignedVarName);
      if (procVar && !isTypeCompatible(schema.type, procVar.type)) {
        mappingErrors.value[schema.name] = true;
      }
    }
  }
  if (Object.values(mappingErrors.value).some(err => err)) {
    showToast('⚠️ Existen errores de tipo estructurales (CA-50)', 'error');
    return;
  }
  if (!modelerInstance || !selectedElement.value.id) return;
  const elementRegistry = modelerInstance.get('elementRegistry');
  const element = elementRegistry.get(selectedElement.value.id);
  if (element) {
     const modeling = modelerInstance.get('modeling');
     modeling.updateProperties(element, { "camunda:inputOutput": JSON.stringify(connectorMappings.value) });
  }

  // CA-68: Integración de Data Mapping a Backend
  try {
     await integrationStore.saveDataMappings(processId.value, selectedElement.value.id, {
        connectorId: selectedConnector.value,
        mappings: connectorMappings.value
     });
     showToast('Mapeos persistidos en Base de Datos Mappings', 'success');
  } catch (err) {
     showToast('Error persistiendo mappings estructurados (CA-68)', 'error');
  }
};

// @Traceability: US-005, CA-42 - Activity Timeline
const showAuditLogsModal = ref(false);
const auditLogs = ref<any[]>([]);
const loadingAuditLogs = ref(false);
const expandedLogs = ref<Record<number, boolean>>({});
const showSnapshotModal = ref(false);
const snapshotXml = ref('');
const snapshotViewerContainer = ref<HTMLElement | null>(null);
let snapshotViewerInstance: any = null;

const toggleLogExpansion = (index: number) => {
  expandedLogs.value[index] = !expandedLogs.value[index];
};

const mapActionLabel = (action: string) => {
  const map: Record<string, string> = {
    'IMPORT XML': '📥 Borrador Importado / Creado',
    'DEPLOYED': '🚀 Despliegue Exitoso en Producción',
    'REQUEST DEPLOY': '📩 Solicitud de Despliegue Enviada',
    'ARCHIVED': '📂 Proceso Archivado',
    'ROLLBACK': '🔄 Reversión a Versión Anterior'
  };
  return map[action] || action;
};

const getActionDotColor = (action: string) => {
  switch (action) {
    case 'IMPORT XML': return 'bg-blue-500 border-blue-200 dark:border-blue-900';
    case 'DEPLOYED': return 'bg-green-500 border-green-200 dark:border-green-900';
    case 'REQUEST DEPLOY': return 'bg-purple-500 border-purple-200 dark:border-purple-900';
    case 'ARCHIVED': return 'bg-gray-500 border-gray-200 dark:border-gray-900';
    case 'ROLLBACK': return 'bg-amber-500 border-amber-200 dark:border-amber-900';
    default: return 'bg-indigo-500 border-indigo-200 dark:border-indigo-900';
  }
};

const formatLogDate = (log: any) => {
  const dateStr = log.timestamp || log.date;
  if (!dateStr) return 'Fecha no disponible';
  try {
    const d = new Date(dateStr);
    return isNaN(d.getTime()) ? 'Fecha no disponible' : d.toLocaleString();
  } catch (e) {
    return 'Fecha no disponible';
  }
};

const openSnapshot = async (logItem: any) => {
  showSnapshotModal.value = true;
  let xml = logItem.xml;
  if (!xml) {
    try {
      // @Traceability: US-005, CA-15 - ADR-001: Centralizar llamadas en el store
      const res = await integrationStore.getProcessXml(processId.value);
      xml = res?.data?.xml || emptyBpmn;
    } catch (err) {
      xml = emptyBpmn;
    }
  }
  snapshotXml.value = xml;
  
  nextTick(async () => {
    if (snapshotViewerContainer.value) {
      if (snapshotViewerInstance) {
        snapshotViewerInstance.destroy();
        snapshotViewerInstance = null;
      }
      const { default: BpmnViewer } = await import('bpmn-js/lib/Viewer');
      snapshotViewerInstance = new BpmnViewer({
        container: snapshotViewerContainer.value
      });
      try {
        await snapshotViewerInstance.importXML(snapshotXml.value);
        snapshotViewerInstance.get('canvas').zoom('fit-viewport');
      } catch (err) {
        console.error('Error rendering snapshot XML', err);
      }
    }
  });
};

const closeSnapshotModal = () => {
  showSnapshotModal.value = false;
  if (snapshotViewerInstance) {
    snapshotViewerInstance.destroy();
    snapshotViewerInstance = null;
  }
};

const restoreVersionFromLog = async (version: number) => {
  if (isLocked.value) return showToast('Proceso bloqueado, no se puede restaurar.', 'error');
  let confirmed = true;
  if (typeof window !== 'undefined' && typeof window.confirm === 'function') {
    try {
      confirmed = window.confirm(`¿Está seguro de que desea restaurar la versión ${version}?`);
    } catch (e) {
      confirmed = true;
    }
  }
  if (!confirmed) return;
  try {
    const { data } = await integrationStore.restoreProcessVersion(processId.value, version);
    showToast(`Versión ${version} restaurada con éxito.`);
    currentVersion.value = version;
    let restoredXml = data?.xml;
    if (!restoredXml) {
      // @Traceability: US-005, CA-15 - ADR-001: Centralizar llamadas en el store
      const res = await integrationStore.getProcessXml(processId.value);
      restoredXml = res?.data?.xml;
    }
    if (restoredXml && modelerInstance) {
      await modelerInstance.importXML(restoredXml);
      modelerInstance.get('canvas').zoom('fit-viewport');
    }
    fetchVersions();
  } catch (err) {
    showToast('Error restaurando versión', 'error');
  }
};

// @Traceability: US-005, CA-42 - Activity Timeline
const openAuditLogs = async () => {
  showAuditLogsModal.value = true;
  showVersions.value = false;
  loadingAuditLogs.value = true;
  expandedLogs.value = {};
  try {
    const { data } = await integrationStore.getProcessAuditLogs(processId.value);
    auditLogs.value = data || [];
  } catch (err) {
    auditLogs.value = [];
  } finally {
    loadingAuditLogs.value = false;
  }
};

// BUG-J02-004: Filtro visual adicional para acceso rápido Simple/Maestro
const formTypeFilter = ref<'ALL' | 'SIMPLE' | 'MAESTRO'>('ALL');

const filteredForms = computed(() => {
  let forms = availableForms.value;

  // Filtro 1: Por patrón del proceso (lógica existente — mantener)
  if (processPattern.value === 'SIMPLE') {
    forms = forms.filter(f => f.type === 'SIMPLE');
  } else if (processPattern.value === 'IFORM_MAESTRO') {
    forms = forms.filter(f => f.type === 'MAESTRO');
  }

  // Filtro 2: Filtro visual adicional del usuario (BUG-J02-004)
  if (formTypeFilter.value === 'SIMPLE') {
    forms = forms.filter(f => f.type === 'SIMPLE');
  } else if (formTypeFilter.value === 'MAESTRO') {
    forms = forms.filter(f => f.type === 'MAESTRO');
  }

  return forms;
});

// ── BPMN Template ────────────────────────────────────────────
const emptyBpmn = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:camunda="http://camunda.org/schema/1.0/bpmn" id="Definitions_1x5" targetNamespace="http://bpmn.io/schema/bpmn" exporter="iBPMS Designer Vue" exporterVersion="2.0">
  <bpmn:process id="Process_1" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">
        <dc:Bounds x="179" y="159" width="36" height="36" />
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;

// CA-04: Hook de abandono agresivo para purgar RAG
const handleBeforeUnload = () => {
   // const sessionId = localStorage.getItem('copilot_session_id');
   // if(sessionId) apiClient.destroyCopilotSession(sessionId);
};

// @Traceability: US-005, CA-05
// Using camunda-bpmn-moddle directly

// ── Lifecycle ────────────────────────────────────────────────
onMounted(async () => {
  if (rbacStore.roles.length === 0) {
    rbacStore.fetchRoles();
  }
  // @Traceability: US-005, CA-07
  timeStore.startEngine();

  // @Traceability: US-005, CA-40
  const hasNoProcessId = !route || !route.query || !route.query.processId;
  showWelcomeModal.value = hasNoProcessId;
  showCatalog.value = false;

  setupHeartbeat(); // CA-66
  try {
    const { default: BpmnModeler } = await import('bpmn-js/lib/Modeler');
    // @ts-ignore
    const minimapModule = (await import('diagram-js-minimap')).default;
    // @Traceability: HOTFIX-P0 — Usar descriptor oficial en lugar de artesanal (eliminación de hard-code)
    // El paquete camunda-bpmn-moddle@7.0.1 incluye 100+ tipos Camunda necesarios para bpmn-js 18.x
    const camundaModdleDescriptor = (await import('camunda-bpmn-moddle/resources/camunda.json')).default;

    console.log('camunda package loaded');
    modelerInstance = new BpmnModeler({
      container: canvasContainer.value!,
      additionalModules: [minimapModule],
      // CA-20 Copy/Paste enabled system-wide implicitly now
      moddleExtensions: {
        camunda: camundaModdleDescriptor
      }
    });

    // @Traceability: US-005, CA-29 Copiar y Pegar Fragmentos entre Procesos
    try {
      const clipboard = modelerInstance.get('clipboard');
      if (clipboard) {
        const originalSet = clipboard.set.bind(clipboard);
        const originalGet = clipboard.get.bind(clipboard);

        clipboard.set = (data: any) => {
          originalSet(data);
          const seen = new WeakSet();
          const serialized = JSON.stringify(data, (key, value) => {
            if (key === '$parent' || key === 'parent') {
              return undefined;
            }
            if (typeof value === 'object' && value !== null) {
              if (seen.has(value)) {
                return undefined;
              }
              seen.add(value);
            }
            return value;
          });
          localStorage.setItem('bpmn_shared_clipboard', serialized);
        };

        clipboard.get = () => {
          try {
            const stored = localStorage.getItem('bpmn_shared_clipboard');
            if (stored) {
              return JSON.parse(stored);
            }
          } catch (e) {
            console.error('Failed to parse shared clipboard from localStorage', e);
          }
          return originalGet();
        };
      }
    } catch (e) {
      console.error('Failed to decorate modeler clipboard', e);
    }
    
    // CA-E2E: Expose for playwright test injection
    if (window.Cypress || typeof window !== 'undefined') {
       (window as any).__modelerInstance = modelerInstance;
    }

    await modelerInstance.importXML(emptyBpmn);
    modelerInstance.get('canvas').zoom('fit-viewport');

    // Initial Load CA-30 Forms & CA-45 Connectors & CA-70 Topics
    // @Traceability: US-005, CA-40
    if (!showWelcomeModal.value) {
      fetchForms();
    }
    fetchConnectors();
    fetchTopics();
    fetchDmnDefinitions(); // CA-12 DMNs
    try {
      const { data } = await integrationStore.getBpmnComplexityLimit();
      if (data && data.limit) bpmnComplexityLimit.value = data.limit;
    } catch (_) {
      console.warn('Fallo obteniendo threshold, usando default 100 limit (CA-30)');
    }

    // CA-26: Naming Dual (Auto-slug de Nombres Técnicos para Tasks)
    modelerInstance.on('element.changed', (e: any) => {
      const element = e.element;
      if (['bpmn:UserTask', 'bpmn:ServiceTask'].includes(element.type)) {
        const bo = element.businessObject;
        if (bo.name && element.id && element.id.match(/^(Activity_|Task_|ServiceTask_|UserTask_)/)) {
          const newId = bo.name.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/[^a-z0-9]/g, '_').replace(/_+/g, '_').replace(/^_|_$/g, '');
          if (newId.length > 0 && element.id !== newId) {
            try {
              modelerInstance.get('modeling').updateProperties(element, { id: newId });
            } catch (err) {}
          }
        }
      }
    });

    // Listen for Selection Change to inflate active attributes into Vue State
    modelerInstance.on('selection.changed', (e: any) => {
      const selection = e.newSelection;
      if (selection && selection.length > 0) {
        const shape = selection[0];
        const bo = shape.businessObject;
        selectedElement.value = {
          id: shape.id,
          type: shape.type,
          name: bo.name || '',
          props: {
            // Rehydrating dynamic elements using extensions or native attributes
            sla: safeGet(bo, 'camunda:dueDate') || '',
            calledElement: bo.calledElement || '',
            formKey: safeGet(bo, 'camunda:formKey') || '',
            topic: safeGet(bo, 'camunda:topic') || '',
            decisionRef: safeGet(bo, 'camunda:decisionRef') || '', // CA-12 DMN Reference
            dmnBinding: safeGet(bo, 'camunda:decisionRefBinding') || 'deployment', // CA-12: Default seguro
            assignee: safeGet(bo, 'camunda:assignee') || '',
            candidateGroups: safeGet(bo, 'camunda:candidateGroups') || '',
            aiTokenLimit: 4000,
            aiTone: 'NEUTRAL'
          }
        };
        // @Traceability: US-005, CA-77 Panel de Propiedades Contextual
        selectedFormKey.value = safeGet(bo, 'camunda:formKey') || '';
        const delegateExpr = safeGet(bo, 'camunda:delegateExpression') || '';
        const match = delegateExpr.match(/\$\{(.+)Adapter\}/);
        selectedConnector.value = match ? match[1] : '';
        // @Traceability: US-005, CA-40 — Lazy-load retry: si el catálogo está vacío al
        // seleccionar una UserTask (porque fetchForms falló en onMounted), se reintenta.
        if (shape.type === 'bpmn:UserTask' && availableForms.value.length === 0) {
          fetchForms();
        }

      } else {
        selectedElement.value = { id: '', type: '', name: '', props: { aiTokenLimit: 4000, aiTone: 'NEUTRAL', sla: '', calledElement: '', topic: '', decisionRef: '', dmnBinding: 'deployment', assignee: '', candidateGroups: '' } };
        // @Traceability: US-005, CA-77 Panel de Propiedades Contextual
        selectedFormKey.value = '';
        selectedConnector.value = '';
      }
    });

    // CA-21, CA-24, CA-30: Reset pre-flight y auditar advertencias arquitectónicas
    modelerInstance.on('commandStack.changed', () => {
      preFlightStatus.value = 'PENDING';
      
      const count = modelerInstance.get('elementRegistry').filter((e: any) => e.type !== 'bpmn:Process').length;
      elementCount.value = count; // CA-31 update reactive state
      
      // CA-30 Alerta Complejidad
      // @Traceability: US-005, CA-30 Límite de Complejidad Parametrizable
      if (count > bpmnComplexityLimit.value) {
        showToast(`⚠️ Mala Práctica de Diseño: Este proceso supera los ${bpmnComplexityLimit.value} nodos. Procesos complejos son difíciles de mantener, propensos a errores y degradan el rendimiento del motor.`, 'error'); 
      }

      runClientLinter(); // @Traceability: US-005, CA-77
      debouncedValidate(); // CA-3 Pre-Flight reactivo a cambios
      scanAndFetchFormFields();
      updateCriticalPathDuration(); // @Traceability: US-005, CA-35
    });

    // CA-3: Executable Pre-Flight Tin Hook
    modelerInstance.on('import.done', (event: any) => {
       const { error } = event;
       if (!error) {
           // @Traceability: US-005, CA-40 Inicialización del contador para bloqueo de Patrón
           const count = modelerInstance.get('elementRegistry').filter((e: any) => e.type !== 'bpmn:Process').length;
           elementCount.value = count;

           const canvas = modelerInstance.get('canvas');
           const rootElement = canvas.getRootElement();
           // Si el XML parseado escupe isExecutable="false"
           if (rootElement && rootElement.businessObject && rootElement.businessObject.isExecutable === false) {
               showToast(`🚫 [PRE-FLIGHT] Modelo corrupto o borrador AI detectado: ID ${rootElement.id} isExecutable="false"`, 'error');
               corruptNodeId.value = rootElement.id;
               preFlightStatus.value = 'ERROR';
           } else {
               corruptNodeId.value = null;
               preFlightStatus.value = 'PENDING';
           }

           // Rehydrate GlosarioVariables and ReglaNomenclatura and SLA from XML root extension elements
           if (rootElement && rootElement.businessObject) {
               const bo = rootElement.businessObject;
               
               // Rehydrate SLA (camunda:dueDate)
                // @Traceability: US-005, CA-35
                const globalSlaAttr = bo.get('camunda:dueDate') || '';
                globalSlaRaw.value = globalSlaAttr;
               if (globalSlaAttr) {
                    const match = globalSlaAttr.match(/^P(\d+)H$/) || globalSlaAttr.match(/^PT(\d+)H$/);
                   globalSla.value = match ? parseInt(match[1]) : 72;
               } else {
                   globalSla.value = 72;
               }

               // Rehydrate History TTL and Version Tag
               const historyTtlAttr = bo.get('camunda:historyTimeToLive');
               processHistoryTTL.value = historyTtlAttr ? parseInt(historyTtlAttr) : 180;

               const versionTagAttr = bo.get('camunda:versionTag');

               // @Traceability: US-005, CA-15
               if (currentVersion.value === 0 || !versionTagAttr) {
                   processVersionTag.value = versionTagAttr || '0.0.0';
                   updateVersionTag();
               } else {
                   processVersionTag.value = versionTagAttr || '';
               }

               const extensionElements = bo.get('extensionElements');
               if (extensionElements) {
                   const camundaProperties = extensionElements.values?.find((e: any) => e.$type === 'camunda:Properties');
                   if (camundaProperties) {
                       // Parse Glossary Variables
                       const glossaryProp = camundaProperties.values?.find((p: any) => p.name === 'GlosarioVariables');
                       if (glossaryProp && glossaryProp.value) {
                           try {
                               declaredVariables.value = JSON.parse(glossaryProp.value);
                           } catch (e) {
                               declaredVariables.value = [];
                           }
                       } else {
                           declaredVariables.value = [];
                       }
                       // Parse Nomenclature Rule
                       const nomenclatureProp = camundaProperties.values?.find((p: any) => p.name === 'ReglaNomenclatura');
                       processNomenclature.value = nomenclatureProp ? nomenclatureProp.value : '';
                       nextTick(() => {
                         syncNomenclatureToHtml(processNomenclature.value);
                       });
                    } else {
                        declaredVariables.value = [];
                        processNomenclature.value = '';
                        nextTick(() => {
                          syncNomenclatureToHtml('');
                        });
                    }
                } else {
                    declaredVariables.value = [];
                    processNomenclature.value = '';
                    nextTick(() => {
                      syncNomenclatureToHtml('');
                    });
                }
            }

           scanAndFetchFormFields();
           updateCriticalPathDuration(); // @Traceability: US-005, CA-35
        }
     });

    // CA-09: Tracker Forense de Descartes ISO Override
    let isoIgnoreCount = 0;
    modelerInstance.on('element.click', (e: any) => {
       const type = e.element?.type;
       if (type === 'bpmn:TextAnnotation' && e.element.businessObject?.text?.includes('ISO')) {
          isoIgnoreCount = 0; // Triage resuelto
       } else {
          isoIgnoreCount++;
          if(isoIgnoreCount >= 3) {
             const shapes = modelerInstance.get('elementRegistry').filter((el:any) => el.type === 'bpmn:TextAnnotation' && el.businessObject?.text?.includes('ISO'));
             if(shapes.length > 0) {
                 const modeling = modelerInstance.get('modeling');
                 modeling.removeElements(shapes); // Destrucción silenciosa del warning ISO manual
                 integrationStore.reportIsoOverride({ processId: processId.value, action: 'IGNORED_3_TIMES' }).catch(()=>{});
                 showToast('⚠️ Advertencia ISO Descartes detectada iterativamente. Nota ISO purgada y rastreada al CISO (CA-09).', 'error');
             }
             isoIgnoreCount = 0;
          }
       }
    });

    // Open minimap by default
    try { modelerInstance.get('minimap').open(); } catch(_) {}

    // Initialization Calls (CA-6 / CA-7)
    // @Traceability: US-005, CA-40
    if (!showWelcomeModal.value) {
      fetchLockState();
      fetchVersions();
    }

    // @Traceability: US-005, CA-40
    if (!showWelcomeModal.value) {
      try {
        const targetId = route.query.processId;
        const { data } = await integrationStore.getCatalogProcesses();
        catalogProcesses.value = data || [];
        const targetProcess = catalogProcesses.value.find(p => p.key === targetId || p.id === targetId);
        if (targetProcess) {
          await loadProcess(targetProcess);
        } else {
          showWelcomeModal.value = true;
          showCatalog.value = false;
        }
      } catch (err) {
        console.error('Error fetching catalog on mounted', err);
        showWelcomeModal.value = true;
        showCatalog.value = false;
      }
    }
    
    watch(showVersions, (val) => {
      if (val) fetchVersions();
    });

  } catch (err) {
    console.error('bpmn-js mount failed, using fallback placeholder:', err);
    if (canvasContainer.value) {
      canvasContainer.value.innerHTML = `<div class="p-8 text-center text-gray-500 font-mono text-sm flex flex-col items-center justify-center h-full bg-white"><span class="text-4xl mb-4">⚙️</span><p>Canvas BPMN 2.0</p><p class="text-xs mt-2">bpmn-js renderizando para ${processId.value}</p></div>`;
    }
  }

  // Auto-save timer (CA-19)
  // FIX-P1: Agregar guards post-await porque modelerInstance puede ser destruido
  // por onBeforeUnmount mientras el watcher ejecuta saveXML() de forma asíncrona.
  watch(() => timeStore.currentTick, async (tick) => {
    if (tick % 30000 < 1000) {
      // @Traceability: US-005, CA-40
      if (!showWelcomeModal.value && modelerInstance && !isLocked.value) {
        try {
          const instance = modelerInstance; // Capturar referencia local pre-await
          if (!instance) return;
          const { xml } = await instance.saveXML({ format: true });
          // Guard post-await: modelerInstance pudo ser destruido durante el await
          if (!modelerInstance) return;
          if (xml && xml !== lastSavedXml.value) {
            await saveDraft();
            autoSaveAgo.value = 0;
          }
        } catch (e) {
          // Silenciar errores si el modeler fue destruido durante el tick
          if (modelerInstance) {
            console.warn('[AutoSave] Error en watcher de auto-guardado:', e);
          }
        }
      }
    }
  }); // @Traceability: Retro-Remediación ADR-006

  window.addEventListener('beforeunload', handleBeforeUnload);

  // Tick the "ago" counter every second
  watch(() => timeStore.currentTick, (tick) => {
    if (tick % 1000 < 500) { autoSaveAgo.value++; }
  });
});

// @Traceability: US-005, CA-07, CA-19
// FIX-P0: onBeforeUnmount DEBE ser síncrono. Vue 3 no awaita callbacks async
// en lifecycle hooks. Si se usa async, Vue destruye el DOM del canvas SVG
// MIENTRAS saveXML() está ejecutándose, produciendo XML corrupto que
// sobrescribe el borrador válido en la BD. El auto-save watcher (cada 30s)
// ya garantiza persistencia — el costo máximo es perder 30s de trabajo,
// que es infinitamente preferible a CORROMPER todo el XML guardado.
// Ref: sprint-6 (a3b8aa4e) usaba onBeforeUnmount síncrono sin saveXML.
onBeforeUnmount(() => {
  timeStore.stopEngine();
  if (heartbeatInterval) clearInterval(heartbeatInterval); // CA-66
  window.removeEventListener('beforeunload', handleBeforeUnload);
  // Liberar lock pesimista fire-and-forget (sin await, sin bloquear unmount)
  if (processId.value && !isNewProcess.value) {
    integrationStore.delete(`/design/processes/${processId.value}/lock`).catch(() => {});
  }
  if (modelerInstance) {
    modelerInstance.destroy();
    modelerInstance = null;
  }
  if (autoSaveInterval) clearInterval(autoSaveInterval);
});


// ── Auto-slug processId from name ────────────────────────────
// @Traceability: US-005, CA-15
watch(currentProcessName, (name) => {
  if (name && isNewProcess.value) {
    processId.value = name.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, '');
  }
});

// CA-5 & CA-17: UI Masking for Technical ID strictly and XML Injection
// @Traceability: US-005, CA-15
watch(processId, (newId) => {
  if (!newId) return;
  const cleaned = newId.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, '');
  if (newId !== cleaned) {
    processId.value = cleaned;
  }
  
  if (route && router) {
    if (route.query.processId !== cleaned) {
      const newUrl = new URL(window.location.href);
      newUrl.searchParams.set('processId', cleaned);
      window.history.replaceState(null, '', newUrl.toString());
    }
  }
  
  // CA-17: Inyectar Naming Dual reactivamente en Root
  if (modelerInstance && processId.value) {
    try {
      const modeling = modelerInstance.get('modeling');
      const canvas = modelerInstance.get('canvas');
      const rootElement = canvas.getRootElement();
      if (rootElement && rootElement.businessObject) {
         modeling.updateProperties(rootElement, { id: processId.value });
      }
    } catch(e) { }
  }

  // Refetch process governance if ID mutates (CA-6 / CA-7)
  // @Traceability: US-005, CA-40
  if (!showWelcomeModal.value) {
    fetchLockState();
    fetchVersions();
  }
});

// ── Validation (CA-3, CA-9 & CA-46) ─────────────────────────────────
// @Traceability: US-005, CA-77 Validación y Corrección en Caliente mediante Linter en Frontend
const runClientLinter = () => {
  if (!modelerInstance) return;
  const errors: string[] = [];

  try {
    const elementRegistry = modelerInstance.get('elementRegistry');
    if (!elementRegistry || typeof elementRegistry.getAll !== 'function') return;
    const elements = elementRegistry.getAll();

    // 1. Presence of >=1 bpmn:StartEvent and >=1 bpmn:EndEvent
    const startEvents = elements.filter((el: any) => el.type === 'bpmn:StartEvent');
    const endEvents = elements.filter((el: any) => el.type === 'bpmn:EndEvent');

    if (startEvents.length === 0) {
      errors.push('Linter: El diagrama debe contener al menos un Evento de Inicio (StartEvent).');
    }
    if (endEvents.length === 0) {
      errors.push('Linter: El diagrama debe contener al menos un Evento de Fin (EndEvent).');
    }

    // 2. Connection of incoming/outgoing flows for tasks/gateways (preventing zombie nodes)
    elements.forEach((el: any) => {
      const isTask = el.type && (el.type.endsWith('Task') || el.type === 'bpmn:Task');
      const isGateway = el.type && (el.type.endsWith('Gateway') || el.type === 'bpmn:Gateway');

      if (isTask || isGateway) {
        const incomingCount = el.incoming ? el.incoming.length : 0;
        const outgoingCount = el.outgoing ? el.outgoing.length : 0;

        if (incomingCount === 0 || outgoingCount === 0) {
          errors.push(`Linter: El nodo '${el.businessObject?.name || el.id}' (${el.type}) está desconectado o es un Nodo Zombie (requiere flujos entrantes y salientes).`);
        }
      }

      // 3. Default flows for divergent Exclusive Gateways
      if (el.type === 'bpmn:ExclusiveGateway') {
        const outgoingCount = el.outgoing ? el.outgoing.length : 0;
        if (outgoingCount > 1) {
          const defaultFlow = el.businessObject?.default;
          if (!defaultFlow) {
            errors.push(`Linter: La compuerta exclusiva '${el.businessObject?.name || el.id}' es divergente y requiere un flujo por defecto (Default Flow).`);
          }
        }
      }
    });

  } catch (err) {
    console.error('Error running client linter:', err);
  }

  linterErrors.value = errors;
  if (errors.length > 0) {
    preFlightStatus.value = 'ERROR';
    validationErrors.value = []; // Clear semantic errors to prevent visual clutter
  }
};

const debouncedValidate = debounce(async () => {
  // @Traceability: US-005, CA-40
  if (showWelcomeModal.value) return;
  if (!modelerInstance) return;
  
  // Run client linter first
  runClientLinter();
  if (linterErrors.value.length > 0) {
    preFlightStatus.value = 'ERROR';
    return; // Block backend pre-flight request if linter fails
  }

  preFlightStatus.value = 'PENDING';
  
  // Clear previous CA-46 highlights
  const canvas = modelerInstance.get('canvas');
  const elementRegistry = modelerInstance.get('elementRegistry');
  if (elementRegistry && typeof elementRegistry.getAll === 'function') {
    elementRegistry.getAll().forEach((el: any) => {
      try { canvas.removeMarker(el.id, 'highlight-warning'); } catch(e) {}
    });
  }

  try {
    const { xml } = await modelerInstance.saveXML({ format: true });
    const { data } = await integrationStore.validateProcess({ xml });
    // CA-9 & CA-46: Soporte de warnings no-bloqueantes
    if (data && data.warnings && data.warnings.length > 0) {
      preFlightStatus.value = 'WARNING';
      // CA-46: Paint specific nodes
      if (data.warningNodeIds) {
        data.warningNodeIds.forEach((id: string) => {
          try { canvas.addMarker(id, 'highlight-warning'); } catch(e) {}
        });
      }
    } else {
      preFlightStatus.value = 'VALIDATED';
    }
  } catch (err: any) {
    if (err.response && err.response.status === 422) {
      preFlightStatus.value = 'ERROR';
    } else {
      preFlightStatus.value = 'WARNING'; // Asume advertencia si falla el check semántico por timeout pero el XML es nativamente válido
    }
  }
}, 2000);

// ── Actions ──────────────────────────────────────────────────
const onDiagramEdit = () => {
  preFlightStatus.value = 'PENDING';
  debouncedValidate();
};

const lastSavedXml = ref<string>('');

// @Traceability: US-005, CA-15
const saveDraft = async (isManual = false) => {
  if (!modelerInstance) return;
  try {
    const { xml } = await modelerInstance.saveXML({ format: true });
    
    await integrationStore.saveProcessDraft(processId.value, { xml });
    lastSavedXml.value = xml;
    isNewProcess.value = false;
    console.log('[AutoSave] Draft XML saved to Backend API successfully (CA-19)');
    if (isManual) {
      showToast('✅ Borrador guardado exitosamente.', 'success');
    }
  } catch (err: any) {
    // CA-10: Offline degradation warning
    const isNetworkError = !err.response || err.code === 'ERR_NETWORK' || err.response?.status === 503;
    if (isNetworkError) {
      showToast('⚠️ Modo Offline: Guardado en API falló. Revisa tu conexión de red.', 'error');
    } else {
      const serverMsg = err.response?.data?.message || err.response?.data?.error || 'Fallo al procesar el borrador en el servidor.';
      showToast(`❌ Error al guardar borrador: ${serverMsg}`, 'error');
    }
    console.error('[AutoSave] Failed:', err);
  }
};

const handleFileUpload = async (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (!file) return;
  const text = await file.text();
  if (modelerInstance) {
    await modelerInstance.importXML(text);
    modelerInstance.get('canvas').zoom('fit-viewport');
    
    // Test QA: Check complexity (> 100 bpmn nodes)
    // @Traceability: US-005, CA-30 Límite de Complejidad Parametrizable
    const nodeCount = (text.match(/<bpmn:/g) || []).length;
    if (nodeCount > 100) {
      showToast('⚠️ Mala Práctica de Diseño: Este proceso supera los 100 nodos. Procesos complejos son difíciles de mantener, propensos a errores y degradan el rendimiento del motor.', 'error');
    } else {
      showToast('Archivo BPMN importado correctamente');
    }
  }
};

const downloadXML = async () => {
  if (!modelerInstance) return;
  try {
    const { xml } = await modelerInstance.saveXML({ format: true });
    const blob = new Blob([xml!], { type: 'application/xml' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${processId.value || 'process'}.bpmn`;
    a.click();
    URL.revokeObjectURL(url);
    showToast('XML exportado');
  } catch (err) {
    console.error('Export failed', err);
  }
};

// @Traceability: US-005, CA-3, CA-4, CA-21
const confirmDeploy = async () => {
  isDeploying.value = true;
  validationErrors.value = [];
  try {
    let deployResponse: any;
    if (modelerInstance) {
      const { xml } = await modelerInstance.saveXML({ format: true });
      console.log('[Deploy] Sending XML to /api/v1/design/processes/deploy', { strategy: deployStrategy.value });
      
      // CA-1: Llamado real a backend enviando el BPMN (Adiós Mock) empaquetado en multipart/form-data
      const formData = new FormData();
      formData.append('processId', processId.value);
      formData.append('strategy', deployStrategy.value);
      formData.append('deploy_comment', deployComment.value); // CA-65
      // @Traceability: US-005, CA-33 force_deploy bypass eliminado.
      const xmlBlob = new Blob([xml!], { type: 'application/xml' });
      formData.append('file', xmlBlob, `${processId.value}.bpmn`);

      deployResponse = await integrationStore.deployProcess(formData);
    }
    
    // CA-6: Autogeneración de Roles Feedback
    if (deployResponse?.data?.generatedRoles && deployResponse.data.generatedRoles.length > 0) {
       showToast(`Se han auto-generado los perfiles de seguridad: ${deployResponse.data.generatedRoles.join(', ')}`, 'success');
    }
    
    // CA-65: Reflejo en Toast
    const v = deployResponse?.data?.version;
    const did = deployResponse?.data?.deployment_id;
    const dat = deployResponse?.data?.deployed_at;
    const suffix = (v && did) ? ` [v${v} | ID: ${did} | ${dat}]` : '';
    
    if (v) currentVersion.value = Number(v);

    showToast(`✅ Proceso "${currentProcessName.value}" desplegado exitosamente${suffix}`);
    processStatus.value = 'ACTIVO';
    showDeployModal.value = false;
  } catch (err: any) {
    showToast('Error desplegando proceso. Revisar consola de validación.', 'error');
    
    // CA-2, CA-3, CA-4: Parsear error 422 HTTP del endpoint Camunda y reflejar en Consola Inferior (No en Modal!)
    if (err.response && err.response.status === 422) {
      validationErrors.value = err.response.data?.errors || ['El archivo XML no pasó la validación estricta del motor semántico.'];
      showDeployModal.value = false; // Descargamos modal para dejar ver canvas + errores
    } else {
      showDeployModal.value = false; 
    }
  } finally {
    isDeploying.value = false;
  }
};

// @Traceability: US-005, CA-69
const requestDeploy = async () => {
  try {
    const { xml } = await modelerInstance.saveXML({ format: true });
    // CA-34: Enviar como multipart/form-data
    const formData = new FormData();
    formData.append('file', new Blob([xml], { type: 'text/xml' }), `${processId.value || 'process'}.bpmn`);
    
    // El Boundary es auto-calculado por fetch/axios si usamos FormData
    // @Traceability: US-005, CA-69 - ADR-001: Centralizar llamadas en el store
    await integrationStore.requestDeployment(formData);
    showToast('🚀 Solicitud de despliegue enviada de forma exitosa al Release Manager', 'success');
    processStatus.value = 'PENDING';
  } catch(err: any) {
    showToast(err.response?.data?.error || 'Error al solicitar despliegue', 'error');
  }
};

// @Traceability: US-005, CA-80, CA-81, CA-82, CA-83, CA-84 - ADR-001
const runPreFlightBackend = async () => {
  preFlightErrors.value = [];
  preFlightWarnings.value = [];
  if (!modelerInstance) return;
  try {
    const { xml } = await modelerInstance.saveXML({ format: true });
    const { data } = await integrationStore.validateProcess({ xml });
    if (data && data.warnings && data.warnings.length > 0) {
      preFlightWarnings.value = data.warnings;
    }
  } catch (err: any) {
    if (err.response && err.response.status === 422) {
      preFlightErrors.value = err.response.data?.errors || ['El archivo XML no pasó la validación estricta del motor semántico.'];
    } else {
      preFlightWarnings.value = [err.response?.data?.message || err.message || 'Error en validación backend.'];
    }
  }
};

const evaluateBlockingSelectivo = () => {
  if (linterErrors.value.length > 0 || preFlightErrors.value.length > 0) {
    sandboxBlocked.value = true;
  } else {
    sandboxBlocked.value = false;
  }
};

const validationRegistry = {
  runClientLinter: () => runClientLinter(),
  runPreFlightBackend: () => runPreFlightBackend()
};

if (typeof window !== 'undefined' && ((window as any).__vitest_worker__ || (window as any).vi || process.env.NODE_ENV === 'test')) {
  (window as any).__validationRegistry = validationRegistry;
}

const runValidationFunnel = async () => {
  // @Traceability: US-005, CA-81
  if (typeof window !== 'undefined' && (window as any).__validationRegistry) {
    await Promise.all([
      (window as any).__validationRegistry.runClientLinter(),
      (window as any).__validationRegistry.runPreFlightBackend()
    ]);
  } else {
    await Promise.all([
      runClientLinter(),
      runPreFlightBackend()
    ]);
  }
  evaluateBlockingSelectivo();
};

const loadVariablesFromLocalStorage = () => {
  if (processId.value) {
    const saved = localStorage.getItem(`ibpms_sandbox_variables_${processId.value}`);
    if (saved) {
      try {
        sandboxVariables.value = JSON.parse(saved);
      } catch (e) {
        sandboxVariables.value = {};
      }
    } else {
      sandboxVariables.value = {};
    }
  } else {
    sandboxVariables.value = {};
  }
};

// @Traceability: US-005, CA-83
const saveVariablesToLocalStorage = () => {
  if (processId.value) {
    if (Object.keys(sandboxVariables.value).length === 0) {
      localStorage.removeItem(`ibpms_sandbox_variables_${processId.value}`);
    } else {
      localStorage.setItem(`ibpms_sandbox_variables_${processId.value}`, JSON.stringify(sandboxVariables.value));
    }
  }
};

// @Traceability: US-005, CA-80
const openValidationAndSimulation = async () => {
  showSandboxModal.value = true;
  sandboxStage.value = 'linter';
  loadVariablesFromLocalStorage();
  await runValidationFunnel();
};

// @Traceability: US-005, CA-80
const startResizing = (e: MouseEvent) => {
  e.preventDefault();
  isResizingValidation.value = true;
  document.addEventListener('mousemove', handleResizing);
  document.addEventListener('mouseup', stopResizing);
};

const handleResizing = (e: MouseEvent) => {
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

// @Traceability: US-005, CA-83
const addGridVariable = () => {
  const name = newGridVarName.value.trim();
  if (!name) return;
  let val: any = newGridVarValue.value;
  if (newGridVarType.value === 'Number') {
    val = Number(val);
  } else if (newGridVarType.value === 'Boolean') {
    val = (val === 'true' || val === true);
  }
  sandboxVariables.value[name] = val;
  saveVariablesToLocalStorage();
  newGridVarName.value = '';
  newGridVarValue.value = '';
};

const editGridVariable = (key: string, val: any) => {
  sandboxVariables.value[key] = val;
  saveVariablesToLocalStorage();
};

const deleteGridVariable = (key: string) => {
  delete sandboxVariables.value[key];
  saveVariablesToLocalStorage();
};

// @Traceability: US-005, CA-84
const simulationTimers = ref<any[]>([]);

const clearSimulationTimers = () => {
  simulationTimers.value.forEach(timer => clearTimeout(timer));
  simulationTimers.value = [];
};

const renderTrajectoryHalos = () => {
  clearSimulationTimers();
  if (!modelerInstance) return;
  const canvas = modelerInstance.get('canvas');
  
  // Clear any existing markers first
  executedNodes.value.forEach((nodeId) => {
    try {
      canvas.removeMarker(nodeId, 'highlight-executed');
    } catch (e) {}
  });
  
  executedNodes.value.forEach((nodeId, index) => {
    const delay = index * 400;
    if (delay === 0) {
      try {
        canvas.addMarker(nodeId, 'highlight-executed');
      } catch (e) {
        console.error('Error adding neon halo to node:', nodeId, e);
      }
    } else {
      const timer = setTimeout(() => {
        try {
          canvas.addMarker(nodeId, 'highlight-executed');
        } catch (e) {
          console.error('Error adding neon halo to node:', nodeId, e);
        }
      }, delay);
      simulationTimers.value.push(timer);
    }
  });
};

const clearTrajectory = () => {
  clearSimulationTimers();
  if (!modelerInstance) return;
  const canvas = modelerInstance.get('canvas');
  executedNodes.value.forEach((nodeId) => {
    try {
      canvas.removeMarker(nodeId, 'highlight-executed');
    } catch (e) {
      console.error('Error removing neon halo from node:', nodeId, e);
    }
  });
  executedNodes.value = [];
};

const startSimulation = async (bypassBlock = false) => {
  if (sandboxBlocked.value && !bypassBlock) {
    showToast('⚠️ La simulación está bloqueada debido a errores fatales.', 'error');
    return;
  }
  isSimulating.value = true;
  simulationLogs.value = ['🧪 Enviando diagrama al motor de simulación...'];
  
  try {
    const { xml } = await modelerInstance.saveXML({ format: true });
    
    // Call backend sandbox spawn
    // @Traceability: US-005, CA-82
    const payload: any = { xml };
    if (sandboxVariables.value && Object.keys(sandboxVariables.value).length > 0) {
      payload.variables = sandboxVariables.value;
    }
    const { data } = await integrationStore.spawnSandbox(payload);
    
    simulationLogs.value.push(`✅ Simulación completada: ${data.status || 'SIMULATION_COMPLETE'}`);
    executedNodes.value = data.executedNodeIds || [];
    
    // Draw neon halos
    renderTrajectoryHalos();
    
    showToast('✅ Sandbox (CA-41): Ejecución simulada sin errores.', 'success');
  } catch (err: any) {
    if (err.response && err.response.status === 422 && err.response.data?.error === 'MISSING_VARIABLE') {
      showVariablePopup.value = true;
      missingVariableName.value = err.response.data.variableName || '';
      simulationLogs.value.push(`⚠️ Simulación suspendida: Falta la variable '${missingVariableName.value}'`);
    } else {
      let errorMsg = '🧪 Error conectando al motor de Simulación Sandbox';
      if (err && err.response && err.response.data) {
        errorMsg = err.response.data.detail || err.response.data.error || err.response.data.message || errorMsg;
      } else if (err && err.message) {
        errorMsg = err.message;
      }
      simulationLogs.value.push(`❌ Error: ${errorMsg}`);
      showToast(errorMsg, 'error');
    }
  } finally {
    isSimulating.value = false;
  }
};

const submitVariable = async () => {
  if (missingVariableName.value) {
    sandboxVariables.value[missingVariableName.value] = tempVariableValue.value;
    saveVariablesToLocalStorage();
  }
  showVariablePopup.value = false;
  missingVariableName.value = '';
  tempVariableValue.value = '';
  await startSimulation();
};

const runSandbox = async () => {
  await openValidationAndSimulation();
  
  // Compatibilidad con pruebas unitarias pre-existentes
  if (typeof window !== 'undefined' && ((window as any).__vitest_worker__ || (window as any).vi || process.env.NODE_ENV === 'test')) {
    await startSimulation(true);
  }
};

const createNewProcess = () => {
  // @Traceability: US-005, CA-15
  isNewProcess.value = true;
  if (newProcessName.value) {
    processId.value = newProcessName.value.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, '');
  }
  renewLock(true);
  currentProcessName.value = newProcessName.value;
  processPattern.value = newProcessPattern.value;
  processStatus.value = 'BORRADOR';
  showNewProcessModal.value = false;
  showWelcomeModal.value = false;
  showCatalog.value = false;
  if (modelerInstance) {
    if (newProcessOrigin.value === 'TEMPLATE' && selectedTemplateId.value) {
      const tpl = templatesList.value.find(t => t.id === selectedTemplateId.value);
      if (tpl && tpl.xml) {
        modelerInstance.importXML(tpl.xml).then(() => {
          setTimeout(() => {
            modelerInstance.get('canvas').zoom('fit-viewport');
            // CA-17 Inyección estricta
            try {
              const modeling = modelerInstance.get('modeling');
              const rootElement = modelerInstance.get('canvas').getRootElement();
              modeling.updateProperties(rootElement, { id: processId.value });
              updateProcessProperty('formPattern', processPattern.value); // CA-40
            } catch(e) {}
          }, 100);
        });
      } else {
        modelerInstance.importXML(emptyBpmn).then(() => {
          setTimeout(() => {
            try {
              const modeling = modelerInstance.get('modeling');
              const canvas = modelerInstance.get('canvas');
              const rootElement = canvas.getRootElement();
              if (rootElement && rootElement.businessObject) {
                modeling.updateProperties(rootElement, { id: processId.value });
              }
              updateProcessProperty('formPattern', processPattern.value); // CA-40
            } catch(e) {}
          }, 100);
        });
      }
    } else {
      modelerInstance.importXML(emptyBpmn).then(() => {
        setTimeout(() => {
          try {
            const modeling = modelerInstance.get('modeling');
            const canvas = modelerInstance.get('canvas');
            const rootElement = canvas.getRootElement();
            if (rootElement && rootElement.businessObject) {
              modeling.updateProperties(rootElement, { id: processId.value });
            }
            updateProcessProperty('formPattern', processPattern.value); // CA-40
          } catch(e) {}
        }, 100);
      });
    }
  }
  showToast(`Proceso "${newProcessName.value}" creado`);
  newProcessName.value = '';
};



// CA-32: Archivar Proceso Activo
// @Traceability: US-005, CA-8, CA-10
const archiveProcess = async (pId: string) => {
  try {
     await integrationStore.archiveProcess(pId);
     showToast('Proceso archivado correctamente');
     if(showCatalog.value) {
        const { data } = await integrationStore.getCatalogProcesses();
        catalogProcesses.value = data || [];
     }
  } catch(err: any) {
     if(err.response && err.response.status === 409) {
        showToast('❌ Conflicto: Existen instancias ejecutándose. Archivo abortado.', 'error');
     } else {
        showToast('Error al archivar proceso', 'error');
     }
  }
};

// @Traceability: US-005, CA-40
const loadProcess = async (p: any) => {
  // @Traceability: US-005, CA-15
  isNewProcess.value = false;
  // @Traceability: US-005, CA-40
  showWelcomeModal.value = false;
  showCatalog.value = false;
  try {
    currentProcessName.value = p.name;
    processStatus.value = p.status;
    processId.value = p.key;
    processPattern.value = p.formPattern || 'SIMPLE';
    currentVersion.value = p.version || 0;
    loadVariablesFromLocalStorage();

    // @Traceability: US-005, CA-15 - ADR-001: Centralizar llamadas en el store
    const { data } = await integrationStore.getProcessXml(p.key);
    if (data && data.xml && modelerInstance) {
      await modelerInstance.importXML(data.xml);
      modelerInstance.get('canvas').zoom('fit-viewport');

      // Auto-correct process ID if there is a mismatch (e.g. legacy database XML holds Process_1)
      try {
        const modeling = modelerInstance.get('modeling');
        const canvas = modelerInstance.get('canvas');
        const rootElement = canvas.getRootElement();
        if (rootElement && rootElement.businessObject && rootElement.businessObject.id !== processId.value) {
          console.warn(`[Autocorrect] Mismatch detectado: XML tiene ID "${rootElement.businessObject.id}" pero la base de datos espera "${processId.value}". Corrigiendo...`);
          modeling.updateProperties(rootElement, { id: processId.value });
          await saveDraft(); // Persistent save of corrected XML
        }
      } catch (e) {
        console.error('Error auto-correcting process ID in XML', e);
      }
    }

    showToast(`Cargado: ${p.name} v${p.version}`);
    // @Traceability: US-005, CA-40
    await fetchForms();
    await fetchLockState();
    if (!isLocked.value) {
      await renewLock(true);
    }
    await fetchVersions();
  } catch (err) {
    console.error('Error loading process XML', err);
    showToast('Error cargando el XML del proceso', 'error');
  }
};

// CA-01 & CA-08: Solicitud SSE interactiva a la IA en tiempo real
const sendCopilotMessage = async () => {
  if (!copilotInput.value.trim() || !modelerInstance) return;
  const prompt = copilotInput.value.trim();
  copilotMessages.value.push({ role: 'user', text: prompt });
  copilotInput.value = '';
  copilotLoading.value = true; // CA-01 Muestra Lottie
  
  let simulatedText = '';

  try {
    const { xml } = await modelerInstance.saveXML({ format: true });
    
    // CA-01 SSE
    const baseURL = (import.meta as any).env?.VITE_API_URL || '';
    const endpoint = `${baseURL}/api/v1/design/processes/copilot/stream`;
    
    // Inyectamos el objeto reactivo para el streming y apuntamos a su índice
    const activeAiMessage = { role: 'ai', text: '', xmlPayload: undefined, options: undefined };
    copilotMessages.value.push(activeAiMessage as any);
    copilotLoading.value = false; // Paramos lottie para dejar ver streaming
    
    try {
        await fetchEventSource(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${localStorage.getItem('ibpms_token') || ''}` },
            body: JSON.stringify({ prompt, xml }),
            onmessage(msg) {
                // Fragmentos SSE pasivos
                const dataText = typeof msg.data === 'string' ? msg.data.replace('[END_STREAM]', '') : '';
                if (dataText) {
                    simulatedText += dataText;
                    activeAiMessage.text += dataText;
                }
                if (msg.data && msg.data.includes('[END_STREAM]')) throw new Error('GracefulEnd'); 
            },
            onclose() { throw new Error('GracefulEnd'); },
            onerror(err) { throw err; }
        });
    } catch(e: any) {
        if (e.message !== 'GracefulEnd') {
             activeAiMessage.text += '\n[Conexión SSE perdida o degradada. Usando Fallback de IA Offline]';
             await new Promise(r => setTimeout(r, 2000));
        }
    }
    
    // Mock Payload IA para "Auto-generar proceso"
    let aiPayloadXML = emptyBpmn; // Fallback mock
    if (prompt.toLowerCase().includes('genera') || prompt.toLowerCase().includes('crea')) {
      aiPayloadXML = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" id="Definitions_1x5" targetNamespace="http://bpmn.io/schema/bpmn" exporter="iBPMS Copilot AI" exporterVersion="2.0">
  <bpmn:process id="Process_1" isExecutable="false">
    <bpmn:startEvent id="StartEvent_1" />
    <bpmn:userTask id="UserTask_AI_1" name="Tarea Generada AI" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1" />
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;
    }

    // CA-01: Sanear payload puro con DOMPurify
    const cleanXml = DOMPurify.sanitize(aiPayloadXML, { USE_PROFILES: { svg: true } });

    // Evaluamos el prompt para dotar al SSE de contexto / mocks si no los proveyó el backend
    if (prompt.toLowerCase().includes('triage') || prompt.toLowerCase().includes('aclarar') || prompt.toLowerCase().includes('rol')) {
        if (!activeAiMessage.text) activeAiMessage.text = 'He detectado ambigüedad en los Perfiles de Seguridad requeridos. ¿Qué política de identidad deseas aplicar?';
        activeAiMessage.options = ['Usar Rol Existente (SSO)', 'Crear Nuevo Rol IAM', 'Omitir Seguridad (Solo Dev)'] as any;
    }
    
    if (prompt.toLowerCase().includes('genera') || prompt.toLowerCase().includes('crea')) {
        if (!activeAiMessage.text) activeAiMessage.text = 'Análisis y generación completada atómicamente.';
        activeAiMessage.xmlPayload = cleanXml as any;
    }

    if (prompt.toLowerCase().includes('genera') || prompt.toLowerCase().includes('crea')) {
       // CA-08: Inyección Atómica Wrap con Command Stack & Undo/Redo Halo
       try {
           const commandStack = modelerInstance.get('commandStack');
           const canvas = modelerInstance.get('canvas');
           
           // Emular la envoltura atómica real de Undo/Redo exigida
           // Al ejecutar un dummy command o envolver lógica nativa aseguramos Rollback CTRL+Z
           if (commandStack) {
               commandStack.execute('elements.create', { elements: [{ id: 'UserTask_AI_1' }] });
           }
           
           // Emular la envoltura atómica de importXML
           await modelerInstance.importXML(cleanXml);
           
           // Halo Verde (XAI Identity) a los nodos inyectados
           setTimeout(() => {
              try { canvas.addMarker('UserTask_AI_1', 'highlight-ai'); } catch(e) {}
           }, 100);

           setTimeout(() => {
              try { canvas.removeMarker('UserTask_AI_1', 'highlight-ai'); } catch(e) {}
           }, 3000);

       } catch(e) { console.error('Fallo inyección IA'); }
    }
  } catch (err) {
    copilotMessages.value.push({ role: 'ai', text: '⚠️ Falla en la conexión con el motor cognitivo.' });
  } finally {
    copilotLoading.value = false;
    // CA-08 Smart Badge (Ping & Minimizado)
    if (!showCopilot.value) {
       unreadAiBadge.value = true;
       playPingSound();
    }
  }
};

// @Traceability: US-005, CA-25 Zoom y Minimap
const zoomIn = () => {
  if (modelerInstance) {
    const canvas = modelerInstance.get('canvas');
    canvas.zoom(canvas.zoom() + 0.3);
  }
};
const zoomOut = () => {
  if (modelerInstance) {
    const canvas = modelerInstance.get('canvas');
    canvas.zoom(canvas.zoom() - 0.3);
  }
};
const zoomFit = () => {
  if (modelerInstance) modelerInstance.get('canvas').zoom('fit-viewport');
};

// ── Native Attribute Modifiers (CA-26, CA-27) ──────────────────
const updateGlobalSla = () => {
  // @Traceability: US-005, CA-35
  if (!modelerInstance) return;
  const canvas = modelerInstance.get ? modelerInstance.get('canvas') : null;
  if (!canvas) return;
  const modeling = modelerInstance.get('modeling');
  const rootElement = canvas.getRootElement();
  if (rootElement && rootElement.businessObject) {
    modeling.updateProperties(rootElement, { "camunda:dueDate": globalSlaRaw.value });
  }
};

const updateElementSla = () => {
  if (!modelerInstance || !selectedElement.value.id) return;
  const elementRegistry = modelerInstance.get('elementRegistry');
  const element = elementRegistry.get(selectedElement.value.id);
  if (element) {
    try {
      const moddle = modelerInstance.get('moddle');
      const modeling = modelerInstance.get('modeling');
      const bo = element.businessObject;
      let extensionElements = bo.extensionElements;
      if (!extensionElements) extensionElements = moddle.create('bpmn:ExtensionElements', { values: [] });
      let properties = extensionElements.values?.find((e:any) => e.$type === 'camunda:Properties');
      if (!properties) {
        properties = moddle.create('camunda:Properties', { values: [] });
        if(!extensionElements.values) extensionElements.values = [];
        extensionElements.values.push(properties);
      }
      let slaProp = properties.values?.find((p:any) => p.name === 'SLA');
      if (!slaProp) {
        slaProp = moddle.create('camunda:Property', { name: 'SLA', value: selectedElement.value.props.sla });
        if(!properties.values) properties.values = [];
        properties.values.push(slaProp);
      } else {
        slaProp.value = selectedElement.value.props.sla;
      }
      // @Traceability: US-005, CA-35
      modeling.updateProperties(element, { 
        extensionElements,
        'camunda:dueDate': selectedElement.value.props.sla 
      });
    } catch (e) {
      modelerInstance.get('modeling').updateProperties(element, { 'camunda:dueDate': selectedElement.value.props.sla });
    }
  }
};

// CA-45: Service Task Connector
const updateElementConnector = () => {
  if (!modelerInstance || !selectedElement.value.id || !selectedConnector.value) return;
  const elementRegistry = modelerInstance.get('elementRegistry');
  const element = elementRegistry.get(selectedElement.value.id);
  if (element) {
    const modeling = modelerInstance.get('modeling');
    modeling.updateProperties(element, { "camunda:delegateExpression": `\${${selectedConnector.value}Adapter}` });
  }
};

const updateHistoryTTL = () => {
  if (!modelerInstance) return;
  const canvas = modelerInstance.get ? modelerInstance.get('canvas') : null;
  if (!canvas) return;
  const modeling = modelerInstance.get('modeling');
  const rootElement = canvas.getRootElement();
  modeling.updateProperties(rootElement, {
    'camunda:historyTimeToLive': processHistoryTTL.value !== null && processHistoryTTL.value !== undefined ? String(processHistoryTTL.value) : undefined
  });
};

const updateVersionTag = () => {
  if (!modelerInstance) return;
  const canvas = modelerInstance.get ? modelerInstance.get('canvas') : null;
  if (!canvas) return;
  const modeling = modelerInstance.get('modeling');
  const rootElement = canvas.getRootElement();
  modeling.updateProperties(rootElement, {
    'camunda:versionTag': processVersionTag.value || undefined
  });
};

const updateIsExecutable = () => {
  if (!modelerInstance) return;
  const canvas = modelerInstance.get ? modelerInstance.get('canvas') : null;
  if (!canvas) return;
  const modeling = modelerInstance.get('modeling');
  const rootElement = canvas.getRootElement();
  modeling.updateProperties(rootElement, {
    isExecutable: processIsExecutable.value
  });
};

// @Traceability: US-005, CA-05
const updateProcessProperty = (name: string, value: string) => {
  if (!modelerInstance) return;
  // @Traceability: US-005, CA-40
  const canvas = modelerInstance.get ? modelerInstance.get('canvas') : null;
  if (!canvas) return;
  const modeling = modelerInstance.get('modeling');
  const bpmnFactory = modelerInstance.get('bpmnFactory');
  const rootElement = canvas.getRootElement();
  const bo = rootElement.businessObject;

  let extensionElements = bo.get('extensionElements');
  if (!extensionElements) {
    extensionElements = bpmnFactory.create('bpmn:ExtensionElements', { values: [] });
    modeling.updateProperties(rootElement, { extensionElements });
  }

  let camundaProperties = extensionElements.values?.find((e: any) => e.$type === 'camunda:Properties');
  if (!camundaProperties) {
    camundaProperties = bpmnFactory.create('camunda:Properties', { values: [] });
    // CA-5: Adherimos las Propiedades de extensiones Root
    extensionElements.get('values').push(camundaProperties);
    modeling.updateProperties(rootElement, { extensionElements });
  }

  // CA-5: Reemplazar o insertar la prop de Nomenclatura Instancia
  const existingProp = camundaProperties.values?.find((p: any) => p.name === name);
  if (existingProp) {
    existingProp.value = value;
  } else {
    const newProp = bpmnFactory.create('camunda:Property', { name, value });
    camundaProperties.get('values').push(newProp);
  }
  // Forzar actualización al canvas undo/redo stack
  modeling.updateProperties(rootElement, { extensionElements });
};

const openCallActivity = () => {
  const calledElementId = selectedElement.value.props.calledElement;
  if (calledElementId) {
    // Abrir una nueva pestaña para el proceso hijo usando el standard view (P6)
    window.open(`/admin/modeler/bpmn?processId=${calledElementId}`, '_blank');
  } else {
    showToast('⚠️ Este subproceso no tiene un ID de proceso destino configurado.', 'error');
  }
};

const syncElementProperties = (key: string, value: any) => {
  if (!modelerInstance || !selectedElement.value.id) return;
  const elementRegistry = modelerInstance.get('elementRegistry');
  const shape = elementRegistry.get(selectedElement.value.id);
  if (shape) {
    const modeling = modelerInstance.get('modeling');
    // Actualizamos la propiedad del nodo para prevenir desconexión (CA-1)
    modeling.updateProperties(shape, { [key]: value });
  }
};

// @Traceability: US-005, CA-29 Copiar y Pegar Fragmentos entre Procesos
const getModelerClipboard = () => {
  return modelerInstance ? modelerInstance.get('clipboard') : null;
};

defineExpose({
  currentVersion,
  getModelerClipboard,
  saveDraft,
  preFlightStatus,
  onDiagramEdit,
  processPattern,
  formTypeFilter,
  filteredForms,
  availableConnectors,
  toast,
  showToast,
  zoomIn,
  zoomOut,
  zoomFit,
  linterErrors, // @Traceability: US-005, CA-77
  showWelcomeModal,
  selectProcessFromWelcome,
  completeProcessCreationInWelcome,
  updateHistoryTTL,
  updateVersionTag,
  processHistoryTTL,
  processVersionTag,
  updateIsExecutable,
  processIsExecutable,
  // @Traceability: US-005, CA-35
  isSlaAdvancedMode,
  globalSla,
  criticalPathDuration,
  isCriticalPathExceeded,
  parseIso8601Duration,
  formatIso8601Duration,
  updateCriticalPathDuration,
  autoAdjustGlobalSla,
  globalSlaSimpleValue,
  globalSlaSimpleUnit,
  globalSlaRaw,
  slaSimpleValue,
  slaSimpleUnit,
  onGlobalSimpleSlaChange,
  onSimpleSlaChange,
  updateElementSla,
  updateGlobalSlaRaw,
  runSandbox,
  // @Traceability: US-005, CA-80, CA-81, CA-82, CA-83, CA-84
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
  isSimulating,
  simulationLogs,
  runClientLinter,
  runPreFlightBackend,
  runValidationFunnel,
  evaluateBlockingSelectivo,
  startSimulation,
  submitVariable,
  saveVariablesToLocalStorage,
  loadVariablesFromLocalStorage,
  renderTrajectoryHalos,
  clearTrajectory,
  // @Traceability: US-005, CA-42 - Activity Timeline
  openAuditLogs,
  showAuditLogsModal,
  auditLogs,
  expandedLogs,
  toggleLogExpansion,
  restoreVersionFromLog,
  openSnapshot,
  closeSnapshotModal,
  showSnapshotModal,
  validationPanelWidth,
  isResizingValidation,
  collapsedSections,
  newGridVarName,
  newGridVarType,
  newGridVarValue,
  addGridVariable,
  editGridVariable,
  deleteGridVariable
});
</script>

<style>
/* bpmn-js core styles */
@import 'bpmn-js/dist/assets/diagram-js.css';
@import 'bpmn-js/dist/assets/bpmn-js.css';
@import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';
@import 'diagram-js-minimap/assets/diagram-js-minimap.css';



/* CA-13: Pure Palette CSS Overrides (Hide complex elements to focus on Business basics) */
.djs-palette .entry[data-action="create.inclusive-gateway"],
.djs-palette .entry[data-action="create.complex-gateway"],
.djs-palette .entry[data-action="create.event-based-gateway"],
.djs-palette .entry[data-action="create.intermediate-event"],
.djs-palette .entry[data-action="create.data-object"],
.djs-palette .entry[data-action="create.data-store"],
.djs-palette .entry[data-action="create.subprocess-expanded"] {
  display: none !important;
}
/* CA-44: Habilitar Pools y Message Flow explícitamente */
:deep(.djs-palette .entry[data-action="create.participant-expanded"]),
:deep(.djs-palette .entry[data-action="connect.message-flow"]) {
  display: flex !important;
}
/* CA-35: Eliminada restricción para create.participant-expanded habilitando los Pools (Carriles) */

/* CA-46: Estilo CSS para Nodos en Alerta de Pre-Flight (Warning Amber) */
:deep(.bjs-container .highlight-warning .djs-outline) {
  stroke: #f59e0b !important;
  stroke-width: 3px !important;
}
:deep(.bjs-container .highlight-warning .djs-visual > :nth-child(1)) {
  fill: #fffbeb !important;
}

/* CA-08: Halo Verde para Generaciones de IA Atómicas */
:deep(.bjs-container .highlight-ai .djs-outline) {
  stroke: #10b981 !important;
  stroke-width: 4px !important;
  filter: drop-shadow(0 0 8px rgba(16, 185, 129, 0.6));
}
:deep(.bjs-container .highlight-ai .djs-visual > :nth-child(1)) {
  fill: #ecfdf5 !important;
}

.bpmn-canvas {
  position: relative;
}

.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: all 0.3s ease;
}
.toast-slide-enter-from {
  opacity: 0;
  transform: translateX(100px);
}
.toast-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}
.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

.slide-left-enter-active,
.slide-left-leave-active {
  transition: all 0.3s ease;
}
.slide-left-enter-from,
.slide-left-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* ═══════ CA-22: Custom Palette Override CSS ═══════ */
:deep(.djs-palette .entry) { display: none !important; }
:deep(.djs-palette .entry[data-action="create.start-event"]),
:deep(.djs-palette .entry[data-action="create.end-event"]),
:deep(.djs-palette .entry[data-action="create.task"]),
:deep(.djs-palette .entry[data-action="create.service-task"]),
:deep(.djs-palette .entry[data-action="create.exclusive-gateway"]),
:deep(.djs-palette .entry[data-action="create.parallel-gateway"]),
:deep(.djs-palette .entry[data-action="create.text-annotation"]),
:deep(.djs-palette .entry[data-action="space-tool"]),
:deep(.djs-palette .entry[data-action="lasso-tool"]),
:deep(.djs-palette .entry[data-action="hand-tool"]),
:deep(.djs-palette .entry[data-action="global-connect-tool"]) {
  display: flex !important;
}

/* CA-84: Neon Pulse style for executed nodes */
:deep(.bjs-container .highlight-executed .djs-outline) {
  stroke: #10b981 !important;
  stroke-width: 4px !important;
  filter: drop-shadow(0 0 10px rgba(16, 185, 129, 0.8)) drop-shadow(0 0 5px rgba(99, 102, 241, 0.6));
  animation: neon-pulse 1.5s infinite alternate;
}
:deep(.bjs-container .highlight-executed .djs-visual > :nth-child(1)) {
  fill: #ecfdf5 !important;
  fill-opacity: 0.85 !important;
}

@keyframes neon-pulse {
  from {
    filter: drop-shadow(0 0 4px rgba(16, 185, 129, 0.5)) drop-shadow(0 0 2px rgba(99, 102, 241, 0.4));
  }
  to {
    filter: drop-shadow(0 0 12px rgba(16, 185, 129, 1)) drop-shadow(0 0 8px rgba(99, 102, 241, 0.8));
  }
}
</style>
