<template>
  <div class="h-full w-full bg-gray-50 flex flex-col" v-cloak>

    <!-- ═══════ Toast Notifications (CA-7) ═══════ -->
    <Teleport to="body">
      <Transition name="toast-slide">
        <div v-if="toast.msg" :class="toast.type === 'success' ? 'bg-green-600' : 'bg-red-600'" class="fixed top-4 right-4 z-[5000] text-white px-5 py-3 rounded-lg shadow-xl flex items-center space-x-3">
          <span class="text-sm font-medium">{{ toast.msg }}</span>
          <button @click="toast.msg = ''" class="ml-2 opacity-70 hover:opacity-100">&times;</button>
        </div>
      </Transition>
    </Teleport>

    <!-- ═══════ Header Toolbar (UX Refactor Tarea 1) ═══════ -->
    <header class="flex justify-between items-center px-6 py-3 bg-white border-b border-gray-200 shrink-0">
      <div class="flex items-center space-x-4">
        <div>
          <h1 class="text-xl font-bold text-gray-900 flex items-center gap-2">
            IDE de Formularios Vue3/Zod
            <span class="text-xs font-bold text-white px-2 py-0.5 rounded-full" :class="formPattern === 'IFORM_MAESTRO' ? 'bg-blue-600' : 'bg-green-600'">
              {{ formPattern === 'IFORM_MAESTRO' ? '🔵 iForm Maestro' : '🟢 Simple' }}
            </span>
            <!-- CA-12: Badge de revocación QA -->
            <span v-if="certificationState === 'revoked'"
                  class="text-xs bg-amber-100 text-amber-800 border border-amber-300 px-2 py-0.5 rounded shadow-sm font-bold ml-2">
              ⚠️ Certificación QA revocada — Modificación detectada
            </span>
            <span v-else-if="certificationState === 'certified'"
                  class="text-xs bg-green-100 text-green-800 border border-green-300 px-2 py-0.5 rounded shadow-sm font-bold ml-2">
              ✅ Certificado QA
            </span>
            <!-- Zona 1: Visores -->
            <button @click="isFullScreen = !isFullScreen" class="text-gray-400 hover:text-indigo-600 transition ml-2 focus:outline-none" :title="isFullScreen ? 'Salir Inmersión' : 'Pantalla Completa (Inmersivo)'">
              🖵
            </button>
            <button @click="isPrintMode = !isPrintMode" class="text-gray-400 hover:text-indigo-600 transition focus:outline-none" :class="{ 'text-blue-600': isPrintMode }" title="Vista de Lectura Estática (Print Mode)">
              👁️
            </button>
          </h1>
          <p class="text-xs text-gray-500 mt-0.5">Editor bidireccional Vue3 Composition API + Validaciones Zod (US-003)</p>
        </div>
      </div>
      
      <div class="flex items-center gap-4">
        <!-- CA-15.1: Permitir Trámite Público -->
        <div class="flex items-center gap-2 bg-emerald-50 px-3 py-1.5 rounded border border-emerald-200" title="Permite recolectar datos sin autenticación previa (Bypass CA-15)">
           <label for="publicToggle" class="text-xs font-bold text-emerald-800 cursor-pointer">🌐 Trámite Público</label>
           <input type="checkbox" id="publicToggle" v-model="isPublic" class="text-emerald-600 rounded focus:ring-emerald-500 w-4 h-4 cursor-pointer" />
        </div>
        
        <!-- URL Banner si es público -->
        <div v-if="isPublic" class="flex items-center gap-2 bg-gray-100 px-3 py-1.5 rounded border border-gray-300">
           <span class="text-[10px] font-mono text-gray-600 select-all truncate max-w-[200px]" title="Doble clic para seleccionar todo">{{ publicUrl }}</span>
           <button @click="copyPublicUrl" class="text-gray-500 hover:text-indigo-600 flex items-center justify-center p-0.5 bg-white border rounded shadow-sm" title="Copiar Link"><span class="text-xs">📋</span></button>
        </div>

        <div class="h-6 w-px bg-gray-300 mx-1"></div>

        <!-- Zona 2: Dropdown DevTools -->
        <div class="relative group">
          <button class="bg-gray-100 text-gray-700 px-3 py-1.5 border border-gray-300 rounded shadow-sm text-xs font-semibold hover:bg-gray-200 transition flex gap-1.5 items-center">
            🛠️ Herramientas Avanzadas ▼
          </button>
          <div class="absolute right-0 mt-2 w-48 bg-white border border-gray-200 rounded shadow-xl hidden group-hover:block z-50 overflow-hidden">
            <button @click="fetchVersions" class="block w-full text-left px-4 py-2 hover:bg-gray-50 text-xs text-gray-700 transition">🕰️ Historial JSON</button>
            <button @click="exportToPdf" class="block w-full text-left px-4 py-2 hover:bg-gray-50 text-xs text-gray-700 transition">📄 Exportar a PDF</button>
            <button @click="showGlobalRulesModal = true" class="block w-full text-left px-4 py-2 hover:bg-gray-50 text-xs text-gray-700 transition">⚙️ Reglas Zod O-T-F</button>
            <button @click="generateVitestSpec" class="block w-full text-left px-4 py-2 hover:bg-green-50 text-xs text-green-700 font-bold transition border-t border-gray-100">🤖 Exportar Robo-Tests</button>
            <div class="border-t border-gray-100"></div>
            <button @click="openFuzzerSandbox" class="block w-full text-left px-4 py-2 bg-yellow-50 hover:bg-yellow-100 text-xs text-yellow-800 font-bold transition">⚡ QA Sandbox Fuzzer (RAM)</button>
          </div>
        </div>

        <!-- GAP 9: Simulador RBAC (Mimetismo) -->
        <div class="relative group ml-1">
          <button class="bg-blue-100 text-blue-700 border border-blue-300 px-3 py-1.5 rounded shadow-sm text-xs font-bold hover:bg-blue-200 transition flex gap-1 items-center">
            👁️ Simular Mimetismo ▼
          </button>
          <div class="absolute right-0 mt-2 w-40 bg-white border border-gray-200 rounded shadow-xl hidden group-hover:block z-50 overflow-hidden text-xs">
            <div class="px-3 py-2 bg-gray-50 border-b border-gray-100 font-bold text-gray-500">Rol Activo:</div>
            <button @click="simulatorContext.rbacRole = 'ADMIN'" :class="{'bg-blue-50 font-bold': simulatorContext.rbacRole === 'ADMIN'}" class="block w-full text-left px-4 py-2 hover:bg-gray-50 text-gray-700 transition">🛡️ ADMIN</button>
            <button @click="simulatorContext.rbacRole = 'OPERATOR'" :class="{'bg-blue-50 font-bold': simulatorContext.rbacRole === 'OPERATOR'}" class="block w-full text-left px-4 py-2 hover:bg-gray-50 text-gray-700 transition">👨‍💻 OPERATOR</button>
            <button @click="simulatorContext.rbacRole = 'MANAGER'" :class="{'bg-blue-50 font-bold': simulatorContext.rbacRole === 'MANAGER'}" class="block w-full text-left px-4 py-2 hover:bg-gray-50 text-gray-700 transition">👔 MANAGER</button>
            <button @click="simulatorContext.rbacRole = 'GUEST'" :class="{'bg-blue-50 font-bold': simulatorContext.rbacRole === 'GUEST'}" class="block w-full text-left px-4 py-2 hover:bg-gray-50 text-gray-700 transition">🏃 GUEST</button>
          </div>
        </div>

        <!-- Zona 4: Acciones Críticas -->
        <button @click="showAiModal = true" class="bg-purple-100 text-purple-700 border border-purple-300 px-3 py-1.5 rounded shadow-sm text-xs font-bold hover:bg-purple-200 transition flex gap-1 items-center ml-2">
          ✨ Escáner Mágico IA
        </button>
        <button @click="openPreview" class="bg-emerald-100 text-emerald-800 border border-emerald-300 px-3 py-1.5 rounded shadow-sm text-xs font-bold hover:bg-emerald-200 transition flex gap-1 items-center ml-2">
          👁️ Previsualizar UI
        </button>
        <button @click="confirmReset" class="bg-white text-red-600 px-3 py-1.5 border border-red-200 rounded shadow-sm text-xs font-semibold hover:bg-red-50 transition flex gap-1.5 items-center ml-2 outline outline-offset-1 outline-transparent hover:outline-red-200">
          🗑 Reset
        </button>

        <button @click="saveForm" class="bg-blue-600 text-white px-4 py-1.5 rounded shadow text-xs font-semibold hover:bg-blue-700 transition flex items-center gap-2">💾 Guardar Versión</button>
        <button @click="simulateMockSubmit" class="bg-indigo-600 text-white px-4 py-1.5 rounded shadow text-xs font-semibold hover:bg-indigo-700 transition flex items-center gap-2">
          🚀 Probar (Submit)
        </button>
      </div>
    </header>

    <!-- ═══════ Main Layout ═══════ -->
    <main class="flex-1 flex min-h-0 relative">
      
      <!-- Toolbox Izquierda (Componentes Lego) -->
      <aside v-show="!isFullScreen" class="w-64 bg-white border-r border-gray-200 flex flex-col shrink-0 transition-all">
        <div class="p-3 border-b border-gray-100 bg-gray-50">
          <h3 class="text-[11px] font-bold text-gray-400 uppercase tracking-widest flex items-center gap-2">🧩 Componentes</h3>
        </div>
        
        <div class="flex-1 overflow-y-auto p-3 space-y-6">
          <div v-for="category in toolboxCategories" :key="category.name">
            <h4 class="text-xs font-bold text-gray-800 mb-3">{{ category.name }}</h4>
            <VueDraggable
              :list="category.items"
              :group="{ name: 'form-builder', pull: 'clone', put: false }"
              :clone="cloneComponent"
              item-key="type"
              class="space-y-2"
            >
              <template #item="{ element }">
                <div class="text-xs p-2.5 border border-gray-200 rounded-md bg-white hover:border-indigo-400 hover:shadow-sm cursor-grab flex items-center gap-2 transition">
                  <span class="text-lg w-6 text-center">{{ element.icon }}</span>
                  <div class="flex flex-col">
                    <span class="font-semibold text-gray-700">{{ element.label }}</span>
                    <span class="text-[9px] text-gray-400">{{ element.desc }}</span>
                  </div>
                </div>
              </template>
            </VueDraggable>
          </div>
        </div>
      </aside>

      <!-- Lienzo Central (Canvas Drag & Drop) -->
      <section class="flex-1 bg-gray-50/50 flex flex-col relative">
        <!-- Barra de Simulación del Stage (Solo para Maestro) -->
        <div v-if="formPattern === 'IFORM_MAESTRO'" class="absolute top-4 left-1/2 -translate-x-1/2 bg-blue-50 border border-blue-200 text-blue-800 px-4 py-2 rounded-full shadow-sm text-xs font-bold flex items-center gap-3 z-10">
          <span>Simulation Stage:</span>
          <select v-model="activeStageSim" class="bg-white border-blue-300 rounded text-xs py-0.5 focus:ring-blue-500 font-mono">
            <option value="START_EVENT">START_EVENT</option>
            <option value="ANALYSIS">ANALYSIS</option>
            <option value="DECISION">DECISION</option>
            <option value="ALL">Mostrar Todos (Ideation)</option>
          </select>
        </div>

        <div class="flex-1 overflow-y-auto p-6 md:p-8 lg:p-12">
          <!-- CA-6 Shadow DOM (Isolation real via attachShadow & Teleport) -->
          <div ref="designerHostRef" class="w-full min-h-full"></div>
          <Teleport v-if="designerShadowContainer" :to="designerShadowContainer">
            <div class="shadow-dom-isolation-wrapper bg-white rounded-xl shadow-sm border border-gray-200 min-h-full p-8 max-w-4xl mx-auto flex flex-col relative" style="all: revert; box-sizing: border-box;">
              <h2 class="text-xl font-bold text-gray-800 mb-6 border-b pb-4 font-sans">{{ formTitle }}</h2>

            <div v-if="isHighDensityForm" class="mb-4 p-4 bg-yellow-50 border-l-4 border-yellow-400 text-yellow-800 shadow-sm rounded flex items-center gap-3">
               <span class="text-2xl">⚠️</span>
               <div>
                  <h4 class="text-sm font-bold">Modo de Alta Densidad Activado (CA-90)</h4>
                  <p class="text-xs mt-0.5">La profundidad del esquema supera los 200 campos. El Workdesk usará Lazy Mount para no bloquear el hilo principal.</p>
               </div>
            </div>
            
            <VueDraggable
              v-model="canvasFields"
              :group="{ name: 'form-builder', pull: true, put: true }"
              item-key="id"
              class="flex-1 min-h-[300px] font-sans"
              animation="200"
              ghost-class="ghost-dropzone"
            >
              <template #item="{ element, index }">
                <div 
                  v-show="(formPattern !== 'IFORM_MAESTRO' || activeStageSim === 'ALL' || element.stage === activeStageSim) && evaluateMockVis(element)"
                  class="group relative border border-transparent hover:border-indigo-300 hover:bg-indigo-50/30 p-4 rounded-lg mb-4 transition"
                >
                  
                  <!-- Controles del Campo (Hover) -->
                  <div class="absolute -top-3 right-2 hidden group-hover:flex bg-white border border-gray-200 shadow-sm rounded-md overflow-hidden text-xs z-20">
                    <button @click="editField(element)" class="px-2 py-1 text-gray-600 hover:bg-gray-100 border-r border-gray-200" title="Propiedades">⚙️</button>
                    <button @click="saveAsFragment(element)" class="px-2 py-1 text-blue-600 hover:bg-blue-50 border-r border-gray-200" title="Guardar como Fragmento">💾</button>
                    <button @click="removeField(canvasFields, index)" class="px-2 py-1 text-red-500 hover:bg-red-50" title="Eliminar">🗑️</button>
                  </div>

                  <!-- Badge de Stage actual (Solo Maestro) -->
                  <div v-if="formPattern === 'IFORM_MAESTRO' && activeStageSim === 'ALL'" class="absolute -top-2 left-4 bg-blue-100 text-blue-800 text-[9px] font-bold px-1.5 py-0.5 rounded shadow-sm font-mono border border-blue-200">
                    v-if stage == '{{ element.stage }}'
                  </div>

                  <!-- Renderizado Dinámico CA-8 (Soporte Nested Container) -->
                  <div class="flex flex-col gap-1 mt-1">
                    <label class="text-sm font-bold text-gray-700">{{ element.label }} <span v-if="element.required" class="text-red-500">*</span></label>
                    <p v-if="element.desc" class="text-[10px] text-gray-400 mb-1">{{ element.desc }}</p>
                    
                    <input v-if="element.type === 'text'" :placeholder="element.placeholder" class="form-input text-sm w-full rounded-md border-gray-300 focus:border-indigo-500 focus:ring-indigo-500 shadow-sm disabled:bg-gray-100 mt-1" />
                    <textarea v-if="element.type === 'textarea'" :placeholder="element.placeholder" class="form-input text-sm rounded-md border-gray-300 focus:border-indigo-500 focus:ring-indigo-500 shadow-sm w-full mt-1 disabled:bg-gray-100" rows="2"></textarea>
                    <input v-if="element.type === 'number'" type="number" :placeholder="element.placeholder" class="form-input text-sm w-full rounded-md border-gray-300 focus:border-indigo-500 focus:ring-indigo-500 shadow-sm mt-1" />
                    <input v-if="element.type === 'date'" type="date" class="form-input text-sm rounded-md border-gray-300 focus:border-indigo-500 focus:ring-indigo-500 shadow-sm w-full mt-1" disabled />
                    <input v-if="element.type === 'time'" type="time" class="form-input text-sm rounded-md border-gray-300 focus:border-indigo-500 focus:ring-indigo-500 shadow-sm w-full mt-1" disabled />
                    
                    <select v-if="element.type === 'select'" class="form-select text-sm rounded-md border-gray-300 focus:border-indigo-500 focus:ring-indigo-500 shadow-sm w-full mt-1">
                      <option disabled selected>{{ element.placeholder }}</option>
                      <option v-for="opt in element.options || ['Opción 1']" :key="opt">{{ opt }}</option>
                    </select>
                    
                    <div v-if="element.type === 'checkbox'" class="flex items-center gap-2 mt-1 px-2">
                       <input type="checkbox" class="rounded text-indigo-600 focus:ring-indigo-500 border-gray-300" disabled />
                       <span class="text-sm text-gray-700">{{ element.placeholder || element.label }}</span>
                    </div>
                    <div v-if="element.type === 'radio'" class="flex flex-col gap-1 mt-1 px-2">
                       <label v-for="opt in element.options || ['Opción 1', 'Opción 2']" :key="opt" class="flex items-center gap-2">
                          <input type="radio" class="text-indigo-600 border-gray-300 focus:ring-indigo-500" disabled />
                          <span class="text-sm font-medium text-gray-600">{{ opt }}</span>
                       </label>
                    </div>

                    <div v-if="element.type === 'file'" class="border-2 border-dashed border-gray-300 rounded p-4 text-center text-xs text-gray-500 hover:bg-gray-50 cursor-pointer bg-white mt-1">
                      📂 {{ element.placeholder }} (Drag & Drop SGDEA)
                    </div>

                    <!-- CA-14 Smart Buttons -->
                    <button v-if="element.type === 'button_draft'" class="w-full px-4 py-2 border-2 border-dashed border-gray-300 text-gray-600 font-bold rounded-lg mt-3 cursor-pointer bg-gray-50 uppercase text-xs">💾 {{ element.label }}</button>
                    <button v-if="element.type === 'button_submit'" class="w-full px-4 py-2 bg-indigo-600 text-white font-bold rounded-lg mt-3 cursor-pointer shadow-md">✅ {{ element.label }}</button>
                    <button v-if="element.type === 'button_reject'" class="w-full px-4 py-2 bg-red-600 text-white font-bold rounded-lg mt-3 cursor-pointer shadow-md">❌ {{ element.label }}</button>

                    
                    <div v-if="element.type === 'container'" class="border border-indigo-200 bg-indigo-50/50 rounded-lg p-4 mt-2 min-h-[120px]">
                      <VueDraggable
                         v-model="element.children"
                         :group="{ name: 'form-builder', pull: true, put: true }"
                         item-key="id"
                         class="min-h-[120px] transition-all"
                         :class="{'border-2 border-dashed border-gray-300 bg-gray-50 flex flex-col items-center justify-center': !element.children || element.children.length === 0}"
                         animation="200"
                         ghost-class="ghost-dropzone"
                      >
                         <template #item="{ element: child, index: childIdx }">
                            <div v-show="evaluateMockVis(child)" class="group/child relative bg-white border border-gray-200 p-3 rounded mb-2 hover:border-indigo-300 shadow-sm transition">
                               <div class="absolute -top-3 right-2 hidden group-hover/child:flex bg-white border border-gray-200 shadow-sm rounded-md overflow-hidden text-xs z-20">
                                 <button @click="editField(child)" class="px-2 py-1 text-gray-600 hover:bg-gray-100 border-r border-gray-200">⚙️</button>
                                 <button @click="saveAsFragment(child)" class="px-2 py-1 text-blue-600 hover:bg-blue-50 border-r border-gray-200">💾</button>
                                 <button @click="removeField(element.children, childIdx)" class="px-2 py-1 text-red-500 hover:bg-red-50">🗑️</button>
                               </div>
                               <label class="text-xs font-bold text-gray-700 block">{{ child.label }} <span v-if="child.required" class="text-red-500">*</span></label>
                               <input v-if="child.type === 'text'" :placeholder="child.placeholder" class="form-input text-xs w-full mt-1 border-gray-300 rounded shadow-sm" />
                               <textarea v-if="child.type === 'textarea'" :placeholder="child.placeholder" class="form-input text-xs w-full mt-1 border-gray-300 rounded shadow-sm" rows="1"></textarea>
                               <input v-if="child.type === 'number'" type="number" :placeholder="child.placeholder" class="form-input text-xs w-full mt-1 border-gray-300 rounded shadow-sm" />
                               <input v-if="child.type === 'date'" type="date" class="form-input text-xs w-full mt-1 border-gray-300 rounded shadow-sm" disabled />
                               <input v-if="child.type === 'time'" type="time" class="form-input text-xs w-full mt-1 border-gray-300 rounded shadow-sm" disabled />
                               <select v-if="child.type === 'select'" class="form-select text-xs w-full mt-1 border-gray-300 rounded shadow-sm">
                                 <option disabled selected>{{ child.placeholder }}</option>
                               </select>
                               <div v-if="child.type === 'checkbox'" class="flex items-center gap-1 mt-1">
                                  <input type="checkbox" class="rounded text-indigo-600 border-gray-300" disabled />
                                  <span class="text-[10px] text-gray-700">{{ child.placeholder || child.label }}</span>
                               </div>
                               <button v-if="child.type === 'button_submit'" class="w-full px-2 py-1 bg-indigo-600 text-white font-bold rounded mt-2 text-[10px]">✅ {{ child.label }}</button>
                            </div>
                         </template>
                         <template #footer>
                            <div v-if="!element.children || element.children.length === 0" class="text-gray-400 font-bold text-xs pointer-events-none mt-2">Arrastre componentes aquí para este contenedor</div>
                         </template>
                      </VueDraggable>
                    </div>

                    <!-- CA-08 Tabs -->
                    <div v-if="element.type === 'tabs'" class="border border-gray-300 bg-white shadow-sm rounded-lg mt-2 overflow-hidden">
                       <div class="flex border-b border-gray-200 bg-gray-50 pt-2 px-2 gap-1 overflow-x-auto">
                          <button v-for="(pane, paneIdx) in element.children" :key="paneIdx"
                                  @click.prevent="element.activeTab = paneIdx"
                                  :class="element.activeTab === paneIdx ? 'border-b-2 border-indigo-500 text-indigo-700 bg-white font-bold pb-2' : 'border-b-2 border-transparent text-gray-500 hover:bg-gray-100 font-medium pb-2'"
                                  class="px-4 pt-2 text-xs transition rounded-t-lg focus:outline-none whitespace-nowrap">
                             {{ pane.label || 'Tab ' + (Number(paneIdx) + 1) }}
                          </button>
                       </div>
                       <div class="p-4 bg-white min-h-[120px]">
                          <div v-for="(pane, paneIdx) in element.children" :key="'tp'+paneIdx" v-show="element.activeTab === paneIdx">
                             <VueDraggable v-model="pane.children" :group="{ name: 'form-builder', pull: true, put: true }" item-key="id" class="min-h-[120px] transition-all" :class="{'border-2 border-dashed border-gray-300 bg-gray-50 flex flex-col items-center justify-center': !pane.children || pane.children.length === 0}" animation="200" ghost-class="ghost-dropzone">
                                <template #item="{ element: child, index: childIdx }">
                                   <!-- Sub-nivel Visual -->
                                   <div v-show="evaluateMockVis(child)" class="group/child relative bg-white border border-gray-200 p-3 rounded mb-2 hover:border-indigo-300 shadow-sm transition">
                                      <div class="absolute -top-3 right-2 hidden group-hover/child:flex bg-white border border-gray-200 shadow-sm rounded-md overflow-hidden text-xs z-20">
                                        <button @click="editField(child)" class="px-2 py-1 text-gray-600 hover:bg-gray-100 border-r border-gray-200">⚙️</button>
                                        <button @click="saveAsFragment(child)" class="px-2 py-1 text-blue-600 hover:bg-blue-50 border-r border-gray-200">💾</button>
                                        <button @click="removeField(pane.children, childIdx)" class="px-2 py-1 text-red-500 hover:bg-red-50">🗑️</button>
                                      </div>
                                      <label class="text-xs font-bold text-gray-700 block">{{ child.label }} <span v-if="child.required" class="text-red-500">*</span></label>
                                      <input v-if="child.type === 'text'" :placeholder="child.placeholder" class="form-input text-xs w-full mt-1 border-gray-300 rounded shadow-sm" />
                                      <textarea v-if="child.type === 'textarea'" :placeholder="child.placeholder" class="form-input text-xs w-full mt-1 border-gray-300 rounded shadow-sm" rows="1"></textarea>
                                      <input v-if="child.type === 'number'" type="number" :placeholder="child.placeholder" class="form-input text-xs w-full mt-1 border-gray-300 rounded shadow-sm" />
                                      <input v-if="child.type === 'date'" type="date" class="form-input text-xs w-full mt-1 border-gray-300 rounded shadow-sm" disabled />
                                      <input v-if="child.type === 'time'" type="time" class="form-input text-xs w-full mt-1 border-gray-300 rounded shadow-sm" disabled />
                                      <select v-if="child.type === 'select'" class="form-select text-xs w-full mt-1 border-gray-300 rounded shadow-sm">
                                        <option disabled selected>{{ child.placeholder }}</option>
                                      </select>
                                      <div v-if="child.type === 'checkbox'" class="flex items-center gap-1 mt-1">
                                         <input type="checkbox" class="rounded text-indigo-600 border-gray-300" disabled />
                                         <span class="text-[10px] text-gray-700">{{ child.placeholder || child.label }}</span>
                                      </div>
                                      <button v-if="child.type === 'button_submit'" class="w-full px-2 py-1 bg-indigo-600 text-white font-bold rounded mt-2 text-[10px]">✅ {{ child.label }}</button>
                                   </div>
                                 </template>
                                 <template #footer>
                                    <div v-if="!pane.children || pane.children.length === 0" class="text-gray-400 font-bold text-xs pointer-events-none mt-2">Arrastre componentes aquí para esta pestaña</div>
                                 </template>
                             </VueDraggable>
                          </div>
                       </div>
                    </div>

                    <!-- CA-08 Accordion -->
                    <div v-if="element.type === 'accordion'" class="mt-2 space-y-2">
                       <details v-for="(panel, pIdx) in element.children" :key="'ap'+pIdx" class="border border-gray-300 bg-white rounded-lg shadow-sm group">
                          <summary class="px-4 py-3 text-xs font-bold text-gray-700 cursor-pointer bg-gray-50 hover:bg-gray-100 transition list-none flex justify-between items-center rounded-lg group-open:rounded-b-none border-b border-transparent group-open:border-gray-200">
                             {{ panel.label || 'Panel ' + (Number(pIdx) + 1) }}
                             <span class="text-gray-400 group-open:rotate-180 transition-transform font-mono text-[10px]">▼</span>
                          </summary>
                          <div class="p-4 bg-white rounded-b-lg min-h-[120px]">
                            <VueDraggable v-model="panel.children" :group="{ name: 'form-builder', pull: true, put: true }" item-key="id" class="min-h-[120px] transition-all" :class="{'border-2 border-dashed border-gray-300 bg-gray-50 flex flex-col items-center justify-center': !panel.children || panel.children.length === 0}" animation="200" ghost-class="ghost-dropzone">
                               <template #item="{ element: child, index: childIdx }">
                                  <!-- Sub-nivel Visual -->
                                  <div v-show="evaluateMockVis(child)" class="group/child relative bg-white border border-gray-200 p-3 rounded mb-2 hover:border-indigo-300 shadow-sm transition">
                                     <div class="absolute -top-3 right-2 hidden group-hover/child:flex bg-white border border-gray-200 shadow-sm rounded-md overflow-hidden text-xs z-20">
                                       <button @click="editField(child)" class="px-2 py-1 text-gray-600 hover:bg-gray-100 border-r border-gray-200">⚙️</button>
                                       <button @click="saveAsFragment(child)" class="px-2 py-1 text-blue-600 hover:bg-blue-50 border-r border-gray-200">💾</button>
                                       <button @click="removeField(panel.children, childIdx)" class="px-2 py-1 text-red-500 hover:bg-red-50">🗑️</button>
                                     </div>
                                     <label class="text-xs font-bold text-gray-700 block">{{ child.label }} <span v-if="child.required" class="text-red-500">*</span></label>
                                     <input v-if="child.type === 'text'" :placeholder="child.placeholder" class="form-input text-xs w-full mt-1 border-gray-300 rounded shadow-sm" />
                                     <textarea v-if="child.type === 'textarea'" :placeholder="child.placeholder" class="form-input text-xs w-full mt-1 border-gray-300 rounded shadow-sm" rows="1"></textarea>
                                     <input v-if="child.type === 'number'" type="number" :placeholder="child.placeholder" class="form-input text-xs w-full mt-1 border-gray-300 rounded shadow-sm" />
                                     <input v-if="child.type === 'date'" type="date" class="form-input text-xs w-full mt-1 border-gray-300 rounded shadow-sm" disabled />
                                     <input v-if="child.type === 'time'" type="time" class="form-input text-xs w-full mt-1 border-gray-300 rounded shadow-sm" disabled />
                                     <select v-if="child.type === 'select'" class="form-select text-xs w-full mt-1 border-gray-300 rounded shadow-sm">
                                        <option disabled selected>{{ child.placeholder }}</option>
                                     </select>
                                     <div v-if="child.type === 'checkbox'" class="flex items-center gap-1 mt-1">
                                        <input type="checkbox" class="rounded text-indigo-600 border-gray-300" disabled />
                                        <span class="text-[10px] text-gray-700">{{ child.placeholder || child.label }}</span>
                                     </div>
                                     <button v-if="child.type === 'button_submit'" class="w-full px-2 py-1 bg-indigo-600 text-white font-bold rounded mt-2 text-[10px]">✅ {{ child.label }}</button>
                                  </div>
                               </template>
                            </VueDraggable>
                          </div>
                       </details>
                    </div>

                  </div>

                </div>
              </template>
              
              <template #footer>
                 <div v-if="canvasFields.length === 0" class="h-full w-full flex flex-col items-center justify-center text-gray-400 border-2 border-dashed border-gray-200 rounded-lg bg-gray-50 p-12 mt-4 hover:border-indigo-300 transition cursor-default">
                   <span class="text-4xl mb-4">📥</span>
                   <p class="font-medium text-gray-500">Arrastra componentes aquí</p>
                   <p class="text-xs mt-2 text-gray-400 text-center max-w-xs">El código Vue.js se generará e inyectará en tiempo real en el IDE lateral.</p>
                 </div>
              </template>
            </VueDraggable>
          </div>
          </Teleport>
        </div>
      </section>

      <!-- Monaco IDE (Bidireccional V2) -->
      <aside v-show="!isFullScreen" class="w-2/5 min-w-[350px] bg-[#1e1e1e] border-l border-gray-800 flex flex-col shadow-[-4px_0_15px_-3px_rgba(0,0,0,0.1)] z-20 shrink-0 transition-all">
        
        <!-- Tabs -->
        <div class="flex bg-[#252526] text-xs font-mono font-medium text-gray-400 border-b border-[#3e3e42] shrink-0 overflow-x-auto">
          <button @click="attemptTabChange('JSON')" :class="{ 'bg-[#1e1e1e] text-white border-t-2 border-yellow-500': activeCodeTab === 'JSON' }" class="px-4 py-2 hover:bg-[#2d2d2d] transition flex items-center gap-2">
             <span class="text-yellow-400 font-bold">{ }</span> json
          </button>
          <button @click="attemptTabChange('TEMPLATE')" :class="{ 'bg-[#1e1e1e] text-white border-t-2 border-emerald-500': activeCodeTab === 'TEMPLATE' }" class="px-4 py-2 hover:bg-[#2d2d2d] transition flex items-center gap-2">
            <span class="text-emerald-400">&lt;&gt;</span> template
          </button>
          <button @click="attemptTabChange('SCRIPT')" :class="{ 'bg-[#1e1e1e] text-white border-t-2 border-blue-500': activeCodeTab === 'SCRIPT' }" class="px-4 py-2 hover:bg-[#2d2d2d] transition flex items-center gap-2 whitespace-nowrap">
            <span class="text-blue-400">&lt;script setup&gt;</span>
            <AppTooltip content="Código Vue.js autogenerado con Composition API (Solo Lectura)." />
          </button>
          <button @click="attemptTabChange('STYLE')" :class="{ 'bg-[#1e1e1e] text-white border-t-2 border-pink-500': activeCodeTab === 'STYLE' }" class="px-4 py-2 hover:bg-[#2d2d2d] transition flex items-center gap-2 whitespace-nowrap">
            <span class="text-pink-400">&lt;style scoped&gt;</span>
            <AppTooltip content="Estilizado CSS inyectado para Tailwind y clases utilitarias (Solo Lectura)." />
          </button>
          <button @click="attemptTabChange('ZOD')" :class="{ 'bg-[#1e1e1e] text-white border-t-2 border-indigo-500': activeCodeTab === 'ZOD' }" class="px-4 py-2 hover:bg-[#2d2d2d] transition flex items-center gap-2">
             <span class="text-indigo-400 font-bold">Z</span> zod
          </button>
          <div class="ml-auto px-4 flex items-center group relative cursor-help">
            <span class="text-gray-500 text-sm">❕</span>
            <div class="absolute right-0 top-full mt-2 w-56 p-2 bg-gray-800 text-xs text-gray-300 rounded shadow-xl hidden group-hover:block z-50 whitespace-normal">
              Bidireccionalidad Activa (CA-2): Editar Zod o Template actualizará el lienzo visual con un AST Parser Seguro. Sin eval() (CA-4).
            </div>
          </div>
        </div>

        <!-- Monaco Editor Container -->
        <div class="flex-1 relative" :class="{'border-4 border-red-500 rounded-lg shadow-inner': zodParseError}">
           <VueMonacoEditor 
             v-model:value="computedCode"
             :language="editorLanguage"
             theme="vs-dark"
             :options="monacoOptions"
             @mount="onMonacoMount"
             class="absolute inset-0"
           />
        </div>
      </aside>

    </main>

    <!-- ═══════ Modals (CA-7 Teleport) ═══════ -->
    <Teleport to="body">
      <!-- Pattern Selection Modal (On Mount if Empty) -->
      <div v-if="showPatternModal" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[900] p-4 backdrop-blur-sm">
        <div class="bg-white rounded-xl shadow-2xl p-6 md:p-8 max-w-2xl w-full">
          <h2 class="text-2xl font-bold text-gray-900 mb-2">Crear Nuevo Formulario (Dual-Pattern)</h2>
          <p class="text-sm text-gray-600 mb-8">Selecciona la arquitectura de este formulario según la directriz (CA-1).</p>
          
          <div class="grid md:grid-cols-2 gap-6">
            <button @click="selectPattern('SIMPLE')" class="text-left border-2 border-gray-200 hover:border-green-500 hover:bg-green-50/30 rounded-xl p-6 transition group">
              <div class="text-4xl mb-4 group-hover:scale-110 transition-transform">🟢</div>
              <h3 class="text-lg font-bold text-green-700 mb-2">Formulario Simple</h3>
              <p class="text-xs text-gray-500 leading-relaxed">Formulario estándar de una sola vista. Ideal para tareas aisladas sin ciclo de vida complejo en Camunda.</p>
            </button>
            <button @click="selectPattern('IFORM_MAESTRO')" class="text-left border-2 border-gray-200 hover:border-blue-500 hover:bg-blue-50/30 rounded-xl p-6 transition group">
              <div class="text-4xl mb-4 group-hover:scale-110 transition-transform">🔵</div>
              <h3 class="text-lg font-bold text-blue-700 mb-2">iForm Maestro</h3>
              <p class="text-xs text-gray-500 leading-relaxed">Formulario universal mutante. Viajará por todo el proceso BPMN revelando u ocultando componentes dinámicamente según la variable <code class="bg-gray-100 px-1 rounded">stage</code>.</p>
            </button>
          </div>
        </div>
      </div>

      <!-- CA-27: Historial de Versiones UI -->
      <div v-if="showHistoryModal" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[900] p-4 backdrop-blur-sm">
         <div class="bg-white rounded-xl shadow-2xl p-6 md:p-8 max-w-lg w-full">
            <div class="flex items-center justify-between mb-6 border-b pb-4">
               <h2 class="text-xl font-bold text-gray-800">🕰️ Historial de Versiones</h2>
               <button @click="showHistoryModal = false" class="text-gray-400 hover:text-gray-600 text-xl font-bold">&times;</button>
            </div>
            <div class="max-h-[60vh] overflow-y-auto space-y-3">
               <div v-if="formVersions.length === 0" class="text-center text-gray-500 py-8 text-sm">No hay versiones publicadas aún.</div>
               <div v-for="ver in formVersions" :key="ver.id" class="border border-gray-200 rounded-lg p-4 hover:bg-gray-50 transition cursor-pointer flex justify-between items-center group">
                  <div>
                    <h4 class="font-bold text-indigo-700 text-sm flex items-center gap-2">Versión {{ ver.version }}</h4>
                    <p class="text-[10px] text-gray-500 mt-1">Ref: {{ ver.id }}</p>
                    <p class="text-xs text-gray-600 mt-1"><span class="font-semibold">Actualizado:</span> {{ new Date(ver.updatedAt).toLocaleString() }}</p>
                  </div>
                  <button @click="restoreVersion(ver)" class="bg-indigo-100 text-indigo-800 text-xs px-3 py-1.5 rounded-md font-bold opacity-0 group-hover:opacity-100 transition shadow-sm">Restaurar</button>
               </div>
            </div>
         </div>
      </div>

      <!-- Properties Modal (Field Editor) -->
      <div v-if="editingField" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[900] p-4">
        <div class="bg-white rounded-lg shadow-2xl p-6 w-full max-w-md max-h-[90vh] overflow-y-auto">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-lg font-bold text-gray-800 flex items-center gap-2">🛠️ Propiedades del Componente</h3>
            <button @click="editingField = null" class="text-gray-400 hover:text-gray-600">&times;</button>
          </div>
          
          <div class="space-y-4">
            <div>
              <label class="block text-xs font-bold text-gray-700 mb-1">Identificador Interno (Sin espacios, ej: nit_empresa)</label>
              <input v-model="editingField.id" @focus="oldIdTemp = editingField.id" @blur="handleIdChange(editingField)" class="w-full text-sm border-gray-300 rounded font-mono bg-gray-50 uppercase" />
            </div>
            <div>
              <label class="block text-xs font-bold text-gray-700 mb-1 flex items-center gap-1">Nombre del Campo en Pantalla <AppTooltip content="El texto de la etiqueta que el usuario leerá en la pantalla visual producida." /></label>
              <input v-model="editingField.label" class="w-full text-sm border-gray-300 rounded" />
            </div>
            <div>
               <label class="block text-xs font-bold text-gray-700 mb-1">Mensaje de Ayuda para el Usuario Final</label>
               <input v-model="editingField.tooltipText" class="w-full text-sm border-gray-300 rounded" placeholder="Ej: Ingrese su nombre completo..." />
            </div>
            <div v-if="['text', 'textarea', 'number', 'email', 'url', 'password', 'info_modal'].includes(editingField.type)">
               <label class="block text-xs font-bold text-gray-700 mb-1 flex items-center gap-1">{{ editingField.type === 'info_modal' ? 'Contenido HTML / Pleno (Cuerpo del Modal)' : 'Texto Fantasma de Ejemplo' }}</label>
               <component :is="editingField.type === 'info_modal' ? 'textarea' : 'input'" v-model="editingField.placeholder" class="w-full text-sm border-gray-300 rounded" :placeholder="editingField.type === 'info_modal' ? 'Escribe el contenido detallado aquí...' : 'Ej: Juan Pérez'" :rows="editingField.type === 'info_modal' ? 6 : null" />
            </div>
            
            <!-- CA-30: Autocomplete Integration Section -->
            <div v-if="['text', 'password', 'email', 'url'].includes(editingField.type)" class="bg-[#f0f9ff] p-3 rounded border border-blue-200 space-y-3">
               <h4 class="text-xs font-bold text-blue-800 flex items-center gap-1">🌐 Autocomplete (CA-30)</h4>
               
               <div class="flex items-center gap-2">
                  <input type="checkbox" id="enableAutocomplete" v-model="editingField.enableAutocomplete" class="text-blue-600 rounded focus:ring-blue-500" />
                  <label for="enableAutocomplete" class="text-xs font-medium text-gray-700 cursor-pointer">Enable Autocomplete</label>
               </div>
               
               <div v-if="editingField.enableAutocomplete" class="space-y-2">
                  <div>
                      <label class="block text-[10px] font-bold text-gray-700 mb-1">Autocomplete URL</label>
                      <select data-test="autocomplete-select" v-model="editingField.autocompleteUrl" class="w-full text-sm border-blue-300 rounded font-mono bg-blue-50">
                        <option v-for="conn in approvedConnectors" :key="conn" :value="conn">
                          {{ conn }}
                        </option>
                      </select>
                   </div>
                  <div>
                     <label class="block text-[10px] font-bold text-gray-700 mb-1">Mappings JSON Array</label>
                     <textarea v-model="autocompleteMappingsText" class="w-full text-xs font-mono border-blue-300 rounded" rows="3" placeholder='[\n  {\n    "from": "nombre",\n    "to": "nombre_completo"\n  }\n]'></textarea>
                     <p class="text-[9px] text-blue-600">Formato: [{"from": "llave_api", "to": "camunda_variable_o_id"}]</p>
                  </div>
               </div>
            </div>
            <div v-if="editingField.type === 'async_select'" class="bg-purple-50 p-3 rounded border border-purple-200">
               <label class="block text-xs font-bold text-purple-800 mb-1">URL Endpoint Async</label>
               <input v-model="editingField.asyncUrl" class="w-full text-sm border-purple-300 rounded font-mono" placeholder="Ej: /api/v1/customers" />
               <p class="text-[10px] text-purple-600 mt-1">El input interrogará este endpoint con parámetros `?q=valor` en tiempo real (Typeahead AST).</p>
            </div>
            <div v-if="editingField.type === 'select'" class="bg-green-50 p-3 rounded border border-green-200">
               <label class="block text-xs font-bold text-green-800 mb-1">📥 Cargar una lista grande de opciones (Archivo CSV)</label>
               <input type="file" accept=".csv" @change="(e) => importCSVOptions(e, editingField!)" class="block w-full text-xs text-gray-500 file:mr-4 file:py-1 file:px-3 file:rounded file:border-0 file:text-xs file:font-semibold file:bg-green-100 file:text-green-700 hover:file:bg-green-200 cursor-pointer border border-green-200 rounded" />
               <p class="text-[10px] text-green-600 mt-1">Sube un archivo de Excel (.csv) con una sola columna. Esto llenará automáticamente las opciones de este menú sin que tengas que escribirlas una por una.</p>
               <p v-if="editingField.options" class="text-[10px] font-bold mt-1 text-green-800">{{ editingField.options.length }} Opciones Cargadas.</p>
            </div>
            
            <!-- CA-38: Zod Lengths -->
            <div v-if="['text', 'textarea'].includes(editingField.type)" class="flex gap-2">
               <div class="flex-1">
                 <label class="block text-xs font-bold text-gray-700 mb-1">Mínimo Caracteres</label>
                 <input type="number" v-model="editingField.minLength" class="w-full text-sm border-gray-300 rounded" placeholder="Ej: 5" />
               </div>
               <div class="flex-1">
                 <label class="block text-xs font-bold text-gray-700 mb-1">Máximo Caracteres</label>
                 <input type="number" v-model="editingField.maxLength" class="w-full text-sm border-gray-300 rounded" placeholder="Ej: 255" />
               </div>
            </div>

            <!-- CA-36/38: Input Mask (GAP 7) -->
            <div v-if="['text', 'number'].includes(editingField.type)" class="bg-gray-50 border border-gray-200 p-3 rounded">
               <label class="block text-xs font-bold text-gray-700 mb-1 flex items-center gap-1">Formato Predefinido de UI (IMask) <AppTooltip content="Inyecta un formato visual UX a medida que el usuario teclea, pero internamente emite el valor sin máscara (unmaskedValue)." /></label>
               <select v-model="editingField.predefinedFormat" class="w-full text-sm border-gray-300 rounded font-mono pr-8">
                 <option :value="undefined">Libre (Sin Máscara UX)</option>
                 <option value="currency">Moneda ($ 1.500,00)</option>
                 <option value="phone">Teléfono (+XX XXX-XXXX)</option>
                 <option value="idcard">Cédula Ciudadana (XX.XXX.XXX)</option>
                 <option value="regex">Manual Avanzado (Regex clásico)</option>
               </select>

               <div v-if="editingField.predefinedFormat === 'regex'" class="mt-3">
                  <label class="block text-[10px] font-bold text-gray-700 mb-1">Regex Crudo:</label>
                  <input v-model="editingField.mask" class="w-full text-sm border-gray-300 rounded font-mono text-gray-600" placeholder="Ej: ^[0-9]{4}$" />
               </div>
            </div>
            <!-- CA-39: File Upload Constraints -->
            <div v-if="editingField.type === 'file'" class="border border-orange-200 bg-orange-50 p-3 rounded">
               <h4 class="text-xs font-bold text-orange-800 mb-2">Restricciones de Archivo</h4>
               <div class="flex gap-2 mb-2">
                 <div class="flex-1">
                   <label class="block text-xs font-bold text-gray-700 mb-1">Peso Máx (MB)</label>
                   <input type="number" v-model="editingField.maxSizeMb" class="w-full text-sm border-gray-300 rounded" placeholder="Ej: 5" />
                 </div>
                 <div class="flex-1">
                   <input v-model="editingField.allowedExts" class="w-full text-sm border-gray-300 rounded" placeholder=".pdf,.png" />
                 </div>
               </div>
               
               <div class="flex gap-2 mb-2">
                 <div class="flex-1">
                   <label class="block text-xs font-bold text-gray-700 mb-1">Mínimo Archivos</label>
                   <input type="number" v-model="editingField.minFiles" class="w-full text-sm border-gray-300 rounded" placeholder="Ej: 1" />
                 </div>
                 <div class="flex-1">
                   <label class="block text-xs font-bold text-gray-700 mb-1">Máximo Archivos</label>
                   <input type="number" v-model="editingField.maxFiles" class="w-full text-sm border-gray-300 rounded" placeholder="Ej: 5" />
                 </div>
               </div>
               <p class="text-[10px] text-orange-600">Validará en frontend antes de subir por Axios.</p>
            </div>
            
            <!-- CA-41: Grid Constraints -->
            <div v-if="editingField.type === 'field_array'" class="flex gap-2">
               <div class="flex-1">
                 <label class="block text-xs font-bold text-gray-700 mb-1">Mínimo Filas</label>
                 <input type="number" v-model="editingField.minRows" class="w-full text-sm border-gray-300 rounded" placeholder="Ej: 1" />
               </div>
               <div class="flex-1">
                 <label class="block text-xs font-bold text-gray-700 mb-1">Máximo Filas</label>
                 <input type="number" v-model="editingField.maxRows" class="w-full text-sm border-gray-300 rounded" placeholder="Ej: 10" />
               </div>
            </div>

            <!-- CA-45: Multi-select Chips -->
            <div v-if="['select', 'async_select'].includes(editingField.type)" class="flex items-center gap-2 pt-2 border-t mt-2">
               <input type="checkbox" v-model="editingField.isMultiple" id="multipleCheck" class="text-indigo-600 rounded focus:ring-indigo-500" />
               <label for="multipleCheck" class="text-xs font-medium text-gray-700 cursor-pointer">☑️ Permitir que el usuario elija varias opciones al mismo tiempo</label>
            </div>
            
            <div v-if="formPattern === 'IFORM_MAESTRO'" class="bg-blue-50 p-3 rounded border border-blue-200">
               <label class="block text-xs font-bold text-blue-800 mb-1 flex items-center gap-1">Stage (Etapa BPMN de aparición) <AppTooltip content="Etapa en la cual el campo se revelará dinámicamente o dejará de bloquearse." /></label>
               <input v-model="editingField.stage" class="w-full text-sm border-blue-300 rounded font-mono" placeholder="Ej: ANALYSIS" />
            </div>
            <div>
              <label class="block text-xs font-bold text-indigo-700 mb-1 flex items-center gap-1">Enlace con el Proceso (Camunda) <AppTooltip content="Con este nombre viajará el dato a través de las siguientes etapas." /></label>
              <input v-model="editingField.camundaVariable" list="dictionary-datalist" @change="applyDictionaryVariable" class="w-full text-sm border-indigo-300 rounded font-mono bg-indigo-50" placeholder="Ej: customerName" />
              <datalist id="dictionary-datalist">
                <option v-for="item in dictionaryItems" :key="item.id" :value="item.id">
                  {{ item.label }} ({{ item.type || 'text' }})
                </option>
              </datalist>
            </div>
            <div class="flex items-center gap-2 pt-2 border-t mt-4">
               <input type="checkbox" v-model="editingField.required" id="reqCheck" class="text-indigo-600 rounded" />
               <label for="reqCheck" class="text-sm font-medium text-gray-700 cursor-pointer flex items-center gap-1">🔴 ¿Es de llenado obligatorio? <AppTooltip content="Fuerza al validador Zod On-The-Fly a bloquear el envío si el campo es nulo o vacío." /></label>
            </div>
            <div v-if="formPattern === 'IFORM_MAESTRO'" class="flex items-center gap-2 pt-2 border-t">
               <input type="checkbox" v-model="editingField.soloLecturaPosterior" id="roCheck" class="text-indigo-600 rounded focus:ring-indigo-500" />
               <label for="roCheck" class="text-sm font-medium text-gray-700 cursor-pointer flex items-center gap-1">Bloquear en etapas futuras (RBAC) <AppTooltip content="Si se activa, este input será Deshabilitado (:disabled) si el proceso actual avanza a una etapa diferente." /></label>
            </div>
            <div class="flex items-center gap-2 pt-2 border-t mt-2">
               <input type="checkbox" v-model="editingField.enableAuditLog" id="auditCheck" class="text-red-500 rounded focus:ring-red-500 border-gray-300" />
               <label for="auditCheck" class="text-xs font-medium text-red-700 cursor-pointer flex items-center gap-1">🛡️ Rastrear usuario que llene este campo (Forense) <AppTooltip content="Guarda un registro oculto de quién escribió este dato, la fecha y hora, para protegerse en futuras auditorías legales." /></label>
            </div>
            <div class="flex items-center gap-2 pt-2 border-t mt-2">
               <input type="checkbox" v-model="editingField.isPII" id="piiCheck" class="text-indigo-600 rounded focus:ring-indigo-500 border-gray-300" />
               <label for="piiCheck" class="text-xs font-medium text-gray-700 cursor-pointer flex items-center gap-1">🔒 Clasificar como Dato Sensible PII/PHI (Análisis Shift-Left para Zod) <AppTooltip content="Añade el decorador .describe('isPII') al motor Zod para que el Backend intercepte y enmascare este dato en bases de datos Cloud" /></label>
            </div>
          </div>

          <!-- CA-48: Condicional Zod Validaciones -->
          <div class="p-4 bg-yellow-50 border border-yellow-200 rounded-lg mt-4 shadow-inner">
             <h4 class="text-xs font-bold text-yellow-800 mb-2 border-b border-yellow-300 pb-1 flex items-center gap-2">⚡ Reglas de Visibilidad (Mostrar/Ocultar y Requerir dinámicamente)</h4>
             <div class="flex gap-2 mb-2">
                 <div class="flex-1">
                   <label class="block text-[10px] font-bold text-gray-700 mb-1">Requiere si Campo (ID):</label>
                   <input v-model="editingField.requiredIfField" class="w-full text-xs border-yellow-300 rounded font-mono" placeholder="Ej: TIENE_HIJOS" />
                 </div>
                 <div class="flex-1">
                   <label class="block text-[10px] font-bold text-gray-700 mb-1">Es Igual A (Valor):</label>
                   <input v-model="editingField.requiredIfValue" class="w-full text-xs border-yellow-300 rounded font-mono" placeholder="Ej: SI" />
                 </div>
             </div>
             <p class="text-[9px] text-yellow-700 leading-tight">Hace que este campo sea obligatorio de llenar SOLAMENTE si la regla de arriba se cumple.</p>
          </div>

          <div class="p-4 bg-gray-50 border border-gray-200 rounded-lg mt-4 shadow-inner">
             <h4 class="text-xs font-bold text-gray-800 mb-2 border-b border-gray-300 pb-1 flex items-center gap-2">Carga y Guardado de Datos (Motor BPM)</h4>
             <div class="flex items-center gap-2 mb-2">
                <input type="checkbox" v-model="editingField.isPrefilled" id="prefillCheck" class="text-indigo-600 rounded focus:ring-indigo-500" />
                <label for="prefillCheck" class="text-xs font-medium text-gray-700 cursor-pointer flex items-center gap-1">Auto-completar con datos de etapas previas</label>
             </div>
             <div class="flex items-center gap-2">
                <input type="checkbox" v-model="editingField.isOutputToken" id="outCheck" class="text-indigo-600 rounded focus:ring-indigo-500" />
                <label for="outCheck" class="text-xs font-medium text-gray-700 cursor-pointer flex items-center gap-1">Sobrescribir el dato en el proceso global</label>
             </div>
          </div>

          <!-- CA-55, CA-57, CA-58 -->
          <div v-if="editingField.type === 'container'" class="p-4 bg-gray-50 border border-gray-200 rounded-lg mt-4 shadow-inner">
             <h4 class="text-xs font-bold text-gray-800 mb-2 border-b border-gray-300 pb-1">🗂️ Layout Multicolumna (CA-55)</h4>
             <label class="block text-xs font-bold text-gray-700 mb-1">Número de Columnas Grid</label>
             <select v-model.number="editingField.columns" class="w-full text-sm border-gray-300 rounded">
                <option :value="undefined">Automático (Flex Col)</option>
                <option :value="1">1 Columna (100%)</option>
                <option :value="2">2 Columnas (50%)</option>
                <option :value="3">3 Columnas (33%)</option>
                <option :value="4">4 Columnas (25%)</option>
             </select>
          </div>

          <div v-if="editingField.type === 'timer'" class="p-4 bg-gray-50 border border-gray-200 rounded-lg mt-4 shadow-inner">
             <h4 class="text-xs font-bold text-gray-800 mb-2 border-b border-gray-300 pb-1">⏱️ Cronómetro (CA-58)</h4>
             <label class="block text-xs font-bold text-gray-700 mb-1">Modo de Temporizador</label>
             <select v-model="editingField.timerMode" class="w-full text-sm border-gray-300 rounded">
                <option value="manual">Controles Manuales (Play/Pause)</option>
                <option value="background">Oculto Transparente (Ticking JS)</option>
                <option value="api">Mock API (Obtiene timestamp remoto)</option>
             </select>
          </div>

          <div v-if="!['container','button_submit','button_draft','button_reject'].includes(editingField.type)" class="p-4 bg-gray-50 border border-gray-200 rounded-lg mt-4 shadow-inner">
             <h4 class="text-xs font-bold text-gray-800 mb-2 border-b border-gray-300 pb-1">🔒 Congelar o Bloquear Campo bajo Condición</h4>
             <label class="block text-xs font-bold text-gray-700 mb-1">Condición de Bloqueo Eval</label>
             <input v-model="editingField.disableCondition" class="w-full text-sm border-gray-300 rounded font-mono" placeholder="Ej: formData.ROL === 'INVITADO'" />
             <p class="text-[10px] text-gray-500 mt-1">Escriba cuándo se debe bloquear. Ej: Si el ROL es INVITADO, el campo se congela.</p>
          </div>

          <div class="p-4 bg-gray-50 border border-gray-200 rounded-lg mt-4 shadow-inner">
             <h4 class="text-xs font-bold text-gray-800 mb-2 border-b border-gray-300 pb-1 flex items-center gap-2">👁️ Ocultar Campo usando Fórmulas</h4>
             <div>
                <label class="block text-xs font-bold text-gray-700 mb-1">Regla para mostrarlo en pantalla:</label>
                <input v-model="editingField.visibilityCondition" class="w-full text-sm border-gray-300 rounded font-mono" placeholder="Ej: país == 'Colombia'" />
                <p class="text-[10px] text-gray-500 mt-1">Si la fórmula coincide, el usuario verá esta caja. Si no, permanecerá invisible en la pantalla.</p>
             </div>
             <div class="mt-3 flex items-center gap-2 pt-2 border-t border-gray-200">
                <input type="checkbox" v-model="editingField.clearOnHide" id="clearHideCheck" class="text-indigo-600 rounded focus:ring-indigo-500 cursor-pointer" />
                <label for="clearHideCheck" class="text-[10px] font-bold text-red-700 cursor-pointer">💣 Auto-Purgar (Destroy on Hide) <AppTooltip content="GAP 8: Previene fugas de memoria borrando el V-Model si la fórmula oculta el campo." /></label>
             </div>
          </div>

          <div class="mt-6 flex justify-end gap-3">
            <button @click="editingField = null" class="bg-indigo-600 text-white px-4 py-2 rounded text-sm font-semibold hover:bg-indigo-700">Guardar Cambios</button>
          </div>
        </div>
      </div>

      <!-- Test Gen / Result Modal -->
      <div v-if="showResultModal" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[900] p-4 backdrop-blur-sm">
          <div class="bg-gray-900 rounded-xl max-w-2xl w-full shadow-2xl border border-gray-700 flex flex-col overflow-hidden">
              <div class="px-5 py-3 bg-gray-800 border-b border-gray-700 flex justify-between items-center text-white">
                 <h3 class="font-bold flex items-center gap-2 text-sm">{{ modalTitle }}</h3>
                 <button @click="showResultModal = false" class="text-gray-400 hover:text-white">&times;</button>
              </div>
              <div class="p-5 overflow-y-auto font-mono text-xs text-green-400 whitespace-pre-wrap leading-relaxed max-h-[60vh]">
  {{ modalContent }}
              </div>
              <div class="px-5 py-3 bg-gray-800 border-t border-gray-700 flex justify-between">
                  <button v-if="modalTitle.includes('Tests')" class="text-xs text-gray-400 hover:text-white flex items-center gap-1">📋 Copiar al Portapapeles</button>
                  <div v-else></div>
                  <button @click="showResultModal = false" class="bg-indigo-600 text-white px-4 py-1.5 rounded hover:bg-indigo-700 text-xs font-bold font-sans">Cerrar</button>
              </div>
          </div>
      </div>

      <!-- Modal de Confirmación de Reset Dual (CA-43) -->
      <div v-if="showResetModal" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[900] p-4 backdrop-blur-sm animate-slide-in">
        <div class="bg-white rounded-xl shadow-2xl p-6 max-w-sm w-full border border-gray-200">
          <div class="flex items-center gap-3 mb-4 text-red-600">
             <svg class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
             </svg>
             <h3 class="text-lg font-bold">Confirmar Reset</h3>
          </div>
          <p class="text-sm text-gray-600 mb-6">
             ¿Está seguro que desea borrar todo el diseño del formulario? <b>Esta acción no se puede deshacer</b> y todo el código generado se perderá.
          </p>
          <div class="flex justify-end gap-3">
            <button @click="showResetModal = false" class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition">Cancelar</button>
            <button @click="executeReset" class="px-4 py-2 text-sm font-medium text-white bg-red-600 hover:bg-red-700 rounded-lg shadow transition">Sí, Borrar</button>
          </div>
        </div>
      </div>
    </Teleport>

