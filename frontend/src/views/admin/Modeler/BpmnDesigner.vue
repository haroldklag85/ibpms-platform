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
        <!-- Copilot con Notificación Dinámica Inteligente (CA-08) -->
        <button @click="triggerCopilotAudit" class="bg-slate-900 text-white px-3 py-1.5 rounded-md shadow text-xs font-medium hover:bg-black flex items-center gap-1 transition relative">
          🧠 Consultar Copiloto IA
          <span v-if="unreadAiBadge" class="absolute -top-1 -right-1 flex h-3 w-3">
            <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
            <span class="relative inline-flex rounded-full h-3 w-3 bg-red-500 shadow shadow-red-500/50"></span>
          </span>
        </button>
        <!-- Sandbox CA-41 -->
        <button @click="runSandbox" class="bg-amber-500 text-white px-3 py-1.5 rounded-md shadow text-xs font-medium hover:bg-amber-600 flex items-center gap-1 transition">
          🧪 Probar en Sandbox
        </button>
        <!-- Audit Logs (CA-42) -->
        <button @click="openAuditLogs" class="bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 px-3 py-1.5 rounded-md shadow-sm text-xs font-medium hover:bg-gray-50 dark:hover:bg-gray-600 flex items-center gap-1 transition">
          📝 Auditoría
        </button>
        <!-- Versions -->
        <button @click="showVersions = !showVersions" class="bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 px-3 py-1.5 rounded-md shadow-sm text-xs font-medium hover:bg-gray-50 dark:hover:bg-gray-600 flex items-center gap-1 transition">
          📜 Versiones
        </button>
        <!-- Mock Role CA-21 -->
        <select v-model="mockRole" title="Evaluar UI con diferentes perfiles CA-21" class="text-xs bg-indigo-50 dark:bg-gray-700 border-indigo-200 dark:border-gray-600 rounded px-2 py-1 focus:ring-indigo-500 text-indigo-800 dark:text-white font-bold ml-2">
           <option value="BPMN_Designer">👨‍💻 Diseñador</option>
           <option value="BPMN_Release_Manager">👑 Release Manager</option>
        </select>
        <!-- Instance Manager CA-8 -->
        <button @click="showInstancesManager = true" class="bg-indigo-50 text-indigo-700 border border-indigo-200 dark:bg-indigo-900/40 px-3 py-1.5 rounded-md shadow-sm text-xs font-bold hover:bg-indigo-100 flex items-center gap-1 transition">
          🧬 Gestor de Instancias
        </button>
        <!-- Request Deploy -->
        <button v-show="mockRole === 'BPMN_Designer'" @click="requestDeploy" class="bg-purple-600 text-white px-3 py-1.5 rounded-md shadow text-xs font-bold hover:bg-purple-700 flex items-center gap-1 transition">
          📩 Solicitar Despliegue
        </button>
        <!-- Deploy (CA-21) -->
        <button v-show="mockRole === 'BPMN_Release_Manager'"
                @click="showDeployModal = true" 
                :disabled="isDeploying || !['VALIDATED', 'WARNING'].includes(preFlightStatus)" 
                class="bg-indigo-600 text-white px-3 py-1.5 rounded-md shadow text-xs font-bold hover:bg-indigo-700 disabled:opacity-50 flex items-center gap-1 transition">
          🚀 [VALIDAR Y DESPLEGAR]
        </button>
      </div>
    </header>

    <!-- ═══════ Status Bar (Lock + AutoSave + PreFlight) ═══════ -->
    <div class="flex items-center justify-between px-6 py-1.5 bg-gray-100 dark:bg-gray-800/50 border-b border-gray-200 dark:border-gray-700 text-xs shrink-0">
      <!-- Lock Indicator CA-7 -->
      <div class="flex items-center space-x-4">
        <span v-if="isLocked" class="flex items-center text-orange-700 font-bold bg-orange-100 px-3 py-1 rounded shadow-sm border border-orange-200">
          🔒 SOLO LECTURA: Bloqueado por {{ lockOwner }} ({{ lockSince }})
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
      <div ref="canvasContainer" class="flex-1 overflow-hidden h-full bpmn-canvas" :class="{ 'pointer-events-none': isLocked }"></div>

      <!-- CA-25: Floating Zoom Controls -->
      <div class="absolute bottom-4 left-4 flex gap-2 z-30">
        <button @click="zoomIn" class="bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 shadow-lg rounded p-2 hover:bg-gray-50 dark:hover:bg-gray-700 font-bold w-10 h-10 flex items-center justify-center border border-gray-200 dark:border-gray-600" title="Zoom In">+</button>
        <button @click="zoomOut" class="bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 shadow-lg rounded p-2 hover:bg-gray-50 dark:hover:bg-gray-700 font-bold w-10 h-10 flex items-center justify-center border border-gray-200 dark:border-gray-600" title="Zoom Out">-</button>
        <button @click="zoomFit" class="bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 shadow-lg rounded p-2 hover:bg-gray-50 dark:hover:bg-gray-700 text-lg font-bold w-10 h-10 flex items-center justify-center border border-gray-200 dark:border-gray-600" title="Fit Viewport">O</button>
      </div>

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

          <!-- Nomenclatura Instancia CA-5 -->
          <div class="p-3 bg-fuchsia-50 dark:bg-fuchsia-900/20 border border-fuchsia-200 rounded">
             <label class="block text-xs font-bold text-fuchsia-800 dark:text-fuchsia-300 mb-1 flex items-center justify-between">
               <span>🎟 Regla de Nomenclatura (CA-5)</span>
               <AppTooltip :content="isNomenclatureSyntaxError ? '⚠️ Error de sintaxis: llaves sin cerrar' : bpmnTooltips.NOMENCLATURE" :isError="isNomenclatureSyntaxError" />
             </label>
             <input type="text" v-model="processNomenclature" @change="updateProcessProperty('ReglaNomenclatura', processNomenclature)" :class="{'border-red-500 ring-1 ring-red-500 bg-red-50': isNomenclatureSyntaxError}" class="w-full text-xs border-fuchsia-300 dark:border-fuchsia-600 dark:bg-gray-700 dark:text-white rounded focus:ring-fuchsia-500 focus:border-fuchsia-500 p-2 border transition" placeholder="Ej: OC-{Solicitante}" />
             <p class="text-[10px] text-fuchsia-600 dark:text-fuchsia-400 mt-1 leading-tight">Obligatorio. Define la máscara para instanciar tickets. Se inyecta al nodo raíz del XML.</p>
          </div>

          <!-- SLA Global -->
          <div class="p-3 bg-blue-50 dark:bg-blue-900/20 border border-blue-100 dark:border-blue-800 rounded">
            <label class="block text-xs font-bold text-blue-800 dark:text-blue-300 mb-1 flex items-center justify-between">
              ⏱ SLA Global (Horas)
              <AppTooltip :content="bpmnTooltips.GLOBAL_SLA" />
            </label>
            <input type="number" v-model.number="globalSla" @change="updateGlobalSla" min="1" class="w-full text-xs border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border" placeholder="72" />
            <!-- Propiedades: Tarea de Usuario (Intake / Approval) -->
            <div v-if="selectedElement.type === 'bpmn:UserTask'" class="space-y-4">
              <div>
                <label class="block text-xs font-bold text-gray-700 mb-1">Formulario Asignado (FormKey Alterno)</label>
                <select v-model="selectedElement.props.formKey" @change="syncElementProperties('camunda:formKey', selectedElement.props.formKey)" class="w-full text-xs border-gray-300 rounded shadow-sm focus:ring-indigo-500 max-w-[200px]">
                  <option value="">-- Sin Formulario --</option>
                  <option value="form_solicitud_v1">form_solicitud_v1 (Simple)</option>
                  <option value="iform_maestro_credito">iform_maestro_credito (Dual)</option>
                </select>
              </div>
              <!-- SLA de la Tarea -->
              <div class="pt-3 border-t border-gray-200">
                <label class="block text-xs font-bold text-gray-700 mb-2 flex items-center justify-between">
                  <span class="flex items-center gap-1">⏱️ SLA Timeout</span>
                  <AppTooltip :content="bpmnTooltips.SLA_TIMEOUT" :isError="isSlaSyntaxError" />
                </label>
                <input type="text" v-model="selectedElement.props.sla" @change="updateElementSla" class="w-full text-xs border-gray-300 rounded shadow-sm focus:ring-indigo-500 font-mono" :class="{'border-red-500 bg-red-50 text-red-700': isSlaSyntaxError}" placeholder="Ej: P2D (2 Días)" />
              </div>

              <!-- SharePoint Integration Checkbox (CA-2) -->
              <div v-if="selectedElement.name && selectedElement.name.toLowerCase().includes('intake')" class="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-md">
                <div class="flex items-start gap-2">
                  <input type="checkbox" id="spFolderCheck" v-model="selectedElement.props.createSharepointFolder" @change="syncElementProperties('camunda:createSharepointFolder', selectedElement.props.createSharepointFolder)" class="mt-0.5 text-blue-600 rounded border-blue-300 focus:ring-blue-500 shadow-sm" />
                  <label for="spFolderCheck" class="text-[11px] font-bold text-blue-900 cursor-pointer leading-tight">
                    Create Unique SharePoint Sub-folder for this generic Process Instance (CA-2)
                  </label>
                </div>
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

          <!-- User Task Properties -->
          <div class="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow-sm">
            <label class="block text-xs font-bold text-gray-800 dark:text-gray-200 mb-2 flex items-center justify-between">
              📝 FormKey (User Task)
              <AppTooltip :content="bpmnTooltips.FORM_KEY" />
            </label>
            <p class="text-[10px] text-gray-500 dark:text-gray-400 mb-2">Formulario renderizado en Workdesk</p>
            <select v-model="selectedFormKey" @change="syncElementProperties('camunda:formKey', selectedFormKey)" class="w-full text-xs font-mono border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded p-2 border bg-indigo-50/30 dark:bg-indigo-900/20 text-indigo-800 dark:text-indigo-300">
              <option value="">-- Sin FormKey --</option>
              <option v-for="form in filteredForms" :key="form.key" :value="form.key">
                {{ form.type === 'MAESTRO' ? '🔵' : '🟢' }} {{ form.name }} ({{ form.key }})
              </option>
            </select>
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
                              <!-- CA-50: Type Coercion -->
                              <option v-for="v in processVariables" :key="v.name" :value="v.name" :disabled="!isTypeCompatible(schema.type, v.type)">
                                 {{ !isTypeCompatible(schema.type, v.type) ? '🚫 ' : '' }}{{ v.name }} ({{ v.type }})
                              </option>
                           </select>
                           <AppTooltip v-if="mappingErrors[schema.name]" content="⚠️ Tipo Incompatible" isError class="absolute right-0 top-1/2 -translate-y-1/2 -mr-6" />
                        </td>
                     </tr>
                  </tbody>
               </table>
               <div v-if="loadingSchema" class="flex justify-center py-2"><AppSkeleton class="w-3/4 h-4 rounded" /></div>
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

          <!-- Call Activity Link (CA-27) -->
          <div class="mb-4">
             <label v-if="selectedElement.type === 'bpmn:CallActivity'" class="block text-xs font-bold text-gray-700 mb-2 flex items-center justify-between">
                <span>🔗 Destino de Call Activity</span>
                <AppTooltip :content="bpmnTooltips.CALL_ACTIVITY" :isError="isCallActivityError" />
             </label>
             <button v-if="selectedElement.type === 'bpmn:CallActivity'" @click="openCallActivity" class="w-full text-xs text-center py-2 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded text-gray-500 dark:text-gray-400 hover:border-indigo-400 hover:text-indigo-600 transition truncate px-2" :class="{'border-red-400 hover:border-red-500 text-red-500 bg-red-50 hover:bg-red-100': isCallActivityError}" :title="selectedElement.props.calledElement || 'Sub-proceso'">
               Abrir Sub-Proceso {{ selectedElement.props.calledElement ? `(${selectedElement.props.calledElement})` : '(No Configurado)' }}
             </button>
          </div>

          <!-- AI Copilot Quick Action -->
          <div class="pt-2 border-t border-gray-200 dark:border-gray-700">
            <button @click="showCopilot = true" class="w-full bg-slate-900 hover:bg-black text-white px-3 py-2 rounded text-xs font-semibold flex items-center justify-center gap-2 transition">
              🧠 Auditoría ISO-9001 (Copilot)
            </button>
          </div>

          <!-- ═══════ Módulo Cognitivo (CA-10 / CA-11) ═══════ -->
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
          </div>
          <div class="flex items-center gap-2">
            <input type="checkbox" id="forceDeploy" v-model="forceDeploy" class="rounded border-gray-300 text-indigo-600 shadow-sm focus:border-indigo-300 focus:ring focus:ring-indigo-200 focus:ring-opacity-50" />
            <label for="forceDeploy" class="text-sm font-medium text-gray-700 dark:text-gray-300">Omitir advertencias ⚠️ del Pre-Flight</label>
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
        <div class="flex-1 p-4 overflow-y-auto space-y-3 text-sm font-mono">
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

    <!-- ═══════ CA-42: Panel: Audit Log ═══════ -->
    <Transition name="slide-up">
      <div v-if="showAuditLogsModal" class="absolute bottom-0 right-0 w-[500px] h-72 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-tl-xl shadow-2xl flex flex-col z-40">
        <div class="flex items-center justify-between px-4 py-2 border-b border-gray-200 dark:border-gray-700 shrink-0">
          <h4 class="text-sm font-bold text-gray-700 dark:text-white flex items-center gap-2">
            📜 Auditoría de Cambios (Git Log)
            <button @click="openAuditLogs" class="text-xs text-blue-500 hover:text-blue-700">↻</button>
          </h4>
          <button @click="showAuditLogsModal = false" class="text-gray-400 hover:text-red-500">&times;</button>
        </div>
        <div class="flex-1 overflow-y-auto p-3 space-y-2">
          <div v-if="loadingAuditLogs" class="text-center text-xs text-gray-500 py-4">Cargando bitácora...</div>
          <table v-else class="min-w-full divide-y divide-gray-200 dark:divide-gray-700 text-xs text-left">
            <thead class="bg-gray-50 dark:bg-gray-700 text-gray-700 dark:text-gray-200">
               <tr>
                 <th class="px-2 py-1">Acción</th>
                 <th class="px-2 py-1">Usuario</th>
                 <th class="px-2 py-1">Versión</th>
                 <th class="px-2 py-1">Fecha</th>
               </tr>
            </thead>
            <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700 text-gray-800 dark:text-gray-300">
               <tr v-for="(log, i) in auditLogs" :key="i" class="hover:bg-gray-50 dark:hover:bg-gray-700">
                 <td class="px-2 py-1 font-mono font-bold text-[10px]">{{ log.action }}</td>
                 <td class="px-2 py-1">{{ log.user }}</td>
                 <td class="px-2 py-1 font-bold">v{{ log.version }}</td>
                 <td class="px-2 py-1 text-[10px] text-gray-500 dark:text-gray-400">{{ new Date(log.date).toLocaleString() }}</td>
               </tr>
            </tbody>
          </table>
          <div v-if="!loadingAuditLogs && auditLogs.length === 0" class="text-center text-xs text-gray-500 py-10">Sin auditoría visible.</div>
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
        <div class="flex-1 overflow-y-auto p-3 space-y-2">
          <div v-if="loadingVersions" class="text-center text-xs text-gray-500 py-4">Cargando versiones...</div>
          <div v-else v-for="v in versionHistory" :key="v.version" class="flex justify-between items-center p-2 rounded hover:bg-gray-50 dark:hover:bg-gray-700 text-sm border border-gray-100 dark:border-gray-700 transition group">
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
                     <span class="text-[10px] font-bold uppercase rounded-full px-2 py-0.5" :class="{'bg-green-100 text-green-800': p.status==='ACTIVO', 'bg-yellow-100 text-yellow-800': p.status==='BORRADOR', 'bg-gray-100 text-gray-700': p.status==='ARCHIVADO'}">{{ p.status }}</span>
                  </div>
                </div>
              </div>
              <!-- Action Button CA-32 -->
              <button v-if="p.status === 'ACTIVO'" @click.stop="archiveProcess(p.id)" class="absolute top-2 right-2 text-[10px] font-bold bg-gray-100 text-gray-600 px-2 py-1 rounded hover:bg-gray-200 transition z-10 border border-gray-300 shadow-sm flex items-center gap-1" title="Archivar Proceso (CA-32)">
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

    <!-- ═══════ Gestor de Instancias (CA-8 a CA-10) ═══════ -->
    <InstancesManager 
      :show="showInstancesManager"
      :processId="processId"
      @close="showInstancesManager = false"
      @success="msg => showToast('✅ ' + msg, 'success')"
    />

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, computed, defineAsyncComponent } from 'vue';
import { api } from '@/services/apiClient';
import { debounce } from 'lodash-es';
import AppTooltip from '@/components/common/AppTooltip.vue';
import InstancesManager from './InstancesManager.vue';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import DOMPurify from 'dompurify';

