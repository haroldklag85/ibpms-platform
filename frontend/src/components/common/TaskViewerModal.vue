<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useLocalStorage } from '@vueuse/core';
import { z } from 'zod';
import apiClient from '@/services/apiClient';
import FormRenderer from '@/components/forms/FormRenderer.vue';

// ==========================================
// 1. PROPS Y TYPES (El Camaleón)
// ==========================================
interface TaskContext {
  taskId: string;
  sourceEngine: 'BPMN' | 'AGILE'; // Camunda vs Gantt/Kanban
  prefillData: {
    Case_ID: string;
    Client_Name: string;
    Priority: string;
    SLA: string;
    [key: string]: any; // Whitelist ya filtrado por BFF
  };
  requiresEvidence?: boolean; // CA-5 Flag de Fricción TRD
  formSnapshot?: any[]; // CA-78 Ley del Abuelo In-Flight
}

const props = defineProps<{
  isOpen: boolean;
  context: TaskContext;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'save-progress', payload: any): void;
  (e: 'complete', payload: any): void;
  (e: 'error-boundary', payload: any): void;
}>();

// ==========================================
// 2. RESILIENCIA Y AUTOSAVE (LocalStorage)
// ==========================================
// Clave dinámica inquebrantable atada al TaskID
const storageKey = computed(() => `draft_task_${props.context?.taskId}`);

const formData = useLocalStorage(storageKey.value, {
  progressPercentage: 0,
  comments: '',
  attachments: [] as string[], // Arreglo de UUIDs
  documentType: '', // CA-5 Tipificación SGDEA
  isEscalated: false // Toggle Botón Pánico
});

// Purga de seguridad al montar si el ID cambia
watch(() => props.context?.taskId, (newId) => {
  if (newId) {
    formData.value = JSON.parse(localStorage.getItem(`draft_task_${newId}`) || '{"progressPercentage":0,"comments":"","attachments":[],"documentType":"","isEscalated":false}');
  }
});

// ==========================================
// 3. MUTACIÓN INTELIGENTE (Zod superRefine)
// ==========================================
const genericFormSchema = z.object({
  progressPercentage: z.number().min(0).max(100),
  comments: z.string().optional(),
  attachments: z.array(z.string()).optional(),
  documentType: z.string().optional(),
  isEscalated: z.boolean().default(false)
}).superRefine((data, ctx) => {
  // Regla 1: Fricción por Avance Parcial (Agile)
  if (props.context.sourceEngine === 'AGILE' && data.progressPercentage < 100) {
    if (!data.comments || data.comments.trim() === '') {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ['comments'], message: 'El comentario es ESTRICTAMENTE OBLIGATORIO para guardar avances parciales menores al 100%.' });
    }
  }

  // Regla 2: El 'Botón de Pánico' (Escalar a Nivel 2)
  if (data.isEscalated) {
    if (!data.comments || data.comments.trim() === '') {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ['comments'], message: 'Debe justificar OBLIGATORIAMENTE el motivo técnico para disparar el Exception Boundary Event.' });
    }
  }

  // Regla 3: Bloqueo de Subida de Evidencia SGDEA (CA-5)
  if (props.context.requiresEvidence) {
    if (!data.attachments || data.attachments.length === 0) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ['attachments'], message: 'La política de auditoría exige al menos un (1) soporte documental (TRD).' });
    }
    if (!data.documentType || data.documentType === '') {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ['documentType'], message: 'Debe tipificar la naturaleza del comprobante anexo.' });
    }
  }
});

// Estado de validación
const errors = ref<Record<string, string>>({});

const validateForm = () => {
  try {
    genericFormSchema.parse(formData.value);
    errors.value = {};
    return true;
  } catch (err) {
    if (err instanceof z.ZodError) {
      const formattedErrors: Record<string, string> = {};
      err.errors.forEach((e) => {
        if (e.path[0]) formattedErrors[e.path[0].toString()] = e.message;
      });
      errors.value = formattedErrors;
    }
    return false;
  }
};

// ==========================================
// 4. MÉTODOS TRANSACCIONALES
// ==========================================
// Estado para Loader de Envío
const isSubmitting = ref(false);