<!-- Modal CA-32 Zod Global Rules -->
    <Teleport to="body">
      <div v-if="showGlobalRulesModal" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[900] p-4 backdrop-blur-sm">
        <div class="bg-white rounded-xl shadow-2xl p-6 px-8 max-w-2xl w-full border border-gray-200">
          <h2 class="text-lg font-bold mb-4 flex items-center gap-2">⚙️ Logic Builder Visual (CA-32) <AppTooltip content="Construye validaciones cruzadas AST Zero-Code aplicadas de forma transversal al documento entero."/></h2>
          
          <div class="space-y-3 max-h-[50vh] overflow-y-auto mb-4 border border-gray-100 p-2 rounded bg-gray-50/50">
             <div v-if="visualRules.length === 0" class="text-center text-sm text-gray-400 py-6">No hay reglas cruzadas globales configuradas.</div>
             
             <div v-for="(rule, idx) in visualRules" :key="idx" class="border border-indigo-200 bg-white p-3 rounded shadow-sm flex flex-col gap-2 relative group">
                <button @click="visualRules.splice(idx, 1)" class="absolute top-2 right-2 text-red-400 hover:text-red-600 opacity-0 group-hover:opacity-100 transition"><span class="material-symbols-outlined text-[16px]">delete</span></button>
                <div class="flex gap-2 items-center">
                   <select v-model="rule.fieldA" class="form-select text-xs font-mono border-gray-300 flex-1 rounded text-indigo-700">
                      <option disabled value="">[Seleccionar Campo A]</option>
                      <option v-for="f in availableFieldsFlat" :key="f.id" :value="f.id">{{ f.label }} ({{f.id}})</option>
                   </select>
                   <select v-model="rule.operator" class="form-select text-xs font-bold border-gray-300 w-24 rounded bg-gray-100 text-center">
                      <option value=">">MAYOR QUE</option>
                      <option value="<">MENOR QUE</option>
                      <option value="==">IGUAL A</option>
                      <option value="!=">DIFERENTE A</option>
                   </select>
                   <select v-model="rule.fieldB" class="form-select text-xs font-mono border-gray-300 flex-1 rounded text-orange-700">
                      <option disabled value="">[Seleccionar Campo B]</option>
                      <option v-for="f in availableFieldsFlat" :key="f.id" :value="f.id">{{ f.label }} ({{f.id}})</option>
                   </select>
                </div>
                <div>
                   <input v-model="rule.errorMessage" class="w-full text-[11px] border-red-200 focus:border-red-400 text-red-600 rounded bg-red-50/30" placeholder="Mensaje de error si falla la validación (Ej: Fecha Fin no puede ser antes de Fecha Inicio).." />
                </div>
             </div>
          </div>
          
          <button @click="visualRules.push({ fieldA: '', operator: '>', fieldB: '', errorMessage: 'Valores inconsistentes cruzados.' })" class="w-full border-2 border-dashed border-gray-300 p-2 rounded text-gray-500 font-bold hover:bg-gray-50 hover:text-indigo-600 transition text-sm flex justify-center items-center gap-1">
             <span class="text-xl leading-none">+</span> Añadir Nueva Regla
          </button>

          <div class="mt-6 flex justify-end gap-3 border-t pt-4">
            <button @click="saveVisualRules" class="bg-indigo-600 text-white font-bold px-5 py-2 rounded hover:bg-indigo-700 transition shadow-sm">💾 Guardar Ast Zod</button>
          </div>
        </div>
      </div>

      <!-- Escáner Mágico IA Modal (CA-73) -->
      <div v-if="showAiModal" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[900] p-4 backdrop-blur-sm">
         <div class="bg-white rounded-xl shadow-2xl p-6 md:p-8 max-w-lg w-full">
            <h2 class="text-xl font-bold text-gray-800 mb-2 flex items-center gap-2">✨ Escáner Mágico IA</h2>
            <p class="text-xs text-gray-600 mb-4">Describe el formulario que necesitas en lenguaje natural o pega un texto crudo de requerimientos. El motor LMM construirá el JSON AST automáticamente.</p>
            <textarea v-model="aiPrompt" rows="5" class="w-full form-textarea border-gray-300 rounded text-sm mb-4 bg-purple-50 focus:border-purple-400 focus:ring-purple-400" placeholder="Ej: Necesito un formulario de onboarding para proveedores con nombre, nit, y tabla de documentos..."></textarea>
            <div class="flex justify-end gap-3">
               <button @click="showAiModal = false" class="px-4 py-2 text-gray-600 bg-gray-100 rounded text-sm font-bold">Cancelar</button>
               <button @click="generateAiForm" :disabled="isScanningAi" class="px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded text-sm font-bold flex items-center gap-2 disabled:bg-purple-400 transition">
                  <span v-if="isScanningAi" class="animate-pulse">Generando JSON...</span>
                  <span v-else>🚀 Generar Diseño</span>
               </button>
            </div>
         </div>
      </div>

      <!-- Runtime Template Preview (Shadow DOM) -->
      <div v-if="showPreviewModal" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[900] p-4 backdrop-blur-sm">
         <div class="bg-gray-100 rounded-xl shadow-2xl w-full max-w-4xl max-h-[90vh] flex flex-col overflow-hidden animate-slide-in">
            <div class="p-4 bg-white border-b border-gray-200 flex justify-between items-center shadow-sm z-10">
               <h2 class="text-lg font-bold text-gray-800 flex items-center gap-2">👁️ Virtual DOM Renderer <span class="bg-emerald-100 text-emerald-800 text-[10px] uppercase px-2 py-0.5 rounded font-mono">Shadow DOM O-T-F</span></h2>
               <button @click="showPreviewModal = false" class="text-gray-400 hover:text-gray-600 font-bold text-xl">&times;</button>
            </div>
            <div class="p-6 overflow-y-auto flex-1 relative bg-white m-4 rounded shadow-sm border border-gray-200">
               <FormRenderer :schema="canvasFields" v-model="previewFormData" :simulatorContext="simulatorContext" />
            </div>
            <div class="p-4 border-t border-gray-200 bg-gray-50 flex justify-between items-center text-xs text-gray-400 font-mono">
               <span>Live FormData: {{ JSON.stringify(previewFormData) }}</span>
            </div>
         </div>
      </div>

      <!-- QA Sandbox Fuzzer Modal (CA-79) -->
      <div v-if="showFuzzerModal" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[900] p-4 backdrop-blur-sm">
         <div class="bg-gray-100 rounded-xl shadow-2xl p-6 md:p-8 max-w-4xl w-full flex flex-col h-[80vh]">
            <div class="flex items-center justify-between mb-4 border-b border-gray-200 pb-2">
               <h2 class="text-xl font-bold text-gray-800 flex items-center gap-2">
                  ⚡ QA Sandbox Fuzzer (RAM)
                  <!-- CA-13: Indicador de versión en Sandbox -->
                  <span class="text-xs bg-gray-100 text-gray-700 border border-gray-300 px-2 py-0.5 rounded font-mono ml-2">
                    📋 Esquema V{{ currentSchemaVersion }} — 
                    <span v-if="certificationState === 'certified'" class="text-green-700">Certificado ✅</span>
                    <span v-else class="text-amber-700">Sin certificar ⚠️</span>
                  </span>
               </h2>
               <button @click="showFuzzerModal = false" class="text-gray-400 hover:text-gray-600 text-xl font-bold">&times;</button>
            </div>
            <div class="flex gap-4 flex-1 overflow-hidden">
               <div class="w-1/2 flex flex-col">
                  <div class="flex justify-between items-center mb-2">
                     <span class="text-xs font-bold text-gray-700">JSON Payload (Modificable)</span>
                     <div class="flex gap-2">
                        <button @click="generateMockPath('happy')" class="text-[10px] bg-green-100 text-green-800 px-2 py-1 rounded hover:bg-green-200">Autocompletar Happy</button>
                        <button @click="generateMockPath('sad')" class="text-[10px] bg-red-100 text-red-800 px-2 py-1 rounded hover:bg-red-200">Autocompletar Sad</button>
                        <button @click="fuzzerPayload = '{\n  \n}'" class="text-[10px] bg-gray-100 text-gray-700 px-2 py-1 rounded hover:bg-gray-200">🗑️ Limpiar</button>
                     </div>
                  </div>
                  <textarea v-model="fuzzerPayload" class="flex-1 form-textarea font-mono text-xs p-3 border-gray-300 rounded shadow-sm resize-none"></textarea>
               </div>
               <div class="w-1/2 flex flex-col">
                  <button @click="runFuzzerZod" class="bg-indigo-600 text-white font-bold py-2 rounded shadow mb-4 hover:bg-indigo-700 transition">▶️ Ejecutar Zod in-memory</button>
                  <div class="flex-1 bg-black rounded p-4 overflow-y-auto">
                     <!-- CA-14: Indicador de SuperRefine -->
                     <div v-if="superRefineCount > 0" class="text-orange-400 font-mono text-xs mb-2 border-b border-orange-800 pb-1">
                       🔧 {{ superRefineCount }} validaciones cruzadas detectadas — Requieren corrección manual del QA
                     </div>
                     <div v-if="fuzzerErrors.length === 0" class="text-green-400 font-mono text-xs flex items-center gap-2">
                        <span>> Esperando ejecución o Validado exitosamente sin errores O-T-F.</span>
                     </div>
                     <div v-else class="text-gray-400 font-mono text-xs space-y-1">
                        <div v-for="(err, i) in fuzzerErrors" :key="i" :class="err.isRefine ? 'text-orange-400' : 'text-red-400'">
                           <span v-if="err.isRefine">⚠️</span><span v-else>❌</span> {{ err.msg }}
                        </div>
                     </div>
                  </div>
                  
                  <!-- @Traceability: US-028 - CA-11 - Certificación de Contrato Zod -->
                  <div class="mt-4 flex justify-end" v-if="fuzzerErrors.length === 0 && fuzzerPayload !== '{}' && fuzzerPayload !== ''">
                     <button @click="handleCertifyForm" class="bg-yellow-500 text-black font-bold py-2 px-4 rounded shadow hover:bg-yellow-600 transition flex items-center gap-2">
                        🏆 CERTIFICAR CONTRATO ZOD
                     </button>
                  </div>

                  <!-- CA-17: Panel de Coherencia BPMN ↔ Zod -->
                  <details v-if="formKey" class="mt-4 bg-gray-800 rounded p-3 border border-gray-700">
                    <summary class="text-xs font-bold text-cyan-400 cursor-pointer">🔗 Coherencia BPMN ↔ Zod</summary>
                    <div class="mt-2 space-y-1 text-xs font-mono">
                      <div v-for="item in bpmnCoherenceResults" :key="item.name" :class="item.class">
                        {{ item.icon }} {{ item.label }}
                      </div>
                    </div>
                  </details>
                  <div v-else class="mt-4 text-xs text-gray-500 italic">
                    🔗 Sin proceso BPMN vinculado — Validación de coherencia no aplica
                  </div>
               </div>
            </div>
         </div>
      </div>
    </Teleport>

  </div>
