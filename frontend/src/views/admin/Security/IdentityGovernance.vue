<template>
  <div class="h-full w-full bg-gray-50 flex flex-col p-6 overflow-hidden relative" v-cloak>
    
    <!-- ═══════ Toast Notifications ═══════ -->
    <Transition name="toast-slide">
      <div v-if="toast.msg" :class="toast.type === 'success' ? 'bg-emerald-600' : 'bg-red-600'" class="fixed top-4 right-4 z-[100] text-white px-5 py-3 rounded-lg shadow-xl flex items-center space-x-3">
        <span class="text-sm font-medium">{{ toast.msg }}</span>
        <button @click="toast.msg = ''" class="ml-2 opacity-70 hover:opacity-100">&times;</button>
      </div>
    </Transition>

    <header class="flex justify-between items-center mb-6 shrink-0">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 flex items-center gap-2">
          🛡️ Identity Governance & RBAC (Pantalla 14)
        </h1>
        <p class="text-sm text-gray-500 mt-1">Control de Accesos, Delegaciones, Cuentas de Servicio y Auditoría (Matriz de Procesos).</p>
      </div>
      
      <div class="flex gap-2">
         <button @click="downloadMatrixCsv" class="bg-indigo-50 border border-indigo-200 text-indigo-700 px-4 py-2 rounded-md shadow-sm text-sm font-bold hover:bg-indigo-100 transition flex items-center gap-2">
            ⬇️ Download Access Matrix CSV
         </button>
      </div>
    </header>

    <main class="flex-1 flex flex-col min-h-0 bg-white border border-gray-200 shadow-sm rounded-xl overflow-hidden">
      
      <!-- Sub-navegación -->
      <nav class="flex border-b border-gray-200 bg-gray-50/50 px-2 shrink-0 overflow-x-auto">
        <button 
          v-for="tab in tabs" 
          :key="tab.id"
          @click="currentTab = tab.id"
          :class="currentTab === tab.id ? 'border-b-2 border-indigo-600 text-indigo-700 font-bold bg-white' : 'text-gray-500 hover:text-gray-700 font-medium'"
          class="px-5 py-3 text-sm whitespace-nowrap transition-colors"
        >
          {{ tab.name }}
        </button>
      </nav>

      <!-- Contenedor del Tab Activo -->
      <div class="p-6 overflow-y-auto flex-1 bg-white">
        
        <!-- ============================================== -->
        <!-- TAB 1: USERS (EntraID Sync & Kill Session)     -->
        <!-- ============================================== -->
        <div v-if="currentTab === 'users'">
          <div class="flex justify-between mb-4">
            <h2 class="text-lg font-bold text-gray-800">Directorio Activo (Sincronizado)</h2>
            <input type="text" placeholder="Buscar usuario..." class="border border-gray-300 rounded px-3 py-1.5 text-sm focus:ring-indigo-500 focus:border-indigo-500" />
          </div>
          
          <table class="min-w-full divide-y divide-gray-200 border rounded-lg overflow-hidden">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-4 py-3 text-left text-xs font-bold text-gray-500 uppercase">Usuario</th>
                <th class="px-4 py-3 text-left text-xs font-bold text-gray-500 uppercase">Departamento</th>
                <th class="px-4 py-3 text-left text-xs font-bold text-gray-500 uppercase">Rol Principal</th>
                <th class="px-4 py-3 text-left text-xs font-bold text-gray-500 uppercase">Estado Sesión</th>
                <th class="px-4 py-3 text-right text-xs font-bold text-gray-500 uppercase">Acción de Emergencia</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200 bg-white">
              <tr v-for="user in mockUsers" :key="user.id" class="hover:bg-gray-50">
                <td class="px-4 py-3 text-sm font-medium text-gray-900">{{ user.name }} <span class="text-xs text-gray-400 block">{{ user.email }}</span></td>
                <td class="px-4 py-3 text-sm text-gray-500">{{ user.department }}</td>
                <td class="px-4 py-3 text-sm text-gray-500"><span class="bg-blue-100 text-blue-800 px-2 py-0.5 rounded text-xs font-bold">{{ user.role }}</span></td>
                <td class="px-4 py-3 text-sm">
                  <span v-if="user.active" class="text-emerald-600 font-bold flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-emerald-500 inline-block"></span> Online</span>
                  <span v-else class="text-gray-400">Offline</span>
                </td>
                <td class="px-4 py-3 text-right text-sm">
                  <button @click="killSession(user)" :disabled="!user.active" :class="user.active ? 'bg-red-50 text-red-600 hover:bg-red-100 border-red-200 focus:ring-red-500' : 'bg-gray-50 text-gray-400 cursor-not-allowed border-gray-100'" class="px-3 py-1.5 rounded font-bold border transition text-xs">
                    Kill Session
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- ============================================== -->
        <!-- TAB 2: PROCESS PERMISSIONS (Matrix View)       -->
        <!-- ============================================== -->
        <div v-else-if="currentTab === 'matrix'">
          <div class="mb-4">
            <h2 class="text-lg font-bold text-gray-800">Matriz de Permisos (Rol vs Proceso)</h2>
            <p class="text-sm text-gray-500">I = Initiate (Puede Instanciar), E = Execute (Puede Completar Tareas Humanas).</p>
          </div>
          
          <div class="overflow-x-auto border rounded-xl overflow-hidden shadow-sm">
             <table class="min-w-full divide-y divide-gray-200">
               <thead class="bg-gray-800">
                 <tr>
                   <th class="px-4 py-3 text-left text-xs font-bold text-white uppercase sticky left-0 bg-gray-900 z-10 w-48">Rol Funcional</th>
                   <th v-for="proc in mockProcesses" :key="proc.id" class="px-4 py-3 text-center text-[10px] font-bold text-gray-300 uppercase w-32 border-l border-gray-700">
                     {{ proc.name }}
                   </th>
                 </tr>
               </thead>
               <tbody class="divide-y divide-gray-200 bg-white">
                 <tr v-for="role in mockRoles" :key="role.id" class="hover:bg-indigo-50/30">
                   <td class="px-4 py-3 text-xs font-bold text-gray-900 bg-gray-50/80 sticky left-0 z-10 border-r">{{ role.name }}</td>
                   <td v-for="proc in mockProcesses" :key="proc.id" class="px-2 py-3 text-center border-l bg-white">
                      <div class="flex justify-center items-center gap-3">
                        <label class="flex items-center gap-1 cursor-pointer" title="Puede Iniciar el Proceso">
                          <input type="checkbox" v-model="matrixState[`${role.id}_${proc.id}_I`]" class="w-3.5 h-3.5 text-indigo-600 focus:ring-indigo-500 rounded border-gray-300" @change="markMatrixDirty">
                          <span class="text-[10px] font-bold text-gray-500">I</span>
                        </label>
                        <label class="flex items-center gap-1 cursor-pointer" title="Puede Ejecutar Tareas del Proceso">
                          <input type="checkbox" v-model="matrixState[`${role.id}_${proc.id}_E`]" class="w-3.5 h-3.5 text-emerald-600 focus:ring-emerald-500 rounded border-gray-300" @change="markMatrixDirty">
                          <span class="text-[10px] font-bold text-gray-500">E</span>
                        </label>
                      </div>
                   </td>
                 </tr>
               </tbody>
             </table>
          </div>
          
          <div class="mt-4 flex justify-end">
             <button :disabled="!isMatrixDirty" @click="saveMatrix" class="bg-indigo-600 text-white px-5 py-2 rounded shadow text-sm font-bold disabled:opacity-50 transition">
               Guardar Cambios de Matriz
             </button>
          </div>
        </div>

        <!-- ============================================== -->
        <!-- TAB 3: DELEGATIONS (Self Service)              -->
        <!-- ============================================== -->
        <div v-else-if="currentTab === 'delegations'" class="max-w-3xl border border-gray-200 rounded-lg p-6 bg-gray-50/50">
          <h2 class="text-lg font-bold text-gray-800 mb-2">Delegación de Casillas (Vacaciones / Ausencia)</h2>
          <p class="text-sm text-gray-500 mb-6">Traspasa temporalmente tu poder de ejecución a otro colaborador.</p>
          
          <form @submit.prevent="createDelegation" class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-bold text-gray-700 mb-1">Delegar hacia (Asistente/Colega)</label>
                <select v-model="delForm.targetUser" required class="w-full text-sm border-gray-300 rounded focus:ring-indigo-500 focus:border-indigo-500 p-2 border">
                   <option value="" disabled>Seleccione usuario...</option>
                   <option v-for="u in mockUsers" :key="u.id" :value="u.id">{{ u.name }} ({{ u.role }})</option>
                </select>
              </div>
              <div class="grid grid-cols-2 gap-2">
                <div>
                  <label class="block text-xs font-bold text-gray-700 mb-1">Fecha Inicio</label>
                  <input type="date" v-model="delForm.start" required class="w-full text-sm border-gray-300 rounded focus:ring-indigo-500 p-2 border" />
                </div>
                <div>
                  <label class="block text-xs font-bold text-gray-700 mb-1">Fecha Fin</label>
                  <input type="date" v-model="delForm.end" required class="w-full text-sm border-gray-300 rounded focus:ring-indigo-500 p-2 border" />
                </div>
              </div>
            </div>
            
            <button type="submit" class="bg-purple-600 text-white px-4 py-2 rounded shadow text-sm font-bold hover:bg-purple-700 transition w-full">
              Crear Regla de Delegación
            </button>
          </form>

          <hr class="my-6 border-gray-200" />
          
          <h3 class="text-sm font-bold text-gray-700 mb-3">Delegaciones Activas</h3>
          <ul class="space-y-2">
            <li v-for="d in activeDelegations" :key="d.id" class="bg-white border border-gray-200 p-3 rounded flex justify-between items-center shadow-sm">
              <div>
                <p class="text-sm font-bold text-gray-900">Otorgado a: <span class="text-purple-600">{{ d.targetName }}</span></p>
                <p class="text-xs text-gray-500">Vigencia: {{ d.start }} al {{ d.end }}</p>
              </div>
              <button @click="revokeDelegation(d.id)" class="text-red-500 hover:text-red-700 text-xs font-bold bg-red-50 px-3 py-1.5 rounded transition">Revocar</button>
            </li>
            <li v-if="activeDelegations.length === 0" class="text-xs text-gray-400 py-2 border-dashed border-2 rounded text-center">
               No hay traslados de poder activos.
            </li>
          </ul>
        </div>

        <!-- ============================================== -->
        <!-- TAB 4: SERVICE ACCOUNTS (API Keys)             -->
        <!-- ============================================== -->
        <div v-else-if="currentTab === 'api_keys'" class="max-w-4xl">
          <div class="flex justify-between mb-4">
            <div>
              <h2 class="text-lg font-bold text-gray-800">Cuentas de Servicio (M2M)</h2>
              <p class="text-xs text-red-600 font-bold mt-1">⚠️ ATENCIÓN: Por seguridad, el Secret Key solo se mostrará una vez.</p>
            </div>
            <button @click="generateApiKey" class="bg-emerald-600 text-white px-4 py-2 rounded shadow-sm text-sm font-bold hover:bg-emerald-700 transition">
              + Generar Nueva API Key
            </button>
          </div>
          
          <!-- Modal inline para mostrar el Secret -->
          <div v-if="newlyCreatedSecret" class="mb-6 bg-yellow-50 border-2 border-yellow-400 p-5 rounded-lg shadow-inner">
             <h3 class="text-sm font-bold text-yellow-800 mb-2">¡API Key Generada Exitosamente!</h3>
             <p class="text-xs text-yellow-700 mb-4">Copia este secreto inmediatamente. Una vez cierres este mensaje, no podrás volver a verlo.</p>
             <div class="flex items-center gap-2">
                <input type="text" :value="newlyCreatedSecret" readonly class="flex-1 bg-white border border-yellow-300 font-mono text-sm px-3 py-2 rounded focus:outline-none" />
                <button @click="copySecret" class="bg-yellow-600 text-white px-3 py-2 rounded font-bold text-xs hover:bg-yellow-700 transition">Copiar</button>
             </div>
             <button @click="newlyCreatedSecret = null" class="mt-4 text-xs font-bold text-gray-500 hover:text-gray-800 underline">Ya lo he copiado pacientemente, cerrar aviso.</button>
          </div>

          <table class="min-w-full divide-y divide-gray-200 border rounded-lg overflow-hidden">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-4 py-3 text-left text-xs font-bold text-gray-500 uppercase">App Name</th>
                <th class="px-4 py-3 text-left text-xs font-bold text-gray-500 uppercase">Client ID</th>
                <th class="px-4 py-3 text-left text-xs font-bold text-gray-500 uppercase">Creado</th>
                <th class="px-4 py-3 text-right text-xs font-bold text-gray-500 uppercase">Estado</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200 bg-white">
               <tr v-for="key in apiKeys" :key="key.clientId">
                 <td class="px-4 py-3 text-sm font-bold text-gray-800">{{ key.appName }}</td>
                 <td class="px-4 py-3 text-xs font-mono text-gray-500">{{ key.clientId }}</td>
                 <td class="px-4 py-3 text-xs text-gray-500">{{ key.createdAt }}</td>
                 <td class="px-4 py-3 text-right">
                    <span class="bg-green-100 text-green-800 px-2 py-0.5 rounded text-xs font-bold">ACTIVO</span>
                 </td>
               </tr>
            </tbody>
          </table>
        </div>

      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

