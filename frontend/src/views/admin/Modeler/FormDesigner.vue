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
            <button @click="mockContext.rbacRole = 'ADMIN'" :class="{'bg-blue-50 font-bold': mockContext.rbacRole === 'ADMIN'}" class="block w-full text-left px-4 py-2 hover:bg-gray-50 text-gray-700 transition">🛡️ ADMIN</button>
            <button @click="mockContext.rbacRole = 'OPERATOR'" :class="{'bg-blue-50 font-bold': mockContext.rbacRole === 'OPERATOR'}" class="block w-full text-left px-4 py-2 hover:bg-gray-50 text-gray-700 transition">👨‍💻 OPERATOR</button>
            <button @click="mockContext.rbacRole = 'MANAGER'" :class="{'bg-blue-50 font-bold': mockContext.rbacRole === 'MANAGER'}" class="block w-full text-left px-4 py-2 hover:bg-gray-50 text-gray-700 transition">👔 MANAGER</button>
            <button @click="mockContext.rbacRole = 'GUEST'" :class="{'bg-blue-50 font-bold': mockContext.rbacRole === 'GUEST'}" class="block w-full text-left px-4 py-2 hover:bg-gray-50 text-gray-700 transition">🏃 GUEST</button>
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

        <button @click="simulateMockSubmit" class="bg-indigo-600 text-white px-4 py-1.5 rounded shadow text-xs font-semibold hover:bg-indigo-700 transition flex items-center gap-2">
          🚀 Probar (Submit Mock)
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
          <!-- CA-6 Shadow DOM (Isolation css context class) -->
          <div class="shadow-dom-isolation-wrapper bg-white rounded-xl shadow-sm border border-gray-200 min-h-full p-8 max-w-4xl mx-auto flex flex-col relative" style="all: revert; box-sizing: border-box;">
            <h2 class="text-xl font-bold text-gray-800 mb-6 border-b pb-4 font-sans">{{ formTitle }}</h2>
            
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
        </div>
      </section>

      <!-- Monaco IDE (Bidireccional V2) -->
      <aside v-show="!isFullScreen" class="w-2/5 min-w-[350px] bg-[#1e1e1e] border-l border-gray-800 flex flex-col shadow-[-4px_0_15px_-3px_rgba(0,0,0,0.1)] z-20 shrink-0 transition-all">
        
        <!-- Tabs -->
        <div class="flex bg-[#252526] text-xs font-mono font-medium text-gray-400 border-b border-[#3e3e42] shrink-0 overflow-x-auto">
          <button @click="activeCodeTab = 'TEMPLATE'" :class="{ 'bg-[#1e1e1e] text-white border-t-2 border-emerald-500': activeCodeTab === 'TEMPLATE' }" class="px-4 py-2 hover:bg-[#2d2d2d] transition flex items-center gap-2">
            <span class="text-emerald-400">&lt;&gt;</span> template
          </button>
          <button @click="activeCodeTab = 'SCRIPT'" :class="{ 'bg-[#1e1e1e] text-white border-t-2 border-blue-500': activeCodeTab === 'SCRIPT' }" class="px-4 py-2 hover:bg-[#2d2d2d] transition flex items-center gap-2 whitespace-nowrap">
            <span class="text-blue-400">&lt;script setup&gt;</span>
            <AppTooltip content="Código Vue.js autogenerado con Composition API (Solo Lectura)." />
          </button>
          <button @click="activeCodeTab = 'STYLE'" :class="{ 'bg-[#1e1e1e] text-white border-t-2 border-pink-500': activeCodeTab === 'STYLE' }" class="px-4 py-2 hover:bg-[#2d2d2d] transition flex items-center gap-2 whitespace-nowrap">
            <span class="text-pink-400">&lt;style scoped&gt;</span>
            <AppTooltip content="Estilizado CSS inyectado para Tailwind y clases utilitarias (Solo Lectura)." />
          </button>
          <button @click="activeCodeTab = 'ZOD'" :class="{ 'bg-[#1e1e1e] text-white border-t-2 border-indigo-500': activeCodeTab === 'ZOD' }" class="px-4 py-2 hover:bg-[#2d2d2d] transition flex items-center gap-2">
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
          <p class="text-sm text-gray-600 mb-8">Selecciona la arquitectura de este formulario según la directriz (CA-2).</p>
          
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
              <input v-model="editingField.camundaVariable" class="w-full text-sm border-indigo-300 rounded font-mono bg-indigo-50" placeholder="Ej: customerName" />
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
               <FormRenderer :schema="canvasFields" v-model="previewFormData" :mockContext="mockContext" />
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
               <h2 class="text-xl font-bold text-gray-800 flex items-center gap-2">⚡ QA Sandbox Fuzzer (RAM)</h2>
               <button @click="showFuzzerModal = false" class="text-gray-400 hover:text-gray-600 text-xl font-bold">&times;</button>
            </div>
            <div class="flex gap-4 flex-1 overflow-hidden">
               <div class="w-1/2 flex flex-col">
                  <div class="flex justify-between items-center mb-2">
                     <span class="text-xs font-bold text-gray-700">JSON Payload (Modificable)</span>
                     <div class="flex gap-2">
                        <button @click="generateMockPath('happy')" class="text-[10px] bg-green-100 text-green-800 px-2 py-1 rounded hover:bg-green-200">Autocompletar Happy</button>
                        <button @click="generateMockPath('sad')" class="text-[10px] bg-red-100 text-red-800 px-2 py-1 rounded hover:bg-red-200">Autocompletar Sad</button>
                     </div>
                  </div>
                  <textarea v-model="fuzzerPayload" class="flex-1 form-textarea font-mono text-xs p-3 border-gray-300 rounded shadow-sm resize-none"></textarea>
               </div>
               <div class="w-1/2 flex flex-col">
                  <button @click="runFuzzerZod" class="bg-indigo-600 text-white font-bold py-2 rounded shadow mb-4 hover:bg-indigo-700 transition">▶️ Ejecutar Zod in-memory</button>
                  <div class="flex-1 bg-black rounded p-4 overflow-y-auto">
                     <div v-if="fuzzerErrors.length === 0" class="text-green-400 font-mono text-xs flex items-center gap-2">
                        <span>> Esperando ejecución o Validado exitosamente sin errores O-T-F.</span>
                     </div>
                     <div v-else class="text-red-400 font-mono text-xs space-y-1">
                        <div v-for="(err, i) in fuzzerErrors" :key="i">❌ {{ err }}</div>
                     </div>
                  </div>
               </div>
            </div>
         </div>
      </div>
    </Teleport>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import VueDraggable from 'vuedraggable';
import VueMonacoEditor from '@guolao/vue-monaco-editor';
import { ZodBuilder, FormFieldMetadataDTO } from './ZodBuilder';
import apiClient from '@/services/apiClient';
import AppTooltip from '@/components/common/AppTooltip.vue';
import FormRenderer from '@/components/forms/FormRenderer.vue';
import jexl from 'jexl';

// GAP 9: Mimetismo RBAC
import { reactive } from 'vue';
const mockContext = reactive({ rbacRole: 'ADMIN' });

const evaluateMockVis = (node: any) => {
    if (!node.visibilityCondition) return true;
    try {
        return jexl.evalSync(node.visibilityCondition, { data: {}, context: mockContext });
    } catch {
        return true; 
    }
};

// ── Types ────────────────────────────────────────────────────────
interface FormField extends FormFieldMetadataDTO {
  soloLecturaPosterior?: boolean; // CA-20
  asyncUrl?: string; // CA-30
  enableAuditLog?: boolean; // CA-28
}

// ── State ────────────────────────────────────────────────────────
const formTitle = ref('Solicitud Onboarding (V1)');
const formPattern = ref<'SIMPLE' | 'IFORM_MAESTRO' | null>(null);
const showPatternModal = ref(true);
const isFullScreen = ref(false); // Estado para CA-9/CA-10

// CA-15.1: Formularios Públicos
const isPublic = ref(false);
const processKeyMock = formTitle.value.toUpperCase().replace(/\s+/g, '_').substring(0, 15);
const publicUrl = computed(() => `${window.location.origin}/public/start/${processKeyMock}`);

const copyPublicUrl = () => {
    navigator.clipboard.writeText(publicUrl.value);
    showToast('Enlace público (Huérfano) copiado al portapapeles', 'success');
};

const visualRules = ref<{fieldA: string, operator: string, fieldB: string, errorMessage: string}[]>([]); // CA-32
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

const saveVisualRules = () => {
    showToast(`Reglas cruzadas configuradas (${visualRules.value.length} activas)`, 'success');
    showGlobalRulesModal.value = false;
};
const zodParseError = ref(false); // CA-Tarea 3 ZodRefactor

// CA-73: Escáner Mágico LMM
const showAiModal = ref(false);
const aiPrompt = ref('');
const isScanningAi = ref(false);

const generateAiForm = async () => {
    if (!aiPrompt.value) return;
    isScanningAi.value = true;
    try {
        const response = await apiClient.post('/api/v1/design/forms/generate', { prompt: aiPrompt.value });
        if (response.data && response.data.schema) {
            canvasFields.value = typeof response.data.schema === 'string' ? JSON.parse(response.data.schema) : response.data.schema;
            showToast('Formulario generado por LMM con éxito', 'success');
            showAiModal.value = false;
        }
    } catch(e) {
        showToast('Falla de conexión LMM (CA-73)', 'error');
    } finally {
        isScanningAi.value = false;
    }
};