</template>

<script setup lang="ts">
// @Traceability: US-003 - CA-27, CA-30, CA-74, CA-77
import { useIntegrationStore } from '@/stores/useIntegrationStore';
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useLocalStorage } from '@vueuse/core';
import VueDraggable from 'vuedraggable';
import VueMonacoEditor from '@guolao/vue-monaco-editor';
import { ZodBuilder, FormFieldMetadataDTO } from './ZodBuilder';
import AppTooltip from '@/components/common/AppTooltip.vue';
import FormRenderer from '@/components/forms/FormRenderer.vue';
// @ts-ignore
import jexl from 'jexl';
import { useAuthStore } from '@/stores/authStore';
import { useFormDesignerStore } from '@/stores/useFormDesignerStore';
import { storeToRefs } from 'pinia';

// @Traceability: Retro-Remediación ADR-006
const integrationStore = useIntegrationStore();


// GAP 9: Mimetismo RBAC
const authStore = useAuthStore();
const formStore = useFormDesignerStore();
const {
  canvasFields,
  formTitle,
  formPattern,
  activeStageSim,
  visualRules,
  formVersions,
  isPublic,
  certificationState,
  currentSchemaVersion,
  currentFormId,
  bpmnCoherenceResults,
  formKey,
  zodParseError,
  aiPrompt,
  isScanningAi,
  fuzzerErrors,
  superRefineCount,
  toolboxCategories,
  activeCodeTab,
  localJsonCode,
  editingField,
  computedCode,
  dictionaryItems,
  approvedConnectors
} = storeToRefs(formStore);