const submitTaskPayload = async (eventType: 'save-progress' | 'complete' | 'error-boundary') => {
  if (!validateForm()) return;
  
  const safeTaskId = String(props.context.taskId).replace(/[^a-zA-Z0-9]/g, '');
  const prefix = `${safeTaskId}_`;

  const payloadRaw = formData.value;
  const namespacedPayload = Object.keys(payloadRaw).reduce((acc: Record<string, any>, key) => {
    acc[`${prefix}${key}`] = payloadRaw[key as keyof typeof payloadRaw];
    return acc;
  }, {});

  try {
     isSubmitting.value = true;
     // CA-6: El Modal asume la responsabilidad transaccional para atrapar el 403 SoD
     let endpoint = `/api/v1/tasks/${props.context.taskId}/complete`;
     if (eventType === 'save-progress') endpoint = `/api/v1/tasks/${props.context.taskId}/save`;
     if (eventType === 'error-boundary') endpoint = `/api/v1/tasks/${props.context.taskId}/escalate`;
     
     // Simulamos la llamada (O descomentar en Prod)
     // await apiClient.post(endpoint, namespacedPayload);
     
     // Simular Fallback: si es 'complete', forzamos el Happy Path o Error Mock
     emit(eventType as any, namespacedPayload);
     flushStateIfTerminal(eventType);
  } catch (error: any) {
     // CA-6 Interceptor de Conflicto de Interés SoD
     if (error.response?.status === 403) {
        alert('⛔ Bloqueo de Auditoría (SoD): No puede procesar una tarea en una instancia que usted originó o radicó inicialmente.');
     } else {
        alert('Se produjo un error transaccional enviando la tarea.');
     }
  } finally {
     isSubmitting.value = false;
  }
};

const flushStateIfTerminal = (eventType: 'save-progress' | 'complete' | 'error-boundary') => {
  if (eventType === 'complete' || eventType === 'error-boundary') {
    localStorage.removeItem(storageKey.value);
    formData.value = { progressPercentage: 0, comments: '', attachments: [], documentType: '', isEscalated: false };
    emit('close');
  }
};

const handleComplete = () => {
  if (props.context.sourceEngine === 'BPMN') {
    formData.value.progressPercentage = 100;
  }
  submitTaskPayload('complete');
};

