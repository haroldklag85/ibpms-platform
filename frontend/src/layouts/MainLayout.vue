<template>
  <div class="h-screen flex bg-slate-50 font-['Inter'] text-slate-900 overflow-hidden">
    <!-- Global Role Switching Loader Overlay (A6) -->
    <div v-if="isRoleSwitching" class="fixed inset-0 bg-slate-900/75 backdrop-blur-sm z-[9999] flex flex-col items-center justify-center text-white">
      <span class="material-symbols-outlined animate-spin text-5xl text-indigo-400 mb-4">sync</span>
      <p class="text-lg font-semibold tracking-wide">Reconfigurando entorno de trabajo...</p>
    </div>

    <!-- Toast de Notificaciones del Layout (A5) -->
    <Transition name="toast-slide">
      <div v-if="layoutToast" class="fixed top-4 right-4 z-[10000] bg-amber-500 text-white px-5 py-3 rounded-lg shadow-xl flex items-center space-x-3">
        <span class="material-symbols-outlined text-white text-xl">warning</span>
        <span class="text-sm font-medium">{{ layoutToast }}</span>
        <button @click="layoutToast = ''" class="ml-2 text-amber-200 hover:text-white">&times;</button>
      </div>
    </Transition>

    <ImpersonationBanner v-if="authStore.isImpersonating" />
    <ImpersonationSelector v-if="showImpersonationSelector" @close="showImpersonationSelector = false" />
    <CQRSConnectionToast />
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
          :title="isSidebarCollapsed ? t('sidebar.expand') : t('sidebar.collapse')"
        >
          <span class="material-symbols-outlined text-[18px]">
            {{ isSidebarCollapsed ? 'chevron_right' : 'chevron_left' }}
          </span>
        </button>
      </div>
      
      <!-- Navigation Menu Dinámico (CA-6) -->
      <nav class="flex-1 overflow-y-auto overflow-x-hidden px-3 no-scrollbar flex flex-col gap-1 w-full relative">
         <!-- Enlace Directo al Portal (Pantalla 0) -->
         <router-link to="/" class="nav-item group/link" active-class="nav-active" title="Portal">
            <span class="material-symbols-outlined nav-icon">home</span>
            <span v-if="!isSidebarCollapsed" class="nav-text flex-1">Portal</span>
            <div v-if="isSidebarCollapsed" class="tooltip-mockup">Portal</div>
         </router-link>

         <!-- Loading Phases -->
         <div v-if="menuStore.isLoading" class="p-4">
            <!-- Skeleton Phase (0-5s) -->
            <div v-if="loadingPhase === 'skeleton'" class="flex flex-col gap-3">
               <div class="h-10 bg-slate-800/50 rounded animate-pulse w-full"></div>
               <div class="h-10 bg-slate-800/50 rounded animate-pulse w-full"></div>
               <div class="h-10 bg-slate-800/50 rounded animate-pulse w-5/6"></div>
               <div class="h-10 bg-slate-800/50 rounded animate-pulse w-4/6"></div>
            </div>
            
            <!-- Spinner Phase (5-15s) -->
            <div v-else-if="loadingPhase === 'spinner'" class="flex flex-col items-center justify-center gap-2 mt-4 fade-in">
               <span class="material-symbols-outlined animate-spin text-indigo-400 text-3xl">sync</span>
               <span v-if="!isSidebarCollapsed" class="text-xs text-slate-400 font-medium text-center">Cargando módulos...</span>
            </div>

            <!-- Timeout Phase (>15s) -->
            <div v-else-if="loadingPhase === 'timeout'" class="flex flex-col items-center justify-center gap-2 mt-4 fade-in">
               <span class="material-symbols-outlined text-red-400 text-3xl">error_outline</span>
               <span v-if="!isSidebarCollapsed" class="text-xs text-slate-400 text-center">Demasiado tiempo</span>
               <button v-if="!isSidebarCollapsed" @click="menuStore.fetchMenuLayout()" class="mt-2 text-[10px] uppercase font-bold tracking-wider px-3 py-1 bg-slate-800 text-white rounded hover:bg-slate-700 transition-colors">Reintentar</button>
            </div>
         </div>
         
         <!-- @Traceability: US-036 - CA-26 -->
         <!-- Fallback Visual CA-26: Sin topología de menús -->
         <template v-else-if="menuStore.layout.length === 0">
            <div class="flex flex-col items-center justify-center p-4 mt-8 text-center fade-in" v-if="!isSidebarCollapsed">
                <span class="material-symbols-outlined text-4xl text-slate-600 mb-2">security_update_warning</span>
                <p class="text-xs text-slate-400 font-bold">Sin Topología de Menús</p>
                <p class="text-[10px] text-slate-500 mt-1">Sus roles no tienen acceso a ningún módulo. Contacte al Administrador o CISO.</p>
            </div>
            <div class="flex flex-col items-center justify-center p-2 mt-8 text-center fade-in" v-else>
                <span class="material-symbols-outlined text-2xl text-slate-600" title="Sin Topología de Menús">security_update_warning</span>
            </div>
         </template>
         
         <template v-else v-for="(group, gIdx) in menuStore.layout" :key="'g'+gIdx">
            <template v-if="true">
               
               <!-- Separador Visual / Título del Grupo -->
               <div v-if="gIdx > 0" class="h-px bg-slate-800 my-4 mx-2"></div>
               <p v-if="!isSidebarCollapsed && group.title === 'Workdesk'" class="text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-2 px-2 fade-in">{{ group.title }}</p>

               <!-- Renderizado Plano (Si es Workdesk/Operación) o Acordeón para otros -->
               <template v-if="group.title === 'Workdesk'">
                   <router-link v-for="(item, iIdx) in group.items" :key="'w'+iIdx" :to="item.path" class="nav-item group/link" active-class="nav-active" :title="item.label">
                      <span class="material-symbols-outlined nav-icon">{{ item.icon }}</span>
                      <span v-if="!isSidebarCollapsed" class="nav-text flex-1">{{ item.label }}</span>
                      <div v-if="isSidebarCollapsed" class="tooltip-mockup">{{ item.label }}</div>
                   </router-link>
               </template>

               <!-- Renderizado Acordeón -->
               <template v-else>
                   <div 
                      @click="toggleGroup(group.title)" 
                      class="nav-item cursor-pointer group/admin relative flex items-center"
                      :class="{ 'bg-slate-800/50 text-white': isGroupExpanded(group.title) && !isSidebarCollapsed }"
                      :title="group.title"
                   >
                      <span class="material-symbols-outlined nav-icon" :class="{ 'text-indigo-400': isGroupExpanded(group.title) }">account_tree</span>
                      <span v-if="!isSidebarCollapsed" class="nav-text flex-1" :class="{ 'font-semibold': isGroupExpanded(group.title) }">{{ group.title }}</span>
                      <span v-if="!isSidebarCollapsed" class="material-symbols-outlined text-[16px] text-slate-500 transition-transform duration-200" :class="{ 'rotate-180': isGroupExpanded(group.title) }">expand_more</span>
                      <div v-if="isSidebarCollapsed" class="tooltip-mockup">{{ group.title }}</div>
                   </div>

                   <!-- Sub-Items del Acordeón -->
                   <div v-show="isGroupExpanded(group.title) && !isSidebarCollapsed" class="flex flex-col gap-1 pl-9 pr-2 mt-1 fade-in">
                      <router-link v-for="(item, iIdx) in group.items" :key="'i'+iIdx" :to="item.path" class="sub-nav-item" active-class="sub-nav-active">
                          <span class="material-symbols-outlined text-[14px] mr-2">{{ item.icon }}</span> {{ item.label }}
                      </router-link>
                   </div>
               </template>

            </template>
         </template>

      </nav>

      <!-- Bottom Profile / Sign Out -->
      <div class="mt-auto px-3 pt-4 border-t border-slate-800 flex flex-col gap-2">
        <button @click="logout" class="nav-item text-red-400 hover:text-red-300 hover:bg-red-500/10 group/link" :title="t('header.logout')">
           <span class="material-symbols-outlined nav-icon">logout</span>
           <span v-if="!isSidebarCollapsed" class="nav-text">{{ t('header.logout') }}</span>
           <div v-if="isSidebarCollapsed" class="tooltip-mockup !bg-red-900 border border-red-800">{{ t('header.logout') }}</div>
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
        
        <!-- Breadcrumbs Dinámicos (R3-B) -->
        <div class="flex items-center gap-1 hidden sm:flex truncate flex-1">
           <router-link to="/" class="flex items-center text-sm font-medium text-slate-500 hover:text-indigo-600 transition-colors">
              <span class="material-symbols-outlined text-[18px] mr-1">business_center</span>
              <span>iBPMS</span>
           </router-link>
           <template v-for="(crumb, idx) in breadcrumbs" :key="idx">
              <span class="mx-1 text-slate-300 text-xs">/</span>
              <router-link v-if="idx < breadcrumbs.length - 1" :to="crumb.path" class="text-sm font-medium text-slate-500 hover:text-indigo-600 transition-colors truncate">
                {{ crumb.label }}
              </router-link>
              <span v-else class="text-sm font-bold text-slate-900 truncate">{{ crumb.label }}</span>
           </template>
        </div>

        <div class="flex items-center gap-4 shrink-0">
          <div v-if="!isMobile" class="relative hidden md:block w-64">
            <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
               <span class="material-symbols-outlined text-[18px]">search</span>
            </span>
            <input type="text" :placeholder="t('header.search')" class="w-full pl-9 pr-4 py-1.5 bg-slate-100/80 hover:bg-slate-100 border border-transparent focus:border-indigo-300 focus:bg-white focus:ring-2 focus:ring-indigo-100 rounded-lg text-sm transition-all outline-none">
          </div>

          <!-- CA-11: Chip de Roles Operativos (Identidad Multi-Rol) -->
          <div v-if="!isMobile" class="hidden lg:flex items-center gap-1.5 px-3 py-1 bg-indigo-50 border border-indigo-100 rounded-full shrink-0 shadow-sm" :title="'Roles Activos: ' + topRolesTipText">
             <span class="material-symbols-outlined text-indigo-600 text-[14px]">verified_user</span>
             <span class="text-[10px] font-bold text-indigo-700 truncate max-w-[150px]">{{ topRolesTipText }}</span>
          </div>

          <RoleSelectorDropdown v-if="authStore.roles.length > 1" />

          <div class="h-6 w-px bg-slate-200 hidden md:block mx-1"></div>

          <!-- UI Density Toggle (Zero-If Rule Target) -->
          <div class="flex items-center gap-1 bg-slate-100 p-1 rounded-lg">
             <button 
                 @click="preferencesStore.uiDensity = 'COMPACT'" 
                 :class="{'bg-white shadow text-indigo-600': preferencesStore.uiDensity === 'COMPACT', 'text-slate-400': preferencesStore.uiDensity !== 'COMPACT'}"
                 class="p-1 rounded text-xs px-2 font-medium hover:text-indigo-500 transition-all focus:outline-none" :title="t('header.compact')"
             >
                <span class="material-symbols-outlined text-[16px]">compress</span>
             </button>
             <button 
                 @click="preferencesStore.uiDensity = 'STANDARD'" 
                 :class="{'bg-white shadow text-indigo-600': preferencesStore.uiDensity === 'STANDARD', 'text-slate-400': preferencesStore.uiDensity !== 'STANDARD'}"
                 class="p-1 rounded text-xs px-2 font-medium hover:text-indigo-500 transition-all focus:outline-none" :title="t('header.standard')"
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

          <!-- Botón Ver Como (Impersonation) -->
          <button v-if="authStore.hasAnyRole(['ROLE_SUPER_ADMIN'])" @click="showImpersonationSelector = true" class="relative p-1.5 rounded-full text-slate-400 hover:bg-slate-100 hover:text-indigo-600 transition-colors" :title="t('header.viewAs')">
            <span class="material-symbols-outlined text-[22px]">switch_account</span>
          </button>

          <!-- Toggle de Idioma -->
          <button @click="toggleLocale" class="relative p-1.5 rounded-full text-slate-400 hover:bg-slate-100 hover:text-indigo-600 transition-colors" :title="t('header.language')">
            {{ locale === 'es' ? '🇪🇸' : '🇺🇸' }}
          </button>

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
      <div class="flex-1 overflow-auto bg-transparent relative">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <!-- @Traceability(US = "US-001", CA = {"CA-12"}) Acierto UX: Keep-Alive retiene scroll y filtros en RAM para 0ms de carga en regresos -->
            <keep-alive include="Workdesk">
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </div>

    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { usePreferencesStore } from '@/stores/usePreferencesStore';