const { simulatorContext, evaluateMockVis, cloneComponent, attemptTabChange, fetchDictionary, fetchSnippets, saveSnippet, fetchApprovedConnectors } = formStore;

// ── Types ────────────────────────────────────────────────────────
interface FormField extends FormFieldMetadataDTO {
  soloLecturaPosterior?: boolean; // CA-20
  asyncUrl?: string; // CA-30
  enableAuditLog?: boolean; // CA-28
  predefinedFormat?: string; // CA-36
  mask?: string; // CA-36
  clearOnHide?: boolean; // CA-8
  enableAutocomplete?: boolean; // CA-30
  autocompleteUrl?: string; // CA-30
  autocompleteMappings?: { from: string; to: string }[]; // CA-30
}

// ── State ────────────────────────────────────────────────────────
const route = useRoute();
// const formTitle removed

const showPatternModal = ref(true);
const isFullScreen = ref(false); // Estado para CA-9/CA-10

// CA-6: Shadow DOM Host References
const designerHostRef = ref<HTMLElement | null>(null);
const designerShadowContainer = ref<HTMLElement | null>(null);

// CA-15.1: Formularios Públicos

const processKeyMock = formTitle.value.toUpperCase().replace(/\s+/g, '_').substring(0, 15);
formKey.value = (route.query.processKey || route.query.formKey || '') as string;



