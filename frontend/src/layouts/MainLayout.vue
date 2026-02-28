<template>
  <div class="h-screen flex bg-ibpms-light">
    <!-- Sidebar / Menú Lateral Mínimo -->
    <aside class="w-20 bg-ibpms text-white flex flex-col items-center py-6">
      <div class="font-bold text-xl mb-10 cursor-pointer" @click="$router.push('/')">iBPMS</div>
      
      <nav class="flex-1 w-full">
        <ul class="space-y-6">
          <li>
            <router-link to="/" class="block w-full text-center hover:text-ibpms-brand transition" active-class="text-ibpms-brand border-r-2 border-ibpms-brand">
              <span class="text-2xl" title="Inicio / Portal">🏠</span>
            </router-link>
          </li>
          <li>
            <router-link to="/workdesk" class="block w-full text-center hover:text-ibpms-brand transition" active-class="text-ibpms-brand border-r-2 border-ibpms-brand">
              <span class="text-2xl" title="Workdesk (Tareas)">📥</span>
            </router-link>
          </li>
        </ul>
      </nav>

      <div class="mt-auto">
        <button @click="logout" class="text-xl" title="Cerrar Sesión">🚪</button>
      </div>
    </aside>

    <!-- Contenido Principal Dinámico -->
    <main class="flex-1 flex flex-col h-full overflow-hidden">
      <!-- Navbar Superior -->
      <header class="h-16 bg-white border-b flex justify-between items-center px-6 shadow-sm">
        <h1 class="text-xl font-semibold text-gray-800">{{ $route.name }}</h1>
        <div class="flex items-center space-x-4">
          <span class="text-gray-500">🔍 Buscar...</span>
          <span class="text-gray-500">🔔 (0)</span>
          <div class="w-8 h-8 rounded-full bg-ibpms-brand text-white flex items-center justify-center font-bold">
            👤
          </div>
        </div>
      </header>
      
      <!-- Lienzo donde se renderizan las vistas secundarias -->
      <div class="p-6 flex-1 overflow-auto bg-gray-50">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

const logout = () => {
  localStorage.removeItem('ibpms_token');
  router.push('/login');
};
</script>
