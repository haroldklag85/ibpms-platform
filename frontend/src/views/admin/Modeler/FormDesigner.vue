<template>
  <div class="h-full w-full bg-gray-50 flex flex-col" v-cloak>

    <!-- ═══════ Toast Notifications (CA-7) ═══════ -->
    <Teleport to="body">
      <Transition name="toast-slide">
        <div v-if="toast.msg" :class="toast.type === 'success' ? 'bg-green-600' : 'bg-red-600'" class="fixed top-4 right-4 z-[100] text-white px-5 py-3 rounded-lg shadow-xl flex items-center space-x-3">
          <span class="text-sm font-medium">{{ toast.msg }}</span>
          <button @click="toast.msg = ''" class="ml-2 opacity-70 hover:opacity-100">&times;</button>
        </div>
      </Transition>
    </Teleport>

    <!-- ═══════ Header Toolbar ═══════ -->
    <header class="flex justify-between items-center px-6 py-3 bg-white border-b border-gray-200 shrink-0">
      <div class="flex items-center space-x-4">
        <div>
          <h1 class="text-xl font-bold text-gray-900 flex items-center gap-2">
            IDE de Formularios Vue3/Zod
            <span class="text-xs font-bold text-white px-2 py-0.5 rounded-full" :class="formPattern === 'IFORM_MAESTRO' ? 'bg-blue-600' : 'bg-green-600'">
              {{ formPattern === 'IFORM_MAESTRO' ? '🔵 iForm Maestro' : '🟢 Simple' }}
            </span>
          </h1>
          <p class="text-xs text-gray-500 mt-0.5">Editor bidireccional Vue3 Composition API + Validaciones Zod (US-003)</p>
        </div>
      </div>
      
      <div class="flex items-center gap-2">
        <!-- Full-Screen Inmersivo (CA-9/CA-10) -->
        <button @click="isFullScreen = !isFullScreen" class="bg-gray-100 text-gray-700 px-3 py-1.5 border border-gray-300 rounded shadow-sm text-xs font-semibold hover:bg-gray-200 transition flex gap-1.5 items-center">
          {{ isFullScreen ? '🗗 Salir Inmersión' : '🖵 Pantalla Completa' }}
        </button>

        <!-- Generador Tests -->
        <button @click="generateTests" class="bg-gray-800 text-yellow-400 px-3 py-1.5 border border-black rounded shadow-sm text-xs font-semibold hover:bg-black transition flex gap-1.5 items-center">
          ⚡ Generar Tests Zod (CA-115)
        </button>

        <!-- Reset Dual -->
        <button @click="confirmReset" class="bg-white text-red-600 px-3 py-1.5 border border-red-200 rounded shadow-sm text-xs font-semibold hover:bg-red-50 transition flex gap-1.5 items-center">
          🗑 Reset (CA-43)
        </button>

        <!-- Submit Mock -->
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
              :group="{ name: 'components', pull: 'clone', put: false }"
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
              group="components"
              item-key="id"
              class="flex-1 min-h-[300px] font-sans"
              animation="200"
              ghost-class="ghost-dropzone"
            >
              <template #item="{ element, index }">
                <div 
                  v-show="formPattern !== 'IFORM_MAESTRO' || activeStageSim === 'ALL' || element.stage === activeStageSim"
                  class="group relative border border-transparent hover:border-indigo-300 hover:bg-indigo-50/30 p-4 rounded-lg mb-4 transition"
                >
                  
                  <!-- Controles del Campo (Hover) -->
                  <div class="absolute -top-3 right-2 hidden group-hover:flex bg-white border border-gray-200 shadow-sm rounded-md overflow-hidden text-xs z-20">
                    <button @click="editField(element)" class="px-2 py-1 text-gray-600 hover:bg-gray-100 border-r border-gray-200" title="Propiedades">⚙️</button>
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

                    
                    <!-- Nested Container (CA-8) -->
                    <div v-if="element.type === 'container'" class="border border-indigo-200 bg-indigo-50/50 rounded-lg p-4 mt-2 min-h-[100px]">
                      <VueDraggable
                         v-model="element.children"
                         group="components"
                         item-key="id"
                         class="min-h-[80px]"
                         animation="200"
                         ghost-class="ghost-dropzone"
                      >
                         <template #item="{ element: child, index: childIdx }">
                            <div class="group/child relative bg-white border border-gray-200 p-3 rounded mb-2 hover:border-indigo-300 shadow-sm transition">
                               <div class="absolute -top-3 right-2 hidden group-hover/child:flex bg-white border border-gray-200 shadow-sm rounded-md overflow-hidden text-xs z-20">
                                 <button @click="editField(child)" class="px-2 py-1 text-gray-600 hover:bg-gray-100 border-r border-gray-200">⚙️</button>
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
                      </VueDraggable>
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
        <div class="flex-1 relative">
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
      <div v-if="showPatternModal" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[200] p-4 backdrop-blur-sm">
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

      <!-- Properties Modal (Field Editor) -->
      <div v-if="editingField" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[150] p-4">
        <div class="bg-white rounded-lg shadow-2xl p-6 w-full max-w-md">
          <h3 class="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">⚙️ Propiedades del Componente</h3>
          
          <div class="space-y-4">
            <div>
              <label class="block text-xs font-bold text-gray-700 mb-1 flex items-center gap-1">ID (Variable Name) <AppTooltip content="Identificador único del campo en el Motor. Se utiliza si el Binding explícito no está declarado (CA-18)." /></label>
              <input v-model="editingField.id" class="w-full text-sm border-gray-300 rounded font-mono bg-gray-50 uppercase" />
            </div>
            <div>
              <label class="block text-xs font-bold text-gray-700 mb-1 flex items-center gap-1">Label (Nombre Visible) <AppTooltip content="El texto de la etiqueta que el usuario leerá en la pantalla visual producida." /></label>
              <input v-model="editingField.label" class="w-full text-sm border-gray-300 rounded" />
            </div>
            <div v-if="formPattern === 'IFORM_MAESTRO'" class="bg-blue-50 p-3 rounded border border-blue-200">
               <label class="block text-xs font-bold text-blue-800 mb-1 flex items-center gap-1">Stage (Etapa BPMN de aparición) <AppTooltip content="Etapa en la cual el campo se revelará dinámicamente o dejará de bloquearse (CA-20)." /></label>
               <input v-model="editingField.stage" class="w-full text-sm border-blue-300 rounded font-mono" placeholder="Ej: ANALYSIS" />
            </div>
            <div>
              <label class="block text-xs font-bold text-indigo-700 mb-1 flex items-center gap-1">Camunda Variable (I/O Binding) <AppTooltip content="La variable exacta en el Process Instance de Camunda con la que se emparejará bidireccionalmente este campo." /></label>
              <input v-model="editingField.camundaVariable" class="w-full text-sm border-indigo-300 rounded font-mono bg-indigo-50" placeholder="Ej: customerName" />
              <p class="text-[10px] text-gray-500 mt-1">El valor del campo se mapeará a esta variable en Process Engine.</p>
            </div>
            <div class="flex items-center gap-2 pt-2 border-t mt-4">
               <input type="checkbox" v-model="editingField.required" id="reqCheck" class="text-indigo-600 rounded" />
               <label for="reqCheck" class="text-sm font-medium text-gray-700 cursor-pointer flex items-center gap-1">Campo Requerido (Agrega .min(1) a Zod) <AppTooltip content="Fuerza al validador Zod On-The-Fly a bloquear el envío si el campo es nulo o vacío." /></label>
            </div>
            <div v-if="formPattern === 'IFORM_MAESTRO'" class="flex items-center gap-2 pt-2 border-t">
               <input type="checkbox" v-model="editingField.soloLecturaPosterior" id="roCheck" class="text-indigo-600 rounded focus:ring-indigo-500" />
               <label for="roCheck" class="text-sm font-medium text-gray-700 cursor-pointer flex items-center gap-1">Bloquear en etapas futuras (RBAC) <AppTooltip content="CA-20 Corregido: Si se activa, este input será Deshabilitado (:disabled) si el proceso actual avanza a una etapa diferente." /></label>
            </div>
          </div>

          <div class="p-4 bg-gray-50 border border-gray-200 rounded-lg mt-4 shadow-inner">
             <h4 class="text-xs font-bold text-gray-800 mb-2 border-b border-gray-300 pb-1 flex items-center gap-2">🗃️ Data Binding Camunda (I/O) (CA-12/CA-13)</h4>
             <div class="flex items-center gap-2 mb-2">
                <input type="checkbox" v-model="editingField.isPrefilled" id="prefillCheck" class="text-indigo-600 rounded focus:ring-indigo-500" />
                <label for="prefillCheck" class="text-xs font-medium text-gray-700 cursor-pointer flex items-center gap-1">Pre-Fill (Lectura) <AppTooltip content="Avisa al motor que debe leer esta variable desde DB/Camunda CND se cargue la forma." /></label>
             </div>
             <div class="flex items-center gap-2">
                <input type="checkbox" v-model="editingField.isOutputToken" id="outCheck" class="text-indigo-600 rounded focus:ring-indigo-500" />
                <label for="outCheck" class="text-xs font-medium text-gray-700 cursor-pointer flex items-center gap-1">Update Token (Escritura) <AppTooltip content="Informa al backend que el valor modificado debe persistirse como variable global del Process Instance." /></label>
             </div>
          </div>

          <div class="mt-6 flex justify-end gap-3">
            <button @click="editingField = null" class="bg-indigo-600 text-white px-4 py-2 rounded text-sm font-semibold hover:bg-indigo-700">Guardar Cambios</button>
          </div>
        </div>
      </div>

      <!-- Test Gen / Result Modal -->
      <div v-if="showResultModal" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[150] p-4">
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
      <div v-if="showResetModal" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[200] p-4 backdrop-blur-sm animate-slide-in">
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

  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import VueDraggable from 'vuedraggable';