const publicUrl = computed(() => `${window.location.origin}/public/start/${processKeyMock}`);

const copyPublicUrl = () => {
    navigator.clipboard.writeText(publicUrl.value);
    showToast('Enlace público (Huérfano) copiado al portapapeles', 'success');
};



const showGlobalRulesModal = ref(false); // CA-32
const availableFieldsFlat = computed(() => {
    const flat = (arr: any[]): any[] => {
        let res: any[] = [];
        for (const f of arr) {
            if (['container', 'tabs', 'tab_pane', 'accordion', 'accordion_panel'].includes(f.type) && f.children) res = res.concat(flat(f.children));
            else if (!['container', 'tabs', 'tab_pane', 'accordion', 'accordion_panel'].includes(f.type) && !f.type.startsWith('button_')) res.push(f);
        }
        return res;
    };
    return flat(canvasFields.value);
});

const autocompleteMappingsText = computed({
  get() {
    if (!editingField.value || !editingField.value.autocompleteMappings) return '';
    return JSON.stringify(editingField.value.autocompleteMappings, null, 2);
  },
  set(val: string) {
    if (!editingField.value) return;
    try {
      const parsed = JSON.parse(val);
      if (Array.isArray(parsed)) {
         editingField.value.autocompleteMappings = parsed;
      }
    } catch (e) {
      // Ignorar error de sintaxis temporal mientras escribe
    }
  }
});

