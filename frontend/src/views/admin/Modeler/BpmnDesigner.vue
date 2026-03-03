<template>
  <div class="h-full w-full bg-gray-50 dark:bg-gray-900 flex flex-col" v-cloak>

    <!-- ═══════ Toast Notifications ═══════ -->
    <Transition name="toast-slide">
      <div v-if="toast.msg" :class="toast.type === 'success' ? 'bg-green-600' : 'bg-red-600'" class="fixed top-4 right-4 z-[100] text-white px-5 py-3 rounded-lg shadow-xl flex items-center space-x-3">
        <span class="text-sm font-medium">{{ toast.msg }}</span>
        <button @click="toast.msg = ''" class="ml-2 opacity-70 hover:opacity-100">&times;</button>
      </div>
    </Transition>

    <!-- ═══════ Top Toolbar ═══════ -->
    <header class="flex flex-wrap justify-between items-center px-6 py-3 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 shrink-0 gap-3">
      <div class="flex items-center space-x-3">
        <button @click="showCatalog = true" class="text-sm font-medium text-gray-600 dark:text-gray-300 hover:text-blue-600 flex items-center space-x-1">
          <span>📜</span><span>Catálogo</span>
        </button>
        <span class="text-gray-300">|</span>
        <h1 class="text-lg font-bold text-gray-900 dark:text-white">{{ currentProcessName || 'Proceso Sin Título' }}</h1>
        <span v-if="processStatus" class="text-xs font-bold uppercase tracking-wider px-2 py-0.5 rounded-full"
              :class="{
                'bg-yellow-100 text-yellow-800': processStatus === 'BORRADOR',
                'bg-green-100 text-green-800': processStatus === 'ACTIVO',
                'bg-gray-100 text-gray-600': processStatus === 'ARCHIVADO'
              }">{{ processStatus }}</span>
      </div>

      <div class="flex items-center gap-2 flex-wrap">
        <!-- Import -->
        <label class="cursor-pointer bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 px-3 py-1.5 rounded-md shadow-sm text-xs font-medium hover:bg-gray-50 dark:hover:bg-gray-600 flex gap-1 items-center transition">
          ⬆️ Importar
          <input type="file" @change="handleFileUpload" accept=".bpmn,.xml" class="hidden" />
        </label>
        <!-- Export -->
        <button @click="downloadXML" class="bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 px-3 py-1.5 rounded-md shadow-sm text-xs font-medium hover:bg-gray-50 dark:hover:bg-gray-600 flex items-center gap-1 transition">
          ⬇️ Exportar .bpmn
        </button>
        <!-- Copilot -->
        <button @click="showCopilot = !showCopilot" class="bg-slate-900 text-white px-3 py-1.5 rounded-md shadow text-xs font-medium hover:bg-black flex items-center gap-1 transition">
          🧠 Copiloto IA
        </button>
        <!-- Sandbox -->
        <button @click="runSandbox" class="bg-amber-500 text-white px-3 py-1.5 rounded-md shadow text-xs font-medium hover:bg-amber-600 flex items-center gap-1 transition">
          🧪 Sandbox
        </button>
        <!-- Versions -->
        <button @click="showVersions = !showVersions" class="bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 px-3 py-1.5 rounded-md shadow-sm text-xs font-medium hover:bg-gray-50 dark:hover:bg-gray-600 flex items-center gap-1 transition">
          📜 Versiones
        </button>
        <!-- Deploy / Request Deploy (RBAC) -->
        <button v-if="userRole === 'RELEASE_MANAGER'" @click="showDeployModal = true" :disabled="isDeploying" class="bg-indigo-600 text-white px-3 py-1.5 rounded-md shadow text-xs font-bold hover:bg-indigo-700 disabled:opacity-50 flex items-center gap-1 transition">
          🚀 Desplegar
        </button>
        <button v-else @click="requestDeploy" class="bg-purple-600 text-white px-3 py-1.5 rounded-md shadow text-xs font-bold hover:bg-purple-700 flex items-center gap-1 transition">
          📩 Solicitar Despliegue
        </button>
      </div>
    </header>

    <!-- ═══════ Status Bar (Lock + AutoSave + PreFlight) ═══════ -->
    <div class="flex items-center justify-between px-6 py-1.5 bg-gray-100 dark:bg-gray-800/50 border-b border-gray-200 dark:border-gray-700 text-xs shrink-0">
      <!-- Lock Indicator -->
      <div class="flex items-center space-x-4">
        <span v-if="lockOwner" class="flex items-center text-orange-600 font-medium">
          🔒 Editando: {{ lockOwner }} desde {{ lockSince }}
        </span>
        <span v-else class="text-green-600 font-medium">🔓 Disponible para edición</span>
      </div>

      <div class="flex items-center space-x-4">
        <!-- Auto-Save -->
        <span class="text-gray-500 dark:text-gray-400">
          ✅ Guardado: hace {{ autoSaveAgo }}s
        </span>
        <!-- Pre-Flight Badge -->
        <span class="font-bold px-2 py-0.5 rounded-full"
              :class="{
                'bg-green-100 text-green-800': preFlightStatus === 'VALIDATED',
                'bg-yellow-100 text-yellow-800': preFlightStatus === 'PENDING',
                'bg-red-100 text-red-800': preFlightStatus === 'ERROR'
              }">
          {{ preFlightStatus === 'VALIDATED' ? '✅ Validado' : preFlightStatus === 'ERROR' ? '❌ Errores' : '⚠️ Pendiente' }}
        </span>
      </div>
    </div>

    <!-- ═══════ Main Canvas Area ═══════ -->
    <main class="flex-1 flex min-h-0 overflow-hidden relative">
      
      <!-- BPMN Canvas -->
      <div ref="canvasContainer" class="flex-1 overflow-hidden h-full bpmn-canvas"></div>

      <!-- ═══════ Properties Side Panel ═══════ -->
      <aside class="w-80 border-l border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 shrink-0 flex flex-col overflow-y-auto">
        <div class="p-4 border-b border-gray-200 dark:border-gray-700">
          <h3 class="text-xs font-bold text-gray-400 uppercase tracking-widest flex items-center gap-2">
            ⚙️ Camunda Properties
          </h3>
        </div>

        <div class="p-4 space-y-5 flex-1">
          <!-- Naming Dual -->
          <div>
            <label class="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">Nombre de Negocio</label>
            <input type="text" v-model="currentProcessName" @input="onDiagramEdit" class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded focus:ring-indigo-500 focus:border-indigo-500 p-2 border" placeholder="Ej: Crédito de Consumo" />
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">ID Técnico</label>
            <input type="text" v-model="processId" class="w-full text-xs font-mono border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded focus:ring-indigo-500 focus:border-indigo-500 p-2 border bg-gray-50 dark:bg-gray-900" placeholder="Auto: credito-de-consumo" />
          </div>

          <!-- SLA Global -->
          <div class="p-3 bg-blue-50 dark:bg-blue-900/20 border border-blue-100 dark:border-blue-800 rounded">
            <label class="block text-xs font-bold text-blue-800 dark:text-blue-300 mb-1">⏱ SLA Global (Horas)</label>
            <input type="number" v-model.number="globalSla" min="1" class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border" placeholder="72" />
            <!-- Propiedades: Tarea de Usuario (Intake / Approval) -->
            <div v-if="selectedElement.type === 'bpmn:UserTask'" class="space-y-4">
              <div>
                <label class="block text-xs font-bold text-gray-700 mb-1">Formulario Asignado (FormKey)</label>
                <select v-model="selectedElement.props.formKey" class="w-full text-xs border-gray-300 rounded shadow-sm focus:ring-indigo-500 max-w-[200px]">
                  <option value="">-- Sin Formulario --</option>
                  <option value="form_solicitud_v1">form_solicitud_v1 (Simple)</option>
                  <option value="iform_maestro_credito">iform_maestro_credito (Dual)</option>
                </select>
              </div>
              <div class="pt-3 border-t border-gray-200">
                <label class="block text-xs font-bold text-gray-700 mb-2 flex items-center gap-1">⏱️ SLA Timeout (ISO 8601)</label>
                <input type="text" v-model="selectedElement.props.sla" class="w-full text-xs border-gray-300 rounded shadow-sm focus:ring-indigo-500 font-mono" placeholder="Ej: P2D (2 Días)" />
              </div>

              <!-- SharePoint Integration Checkbox (CA-2) -->
              <div v-if="selectedElement.name && selectedElement.name.toLowerCase().includes('intake')" class="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-md">
                <div class="flex items-start gap-2">
                  <input type="checkbox" id="spFolderCheck" v-model="selectedElement.props.createSharepointFolder" class="mt-0.5 text-blue-600 rounded border-blue-300 focus:ring-blue-500 shadow-sm" />
                  <label for="spFolderCheck" class="text-[11px] font-bold text-blue-900 cursor-pointer leading-tight">
                    Create Unique SharePoint Sub-folder for this generic Process Instance (CA-2)
                  </label>
                </div>
              </div>
            </div>
          </div>

          <!-- Process Pattern -->
          <div>
            <label class="block text-xs font-bold text-gray-700 dark:text-gray-300 mb-1">Patrón de Proceso</label>
            <select v-model="processPattern" class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border">
              <option value="SIMPLE">🟢 Simple (Formularios independientes)</option>
              <option value="IFORM_MAESTRO">🔵 iForm Maestro (Formulario mutante)</option>
            </select>
          </div>

          <!-- User Task Properties -->
          <div class="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow-sm">
            <label class="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-2">📝 FormKey (User Task)</label>
            <p class="text-[10px] text-gray-500 dark:text-gray-400 mb-2">Formulario renderizado en Workdesk</p>
            <select v-model="selectedFormKey" class="w-full text-xs font-mono border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border bg-indigo-50/30 dark:bg-indigo-900/20 text-indigo-800 dark:text-indigo-300">
              <option value="">-- Sin FormKey --</option>
              <option v-for="form in filteredForms" :key="form.key" :value="form.key">
                {{ form.type === 'MAESTRO' ? '🔵' : '🟢' }} {{ form.name }} ({{ form.key }})
              </option>
            </select>
          </div>

          <!-- Service Task Connector -->
          <div class="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow-sm">
            <label class="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-2">🔌 Conector API (Service Task)</label>
            <select v-model="selectedConnector" class="w-full text-xs font-mono border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border">
              <option value="">-- Sin Conector --</option>
              <option v-for="c in availableConnectors" :key="c.id" :value="c.id">
                {{ c.icon }} {{ c.name }}
              </option>
            </select>
          </div>

          <!-- Escalamiento -->
          <div class="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow-sm">
            <label class="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-2">🔺 Escalamiento & Ping-Pong</label>
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

          <!-- Call Activity Link -->
          <button class="w-full text-xs text-center py-2 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded text-gray-500 dark:text-gray-400 hover:border-indigo-400 hover:text-indigo-600 transition">
            🔗 Abrir Sub-Proceso (Call Activity)
          </button>

          <!-- AI Copilot Quick Action -->
          <div class="pt-2 border-t border-gray-200 dark:border-gray-700">
            <button @click="showCopilot = true" class="w-full bg-slate-900 hover:bg-black text-white px-3 py-2 rounded text-xs font-semibold flex items-center justify-center gap-2 transition">
              🧠 Auditoría ISO-9001 (Copilot)
            </button>
          </div>
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
          <div class="flex justify-end space-x-3 pt-2">
            <button @click="showDeployModal = false" class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition">Cancelar</button>
            <button @click="confirmDeploy" :disabled="isDeploying" class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg shadow transition disabled:opacity-50">
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
              <button @click="newProcessOrigin = 'SCRATCH'" :class="newProcessOrigin === 'SCRATCH' ? 'bg-gray-200 dark:bg-gray-600 font-bold' : ''" class="flex-1 border rounded-lg p-3 text-sm text-center hover:bg-gray-100 dark:hover:bg-gray-700 transition">Desde Cero</button>
              <button @click="newProcessOrigin = 'TEMPLATE'" :class="newProcessOrigin === 'TEMPLATE' ? 'bg-gray-200 dark:bg-gray-600 font-bold' : ''" class="flex-1 border rounded-lg p-3 text-sm text-center hover:bg-gray-100 dark:hover:bg-gray-700 transition">Usar Plantilla</button>
            </div>
          </div>
          <div class="flex justify-end space-x-3 pt-2">
            <button @click="showNewProcessModal = false" class="px-4 py-2 text-sm text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition">Cancelar</button>
            <button @click="createNewProcess" :disabled="!newProcessName.trim()" class="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow transition disabled:opacity-50">Crear Proceso</button>
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
        <div class="flex-1 p-4 overflow-y-auto space-y-3 text-sm font-mono">
          <div v-for="(msg, i) in copilotMessages" :key="i" class="flex items-start gap-2">
            <span :class="msg.role === 'ai' ? 'text-emerald-400' : 'text-blue-400'">{{ msg.role === 'ai' ? '🤖' : '👤' }}</span>
            <p class="text-gray-300 leading-relaxed whitespace-pre-wrap">{{ msg.text }}</p>
          </div>
          <div v-if="copilotLoading" class="flex items-center gap-2 text-emerald-400">
            <svg class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path></svg>
            Analizando diagrama...
          </div>
        </div>
        <div class="px-4 py-2 bg-gray-800 flex gap-2 shrink-0">
          <input v-model="copilotInput" @keyup.enter="sendCopilotMessage" type="text" placeholder="Pregunta al Copiloto sobre tu proceso..." class="flex-1 bg-gray-700 text-white text-sm rounded px-3 py-1.5 border border-gray-600 focus:border-emerald-500 focus:ring-0" />
          <button @click="sendCopilotMessage" :disabled="copilotLoading" class="bg-emerald-600 hover:bg-emerald-700 text-white px-3 py-1.5 rounded text-xs font-bold disabled:opacity-50 transition">Enviar</button>
        </div>
      </div>
    </Transition>

    <!-- ═══════ Panel: Version History ═══════ -->
    <Transition name="slide-up">
      <div v-if="showVersions" class="absolute bottom-0 right-0 w-96 h-72 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-tl-xl shadow-2xl flex flex-col z-40">
        <div class="flex items-center justify-between px-4 py-2 border-b border-gray-200 dark:border-gray-700 shrink-0">
          <h4 class="text-sm font-bold text-gray-700 dark:text-white">📜 Historial de Versiones</h4>
          <button @click="showVersions = false" class="text-gray-400 hover:text-red-500">&times;</button>
        </div>
        <div class="flex-1 overflow-y-auto p-3 space-y-2">
          <div v-for="v in versionHistory" :key="v.version" class="flex justify-between items-center p-2 rounded hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer text-sm border border-gray-100 dark:border-gray-700">
            <div>
              <span class="font-bold text-gray-800 dark:text-white">v{{ v.version }}</span>
              <p class="text-[10px] text-gray-500">{{ v.date }} — {{ v.author }}</p>
            </div>
            <span :class="v.status === 'ACTIVO' ? 'text-green-600' : 'text-gray-500'" class="text-xs font-bold">{{ v.status }}</span>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ═══════ Process Catalog Overlay ═══════ -->
    <div v-if="showCatalog" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-6">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-2xl w-full max-w-3xl max-h-[80vh] flex flex-col overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between shrink-0">
          <h3 class="text-lg font-bold text-gray-900 dark:text-white">📜 Catálogo de Procesos</h3>
          <div class="flex items-center gap-3">
            <button @click="showNewProcessModal = true; showCatalog = false" class="bg-blue-600 text-white px-3 py-1.5 rounded text-xs font-bold hover:bg-blue-700 transition">+ Nuevo Proceso</button>
            <button @click="showCatalog = false" class="text-gray-400 hover:text-gray-600 text-xl">&times;</button>
          </div>
        </div>
        <div class="flex-1 overflow-y-auto">
          <table class="w-full text-sm">
            <thead class="bg-gray-50 dark:bg-gray-900 sticky top-0">
              <tr>
                <th class="text-left px-4 py-2 text-xs font-bold text-gray-500 uppercase">Nombre</th>
                <th class="text-left px-4 py-2 text-xs font-bold text-gray-500 uppercase">Estado</th>
                <th class="text-left px-4 py-2 text-xs font-bold text-gray-500 uppercase">Versión</th>
                <th class="text-left px-4 py-2 text-xs font-bold text-gray-500 uppercase">Última Edición</th>
                <th class="text-left px-4 py-2 text-xs font-bold text-gray-500 uppercase">Autor</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in catalogProcesses" :key="p.id" @click="loadProcess(p)" class="hover:bg-blue-50 dark:hover:bg-blue-900/20 cursor-pointer border-t border-gray-100 dark:border-gray-700 transition">
                <td class="px-4 py-3 font-medium text-gray-900 dark:text-white">{{ p.name }}</td>
                <td class="px-4 py-3">
                  <span class="text-xs font-bold uppercase px-2 py-0.5 rounded-full"
                        :class="{
                          'bg-yellow-100 text-yellow-800': p.status === 'BORRADOR',
                          'bg-green-100 text-green-800': p.status === 'ACTIVO',
                          'bg-gray-100 text-gray-600': p.status === 'ARCHIVADO'
                        }">{{ p.status }}</span>
                </td>
                <td class="px-4 py-3 text-gray-600 dark:text-gray-400">v{{ p.version }}</td>
                <td class="px-4 py-3 text-gray-600 dark:text-gray-400">{{ p.lastEdited }}</td>
                <td class="px-4 py-3 text-gray-600 dark:text-gray-400">{{ p.author }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, computed } from 'vue';

