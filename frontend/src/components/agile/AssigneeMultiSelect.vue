<template>
  <div class="relative">
    <div class="flex -space-x-2 overflow-hidden mb-2">
      <!-- Show assigned avatars -->
      <img 
        v-for="user in currentAssignees" 
        :key="user.userId"
        class="inline-block h-8 w-8 rounded-full ring-2 ring-white" 
        :src="user.avatarUrl || `https://ui-avatars.com/api/?name=${user.name}&background=random`" 
        :alt="user.name"
        :title="user.name"
      />
      <button 
        type="button" 
        @click="isDropdownOpen = !isDropdownOpen"
        class="inline-flex items-center justify-center h-8 w-8 rounded-full ring-2 ring-white bg-gray-100 text-gray-500 hover:bg-gray-200"
      >
        <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
      </button>
    </div>

    <!-- Dropdown to select/search users -->
    <div v-if="isDropdownOpen" class="absolute left-0 mt-1 w-64 bg-white rounded-md shadow-lg border border-gray-200 z-50 p-2">
       <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="Buscar usuarios..."
          class="block w-full border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm h-8 px-2 border mb-2"
       />
       <ul class="max-h-40 overflow-y-auto w-full pr-1">
          <li 
             v-for="user in filteredActiveDirectory" 
             :key="user.userId"
             @click="toggleAssignUser(user.userId)"
             class="cursor-pointer hover:bg-indigo-50 px-2 py-1.5 rounded flex items-center justify-between text-sm"
          >
            <div class="flex items-center gap-2">
               <img :src="`https://ui-avatars.com/api/?name=${user.name}&background=random`" class="w-6 h-6 rounded-full" />
               <span class="truncate">{{ user.name }}</span>
            </div>
            <span v-if="isAssigned(user.userId)" class="material-symbols-outlined text-indigo-600 text-[16px]">check_circle</span>
          </li>
          <li v-if="filteredActiveDirectory.length === 0" class="text-xs text-slate-500 text-center py-2">No se encontraron usuarios.</li>
       </ul>
       <div class="border-t border-gray-200 mt-2 pt-2 flex justify-end">
          <button @click="saveAssignments" type="button" class="bg-indigo-600 text-white px-3 py-1 rounded text-xs font-semibold shadow-sm hover:bg-indigo-700">Guardar</button>
       </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import type { AgileAssignee } from '@/types/agile';
import { useAgileStore } from '@/stores/agileStore';
import { useUserStore } from '@/stores/useUserStore';

const props = defineProps<{
  itemId: string;
  currentAssignees: AgileAssignee[];
}>();

const agileStore = useAgileStore();
const userStore = useUserStore();
const isDropdownOpen = ref(false);
const searchQuery = ref('');

// A local tracker for checkboxes inside the dropdown
const pendingAssignments = ref<string[]>([...props.currentAssignees.map(a => a.userId)]);

onMounted(async () => {
   await userStore.fetchUsers();
});

const filteredActiveDirectory = computed(() => {
  if (!searchQuery.value) return userStore.users;
  const q = searchQuery.value.toLowerCase();
  return userStore.users.filter(u => u.name?.toLowerCase().includes(q) || u.email?.toLowerCase().includes(q));
});

const isAssigned = (userId: string) => pendingAssignments.value.includes(userId);

const toggleAssignUser = (userId: string) => {
  if (isAssigned(userId)) {
     pendingAssignments.value = pendingAssignments.value.filter(id => id !== userId);
  } else {
     pendingAssignments.value.push(userId);
  }
};

const saveAssignments = async () => {
   try {
     await agileStore.assignUsersToItem(props.itemId, pendingAssignments.value);
     isDropdownOpen.value = false;
     searchQuery.value = '';
   } catch (error) {
     console.error('Save assignment failed', error);
   }
};
</script>