import { useAuthStore } from '@/stores/authStore';
import { useMenuStore } from '@/stores/useMenuStore';
import RoleSelectorDropdown from '@/components/shell/RoleSelectorDropdown.vue';
import ImpersonationBanner from '@/components/admin/ImpersonationBanner.vue';
import ImpersonationSelector from '@/components/admin/ImpersonationSelector.vue';
import CQRSConnectionToast from '@/components/common/CQRSConnectionToast.vue';
import { useI18n } from 'vue-i18n';

const router = useRouter();
const route = useRoute();
const preferencesStore = usePreferencesStore();
const authStore = useAuthStore();
const menuStore = useMenuStore();
const { t, locale } = useI18n();

const showImpersonationSelector = ref(false);
const isRoleSwitching = ref(false);
const layoutToast = ref('');

const handleRoleSwitchStart = () => {
    isRoleSwitching.value = true;
};
const handleRoleSwitchEnd = () => {
    isRoleSwitching.value = false;
};
const handleLayoutToast = (event: Event) => {
    const customEvent = event as CustomEvent;
    layoutToast.value = customEvent.detail?.message || '';
    setTimeout(() => {
        if (layoutToast.value === customEvent.detail?.message) {
            layoutToast.value = '';
        }
    }, 4000);
};

const toggleLocale = () => {
    locale.value = locale.value === 'es' ? 'en' : 'es';
    localStorage.setItem('ibpms_locale', locale.value);
};

