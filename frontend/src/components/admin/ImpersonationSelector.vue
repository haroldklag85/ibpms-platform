<template>
  <div
    class="fixed inset-0 z-[80] backdrop-blur-md bg-black/30 flex items-center justify-center p-4"
    data-testid="impersonation-selector"
    @click.self="$emit('close')"
  >
    <div class="bg-white rounded-2xl shadow-2xl p-6 max-w-lg w-full max-h-[70vh] flex flex-col">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-xl font-bold text-slate-800">{{ t('impersonation.selectUser') }}</h2>
        <button @click="$emit('close')" class="text-slate-400 hover:text-slate-600">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>

      <div class="relative mb-4 shrink-0">
        <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
           <span class="material-symbols-outlined text-[18px]">search</span>
        </span>
        <input 
          v-model="searchQuery"
          type="text" 
          :placeholder="t('impersonation.searchUsers')" 
          class="w-full pl-9 pr-4 py-2 bg-slate-50 border border-slate-200 focus:border-indigo-500 focus:bg-white focus:ring-2 focus:ring-indigo-100 rounded-lg text-sm transition-all outline-none"
        >
      </div>

      <div class="flex-1 overflow-y-auto min-h-[200px]">
        <div v-if="isLoading" class="flex justify-center p-8">
           <span class="material-symbols-outlined animate-spin text-indigo-500 text-3xl">sync</span>
        </div>
        <div v-else-if="filteredUsers.length === 0" class="text-center p-8 text-slate-500 text-sm">
           {{ t('emptyState.noResults') }}
        </div>
        <div v-else class="space-y-2">
          <div
            v-for="user in filteredUsers"
            :key="user.id"
            @click="selectUser(user)"
            class="hover:bg-indigo-50 rounded-lg p-3 cursor-pointer transition border border-transparent hover:border-indigo-100 flex items-center gap-3"
          >
            <div class="w-10 h-10 rounded-full bg-slate-100 flex items-center justify-center text-slate-500 font-bold shrink-0">
              {{ user.username.substring(0, 2).toUpperCase() }}
            </div>
            <div class="flex flex-col overflow-hidden">
              <span class="text-sm font-semibold text-slate-800 truncate">{{ user.username }}</span>
              <span class="text-xs text-slate-500 truncate">{{ user.email }}</span>
            </div>
            <div class="ml-auto flex gap-1 flex-wrap justify-end max-w-[120px]">
              <span v-for="role in user.roles" :key="role" class="text-[10px] bg-slate-100 text-slate-600 px-1.5 py-0.5 rounded">
                {{ role.replace('ROLE_', '') }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import apiClient from '@/services/apiClient';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const authStore = useAuthStore();
const emit = defineEmits<{ (e: 'close'): void }>();

interface UserResponseDTO {
  id: string;
  username: string;
  email: string;
  roles: string[];
}

const users = ref<UserResponseDTO[]>([]);
const searchQuery = ref('');
const isLoading = ref(true);

const fetchUsers = async () => {
  try {
    const { data } = await apiClient.get('/admin/users');
    users.value = data.filter((u: UserResponseDTO) => !u.roles.includes('ROLE_SUPER_ADMIN'));
  } catch (error) {
    console.error('Error fetching users for impersonation', error);
  } finally {
    isLoading.value = false;
  }
};

const filteredUsers = computed(() => {
  const query = searchQuery.value.toLowerCase();
  return users.value.filter(u => 
    u.username.toLowerCase().includes(query) || 
    u.email.toLowerCase().includes(query)
  );
});

const selectUser = async (user: UserResponseDTO) => {
  await authStore.startImpersonation(user.id);
  emit('close');
};

onMounted(() => {
  fetchUsers();
});
</script>
