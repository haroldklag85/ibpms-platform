<template>
  <div v-if="show" class="fixed inset-0 bg-black/60 z-[100] flex items-center justify-center p-4 backdrop-blur-sm">
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-2xl w-full max-w-4xl flex flex-col overflow-hidden max-h-[90vh]">
      
      <!-- Header -->
      <div class="px-6 py-4 bg-indigo-50 dark:bg-indigo-900/30 border-b border-indigo-100 dark:border-indigo-800 flex items-center justify-between shrink-0">
        <div>
          <h3 class="text-xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
            🧬 Gestor de Instancias Activas (Cirugía Quirúrgica)
            <span v-if="isSandbox" class="text-xs bg-purple-100 text-purple-800 border border-purple-300 px-2 py-0.5 rounded shadow-sm font-bold ml-2 whitespace-nowrap">🧪 MAX 5 INSTANCIAS SIMULTÁNEAS</span>
          </h3>
          <p class="text-xs text-indigo-700 dark:text-indigo-300 mt-1">
            Migración de tokens de versiones obsoletas hacia la nueva topología. Regla Zero Data-Patching activa (CA-10).
          </p>
        </div>
        <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600 text-2xl font-bold">&times;</button>
      </div>

      <!-- Body / Table -->
      <div class="flex-1 overflow-y-auto p-6 bg-gray-50 dark:bg-gray-900">
        <div v-if="loading" class="text-center py-10 opacity-70 animate-pulse font-bold text-indigo-600">
          Escaneando topología y tokens en vuelo...
        </div>
        
        <table v-else class="w-full text-sm bg-white dark:bg-gray-800 shadow rounded-lg overflow-hidden border border-gray-200 dark:border-gray-700">
          <thead class="bg-gray-100 dark:bg-gray-700 border-b border-gray-200 dark:border-gray-600">
            <tr>
              <!-- EXPRESAMENTE PROHIBIDO (CA-8): Select All (Por esto no hay checkbox en el thead) -->
              <th class="w-12 text-center py-3 px-2">Migrar</th>
              <th class="text-left font-bold text-gray-600 dark:text-gray-300 py-3 px-4">Instancia ID</th>
              <th class="text-left font-bold text-gray-600 dark:text-gray-300 py-3 px-4">Versión Origen</th>
              <th class="text-left font-bold text-gray-600 dark:text-gray-300 py-3 px-4">Nodo Actual (Token)</th>
              <th class="text-left font-bold text-gray-600 dark:text-gray-300 py-3 px-4">Validación Topológica (CA-9)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="instance in mockedInstances" :key="instance.id" 
                class="border-b border-gray-100 dark:border-gray-700 transition"
                :class="{ 'opacity-60 bg-red-50/30 dark:bg-red-900/10': !instance.isMigratable, 'hover:bg-indigo-50 dark:hover:bg-indigo-900/20': instance.isMigratable }">
              
              <td class="text-center py-3 px-2">
                <input 
                  type="checkbox" 
                  :value="instance.id" 
                  v-model="selectedInstances"
                  class="rounded text-indigo-600 focus:ring-indigo-500 w-4 h-4 cursor-pointer disabled:cursor-not-allowed disabled:bg-gray-200"
                  :disabled="!instance.isMigratable || isMigrating"
                />
              </td>
              <td class="py-3 px-4 font-mono text-xs text-gray-800 dark:text-gray-200">{{ instance.id }}</td>
              <td class="py-3 px-4 text-xs font-bold text-gray-500">{{ instance.version }}</td>
              <td class="py-3 px-4 text-xs">
                <span class="bg-blue-100 text-blue-800 px-2 py-0.5 rounded-full font-medium">{{ instance.currentNode }}</span>
              </td>
              <td class="py-3 px-4 text-xs">
                <span v-if="instance.isMigratable" class="text-green-600 font-bold flex items-center gap-1">
                  ✅ Destino Compatible
                </span>
                <span v-else class="text-red-600 font-bold flex items-center gap-1" title="El identificador técnico del nodo no se halló en la versión V2 de destino.">
                  🚫 Bloqueo: El nodo actual no existe en la Versión Destino
                </span>
              </td>
            </tr>
            <tr v-if="mockedInstances.length === 0">
              <td colspan="5" class="text-center py-6 text-gray-500 italic">No hay instancias antiguas en vuelo para este proceso.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Footer Actions -->
      <div class="px-6 py-4 border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 flex justify-between items-center shrink-0">
        <span class="text-xs font-bold text-gray-500 dark:text-gray-400">
           Seleccionadas: <span class="text-indigo-600">{{ selectedInstances.length }}</span> tokens
        </span>
        <div class="flex gap-3">
          <button @click="$emit('close')" :disabled="isMigrating" class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition disabled:opacity-50">
            Cancelar
          </button>
          <button @click="executeMigration" :disabled="selectedInstances.length === 0 || isMigrating" class="px-4 py-2 text-sm font-bold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg shadow transition disabled:opacity-50 flex items-center gap-2">
            {{ isMigrating ? 'Empujando...' : '🚀 Ejecutar Migración Seleccionada' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

const props = defineProps({
  show: { type: Boolean, default: false },
  processId: { type: String, required: true },
  isSandbox: { type: Boolean, default: false }
});

const emit = defineEmits(['close', 'success']);

const loading = ref(true);
const isMigrating = ref(false);
const selectedInstances = ref<string[]>([]);

// Mocked DB state
const mockedInstances = ref([
  { id: '10f9-a1b2-inst-001', version: 'V1', currentNode: 'Task_AprobarVentas', isMigratable: true },
  { id: '20c4-f8d9-inst-002', version: 'V1', currentNode: 'Task_ValidarCompliance', isMigratable: true },
  { id: '39a1-b6e5-inst-003', version: 'V1', currentNode: 'Task_LegacyVerification', isMigratable: false }, // Guillotina visual (CA-9)
  { id: '44d8-c2a1-inst-004', version: 'V1', currentNode: 'Task_AprobarVentas', isMigratable: true }
]);

onMounted(() => {
  // Simulate network fetch
  setTimeout(() => {
    loading.value = false;
  }, 800);
});

const executeMigration = async () => {
  if (selectedInstances.value.length === 0) return;
  isMigrating.value = true;
  
  try {
    // CA-10 Payload Strict Rule: Zero Data Patching. SOLO arreglo de IDs.
    const payload: any = {
      instanceIds: selectedInstances.value
    };
    if (props.isSandbox) {
       payload.isSandbox = true;
       // CA-67: Limitar envío a un máximo de 5. Array slice preventivo en frontend
       payload.instanceIds = payload.instanceIds.slice(0, 5); 
    }
    
    // Simulate API POST /api/v1/design/processes/migrate
    console.log('[InstancesManager] Executing Migration Payload:', payload);
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    emit('success', `${selectedInstances.value.length} instancias empujadas a la nueva versión exitosamente.`);
    emit('close');
  } catch (error) {
    console.error('Migration failed', error);
    alert('Fallo en la comunicación con el motor transaccional.');
  } finally {
    isMigrating.value = false;
  }
};
</script>