const handleEscalate = () => {
  formData.value.isEscalated = true;
  submitTaskPayload('error-boundary');
};
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 backdrop-blur-sm transition-opacity">
    <div class="bg-white w-full max-w-2xl rounded-xl shadow-2xl flex flex-col overflow-hidden">
      
      <!-- HEADER CONTEXTUAL READ-ONLY -->
      <header class="bg-gray-50 border-b p-4">
        <div class="flex justify-between items-start">
          <div>
            <h2 class="text-xl font-bold text-gray-800">
               {{ context.sourceEngine === 'BPMN' ? 'Validación de Tarea (Camunda)' : 'Reporte Operativo (Kanban)' }}
            </h2>
            <p class="text-sm text-gray-500 mt-1">Ref: {{ context.taskId }}</p>
          </div>
          <button @click="emit('close')" class="text-gray-400 hover:text-gray-600 transition-colors">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </div>

        <!-- Grid de Metadatos Anti Context-Bleeding -->
        <div class="mt-4 grid grid-cols-2 gap-4 text-sm bg-blue-50/50 p-3 rounded border border-blue-100">
          <div v-for="(val, key) in context.prefillData" :key="key" class="flex flex-col">
            <span class="text-blue-800 font-semibold uppercase text-xs">{{ key.replace('_', ' ') }}</span>
            <span class="text-gray-700 font-medium truncate">{{ val }}</span>
          </div>
        </div>
      </header>

      <!-- BODY DEL FORMULARIO CHAMELEON -->
      <div class="p-6 flex-1 overflow-y-auto space-y-6">
        
        <!-- Si tiene Snapshot de Formulario (CA-78) -->
        <div v-if="context.formSnapshot && context.formSnapshot.length > 0" class="bg-indigo-50/30 p-4 rounded-xl border border-indigo-100 mb-4">
           <h3 class="text-xs font-bold text-indigo-800 mb-4 border-b border-indigo-100 pb-2 flex items-center gap-2"><span class="material-symbols-outlined text-[14px]">history</span> Snapshot In-Flight (CA-78)</h3>
           <FormRenderer :schema="context.formSnapshot" v-model="formData" />
        </div>

        <!-- Slider de Progreso (Solamente en AGILE y si NO hay formSnapshot) -->
        <div v-if="context.sourceEngine === 'AGILE' && (!context.formSnapshot || context.formSnapshot.length === 0)" class="space-y-2">
          <label class="block text-sm font-semibold text-gray-700 flex justify-between">
            <span>Progreso Físico de la Tarea</span>
            <span class="text-blue-600 font-bold">{{ formData.progressPercentage }}%</span>
          </label>
          <input 
            type="range" min="0" max="100" step="5"
            v-model.number="formData.progressPercentage"
            class="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-blue-600"
          >
        </div>

        <!-- Observations Area (Friction Tax Mapped via Zod) -->
        <div class="space-y-2">
          <label class="block text-sm font-semibold text-gray-700">
            Observaciones o Comentarios <span v-if="(context.sourceEngine === 'AGILE' && formData.progressPercentage < 100) || formData.isEscalated" class="text-red-500">*</span>
          </label>
          <textarea
            v-model="formData.comments"
            rows="4"
            class="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all resize-none shadow-sm"
            :class="{'border-red-300 bg-red-50': errors.comments, 'border-gray-300': !errors.comments}"
            placeholder="Ingrese soporte textual si el avance es parcial o hay anomalías..."
          ></textarea>
          <p v-if="errors.comments" class="text-red-500 text-xs font-semibold animate-pulse">{{ errors.comments }}</p>
        </div>

        <!-- SGDEA Dropzone (Mock/Placeholder for US-029) y Mutante CA-5 -->
        <div class="space-y-4">
          <div class="space-y-2">
            <label class="block text-sm font-semibold text-gray-700">Evidencia Documental (SGDEA) <span v-if="context.requiresEvidence" class="text-red-500">*</span></label>
            <!-- Mock Attach array push to pass validation -->
            <div @click="formData.attachments?.push(`DOC-${Math.random().toString(36).substr(2,6)}`)" class="border-2 border-dashed rounded-lg p-6 flex flex-col items-center justify-center text-gray-500 hover:bg-gray-50 hover:border-blue-400 transition-colors cursor-pointer group" :class="errors.attachments ? 'border-red-400 bg-red-50' : 'border-gray-300'">
              <svg class="w-8 h-8 text-gray-400 group-hover:text-blue-500 mb-2 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path></svg>
              <p class="text-sm font-medium">Arrastre comprobantes en PDF (Upload-First)</p>
              <p class="text-[10px] mt-2 bg-gray-200 px-2 rounded">{{ formData.attachments?.length || 0 }} subidos (Click para Mock)</p>
            </div>
            <p v-if="errors.attachments" class="text-red-500 text-xs font-semibold animate-pulse">{{ errors.attachments }}</p>
          </div>

          <!-- Select de Tipo Documental (Visibilidad Condicional CA-5) -->
          <div v-if="context.requiresEvidence" class="space-y-2 bg-blue-50/40 p-3 rounded border border-blue-100">
             <label class="block text-sm font-semibold text-gray-700">Clasificación TRD Externa <span class="text-red-500">*</span></label>
             <select v-model="formData.documentType" class="w-full text-sm p-2 border rounded-lg focus:ring-blue-500 text-gray-700" :class="errors.documentType ? 'border-red-400' : 'border-gray-300'">
                <option value="" disabled>Seleccione Clasificación...</option>
                <option value="CONTRATO">Contrato Marco / Orden de Compra</option>
                <option value="FACTURA">Facturación / Timbre Electrónico</option>
                <option value="IDENTIFICACION">Documento Identidad (KYC/AML)</option>
                <option value="OTROS">Anexos Multipropósito (Auditoría)</option>
             </select>
             <p v-if="errors.documentType" class="text-red-500 text-xs font-semibold">{{ errors.documentType }}</p>
          </div>
        </div>

      </div>

      <!-- FOOTER DE ACCIONES -->
      <footer class="bg-gray-50 border-t p-4 flex gap-3 justify-end items-center">
        <!-- Error Boundary (Solo BPMN) -->
        <button 
          v-if="context.sourceEngine === 'BPMN'"
          @click="handleEscalate"
          class="px-4 py-2 border-2 border-red-200 text-red-600 font-semibold rounded-lg hover:bg-red-50 hover:border-red-300 transition-all flex items-center gap-2"
        >
          <span>⚠️ Escalar Incidencia</span>
        </button>

        <div class="flex-1"></div>

        <!-- Guardar Progreso (Solo AGILE y <100%) -->
        <button 
          v-if="context.sourceEngine === 'AGILE' && formData.progressPercentage < 100"
          @click="clearCacheAndEmit('save-progress')"
          class="px-5 py-2 bg-white border-2 border-blue-600 text-blue-600 font-bold rounded-lg hover:bg-blue-50 focus:ring-4 focus:ring-blue-100 transition-all"
        >
          💾 Guardar Progreso
        </button>

        <!-- Completar Definitivo -->
        <button 
          @click="handleComplete"
          class="px-6 py-2 bg-blue-600 text-white font-bold rounded-lg shadow-md hover:bg-blue-700 focus:ring-4 focus:ring-blue-300 hover:shadow-lg transition-all flex items-center gap-2"
        >
          <span>{{ context.sourceEngine === 'BPMN' ? 'Completar Tarea' : 'Cerrar Tarea (100%)' }}</span>
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
        </button>
      </footer>

    </div>
  </div>
</template>
