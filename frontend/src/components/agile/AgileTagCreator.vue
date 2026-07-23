<template>
  <div class="relative inline-block text-left w-full">
    <div class="flex items-center gap-2 flex-wrap mb-2">
      <span 
        v-for="tag in currentTags" 
        :key="tag.id" 
        class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium text-white shadow-sm"
        :style="{ backgroundColor: tag.color || '#3b82f6' }"
      >
        {{ tag.label }}
      </span>
    </div>
    
    <div class="mt-2 flex items-center gap-2">
      <input 
        type="text" 
        v-model="newTagLabel" 
        @keydown.enter="handleAddTag"
        placeholder="Add tag..." 
        class="block w-full sm:text-sm border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500 h-8 px-2 border shadow-sm"
      />
      <input 
        type="color" 
        v-model="newTagColor" 
        class="h-8 w-8 border-0 p-0 rounded cursor-pointer shrink-0" 
        title="Choose tag color"
      />
      <button 
        @click="handleAddTag"
        type="button" 
        class="inline-flex items-center p-1 border border-transparent shadow-sm text-sm font-medium rounded text-white bg-indigo-600 hover:bg-indigo-700 h-8 w-8 justify-center shrink-0"
      >
        <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
      </button>
    </div>
    <p v-if="errorMsg" class="text-xs text-red-500 mt-1">{{ errorMsg }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { AgileTag } from '@/types/agile';
import { useAgileStore } from '@/stores/agileStore';

const props = defineProps<{
  itemId: string;
  currentTags: AgileTag[];
}>();

const agileStore = useAgileStore();

const newTagLabel = ref('');
const newTagColor = ref('#3b82f6');
const errorMsg = ref('');

const handleAddTag = async () => {
  errorMsg.value = '';
  if (!newTagLabel.value.trim()) return;

  try {
    await agileStore.createAndAssignTag(props.itemId, newTagLabel.value.trim(), newTagColor.value);
    newTagLabel.value = '';
    // Reset color to default blue
    newTagColor.value = '#3b82f6';
  } catch (err: any) {
    errorMsg.value = err.message || 'Failed to add tag';
  }
};
</script>
