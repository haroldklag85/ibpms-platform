<template>
  <div class="relative" data-testid="role-selector" v-if="authStore.roles.length > 1" ref="dropdownRef">
    <!-- Dropdown Trigger Button -->
    <button 
      @click="isOpen = !isOpen"
      :aria-expanded="isOpen"
      class="flex items-center gap-2 px-3 py-1.5 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 transition-colors shadow-sm text-sm font-medium text-slate-700"
    >
      <span class="material-symbols-outlined text-[18px] text-indigo-600">admin_panel_settings</span>
      <span>{{ formattedActiveRole }}</span>
      <span class="material-symbols-outlined text-[18px] text-slate-400 transition-transform duration-200" :class="{ 'rotate-180': isOpen }">expand_more</span>
    </button>

    <!-- Dropdown Menu (Glassmorphism) -->
    <transition
      enter-active-class="transition duration-100 ease-out"
      enter-from-class="transform scale-95 opacity-0"
      enter-to-class="transform scale-100 opacity-100"
      leave-active-class="transition duration-75 ease-in"
      leave-from-class="transform scale-100 opacity-100"
      leave-to-class="transform scale-95 opacity-0"
    >
      <div 
        v-if="isOpen"
        role="menu"
        class="absolute right-0 mt-2 w-48 rounded-xl border border-slate-200 bg-white/90 backdrop-blur-md shadow-lg overflow-hidden z-50"
      >
        <div class="p-1">
          <button
            v-for="role in authStore.roles"
            :key="role"
            @click="selectRole(role)"
            role="menuitem"
            class="w-full text-left px-3 py-2 text-sm rounded-lg transition-colors flex items-center justify-between"
            :class="[
              authStore.activeRole === role 
                ? 'bg-indigo-50 text-indigo-700 font-semibold' 
                : 'text-slate-700 hover:bg-slate-100'
            ]"
          >
            <span>{{ formatRole(role) }}</span>
            <span v-if="authStore.activeRole === role" class="material-symbols-outlined text-[16px] text-indigo-600">check</span>
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import apiClient from '@/services/apiClient';

const authStore = useAuthStore();
const { t } = useI18n();
const router = useRouter();
const isOpen = ref(false);
const dropdownRef = ref<HTMLElement | null>(null);

const formatRole = (role: string | null) => {
  if (!role) return 'Desconocido';
  const clean = role.replace('ROLE_', '').replace(/_/g, ' ');
  return clean.charAt(0).toUpperCase() + clean.slice(1).toLowerCase();
};

const formattedActiveRole = computed(() => formatRole(authStore.activeRole));

const selectRole = async (role: string) => {
  if (role !== authStore.activeRole) {
    isOpen.value = false;
    
    // Disparar overlay global en MainLayout
    window.dispatchEvent(new CustomEvent('role-switching-start'));

    try {
      // Registrar cambio en Audit Log central (ISO 27001)
      await apiClient.post('/admin/audit/telemetry', {
        action: 'ROLE_SWITCH',
        details: { from: authStore.activeRole, to: role },
        timestamp: new Date().toISOString()
      }).catch(err => {
         console.warn('No se pudo reportar telemetría de auditoría:', err);
      });

      const oldRole = authStore.activeRole;
      authStore.switchRole(role);

      // Evaluar acceso de la ruta actual
      const currentRoute = router.currentRoute.value;
      if (currentRoute.meta.roles && Array.isArray(currentRoute.meta.roles)) {
        const hasAccess = currentRoute.meta.roles.includes(role);
        if (!hasAccess) {
          // Redirigir a raíz con advertencia
          window.dispatchEvent(new CustomEvent('layout-toast-dispatch', {
            detail: { message: 'Acceso denegado: El rol seleccionado no tiene permisos para la vista anterior.' }
          }));
          await router.push('/');
        } else {
          // Redirigir a raíz por A2 para rehidratación limpia
          await router.push('/');
        }
      } else {
        // Redirigir a raíz por A2
        await router.push('/');
      }
    } catch (e) {
      console.error('Error al cambiar de rol:', e);
    } finally {
      // Ocultar overlay tras recarga de menú
      setTimeout(() => {
        window.dispatchEvent(new CustomEvent('role-switching-end'));
      }, 500);
    }
  } else {
    isOpen.value = false;
  }
};

// Cierra el dropdown al hacer clic fuera
const handleClickOutside = (event: MouseEvent) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    isOpen.value = false;
  }
};

onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside);
});
</script>
