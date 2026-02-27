<template>
  <div class="h-full bg-background p-6 overflow-auto">
    <div class="max-w-4xl mx-auto space-y-6">
      
      <!-- Cabecera de la Tarea/Expediente -->
      <header class="bg-surface rounded-xl shadow-sm border border-border p-6 flex flex-col md:flex-row justify-between items-start md:items-center">
        <div>
          <h1 class="text-2xl font-bold text-text mb-1">Revisión Legal Documental</h1>
          <p class="text-text-muted text-sm tracking-wide">Expediente: <span class="font-mono text-primary font-semibold">RAD-2024-001</span></p>
        </div>
        <div class="mt-4 md:mt-0 flex space-x-2">
           <span class="bg-blue-100 text-blue-800 text-xs font-bold px-3 py-1 rounded-full">Proceso Laboral</span>
           <span class="bg-red-100 text-red-800 text-xs font-bold px-3 py-1 rounded-full">SLA Riesgo Alto</span>
        </div>
      </header>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
         <!-- Panel de Metainformación e Historial -->
         <aside class="col-span-1 space-y-6">
            <div class="bg-surface border border-border rounded-xl shadow-sm p-5">
              <h3 class="font-bold border-b border-border pb-2 mb-3">Detalle del Involucrado</h3>
              <p class="text-sm font-semibold text-text">Juan Pérez</p>
              <p class="text-xs text-text-muted mb-4">C.C. 1.012.345.678</p>
              
              <h3 class="font-bold border-b border-border pb-2 mb-3">AI Copilot (Insights)</h3>
              <div class="bg-gray-50 border border-info border-dashed p-3 rounded text-sm text-text-muted">
                 <strong>Sugerencia DMN:</strong> Dado el umbral de impagos (3) y tipo de cliente (VIP), el sistema recomienda "Acuerdo Conciliatorio".
              </div>
            </div>
         </aside>

         <!-- Área principal de ejecución (Dynamic Form) -->
         <main class="col-span-1 md:col-span-2">
            <DynamicForm 
               :schema="sampleSchema" 
               @submit="handleTaskCompletion"
               @cancel="goBack" 
            />
         </main>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import DynamicForm from '../components/tasks/DynamicForm.vue'

const router = useRouter()

// Mock del schema recuperado del Sprint 2
const sampleSchema = ref({
  fields: [
    { key: 'decision_abogado', label: 'Decisión del Litigio', type: 'enum', options: [
      { label: 'Aprobar Conciliación', value: 'CONCILIACION' },
      { label: 'Escalar a Juez', value: 'ESCALAMIENTO' }
    ], required: true },
    { key: 'comentarios_cierre', label: 'Comentarios o Justificación', type: 'string', required: false },
    { key: 'enviar_email_cliente', label: 'Notificar Cierre al Cliente (M365)', type: 'boolean' }
  ]
})

const handleTaskCompletion = (data: any) => {
  console.log("Completando tarea con payload:", data)
  alert("Tarea enviada al orquestador Camunda con éxito.")
  router.push('/inbox')
}

const goBack = () => {
  router.push('/inbox')
}
</script>