// R3-B: Mapa de nombres legibles para breadcrumbs
const routeNameMap: Record<string, string> = {
  'workdesk': 'Bandeja Unificada',
  'inbox': 'Workdesk Legacy',
  'kanban': 'Tablero Kanban',
  'intake-triage': 'Inbox Intake',
  'admin': 'Administración',
  'modeler': 'Diseñador',
  'bpmn': 'BPMN Modeler',
  'dmn': 'DMN Copilot',
  'forms': 'Formularios',
  'designer': 'Diseñador Formularios',
  'integration': 'Integración',
  'catalog': 'Catálogo Conectores',
  'builder': 'Constructor API',
  'mapper': 'Visual Mapper',
  'dlq': 'DLQ Dashboard',
  'analytics': 'Análisis',
  'bam': 'BAM Dashboard',
  'security': 'Seguridad',
  'identity': 'Gobernanza Identidades',
  'incidents': 'Centro Incidentes',
  'projects': 'Proyectos',
  'manager': 'Gestor Proyectos',
  'agile-hub': 'Hub Ágil',
  'project-builder': 'Project Builder',
  'intake': 'Intake Manual',
  'customer360': 'Customer 360',
  'mailboxes': 'Buzones SAC',
  'portal': 'Portal',
  'tracking': 'Seguimiento Cliente',
  'sgdea': 'SGDEA',
  'vault': 'Bóveda Documental',
  'ai': 'Inteligencia Artificial',
  'prompts': 'Librería Prompts',
  'pmo': 'PMO',
  'settings': 'Configuración'
};

