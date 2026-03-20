<template>
  <div class="h-screen flex bg-slate-50 font-['Inter'] text-slate-900 overflow-hidden">
    <!-- Sidebar: Expandable / Collapsed State (Left) -->
    <!-- Transición suave de ancho: w-64 cuando expandido, w-16 cuando colapsado -->
    <aside 
      :class="isSidebarCollapsed ? 'w-16' : 'w-64'" 
      class="bg-[#1A1C1E] flex flex-col py-6 border-r border-slate-800 shrink-0 z-40 transition-all duration-300 ease-in-out relative group"
    >
      <!-- Brand Logo & Collapse Toggle -->
      <div class="mb-8 px-4 flex items-center justify-between">
        <div class="flex items-center gap-3 overflow-hidden cursor-pointer whitespace-nowrap" @click="$router.push('/')">
          <div class="w-8 h-8 shrink-0 bg-indigo-600 rounded-lg flex items-center justify-center shadow-lg">
            <span class="material-symbols-outlined text-white text-lg">balance</span>
          </div>
          <!-- Texto oculto al colapsar -->
          <span v-if="!isSidebarCollapsed" class="text-white font-bold text-lg tracking-tight fade-in">iBPMS Corp</span>
        </div>
        
        <!-- Botón para Colapsar/Expandir (Solo visible si está expandido o en hover si está colapsado simulando comportamiento B2B) -->
        <button 
          @click="toggleSidebar"
          class="shrink-0 p-1 rounded-md text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          :class="isSidebarCollapsed ? 'absolute -right-3 top-7 bg-[#1A1C1E] border border-slate-700 rounded-full shadow-md z-50 p-0.5' : ''"
        >
          <span class="material-symbols-outlined text-[18px]">
            {{ isSidebarCollapsed ? 'chevron_right' : 'chevron_left' }}
          </span>
        </button>
      </div>
      
      <!-- Navigation Menu -->
      <nav class="flex-1 overflow-y-auto overflow-x-hidden px-3 no-scrollbar flex flex-col gap-1 w-full">
        <!-- SECCIÓN 1: OPERATIVA -->
        <p v-if="!isSidebarCollapsed" class="text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-2 mt-2 px-2 fade-in">Área Operativa</p>

        <router-link to="/" class="nav-item group/link" active-class="nav-active" title="Inicio">
          <span class="material-symbols-outlined nav-icon">home</span>
          <span v-if="!isSidebarCollapsed" class="nav-text">Inicio</span>
          <!-- Tooltip en modo colapsado -->
          <div v-if="isSidebarCollapsed" class="tooltip-mockup">Inicio</div>
        </router-link>

        <router-link to="/workdesk" class="nav-item group/link" active-class="nav-active" title="Bandeja Unificada">
          <span class="material-symbols-outlined nav-icon">work</span>
          <span v-if="!isSidebarCollapsed" class="nav-text flex-1">Workdesk</span>
          <span v-if="!isSidebarCollapsed" class="bg-indigo-600/20 text-indigo-400 text-[10px] px-1.5 py-0.5 rounded font-bold">12</span>
          <div v-if="isSidebarCollapsed" class="tooltip-mockup">Workdesk <span class="text-indigo-400 ml-1">12</span></div>
        </router-link>

        <router-link to="/inbox" class="nav-item group/link" active-class="nav-active" title="Inbox Mail">
          <span class="material-symbols-outlined nav-icon">mail</span>
          <span v-if="!isSidebarCollapsed" class="nav-text flex-1">Inbox Mail</span>
          <span v-if="!isSidebarCollapsed" class="bg-red-500/20 text-red-400 text-[10px] px-1.5 py-0.5 rounded font-bold">5</span>
          <div v-if="isSidebarCollapsed" class="tooltip-mockup">Inbox Mail <span class="text-red-400 ml-1">5</span></div>
          <!-- Punto rojo si está colapsado y hay notificaciones (simulación) -->
          <span v-if="isSidebarCollapsed" class="absolute top-2 right-2 size-2 bg-red-500 rounded-full border-2 border-[#1A1C1E]"></span>
        </router-link>

        <router-link to="/kanban" class="nav-item group/link" active-class="nav-active" title="Proyectos (Kanban)">
          <span class="material-symbols-outlined nav-icon">view_kanban</span>
          <span v-if="!isSidebarCollapsed" class="nav-text">Proyectos Kanban</span>
          <div v-if="isSidebarCollapsed" class="tooltip-mockup">Proyectos Kanban</div>
        </router-link>

        <router-link to="/admin/project-builder" class="nav-item group/link" active-class="nav-active" title="Plantillas">
          <span class="material-symbols-outlined nav-icon">architecture</span>
          <span v-if="!isSidebarCollapsed" class="nav-text">Constructor Plantillas</span>
          <div v-if="isSidebarCollapsed" class="tooltip-mockup">Constructor Plantillas</div>
        </router-link>

        <router-link to="/admin/analytics/bam" class="nav-item group/link" active-class="nav-active" title="Dashboards BAM">
          <span class="material-symbols-outlined nav-icon">analytics</span>
          <span v-if="!isSidebarCollapsed" class="nav-text">Dashboards BAM</span>
          <div v-if="isSidebarCollapsed" class="tooltip-mockup">Dashboards BAM</div>
        </router-link>

        <div class="h-px bg-slate-800 my-4 mx-2"></div>

        <!-- SECCIÓN 2: ADMINISTRACIÓN (Acordeón) -->
        <p v-if="!isSidebarCollapsed" class="text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-2 px-2 fade-in">System Design</p>

        <!-- Botón Toggle de Acordeón Admin -->
        <div 
          @click="toggleAdminMenu" 
          class="nav-item cursor-pointer group/admin relative flex items-center"
          :class="{ 'bg-slate-800/50 text-white': isAdminExpanded && !isSidebarCollapsed }"
          title="Administración del Sistema"
        >
          <span class="material-symbols-outlined nav-icon" :class="{ 'text-indigo-400': isAdminExpanded }">settings_suggest</span>
          <span v-if="!isSidebarCollapsed" class="nav-text flex-1" :class="{ 'font-semibold': isAdminExpanded }">Administración</span>
          <span v-if="!isSidebarCollapsed" class="material-symbols-outlined text-[16px] text-slate-500 transition-transform duration-200" :class="{ 'rotate-180': isAdminExpanded }">expand_more</span>
          <div v-if="isSidebarCollapsed" class="tooltip-mockup">Administración</div>
        </div>

        <!-- Submenú de Administración (Solo visible si expandido el acordeón y No colapsado el sidebar) -->
        <!-- Si el sidebar se colapsa, obligamos a ocultar el submenú para mantener UX de iconos limpios, o podríamos transformarlo en un flyout, pero por ahora se oculta de la barra fina -->
        <div v-show="isAdminExpanded && !isSidebarCollapsed" class="flex flex-col gap-1 pl-9 pr-2 mt-1 fade-in">
           <router-link to="/admin/modeler/bpmn" class="sub-nav-item" active-class="sub-nav-active">
              <span class="material-symbols-outlined text-[14px] mr-2">account_tree</span> Diseño BPMN
           </router-link>
           <router-link to="/admin/modeler/forms" class="sub-nav-item" active-class="sub-nav-active">
              <span class="material-symbols-outlined text-[14px] mr-2">dynamic_form</span> Formularios
           </router-link>
           <router-link to="/admin/modeler/dmn" class="sub-nav-item" active-class="sub-nav-active">
              <span class="material-symbols-outlined text-[14px] mr-2">psychology</span> Reglas IA
           </router-link>
           <router-link to="/admin/integration/catalog" class="sub-nav-item" active-class="sub-nav-active">
              <span class="material-symbols-outlined text-[14px] mr-2">cable</span> Integración API
           </router-link>
           <router-link v-if="authStore.roles.includes('ROLE_SUPER_ADMIN')" to="/admin/security/identity" class="sub-nav-item" active-class="sub-nav-active">
              <span class="material-symbols-outlined text-[14px] mr-2">shield_person</span> Seguridad (RBAC)
           </router-link>
           <router-link to="/admin/integration/builder" class="sub-nav-item" active-class="sub-nav-active">
              <span class="material-symbols-outlined text-[14px] mr-2">extension</span> Extensiones
           </router-link>
           <router-link to="/admin/projects/manager" class="sub-nav-item" active-class="sub-nav-active">
              <span class="material-symbols-outlined text-[14px] mr-2">folder_managed</span> Gestor Proyectos
           </router-link>
           <router-link to="/admin/projects/agile-hub" class="sub-nav-item" active-class="sub-nav-active">
              <span class="material-symbols-outlined text-[14px] mr-2">speed</span> Hub Ágil
           </router-link>
           <router-link to="/admin/mailboxes" class="sub-nav-item" active-class="sub-nav-active">
              <span class="material-symbols-outlined text-[14px] mr-2">mark_email_read</span> Buzones SAC
           </router-link>
        </div>

      </nav>

      <!-- Bottom Profile / Sign Out -->
      <div class="mt-auto px-3 pt-4 border-t border-slate-800 flex flex-col gap-2">
        <button @click="logout" class="nav-item text-red-400 hover:text-red-300 hover:bg-red-500/10 group/link" title="Cerrar Sesión">
           <span class="material-symbols-outlined nav-icon">logout</span>
           <span v-if="!isSidebarCollapsed" class="nav-text">Cerrar Sesión</span>
           <div v-if="isSidebarCollapsed" class="tooltip-mockup !bg-red-900 border border-red-800">Cerrar Sesión</div>
        </button>
        
        <div class="flex items-center gap-3 px-2 py-2 mt-2" :class="isSidebarCollapsed ? 'justify-center' : ''">
          <div class="w-8 h-8 rounded-full bg-slate-700 overflow-hidden border border-slate-600 shrink-0 flex items-center justify-center text-xs font-bold text-slate-300">
             US
          </div>
          <div v-if="!isSidebarCollapsed" class="flex flex-col overflow-hidden fade-in">
            <span class="text-xs font-semibold text-white truncate">Usuario Activo</span>
            <span class="text-[10px] text-slate-500 truncate" :title="topRolesTipText">{{ topRolesTipText }}</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main Content Area (Right) -->
    <main class="flex-1 flex flex-col h-full bg-slate-50 overflow-hidden relative">
      <!-- Top Navigation Bar Header Global -->
      <header class="h-16 border-b border-slate-200 bg-white/80 backdrop-blur-md flex items-center px-8 shrink-0 justify-between z-20">
        
        <!-- Breadcrumbs o Título de Contexto -->
        <div class="flex items-center gap-4 hidden sm:flex truncate flex-1">
           <div class="flex items-center text-sm font-medium text-slate-500">
              <span class="material-symbols-outlined text-[18px] mr-1">business_center</span>
              <span>Workspace</span>
              <span class="mx-2 text-slate-300">/</span>
              <span class="text-slate-900 font-bold truncate">Enterprise Application</span>
           </div>
        </div>

        <div class="flex items-center gap-4 shrink-0">
          <div class="relative hidden md:block w-64">
            <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
               <span class="material-symbols-outlined text-[18px]">search</span>
            </span>
            <input type="text" placeholder="Buscar expedientes..." class="w-full pl-9 pr-4 py-1.5 bg-slate-100/80 hover:bg-slate-100 border border-transparent focus:border-indigo-300 focus:bg-white focus:ring-2 focus:ring-indigo-100 rounded-lg text-sm transition-all outline-none">
          </div>

          <div class="h-6 w-px bg-slate-200 hidden md:block mx-1"></div>

          <!-- UI Density Toggle (Zero-If Rule Target) -->
          <div class="flex items-center gap-1 bg-slate-100 p-1 rounded-lg">
             <button 
                 @click="preferencesStore.uiDensity = 'COMPACT'" 
                 :class="{'bg-white shadow text-indigo-600': preferencesStore.uiDensity === 'COMPACT', 'text-slate-400': preferencesStore.uiDensity !== 'COMPACT'}"
                 class="p-1 rounded text-xs px-2 font-medium hover:text-indigo-500 transition-all focus:outline-none" title="Compacto"
             >
                <span class="material-symbols-outlined text-[16px]">compress</span>
             </button>
             <button 
                 @click="preferencesStore.uiDensity = 'STANDARD'" 
                 :class="{'bg-white shadow text-indigo-600': preferencesStore.uiDensity === 'STANDARD', 'text-slate-400': preferencesStore.uiDensity !== 'STANDARD'}"
                 class="p-1 rounded text-xs px-2 font-medium hover:text-indigo-500 transition-all focus:outline-none" title="Estándar"
             >
                <span class="material-symbols-outlined text-[16px]">view_agenda</span>
             </button>
             <button 
                 @click="preferencesStore.uiDensity = 'COMFORTABLE'" 
                 :class="{'bg-white shadow text-indigo-600': preferencesStore.uiDensity === 'COMFORTABLE', 'text-slate-400': preferencesStore.uiDensity !== 'COMFORTABLE'}"
                 class="p-1 rounded text-xs px-2 font-medium hover:text-indigo-500 transition-all focus:outline-none" title="Cómodo"
             >
                <span class="material-symbols-outlined text-[16px]">expand</span>
             </button>
          </div>

          <button class="relative p-1.5 rounded-full text-slate-400 hover:bg-slate-100 hover:text-indigo-600 transition-colors">
            <span class="material-symbols-outlined text-[22px]">notifications</span>
            <span class="absolute top-1 right-1 size-2 bg-red-500 rounded-full border-2 border-white"></span>
          </button>
          
          <button class="p-1.5 rounded-full text-slate-400 hover:bg-slate-100 hover:text-indigo-600 transition-colors">
            <span class="material-symbols-outlined text-[22px]">help</span>
          </button>
        </div>
      </header>
      
      <!-- Lienzo donde se renderizan las vistas secundarias (Router View) -->
      <div class="flex-1 overflow-auto bg-transparent">
        <router-view />
      </div>

    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { usePreferencesStore } from '@/stores/usePreferencesStore';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const preferencesStore = usePreferencesStore();
