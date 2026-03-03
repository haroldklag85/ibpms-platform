<template>
  <div class="h-full flex flex-col bg-gray-100 relative">
    
    <!-- No selected state -->
    <div v-if="!document" class="flex-1 flex flex-col items-center justify-center p-8 text-center border-l border-gray-200">
      <div class="w-16 h-16 bg-white rounded-full flex items-center justify-center mb-4 shadow-sm">
        <svg class="w-8 h-8 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path></svg>
      </div>
      <h3 class="text-sm font-bold text-gray-700 mb-1">Ningún documento seleccionado</h3>
      <p class="text-[11px] text-gray-500 max-w-xs">Selecciona "Ver Archivo" en la tabla para previsualizar el binario sin descargarlo a tu equipo local (Política de Seguridad CA-7).</p>
    </div>

    <!-- Active Viewer -->
    <div v-else class="flex-1 flex flex-col border-l border-gray-200">
      
      <!-- Toolbar Header -->
      <div class="bg-gray-800 px-4 py-3 flex justify-between items-center z-10 shadow-sm shrink-0">
        <div class="flex items-center gap-3 overflow-hidden">
          <div class="bg-indigo-500 text-white text-[10px] font-bold px-2 py-0.5 rounded shadow-sm shrink-0">
            {{ document.type }}
          </div>
          <h3 class="text-sm font-bold text-white truncate" :title="document.name">{{ document.name }}</h3>
        </div>
        
        <div class="flex items-center gap-2">
          <!-- Tooltips preventivos -->
          <div class="group relative cursor-help">
             <button class="text-gray-400 hover:text-white transition">
               <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
             </button>
             <div class="absolute right-0 top-full mt-2 w-48 p-2 bg-gray-900 border border-gray-700 text-[10px] text-gray-300 rounded shadow-xl hidden group-hover:block z-50">
               Modo Read-Only Activo. Acciones bloqueadas para evitar filtraciones de datos locales (Zero-Trust Endpoint CA-7).
             </div>
          </div>
          <button @click="$emit('close')" class="text-gray-400 hover:text-white transition bg-gray-700 hover:bg-gray-600 rounded p-1">
             <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </div>
      </div>

      <!-- Iframe Wrapper -->
      <div class="flex-1 bg-[#323639] relative flex flex-col items-center justify-center p-4">
        
        <div class="absolute top-4 left-4 right-4 bg-gray-900/80 backdrop-blur border border-gray-700 rounded p-3 shadow-2xl flex items-center justify-between text-xs text-white z-20">
           <div class="flex flex-col">
             <span class="text-gray-400 font-bold text-[10px] uppercase tracking-wider">Metadatos de Inyección</span>
             <span class="font-mono mt-0.5 text-indigo-300">Hash: {{ document.sha256.substring(0, 16) }}...</span>
           </div>
           <div class="text-right flex flex-col items-end">
             <span class="text-gray-400 font-bold text-[10px] uppercase tracking-wider">Custodia Física</span>
             <span class="font-mono mt-0.5 text-green-400">SharePoint V1 / URL Segura</span>
           </div>
        </div>

        <div class="w-full h-full bg-white rounded shadow-2xl overflow-hidden mt-12 border border-black group relative">
          <!-- Simulación de visor -->
          <iframe 
            v-if="document.type === 'PDF'"
            :src="document.url + '#toolbar=0&navpanes=0&scrollbar=1'" 
            class="w-full h-full"
            title="PDF Secure Viewer"
            sandbox="allow-scripts allow-same-origin"
          ></iframe>
          <div v-else class="w-full h-full flex flex-col items-center justify-center bg-gray-50 text-gray-400">
             <span class="text-4xl mb-4">🗂️</span>
             <p class="text-sm font-medium">Buscando previsualizador para {{ document.type }}</p>
          </div>
          
          <!-- Capa anti-descarga transparente por UI (Bloquea clicks y right clics al body del iframe) -->
          <div class="absolute inset-0 bg-transparent z-10" @contextmenu.prevent></div>
        </div>

      </div>
    </div>

  </div>
</template>

<script setup lang="ts">

defineProps<{
  document: {
    id: string;
    name: string;
    type: string;
    sha256: string;
    url: string;
  } | null
}>();

defineEmits(['close']);
</script>