// ── Navegación Tabs ──
const tabs = [
  { id: 'users', name: 'Usuarios y Sesiones' },
  { id: 'matrix', name: 'Permisos de Procesos' },
  { id: 'delegations', name: 'Delegaciones' },
  { id: 'api_keys', name: 'Cuentas de Servicio' }
];
const currentTab = ref('users');
const toast = ref<{ msg: string; type: 'success' | 'error' }>({ msg: '', type: 'success' });

const showToast = (msg: string, type: 'success' | 'error' = 'success') => {
  toast.value = { msg, type };
  setTimeout(() => { toast.value.msg = ''; }, 4000);
};

// ── TAB 1: Mocks de Usuarios ──
const mockUsers = ref([
  { id: 'U-001', name: 'Carlos Admin', email: 'cerberos@ibpms.local', department: 'TI', role: 'Global Admin', active: true },
  { id: 'U-002', name: 'Ana Ramos', email: 'aramos@ibpms.local', department: 'Riesgos', role: 'Risk Analyst', active: true },
  { id: 'U-003', name: 'Luisa F.', email: 'lf@ibpms.local', department: 'Ventas', role: 'Comercial', active: false }
]);

const killSession = (user: any) => {
  if (confirm(`⚠️ ¿Desconectar forzosamente al usuario ${user.name} (Destruir JWT Remoto)?`)) {
    user.active = false;
    showToast(`Sesión de ${user.email} terminada y añadida al Blacklist.`, 'success');
  }
};

