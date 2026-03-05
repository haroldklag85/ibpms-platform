<template>
  <div class="h-screen flex bg-ibpms-light">
    <!-- Sidebar / Menú Lateral Completo (Expandido según Wireframe) -->
    <aside class="w-64 bg-ibpms text-white flex flex-col py-6">
      <div class="font-bold text-2xl px-6 mb-8 cursor-pointer flex items-center" @click="$router.push('/')">
        <span class="mr-2">🔷</span> iBPMS
      </div>
      
      <nav class="flex-1 overflow-y-auto px-4">
        <ul class="space-y-2 mb-6">
          <li>
            <router-link to="/" class="flex items-center px-4 py-3 rounded-lg hover:bg-white/10 transition" active-class="bg-ibpms-brand text-white font-bold shadow">
              <span class="text-xl mr-3">🏠</span> Inicio
            </router-link>
          </li>
          <li>
            <router-link to="/workdesk" class="flex items-center px-4 py-3 rounded-lg hover:bg-white/10 transition" active-class="bg-ibpms-brand text-white font-bold shadow">
              <span class="text-xl mr-3">📋</span> Workdesk (12)
            </router-link>
          </li>
          <li>
            <router-link to="/inbox" class="flex items-center px-4 py-3 rounded-lg hover:bg-white/10 transition" active-class="bg-ibpms-brand text-white font-bold shadow">
              <span class="text-xl mr-3">📥</span> Inbox (5)
            </router-link>
          </li>
          <li>
            <router-link to="/kanban" class="flex items-center px-4 py-3 rounded-lg hover:bg-white/10 transition" active-class="bg-ibpms-brand text-white font-bold shadow">
              <span class="text-xl mr-3">📂</span> Proyectos (Kanban)
            </router-link>
          </li>
          <li>
            <router-link to="/admin/project-builder" class="flex items-center px-4 py-3 rounded-lg hover:bg-white/10 transition" active-class="bg-ibpms-brand text-white font-bold shadow">
              <span class="text-xl mr-3">🏗️</span> Plantillas Proyectos
            </router-link>
          </li>
          <li>
            <router-link to="/admin/analytics/bam" class="flex items-center px-4 py-3 rounded-lg hover:bg-white/10 transition text-gray-300" active-class="bg-ibpms-brand text-white font-bold shadow">
              <span class="text-xl mr-3">📊</span> Dashboards
            </router-link>
          </li>
        </ul>

        <div class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3 mt-6 ml-4">--- Admin ---</div>
        <ul class="space-y-2">
           <li>
            <router-link to="/admin/modeler/bpmn" class="flex items-center px-4 py-2.5 rounded hover:bg-white/10 transition text-gray-300 text-sm" active-class="bg-white/20 text-white font-bold">
              <span class="text-lg mr-3">⚙️</span> Diseño BPMN
            </router-link>
          </li>
          <li>
            <router-link to="/admin/modeler/forms" class="flex items-center px-4 py-2.5 rounded hover:bg-white/10 transition text-gray-300 text-sm" active-class="bg-white/20 text-white font-bold">
              <span class="text-lg mr-3">📝</span> Formularios
            </router-link>
          </li>
          <li>
            <router-link to="/admin/modeler/dmn" class="flex items-center px-4 py-2.5 rounded hover:bg-white/10 transition text-gray-300 text-sm" active-class="bg-white/20 text-white font-bold">
              <span class="text-lg mr-3">🧠</span> Reglas IA
            </router-link>
          </li>
          <li>
            <router-link to="/admin/integration/catalog" class="flex items-center px-4 py-2.5 rounded hover:bg-white/10 transition text-gray-300 text-sm" active-class="bg-white/20 text-white font-bold">
              <span class="text-lg mr-3">🔌</span> Integración
            </router-link>
          </li>
          <li>
             <router-link to="/admin/security/identity" class="flex items-center px-4 py-2.5 rounded hover:bg-white/10 transition text-gray-300 text-sm" active-class="bg-white/20 text-white font-bold">
              <span class="text-lg mr-3">🛡️</span> Seguridad (RBAC)
            </router-link>
          </li>
          <li>
             <router-link to="/admin/integration/builder" class="flex items-center px-4 py-2.5 rounded hover:bg-white/10 transition text-gray-300 text-sm" active-class="bg-white/20 text-white font-bold">
              <span class="text-lg mr-3">📦</span> Extensión
            </router-link>
          </li>
          <li>
             <router-link to="/admin/projects/manager" class="flex items-center px-4 py-2.5 rounded hover:bg-white/10 transition text-gray-300 text-sm" active-class="bg-white/20 text-white font-bold">
              <span class="text-lg mr-3">📁</span> Gestor Proyectos
            </router-link>
          </li>
          <li>
             <router-link to="/admin/projects/agile-hub" class="flex items-center px-4 py-2.5 rounded hover:bg-white/10 transition text-gray-300 text-sm" active-class="bg-white/20 text-white font-bold">
              <span class="text-lg mr-3">⚡</span> Hub Ágil
            </router-link>
          </li>
          <li>
             <router-link to="/admin/mailboxes" class="flex items-center px-4 py-2.5 rounded hover:bg-white/10 transition text-gray-300 text-sm" active-class="bg-white/20 text-white font-bold">
              <span class="text-lg mr-3">📬</span> Buzones SAC
            </router-link>
          </li>
        </ul>
      </nav>

      <div class="px-6 mt-auto">
        <button @click="logout" class="flex items-center w-full px-4 py-3 text-red-400 hover:bg-white/10 rounded transition" title="Cerrar Sesión">
           <span class="text-xl mr-3">🚪</span> <span class="text-sm font-medium">Cerrar Sesión</span>
        </button>
      </div>
    </aside>

    <!-- Contenido Principal Dinámico -->
    <main class="flex-1 flex flex-col h-full overflow-hidden">
      <header class="h-16 bg-white border-b flex items-center px-6 shadow-sm justify-between">
        <div class="w-1/3">
           <div class="relative">
             <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-500">🔍</span>
             <input type="text" placeholder="Buscar procesos, políticas..." class="w-full pl-10 pr-4 py-2 bg-gray-100 border-none rounded-md focus:ring-ibpms-brand text-sm">
           </div>
        </div>
        <div class="flex items-center space-x-6">
          <span class="text-gray-500 hover:text-ibpms-brand cursor-pointer transition relative">
             🔔
             <span class="absolute -top-1 -right-2 bg-red-500 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full">3</span>
          </span>
          <div class="flex items-center gap-2 cursor-pointer hover:bg-gray-50 p-1 rounded transition">
            <div class="w-9 h-9 rounded-full bg-ibpms-brand text-white flex items-center justify-center font-bold">
              👤
            </div>
            <span class="text-sm font-medium text-gray-700 hidden lg:block">Mi Perfil</span>
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
