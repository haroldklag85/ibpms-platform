<template>
  <div class="flex flex-col items-center justify-center p-8 text-center" data-testid="empty-state">
    <div class="text-slate-400 mb-4" data-testid="empty-state-icon">
      <!-- Iconos según variante -->
      <span v-if="variant === 'no-tasks'" class="material-symbols-outlined text-6xl">inventory_2</span>
      <span v-else-if="variant === 'no-access'" class="material-symbols-outlined text-6xl">lock</span>
      <span v-else-if="variant === 'error'" class="material-symbols-outlined text-6xl">error</span>
      <span v-else-if="variant === 'no-results'" class="material-symbols-outlined text-6xl">search_off</span>
    </div>
    
    <h3 v-if="computedTitle" class="text-lg font-medium text-slate-600 mb-1" data-testid="empty-state-title">
      {{ computedTitle }}
    </h3>
    
    <p v-if="computedSubtitle" class="text-sm text-slate-500 mb-4 max-w-md" data-testid="empty-state-subtitle">
      {{ computedSubtitle }}
    </p>
    
    <button
      v-if="actionLabel"
      @click="$emit('action')"
      class="px-4 py-2 bg-indigo-50 text-indigo-600 hover:bg-indigo-100 rounded-lg text-sm font-medium transition-colors"
      data-testid="empty-state-action"
    >
      {{ actionLabel }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const props = defineProps<{
  variant: 'no-tasks' | 'no-access' | 'error' | 'no-results';
  title?: string;
  subtitle?: string;
  actionLabel?: string;
}>();

const variantKeyMap: Record<string, string> = {
  'no-tasks': 'noTasks',
  'no-access': 'noAccess',
  'error': 'errorTitle',
  'no-results': 'noResults'
};

const variantDescMap: Record<string, string> = {
  'no-tasks': 'noTasksDesc',
  'no-access': 'noAccessDesc',
  'error': 'errorDesc',
  'no-results': 'noResultsDesc'
};

const computedTitle = computed(() => props.title || t(`emptyState.${variantKeyMap[props.variant]}`));
const computedSubtitle = computed(() => props.subtitle || t(`emptyState.${variantDescMap[props.variant]}`));

const emit = defineEmits<{
  (e: 'action'): void;
}>();
</script>