const breadcrumbs = computed(() => {
  const segments = route.path.split('/').filter(Boolean);
  return segments.map((seg, idx) => ({
    label: routeNameMap[seg] || seg.charAt(0).toUpperCase() + seg.slice(1),
    path: '/' + segments.slice(0, idx + 1).join('/')
  }));
});

const handleRoleSwitch = async () => {
    menuStore.purgeTopology();
    await menuStore.fetchMenuLayout();
};

const isMobile = ref(window.innerWidth < 768);
const handleResize = () => {
  isMobile.value = window.innerWidth < 768;
  if (isMobile.value) {
    isSidebarCollapsed.value = true;
  }
};

onMounted(() => {
    // CA-6: Hidratación dinámica del árbol Topológico de Rutas
    menuStore.fetchMenuLayout();
    window.addEventListener('role-switched', handleRoleSwitch);
    window.addEventListener('role-switching-start', handleRoleSwitchStart);
    window.addEventListener('role-switching-end', handleRoleSwitchEnd);
    window.addEventListener('layout-toast-dispatch', handleLayoutToast);
    window.addEventListener('resize', handleResize);
    handleResize();
});

onUnmounted(() => {
    window.removeEventListener('role-switched', handleRoleSwitch);
    window.removeEventListener('role-switching-start', handleRoleSwitchStart);
    window.removeEventListener('role-switching-end', handleRoleSwitchEnd);
    window.removeEventListener('layout-toast-dispatch', handleLayoutToast);
    window.removeEventListener('resize', handleResize);
    if (loadingTimer1) clearTimeout(loadingTimer1);
    if (loadingTimer2) clearTimeout(loadingTimer2);
});

// Loading Phase Logic
const loadingPhase = ref<'skeleton' | 'spinner' | 'timeout'>('skeleton');
let loadingTimer1: ReturnType<typeof setTimeout> | null = null;
let loadingTimer2: ReturnType<typeof setTimeout> | null = null;

watch(() => menuStore.isLoading, (isLoading) => {
  if (isLoading) {
    loadingPhase.value = 'skeleton';
    loadingTimer1 = setTimeout(() => {
      if (menuStore.isLoading) loadingPhase.value = 'spinner';
    }, 5000);
    loadingTimer2 = setTimeout(() => {
      if (menuStore.isLoading) loadingPhase.value = 'timeout';
    }, 15000);
  } else {
    if (loadingTimer1) clearTimeout(loadingTimer1);
    if (loadingTimer2) clearTimeout(loadingTimer2);
    loadingPhase.value = 'skeleton';
  }
});

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

// Estado dinámico de Acordeones
const expandedGroups = ref<Record<string, boolean>>({});

const isGroupExpanded = (title: string) => {
    return !!expandedGroups.value[title];
};

const toggleGroup = (title: string) => {
    expandedGroups.value[title] = !expandedGroups.value[title];
};

const toggleSidebar = () => {
    isSidebarCollapsed.value = !isSidebarCollapsed.value;
    if(isSidebarCollapsed.value) {
       expandedGroups.value = {};
    }
};

const logout = () => {
  authStore.logout();
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

/* Router Transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
