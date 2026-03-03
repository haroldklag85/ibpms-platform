<template>
  <div class="h-full w-full bg-gray-50 flex flex-col p-6 overflow-hidden relative" v-cloak>
    
    <!-- ═══════ Toast Notifications ═══════ -->
    <Transition name="toast-slide">
      <div v-if="toast.msg" :class="toast.type === 'success' ? 'bg-green-600' : 'bg-red-600'" class="fixed top-4 right-4 z-[100] text-white px-5 py-3 rounded-lg shadow-xl flex items-center space-x-3">
        <span class="text-sm font-medium">{{ toast.msg }}</span>
        <button @click="toast.msg = ''" class="ml-2 opacity-70 hover:opacity-100">&times;</button>
      </div>
    </Transition>

    <header class="flex justify-between items-center mb-6 shrink-0">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 flex items-center gap-2">
          📁 Bóveda Documental (SGDEA)
        </h1>
        <p class="text-sm text-gray-500 mt-1">Expediente Oficial Electrónico. Los binarios descansan en el Gestor Documental Corporativo (CA-14).</p>
      </div>
      
      <!-- Upload Dropzone Button -->
      <div class="relative">
        <input 
          type="file" 
          id="fileUpload" 
          class="hidden" 
          @change="handleFileUpload" 
          accept=".pdf,.xml,.docx,.p7m" 
        />
        <label for="fileUpload" class="bg-indigo-600 text-white px-4 py-2 rounded-md shadow text-sm font-medium hover:bg-indigo-700 transition flex items-center gap-2 cursor-pointer" :class="{ 'opacity-50 cursor-not-allowed': uploading }">
          <svg v-if="!uploading" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path></svg>
          <svg v-else class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path></svg>
          Adjuntar Documento Nuevo
        </label>
      </div>
    </header>

    <main class="flex-1 flex gap-6 min-h-0 bg-white border border-gray-200 shadow-sm rounded-xl overflow-hidden p-1">
      
      <!-- Panel Izquierdo: Data Grid (CA-13) -->
      <section class="flex-1 border-r border-gray-200 flex flex-col overflow-hidden">
        <div class="px-4 py-3 bg-gray-50 border-b border-gray-200">
           <h2 class="text-xs font-bold text-gray-700 uppercase tracking-widest flex justify-between">
             Inventario del Expediente
             <span class="text-indigo-600 font-mono">{{ documents.length }} Ítems</span>
           </h2>
        </div>
        
        <div class="overflow-y-auto flex-1">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-white sticky top-0 shadow-sm">
              <tr>
                <th scope="col" class="px-4 py-3 text-left text-[10px] font-bold text-gray-400 uppercase tracking-wider w-10">Id</th>
                <th scope="col" class="px-4 py-3 text-left text-[10px] font-bold text-gray-400 uppercase tracking-wider">Metadato Principal</th>
                <th scope="col" class="px-4 py-3 text-left text-[10px] font-bold text-gray-400 uppercase tracking-wider">Fecha Retención (TRD)</th>
                <th scope="col" class="px-4 py-3 text-left text-[10px] font-bold text-gray-400 uppercase tracking-wider hidden md:table-cell">Firma SHA-256</th>
                <th scope="col" class="px-4 py-3 text-right text-[10px] font-bold text-gray-400 uppercase tracking-wider">Visualizar</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-100">
              <tr v-for="doc in documents" :key="doc.id" class="hover:bg-indigo-50/50 transition" :class="{'bg-indigo-50/20': selectedDoc?.id === doc.id}">
                <td class="px-4 py-4 whitespace-nowrap text-xs font-mono text-gray-400">
                  {{ doc.id }}
                </td>
                <td class="px-4 py-4">
                  <div class="flex items-center">
                    <span class="text-2xl mr-3">{{ doc.type === 'PDF' ? '📕' : '📘' }}</span>
                    <div>
                      <div class="text-sm font-bold text-gray-900">{{ doc.name }}</div>
                      <div class="text-[10px] text-gray-500 font-mono flex gap-2 mt-0.5">
                        <span class="bg-gray-100 px-1 rounded">{{ doc.size }}</span>
                        <span>Dcto. Tipo: {{ doc.category }}</span>
                      </div>
                    </div>
                  </div>
                </td>
                <td class="px-4 py-4 whitespace-nowrap">
                  <!-- Expiration Date (TRD) (CA-13) -->
                  <div class="text-xs font-medium text-gray-900">{{ doc.trdDate }}</div>
                  <div class="text-[9px] text-red-500 font-bold uppercase">{{ doc.retentionYears }} Años Retención</div>
                </td>
                <td class="px-4 py-4 hidden md:table-cell max-w-[200px]">
                  <!-- SHA-256 Hash natively displayed (CA-13) -->
                  <div class="text-[9px] font-mono text-gray-400 truncate bg-gray-50 border border-gray-100 px-2 py-1 rounded" :title="doc.sha256">
                    {{ doc.sha256 }}
                  </div>
                  <div class="text-[9px] text-green-600 font-bold mt-1 flex items-center gap-1">
                    <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                    Firma Verificada
                  </div>
                </td>
                <td class="px-4 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <button @click="viewDocument(doc)" class="text-indigo-600 hover:text-indigo-900 bg-indigo-50 px-3 py-1.5 rounded text-xs font-bold border border-indigo-100 hover:border-indigo-300 transition">
                    Ver Archivo
                  </button>
                </td>
              </tr>
              <tr v-if="documents.length === 0">
                 <td colspan="5" class="px-4 py-12 text-center text-gray-400">
                    No hay documentos indexados en el SGDEA para esta instancia.
                 </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- Panel Derecho: Visor de PDF (PdfViewerPane.vue) -->
      <section class="w-1/3 min-w-[350px] max-w-[500px] flex flex-col bg-gray-100">
        <PdfViewerPane :document="selectedDoc" @close="selectedDoc = null" />
      </section>

    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import PdfViewerPane from './PdfViewerPane.vue';

