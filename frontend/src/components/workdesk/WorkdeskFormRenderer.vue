<template>
  <div class="workdesk-form-renderer relative bg-white p-6 shadow rounded-lg max-w-4xl mx-auto border border-gray-200">
     <!-- CA-90 High Density Warning inside renderer -->
     <div v-if="isHighDensityForm" class="mb-4 p-4 bg-yellow-50 border-l-4 border-yellow-400 text-yellow-800 shadow-sm rounded flex items-center gap-3">
        <span class="text-2xl">⚡</span>
        <div>
           <h4 class="text-sm font-bold">Rendimiento Optimizado (Lazy Mount)</h4>
           <p class="text-xs mt-0.5">La profundidad del formulario supera los {{ MAX_FORM_FIELDS }} campos. Se ha habilitado la carga diferida en pestañas y acordeones.</p>
        </div>
     </div>

     <!-- Using a recursive sub-component simplifies rendering -->
     <div class="space-y-4">
        <template v-for="node in schema" :key="node.id || node.camundaVariable || Math.random()">
            <WorkdeskFormField 
               :node="node" 
               v-model="formData" 
               :mockContext="mockContext"
               :isHighDensity="isHighDensityForm"
            />
        </template>
     </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import WorkdeskFormField from './WorkdeskFormField.vue';

const props = defineProps<{ schema: any[], mockContext?: Record<string, any> }>();
const formData = defineModel<Record<string, any>>({ default: () => ({}) });

const MAX_FORM_FIELDS = 200;

// CA-90: Deep calculation of nodes
const flatFields = (fields: any[]): any[] => {
  let res: any[] = [];
  for (const f of fields) {
      if (f.type === 'container' || f.type === 'field_array') {
          if (f.children) res = res.concat(flatFields(f.children));
      } else if (f.type === 'tabs' || f.type === 'accordion') {
          if (f.children) {
              for (const pane of f.children) {
                   if (pane.children) res = res.concat(flatFields(pane.children));
              }
          }
      } else {
          res.push(f);
      }
  }
  return res;
};

const isHighDensityForm = computed(() => {
    return flatFields(props.schema).length > MAX_FORM_FIELDS;
});
</script>