// ── Canvas ───────────────────────────────────────────────────
const canvasContainer = ref<HTMLElement | null>(null);
let modelerInstance: any = null;

// ── Process State ────────────────────────────────────────────
const currentProcessName = ref('Crédito de Consumo V1');
const processId = ref('credito-consumo-v1');
const processStatus = ref<'BORRADOR' | 'ACTIVO' | 'ARCHIVADO'>('BORRADOR');
const processPattern = ref<'SIMPLE' | 'IFORM_MAESTRO'>('SIMPLE');
const globalSla = ref(72);
const selectedFormKey = ref('');
const selectedConnector = ref('');
const userRole = ref('RELEASE_MANAGER'); // Mock RBAC

// ── Lock ─────────────────────────────────────────────────────
const lockOwner = ref<string | null>('ana.garcia@ibpms.co');
const lockSince = ref('20:15');

// ── Auto-Save ────────────────────────────────────────────────
const autoSaveAgo = ref(5);
let autoSaveInterval: any = null;

// ── Pre-Flight ───────────────────────────────────────────────
const preFlightStatus = ref<'VALIDATED' | 'PENDING' | 'ERROR'>('PENDING');

// ── Deploy ───────────────────────────────────────────────────
const isDeploying = ref(false);
const showDeployModal = ref(false);
const deployStrategy = ref('coexist');
const activeInstances = ref(12);