import VueMonacoEditor from '@guolao/vue-monaco-editor';
import { ZodBuilder, FormFieldMetadataDTO } from './ZodBuilder';
import apiClient from '@/services/apiClient';
import AppTooltip from '@/components/common/AppTooltip.vue';

// ── Types ────────────────────────────────────────────────────────
interface FormField extends FormFieldMetadataDTO {
  soloLecturaPosterior?: boolean; // CA-20
}

// ── State ────────────────────────────────────────────────────────
const formTitle = ref('Solicitud Onboarding (V1)');
const formPattern = ref<'SIMPLE' | 'IFORM_MAESTRO' | null>(null);
const showPatternModal = ref(true);
const isFullScreen = ref(false); // Estado para CA-9/CA-10

const canvasFields = ref<FormField[]>([]);
const activeStageSim = ref('ALL');

const activeCodeTab = ref<'TEMPLATE' | 'SCRIPT' | 'ZOD' | 'STYLE'>('TEMPLATE');
const editingField = ref<FormField | null>(null);

const showResetModal = ref(false); // Modal de Reset CA-43

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
const toolboxCategories = [
  {
    name: "Texto",
    items: [
      { icon: 'Ab', label: 'Input Text', desc: 'Validación Regex', type: 'text', placeholder: 'Ej: Juan Pérez', required: true, zodType: 'string', camundaVariable: '' },
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
      { icon: '≡', label: 'Dropdown', desc: 'Typeahead / API', type: 'select', placeholder: '-- Seleccione --', required: true, zodType: 'string', options: ['Opción A', 'Opción B'], camundaVariable: '' },
      { icon: '☑️', label: 'Checkbox', desc: 'Booleano Múltiple', type: 'checkbox', placeholder: 'Marcar opción', required: false, zodType: 'boolean', camundaVariable: '' },
      { icon: '🔘', label: 'Radio Button', desc: 'Opción Única', type: 'radio', placeholder: '', required: true, zodType: 'string', options: ['Opción 1', 'Opción 2'], camundaVariable: '' },
    ]
  },
  {
    name: "Avanzados",
    items: [
      { icon: '📎', label: 'File Upload', desc: 'SGDEA Vault Embed', type: 'file', placeholder: 'Arrastra PDF aquí', required: false, zodType: 'any', camundaVariable: '' },
      { icon: '✍️', label: 'Firma Digital', desc: 'Canvas a Base64', type: 'text', placeholder: 'Firma autógrafa', required: true, zodType: 'string', camundaVariable: '' },
    ]
  },
  {
    name: "Layouts (CA-8)",
    items: [
      { icon: '🗂️', label: 'Contenedor', desc: 'Panel Agrupador', type: 'container', placeholder: 'Nueva Sección de Datos', required: false, zodType: 'object', camundaVariable: '', children: [] }
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
];

// ── Draggable Clone Hook ─────────────────────────────────────────
let idCounter = 1;
const cloneComponent = (original: any) => {
  const cloned = JSON.parse(JSON.stringify(original));
  cloned.id = `FIELD_${idCounter++}`;
  cloned.camundaVariable = cloned.id.toLowerCase();
  cloned.stage = 'START_EVENT'; // Default
  if (cloned.type === 'container') {
    cloned.children = [];
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
  idCounter = 1;
  showResetModal.value = false;
  showPatternModal.value = true;
};

const removeField = (arr: FormField[], index: number) => {
  arr.splice(index, 1);
};

const editField = (field: FormField) => {
  editingField.value = field;
};

declare const monaco: any;

const onMonacoMount = (editorIns: any, monacoIns: any) => {
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
    if (f.type === 'container') {
      if (f.children) res = res.concat(flatFields(f.children));
    } else {
      res.push(f);
    }
  }
  return res;
};

// HTML generator recursivo para Template (AST to Vue)
const generateFieldHTML = (field: any, indent: string = '      '): string => {
  let tpl = '';
  
  if (field.type.startsWith('button_')) {
      tpl += `${indent}<div class="mt-6 field-${field.id.toLowerCase()}">\n`;
      if (field.type === 'button_submit') {
        tpl += `${indent}  <button type="submit" class="w-full bg-indigo-600 text-white py-2 rounded shadow font-bold hover:bg-indigo-700 transition flex items-center justify-center gap-2">✅ ${field.label}</button>\n`;
      } else if (field.type === 'button_draft') {
        tpl += `${indent}  <button type="button" @click="saveDraft" class="w-full border-2 border-dashed border-gray-300 text-gray-700 py-2 rounded shadow-sm font-bold hover:bg-gray-100 transition flex items-center justify-center gap-2">💾 ${field.label}</button>\n`;
      } else if (field.type === 'button_reject') {
        tpl += `${indent}  <button type="button" @click="rejectTask" class="w-full bg-red-600 text-white py-2 rounded shadow-sm font-bold hover:bg-red-700 transition mt-2 flex items-center justify-center gap-2">❌ ${field.label}</button>\n`;
      }
      tpl += `${indent}</div>\n`;
      return tpl;
  }

  if (formPattern.value === 'IFORM_MAESTRO') {
    tpl += `${indent}<div v-if="stage === '${field.stage}'" class="field-${field.id.toLowerCase()}">\n`;
  } else {
    tpl += `${indent}<div class="field-${field.id.toLowerCase()}">\n`;
  }
  
  if (field.type === 'container') {
     tpl += `${indent}  <div class="border rounded-md p-4 bg-gray-50">\n`;
     tpl += `${indent}    <h3 class="font-bold text-md mb-4">${field.label || 'Sección'}</h3>\n`;
     if (field.children && field.children.length > 0) {
       for(const child of field.children) {
         tpl += generateFieldHTML(child, indent + '    ');
       }
     }
     tpl += `${indent}  </div>\n`;
  } else {
    tpl += `${indent}  <label class="block text-sm font-medium text-gray-700">${field.label}${field.required ? '*' : ''}</label>\n`;
    const dsb = (formPattern.value === 'IFORM_MAESTRO' && field.soloLecturaPosterior) ? ` :disabled="stage !== '${field.stage}'"` : '';
    if (field.type === 'text' || field.type === 'number' || field.type === 'date' || field.type === 'time') {
      tpl += `${indent}  <input type="${field.type}" v-model="formData.${field.camundaVariable || field.id}" placeholder="${field.placeholder || ''}" class="form-input mt-1 w-full rounded-md border-gray-300 shadow-sm"${dsb} />\n`;
    } else if (field.type === 'textarea') {
      tpl += `${indent}  <textarea v-model="formData.${field.camundaVariable || field.id}" placeholder="${field.placeholder || ''}" class="form-input mt-1 w-full rounded-md border-gray-300 shadow-sm" rows="3"${dsb}></textarea>\n`;
    } else if (field.type === 'checkbox') {
      tpl += `${indent}  <div class="flex items-center gap-2 mt-1">\n${indent}    <input type="checkbox" v-model="formData.${field.camundaVariable || field.id}" class="rounded text-indigo-600 border-gray-300 focus:ring-indigo-500 shadow-sm"${dsb} />\n${indent}    <span class="text-sm text-gray-700">${field.placeholder || field.label}</span>\n${indent}  </div>\n`;
    } else if (field.type === 'radio') {
      tpl += `${indent}  <div class="flex flex-col gap-1 mt-1">\n${(field.options || ['Opción 1', 'Opción 2']).map((o:string) => `${indent}    <label class="flex items-center gap-2"><input type="radio" value="${o}" v-model="formData.${field.camundaVariable || field.id}" class="text-indigo-600 border-gray-300 focus:ring-indigo-500 shadow-sm"${dsb} /> <span class="text-sm text-gray-600 font-medium">${o}</span></label>`).join('\n')}\n${indent}  </div>\n`;
    } else if (field.type === 'select') {
       tpl += `${indent}  <select v-model="formData.${field.camundaVariable || field.id}" class="form-select mt-1 w-full rounded-md border-gray-300 shadow-sm"${dsb}>\n${indent}    <option disabled value="">${field.placeholder || 'Seleccione'}</option>\n${(field.options || ['Opción 1', 'Opción 2']).map((o:string) => `${indent}    <option value="${o}">${o}</option>`).join('\n')}\n${indent}  </select>\n`;
    } else if (field.type === 'file') {
       tpl += `${indent}  <div class="border-2 border-dashed border-gray-300 rounded p-4 text-center text-xs text-gray-500 cursor-pointer bg-white mt-1">\n${indent}    📂 ${field.placeholder || 'Arrastra tu archivo aquí'}\n${indent}  </div>\n`;
    } else {
       tpl += `${indent}  <!-- Custom Component: ${field.type} -->\n`;
    }
    tpl += `${indent}  <span v-if="errors.${field.camundaVariable || field.id}" class="text-red-500 text-xs">{{ errors.${field.camundaVariable || field.id} }}</span>\n`;
  }
  tpl += `${indent}</div>\n`;
  return tpl;
};

// ── Generators & Parsers (Bidireccional AST-Sandbox) ────────────────────────
const computedCode = computed({
  get: () => {
    if (activeCodeTab.value === 'TEMPLATE') {
      let tpl = `<template>\n  <form @submit.prevent="submitTask" class="space-y-4">`;
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
      let scr = `<script setup lang="ts">\nimport { ref, inject } from 'vue';\nimport { z } from 'zod';\nimport { taskSchema } from './schema.zod.ts';\nimport apiClient from '@/services/apiClient';\n\n`;
      if (formPattern.value === 'IFORM_MAESTRO') {
        scr += `// IFORM_MAESTRO: Inyección de Etapa BPMN actual (Dual-Pattern CA-2)\nconst stage = inject('camunda_process_stage', 'START_EVENT');\n\n`;
      }
      
      scr += `const formData = ref({\n`;
      const allFields = flatFields(canvasFields.value).filter(f => !f.type.startsWith('button_') && f.type !== 'container');
      for (const field of allFields) {
        let def = "''";
        if (field.type === 'number') def = 'null';
        if (field.type === 'checkbox') def = 'false';
        scr += `  ${field.camundaVariable || field.id}: ${def}, // Binding CA-12/13\n`;
      }
      scr += `});\n\nconst errors = ref<Record<string, string>>({});\n`;
      scr += `const taskId = 'MOCK_TASK_ID'; // Inyectar ID real\n\n`;

      const hasDraft = flatFields(canvasFields.value).some(f => f.type === 'button_draft');
      const hasReject = flatFields(canvasFields.value).some(f => f.type === 'button_reject');

      scr += `// CA-15: Smart Actions con Blindaje de Red Try/Catch\n`;
      scr += `const submitTask = async () => {\n  errors.value = {};\n  const result = taskSchema.safeParse(formData.value);\n  if (!result.success) {\n    result.error.issues.forEach(iss => {\n      if (iss.path[0]) errors.value[iss.path[0].toString()] = iss.message;\n    });\n    return;\n  }\n  try {\n    const payload = { variables: result.data };\n    await apiClient.post(\`/engine-rest/task/\${taskId}/complete\`, payload);\n    alert('Tarea Completada (Success)');\n  } catch (error) {\n    alert('Excepción de Red al Completar Tarea: ' + (error as any).message);\n  }\n};\n`;
      
      if (hasDraft) {
        scr += `\nconst saveDraft = async () => {\n  try {\n    await apiClient.post('/api/v1/forms/draft', formData.value);\n    alert('Borrador Guardado (Success)');\n  } catch (error) {\n    alert('Excepción de Red al Guardar Borrador: ' + (error as any).message);\n  }\n};\n`;
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
      let zc = `import { z } from 'zod';\n\nexport const taskSchema = z.object({\n`;
      const allFields = flatFields(canvasFields.value).filter(f => !f.type.startsWith('button_') && f.type !== 'container');
      for (const field of allFields) {
        let zt = 'string';
        if(field.type === 'number') zt = 'number';
        if(field.type === 'file') zt = 'any';
        if(field.type === 'checkbox') zt = 'boolean';
        zc += `  ${field.camundaVariable || field.id}: z.${zt}()${field.required && field.type !== 'checkbox' ? '.min(1, "Campo requerido")' : '.optional()'}, // [${field.stage || 'GLOBAL'}]\n`;
      }
      zc += `});\n\nexport type TaskSchemaPayload = z.infer<typeof taskSchema>;`;
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
      const regex = /^\s*([a-zA-Z0-9_]+):\s*z\.(string|number|any|boolean)\(\)(.*?)(?:\/\/\s*\[([^\]]+)\])?/gm;
      let match;
      const newCanvasFields = [];
      const currentFields = [...canvasFields.value];
      
      while ((match = regex.exec(newCode)) !== null) {
          const varName = match[1];
          const zType = match[2];
          const mods = match[3];
          const stage = match[4] ? match[4].trim() : "START_EVENT";

          const isReq = mods.includes('.min(') || !mods.includes('.optional()');
          
          let cType = 'text';
          if(zType === 'number') cType = 'number';
          if(zType === 'any') cType = 'file';

          const exist = currentFields.find(f => f.camundaVariable === varName || f.id === varName);
          newCanvasFields.push({
             ...(exist || { id: varName.toUpperCase(), label: varName }),
             camundaVariable: varName,
             type: exist && exist.type !== cType && exist.type !== 'select' ? cType : (exist ? exist.type : cType),
             required: isReq,
             stage: stage 
          });
      }
      if (newCanvasFields.length > 0 || newCode.includes('z.object({')) {
          canvasFields.value = newCanvasFields;
      }
    }
  }
});

// ── Modals Triggers ──────────────────────────────────────────────
const generateTests = () => {
    modalTitle.value = "🧪 Vitest/Jest Generator (CA-115)";
    modalContent.value = `import { describe, it, expect } from 'vitest';
import { taskSchema } from './schema.zod.ts';

describe('${formTitle.value} - Zod Validation', () => {
   
   it('Debería retornar Error si el payload viene vacío (simulando 400 Bad Request)', () => {
      const payload = {}; 
      const result = taskSchema.safeParse(payload);
      expect(result.success).toBe(false);
   });

${canvasFields.value.length > 0 ? `   it('Debería aprobar el envío correcto de ${canvasFields.value[0].label}', () => {
      const payload = {
${canvasFields.value.map(f => `         ${f.id}: ${f.type==='number' ? 100 : "'test_value'"},`).join('\n')}
      };
      const result = taskSchema.safeParse(payload);
      expect(result.success).toBe(true);
   });` : '   // Arrastra componentes para generar tests automáticos basados en tus inputs.'}
});`;
    showResultModal.value = true;
};

const simulateMockSubmit = async () => {
    modalTitle.value = "🚀 Execute End-to-End Validation Engine & Integration (CA-29)";
    
    // BUILD DYNAMIC ZOD SCHEMA FACTORY based on live fields metadata
    const executableSchema = ZodBuilder.buildSchema(canvasFields.value);

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