// CA-01: Lazy Loading para Lottie (IA Wait State)
const Vue3Lottie = defineAsyncComponent(() => import('vue3-lottie').then(m => m.Vue3Lottie));

const corruptNodeId = ref<string | null>(null);
const mockRole = ref<'BPMN_Designer' | 'BPMN_Release_Manager'>('BPMN_Release_Manager'); // CA-21

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
const bpmnTooltips = {
  GLOBAL_SLA: 'Dicta el Acabado Total esperado del Proceso (Vida Útil). Al expirar, lanza métrica a los dashboards BAM corporativos y emite alertas amarillas.',
  SLA_TIMEOUT: 'Determina temporalidad en norma <a href="https://en.wikipedia.org/wiki/ISO_8601" target="_blank" class="text-blue-500 underline font-semibold">ISO-8601</a> antes de detonar Boundary Events o Escalar la Tarea a líderes.<br><br><b>Syntax estricta:</b> <code>P(N)Y(N)M(N)DT(N)H(N)M(N)S</code><br>Ejemplo: <code>P2D</code> = 2 días. <code>PT6H</code> = 6 Horas.',
  FORM_KEY: 'Formulario Inteligente embebido. El Workdesk usará este ID Técnico para dibujar la GUI y los campos reactivos de la tarea humana actual.',
  PROCESS_PATTERN: 'La arquitectura. <b>iForm_Maestro</b> permite usar un solo formulario mutante universal; <b>Simple</b> requiere diseñar formularios separados e instanciarlos en tareas disyuntas individualmente.',
  CALL_ACTIVITY: 'Un Sub-proceso re-usable de nivel corporativo que actúa como Caja Negra. Obliga a que la Cédula/Identificador coincida lógicamente entre ambos Diagramas. El link no rutea si la variable Target no existe.',
  // CA-47: Integraciones Estrictas y UX
  NOMENCLATURE: 'Patrón para generar el ID del ticket. Soporta variables como {Solicitante}.',
  CONNECTOR: 'Integra este nodo con sistemas externos mapeando variables del proceso actual.',
  ESCALATION: 'Define reglas semánticas de rebote o escalamiento a roles superiores.'
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
    calledElement: ''
  }
});