const authStore = useAuthStore();

// CA-11: Indicador Tipográfico Multi-Rol (Extrae y formatea máximo 2 roles del JWT EntraID)
const topRolesTipText = computed(() => {
  if (!authStore.roles || authStore.roles.length === 0) return 'Identidad Básica';
  return authStore.roles.slice(0, 2).map((r: string) => {
      const clean = r.replace('ROLE_', '').replace(/_/g, ' ');
      return clean.charAt(0).toUpperCase() + clean.slice(1).toLowerCase();
  }).join(' | ');
});

// Estado del Sidebar principal (Colapsado para lectura profunda o expandido)
const isSidebarCollapsed = ref(true);

// Estado del Submenú Acordeón de Administración
const isAdminExpanded = ref(false);

const toggleSidebar = () => {
    isSidebarCollapsed.value = !isSidebarCollapsed.value;
    // Si colapso el sidebar, opcionalmente repliego el admin menu para limpieza visual al volver a abrir
    if(isSidebarCollapsed.value) {
       isAdminExpanded.value = false;
    }
};

const toggleAdminMenu = () => {
    // Si el menú estaba colapsado y pido abrir admin, despliego primero el sidebar
    if (isSidebarCollapsed.value) {
        isSidebarCollapsed.value = false;
        isAdminExpanded.value = true;
    } else {
        isAdminExpanded.value = !isAdminExpanded.value;
    }
};

