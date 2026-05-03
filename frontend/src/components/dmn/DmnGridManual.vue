<template>
  <div class="flex-1 flex flex-col overflow-hidden bg-white rounded-lg shadow-sm border border-gray-200">
    <!-- Botón Agregar Fila y Límite -->
    <div class="p-3 border-b border-gray-200 bg-gray-50 flex justify-between items-center">
      <h3 class="font-bold text-gray-700">Editor Manual DMN</h3>
      <div class="flex items-center gap-3">
        <span v-if="rows.length >= 100" class="text-orange-500 text-xs font-bold">Límite SRE alcanzado (100)</span>
        <button 
          @click="addRow" 
          :disabled="rows.length >= 100" 
          class="btn-add-row px-3 py-1 bg-indigo-600 text-white rounded shadow-sm text-sm disabled:opacity-50 hover:bg-indigo-700 transition">
          + Agregar Fila
        </button>
      </div>
    </div>

    <!-- Contenedor con Scroll -->
    <DmnGridSearch :rows="rows" @highlight="onSearchHighlight" />
    <div class="flex-1 overflow-auto">
      <table class="w-full text-left text-sm whitespace-nowrap">
        <thead class="bg-gray-100 sticky top-0 z-10 border-b border-gray-200 shadow-sm">
          <tr>
            <th class="py-2 px-3 font-semibold text-gray-700 border-r border-gray-200 w-1/3">
              Input 1
              <select v-model="headers.input1" class="form-select block w-full mt-1 border border-gray-300 rounded p-1 text-xs">
                <option v-for="v in zodDictionary" :key="v.id" :value="v.name">
                  {{ v.name }} ({{ v.type }})
                </option>
              </select>
            </th>
            <th class="py-2 px-3 font-semibold text-gray-700 border-r border-gray-200 w-1/3">
              Input 2
              <select v-model="headers.input2" class="form-select block w-full mt-1 border border-gray-300 rounded p-1 text-xs">
                <option v-for="v in zodDictionary" :key="v.id" :value="v.name">
                  {{ v.name }} ({{ v.type }})
                </option>
              </select>
            </th>
            <th class="py-2 px-3 font-semibold text-gray-700 border-r border-gray-200 w-1/3">
              Output 1
            </th>
            <th class="py-2 px-2 text-center text-gray-700 w-12">Acción</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="(row, index) in visibleRows" :key="row.id" :class="{'catch-all': row.isLocked, 'bg-gray-50': row.isLocked}">
            <td class="px-3 py-2 border-r border-gray-100">
              <input 
                v-model="row.inputs[0]" 
                @input="validateFEELSyntax($event.target.value, row, 0)"
                :class="['w-full bg-transparent outline-none border-b focus:border-indigo-400 py-1 font-mono', row.invalidFields?.includes(0) ? 'border-red-500' : 'border-transparent', isMatch(index, 'in-0') ? 'search-highlight bg-yellow-200' : '', isActiveMatch(index, 'in-0') ? 'active-match ring-2 ring-yellow-500' : '']"
                :readonly="row.isLocked"
                placeholder="Ej: < 1000"
              />
            </td>
            <td class="px-3 py-2 border-r border-gray-100">
              <input 
                v-model="row.inputs[1]" 
                @input="validateFEELSyntax($event.target.value, row, 1)"
                :class="['w-full bg-transparent outline-none border-b focus:border-indigo-400 py-1 font-mono', row.invalidFields?.includes(1) ? 'border-red-500' : 'border-transparent', isMatch(index, 'in-1') ? 'search-highlight bg-yellow-200' : '', isActiveMatch(index, 'in-1') ? 'active-match ring-2 ring-yellow-500' : '']"
                :readonly="row.isLocked"
                placeholder="Ej: >= 50"
              />
            </td>
            <td class="px-3 py-2 border-r border-gray-100">
              <input 
                v-model="row.outputs[0]" 
                :class="['w-full bg-transparent outline-none border-b focus:border-indigo-400 py-1 font-bold border-transparent', isMatch(index, 'out-0') ? 'search-highlight bg-yellow-200' : '', isActiveMatch(index, 'out-0') ? 'active-match ring-2 ring-yellow-500' : '']"
                :readonly="row.isLocked"
              />
            </td>
            <td class="px-2 py-2 text-center">
              <span v-if="row.isLocked" class="material-symbols-outlined text-sm text-yellow-600" title="Fila Protegida">lock</span>
              <button v-else @click="removeRow(index)" class="delete-row text-red-500 hover:text-red-700 focus:outline-none">
                <span class="material-symbols-outlined text-sm">delete</span>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import DmnGridSearch from './DmnGridSearch.vue';