// CA-74: Fragmentos en LocalStorage
const saveAsFragment = (node: any) => {
    const fragmentCategory = toolboxCategories.value.find(c => c.name === 'Mis Fragmentos');
    if (fragmentCategory) {
       fragmentCategory.items.push(JSON.parse(JSON.stringify(node)));
       localStorage.setItem('workdesk_fragments', JSON.stringify(fragmentCategory.items));
       showToast(`Componente consolidado en Fragmentos`, 'success');
    }
};

onMounted(() => {
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

const canvasFields = ref<FormField[]>([]);
const activeStageSim = ref('ALL');

const showHistoryModal = ref(false); // CA-27
const formVersions = ref<any[]>([]); // Almacena el historial UI

const fetchVersions = async () => {
   try {
       const res = await apiClient.get('/api/v1/forms/mock_id_or_draft/versions');
       formVersions.value = res.data;
   } catch(e) {
       // Mock fallback for UI Demo if API is not fully seeded
       formVersions.value = [
          { id: 'v2.1', version: '2.1', updatedAt: new Date().toISOString() },
          { id: 'v1.0', version: '1.0', updatedAt: new Date(Date.now() - 86400000).toISOString() }
       ];
   }
   showHistoryModal.value = true;
};

const restoreVersion = (ver: any) => {
    if (ver.schema) {
        canvasFields.value = typeof ver.schema === 'string' ? JSON.parse(ver.schema) : ver.schema;
        showToast(`Versión ${ver.version} restaurada exitosamente`, 'success');
        showHistoryModal.value = false;
        return;
    }
    // Fallback Forense LocalStorage (Simulando resiliencia post-desastre para UAT)
    const localDraft = localStorage.getItem('designer_draft_fallback');
    if (localDraft) {
        try {
            canvasFields.value = JSON.parse(localDraft);
            showToast(`Recuperación Forense Exitosa (${ver.version})`, 'success');
            showHistoryModal.value = false;
        } catch (e) {
            showToast('Memoria fría corrupta', 'error');
        }
    } else {
        showToast('No hay huella forense en disco local', 'error');
    }
};

// CA-24 Auto-guardado del Designer Canvas
let designerDraftTimeout: any = null;
watch(canvasFields, (newVal) => {
    clearTimeout(designerDraftTimeout);
    designerDraftTimeout = setTimeout(async () => {
        try {
            await apiClient.post('/api/v1/forms/draft', { schema: newVal, title: formTitle.value, formRules: visualRules.value });
            console.log('✅ Diseño auto-guardado en API (Modelador)');
        } catch (e) {
            localStorage.setItem('designer_draft_fallback', JSON.stringify(newVal));
            console.warn('⚠️ Fallback a LocalStorage activado para autoguardado del modelador');
        }
    }, 2000);
}, { deep: true });

const activeCodeTab = ref<'TEMPLATE' | 'SCRIPT' | 'ZOD' | 'STYLE'>('TEMPLATE');
const editingField = ref<FormField | null>(null);

const showResetModal = ref(false); // Modal de Reset CA-43
const isPrintMode = ref(false); // Modo Lectura PDF CA-56

const exportToPdf = () => {
    window.print();
};

// GAP 10: Vitest Spec Generator
const generateVitestSpec = () => {
    let specStr = `import { describe, it, expect } from 'vitest';\n`;
    specStr += `import { taskSchema } from './${formTitle.value.replace(/[^a-zA-Z0-9]/g, '')}Schema';\n\n`;
    specStr += `describe('Form Validation: ${formTitle.value}', () => {\n`;
    
    specStr += `  it('debe aceptar un payload Happy Path con todos los campos requeridos', () => {\n`;
    specStr += `    const validData = {\n`;
    availableFieldsFlat.value.forEach(f => {
        const key = f.camundaVariable || f.id;
        if(f.required) {
            if(f.type === 'number' || f.type === 'timer') specStr += `      ${key}: 42,\n`;
            else if(f.type === 'checkbox') specStr += `      ${key}: true,\n`;
            else if(f.type === 'email') specStr += `      ${key}: 'test@test.com',\n`;
            else if(f.type === 'url') specStr += `      ${key}: 'https://test.com',\n`;
            else if(f.isMultiple) specStr += `      ${key}: ['Option1'],\n`;
            else specStr += `      ${key}: 'Dummy Data',\n`;
        }
    });
    specStr += `    };\n`;
    specStr += `    const result = taskSchema.safeParse(validData);\n`;
    specStr += `    expect(result.success).toBe(true);\n`;
    specStr += `  });\n\n`;

    availableFieldsFlat.value.filter(f => f.required).forEach(f => {
        const key = f.camundaVariable || f.id;
        specStr += `  it('debe fallar si falta el campo requerido: ${key}', () => {\n`;
        specStr += `    const invalidData = { /* Omitir ${key} deliberadamente */ };\n`;
        specStr += `    const result = taskSchema.safeParse(invalidData);\n`;
        specStr += `    expect(result.success).toBe(false);\n`;
        specStr += `  });\n\n`;
    });

    specStr += `});\n`;

    const blob = new Blob([specStr], { type: 'text/typescript' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${formTitle.value.replace(/[^a-zA-Z0-9]/g, '')}.spec.ts`;
    a.click();
    URL.revokeObjectURL(url);
    showToast('Archivo .spec.ts exportado exitosamente', 'success');
};

// CA-79: Consola QA Sandbox Fuzzer
const showFuzzerModal = ref(false);
const fuzzerPayload = ref('{\n  \n}');
const fuzzerErrors = ref<string[]>([]);

const openFuzzerSandbox = () => {
    fuzzerPayload.value = '{\n  \n}';
    fuzzerErrors.value = [];
    showFuzzerModal.value = true;
};

const runFuzzerZod = () => {
    fuzzerErrors.value = [];
    try {
        const payload = JSON.parse(fuzzerPayload.value);
        const schema = ZodBuilder.buildSchema(canvasFields.value, visualRules.value);
        const result = schema.safeParse(payload);
        if (!result.success) {
            fuzzerErrors.value = result.error.issues.map(iss => `[${iss.path.join('.')}] - ${iss.message}`);
        } else {
            showToast('Payload Válido 🎉', 'success');
        }
    } catch(e: any) {
        fuzzerErrors.value = [`[JSON Syntax Error] - ${e.message}`];
    }
};

const generateMockPath = (type: string) => {
    let mock: any = {};
    const flatF = flatFields(canvasFields.value);
    flatF.forEach(f => {
        if(f.type.startsWith('button_') || f.type === 'container') return;
        const key = f.camundaVariable || f.id;
        if (type === 'happy') {
            if(f.type === 'number' || f.type === 'timer') mock[key] = 42;
            else if(f.type === 'checkbox') mock[key] = true;
            else if(f.type === 'email') mock[key] = 'test@example.com';
            else if(f.type === 'url') mock[key] = 'https://example.com';
            else if(f.isMultiple) mock[key] = ['Option1'];
            else mock[key] = 'Dummy Data';
        } else {
            mock[key] = null;
        }
    });
    fuzzerPayload.value = JSON.stringify(mock, null, 2);
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

// ── Toolbox Categories ───────────────────────────────────────────
const toolboxCategories = ref([
  {
    name: "Mis Fragmentos",
    items: [] as any[]
  },
  {
    name: "Texto",
    items: [
      { icon: 'Ab', label: 'Input Text', desc: 'Validación Regex', type: 'text', placeholder: 'Ej: Juan Pérez', required: true, zodType: 'string', camundaVariable: '' },
      { icon: '🔑', label: 'Password', desc: 'Dato Sensible (CA-53)', type: 'password', placeholder: 'Ingrese contraseña', required: true, zodType: 'string', camundaVariable: '' },
      { icon: '📧', label: 'Email', desc: 'Validación Zod .email()', type: 'email', placeholder: 'correo@ejemplo.com', required: true, zodType: 'string', camundaVariable: '' },
      { icon: '🔗', label: 'URL', desc: 'Validación Zod .url()', type: 'url', placeholder: 'https://ejemplo.com', required: false, zodType: 'string', camundaVariable: '' },
      { icon: '📝', label: 'Long Text', desc: 'Textarea (2+ filas)', type: 'textarea', placeholder: 'Comentarios...', required: false, zodType: 'string', camundaVariable: '' },
    ]
  },
  {
    name: "Numérico & Fechas",
    items: [
      { icon: '#', label: 'Number Field', desc: 'Zod min/max', type: 'number', placeholder: '0.00', required: true, zodType: 'number', camundaVariable: '' },
      { icon: '📅', label: 'Date Picker', desc: 'DD/MM/YYYY', type: 'date', placeholder: 'Seleccionar Fecha', required: false, zodType: 'string', camundaVariable: '' },
      { icon: '⏰', label: 'Time Picker', desc: 'HH:MM AM/PM', type: 'time', placeholder: 'Seleccionar Hora', required: false, zodType: 'string', camundaVariable: '' },
    ]
  },
  {
    name: "Selección",
    items: [
      { icon: '≡', label: 'Dropdown', desc: 'Soporta Array CSV', type: 'select', placeholder: '-- Seleccione --', required: true, zodType: 'string', options: ['Opción A', 'Opción B'], camundaVariable: '' },
      { icon: '🔄', label: 'Async Typeahead', desc: 'API Fetch (CA-30)', type: 'async_select', placeholder: 'Buscar en API...', required: true, zodType: 'string', asyncUrl: '', camundaVariable: '' },
      { icon: '☑️', label: 'Checkbox', desc: 'Booleano Múltiple', type: 'checkbox', placeholder: 'Marcar opción', required: false, zodType: 'boolean', camundaVariable: '' },
      { icon: '🔘', label: 'Radio Button', desc: 'Opción Única', type: 'radio', placeholder: '', required: true, zodType: 'string', options: ['Opción 1', 'Opción 2'], camundaVariable: '' },
    ]
  },
  {
    name: "Avanzados",
    items: [
      { icon: '📎', label: 'File Upload', desc: 'SGDEA Vault Embed', type: 'file', placeholder: 'Arrastra PDF aquí', required: false, zodType: 'any', camundaVariable: '' },
      { icon: '✍️', label: 'Firma Digital', desc: 'Canvas HTML5 (CA-31)', type: 'signature', placeholder: 'Dibuja tu firma', required: true, zodType: 'string', camundaVariable: '' },
      { icon: '📌', label: 'GPS Geolocation', desc: 'Coordenadas HTML5 (CA-61)', type: 'gps', placeholder: 'Ubicación...', required: true, zodType: 'string', camundaVariable: '' },
      { icon: '📷', label: 'Scan QR', desc: 'WebRTC Dummy (CA-62)', type: 'qr', placeholder: 'Código QR...', required: true, zodType: 'string', camundaVariable: '' },
    ]
  },
  {
    name: "Layouts (CA-8, CA-34)",
    items: [
      { icon: '🗂️', label: 'Contenedor', desc: 'Panel Agrupador', type: 'container', placeholder: 'Nueva Sección de Datos', required: false, zodType: 'object', camundaVariable: '', children: [] },
      { icon: '📇', label: 'Pestañas (Tabs)', desc: 'Multivista Horizontal', type: 'tabs', placeholder: 'Contenedor de Pestañas', required: false, zodType: 'object', camundaVariable: '', activeTab: 0, children: [] },
      { icon: '↕️', label: 'Acordeón', desc: 'Paneles Colapsables', type: 'accordion', placeholder: 'Acordeón Estructurado', required: false, zodType: 'object', camundaVariable: '', children: [] },
      { icon: '📑', label: 'Data Grid', desc: 'Fila Repetible', type: 'field_array', placeholder: 'Nueva Tabla', required: false, zodType: 'array', camundaVariable: '', children: [] },
      { icon: 'ℹ️', label: 'Modal Informativo', desc: 'Teleport Z-900 (Estéril)', type: 'info_modal', placeholder: 'Contenido del modal...', tooltipText: 'Título del Pop-up', required: false, zodType: 'none', camundaVariable: '' },
      { icon: '👁️‍🗨️', label: 'Hidden Input', desc: 'ID/Token Silencioso (CA-47)', type: 'hidden', placeholder: '', required: false, zodType: 'any', camundaVariable: '' }
    ]
  },
  {
    name: "Accionadores (CA-14)",
    items: [
      { icon: '💾', label: 'Guardar Borrador', desc: 'API DRAFT', type: 'button_draft', placeholder: '', required: false, zodType: 'none', camundaVariable: '' },
      { icon: '✅', label: 'Completar Tarea', desc: 'API POST Complete', type: 'button_submit', placeholder: '', required: false, zodType: 'none', camundaVariable: '' },
      { icon: '❌', label: 'Rechazar Tarea', desc: 'BPMN Error', type: 'button_reject', placeholder: '', required: false, zodType: 'none', camundaVariable: '' },
    ]
  }
]);

// ── Draggable Clone Hook ─────────────────────────────────────────
let idCounter = 1;
const cloneComponent = (original: any) => {
  const cloned = JSON.parse(JSON.stringify(original));
  cloned.id = `FIELD_${idCounter++}`;
  cloned.camundaVariable = cloned.id.toLowerCase();
  cloned.stage = 'START_EVENT'; // Default
  if (cloned.type === 'container' || cloned.type === 'field_array') {
    cloned.children = [];
  }
  if (cloned.type === 'tabs') {
    cloned.children = [
      { id: `FIELD_${idCounter++}_tab1`, label: 'Tab 1', type: 'tab_pane', children: [] },
      { id: `FIELD_${idCounter++}_tab2`, label: 'Tab 2', type: 'tab_pane', children: [] }
    ];
    cloned.activeTab = 0;
  }
  if (cloned.type === 'accordion') {
    cloned.children = [
      { id: `FIELD_${idCounter++}_panel1`, label: 'Panel 1', type: 'accordion_panel', children: [] },
      { id: `FIELD_${idCounter++}_panel2`, label: 'Panel 2', type: 'accordion_panel', children: [] }
    ];
  }
  return cloned;
};

// ── Actions ──────────────────────────────────────────────────────
const selectPattern = (pattern: 'SIMPLE' | 'IFORM_MAESTRO') => {
  formPattern.value = pattern;
  showPatternModal.value = false;
  showToast(`Patrón ${pattern === 'SIMPLE' ? 'Simple' : 'iForm Maestro'} seleccionado.`);
};

const confirmReset = () => {
  showResetModal.value = true;
};

const executeReset = () => {
  canvasFields.value = [];
  editingField.value = null; // CA-59: Purge state local
  idCounter = 1;
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

// Flat extractor helper for recursion script generation
const flatFields = (fields: any[]): any[] => {
  let res: any[] = [];
  for (const f of fields) {
    if (f.type === 'container' || f.type === 'field_array') {
      if (f.children) res = res.concat(flatFields(f.children));
    } else {
      res.push(f);
    }
  }
  return res;
};

// HTML generator recursivo para Template (AST to Vue)
const generateFieldHTML = (field: any, indent: string = '      ', parentBinding: string = 'formData'): string => {
  let tpl = '';
  
  if (field.type.startsWith('button_')) {
      tpl += `${indent}<div class="mt-6 field-${field.id.toLowerCase()} no-print" v-if="(typeof isAuditMode === 'undefined' ? false : !isAuditMode) && (typeof stage === 'undefined' ? true : stage !== 'AUDIT')">\n`;
      if (field.type === 'button_submit') {
        tpl += `${indent}  <button type="submit" class="w-full bg-indigo-600 text-white py-2 rounded shadow font-bold hover:bg-indigo-700 transition flex items-center justify-center gap-2" :disabled="typeof isAsyncLoading !== 'undefined' && isAsyncLoading">✅ ${field.label}</button>\n`;
      } else if (field.type === 'button_draft') {
        tpl += `${indent}  <button type="button" @click="saveDraft" class="w-full border-2 border-dashed border-gray-300 text-gray-700 py-2 rounded shadow-sm font-bold hover:bg-gray-100 transition flex items-center justify-center gap-2" :disabled="typeof isAsyncLoading !== 'undefined' && isAsyncLoading">💾 ${field.label}</button>\n`;
      } else if (field.type === 'button_reject') {
        tpl += `${indent}  <button type="button" @click="rejectTask" class="w-full bg-red-600 text-white py-2 rounded shadow-sm font-bold hover:bg-red-700 transition mt-2 flex items-center justify-center gap-2" :disabled="typeof isAsyncLoading !== 'undefined' && isAsyncLoading">❌ ${field.label}</button>\n`;
      }
      tpl += `${indent}</div>\n`;
      return tpl;
  }

  let vIfDir = '';
  if (field.visibilityCondition) {
      if (formPattern.value === 'IFORM_MAESTRO') {
         vIfDir = `v-if="stage === '${field.stage}' && (${field.visibilityCondition})" `;
      } else {
         vIfDir = `v-if="${field.visibilityCondition}" `;
      }
  } else if (formPattern.value === 'IFORM_MAESTRO') {
      vIfDir = `v-if="stage === '${field.stage}'" `;
  }

  const vModelBase = parentBinding === 'formData' 
    ? `formData.${field.camundaVariable || field.id}` 
    : `row.${field.camundaVariable || field.id}`;

  if (field.type === 'container' || field.type === 'field_array') {
     let containerClass = `${field.type === 'field_array' ? 'border-2 border-indigo-100' : 'border'} rounded-md p-4 bg-gray-50 field-${field.id.toLowerCase()}`;
     if (field.type === 'container' && field.columns && field.columns > 1) {
         containerClass += ` grid grid-cols-${field.columns} gap-4`; // CA-55
     }
     tpl += `${indent}<div ${vIfDir}class="${containerClass}">\n`;
     tpl += `${indent}  <h3 class="font-bold text-md mb-4">${field.label || 'Sección'}</h3>\n`;
     
     if (field.type === 'field_array') {
        tpl += `${indent}  <div v-for="(row, index) in ${vModelBase}" :key="index" class="p-4 border border-gray-200 bg-white mb-3 rounded isolate relative">\n`;
        tpl += `${indent}    <button type="button" @click="${vModelBase}.splice(index, 1)" class="absolute top-2 right-2 text-red-500 hover:text-red-700 font-bold no-print" title="Eliminar Fila">🗑</button>\n`;
     }

     if (field.children && field.children.length > 0) {
       for(const child of field.children) {
         if (field.type === 'field_array') tpl += generateFieldHTML(child, indent + '    ', 'row');
         else tpl += generateFieldHTML(child, indent + '  ', parentBinding);
       }
     }
     
     if (field.type === 'field_array') {
        tpl += `${indent}  </div>\n`;
        tpl += `${indent}  <button type="button" @click="${vModelBase}.push({})" class="text-sm border-2 border-dashed border-indigo-300 text-indigo-700 px-4 py-2 rounded hover:bg-indigo-50 font-bold w-full mt-2 no-print">[+ Agregar Fila]</button>\n`;
     }
     tpl += `${indent}</div>\n`;
  } else {
    tpl += `${indent}<div ${vIfDir}class="field-${field.id.toLowerCase()}">\n`;
    const ttip = field.tooltipText ? ` <span title="${field.tooltipText}" class="cursor-help text-indigo-500 font-bold ml-1 text-xs outline-none">ⓘ</span>` : '';
    tpl += `${indent}  <label class="block text-sm font-medium text-gray-700">${field.label}${field.required ? '*' : ''}${ttip}</label>\n`;
    
    // CA-56 Print Mode Wrapper
    tpl += `${indent}  <div v-if="!isPrintMode">\n`;

    const dsbObj = formPattern.value === 'IFORM_MAESTRO' ? `isAuditMode || stage === 'AUDIT' || (stage !== '${field.stage}' && ${field.soloLecturaPosterior || false})` : `isAuditMode`;
    const finalDsbObj = field.disableCondition ? `(${dsbObj}) || (${field.disableCondition})` : dsbObj; // CA-57
    const dsb = parentBinding === 'row' ? ` :disabled="${finalDsbObj} || row._locked"` : ` :disabled="${finalDsbObj}"`; // CA-51 Grid Locked Rows
    
    if (field.type === 'text' || field.type === 'number' || field.type === 'date' || field.type === 'time' || field.type === 'password' || field.type === 'email' || field.type === 'url') { // CA-53, CA-63
      if (field.mask) {
         // CA-36: Proxy Value/Event Masking
         tpl += `${indent}  <input type="${field.type === 'password' ? 'password' : 'text'}" :value="formatMask(${vModelBase}, '${field.mask}')" @change="(e) => { ${vModelBase} = unmask((e.target as HTMLInputElement).value, '${field.type}'); validateField('${field.camundaVariable || field.id}'); }" placeholder="${field.placeholder || field.mask}" class="form-input mt-1 w-full rounded-md border-gray-300 shadow-sm font-mono"${dsb} />\n`;
      } else {
         const nativeType = (field.type === 'email' || field.type === 'url' || field.type === 'password') ? field.type : field.type;
         tpl += `${indent}  <input type="${nativeType}" v-model.lazy="${vModelBase}" @blur="validateField('${field.camundaVariable || field.id}')" placeholder="${field.placeholder || ''}" class="form-input mt-1 w-full rounded-md border-gray-300 shadow-sm"${dsb} />\n`;
      }
      if (field.type === 'password') {
         // CA-64 Hints Multi-Estado
         tpl += `${indent}  <div class="mt-1 text-xs px-1 space-y-1 font-mono font-medium" v-if="${vModelBase}">\n`;
         tpl += `${indent}    <p :class="${vModelBase}.length >= 8 ? 'text-green-600' : 'text-gray-500'">Mínimo 8 caracteres {{${vModelBase}.length >= 8 ? '✅' : '❌'}}</p>\n`;
         tpl += `${indent}    <p :class="/[A-Z]/.test(${vModelBase}) ? 'text-green-600' : 'text-gray-500'">1 Mayúscula {{/[A-Z]/.test(${vModelBase}) ? '✅' : '❌'}}</p>\n`;
         tpl += `${indent}  </div>\n`;
      }
    } else if (field.type === 'textarea') {
      tpl += `${indent}  <textarea v-model.lazy="${vModelBase}" @blur="validateField('${field.camundaVariable || field.id}')" placeholder="${field.placeholder || ''}" class="form-input mt-1 w-full rounded-md border-gray-300 shadow-sm" rows="3"${dsb}></textarea>\n`;
    } else if (field.type === 'checkbox') {
      tpl += `${indent}  <div class="flex items-center gap-2 mt-1">\n${indent}    <input type="checkbox" v-model="${vModelBase}" class="rounded text-indigo-600 border-gray-300 focus:ring-indigo-500 shadow-sm"${dsb} />\n${indent}    <span class="text-sm text-gray-700">${field.placeholder || field.label}</span>\n${indent}  </div>\n`;
    } else if (field.type === 'radio') {
      tpl += `${indent}  <div class="flex flex-col gap-1 mt-1">\n${(field.options || ['Opción 1', 'Opción 2']).map((o:string) => `${indent}    <label class="flex items-center gap-2"><input type="radio" value="${o}" v-model="${vModelBase}" class="text-indigo-600 border-gray-300 focus:ring-indigo-500 shadow-sm"${dsb} /> <span class="text-sm text-gray-600 font-medium">${o}</span></label>`).join('\n')}\n${indent}  </div>\n`;
    } else if (field.type === 'select' || field.type === 'async_select') {
       if (field.isMultiple) {
           // CA-45: Multi Select Chips
           tpl += `${indent}  <div class="relative">\n`;
           if (field.type === 'select') {
               tpl += `${indent}    <input list="list-${field.id}" @change="(e) => { const val = (e.target as HTMLInputElement).value; if(val && !${vModelBase}.includes(val)) { ${vModelBase}.push(val); (e.target as HTMLInputElement).value=''; } }" placeholder="${field.placeholder || 'Seleccione múltiple...'}" class="form-input mt-1 w-full rounded-md border-gray-300 shadow-sm"${dsb} />\n`;
               tpl += `${indent}    <datalist id="list-${field.id}">\n${(field.options || ['Opción 1', 'Opción 2']).map((o:string) => `${indent}      <option value="${o}">${o}</option>`).join('\n')}\n${indent}    </datalist>\n`;
           } else {
               tpl += `${indent}    <input list="list-${field.id}" @input="(e) => fetchAsyncOpts_${field.id}((e.target as HTMLInputElement).value)" @change="(e) => { const val = (e.target as HTMLInputElement).value; if(val && !${vModelBase}.includes(val)) { ${vModelBase}.push(val); (e.target as HTMLInputElement).value=''; } }" placeholder="${field.placeholder || 'Buscando en servidor...'}" class="form-input mt-1 w-full rounded-md border-gray-300 shadow-sm"${dsb} />\n`;
               tpl += `${indent}    <datalist id="list-${field.id}">\n${indent}      <option v-for="opt in asyncOpts_${field.id}" :key="opt" :value="opt"></option>\n${indent}    </datalist>\n`;
           }
           tpl += `${indent}    <div class="flex flex-wrap gap-2 mt-2">\n`;
           tpl += `${indent}       <span v-for="(chip, idx) in ${vModelBase}" :key="chip" class="bg-indigo-100 text-indigo-800 text-xs px-2 py-1 rounded-full flex items-center gap-1 shadow-sm">\n`;
           tpl += `${indent}         {{ chip }}\n`;
           tpl += `${indent}         <button type="button" @click="${vModelBase}.splice(idx, 1)" class="font-bold hover:text-indigo-900 border-l border-indigo-200 pl-1 ml-1"${dsb}>&times;</button>\n`;
           tpl += `${indent}       </span>\n`;
           tpl += `${indent}    </div>\n`;
           tpl += `${indent}  </div>\n`;
       } else {
           if (field.type === 'select') {
               tpl += `${indent}  <input list="list-${field.id}" v-model="${vModelBase}" placeholder="${field.placeholder || 'Seleccione...'}" class="form-input mt-1 w-full rounded-md border-gray-300 shadow-sm"${dsb} />\n`;
               tpl += `${indent}  <datalist id="list-${field.id}">\n${(field.options || ['Opción 1', 'Opción 2']).map((o:string) => `${indent}    <option value="${o}">${o}</option>`).join('\n')}\n${indent}  </datalist>\n`;
           } else {
               tpl += `${indent}  <input list="list-${field.id}" @input="(e) => fetchAsyncOpts_${field.id}((e.target as HTMLInputElement).value)" v-model="${vModelBase}" placeholder="${field.placeholder || 'Buscando en servidor...'}" class="form-input mt-1 w-full rounded-md border-gray-300 shadow-sm"${dsb} />\n`;
               tpl += `${indent}  <datalist id="list-${field.id}">\n${indent}    <option v-for="opt in asyncOpts_${field.id}" :key="opt" :value="opt"></option>\n${indent}  </datalist>\n`;
           }
       }
    } else if (field.type === 'file') {
       const uTarget = parentBinding === 'formData' ? 'formData.value' : parentBinding;
       // CA-39: Binding de MaxSizeMb y AllowedExts
       const maxMb = field.maxSizeMb || 0;
       const exts = field.allowedExts || '';
       const minFs = field.minFiles || 0;
       const maxFs = field.maxFiles || 1;
       const multAttr = maxFs > 1 ? ' multiple' : '';
       
       // CA-60 Dropzone wrapper
       tpl += `${indent}  <div class="border-2 border-dashed border-gray-300 rounded-lg p-6 bg-gray-50 hover:bg-gray-100 transition text-center cursor-pointer relative" @dragover.prevent @drop.prevent="(e) => dropFile(e, '${field.camundaVariable || field.id}', ${uTarget}, ${maxMb}, '${exts}', ${minFs}, ${maxFs})">\n`;
       tpl += `${indent}     <span class="text-3xl mb-2 block">📥</span>\n`;
       tpl += `${indent}     <p class="text-sm font-bold text-gray-700">Arrastre archivos aquí (CA-60)</p>\n`;
       tpl += `${indent}     <p class="text-xs text-gray-500 mt-1 mb-3">o haga clic para seleccionar desde el navegador.</p>\n`;
       tpl += `${indent}     <input type="file" @change="(e) => uploadFile(e, '${field.camundaVariable || field.id}', ${uTarget}, ${maxMb}, '${exts}', ${minFs}, ${maxFs})" class="absolute inset-0 w-full h-full opacity-0 cursor-pointer no-print"${dsb}${multAttr} />\n`;
       tpl += `${indent}     <div v-if="${vModelBase}" class="mt-2 text-xs text-indigo-700 bg-indigo-50 py-1 px-2 rounded font-bold break-all border border-indigo-200">\n`;
       tpl += `${indent}       Archivo(s): {{ Array.isArray(${vModelBase}) ? ${vModelBase}.join(', ') : ${vModelBase} }}\n`;
       tpl += `${indent}     </div>\n`;
       tpl += `${indent}  </div>\n`;
    } else if (field.type === 'signature') {
       const oTarget = parentBinding === 'formData' ? 'formData.value' : parentBinding;
       tpl += `${indent}  <div class="border rounded bg-white p-2 mt-1">\n`;
       tpl += `${indent}    <canvas :id="'canvas_' + '${field.id}'" width="400" height="200" class="border border-gray-300 bg-gray-50 cursor-crosshair w-full" @mousedown="startSig($event, '${field.id}')" @mousemove="drawSig($event, '${field.id}')" @mouseup="endSig('${field.id}', '${field.camundaVariable || field.id}', ${oTarget})" @mouseleave="endSig('${field.id}', '${field.camundaVariable || field.id}', ${oTarget})" @touchstart="startSig($event, '${field.id}')" @touchmove="drawSig($event, '${field.id}')" @touchend="endSig('${field.id}', '${field.camundaVariable || field.id}', ${oTarget})"></canvas>\n`;
       tpl += `${indent}    <div class="flex justify-between mt-2 no-print">\n`;
       tpl += `${indent}       <button type="button" @click="clearSig('${field.id}', '${field.camundaVariable || field.id}', ${oTarget})" class="text-xs text-red-500 font-bold">Limpiar Firma</button>\n`;
       tpl += `${indent}       <span class="text-[10px] text-gray-400">Dibuja en el recuadro superior</span>\n`;
       tpl += `${indent}    </div>\n`;
       tpl += `${indent}  </div>\n`;
    } else if (field.type === 'timer') {
       if (field.timerMode === 'manual') {
          tpl += `${indent}  <div class="flex items-center gap-2 mt-1">\n`;
          tpl += `${indent}    <span class="text-xl font-mono bg-gray-100 px-3 py-1 rounded border">{{ ${vModelBase} || 0 }}s</span>\n`;
          tpl += `${indent}    <button type="button" @click="toggleTimer('${field.camundaVariable || field.id}', ${parentBinding === 'formData' ? 'formData.value' : 'row'})" class="bg-indigo-50 text-indigo-700 px-3 py-1 rounded text-xs font-bold hover:bg-indigo-100 transition no-print"${dsb}>▶/⏸</button>\n`;
          tpl += `${indent}    <button type="button" @click="resetTimer('${field.camundaVariable || field.id}', ${parentBinding === 'formData' ? 'formData.value' : 'row'})" class="bg-red-50 text-red-700 px-2 py-1 rounded text-xs font-bold hover:bg-red-100 transition no-print"${dsb}>↺</button>\n`;
          tpl += `${indent}  </div>\n`;
       } else {
          tpl += `${indent}  <div class="text-xs text-gray-500 italic flex items-center gap-1 mt-1">\n`;
          tpl += `${indent}    <span class="animate-pulse">⏱️</span> Cronómetro en segundo plano... ({{ ${vModelBase} || 0 }}s)\n`;
          tpl += `${indent}  </div>\n`;
       }
    } else if (field.type === 'gps') {
       const uTarget = parentBinding === 'formData' ? 'formData.value' : parentBinding;
       tpl += `${indent}  <div class="flex gap-2 mt-1">\n`;
       tpl += `${indent}    <input type="text" v-model="${vModelBase}" readonly placeholder="Coordenadas GPS (Lat, Lng)" class="form-input flex-1 rounded-md border-gray-300 shadow-sm bg-gray-100 italic"${dsb} />\n`;
       tpl += `${indent}    <button type="button" @click="captureGPS('${field.camundaVariable || field.id}', ${uTarget})" class="bg-indigo-600 text-white px-4 py-2 rounded shadow font-bold hover:bg-indigo-700 transition flex gap-1 items-center whitespace-nowrap"${dsb}>📌 Capturar GPS</button>\n`;
       tpl += `${indent}  </div>\n`;
    } else if (field.type === 'qr') {
       const uTarget = parentBinding === 'formData' ? 'formData.value' : parentBinding;
       tpl += `${indent}  <div class="flex gap-2 mt-1">\n`;
       tpl += `${indent}    <input type="text" v-model="${vModelBase}" placeholder="Valor escaneado (CA-62)" class="form-input flex-1 rounded-md border-gray-300 shadow-sm"${dsb} />\n`;
       tpl += `${indent}    <button type="button" @click="scanQR('${field.camundaVariable || field.id}', ${uTarget})" class="bg-teal-600 text-white px-4 py-2 rounded shadow font-bold hover:bg-teal-700 transition flex gap-1 items-center whitespace-nowrap"${dsb}>📷 Escanear QR</button>\n`;
       tpl += `${indent}  </div>\n`;
    } else if (field.type === 'hidden') {
       // CA-47: Componente Oculto Silencioso
       tpl += `${indent}  <input type="hidden" v-model="${vModelBase}" id="${field.camundaVariable || field.id}" />\n`;
    } else {
       tpl += `${indent}  <!-- Custom Component: ${field.type} -->\n`;
    }
    
    // CA-56 Print Mode Fallback
    tpl += `${indent}  </div>\n`; // End v-if !isPrintMode
    if (field.type !== 'hidden') {
        tpl += `${indent}  <div v-else class="text-sm text-gray-800 font-medium py-1 px-2 mb-1 mt-1 bg-white border-b border-dashed border-gray-300 min-h-[30px]">\n`;
        if (field.type === 'password') {
           tpl += `${indent}    <span class="text-gray-400 italic">*** Oculto ***</span>\n`;
        } else if (field.type === 'file') {
           tpl += `${indent}    <a v-if="${vModelBase}" :href="${vModelBase}" target="_blank" class="text-blue-600 underline">📎 Adjunto</a>\n`;
        } else if (field.type === 'signature') {
           tpl += `${indent}    <img v-if="${vModelBase}" :src="${vModelBase}" class="max-h-16" />\n`;
        } else if (field.type === 'checkbox') {
           tpl += `${indent}    <span>{{ ${vModelBase} ? '☑ Sí' : '☐ No' }}</span>\n`;
        } else if (field.type === 'timer') {
           tpl += `${indent}    <span>{{ ${vModelBase} || 0 }} seg.</span>\n`;
        } else {
           tpl += `${indent}    <span class="whitespace-pre-wrap">{{ Array.isArray(${vModelBase}) ? ${vModelBase}.join(', ') : (${vModelBase} || '---') }}</span>\n`;
        }
        tpl += `${indent}  </div>\n`;
    }
    
    // CA-28 Auditoria Forense Check
    if (field.enableAuditLog && field.type !== 'hidden') {
       tpl += `${indent}  <p class="text-[9px] text-gray-400 mt-1 uppercase tracking-wider font-mono">Modificado por: {{ currentUser?.name || 'Sistema' }}</p>\n`;
    }

    if (field.type !== 'hidden') {
       tpl += `${indent}  <span v-if="errors.${field.camundaVariable || field.id}" class="text-red-500 text-xs">{{ errors.${field.camundaVariable || field.id} }}</span>\n`;
    }
    tpl += `${indent}</div>\n`;
  }
  return tpl;
};

// ── Generators & Parsers (Bidireccional AST-Sandbox) ────────────────────────
const computedCode = computed({
  get: () => {
    if (activeCodeTab.value === 'TEMPLATE') {
      let tpl = `<template>\n  <form @submit.prevent="submitTask" class="space-y-4">`;
      tpl += `\n    <!-- CA-46: Sello Visual de Aprobatoria (Si existe en prefillData) -->\n    <div v-if="props.prefillData?.approvedBy" class="bg-green-50 border border-green-200 text-green-800 p-3 rounded-md flex items-center gap-3 no-print">\n      <span class="text-2xl">✅</span>\n      <div>\n        <p class="text-sm font-bold">Fase Aprobada Anteriomente</p>\n        <p class="text-xs">Revisor: {{ props.prefillData.approvedBy }}</p>\n      </div>\n    </div>\n`;
      if (canvasFields.value.length === 0) {
        tpl += `\n    <!-- Arrastra componentes al lienzo -->`;
      } else {
        for (const field of canvasFields.value) {
          tpl += generateFieldHTML(field, '    ');
        }
      }
      
      const hasSubmit = flatFields(canvasFields.value).some(f => f.type === 'button_submit');
      if (!hasSubmit && canvasFields.value.length > 0) {
          tpl += `\n    <button type="submit" class="w-full bg-blue-600 text-white font-bold py-2 rounded shadow hover:bg-blue-700 transition mt-6">Enviar Tarea (Auto)</button>`;
      }
      tpl += `\n  </form>\n</template>`;
      return tpl;
    } 
    
    if (activeCodeTab.value === 'SCRIPT') {
      let scr = `<script setup lang="ts">\nimport { ref, inject, watch, onMounted, onUnmounted } from 'vue';\nimport { z } from 'zod';\nimport { taskSchema } from './schema.zod.ts';\nimport apiClient from '@/services/apiClient';\n\n`;
      if (formPattern.value === 'IFORM_MAESTRO') {
        scr += `// IFORM_MAESTRO: Inyección de Etapa BPMN actual (Dual-Pattern CA-2)\nconst stage = inject('camunda_process_stage', 'START_EVENT');\n\n`;
      }
      
      scr += `// CA-37: Visor Histórico Inmutable para Auditoría\nconst isAuditMode = ref(false); // Cambiar a true si es histórico\n\n`;
      
      scr += `// CA-43: Recepción de Datos Precargados (BFF Pattern)\nconst props = defineProps<{ prefillData?: Record<string, any> }>();\n\n`;
      scr += `// CA-52: Control Asíncrono Global\nconst isAsyncLoading = ref(false);\n\n`;
      scr += `// CA-56: Modo Lectura Print/PDF\nconst isPrintMode = ref(false);\n\n`;
      
      const hasAudit = flatFields(canvasFields.value).some(f => f.enableAuditLog);
      if (hasAudit) {
         scr += `// Auditoría (CA-28): Injection Dummy de Usuario Actual\nconst currentUser = ref({ name: 'Admin Demo' });\n\n`;
      }

      const asyncFields = flatFields(canvasFields.value).filter(f => f.type === 'async_select' && f.asyncUrl);
      for (const field of asyncFields) {
         scr += `const asyncOpts_${field.id} = ref<string[]>([]);\n`;
         scr += `const fetchAsyncOpts_${field.id} = async (query: string) => {\n   if(query.trim().length === 0) { asyncOpts_${field.id}.value = []; return; }\n   try {\n      isAsyncLoading.value = true;\n      const res = await apiClient.get(\`${field.asyncUrl}?q=\${query}\`);\n      asyncOpts_${field.id}.value = Array.isArray(res.data) ? res.data.map(i => i.label || i.nombre || i.name || JSON.stringify(i)) : [];\n   } catch (e) { console.error('Typeahead Error (CA-30)', e); } finally { isAsyncLoading.value = false; }\n};\n\n`;
      }

      scr += `const formData = ref<Record<string, any>>({\n`;
      const directFields = canvasFields.value.filter(f => !f.type.startsWith('button_') && f.type !== 'container');
      for (const field of directFields) {
        if (field.type === 'field_array') {
           scr += `  ${field.camundaVariable || field.id}: [], // Grilla CA-34\n`;
        } else {
           let def = "''";
           if (field.type === 'number') def = 'null';
           if (field.type === 'checkbox') def = 'false';
           if (field.isMultiple && ['select', 'async_select'].includes(field.type)) def = '[]';
           scr += `  ${field.camundaVariable || field.id}: ${def}, // Binding CA-12/13\n`;
        }
      }
      scr += `});\n\nconst errors = ref<Record<string, string>>({});\n`;
      scr += `const taskId = 'MOCK_TASK_ID'; // Inyectar ID real\n\n`;
      scr += `// CA-43: Auto-map Pre-fill Binding\nonMounted(() => {\n  if (props.prefillData) {\n    for (const key in props.prefillData) {\n      if (key in formData.value) {\n        formData.value[key] = props.prefillData[key];\n      }\n    }\n  }\n});\n\n`;

      const hasDraft = flatFields(canvasFields.value).some(f => f.type === 'button_draft');
      const hasReject = flatFields(canvasFields.value).some(f => f.type === 'button_reject');
      const hasFile = flatFields(canvasFields.value).some(f => f.type === 'file');
      const hasGPS = flatFields(canvasFields.value).some(f => f.type === 'gps'); // CA-61
      const hasQR = flatFields(canvasFields.value).some(f => f.type === 'qr'); // CA-62
      const hasSignature = flatFields(canvasFields.value).some(f => f.type === 'signature');
      const hasMask = flatFields(canvasFields.value).some(f => f.mask);

      if (hasMask) {
         scr += `// CA-36: Enmascaramiento Dinámico Frontend-Only\n`;
         scr += `const formatMask = (val: string|number|null, _mask: string) => { if (val == null) return ''; return val.toString(); /* Inyección futura de libreria regex-mask */};\n`;
         scr += `const unmask = (val: string, type: string) => { const raw = val.replace(/[^a-zA-Z0-9.\-@:]/g, ''); return type === 'number' ? parseFloat(raw)||null : raw; };\n\n`;
      }

      if (hasSignature) {
         scr += `// CA-31: Signature HTML5 Canvas Engine\n`;
         scr += `const sigState = ref<Record<string, {isDrawing: boolean, ctx: CanvasRenderingContext2D | null}>>({});\n`;
         scr += `const getCtx = (id: string, canvas: HTMLCanvasElement) => {\n  if(!sigState.value[id]) { sigState.value[id] = { isDrawing: false, ctx: canvas.getContext('2d') }; if(sigState.value[id].ctx) { sigState.value[id].ctx!.lineWidth = 2; sigState.value[id].ctx!.lineCap = 'round'; sigState.value[id].ctx!.strokeStyle = '#000'; } }\n  return sigState.value[id];\n};\n`;
         scr += `const startSig = (e: any, id: string) => { e.preventDefault(); const canvas = e.target as HTMLCanvasElement; const st = getCtx(id, canvas); if(!st.ctx) return; st.isDrawing = true; st.ctx.beginPath(); const rect = canvas.getBoundingClientRect(); const x = (e.clientX || e.touches?.[0].clientX) - rect.left; const y = (e.clientY || e.touches?.[0].clientY) - rect.top; st.ctx.moveTo(x, y); };\n`;
         scr += `const drawSig = (e: any, id: string) => { e.preventDefault(); const canvas = e.target as HTMLCanvasElement; const st = getCtx(id, canvas); if(!st || !st.isDrawing || !st.ctx) return; const rect = canvas.getBoundingClientRect(); const x = (e.clientX || e.touches?.[0].clientX) - rect.left; const y = (e.clientY || e.touches?.[0].clientY) - rect.top; st.ctx.lineTo(x, y); st.ctx.stroke(); };\n`;
         scr += `const endSig = (id: string, varName: string, targetObj: any) => { const st = sigState.value[id]; if(!st || !st.isDrawing) return; st.isDrawing = false; const canvas = document.getElementById('canvas_' + id) as HTMLCanvasElement; if(canvas) { targetObj[varName] = canvas.toDataURL('image/png'); } };\n`;
         scr += `const clearSig = (id: string, varName: string, targetObj: any) => { const canvas = document.getElementById('canvas_' + id) as HTMLCanvasElement; if(canvas) { const ctx = canvas.getContext('2d'); ctx?.clearRect(0,0, canvas.width, canvas.height); targetObj[varName] = ''; } };\n\n`;
      }

      scr += `// CA-24: Auto-Guardado Workdesk LocalStorage/API\nlet autoSyncDraftTimeout: any = null;\nwatch(formData, (newVal) => {\n  clearTimeout(autoSyncDraftTimeout);\n  autoSyncDraftTimeout = setTimeout(async () => {\n    try {\n      await apiClient.post('/api/v1/forms/draft', newVal);\n      console.log('✅ Borrador auto-guardado en backend');\n    } catch (e) {\n      localStorage.setItem('workdesk_draft', JSON.stringify(newVal));\n      console.warn('⚠️ Fallback a LocalStorage para auto-guardado');\n    }\n  }, 2000);\n}, { deep: true });\n\n`;

      if (hasFile) {
         scr += `// CA-21, CA-39, CA-49: Conector Multipart File Upload + Constraints\nconst uploadFile = async (event: any, fieldId: string, targetObj: any, maxMb: number, exts: string, minFiles: number, maxFiles: number) => {\n  const target = event.target;\n  const files = target?.files;\n  if (!files || files.length === 0) return;\n  if (files.length < minFiles) { alert('Mínimo ' + minFiles + ' archivo(s) requeridos.'); target.value = ''; return; }\n  if (files.length > maxFiles) { alert('Máximo ' + maxFiles + ' archivo(s) permitidos.'); target.value = ''; return; }\n  let urls: string[] = [];\n  for (let i = 0; i < files.length; i++) {\n     const file = files[i];\n     if (maxMb > 0 && file.size > maxMb * 1024 * 1024) { alert('El archivo \\'' + file.name + '\\' excede el límite de ' + maxMb + 'MB.'); target.value = ''; return; }\n     if (exts) { const ext = '.' + file.name.split('.').pop()?.toLowerCase(); if (!exts.toLowerCase().includes(ext)) { alert('Extensión ' + ext + ' no permitida. Solo: ' + exts); target.value = ''; return; } }\n     const data = new FormData();\n     data.append('file', file);\n     try {\n       const res = await apiClient.post('/api/v1/forms/upload', data, { headers: { 'Content-Type': 'multipart/form-data' } });\n       urls.push(res.data.url || 'subido_exitosamente_' + i);\n     } catch (error) {\n       alert('Error subiendo \\'' + file.name + '\\': ' + (error as any).message);\n       return;\n     }\n  }\n  targetObj[fieldId] = urls.length > 1 ? JSON.stringify(urls) : urls[0];\n  alert('Archivo(s) subido(s) exitosamente');\n};\n\n`;
         scr += `// CA-60: Manejador Drag & Drop Dropzone\nconst dropFile = (event: any, fieldId: string, targetObj: any, maxMb: number, exts: string, minFiles: number, maxFiles: number) => {\n  const dt = event.dataTransfer;\n  if (dt && dt.files && dt.files.length > 0) {\n     uploadFile({ target: { files: dt.files } }, fieldId, targetObj, maxMb, exts, minFiles, maxFiles);\n  }\n};\n\n`;
      }

      if (hasGPS) {
         scr += `// CA-61: Embebido HTML5 GPS Geolocation\nconst captureGPS = (fieldId: string, targetObj: any) => {\n  if (!navigator.geolocation) { alert('Geolocalización no soportada en este navegador.'); return; }\n  navigator.geolocation.getCurrentPosition(\n    (pos) => { targetObj[fieldId] = \`Lat: \${pos.coords.latitude}, Lng: \${pos.coords.longitude}\`; },\n    (err) => { alert('Error obteniendo ubicación: ' + err.message); },\n    { enableHighAccuracy: true }\n  );\n};\n\n`;
      }

      if (hasQR) {
         scr += `// CA-62: WebRTC QR Scanner Mock/Dummy\nconst scanQR = (fieldId: string, targetObj: any) => {\n  // Para paso a producción requeriría importar librería de escaneo webRTC\n  const val = prompt('📸 [Simulador QR] Ingrese el resultado del Escaneo:', 'QR-MOCK-7788');\n  if (val) targetObj[fieldId] = val;\n};\n\n`;
      }


      const timers = flatFields(canvasFields.value).filter(f => f.type === 'timer');
      if (timers.length > 0) {
         scr += `// CA-58: Lógica de Cronómetros de Telemetría\n`;
         scr += `const timerIntervals: Record<string, ReturnType<typeof setInterval>> = {};\n`;
         scr += `const isTimerActive: Record<string, boolean> = {};\n`;
         scr += `const toggleTimer = (key: string, targetObj: any) => {\n`;
         scr += `  if (isTimerActive[key]) {\n`;
         scr += `     clearInterval(timerIntervals[key]);\n`;
         scr += `     isTimerActive[key] = false;\n`;
         scr += `  } else {\n`;
         scr += `     isTimerActive[key] = true;\n`;
         scr += `     if (typeof targetObj[key] !== 'number') targetObj[key] = 0;\n`;
         scr += `     timerIntervals[key] = setInterval(() => { targetObj[key]++; }, 1000);\n`;
         scr += `  }\n`;
         scr += `};\n`;
         scr += `const resetTimer = (key: string, targetObj: any) => {\n`;
         scr += `  clearInterval(timerIntervals[key]);\n`;
         scr += `  isTimerActive[key] = false;\n`;
         scr += `  targetObj[key] = 0;\n`;
         scr += `};\n`;
         const autoTimers = timers.filter(t => t.timerMode === 'background');
         if (autoTimers.length > 0) {
             scr += `onMounted(() => {\n`;
             for (const t of autoTimers) {
                 const key = t.camundaVariable || t.id;
                 scr += `  if (typeof formData.value['${key}'] !== 'number') formData.value['${key}'] = 0;\n`;
                 scr += `  timerIntervals['${key}'] = setInterval(() => { formData.value['${key}']++; }, 1000);\n`;
                 scr += `  isTimerActive['${key}'] = true;\n`;
             }
             scr += `});\n`;
         }
         scr += `onUnmounted(() => {\n`;
         scr += `  Object.values(timerIntervals).forEach(clearInterval);\n`;
         scr += `});\n\n`;
      }

      let phantomLogic = '';
      const fieldsWithCond = flatFields(canvasFields.value).filter(f => f.visibilityCondition || (f.requiredIfField && f.requiredIfValue));
      if (fieldsWithCond.length > 0) {
         phantomLogic += `  // CA-54: Purga de Phantom Data (Ocultos/Condicionales)\n`;
         for (const f of fieldsWithCond) {
             const key = f.camundaVariable || f.id;
             let condStr = '';
             let hasVis = false;
             if (f.visibilityCondition) {
                condStr += `!(${f.visibilityCondition.replace(/formData\./g, 'cleanData.')})`;
                hasVis = true;
             }
             if (f.requiredIfField && f.requiredIfValue) {
                if (hasVis) condStr += ' || ';
                condStr += `!(cleanData.${f.requiredIfField} === '${f.requiredIfValue}')`;
             }
             phantomLogic += `  if (${condStr}) { delete cleanData['${key}']; }\n`;
         }
      }

      scr += `// CA-22: Lazy Zod Validation\n`;
      scr += `const validateField = (fieldId: string) => {\n  const cleanData = JSON.parse(JSON.stringify(formData.value));\n  Object.keys(cleanData).forEach(k => { if (typeof cleanData[k] === 'string' && /^[\\d.,$]+$/.test(cleanData[k])) { const num = parseFloat(cleanData[k].replace(/[^\\d.-]/g, '')); if(!isNaN(num)) cleanData[k] = num; } });\n  const result = taskSchema.safeParse(cleanData);\n  if (!result.success) {\n    const issue = result.error.issues.find(iss => iss.path[0] === fieldId);\n    if (issue) errors.value[fieldId] = issue.message;\n    else delete errors.value[fieldId];\n  } else {\n    delete errors.value[fieldId];\n  }\n};\n\n`;

      scr += `// CA-15, CA-50: Smart Actions con Blindaje y Stripping Numerico\n`;
      scr += `const submitTask = async () => {\n  errors.value = {};\n`;
      scr += `  // CA-50: Stripping Silencioso de formato Numérico\n  const cleanData = JSON.parse(JSON.stringify(formData.value));\n`;
      scr += `  Object.keys(cleanData).forEach(k => { if (typeof cleanData[k] === 'string' && /^[\\d.,$]+$/.test(cleanData[k])) { const num = parseFloat(cleanData[k].replace(/[^\\d.-]/g, '')); if(!isNaN(num)) cleanData[k] = num; } });\n\n`;
      if (phantomLogic) scr += phantomLogic + '\n';
      scr += `  const result = taskSchema.safeParse(cleanData);\n  if (!result.success) {\n    result.error.issues.forEach(iss => {\n      if (iss.path[0]) errors.value[iss.path[0].toString()] = iss.message;\n    });\n    return;\n  }\n  try {\n    const payload = { variables: result.data };\n    await apiClient.post(\`/engine-rest/task/\${taskId}/complete\`, payload, { headers: { 'If-Match': props.prefillData?.versionId || '' } });\n    alert('Tarea Completada (Success)');\n  } catch (error: any) {\n    if (error.response?.status >= 500) {\n      localStorage.setItem('workdesk_draft_fallback', JSON.stringify(cleanData));\n      alert('⚠️ Error 5xx en servidor. Borrador protegido en LocalStorage y postergado (Offline Fallback CA-72).');\n    } else {\n      alert('Excepción de Red al Completar Tarea: ' + error.message);\n    }\n  }\n};\n`;
      
      if (hasDraft) {
        scr += `\nconst saveDraft = async () => {\n  try {\n    const cleanData = JSON.parse(JSON.stringify(formData.value));\n    Object.keys(cleanData).forEach(k => { if (typeof cleanData[k] === 'string' && /^[\\d.,$]+$/.test(cleanData[k])) { const num = parseFloat(cleanData[k].replace(/[^\\d.-]/g, '')); if(!isNaN(num)) cleanData[k] = num; } });\n`;
        if (phantomLogic) scr += phantomLogic;
        scr += `    await apiClient.post('/api/v1/forms/draft', cleanData, { headers: { 'If-Match': props.prefillData?.versionId || '' } });\n    alert('Borrador Guardado (Success)');\n  } catch (error: any) {\n    if (error.response?.status >= 500) {\n      localStorage.setItem('workdesk_draft_fallback', JSON.stringify(cleanData));\n      alert('⚠️ Error 5xx en servidor. Borrador protegido en LocalStorage (Offline Fallback CA-72).');\n    } else {\n      alert('Excepción de Red al Guardar Borrador: ' + error.message);\n    }\n  }\n};\n`;
      }
      if (hasReject) {
         scr += `\nconst rejectTask = async () => {\n  try {\n    await apiClient.post(\`/engine-rest/task/\${taskId}/bpmnError\`, { errorCode: 'REJECTED' });\n    alert('Excepción BPMN Disparada (Success)');\n  } catch (error) {\n    alert('Excepción de Red al Rechazar Tarea: ' + (error as any).message);\n  }\n};\n`;
      }

      scr += `<\/script>`;
      return scr;
    }

    if (activeCodeTab.value === 'STYLE') {
      return `<style scoped>\n/* Estilos inyectados por el motor Zod O-T-F (CA-5) */\n.form-input {\n  @apply w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500;\n}\n.form-select {\n  @apply w-full rounded-md border-gray-300 shadow-sm;\n}\n</style>`;
    }

    if (activeCodeTab.value === 'ZOD') {
      const walkNode = (fieldsArr: any[], isRoot: boolean): string => {
         let zc = `z.object({\n`;
         for(const field of fieldsArr) {
            if(field.type.startsWith('button_') || field.type === 'container') continue;
            if(field.type === 'field_array') {
                if(!field.children || field.children.length === 0) continue;
                let arrCode = `z.array(${walkNode(field.children, false)})`;
                if(field.minRows) arrCode += `.min(${field.minRows}, "Mínimo ${field.minRows} filas")`;
                if(field.maxRows) arrCode += `.max(${field.maxRows}, "Máximo ${field.maxRows} filas")`;
                zc += `  ${field.camundaVariable || field.id}: ${arrCode}, // [GRILLA CA-41]\n`;
                continue;
            }
            let zt = 'string';
            if(field.type === 'number') zt = 'number';
            if(field.type === 'checkbox') zt = 'boolean';
            
            let piiMod = field.isPII ? `.describe('isPII')` : ``;

            if (field.isMultiple && ['select', 'async_select'].includes(field.type)) {
                zc += `  ${field.camundaVariable || field.id}: z.array(z.string())${field.required ? '.min(1, "Seleccione opción")' : '.optional()'}${piiMod}, // [${field.stage || 'GLOBAL'}]\n`;
            } else if (field.type === 'file' || field.type === 'signature') {
                zc += `  ${field.camundaVariable || field.id}: z.string().uuid({ message: "Se requiere un UUID de Puntero S3" })${field.required ? '.min(1, "Campo requerido")' : '.optional()'}${piiMod}, // [${field.stage || 'GLOBAL'}]\n`;
            } else {
                zc += `  ${field.camundaVariable || field.id}: z.${zt}()${field.required && field.type !== 'checkbox' ? '.min(1, "Campo requerido")' : '.optional()'}${piiMod}, // [${field.stage || 'GLOBAL'}]\n`;
            }
         }
         zc += isRoot ? `})` : `        })`;
         return zc;
      };

      let zc = `import { z } from 'zod';\n\nexport const taskSchema = ${walkNode(canvasFields.value, true)}`;
      
      // Inject CA-48 Condicionales Directly via SuperRefine
      const conditionalFields = flatFields(canvasFields.value).filter(f => f.requiredIfField && f.requiredIfValue);
      let crules = '';
      
      if (visualRules.value && visualRules.value.length > 0) {
          crules += `  // CA-32: Validaciones Cruzadas AST\n`;
          visualRules.value.forEach(r => {
             let failCond = '';
             if (r.operator === '>') failCond = `data.${r.fieldA} <= data.${r.fieldB}`;
             if (r.operator === '<') failCond = `data.${r.fieldA} >= data.${r.fieldB}`;
             if (r.operator === '==') failCond = `data.${r.fieldA} !== data.${r.fieldB}`;
             if (r.operator === '!=') failCond = `data.${r.fieldA} === data.${r.fieldB}`;
             crules += `  if (${failCond}) {\n    ctx.addIssue({ code: z.ZodIssueCode.custom, message: "${r.errorMessage}", path: ["${r.fieldA}"] });\n  }\n`;
          });
      }

      if (conditionalFields.length > 0) {
         crules += `  // CA-48: Validaciones Condicionales Declarativas\n`;
         conditionalFields.forEach(f => {
            crules += `  if (data.${f.requiredIfField} === '${f.requiredIfValue}' && !data.${f.camundaVariable || f.id}) {\n    ctx.addIssue({ code: z.ZodIssueCode.custom, message: "Campo obligatorio basado en ${f.requiredIfField}", path: ["${f.camundaVariable || f.id}"] });\n  }\n`;
         });
      }

      if (crules) {
         zc += `\n.superRefine((data, ctx) => {\n${crules}})`;
      }
      zc += `;\n\nexport type TaskSchemaPayload = z.infer<typeof taskSchema>;`;
      return zc;
    }

    return '';
  },
  set: (newCode: string) => {
    // CA-4: Parseo seguro usando Regex (AST Ligero in-memory), PROHIBIDO eval() o new Function()
    if (activeCodeTab.value === 'TEMPLATE') {
      const inputRegex = /v-model="formData\.([^"]+)"/g;
      let m;
      const ids = new Set<string>();
      while ((m = inputRegex.exec(newCode)) !== null) {
          ids.add(m[1]);
      }
      const currentFields = [...canvasFields.value];
      const newCanvasFields = [];
      for (const id of Array.from(ids)) {
          const exist = currentFields.find(f => f.camundaVariable === id || f.id === id);
          if (exist) {
              newCanvasFields.push(exist);
          } else {
              newCanvasFields.push({ id: id.toUpperCase(), camundaVariable: id, type: 'text', label: id, required: false, stage: 'START_EVENT' });
          }
      }
      canvasFields.value = newCanvasFields;
    } 
    else if (activeCodeTab.value === 'ZOD') {
      try {
        const regex = /^\s*([a-zA-Z0-9_]+):\s*(z\.(?:string|number|any|boolean)\(\)|z\.array\(z\.string\(\)\))(.*?)(?:\/\/\s*\[([^\]]+)\])?/gm;
        let match;
        const newCanvasFields = [];
        const currentFields = [...canvasFields.value];
        let parseCount = 0;
        
        while ((match = regex.exec(newCode)) !== null) {
            parseCount++;
            const varName = match[1];
            const zTypeRaw = match[2];
            const mods = match[3];
            const stage = match[4] ? match[4].trim() : "START_EVENT";

            const isReq = mods.includes('.min(') || !mods.includes('.optional()');
            const isMult = zTypeRaw.includes('z.array');
            
            let minL, maxL;
            const minMatch = mods.match(/\.min\((\d+)/);
            if (minMatch) minL = parseInt(minMatch[1], 10);
            const maxMatch = mods.match(/\.max\((\d+)/);
            if (maxMatch) maxL = parseInt(maxMatch[1], 10);
            
            let cType = 'text';
            if(isMult) cType = 'select'; // Prefer select if multiple

            const exist = currentFields.find(f => f.camundaVariable === varName || f.id === varName);
            newCanvasFields.push({
               ...(exist || { id: varName.toUpperCase(), label: varName }),
               camundaVariable: varName,
               type: exist && exist.type !== cType && exist.type !== 'select' && exist.type !== 'async_select' && exist.type !== 'hidden' ? cType : (exist ? exist.type : cType),

               required: isReq,
               stage: stage,
               isMultiple: isMult || exist?.isMultiple,
               minLength: minL || exist?.minLength,
               maxLength: maxL || exist?.maxLength
            });
        }
        
        // Tarea 3: Fallback Try Catch
        if (newCode.includes('z.object({') && parseCount === 0 && newCode.includes(':')) {
            throw new Error('Sintaxis fallida o Regex roto');
        }

        if (newCanvasFields.length > 0 || newCode.includes('z.object({')) {
            canvasFields.value = newCanvasFields;
            zodParseError.value = false;
        }
      } catch (err) {
        zodParseError.value = true;
        showToast('El parseo manual ha fallado, las propiedades visuales prevalecen', 'error');
      }
    }
  }
});

// ── Modals Triggers ──────────────────────────────────────────────
// Eliminado old `generateTests` (CA-115). Se mantiene BDD Generator `generateVitestSpec`.

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
           title: formTitle.value,
           pattern: formPattern.value,
           schemaVariables: canvasFields.value
        };
        const response = await apiClient.post('/forms', dto);
        modalContent.value += `\n[BACKEND HTTP RESPONSE 201 CREATED]:\nRecepción de metadatos aprobada por la API.\nFormulario guardado para distribución:\n\n${JSON.stringify(response.data, null, 2)}`;
    } catch (error: any) {
        modalContent.value += `\n[BACKEND HTTP ERROR]:\n\nEndpoint devolvió fallo. Asegúrate que Java está activo.\n${error.message}`;
    }
    showResultModal.value = true;
};
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