// ── New Process Modal ────────────────────────────────────────
const showNewProcessModal = ref(false);
const newProcessName = ref('');
const newProcessPattern = ref<'SIMPLE' | 'IFORM_MAESTRO'>('SIMPLE');
const newProcessOrigin = ref<'SCRATCH' | 'TEMPLATE'>('SCRATCH');

// ── Copilot ──────────────────────────────────────────────────
const showCopilot = ref(false);
const copilotInput = ref('');
const copilotLoading = ref(false);
const copilotMessages = ref<{ role: 'ai' | 'user'; text: string }[]>([
  { role: 'ai', text: 'Copiloto listo. Puedo auditar tu proceso contra ISO 9001, sugerir mejoras o identificar riesgos.' }
]);

// ── Versions ─────────────────────────────────────────────────
const showVersions = ref(false);
const versionHistory = ref([
  { version: 3, date: '2026-03-01', author: 'Ana García', status: 'BORRADOR' },
  { version: 2, date: '2026-02-15', author: 'Carlos M.', status: 'ACTIVO' },
  { version: 1, date: '2026-01-20', author: 'Ana García', status: 'ARCHIVADO' }
]);

// ── Catalog ──────────────────────────────────────────────────
const showCatalog = ref(false);
const catalogProcesses = ref([
  { id: '1', name: 'Crédito de Consumo', status: 'ACTIVO', version: 2, lastEdited: '2026-02-15', author: 'Ana García' },
  { id: '2', name: 'Onboarding Jurídico', status: 'BORRADOR', version: 1, lastEdited: '2026-03-01', author: 'Carlos M.' },
  { id: '3', name: 'PQRS Ciudadano', status: 'ACTIVO', version: 4, lastEdited: '2026-02-28', author: 'Laura Ríos' },
  { id: '4', name: 'Compras IT (Archivado)', status: 'ARCHIVADO', version: 5, lastEdited: '2026-01-10', author: 'Miguel T.' }
]);