const logout = () => {
  localStorage.removeItem('ibpms_token');
  router.push('/login');
};
</script>

<style scoped>
/* Tipografía y Setup Global */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,300,0,0');

.material-symbols-outlined {
  font-family: 'Material Symbols Outlined';
  font-weight: normal;
  font-style: normal;
  font-variation-settings: 'FILL' 0, 'wght' 300, 'GRAD' 0, 'opsz' 24;
  display: inline-block;
  white-space: nowrap;
  word-wrap: normal;
  direction: ltr;
  -webkit-font-smoothing: antialiased;
}

/* Ocultar barra de scroll en el Sidebar */
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* Base styles for Nav Items */
.nav-item {
  @apply flex items-center h-11 px-3 rounded-lg text-slate-400 hover:text-slate-100 hover:bg-slate-800/80 transition-all duration-200 relative cursor-pointer w-full;
}

/* Estilos de Iconos Globales */
.nav-icon {
  @apply text-[22px] shrink-0 flex items-center justify-center;
}
/* Centrado del icono cuando está colapsado */
aside.w-16 .nav-icon {
  @apply mx-auto;
}
/* Espaciado del icono cuando expandido */
aside.w-64 .nav-icon {
  @apply mr-3;
}

/* Texto de Navegación */
.nav-text {
  @apply text-sm font-medium tracking-wide whitespace-nowrap overflow-hidden text-ellipsis;
}

