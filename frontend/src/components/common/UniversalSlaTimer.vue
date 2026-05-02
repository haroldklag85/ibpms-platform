<template>
  <div v-if="currentState !== 'TODO'" class="flex items-center gap-2 p-1.5 rounded text-xs font-semibold shadow-sm border" :class="containerClass">
    
    <!-- Timer Display -->
    <div class="flex items-center gap-1" :title="'Vence: ' + slaDueDate">
      <span class="material-symbols-outlined text-[14px]">timer</span>
      <span>{{ formattedRemaining }}</span>
    </div>

    <!-- Play/Stop Button -->
    <div v-if="currentState !== 'DONE'" class="flex items-center border-l pl-2" :class="borderClass">
      <button v-if="!activeTimerId" @click.stop="handleStart" class="text-indigo-600 hover:text-indigo-800 flex items-center justify-center p-0.5 rounded hover:bg-white/50 transition">
        <span class="material-symbols-outlined text-[16px]">play_arrow</span>
      </button>
      <button v-else @click.stop="handleStop" class="text-red-600 hover:text-red-800 flex items-center justify-center p-0.5 rounded hover:bg-white/50 transition animate-pulse">
        <span class="material-symbols-outlined text-[16px]">stop</span>
      </button>
    </div>
    <div v-else class="flex items-center border-l pl-2 text-gray-400" :class="borderClass">
      <span class="material-symbols-outlined text-[14px]">lock</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue';
import { useKanbanStore } from '@/stores/kanbanStore';

const props = defineProps<{
  taskId: string;
  referenceType: string;
  currentState: string;
  slaDueDate?: string;
}>();

const store = useKanbanStore();
const activeTimerId = computed(() => store.activeTimers[props.taskId]);

const now = ref(new Date().getTime());
let interval: any = null;

onMounted(() => {
  interval = setInterval(() => { now.value = new Date().getTime(); }, 60000); // update every minute
});

onUnmounted(() => {
  if (interval) clearInterval(interval);
});

// Mock total SLA duration based on created vs due.
// Without createdAt, assume standard 48 hours for demonstration, or calculate ratio.
const slaRatio = computed(() => {
  if (!props.slaDueDate) return 1;
  const due = new Date(props.slaDueDate).getTime();
  // Assume a fixed 48h SLA if we don't know the start time, just to give a ratio
  const totalMs = 48 * 60 * 60 * 1000; 
  const remainingMs = due - now.value;
  return Math.max(0, remainingMs / totalMs);
});

const formattedRemaining = computed(() => {
  if (!props.slaDueDate) return '--:--';
  const due = new Date(props.slaDueDate).getTime();
  let diffMs = due - now.value;
  if (diffMs < 0) return 'Expirado';
  
  const h = Math.floor(diffMs / (1000 * 60 * 60));
  const m = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
  return `${h}h ${m}m`;
});

const containerClass = computed(() => {
  if (props.currentState === 'DONE') return 'bg-gray-100 text-gray-500 border-gray-200';
  
  const ratio = slaRatio.value;
  if (ratio > 0.5) return 'bg-emerald-50 text-emerald-700 border-emerald-200';
  if (ratio >= 0.2) return 'bg-amber-50 text-amber-700 border-amber-200';
  return 'bg-red-50 text-red-700 border-red-200';
});

const borderClass = computed(() => {
  if (props.currentState === 'DONE') return 'border-gray-200';
  const ratio = slaRatio.value;
  if (ratio > 0.5) return 'border-emerald-200';
  if (ratio >= 0.2) return 'border-amber-200';
  return 'border-red-200';
});

const handleStart = async () => {
  await store.startTimer(props.taskId);
};

const handleStop = async () => {
  if (activeTimerId.value) {
    await store.stopTimer(activeTimerId.value);
  }
};
</script>