// CA-90 / REM-003-04: Límites de Rendimiento para Formularios de Alta Densidad
const MAX_FORM_FIELDS = 200;
const isHighDensityForm = computed(() => availableFieldsFlat.value.length > MAX_FORM_FIELDS);


const saveVisualRules = () => {
    showToast(`Reglas cruzadas configuradas (${visualRules.value.length} activas)`, 'success');
    showGlobalRulesModal.value = false;
};


// CA-73: Escáner Mágico LMM
const showAiModal = ref(false);

const generateAiForm = async () => {
    const res = await formStore.generateAiForm(aiPrompt.value);
    if (res?.success) {
       showToast(res.message, 'success');
       showAiModal.value = false;
    } else if (res?.message) {
       showToast(res.message, 'error');
    }
};

// CA-74: Fragmentos en LocalStorage
const saveAsFragment = (node: any) => {
    formStore.saveAsFragment(node);
    showToast(`Componente consolidado en Fragmentos`, 'success');
};

const applyDictionaryVariable = () => {
    if (!editingField.value) return;
    const item = dictionaryItems.value.find((d: any) => d.id === editingField.value.camundaVariable);
    if (item) {
        editingField.value.label = item.label;
        editingField.value.isPII = !!item.isPII;
        if (item.type) {
            editingField.value.type = item.type;
        }
        showToast(`Variable corporativa '${item.id}' aplicada (Gobernanza MDM)`, 'success');
    }
};

