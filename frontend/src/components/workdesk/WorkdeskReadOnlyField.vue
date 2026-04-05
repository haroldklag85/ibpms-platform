<template>
  <div class="mb-3">
     <div v-if="node.type === 'container'" :class="mode === 'print' ? 'mb-4' : 'p-4 border border-indigo-50 bg-white rounded-lg shadow-sm mb-4 mt-2'">
        <h3 :class="mode === 'print' ? 'text-md font-bold text-black uppercase pb-1 border-b border-gray-300 mb-2' : 'text-sm font-bold text-indigo-800 mb-3 uppercase'">
           {{ node.label }}
        </h3>
        <div :class="mode === 'print' ? 'ml-2 border-l-2 border-gray-200 pl-3' : 'space-y-3'">
           <WorkdeskReadOnlyField 
              v-for="(child, idx) in node.children || []" 
              :key="child.id || idx"
              :node="child"
              :formData="formData"
              :mode="mode"
           />
        </div>
     </div>
     
     <div v-else-if="node.type === 'tabs' || node.type === 'accordion'" class="mb-4">
        <div v-for="(pane, idx) in node.children || []" :key="idx" class="mb-4">
           <h4 :class="mode === 'print' ? 'font-bold text-black mb-1' : 'font-bold text-gray-700 bg-gray-100 p-2 rounded-t-lg'">
               {{ pane.label || 'Pestaña/Panel ' + (Number(idx) + 1) }}
           </h4>
           <div :class="mode === 'print' ? '' : 'p-3 border border-gray-200 rounded-b-lg bg-white space-y-3'">
               <WorkdeskReadOnlyField 
                  v-for="(child, cIdx) in pane.children || []" 
                  :key="child.id || cIdx"
                  :node="child"
                  :formData="formData"
                  :mode="mode"
               />
           </div>
        </div>
     </div>

     <div v-else-if="node.type !== 'info_modal' && node.type !== 'button_submit' && node.type !== 'button_draft' && node.type !== 'button_reject'" :class="mode === 'print' ? 'flex flex-col mb-2' : 'grid grid-cols-3 gap-4 items-start border-b border-gray-100 pb-2'">
        <label :class="mode === 'print' ? 'text-xs font-bold text-gray-600' : 'text-sm font-semibold text-gray-700 col-span-1'">
           {{ node.label }}
        </label>
        <div :class="mode === 'print' ? 'text-sm text-black' : 'text-sm text-gray-900 col-span-2'">
           <!-- Booleans -->
           <span v-if="node.type === 'checkbox'">
              {{ val ? '✅ Sí' : '❌ No' }}
           </span>
           <!-- URLs/Emails -->
           <a v-else-if="node.type === 'url' && val" :href="val" target="_blank" class="text-blue-600 underline">
              {{ val }}
           </a>
           <a v-else-if="node.type === 'email' && val" :href="'mailto:'+val" class="text-blue-600 underline">
              {{ val }}
           </a>
           <!-- Objeto Complejo (Async Select con {id, name}) -->
           <span v-else-if="typeof val === 'object' && val !== null">
              {{ val.name || JSON.stringify(val) }}
           </span>
           <!-- String/Number fallback -->
           <span v-else>
              {{ val === undefined || val === null || val === '' ? '-' : val }}
           </span>
        </div>
     </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
   node: any,
   formData: Record<string, any>,
   mode: 'audit' | 'print'
}>();

const val = computed(() => {
    const key = props.node.camundaVariable || props.node.id;
    return props.formData[key];
});
</script>