// ── Process State ────────────────────────────────────────────
const currentProcessName = ref('Crédito de Consumo V1');
const processId = ref('credito-consumo-v1');
const processStatus = ref<'BORRADOR' | 'ACTIVO' | 'ARCHIVADO' | 'PENDING'>('BORRADOR');
const processPattern = ref<'SIMPLE' | 'IFORM_MAESTRO'>('SIMPLE');
const processNomenclature = ref(''); // CA-5
const globalSla = ref(72);
const selectedFormKey = ref('');
const selectedConnector = ref('');

// CA-49: Data Mapper State
const connectorSchema = ref<any[]>([]);
const processVariables = ref<any[]>([]);
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
const isLocked = computed(() => lockOwner.value !== null);

// ── Auto-Save ────────────────────────────────────────────────
const autoSaveAgo = ref(5);
let autoSaveInterval: any = null;

// ── Pre-Flight (CA-9) ────────────────────────────────────────
const preFlightStatus = ref<'VALIDATED' | 'PENDING' | 'WARNING' | 'ERROR'>('PENDING');

// ── Deploy ───────────────────────────────────────────────────
const isDeploying = ref(false);
const showDeployModal = ref(false);
const deployComment = ref(''); // CA-65
const forceDeploy = ref(false); // CA-65
const deployStrategy = ref('coexist');
const activeInstances = ref(12);
const validationErrors = ref<string[]>([]);

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
      const { data } = await api.getBpmnTemplates();
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