/* Estado Activo para Enlaces Principales */
.nav-active {
  @apply text-indigo-400 bg-indigo-500/10;
}
.nav-active .nav-icon {
  @apply text-indigo-400;
}
/* Marca vertical dinámica para activos */
.nav-active::before {
  content: '';
  @apply absolute left-0 top-1/4 h-1/2 w-1 bg-indigo-500 rounded-r-md;
}

/* Sub Navigation Items (Acordeón Admin) */
.sub-nav-item {
  @apply flex items-center px-3 py-2 rounded-md text-[13px] font-medium text-slate-400 hover:text-slate-200 hover:bg-slate-800/60 transition-colors w-full whitespace-nowrap overflow-hidden text-ellipsis;
}
.sub-nav-active {
  @apply text-indigo-300 bg-slate-800 font-semibold;
}

/* Animations */
.fade-in {
  animation: fadeIn 0.3s ease-in-out forwards;
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* Tooltip Mockup flotante para Sidebar Colapsado */
.tooltip-mockup {
  @apply absolute left-[68px] top-1/2 -translate-y-1/2 bg-[#1A1C1E] text-white px-3 py-1.5 rounded-md text-xs font-semibold shadow-xl border border-slate-700 whitespace-nowrap opacity-0 pointer-events-none transition-opacity duration-200 z-50 flex items-center;
}
.group\/link:hover .tooltip-mockup, .group\/admin:hover .tooltip-mockup {
  @apply opacity-100;
}
</style>
