<template>
  <div class="mb-5">
    <label class="block text-sm font-medium text-gray-700 mb-1">Evidencia (Opcional)</label>
    <div 
      class="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-md transition-colors"
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
          <label for="file-upload" class="relative cursor-pointer bg-white rounded-md font-medium text-indigo-600 hover:text-indigo-500 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-indigo-500">
            <span>Sube un archivo</span>
            <input id="file-upload" name="file-upload" type="file" class="sr-only" multiple @change="handleFileSelect" />
          </label>
          <p class="pl-1">o arrastra y suelta</p>
        </div>
        <p class="text-xs text-gray-500">PNG, JPG, PDF hasta 10MB</p>
      </div>
    </div>
    
    <div v-if="store.files.length > 0" class="mt-3">
      <ul class="border border-gray-200 rounded-md divide-y divide-gray-200">
        <li v-for="(file, index) in store.files" :key="index" class="pl-3 pr-4 py-3 flex items-center justify-between text-sm">
          <div class="w-0 flex-1 flex items-center">
            <svg class="flex-shrink-0 h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
              <path fill-rule="evenodd" d="M8 4a3 3 0 00-3 3v4a5 5 0 0010 0V7a1 1 0 112 0v4a7 7 0 11-14 0V7a5 5 0 0110 0v4a3 3 0 11-6 0V7a1 1 0 012 0v4a1 1 0 102 0V7a3 3 0 00-3-3z" clip-rule="evenodd" />
            </svg>
            <span class="ml-2 flex-1 w-0 truncate">
              {{ file.name }}
            </span>
          </div>
          <div class="ml-4 flex-shrink-0">
            <button type="button" @click="removeFile(index)" class="font-medium text-red-600 hover:text-red-500">
              Eliminar
            </button>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useGenericFormStore } from '@/stores/genericFormStore'

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
  }
}

const MAX_FILES = 5

const addFiles = (newFiles: File[]) => {
  const remaining = MAX_FILES - store.files.length
  if (remaining <= 0) return
  const allowed = newFiles.slice(0, remaining)
  store.files = [...store.files, ...allowed]
}

const removeFile = (index: number) => {
  store.files.splice(index, 1)
}
</script>