onMounted(async () => {
    await fetchDictionary();
    await fetchSnippets();
    await fetchApprovedConnectors();
    // CA-6: Initialize Shadow DOM
    if (designerHostRef.value) {
        const shadowRoot = designerHostRef.value.attachShadow({ mode: 'open' });
        
        // Inyectamos Tailwind (Vite dev server) o genérico
        const link = document.createElement('link');
        link.rel = 'stylesheet';
        link.href = '/src/assets/index.tailwind.css'; // Fallback path
        shadowRoot.appendChild(link);

        const tailwindCdn = document.createElement('script');
        tailwindCdn.src = 'https://cdn.tailwindcss.com?plugins=forms';
        shadowRoot.appendChild(tailwindCdn);

        const container = document.createElement('div');
        container.className = 'workdesk-form-designer-canvas h-full';
        shadowRoot.appendChild(container);
        
        designerShadowContainer.value = container;
    }

    const formId = route.query.id as string;
    if (formId) {
        const res = await formStore.fetchForm(formId);
        if (res.success) {
            showToast(res.message, 'success');
        } else {
            showToast(res.message, 'error');
        }
    } else {
        const localStoreKey = 'form_draft_ca85_modeler';
        const savedCA85Msg = localStorage.getItem(localStoreKey);
        if (savedCA85Msg && canvasFields.value.length === 0) {
            try {
                canvasFields.value = JSON.parse(savedCA85Msg);
                showToast('Borrador restaurado (CA-85 Amnesia Cero)', 'success');
            } catch (e) {}
        }
    }

    const savedFragments = localStorage.getItem('workdesk_fragments');
    if (savedFragments) {
        const fragmentCategory = toolboxCategories.value.find(c => c.name === 'Mis Fragmentos');
        if (fragmentCategory) fragmentCategory.items = JSON.parse(savedFragments);
    }
});

// Runtime Render Preview Modal
const showPreviewModal = ref(false);
const previewFormData = ref({});
const openPreview = () => {
   previewFormData.value = {};
   showPreviewModal.value = true;
};

const oldIdTemp = ref('');
const handleIdChange = (field: FormField) => {
  if (oldIdTemp.value && oldIdTemp.value !== field.id) {
    visualRules.value.forEach(rule => {
      if (rule.fieldA === oldIdTemp.value) rule.fieldA = field.id;
      if (rule.fieldB === oldIdTemp.value) rule.fieldB = field.id;
    });
  }
};




const showHistoryModal = ref(false); // CA-27


const fetchVersions = async () => {
   await formStore.fetchVersions();
   showHistoryModal.value = true;
};

const restoreVersion = (ver: any) => {
    const res = formStore.restoreVersion(ver);
    if (res.success) {
       showToast(res.message, 'success');
       showHistoryModal.value = false;
    } else {
       showToast(res.message, 'error');
    }
};

// CA-24 Auto-guardado del Designer Canvas
let designerDraftTimeout: any = null;
let amnesiaDebounce: ReturnType<typeof setTimeout>; // CA-85