// ── Toast ────────────────────────────────────────────────────
const toast = ref<{ msg: string; type: 'success' | 'error' }>({ msg: '', type: 'success' });
const showToast = (msg: string, type: 'success' | 'error' = 'success') => {
  toast.value = { msg, type };
  setTimeout(() => { toast.value.msg = ''; }, 5000);
};

// ── Mock Form/Connector Dropdowns ────────────────────────────
const availableForms = ref([
  { key: 'iForm_Credito_Base', name: 'Crédito Base', type: 'MAESTRO' },
  { key: 'iForm_Onboarding_V3', name: 'Onboarding V3', type: 'MAESTRO' },
  { key: 'form_aprobacion', name: 'Aprobación Rápida', type: 'SIMPLE' },
  { key: 'form_revision_docs', name: 'Revisión Documentos', type: 'SIMPLE' }
]);
const availableConnectors = ref([
  { id: 'o365', name: 'O365/Exchange', icon: '📧' },
  { id: 'sharepoint', name: 'SharePoint MS', icon: '📁' },
  { id: 'netsuite', name: 'Oracle NetSuite', icon: '💰' }
]);

const filteredForms = computed(() => {
  if (processPattern.value === 'SIMPLE') return availableForms.value.filter(f => f.type === 'SIMPLE');
  return availableForms.value;
});