import DOMPurify from 'dompurify';
import apiClient from '@/services/apiClient';

const props = defineProps({
  editable: { type: Boolean, default: true }
});

const emit = defineEmits(['update:isValid']);

const searchQuery = ref('');
const searchMatches = ref<any[]>([]);
const searchCurrentIndex = ref(0);

const onSearchHighlight = (payload: any) => {
  searchQuery.value = payload.query;
  searchMatches.value = payload.matches;
  searchCurrentIndex.value = payload.currentIndex;
};

const isMatch = (rowIdx: number, colType: string) => {
  if (!searchQuery.value) return false;
  return searchMatches.value.some(m => m.rowIdx === rowIdx && m.colIdx === colType);
};

const isActiveMatch = (rowIdx: number, colType: string) => {
  if (!searchQuery.value || searchMatches.value.length === 0) return false;
  const current = searchMatches.value[searchCurrentIndex.value];
  return current.rowIdx === rowIdx && current.colIdx === colType;
};



const zodDictionary = ref<any[]>([
  { id: 1, name: 'nivel_riesgo', type: 'string' },
  { id: 2, name: 'score', type: 'number' },
  { id: 3, name: 'accion', type: 'string' }
]);

onMounted(async () => {
  try {
    const res = await apiClient.get('/forms/current/variables');
    if (res.data && res.data.length > 0) {
      zodDictionary.value = res.data;
    }
  } catch (e) {
    console.warn("Using fallback zod dictionary");
  }
});


const headers = ref({
  input1: 'nivel_riesgo',
  input2: 'score',
  output1: 'accion'
});

interface DmnRow {
  id: string;
  inputs: string[];
  outputs: string[];
  isLocked?: boolean;
  invalidFields?: number[];
}

const rows = ref<DmnRow[]>([
  { id: 'mock-initial-1', inputs: ['"ALTO"', ''], outputs: ['"Rechazar"'], invalidFields: [] },
  { id: 'catch-all', inputs: ['null', 'null'], outputs: ['"Revisión Humana"'], isLocked: true, invalidFields: [] }
]);

const validateFEELSyntax = (value: string, row: DmnRow, fieldIndex: number) => {
  // GAP-03: DOMPurify sanitize
  const sanitized = DOMPurify.sanitize(value);
  if (value !== sanitized) {
      row.inputs[fieldIndex] = sanitized;
      value = sanitized;
  }
  if (!row.invalidFields) row.invalidFields = [];
  
  if (value === '') {
     const idx = row.invalidFields.indexOf(fieldIndex);
     if (idx > -1) row.invalidFields.splice(idx, 1);
     checkFormValidity();
     return;
  }

  // Lógica ligera de FEEL: ej: '< 1000', '>= 50', '"Aprobado"'
  const feelRegex = /^(<|<=|>|>=|=|!=)?\s*("[^"]*"|\d+(\.\d+)?|true|false)$/;
  const isValid = feelRegex.test(value);
  
  if (!isValid) {
    if (!row.invalidFields.includes(fieldIndex)) {
      row.invalidFields.push(fieldIndex);
    }
  } else {
    const idx = row.invalidFields.indexOf(fieldIndex);
    if (idx > -1) row.invalidFields.splice(idx, 1);
  }
  checkFormValidity();
};

const checkFormValidity = () => {
  const isInvalid = rows.value.some(r => r.invalidFields && r.invalidFields.length > 0);
  emit('update:isValid', !isInvalid);
};

const addRow = () => {
  if (rows.value.length < 100) {
    // Insert before the catch-all row
    const catchAllIndex = rows.value.findIndex(r => r.id === 'catch-all');
    const newRow = { id: `row-${Date.now()}`, inputs: ['', ''], outputs: [''], invalidFields: [] };
    if (catchAllIndex !== -1) {
      rows.value.splice(catchAllIndex, 0, newRow);
    } else {
      rows.value.push(newRow);
    }
  }
};

const visibleRows = computed(() => rows.value.slice(0, 30));

const removeRow = (index: number) => {
  rows.value.splice(index, 1);
  checkFormValidity();
};
</script>

<style scoped>
/* Optional custom scrollbar styling could go here */
</style>