watch(canvasFields, (newVal) => {
    clearTimeout(designerDraftTimeout);
    designerDraftTimeout = setTimeout(async () => {
        await formStore.saveDraftToApi(formTitle.value, visualRules.value);
    }, 2000);

    // CA-85 Amnesia Cero Local Storage con Debounce 5s
    clearTimeout(amnesiaDebounce);
    amnesiaDebounce = setTimeout(() => {
        localStorage.setItem('form_draft_ca85_modeler', JSON.stringify(newVal));
        console.log('[CA-85] Amnesia Cero: Borrador de Mónaco/Lienzo persistido preventivamente');
    }, 5000);
}, { deep: true });



const showResetModal = ref(false); // Modal de Reset CA-43
const isPrintMode = ref(false); // Modo Lectura PDF CA-56

const exportToPdf = () => {
    window.print();
};

// GAP 10: Vitest Spec Generator
const generateVitestSpec = () => {
    formStore.generateVitestSpec(availableFieldsFlat.value);
    showToast('Archivo .spec.ts exportado exitosamente', 'success');
};

// CA-79: Consola QA Sandbox Fuzzer
const showFuzzerModal = ref(false);
const fuzzerPayload = useLocalStorage(`fuzzer_${route.query.id || 'draft'}`, `{\n  \n}`);


const openFuzzerSandbox = async () => {
    if (!fuzzerPayload.value || fuzzerPayload.value.trim() === '{}') { fuzzerPayload.value = `{\n  \n}`; }
    fuzzerErrors.value = [];
    showFuzzerModal.value = true;
    await formStore.checkBpmnCoherence(availableFieldsFlat.value);
};

const runFuzzerZod = () => {
    const res = formStore.runFuzzerZod(fuzzerPayload.value);
    if (res.success) {
        showToast(res.message, 'success');
    } else if (res.message && !res.message.includes('errores')) {
        showToast(res.message, 'error');
    }
};

const handleCertifyForm = async () => {
    const formId = route.query.id as string || 'DRAFT_MOCK_ID';
    const res = await formStore.certifyForm(formId, fuzzerPayload.value);
    if (res.success) {
        showToast(res.message, 'success');
    } else {
        showToast(res.message, 'error');
    }
};

const generateMockPath = (type: string) => {
    formStore.generateMockPath(type, fuzzerPayload);
};

// ── Modals / Toasts ──────────────────────────────────────────────
const showResultModal = ref(false);
const modalTitle = ref('');
const modalContent = ref('');
const toast = ref<{ msg: string; type: 'success' | 'error' }>({ msg: '', type: 'success' });

const showToast = (msg: string, type: 'success' | 'error' = 'success') => {
  toast.value = { msg, type };
  setTimeout(() => { toast.value.msg = ''; }, 4000);
};




// ── Actions ──────────────────────────────────────────────────────
const selectPattern = (pattern: 'SIMPLE' | 'IFORM_MAESTRO') => {
  formPattern.value = pattern;
  showPatternModal.value = false;
  if (canvasFields.value.length === 0) {
      canvasFields.value.push({
          id: 'FIELD_SEED_1',
          camundaVariable: 'field_seed_1',
          type: 'text',
          label: 'Campo Base (Semilla)',
          required: false,
          stage: 'START_EVENT'
      });
  }
  showToast(`Patrón ${pattern === 'SIMPLE' ? 'Simple' : 'iForm Maestro'} seleccionado.`);
};

const confirmReset = () => {
  showResetModal.value = true;
};

const executeReset = () => {
  canvasFields.value = [];
  editingField.value = null; // CA-59: Purge state local
  formStore.idCounter = 1;
  showResetModal.value = false;
  showPatternModal.value = true;
  localStorage.removeItem('designer_draft_fallback'); // CA-59
};

const removeField = (arr: FormField[], index: number) => {
  arr.splice(index, 1);
};

// CA-29: Importación Dinámica CSV In-Memory reader
const importCSVOptions = (event: any, fieldObj: FormField) => {
   const file = event.target?.files?.[0];
   if (!file) return;
   const reader = new FileReader();
   reader.onload = (e) => {
      const text = e.target?.result as string;
      if (text) {
         // Convierte cada salto de linea en opcion
         const lines = text.split('\n').map(l => l.trim()).filter(l => l.length > 0);
         fieldObj.options = lines;
         showToast(`Catálogo actualizado: ${lines.length} opciones cargadas.`, 'success');
      }
   };
   reader.readAsText(file);
};

const editField = (field: FormField) => {
  editingField.value = field;
};

const onCamundaVariableChange = (e: Event) => {
  const val = (e.target as HTMLInputElement).value;
  const found = dictionaryItems.value.find((item: any) => item.id === val);
  if (found && editingField.value) {
    editingField.value.label = found.label;
    editingField.value.isPII = found.isPII ?? false;
    if (found.type) {
      editingField.value.type = found.type;
    }
  }
};

declare const monaco: any;

const onMonacoMount = (_editorIns: any, monacoIns: any) => {
  // Intellisense Injection CA-115
  monacoIns.languages.typescript.typescriptDefaults.setCompilerOptions({
      target: monacoIns.languages.typescript.ScriptTarget.ESNext,
      allowNonTsExtensions: true,
      moduleResolution: monacoIns.languages.typescript.ModuleResolutionKind.NodeJs,
      module: monacoIns.languages.typescript.ModuleKind.CommonJS,
      noEmit: true,
      esModuleInterop: true,
      jsx: monacoIns.languages.typescript.JsxEmit.React,
      reactNamespace: "React",
      allowJs: true,
      typeRoots: ["node_modules/@types"]
  });

  monacoIns.languages.typescript.typescriptDefaults.addExtraLib(`
    /** Funciones core de Vue.js Inyectadas. */
    declare module 'vue' {
        /**
         * Crea una referencia reactiva (Mutable Reactivity) para UI State (CA-17 Language Server Hovering).
         */
        export function ref<T>(value: T): { value: T };
        /**
         * Propiedad calculada que se actualiza automáticamente ante cambios de sus dependencias (Read-Only).
         */
        export function computed<T>(getter: () => T): { value: T };
        /**
         * Adquiere un objeto o estado proveído por el Layout o el Host padre.
         */
        export function inject<T>(key: string, defaultValue?: T): T;
    }
    /** Validador de esquemas Zod O-T-F (On-The-Fly) */
    declare module 'zod' {
        /**
         * Creador maestro de construcciones declarativas de validación. 
         * Permite validar strings, numbers y objetos complejos antes del submit (CA-17).
         */
        export const z: any;
    }
    `, 'file:///node_modules/@types/vue-zod/index.d.ts');
};

const editorLanguage = computed(() => {
  if (activeCodeTab.value === 'TEMPLATE' || activeCodeTab.value === 'STYLE') return 'html';
  return 'typescript';
});

const monacoOptions = computed(() => ({
  readOnly: activeCodeTab.value === 'SCRIPT' || activeCodeTab.value === 'STYLE',
  minimap: { enabled: false },
  wordWrap: 'on',
  fontSize: 13,
  lineHeight: 22,
  scrollBeyondLastLine: false,
  padding: { top: 16 }
}));

// ── Modals Triggers ──────────────────────────────────────────────
// Eliminado old `generateTests` (CA-115). Se mantiene BDD Generator `generateVitestSpec`.

const saveForm = async () => {
  const formId = route.query.id as string;
  if (!formId) {
    showToast('No se puede guardar versión sin un ID de formulario', 'error');
    return;
  }
  const res = await formStore.saveForm(formId);
  if (res.success) {
    showToast(res.message, 'success');
    await formStore.fetchVersions();
  } else {
    showToast(res.message, 'error');
  }
};

const simulateMockSubmit = async () => {
    modalTitle.value = "🚀 Execute End-to-End Validation Engine & Integration (CA-29)";
    
    if (canvasFields.value.length === 0) {
        modalContent.value = `[WORKDESK VALIDATION ENGINE] (Vue Realtime Zod Factory)\n⚠️ PREVISUALIZACIÓN VACÍA.\nEl lienzo no tiene componentes para validar. Agrega elementos al diseño.`;
        showResultModal.value = true;
        return;
    }

    let executableSchema;
    try {
        // BUILD DYNAMIC ZOD SCHEMA FACTORY based on live fields metadata
        executableSchema = ZodBuilder.buildSchema(canvasFields.value);
    } catch (err: any) {
        showToast('Error en previsualización: Por favor verifica que todos tus componentes tengan un ID único', 'error');
        console.error('Zod AST Error:', err);
        return;
    }

    // Mapeo inicial vacío del Payload que se "recibe" simulando llenado del Usuario o Camunda
    const rawFormSubmission: Record<string, any> = {};

    // Evaluamos el safeParse en memoria real (SIN MOCKS ESTATICOS STINGS)
    const result = executableSchema.safeParse(rawFormSubmission);

    if(!result.success) {
      modalContent.value = `[WORKDESK VALIDATION ENGINE] (Vue Realtime Zod Factory)\n❌ FALLIDO: Integridad I/O de Camunda no superada.\n\nEl sistema Zod Dinámico arrojó infracciones de validación al intentar procesar payload vacío:\n\n` + 
      result.error.issues.map(iss => `  - [${iss.path.join('.')}] Rule '${iss.code}': ${iss.message}`).join('\n') + 
      `\n\n⚠️ Acción de Submit Abortada por el Front-end. El API no ha sido contactado.`;
      showResultModal.value = true;
      return;
    }

    modalContent.value = `[WORKDESK VALIDATION ENGINE] (Vue Realtime Zod Factory)\n✅ VALIDACION EXITOSA.\n\nEmitiendo POST hacia el Backend End-to-End...\n`;

    try {
        const dto = {
           name: formTitle.value,
           technicalName: formTitle.value.toUpperCase().replace(/\s+/g, '_').substring(0, 50),
           pattern: formPattern.value,
           formFields: canvasFields.value
        };
        const response = await integrationStore.post('/forms', dto);
        modalContent.value += `\n[BACKEND HTTP RESPONSE 201 CREATED]:\nRecepción de metadatos aprobada por la API.\nFormulario guardado para distribución:\n\n${JSON.stringify(response.data, null, 2)}`;
    } catch (error: any) {
        modalContent.value += `\n[BACKEND HTTP ERROR]:\n\nEndpoint devolvió fallo. Asegúrate que Java está activo.\n${error.message}`;
    }
    showResultModal.value = true;
};

defineExpose({
    certificationState,
    showFuzzerModal,
    currentSchemaVersion,
    fuzzerErrors,
    bpmnCoherenceResults,
    formKey
});
</script>

<style>
/* CSS Media Query for Export to PDF (CA-33) */
@media print {
  header, aside, .no-print {
    display: none !important;
  }
  .shadow-dom-isolation-wrapper {
    border: none !important;
    box-shadow: none !important;
    margin: 0 !important;
    padding: 0 !important;
    width: 100% !important;
    max-width: none !important;
  }
  main { overflow: visible !important; }
}

/* Ghost class for VueDraggable */
.ghost-dropzone {
  opacity: 0.5;
  background: #f0fdf4 !important; /* Tailwind green-50 */
  border: 2px dashed #22c55e !important; /* Tailwind green-500 */
}

/* Base custom Scrollbars for IDEs/Canvas */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
}
.dark ::-webkit-scrollbar-thumb,
aside[class*="bg-[#1e1e1e]"] ::-webkit-scrollbar-thumb {
  background-color: #4b5563;
}
</style>