// ── BPMN Template ────────────────────────────────────────────
const emptyBpmn = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" id="Definitions_1x5" targetNamespace="http://bpmn.io/schema/bpmn" exporter="iBPMS Designer Vue" exporterVersion="2.0">
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

// ── Lifecycle ────────────────────────────────────────────────
onMounted(async () => {
  try {
    const { default: BpmnModeler } = await import('bpmn-js/lib/Modeler');
    const minimapModule = (await import('diagram-js-minimap')).default;

    modelerInstance = new BpmnModeler({
      container: canvasContainer.value!,
      additionalModules: [minimapModule]
    });

    await modelerInstance.importXML(emptyBpmn);
    modelerInstance.get('canvas').zoom('fit-viewport');

    // Listen for changes to reset pre-flight
    modelerInstance.on('commandStack.changed', () => {
      preFlightStatus.value = 'PENDING';
    });

    // Open minimap by default
    try { modelerInstance.get('minimap').open(); } catch(_) {}

  } catch (err) {
    console.error('bpmn-js mount failed, using fallback placeholder:', err);
    if (canvasContainer.value) {
      canvasContainer.value.innerHTML = `<div class="p-8 text-center text-gray-500 font-mono text-sm flex flex-col items-center justify-center h-full bg-white"><span class="text-4xl mb-4">⚙️</span><p>Canvas BPMN 2.0</p><p class="text-xs mt-2">bpmn-js renderizando para ${processId.value}</p></div>`;
    }
  }

  // Auto-save timer (every 30s)
  autoSaveInterval = setInterval(() => {
    autoSaveAgo.value = 0;
    saveDraft();
  }, 30000);

  // Tick the "ago" counter every second
  setInterval(() => { autoSaveAgo.value++; }, 1000);
});