interface DocumentFile {
  id: string;
  name: string;
  size: string;
  category: string;
  type: string;
  trdDate: string;
  retentionYears: number;
  sha256: string;
  url: string;
}

const documents = ref<DocumentFile[]>([
  {
    id: 'DOC-001',
    name: 'Formulario_Solicitud_Credito_Firmado.pdf',
    size: '1.2 MB',
    category: 'Oficio Solicitud',
    type: 'PDF',
    trdDate: '2036-03-03', // 10 years
    retentionYears: 10,
    sha256: 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
    url: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf' // Dummy PDF para el iframe
  },
  {
    id: 'DOC-002',
    name: 'Cedula_Representante_Legal.pdf',
    size: '450 KB',
    category: 'Identidad',
    type: 'PDF',
    trdDate: '2126-03-03', // 100 years
    retentionYears: 100,
    sha256: '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
    url: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf'
  }
]);

const selectedDoc = ref<DocumentFile | null>(null);
const uploading = ref(false);
const toast = ref<{ msg: string; type: 'success' | 'error' }>({ msg: '', type: 'success' });

const showToast = (msg: string, type: 'success' | 'error' = 'success') => {
  toast.value = { msg, type };
  setTimeout(() => { toast.value.msg = ''; }, 5000);
};

// ==========================================
// Integridad: Upload Validation (CA-11)
// ==========================================
const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (!target.files || target.files.length === 0) return;
  
  const file = target.files[0];
  const MAX_SIZE_BYTES = 50 * 1024 * 1024; // 50MB
  
  // 1. Validar límite duro antes de tocar la red
  if (file.size > MAX_SIZE_BYTES) {
    // a) Mostrar error genérico y silencioso al usuario
    showToast(`El archivo excede el límite permitido. Por favor contacte soporte.`, 'error');
    
    // b) Enviar log de ERROR silencioso al APIM / Tracing (Simulación)
    console.error(`[SECURITY TRACING]: Intento de subida masiva bloqueado en Client-Side (VUE). 
      Size: ${(file.size / 1024 / 1024).toFixed(2)} MB 
      File: ${file.name}
      Max Allowed: 50.0 MB
      Action: Terminating transaction before hitting API Gateway.`);
      
    // Limpiar input
    target.value = '';
    return;
  }
  
  // 2. Transmisión simulada si es válido
  uploading.value = true;
  
  setTimeout(() => {
    uploading.value = false;
    documents.value.unshift({
      id: `DOC-00${documents.value.length + 1}`,
      name: file.name,
      size: `${(file.size / 1024 / 1024).toFixed(2)} MB`,
      category: 'Anexo Técnico',
      type: 'PDF',
      trdDate: '2040-01-01',
      retentionYears: 15,
      sha256: '9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08', // mock hash
      url: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf'
    });
    showToast('Documento encriptado y depositado en la Bóveda.', 'success');
    target.value = ''; // Reset
  }, 1500);
};

const viewDocument = (doc: DocumentFile) => {
  selectedDoc.value = doc;
};
</script>

<style scoped>
/* Transiciones para el Toast */
.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: all 0.3s ease;
}
.toast-slide-enter-from {
  opacity: 0;
  transform: translateX(100%);
}
.toast-slide-leave-to {
  opacity: 0;
  transform: translateX(100%) translateY(-20px);
}
</style>