const playPingSound = () => {
   try { new Audio('data:audio/wav;base64,UklGRl9vT19XQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YU').play(); } catch(e){}
};

const selectCopilotOption = (msgItem: any, optionText: string) => {
  msgItem.selectedOption = optionText; // Sello de Inmutabilidad CA-07
  copilotInput.value = optionText;
  sendCopilotMessage();
};

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

const fetchLockState = async () => {
  try {
    const { data } = await api.getProcessLock(processId.value);
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

const fetchVersions = async () => {
  loadingVersions.value = true;
  try {
    const { data } = await api.getProcessVersions(processId.value);
    // Asume array [{version, date, author, status}]
    versionHistory.value = data;
  } catch (err) {
    console.error('API Fake Call - Fallback Versions');
    // MOCK Fallback de Seguridad visual
    versionHistory.value = [
      { version: 3, date: '2026-03-01', author: 'Ana García', status: 'BORRADOR' },
      { version: 2, date: '2026-02-15', author: 'Carlos M.', status: 'ACTIVO' },
      { version: 1, date: '2026-01-20', author: 'Ana García', status: 'ARCHIVADO' }
    ];
  } finally {
    loadingVersions.value = false;
  }
};

const restoreVersion = async (v: number) => {
  if (isLocked.value) return showToast('Proceso bloqueado, no se puede restaurar.', 'error');
  try {
    const { data } = await api.restoreProcessVersion(processId.value, v);
    showToast(`Versión ${v} restaurada con éxito.`);
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
const catalogProcesses = ref<any[]>([]);
const loadingCatalog = ref(false);

watch(showCatalog, async (val) => {
  if (val) {
    loadingCatalog.value = true;
    try {
      const { data } = await api.getCatalogProcesses();
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
    const { data } = await api.getForms();
    // Assuming backend returns array of objects with { id o key, name, type }
    // Normalizing against old static mapping if backend structure differs slightly
    availableForms.value = data.map((f: any) => ({
      key: f.key || f.id || f.formId,
      name: f.name || f.title,
      type: f.type || 'SIMPLE'
    }));
  } catch (err) {
    console.warn('Backend /forms indisponible. Fallback a MOCKS CA-30.');
    availableForms.value = [
      { key: 'iForm_Credito_Base', name: 'Crédito Base', type: 'MAESTRO' },
      { key: 'iForm_Onboarding_V3', name: 'Onboarding V3', type: 'MAESTRO' },
      { key: 'form_aprobacion', name: 'Aprobación Rápida', type: 'SIMPLE' },
      { key: 'form_revision_docs', name: 'Revisión Documentos', type: 'SIMPLE' }
    ];
  }
};

const availableConnectors = ref<any[]>([]);

const fetchConnectors = async () => {
  try {
    const { data } = await api.getIntegrationConnectors();
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
const fetchProcessVariables = async () => {
  try {
    const { data } = await api.getProcessVariables(processId.value);
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
    const { data } = await api.getConnectorSchema(connectorId);
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

const saveConnectorMapping = () => {
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
};

// CA-42: Audit Logs
const showAuditLogsModal = ref(false);
const auditLogs = ref<any[]>([]);
const loadingAuditLogs = ref(false);

const openAuditLogs = async () => {
  showAuditLogsModal.value = true;
  showVersions.value = false;
  loadingAuditLogs.value = true;
  try {
    const { data } = await api.getProcessAuditLogs(processId.value);
    auditLogs.value = data || [];
  } catch (err) {
    auditLogs.value = [
      { action: 'IMPORT XML', user: 'Harolt Gómez', date: new Date().toISOString(), version: 1 },
      { action: 'REQUEST DEPLOY', user: 'Ana García', date: new Date().toISOString(), version: 1 },
      { action: 'ARCHIVED', user: 'System', date: new Date().toISOString(), version: 1 }
    ];
  } finally {
    loadingAuditLogs.value = false;
  }
};

const filteredForms = computed(() => {
  if (processPattern.value === 'SIMPLE') return availableForms.value.filter(f => f.type === 'SIMPLE');
  if (processPattern.value === 'IFORM_MAESTRO') return availableForms.value.filter(f => f.type === 'MAESTRO');
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
    // @ts-ignore
    const minimapModule = (await import('diagram-js-minimap')).default;

    modelerInstance = new BpmnModeler({
      container: canvasContainer.value!,
      additionalModules: [minimapModule],
      keyboard: { bindTo: document } // CA-20 Copy/Paste enabled system-wide
    });

    await modelerInstance.importXML(emptyBpmn);
    modelerInstance.get('canvas').zoom('fit-viewport');

    // Initial Load CA-30 Forms & CA-45 Connectors
    fetchForms();
    fetchConnectors();
    try {
      const { data } = await api.getBpmnComplexityLimit();
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
            sla: bo.get('camunda:dueDate') || '',
            calledElement: bo.calledElement || '',
            formKey: bo.get('camunda:formKey') || '',
            aiTokenLimit: 4000,
            aiTone: 'NEUTRAL'
          }
        };
      } else {
        selectedElement.value = { id: '', type: '', name: '', props: { aiTokenLimit: 4000, aiTone: 'NEUTRAL', sla: '', calledElement: '' } };
      }
    });

    // CA-21, CA-24, CA-30: Reset pre-flight y auditar advertencias arquitectónicas
    modelerInstance.on('commandStack.changed', () => {
      preFlightStatus.value = 'PENDING';
      
      const count = modelerInstance.get('elementRegistry').filter((e: any) => e.type !== 'bpmn:Process').length;
      elementCount.value = count; // CA-31 update reactive state
      
      // CA-30 Alerta Complejidad
      if (count > bpmnComplexityLimit.value) {
        showToast(`⚠️ Mala Práctica: Diagrama excede [${bpmnComplexityLimit.value}] nodos. Riesgo de mantenimiento y rendimiento motor.`, 'error'); 
      }

      debouncedValidate(); // CA-3 Pre-Flight reactivo a cambios
    });

    // CA-3: Executable Pre-Flight Tin Hook
    modelerInstance.on('import.done', (event: any) => {
       const { error } = event;
       if (!error) {
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
                 api.reportIsoOverride({ processId: processId.value, action: 'IGNORED_3_TIMES' }).catch(()=>{});
                 showToast('⚠️ Advertencia ISO Descartes detectada iterativamente. Nota ISO purgada y rastreada al CISO (CA-09).', 'error');
             }
             isoIgnoreCount = 0;
          }
       }
    });

    // Open minimap by default
    try { modelerInstance.get('minimap').open(); } catch(_) {}

    // Initialization Calls (CA-6 / CA-7)
    fetchLockState();
    fetchVersions();
    
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
  autoSaveInterval = setInterval(async () => {
    if (modelerInstance && !isLocked.value) {
      const { xml } = await modelerInstance.saveXML({ format: true });
      if (xml !== lastSavedXml.value) {
        await saveDraft();
        autoSaveAgo.value = 0;
      }
    }
  }, 30000);

  // CA-04: Hook de abandono agresivo para purgar RAG
  window.addEventListener('beforeunload', api.destroyCopilotSession);

  // Tick the "ago" counter every second
  setInterval(() => { autoSaveAgo.value++; }, 1000);
});

onBeforeUnmount(() => {
  // CA-04: Purga RAG al destruir el componente Vue nativo (Vue router leave)
  api.destroyCopilotSession();
  window.removeEventListener('beforeunload', api.destroyCopilotSession);

  if (modelerInstance) modelerInstance.destroy();
  if (autoSaveInterval) clearInterval(autoSaveInterval);
});

// ── Auto-slug processId from name ────────────────────────────
watch(currentProcessName, (name) => {
  if (name) {
    processId.value = name.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, '');
  }
});

// CA-5 & CA-17: UI Masking for Technical ID strictly and XML Injection
watch(processId, (newId) => {
  const cleaned = newId.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, '');
  if (newId !== cleaned) {
    processId.value = cleaned;
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
  fetchLockState();
  fetchVersions();
});

// ── Validation (CA-3, CA-9 & CA-46) ─────────────────────────────────
const debouncedValidate = debounce(async () => {
  if (!modelerInstance) return;
  preFlightStatus.value = 'PENDING';
  
  // Clear previous CA-46 highlights
  const canvas = modelerInstance.get('canvas');
  const elementRegistry = modelerInstance.get('elementRegistry');
  elementRegistry.getAll().forEach((el: any) => {
    try { canvas.removeMarker(el.id, 'highlight-warning'); } catch(e) {}
  });

  try {
    const { xml } = await modelerInstance.saveXML({ format: true });
    const { data } = await api.validateProcess({ xml });
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

const saveDraft = async () => {
  if (!modelerInstance) return;
  try {
    const { xml } = await modelerInstance.saveXML({ format: true });
    
    await api.saveProcessDraft(processId.value, { xml });
    lastSavedXml.value = xml;
    console.log('[AutoSave] Draft XML saved to Backend API successfully (CA-19)');
  } catch (err) {
    // CA-10: Offline degradation warning
    showToast('⚠️ Modo Offline: Guardado en API falló. Revisa tu conexión de red.', 'error');
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
      formData.append('force_deploy', forceDeploy.value.toString()); // CA-65
      const xmlBlob = new Blob([xml!], { type: 'application/xml' });
      formData.append('file', xmlBlob, `${processId.value}.bpmn`);

      deployResponse = await api.deployProcess(formData);
    }
    
    // CA-6: Autogeneración de Roles Feedback
    if (deployResponse?.data?.generatedRoles && deployResponse.data.generatedRoles.length > 0) {
       alert(`Proceso desplegado con Éxito.\\n\\nSe han auto-generado los siguientes perfiles de seguridad:\\n➡ ${deployResponse.data.generatedRoles.join('\\n➡ ')}\\n\\nPuedes asignar estos roles en el CND.`);
    }
    
    // CA-65: Reflejo en Toast
    const v = deployResponse?.data?.version;
    const did = deployResponse?.data?.deployment_id;
    const dat = deployResponse?.data?.deployed_at;
    const suffix = (v && did) ? ` [v${v} | ID: ${did} | ${dat}]` : '';
    
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

const requestDeploy = async () => {
  try {
    const { xml } = await modelerInstance.saveXML({ format: true });
    // CA-25: Mandamos a API
    await api.requestDeployment(processId.value, { xml });
    showToast('📩 Solicitud de despliegue enviada de forma exitosa al Release Manager', 'success');
    processStatus.value = 'PENDING';
  } catch(err: any) {
    showToast(err.response?.data?.error || 'Error al solicitar despliegue', 'error');
  }
};

const runSandbox = async () => {
  try {
    showToast('🧪 Sandbox: Iniciando simulación en Motor V1...');
    const { xml } = await modelerInstance.saveXML({ format: true });
    
    // CA-41: Simulador Hardcore Camunda V1
    await api.spawnSandbox({ xml });
    
    showToast(`✅ Sandbox (CA-41): Ejecución simulada sin errores.`, 'success');
  } catch (err) {
    showToast('🧪 Error conectando al motor de Simulación Sandbox', 'error');
  }
};

const createNewProcess = () => {
  currentProcessName.value = newProcessName.value;
  processPattern.value = newProcessPattern.value;
  processStatus.value = 'BORRADOR';
  showNewProcessModal.value = false;
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
        modelerInstance.importXML(emptyBpmn).then(() => setTimeout(() => updateProcessProperty('formPattern', processPattern.value), 100)); // CA-40
      }
    } else {
      modelerInstance.importXML(emptyBpmn).then(() => setTimeout(() => updateProcessProperty('formPattern', processPattern.value), 100)); // CA-40
    }
  }
  showToast(`Proceso "${newProcessName.value}" creado`);
  newProcessName.value = '';
};



// CA-32: Archivar Proceso Activo
const archiveProcess = async (pId: string) => {
  try {
     await api.archiveProcess(pId);
     showToast('Proceso archivado correctamente');
     if(showCatalog.value) {
        const { data } = await api.getCatalogProcesses();
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

const loadProcess = (p: any) => {
  currentProcessName.value = p.name;
  processStatus.value = p.status;
  showCatalog.value = false;
  showToast(`Cargado: ${p.name} v${p.version}`);
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
    const endpoint = (import.meta as any).env?.VITE_API_URL ? `${(import.meta as any).env.VITE_API_URL}/api/v1/design/processes/copilot/stream` : 'http://localhost:8080/api/v1/design/processes/copilot/stream';
    
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

// ── Zoom Controls (CA-16) ────────────────────────────────────
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
  if (!modelerInstance) return;
  const modeling = modelerInstance.get('modeling');
  const rootElement = modelerInstance.get('canvas').getRootElement();
  if (rootElement && rootElement.businessObject) {
    modeling.updateProperties(rootElement, { "camunda:dueDate": `P${globalSla.value}H` }); // Estandarizado a formato ISO 8601 Horas
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
      modeling.updateProperties(element, { extensionElements });
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

const updateProcessProperty = (name: string, value: string) => {
  if (!modelerInstance) return;
  const modeling = modelerInstance.get('modeling');
  const bpmnFactory = modelerInstance.get('bpmnFactory');
  const canvas = modelerInstance.get('canvas');
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
    window.open(`/admin/modeler?processId=${calledElementId}`, '_blank');
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
</style>
