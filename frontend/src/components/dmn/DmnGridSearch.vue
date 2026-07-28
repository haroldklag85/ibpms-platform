<!-- @Traceability: US-007 - CA-24 (Buscador visual en DMN) -->
<template>
  <div v-if="isOpen" class="fixed top-4 right-4 bg-white p-3 rounded shadow-lg border border-gray-200 z-50 flex items-center gap-3">
    <input 
      ref="searchInput"
      v-model="query" 
      @input="onSearch"
      @keyup.enter="nextMatch"
      class="border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:border-indigo-500"
      placeholder="Buscar en grilla..."
    />
    <span class="text-xs text-gray-500 min-w-[80px] text-center">
      {{ matches.length > 0 ? currentIndex + 1 : 0 }} de {{ matches.length }}
    </span>
    <div class="flex gap-1">
      <button @click="prevMatch" :disabled="matches.length === 0" class="p-1 hover:bg-gray-100 rounded disabled:opacity-50">↑</button>
      <button @click="nextMatch" :disabled="matches.length === 0" class="p-1 hover:bg-gray-100 rounded disabled:opacity-50">↓</button>
    </div>
    <button @click="closeSearch" class="p-1 text-gray-400 hover:text-gray-600 ml-2">✕</button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue';

const props = defineProps<{
  rows: any[]
}>();

const emit = defineEmits(['highlight']);

const isOpen = ref(false);
const query = ref('');
const searchInput = ref<HTMLInputElement | null>(null);
const matches = ref<{rowIdx: number, colIdx: number}[]>([]);
const currentIndex = ref(0);

const handleKeyDown = (e: KeyboardEvent) => {
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'f') {
    e.preventDefault();
    isOpen.value = true;
    nextTick(() => searchInput.value?.focus());
  } else if (e.key === 'Escape' && isOpen.value) {
    closeSearch();
  }
};

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown);
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown);
});

const onSearch = () => {
  matches.value = [];
  currentIndex.value = 0;
  if (!query.value.trim()) {
    emit('highlight', { query: '', matches: [] });
    return;
  }
  
  const q = query.value.toLowerCase();
  props.rows.forEach((row, rowIdx) => {
    row.inputs.forEach((val: string, colIdx: number) => {
      if (val.toLowerCase().includes(q)) matches.value.push({ rowIdx, colIdx: `in-${colIdx}` as any });
    });
    row.outputs.forEach((val: string, colIdx: number) => {
      if (val.toLowerCase().includes(q)) matches.value.push({ rowIdx, colIdx: `out-${colIdx}` as any });
    });
  });

  emit('highlight', { query: query.value, matches: matches.value, currentIndex: currentIndex.value });
};

const nextMatch = () => {
  if (matches.value.length === 0) return;
  currentIndex.value = (currentIndex.value + 1) % matches.value.length;
  emit('highlight', { query: query.value, matches: matches.value, currentIndex: currentIndex.value });
  scrollToCurrent();
};

const prevMatch = () => {
  if (matches.value.length === 0) return;
  currentIndex.value = (currentIndex.value - 1 + matches.value.length) % matches.value.length;
  emit('highlight', { query: query.value, matches: matches.value, currentIndex: currentIndex.value });
  scrollToCurrent();
};

const scrollToCurrent = () => {
  nextTick(() => {
    const el = document.querySelector('.search-highlight.active-match');
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'center' });
  });
};

const closeSearch = () => {
  isOpen.value = false;
  query.value = '';
  matches.value = [];
  emit('highlight', { query: '', matches: [] });
};
</script>
