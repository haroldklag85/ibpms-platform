<template>
  <div class="flex h-screen w-full sidebar-group font-sans text-gray-800 bg-gray-50">
    <input class="hidden sidebar-toggle" id="sidebar-toggle" type="checkbox"/>
    <aside class="sidebar flex flex-col w-64 bg-white border-r border-gray-200 transition-all duration-300 ease-in-out shrink-0">
      <div class="flex items-center justify-between p-4 h-[65px] border-b border-gray-200 sidebar-header">
        <a class="flex items-center gap-3 overflow-hidden hover:opacity-80 transition-opacity" href="#">
          <span class="text-blue-600 text-2xl font-bold">🏢</span>
          <span class="text-xl font-bold text-gray-800 sidebar-logo-text">Docketing</span>
        </a>
        <label class="cursor-pointer" for="sidebar-toggle">
          <div class="p-1 rounded-md hover:bg-gray-100 sidebar-toggle-button transition-transform duration-300 ease-in-out text-gray-600 select-none border border-transparent hover:border-gray-200 bg-white shadow-sm">
            <span>☰</span>
          </div>
        </label>
      </div>
      <nav class="flex-1 flex flex-col p-2 space-y-1">
        <router-link to="/" class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-100 text-gray-600 font-medium">
          <span class="text-xl text-center w-6">🏠</span>
          <span class="sidebar-label">Inicio (Volver al iBPMS)</span>
        </router-link>
        <a class="flex items-center gap-3 px-3 py-2.5 rounded-lg bg-blue-100 text-blue-700 font-semibold" href="#">
          <span class="text-xl text-center w-6">📥</span>
          <span class="sidebar-label">Bandeja de Entrada</span>
        </a>
        <a class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-100 text-gray-600 font-medium" href="#">
          <span class="text-xl text-center w-6">📤</span>
          <span class="sidebar-label">Bandeja de Salida</span>
        </a>
        <a class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-100 text-gray-600 font-medium" href="#">
          <span class="text-xl text-center w-6">🧠</span>
          <span class="sidebar-label">Configuración de IA</span>
        </a>
        <a class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-100 text-gray-600 font-medium" href="#">
          <span class="text-xl text-center w-6">🔍</span>
          <span class="sidebar-label">Búsqueda Avanzada</span>
        </a>
        <a class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-100 text-gray-600 font-medium" href="#">
          <span class="text-xl text-center w-6">📄</span>
          <span class="sidebar-label">Plantillas</span>
        </a>
        <a class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-100 text-gray-600 font-medium" href="#">
          <span class="text-xl text-center w-6">📊</span>
          <span class="sidebar-label">Reportes de Gestión</span>
        </a>
        <div class="flex-1"></div>
        <a class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-100 text-gray-600 font-medium" href="#">
          <span class="text-xl text-center w-6">⚙️</span>
          <span class="sidebar-label">Configuraciones de Cuenta</span>
        </a>
      </nav>
    </aside>

    <div class="flex flex-1 overflow-hidden right-panel-group relative bg-white">
      <!-- El Resto del Master & Detail Panel (Directo del Mockup) -->
      <div class="flex h-screen w-full transition-all duration-300">
          <aside class="flex flex-col w-[350px] shrink-0 border-r border-gray-200 bg-white z-10">
              <div class="flex items-center justify-between p-4 border-b border-gray-200 h-[65px]">
                  <div class="flex items-center gap-3">
                      <div class="bg-blue-600 text-white flex items-center justify-center font-bold aspect-square rounded-full size-10">USR</div>
                      <div>
                          <h1 class="text-base font-medium text-gray-800">Bandeja de Entrada</h1>
                          <p class="text-sm text-gray-500">user@email.com</p>
                      </div>
                  </div>
              </div>
              <div class="p-4 border-b border-gray-100 bg-gray-50">
                  <label class="flex flex-col w-full">
                      <div class="relative flex w-full items-center">
                          <span class="absolute left-3 text-gray-500">🔍</span>
                          <input class="w-full rounded-lg border-gray-300 bg-white pl-10 h-10 placeholder:text-gray-500 focus:ring-blue-500 focus:border-blue-500 text-sm border outline-none shadow-sm" placeholder="Buscar correo" value=""/>
                      </div>
                  </label>
              </div>
              <nav class="flex-1 overflow-y-auto">
                  <div class="flex flex-col">
                      <!-- Active Item -->
                      <div class="flex gap-4 bg-blue-50/70 px-4 py-3 justify-between border-l-4 border-blue-600 cursor-pointer">
                          <div class="flex items-start gap-3 w-full">
                              <div class="bg-gray-800 text-white flex items-center justify-center font-bold aspect-square rounded-full size-10">JD</div>
                              <div class="flex-1 overflow-hidden">
                                  <div class="flex justify-between items-center mb-0.5">
                                      <p class="text-gray-900 text-sm font-semibold leading-normal truncate">John Doe</p>
                                      <p class="text-gray-500 text-xs font-normal">5:45 PM</p>
                                  </div>
                                  <p class="text-gray-900 text-sm font-semibold leading-normal truncate">Project Update & Next Steps</p>
                                  <p class="text-gray-600 text-xs leading-normal truncate mt-0.5">Hi team, here is the latest update on the project financials and our plan...</p>
                                  <div class="flex items-center gap-2 mt-2">
                                      <span class="inline-flex items-center gap-1.5 rounded-full bg-purple-100 border border-purple-200 px-2 py-0.5 text-xs font-bold text-purple-800">
                                          🔗 Tarea Creada
                                      </span>
                                  </div>
                              </div>
                          </div>
                      </div>
                      
                      <!-- Item 2 -->
                      <div class="flex gap-4 bg-white px-4 py-3 justify-between border-l-4 border-transparent hover:bg-gray-50 cursor-pointer transition">
                          <div class="flex items-start gap-3 w-full">
                              <div class="bg-orange-500 text-white flex items-center justify-center font-bold aspect-square rounded-full size-10">JS</div>
                              <div class="flex-1 overflow-hidden">
                                  <div class="flex justify-between items-center mb-0.5">
                                      <p class="text-gray-900 text-sm font-medium leading-normal truncate">Jane Smith</p>
                                      <p class="text-gray-500 text-xs font-normal">4:30 PM</p>
                                  </div>
                                  <p class="text-gray-900 text-sm font-medium leading-normal truncate">Re: Meeting Schedule</p>
                                  <p class="text-gray-600 text-xs leading-normal truncate mt-0.5">Sounds good, let's meet tomorrow at 10 AM. I've sent the calendar invite.</p>
                                  <div class="flex items-center gap-2 mt-2">
                                      <span class="inline-flex items-center gap-1.5 rounded-full bg-green-100 border border-green-200 px-2 py-0.5 text-xs font-bold text-green-800">
                                          ✔️ Acuse Enviado
                                      </span>
                                  </div>
                              </div>
                          </div>
                      </div>

                      <!-- Item 3 -->
                      <div class="flex gap-4 bg-white px-4 py-3 justify-between border-l-4 border-transparent hover:bg-gray-50 cursor-pointer transition">
                          <div class="flex items-start gap-3 w-full">
                              <div class="bg-red-500 text-white flex items-center justify-center font-bold aspect-square rounded-full size-10">SC</div>
                              <div class="flex-1 overflow-hidden">
                                  <div class="flex justify-between items-center mb-0.5">
                                      <p class="text-gray-900 text-sm font-medium leading-normal truncate">support@company.com</p>
                                      <p class="text-gray-500 text-xs font-normal">3:15 PM</p>
                                  </div>
                                  <p class="text-gray-900 text-sm font-medium leading-normal truncate">Your ticket has been updated</p>
                                  <p class="text-gray-600 text-xs leading-normal truncate mt-0.5">A new response has been added to your support ticket #12345. Please...</p>
                              </div>
                          </div>
                      </div>
                  </div>
              </nav>
          </aside>

          <!-- Contenido Derecha (Lector & IA) -->
          <main class="flex flex-col flex-1 bg-gray-50 overflow-hidden relative right-panel transition-all duration-300 ease-in-out">
              <div class="flex-1 overflow-y-auto p-6 lg:p-10 relative bg-white">
                  <div class="pb-6 border-b border-gray-200">
                      <h2 class="text-2xl lg:text-3xl font-bold text-gray-800">Project Update & Next Steps</h2>
                      <div class="flex items-center gap-4 mt-6">
                          <div class="bg-gray-800 text-white flex items-center justify-center font-bold text-xl aspect-square rounded-full size-12 shadow-sm">JD</div>
                          <div class="flex-1">
                              <p class="font-semibold text-gray-800 text-lg">John Doe <span class="font-normal text-sm text-gray-500">&lt;john.doe@client.com&gt;</span></p>
                              <p class="text-sm text-gray-500">Para: user@email.com</p>
                          </div>
                          <div class="text-sm text-gray-500 font-medium mr-4 bg-gray-100 px-3 py-1 rounded-full">
                              Hoy, 5:45 PM
                          </div>
                          <div class="flex items-center gap-2">
                              <button class="p-2.5 rounded-lg hover:bg-gray-100 text-gray-500 transition shadow-sm border border-gray-200 bg-white" title="Responder">↩️</button>
                              <button class="p-2.5 rounded-lg hover:bg-gray-100 text-gray-500 transition shadow-sm border border-gray-200 bg-white" title="Reenviar">▶️</button>
                              <button class="p-2.5 rounded-lg hover:bg-gray-100 text-gray-500 transition shadow-sm border border-gray-200 bg-white" title="Archivar">📦</button>
                              <button class="p-2.5 rounded-lg hover:bg-red-50 text-red-500 transition shadow-sm border border-gray-200 bg-white" title="Eliminar">🗑️</button>
                          </div>
                      </div>
                  </div>
                  <div class="py-6 text-gray-700 text-[15px] leading-relaxed max-w-4xl">
                      <p class="mb-5">Hi team,</p>
                      <p class="mb-5">Following up on our discussion, I've attached the financial report for Q3. Please review it by EOD Friday, <strong>October 28th, 2024</strong>.</p>
                      <p class="mb-3">Key highlights:</p>
                      <ul class="list-disc pl-6 mb-6 space-y-2">
                          <li>Revenue is up 15% quarter-over-quarter.</li>
                          <li>New client acquisition costs are down by 5%.</li>
                      </ul>
                      <p class="mb-6">Let's schedule a meeting for next week to discuss the Q4 strategy. I'll send a separate invite for that.</p>
                      <p>Best regards,</p>
                      <p class="font-bold text-gray-800 mt-2">John Doe<br/><span class="text-gray-500 font-normal">Project Manager, Client Corp.</span></p>
                  </div>
              </div>

              <!-- Panel Inteligente IA -->
              <div class="border-t border-gray-200 bg-white group shrink-0 shadow-[0_-5px_15px_rgba(0,0,0,0.02)] z-10 transition-all duration-300">
                  <input :checked="isAiExpanded" @change="isAiExpanded = $event.target.checked" class="peer hidden ai-checkbox" id="ai-toggle" type="checkbox"/>
                  <label class="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-50" for="ai-toggle">
                      <div class="flex items-center gap-3">
                          <h3 class="text-lg font-bold text-gray-800 flex items-center gap-2"><span class="text-xl">✨</span> Sugerencias Inteligentes de IA</h3>
                      </div>
                      <span class="text-gray-500 font-bold transition-transform duration-300 transform" :class="isAiExpanded ? 'rotate-180' : ''">▼</span>
                  </label>
                  
                  <div class="grid-rows-[0fr] opacity-0 transition-all duration-300 ease-in-out peer-checked:grid-rows-[1fr] peer-checked:opacity-100 peer-checked:pb-6 grid max-h-[500px] overflow-y-auto w-full">
                      <div class="overflow-hidden bg-gray-50/30">
                          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5 px-6 pt-2">
                              
                              <!-- Clasificación -->
                              <div class="bg-gray-50 rounded-xl p-5 border border-gray-200 shadow-sm flex flex-col justify-between">
                                  <div>
                                      <p class="text-sm font-medium text-gray-500">Clasificación</p>
                                      <p class="text-base font-bold text-blue-600 mt-1">Informe Financiero</p>
                                  </div>
                                  <div class="flex items-center gap-2 mt-5">
                                      <button class="flex-1 flex items-center justify-center gap-1.5 rounded-lg h-10 px-3 bg-green-50 text-green-700 border border-green-200 text-sm font-bold hover:bg-green-100 transition shadow-sm">✔️ Aceptar</button>
                                      <button class="flex-1 flex items-center justify-center gap-1.5 rounded-lg h-10 px-3 bg-gray-100 text-gray-600 border border-gray-200 text-sm font-bold hover:bg-gray-200 transition shadow-sm">✖️ Rechazar</button>
                                  </div>
                              </div>

                              <!-- Cliente -->
                              <div class="bg-gray-50 rounded-xl p-5 border border-gray-200 shadow-sm flex flex-col justify-between">
                                  <div>
                                      <p class="text-sm font-medium text-gray-500">Cliente Asociado</p>
                                      <p class="text-base font-bold text-blue-600 mt-1">Client Corp.</p>
                                  </div>
                                  <div class="flex items-center gap-2 mt-5">
                                      <button class="flex-1 flex items-center justify-center gap-1.5 rounded-lg h-10 px-3 bg-green-50 text-green-700 border border-green-200 text-sm font-bold hover:bg-green-100 transition shadow-sm">✔️ Aceptar</button>
                                      <button class="flex-1 flex items-center justify-center gap-1.5 rounded-lg h-10 px-3 bg-gray-100 text-gray-600 border border-gray-200 text-sm font-bold hover:bg-gray-200 transition shadow-sm">✖️ Rechazar</button>
                                  </div>
                              </div>

                              <!-- Fechas Claves -->
                              <div class="bg-gray-50 rounded-xl p-5 border border-gray-200 shadow-sm flex flex-col justify-between">
                                  <div>
                                      <p class="text-sm font-medium text-gray-500">Fechas Clave</p>
                                      <p class="text-base font-bold text-blue-600 mt-1">28 de Octubre, 2024</p>
                                  </div>
                                  <div class="flex items-center gap-2 mt-5">
                                      <button class="flex-1 flex items-center justify-center gap-1.5 rounded-lg h-10 px-3 bg-green-50 text-green-700 border border-green-200 text-sm font-bold hover:bg-green-100 transition shadow-sm">✔️ Aceptar</button>
                                      <button class="flex-1 flex items-center justify-center gap-1.5 rounded-lg h-10 px-3 bg-gray-100 text-gray-600 border border-gray-200 text-sm font-bold hover:bg-gray-200 transition shadow-sm">✖️ Rechazar</button>
                                  </div>
                              </div>
                              
                              <!-- Borrador -->
                              <div class="col-span-1 md:col-span-2 lg:col-span-3 mt-2">
                                  <div class="bg-blue-50/50 border border-blue-100 rounded-xl p-6 shadow-sm">
                                      <p class="text-sm font-bold text-blue-700 mb-3 flex items-center gap-2">📝 Propuesta de Respuesta</p>
                                      <div class="bg-white rounded p-4 border border-gray-200 mb-5 shadow-inner min-h-[80px]">
                                          <p class="text-[14px] text-gray-700 italic">"Hi John, thanks for sending over the financial report for Q3. We will review the numbers and the client acquisition costs. We confirm the deadline for feedback by EOD Friday, October 28th. We will wait for your separate invite to discuss the Q4 strategy next week. Best regards."</p>
                                      </div>
                                      <div class="flex items-center justify-end gap-3">
                                          <button class="flex items-center justify-center gap-2 rounded-lg h-10 px-6 bg-blue-600 text-white text-sm font-bold hover:bg-blue-700 shadow-sm transition">🚀 Aceptar</button>
                                          <button class="flex items-center justify-center gap-2 rounded-lg h-10 px-5 bg-white border border-gray-300 text-gray-700 text-sm font-bold hover:bg-gray-50 shadow-sm transition">✖️ Rechazar</button>
                                      </div>
                                  </div>
                              </div>

                              <!-- Crear Tarea -->
                              <div class="col-span-1 md:col-span-2 lg:col-span-3 mt-1">
                                  <div class="bg-gray-50 rounded-xl p-6 border border-gray-200 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
                                      <div>
                                          <p class="text-sm font-medium text-gray-500 uppercase tracking-wider mb-1">Integración con Gestor de Proyectos</p>
                                          <p class="text-lg font-bold text-gray-800 mt-1">Crear tarea / proceso: <span class="text-blue-600 block md:inline mt-0.5 md:mt-0">"Revisar informe financiero Q3"</span></p>
                                          <p class="text-sm text-gray-600 mt-2">Fecha de vencimiento SLA: <span class="font-bold text-gray-800">28 de Oct., 2024</span></p>
                                      </div>
                                      <div class="flex items-center gap-3 shrink-0 mt-2 md:mt-0">
                                          <button class="flex items-center justify-center gap-2 rounded-lg h-11 px-6 bg-green-600 text-white text-sm font-bold hover:bg-green-700 shadow-sm transition">⚡ Instanciar Proceso</button>
                                          <button class="flex items-center justify-center gap-2 rounded-lg h-11 px-6 bg-gray-500 text-white text-sm font-bold hover:bg-gray-600 shadow-sm transition">✖️ Descartar</button>
                                      </div>
                                  </div>
                              </div>
                          </div>
                      </div>
                  </div>
              </div>
          </main>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const isAiExpanded = ref(true); // Control reactivo del acordeón IA
</script>

<style scoped>
/* Transiciones suaves de CSS de la Sidebar y Paneles tomadas del HTML del cliente */
.sidebar-group:has(.sidebar-toggle:checked) .sidebar {
    width: 80px;
}
.sidebar-group:has(.sidebar-toggle:checked) .sidebar-label {
    display: none;
}
.sidebar-group:has(.sidebar-toggle:checked) .sidebar-logo-text {
    display: none;
}
.sidebar-group:has(.sidebar-toggle:checked) .sidebar-header {
    justify-content: center;
}
.sidebar-group:has(.sidebar-toggle:checked) .sidebar-toggle-button {
    transform: rotate(180deg);
}

/* Permitir usar la validación de grid-rows para animar el height en toggle */
.ai-checkbox:checked ~ div.grid {
    grid-template-rows: 1fr;
    opacity: 1;
    padding-bottom: 1.5rem;
}
.ai-checkbox:not(:checked) ~ div.grid {
    grid-template-rows: 0fr;
    opacity: 0;
    padding-bottom: 0;
}
</style>