// ── TAB 2: Mocks Matriz ──
const mockRoles = [
  { id: 'R_GLOBAL', name: 'Global Admin' },
  { id: 'R_RISK', name: 'Risk Analyst' },
  { id: 'R_SALES', name: 'Promotor Ventas' },
  { id: 'R_LEGAL', name: 'Jurídico' }
];
const mockProcesses = [
  { id: 'P_CRED', name: 'Crédito Consumo' },
  { id: 'P_HIPO', name: 'Hipotecario' },
  { id: 'P_PQRS', name: 'Quejas (PQRS)' }
];
const matrixState = ref<Record<string, boolean>>({
  'R_GLOBAL_P_CRED_I': true, 'R_GLOBAL_P_CRED_E': true,
  'R_GLOBAL_P_HIPO_I': true, 'R_GLOBAL_P_HIPO_E': true,
  'R_GLOBAL_P_PQRS_I': true, 'R_GLOBAL_P_PQRS_E': true,
  
  'R_SALES_P_CRED_I': true, 'R_SALES_P_CRED_E': false,
  'R_RISK_P_CRED_I': false, 'R_RISK_P_CRED_E': true,
});
const isMatrixDirty = ref(false);

const markMatrixDirty = () => { isMatrixDirty.value = true; };
const saveMatrix = () => {
  isMatrixDirty.value = false;
  showToast('Matriz de Seguridad propagada hacia Camunda Autorizations.');
};
const downloadMatrixCsv = () => {
  showToast('Descargando auditoría CSV de matriz de permisos (Simulado).');
};

