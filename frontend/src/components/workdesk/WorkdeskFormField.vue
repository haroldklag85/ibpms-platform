<template>
  <div v-if="isVisible" class="mb-4">
     <label v-if="node.type !== 'container' && node.type !== 'tabs'" class="block text-sm font-bold text-gray-700 mb-1">
        {{ node.label }} <span v-if="node.required" class="text-red-500">*</span>
     </label>

     <!-- Básicos -->
     <input v-if="['text', 'password', 'email', 'url', 'number'].includes(node.type)" 
        :type="node.type === 'number' ? 'number' : node.type"
        v-model="internalValue"
        :placeholder="node.placeholder"
        :disabled="isDisabled"
        class="form-input w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm disabled:bg-gray-100" />
     
     <textarea v-else-if="node.type === 'textarea'"
        v-model="internalValue"
        :placeholder="node.placeholder"
        :rows="node.rows || 3"
        :disabled="isDisabled"
        class="form-textarea w-full rounded-md border-gray-300 shadow-sm sm:text-sm disabled:bg-gray-100"></textarea>

     <select v-else-if="node.type === 'select'"
        v-model="internalValue"
        :disabled="isDisabled"
        class="form-select w-full rounded-md border-gray-300 shadow-sm sm:text-sm disabled:bg-gray-100">
        <option value="" disabled>{{ node.placeholder || 'Seleccione...' }}</option>
        <option v-for="opt in node.options || []" :key="opt" :value="opt">{{ opt }}</option>
     </select>

     <div v-else-if="node.type === 'checkbox'" class="flex items-center gap-2">
        <input type="checkbox" v-model="internalValue" :disabled="isDisabled" class="text-indigo-600 rounded border-gray-300 focus:ring-indigo-500" />
        <span class="text-sm font-medium">{{ node.placeholder || node.label }}</span>
     </div>

     <!-- Contenedores Recursivos -->
     <div v-else-if="node.type === 'container'" class="p-4 border border-indigo-100 bg-indigo-50/20 rounded-lg">
        <h3 class="text-sm font-bold text-indigo-800 mb-3 tracking-wider">{{ node.label }}</h3>
        <div class="space-y-4">
           <WorkdeskFormField 
              v-for="(child, idx) in node.children || []" 
              :key="child.id || idx"
              :node="child"
              v-model="formData"
              :mockContext="mockContext"
              :isHighDensity="isHighDensity"
           />
        </div>
     </div>

     <!-- CA-90: Tabs con Lazy Mount Mitigación -->
     <div v-else-if="node.type === 'tabs'" class="border border-gray-200 bg-white shadow-sm rounded-lg overflow-hidden">
        <div class="flex border-b border-gray-200 bg-gray-50 px-2 pt-2 overflow-x-auto gap-1">
           <button v-for="(pane, idx) in node.children || []" :key="idx"
              @click.prevent="activeTab = Number(idx)"
              :class="activeTab === Number(idx) ? 'border-b-2 border-indigo-500 text-indigo-700 font-bold bg-white' : 'text-gray-500 hover:bg-gray-100 border-b-2 border-transparent'"
              class="px-4 py-2 text-sm transition rounded-t-lg">
              {{ pane.label || 'Pestaña ' + (Number(idx) + 1) }}
           </button>
        </div>
        <div class="p-4 bg-white">
           <template v-for="(pane, idx) in node.children || []" :key="'pane'+idx">
              <!-- CA-90: Lazy Mount Core Strategy v-if (conditional render) vs v-show -->
              <!-- If high density -> drop DOM elements when not active (v-if). Else cache them (v-show) for speed -->
              <div v-if="isHighDensity ? (activeTab === Number(idx)) : true" v-show="!isHighDensity ? (activeTab === Number(idx)) : true">
                 <WorkdeskFormField 
                    v-for="(child, cIdx) in pane.children || []" 
                    :key="child.id || cIdx"
                    :node="child"
                    v-model="formData"
                    :mockContext="mockContext"
                    :isHighDensity="isHighDensity"
                 />
              </div>
           </template>
        </div>
     </div>

     <!-- Fallback -->
     <div v-else class="text-xs text-gray-400 border border-dashed border-gray-200 p-2 rounded">
        [Componente tipo {{ node.type }} delegado en el renderer]
     </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
// @ts-ignore
import jexl from 'jexl';

const props = defineProps<{ 
  node: any, 
  mockContext?: Record<string, any>, 
  isHighDensity: boolean 
}>();
const formData = defineModel<Record<string, any>>({ default: () => ({}) });

const activeTab = ref(0);

const getJexlContext = () => ({
    data: formData.value,
    context: props.mockContext || {}
});

const isVisible = computed(() => {
    if (!props.node.visibilityCondition) return true;
    try {
        return !!jexl.evalSync(props.node.visibilityCondition, getJexlContext());
    } catch(e) {
        return false;
    }
});

const isDisabled = computed(() => {
    if (!props.node.disableCondition) return false;
    try {
        return !!jexl.evalSync(props.node.disableCondition, getJexlContext());
    } catch(e) {
        return false;
    }
});

const internalValue = computed({
    get: () => {
        const key = props.node.camundaVariable || props.node.id;
        return formData.value[key];
    },
    set: (val) => {
        const key = props.node.camundaVariable || props.node.id;
        formData.value[key] = val;
    }
});
</script>
