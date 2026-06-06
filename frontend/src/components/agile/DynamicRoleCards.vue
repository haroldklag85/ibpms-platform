<template>
  <div class="dynamic-roles-container">
    <!-- CA-15: Skeleton Loaders -->
    <div v-if="isLoading" class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <SkeletonCard v-for="n in 3" :key="'skeleton-'+n" />
    </div>

    <!-- CA-09: Cards Dinámicas Reales -->
    <TransitionGroup 
        v-else 
        name="list" 
        tag="div" 
        class="grid grid-cols-1 md:grid-cols-3 gap-6">
        
      <!-- CA-11: Atómico V-IF basado en rol estricto. CQRS local. -->
      <template v-for="card in cards" :key="card.id">
        <div 
          v-if="hasAccess(card.requiredRole)"
          class="real-card bg-white dark:bg-gray-800 rounded-xl shadow-lg hover:shadow-xl transition-all duration-300 p-6 border-l-4 border-l-indigo-500"
        >
          <div class="flex justify-between items-start mb-4">
            <h3 class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-indigo-500 to-purple-600">
              {{ card.title }}
            </h3>
            <span class="text-xs font-semibold px-2 py-1 bg-indigo-100 text-indigo-800 rounded-full">
              {{ card.requiredRole }}
            </span>
          </div>
          <p class="text-gray-600 dark:text-gray-300 text-sm">
            Módulo asegurado y filtrado por Zero-Trust Identity.
          </p>
        </div>
      </template>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/authStore';
import SkeletonCard from './SkeletonCard.vue';

interface Card {
  id: string | number;
  title: string;
  requiredRole: string;
}

const props = defineProps<{
    isLoading: boolean;
    cards: Card[];
}>();

const authStore = useAuthStore();

// CA-11/CA-14: Ocultamiento atómico condicional
const hasAccess = (requiredRole: string) => {
    // Si no requiere rol, pasa abierto. Si requiere, verifica activeRole o SuperAdmin
    if (!requiredRole) return true;
    if (authStore.activeRole === 'ROLE_SUPER_ADMIN') return true; 
    return authStore.activeRole === requiredRole;
};
</script>

<style scoped>
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
