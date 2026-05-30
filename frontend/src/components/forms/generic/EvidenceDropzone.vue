<template>
  <div class="mb-5">
    <label class="block text-sm font-medium text-gray-700 mb-1">Evidencia (Opcional)</label>
    <div 
      class="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-md transition-colors relative"
      :class="{'border-indigo-500 bg-indigo-50': isDragging}"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="handleDrop"
    >
      <div class="space-y-1 text-center">
        <svg class="mx-auto h-12 w-12 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48" aria-hidden="true">
          <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        <div class="flex text-sm text-gray-600 justify-center">
          <label for="file-upload" class="relative cursor-pointer bg-white rounded-md font-medium text-indigo-600 hover:text-indigo-500 focus-within:outline-none">
            <span>Sube un archivo</span>
            <input id="file-upload" name="file-upload" type="file" class="sr-only" multiple @change="handleFileSelect" />
          </label>
          <p class="pl-1">o arrastra y suelta</p>
        </div>
        <p class="text-xs text-gray-500">PNG, JPG, PDF, DOCX, XLSX, PPTX, TXT, CSV hasta 25MB</p>
      </div>
    </div>
    
    <div v-if="store.files.length > 0" class="mt-3 space-y-2">
      <ul class="border border-gray-200 rounded-md divide-y divide-gray-200">
        <li v-for="(item, index) in store.files" :key="index" class="pl-3 pr-4 py-3 flex flex-col text-sm">
          <div class="flex items-center justify-between">
            <div class="w-0 flex-1 flex items-center">
              <svg class="flex-shrink-0 h-5 w-5 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M8 4a3 3 0 00-3 3v4a5 5 0 0010 0V7a1 1 0 112 0v4a7 7 0 11-14 0V7a5 5 0 0110 0v4a3 3 0 11-6 0V7a1 1 0 012 0v4a1 1 0 102 0V7a3 3 0 00-3-3z" clip-rule="evenodd" />
              </svg>
              <span class="ml-2 flex-1 w-0 truncate">
                {{ item.file.name }} ({{ (item.file.size / 1024 / 1024).toFixed(2) }}MB)
              </span>
            </div>
            
            <div class="ml-4 flex-shrink-0 flex items-center gap-3">
              <span v-if="item.status === 'SUCCESS'" class="inline-flex items-center text-green-600 bg-green-50 px-2 py-0.5 rounded text-xs font-medium">
                ✅ Completado
              </span>
              <span v-if="item.status === 'ERROR'" class="text-xs text-red-600 font-medium truncate max-w-[120px]">
                {{ item.errorMessage }}
              </span>

              <button v-if="item.status === 'UPLOADING'" @click="cancelUpload(item)" class="text-gray-500 hover:text-gray-700 text-xs px-2 py-1 border border-gray-300 rounded">
                ✕ Cancelar
              </button>
              <button v-if="item.status === 'ERROR'" @click="retryUpload(item)" class="text-indigo-600 hover:text-indigo-800 text-xs font-medium">
                Reintentar
              </button>
              <button @click="removeFile(index)" class="text-red-600 hover:text-red-800 text-xs font-medium">
                🗑️ Eliminar
              </button>
            </div>
          </div>
          
          <div v-if="item.status === 'UPLOADING'" class="mt-2 w-full bg-gray-200 rounded-full h-1.5 overflow-hidden">
            <div class="bg-indigo-600 h-1.5 transition-all duration-300" :style="{ width: item.progress + '%' }"></div>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useIntegrationStore } from '@/stores/useIntegrationStore';
import { ref } from 'vue'
import { useGenericFormStore, type UploadedFile } from '@/stores/genericFormStore'

// @Traceability: Retro-Remediación ADR-006
const integrationStore = useIntegrationStore();

const store = useGenericFormStore()
const isDragging = ref(false)

const handleDrop = (e: DragEvent) => {
  isDragging.value = false
  if (e.dataTransfer?.files) {
    addFiles(Array.from(e.dataTransfer.files))
  }
}

const handleFileSelect = (e: Event) => {
  const target = e.target as HTMLInputElement
  if (target.files) {
    addFiles(Array.from(target.files))
    target.value = '' // Reset para permitir subir el mismo archivo
  }
}

const MAX_FILE_SIZE_MB = 25
const ALLOWED_EXTS = ['.pdf', '.jpg', '.jpeg', '.png', '.gif', '.docx', '.xlsx', '.pptx', '.txt', '.csv']
const MAX_FILES = 5

const getExt = (name: string) => {
    const parts = name.split('.');
    return parts.length > 1 ? '.' + parts.pop()?.toLowerCase() : '';
}

const addFiles = (newFiles: File[]) => {
  const remaining = MAX_FILES - store.files.length
  if (remaining <= 0) return
  
  const validFiles = newFiles.filter(f => {
    if (f.size > MAX_FILE_SIZE_MB * 1024 * 1024) {
      alert(`Archivo ${f.name} excede el límite de ${MAX_FILE_SIZE_MB}MB.`);
      return false;
    }
    const ext = getExt(f.name);
    if (!ALLOWED_EXTS.includes(ext)) {
      alert(`Extensión ${ext} no permitida. Use ${ALLOWED_EXTS.join(', ')}.`);
      return false;
    }
    return true;
  });
  
  const toAdd = validFiles.slice(0, remaining).map(f => ({
      file: f,
      progress: 0,
      status: 'PENDING' as const
  }));
  
  store.files.push(...toAdd);
  
  // Start uploading right away
  toAdd.forEach(item => uploadFile(item));
}

const uploadFile = async (item: UploadedFile) => {
    item.status = 'UPLOADING';
    item.progress = 0;
    item.errorMessage = '';
    item.abortController = new AbortController();
    
    const formData = new FormData();
    formData.append('file', item.file);
    
    try {
        // @implNote Traceability: [DevDavid Merge] - Fix path duplicado (BUG-S7-001-HOTFIX)
        const res = await integrationStore.post('/documents/upload-temp', formData, {
            headers: { 
                'Content-Type': 'multipart/form-data',
                'X-Task-Id': store.taskId 
            },
            signal: item.abortController.signal,
            onUploadProgress: (progressEvent) => {
                if (progressEvent.total) {
                    item.progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                }
            }
        });
        item.status = 'SUCCESS';
        item.progress = 100;
        item.temp_id = res.data.temp_id || res.data;
    } catch(err: any) {
        if (err.name === 'CanceledError') {
            item.status = 'ERROR';
            item.errorMessage = 'Cancelado';
        } else {
            item.status = 'ERROR';
            item.errorMessage = err.response?.data?.message || err.message || 'Error de subida';
        }
    }
}

const retryUpload = (item: UploadedFile) => {
    uploadFile(item);
}

const cancelUpload = (item: UploadedFile) => {
    if (item.abortController) {
        item.abortController.abort();
    }
}

const removeFile = (index: number) => {
  const item = store.files[index];
  if (item.status === 'UPLOADING' && item.abortController) {
      item.abortController.abort();
  }
  store.files.splice(index, 1)
}
</script>