onBeforeUnmount(() => {
  if (modelerInstance) modelerInstance.destroy();
  if (autoSaveInterval) clearInterval(autoSaveInterval);
});

// ── Auto-slug processId from name ────────────────────────────
watch(currentProcessName, (name) => {
  if (name) {
    processId.value = name.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, '');
  }
});

// ── Actions ──────────────────────────────────────────────────
const onDiagramEdit = () => {
  preFlightStatus.value = 'PENDING';
};

const saveDraft = async () => {
  if (!modelerInstance) return;
  try {
    const { xml } = await modelerInstance.saveXML({ format: true });
    console.log('[AutoSave] Draft XML saved', xml?.substring(0, 80));
    // In production: PUT /api/v1/design/processes/{id}/draft
  } catch (err) {
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
    const nodeCount = (text.match(/<bpmn:/g) || []).length;
    if (nodeCount > 100) {
      showToast('⚠️ Advertencia: Alta complejidad. Proceso con más de 100 nodos.', 'error');
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

const confirmDeploy = async () => {
  isDeploying.value = true;
  try {
    if (modelerInstance) {
      const { xml } = await modelerInstance.saveXML({ format: true });
      console.log('[Deploy] Sending XML to /api/v1/deployments', { strategy: deployStrategy.value });
      // POST /api/v1/deployments
    }
    await new Promise(r => setTimeout(r, 1500));
    showToast(`✅ Proceso "${currentProcessName.value}" desplegado exitosamente`);
    processStatus.value = 'ACTIVO';
    showDeployModal.value = false;
  } catch (err) {
    showToast('Error desplegando proceso', 'error');
  } finally {
    isDeploying.value = false;
  }
};

const requestDeploy = () => {
  showToast('📩 Solicitud de despliegue enviada al Release Manager');
};

const runSandbox = () => {
  showToast('🧪 Sandbox: Ejecutando simulación del proceso con datos dummy...');
  // In production: POST /api/v1/design/processes/{id}/sandbox
};

const createNewProcess = () => {
  currentProcessName.value = newProcessName.value;
  processPattern.value = newProcessPattern.value;
  processStatus.value = 'BORRADOR';
  showNewProcessModal.value = false;
  if (modelerInstance) {
    modelerInstance.importXML(emptyBpmn);
  }
  showToast(`Proceso "${newProcessName.value}" creado`);
  newProcessName.value = '';
};

const loadProcess = (p: any) => {
  currentProcessName.value = p.name;
  processStatus.value = p.status;
  showCatalog.value = false;
  showToast(`Cargado: ${p.name} v${p.version}`);
};

const sendCopilotMessage = async () => {
  if (!copilotInput.value.trim()) return;
  copilotMessages.value.push({ role: 'user', text: copilotInput.value });
  const query = copilotInput.value;
  copilotInput.value = '';
  copilotLoading.value = true;

  await new Promise(r => setTimeout(r, 1500));
  
  // Mock AI response
  copilotMessages.value.push({
    role: 'ai',
    text: `Análisis del proceso "${currentProcessName.value}":\n• ISO 9001 §7.5: Se detectaron 2 tareas sin documentación de entrada.\n• Sugerencia: Agregar un Gateway XOR antes del End Event para validar resultado.\n• Score de conformidad: 78% → Recomendación: Revisar SLAs individuales.`
  });
  copilotLoading.value = false;
};
</script>

<style>
/* bpmn-js core styles */
@import 'bpmn-js/dist/assets/diagram-js.css';
@import 'bpmn-js/dist/assets/bpmn-js.css';
@import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';
@import 'diagram-js-minimap/assets/diagram-js-minimap.css';

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
</style>