// ── TAB 3: Delegaciones ──
const delForm = ref({ targetUser: '', start: '', end: '' });
const activeDelegations = ref<{ id: string, targetName: string, start: string, end: string }[]>([]);

const createDelegation = () => {
  const tUser = mockUsers.value.find(u => u.id === delForm.value.targetUser);
  activeDelegations.value.push({
    id: `DEL-${Date.now()}`,
    targetName: tUser?.name || 'Desconocido',
    start: delForm.value.start,
    end: delForm.value.end
  });
  showToast('Delegración temporal activada.');
  delForm.value = { targetUser: '', start: '', end: '' };
};
const revokeDelegation = (id: string) => {
  activeDelegations.value = activeDelegations.value.filter(d => d.id !== id);
  showToast('Delegación revocada.', 'error');
};

// ── TAB 4: API Keys ──
const apiKeys = ref([
  { appName: 'ERP Oracle NetSuite Adapter', clientId: 'cli_9x8a7s6d5f4g3h2j1k', createdAt: '2026-01-15 08:30' }
]);
const newlyCreatedSecret = ref<string | null>(null);

const generateApiKey = () => {
  const name = prompt('Nombre de la aplicación externa a autorizar:');
  if (!name) return;
  
  const tempClientId = 'cli_' + Math.random().toString(36).substr(2, 10);
  const tempSecret = 'sk_live_' + crypto.randomUUID().replace(/-/g, '');
  
  apiKeys.value.unshift({
    appName: name,
    clientId: tempClientId,
    createdAt: new Date().toISOString().split('T')[0]
  });
  
  newlyCreatedSecret.value = tempSecret;
};

const copySecret = () => {
  if (newlyCreatedSecret.value) {
    navigator.clipboard.writeText(newlyCreatedSecret.value);
    showToast('¡Secreto copiado al portapapeles!');
  }
};
</script>

<style scoped>
.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: all 0.3s ease;
}
.toast-slide-enter-from {
  opacity: 0;
  transform: translateX(100%);
}
.toast-slide-leave-to {
  opacity: 0;
  transform: translateX(100%) translateY(-20px);
}
</style>
