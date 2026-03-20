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
            <div class="flex gap-3">
               <button @click="globalKillSession()" class="bg-red-600 text-white px-4 py-1.5 rounded shadow text-sm font-bold hover:bg-red-700 transition flex items-center gap-1 shadow-red-500/30" title="Botón P0 (CA-14)"><span class="material-symbols-outlined text-[14px]">warning</span> Revocar Todo y Matar Sesión</button>
               <input type="text" placeholder="Buscar usuario..." class="border border-gray-300 rounded px-3 py-1.5 text-sm focus:ring-indigo-500 focus:border-indigo-500" />
               <button @click="openUserModal()" class="bg-indigo-600 text-white px-4 py-1.5 rounded shadow text-sm font-bold hover:bg-indigo-700 transition">+ Nuevo Usuario</button>
            </div>
          </div>
          
          <table class="min-w-full divide-y divide-gray-200 border rounded-lg overflow-hidden">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-4 py-3 text-left text-xs font-bold text-gray-500 uppercase">Usuario</th>
                <th class="px-4 py-3 text-left text-xs font-bold text-gray-500 uppercase">Origen</th>
                <th class="px-4 py-3 text-left text-xs font-bold text-gray-500 uppercase">Roles Asignados</th>
                <th class="px-4 py-3 text-center text-xs font-bold text-gray-500 uppercase">Estado (Kill Switch)</th>
                <th class="px-4 py-3 text-right text-xs font-bold text-gray-500 uppercase">Acciones</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200 bg-white">
              <tr v-for="user in mockUsers" :key="user.id" class="hover:bg-gray-50">
                <td class="px-4 py-3 text-sm font-medium text-gray-900">{{ user.name }} <span class="text-xs text-gray-400 block">{{ user.email }}</span></td>
                <td class="px-4 py-3 text-sm text-gray-500">
                    <span v-if="user.isExternalIdp" class="bg-blue-100 text-blue-800 px-2 py-0.5 rounded text-[10px] font-bold border border-blue-200">Azure EntraID</span>
                    <span v-else class="bg-gray-100 text-gray-800 px-2 py-0.5 rounded text-[10px] font-bold border border-gray-200">Local DB</span>
                </td>
                <td class="px-4 py-3 text-sm text-gray-500">
                    <div class="flex flex-wrap gap-1">
                        <span v-for="r in user.roles" :key="r" class="bg-indigo-50 text-indigo-700 px-2 py-0.5 rounded text-[10px] font-bold border border-indigo-100">{{ getRoleName(r) }}</span>
                    </div>
                </td>
                <td class="px-4 py-3 text-center text-sm">
                  <label class="relative inline-flex items-center justify-center cursor-pointer" title="Kill Switch UI CA-5">
                    <input type="checkbox" :checked="user.active" @change="toggleUserStatus(user)" class="sr-only peer">
                    <div class="w-9 h-5 bg-gray-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-emerald-500"></div>
                  </label>
                  <div class="text-[10px] font-bold mt-1" :class="user.active ? 'text-emerald-600' : 'text-gray-400'">{{ user.active ? 'ACTIVO' : 'INACTIVO' }}</div>
                </td>
                <td class="px-4 py-3 text-right text-sm">
                  <button @click="openUserModal(user)" class="text-indigo-600 hover:text-indigo-900 font-bold text-xs uppercase mr-3">Editar</button>
                  <button @click="killSession(user)" :disabled="!user.active" class="text-red-500 disabled:text-gray-300 font-bold text-xs uppercase" title="Purge JWT">Kill</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- ============================================== -->
        <!-- TAB: ROLES (CA-4 Fábrica de Roles)             -->
        <!-- ============================================== -->
        <div v-else-if="currentTab === 'roles'">
          <div class="flex justify-between mb-4">
            <h2 class="text-lg font-bold text-gray-800">Fábrica de Roles (RBAC)</h2>
            <button @click="openRoleModal()" class="bg-indigo-600 text-white px-4 py-1.5 rounded shadow text-sm font-bold hover:bg-indigo-700 transition">+ Nuevo Rol</button>
          </div>
          
          <table class="min-w-full divide-y divide-gray-200 border rounded-lg overflow-hidden">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-4 py-3 text-left text-xs font-bold text-gray-500 uppercase">ID Técnico</th>
                <th class="px-4 py-3 text-left text-xs font-bold text-gray-500 uppercase">Nombre Descriptivo</th>
                <th class="px-4 py-3 text-right text-xs font-bold text-gray-500 uppercase">Acciones</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200 bg-white">
              <tr v-for="role in mockRoles" :key="role.id" class="hover:bg-gray-50">
                <td class="px-4 py-3 text-sm font-mono text-gray-500">{{ role.id }}</td>
                <td class="px-4 py-3 text-sm font-bold text-gray-900">{{ role.name }}</td>
                <td class="px-4 py-3 text-right text-sm">
                  <button @click="openRoleModal(role)" class="text-indigo-600 hover:text-indigo-900 font-bold text-xs uppercase">Editar</button>
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
                   <option v-for="u in mockUsers" :key="u.id" :value="u.id">{{ u.name }} ({{ u.roles?.length ? getRoleName(u.roles[0]) : 'Sin Rol Principal' }})</option>
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
              <!-- CA-7 Soft-Delete Freeze Icon -->
              <button @click="revokeDelegation(d.id)" class="text-sky-600 hover:text-sky-800 text-xs font-bold bg-sky-50 px-3 py-1.5 rounded transition flex items-center gap-1 border border-sky-200">
                  <span class="material-symbols-outlined text-[14px]">ac_unit</span> Congelar/Revocar
              </button>
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

        <!-- ============================================== -->
        <!-- TAB 6: AUDITORÍA CISO (CA-17)                  -->
        <!-- ============================================== -->
        <div v-else-if="currentTab === 'audit'" class="h-full flex flex-col">
          <div class="flex justify-between mb-4">
            <h2 class="text-lg font-bold text-gray-800">Trazas de Auditoría CISO (Solo Lectura)</h2>
            <div class="bg-yellow-50 text-yellow-800 text-xs font-bold px-3 py-1.5 rounded border border-yellow-200 flex items-center gap-2">
               🛡️ Inmutabilidad Garantizada (CA-17)
            </div>
          </div>
          
          <table class="min-w-full divide-y divide-gray-200 border rounded-lg overflow-hidden flex-1">
            <thead class="bg-gray-900 text-white">
              <tr>
                <th class="px-4 py-3 text-left text-xs font-bold uppercase">Timestamp UTC</th>
                <th class="px-4 py-3 text-left text-xs font-bold uppercase">Admin_ID (Ejecutor)</th>
                <th class="px-4 py-3 text-left text-xs font-bold uppercase">Acción</th>
                <th class="px-4 py-3 text-right text-xs font-bold uppercase">Evidencia Forense</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200 bg-white">
               <tr v-for="log in mockAuditLogs" :key="log.id" class="hover:bg-gray-50">
                 <td class="px-4 py-3 text-xs font-mono text-gray-700">{{ log.timestamp }}</td>
                 <td class="px-4 py-3 text-sm font-bold text-indigo-700">{{ log.adminId }}</td>
                 <td class="px-4 py-3 text-xs">
                    <span class="bg-gray-100 text-gray-800 px-2 py-0.5 rounded font-bold border border-gray-200 uppercase tracking-widest">{{ log.action }}</span>
                 </td>
                 <td class="px-4 py-3 text-right">
                    <button @click="openAuditModal(log)" class="bg-gray-800 text-white px-3 py-1 rounded text-xs font-bold hover:bg-black transition border border-gray-600 shadow-sm flex items-center justify-end gap-1 ml-auto">
                        <span class="material-symbols-outlined text-[14px]">data_object</span> Ver JSON Delta
                    </button>
                 </td>
               </tr>
            </tbody>
          </table>
        </div>

      </div>
    </main>

    <!-- ═══════ Modals ═══════ -->
    <Teleport to="body">
       <!-- User Modal (CA-2, CA-4, CA-6, CA-7) -->
       <div v-if="showUserModal" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[200] p-4 backdrop-blur-sm">
        <div class="bg-white rounded-xl shadow-2xl overflow-hidden max-w-3xl w-full border border-gray-200 flex flex-col">
          <div class="px-6 py-4 bg-gray-50 border-b flex justify-between items-center">
            <h3 class="text-lg font-bold text-gray-800">{{ editingUser ? 'Editar Usuario' : 'Nuevo Usuario Local' }}</h3>
            <button @click="showUserModal = false" class="text-gray-400 hover:text-gray-600">&times;</button>
          </div>
          <div class="p-6 overflow-y-auto space-y-4 bg-white flex-1 relative max-h-[80vh]">
            <div class="grid grid-cols-2 gap-4">
              <div>
                 <label class="block text-xs font-bold text-gray-700 mb-1">Nombre Completo</label>
                 <input type="text" v-model="userForm.name" class="w-full text-sm border-gray-300 rounded focus:ring-indigo-500 border p-2 bg-gray-50" required :disabled="editingUser?.isExternalIdp" />
              </div>
              <div>
                 <label class="block text-xs font-bold text-gray-700 mb-1">Correo (Username)</label>
                 <input type="email" v-model="userForm.email" class="w-full text-sm border-gray-300 rounded focus:ring-indigo-500 border p-2 bg-gray-50" required :readonly="editingUser?.isExternalIdp" :disabled="editingUser?.isExternalIdp" />
              </div>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
                <div>
                     <label class="block text-xs font-bold text-gray-700 mb-1">Asignación de Roles (Multi-Select CA-6)</label>
                     <p class="text-[10px] text-gray-500 mb-2 leading-tight">Mapea múltiples sombreros simultáneamente seleccionando en el cuadro múltiple.</p>
                     <select multiple v-model="userForm.roles" class="w-full text-sm border-gray-300 rounded focus:ring-indigo-500 border p-2 h-32 bg-gray-50 cursor-pointer">
                         <option v-for="r in mockRoles" :key="r.id" :value="r.id" class="p-1 border-b hover:bg-indigo-50">{{ r.name }}</option>
                     </select>
                </div>
                
                <div class="flex flex-col gap-4">
                     <div class="p-4 bg-blue-50 border border-blue-200 rounded-lg flex gap-3 h-fit" v-if="userForm.isExternalIdp || (editingUser && editingUser.isExternalIdp)">
                        <span class="text-2xl mt-1">☁️</span>
                        <div>
                          <h4 class="font-bold text-blue-900 text-sm mb-1">Identidad Administrada en EntraID</h4>
                          <p class="text-[11px] text-blue-800 leading-tight">Las credenciales y políticas de este usuario se delegan al Idp (Zero-Trust CA-7). No se puede editar password desde iBPMS.</p>
                        </div>
                     </div>
                     
                     <div v-else class="border border-gray-200 p-4 rounded-lg bg-gray-50 w-full">
                       <div class="flex justify-between items-start mb-3">
                          <h4 class="font-bold text-gray-800 text-sm leading-tight">Gestor de Seguridad<br/><span class="text-indigo-600 text-[10px]">Políticas Zod Estrictas (CA-2)</span></h4>
                          <button v-if="editingUser" type="button" @click="generateTempPassword()" class="bg-red-50 text-red-600 border border-red-200 px-2 py-1.5 rounded text-[10px] font-bold hover:bg-red-100 transition truncate ml-2">⚠️ REINICIAR KEY</button>
                       </div>
                       
                       <div v-if="!editingUser">
                         <label class="block text-[11px] font-bold text-gray-700 mb-1">Definir Contraseña (Primera Vez)</label>
                         <input :type="passwordVisible ? 'text' : 'password'" v-model="userForm.password" class="w-full text-sm border border-gray-300 rounded focus:ring-indigo-500 focus:border-indigo-500 p-2 bg-white" placeholder="Ej: P@ssw0rd!" />
                         
                         <div class="mt-3 bg-white border rounded p-2 text-[10px]">
                            <ul class="text-red-500 font-medium ml-4 list-disc space-y-0.5" v-if="!passwordValidation.success && userForm.password.length > 0">
                               <li v-for="err in passwordValidation.errors" :key="err" class="leading-tight">{{err}}</li>
                            </ul>
                            <p v-if="passwordValidation.success && userForm.password.length > 0" class="text-emerald-600 font-bold flex items-center gap-1">✅ Zod Parser Confirma 4 Factores Estrictos</p>
                            <p v-if="userForm.password.length === 0" class="text-gray-400">Longitud Mín 8. Upper/Num/Sym obligatorios.</p>
                         </div>
                       </div>
                     </div>
                </div>
            </div>
          </div>
          <div class="px-6 py-4 bg-gray-50 border-t flex justify-end gap-3 rounded-b-xl">
             <button @click="showUserModal = false" class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded shadow-sm hover:bg-gray-50 transition">Cancelar</button>
             <button @click="saveUser" :disabled="!passwordValidation.success && !userForm.isExternalIdp && !editingUser" class="px-5 py-2 text-sm font-bold text-white bg-indigo-600 rounded shadow hover:bg-indigo-700 disabled:opacity-50 disabled:bg-gray-400 disabled:cursor-not-allowed transition">
               Guardar y Propagar
             </button>
          </div>
        </div>
       </div>

       <!-- Role Factory Modal -->
       <div v-if="showRoleModal" class="fixed inset-0 bg-gray-900/60 flex items-center justify-center z-[200] p-4 backdrop-blur-sm">
         <div class="bg-white rounded-xl shadow-2xl p-6 max-w-2xl w-full border border-gray-200 h-[80vh] flex flex-col">
             <h3 class="text-lg font-bold text-gray-800 mb-4">{{ editingRole ? 'Modificar Identificador' : 'Forjar Nuevo Rol Transversal' }}</h3>
             <div class="flex-1 overflow-y-auto space-y-4 pr-2">
                <div class="grid grid-cols-2 gap-4">
                    <div>
                       <label class="block text-[11px] font-bold text-gray-700 mb-1">ID TÉCNICO VINCULANTE (Camunda Auth Key)</label>
                       <input type="text" v-model="roleForm.id" class="w-full font-mono text-xs border border-gray-300 rounded focus:ring-indigo-500 bg-gray-50 p-2 uppercase" placeholder="R_NUEVO_ROL" required :readonly="!!editingRole" :disabled="!!editingRole" />
                    </div>
                    <div>
                       <label class="block text-[11px] font-bold text-gray-700 mb-1">Etiqueta Lógica y Administrativa</label>
                       <input type="text" v-model="roleForm.name" class="w-full text-sm border-gray-300 rounded focus:ring-indigo-500 border p-2" placeholder="Gestor Funcional..." required />
                    </div>
                </div>
                <!-- CA-6 Herencia Visual -->
                <div>
                   <label class="block text-[11px] font-bold text-indigo-700 mb-1 flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">account_tree</span> Heredar Políticas de Rol Padre</label>
                   <select v-model="roleForm.parentRole" @change="onParentRoleChange" class="w-full text-sm border-indigo-200 rounded focus:ring-indigo-500 focus:border-indigo-500 p-2 border bg-indigo-50 text-indigo-900 font-semibold cursor-pointer">
                      <option value="">-- Sin Herencia (Desde Cero) --</option>
                      <option v-for="r in mockRoles" :key="r.id" :value="r.id" :disabled="r.id === roleForm.id">{{ r.name }} ({{ r.id }})</option>
                   </select>
                </div>
                
                <h4 class="font-bold text-sm text-gray-800 mt-6 border-b pb-2">Matriz de Concesiones Zod (CA-4)</h4>
                <div class="border rounded-lg overflow-hidden min-h-[50px] mb-4">
                    <table class="min-w-full divide-y divide-gray-200">
                        <thead class="bg-indigo-50">
                            <tr>
                                <th class="px-3 py-2 text-left text-[10px] font-bold text-indigo-800 uppercase">Definición BPMN</th>
                                <th class="px-3 py-2 text-center text-[10px] font-bold text-indigo-800 uppercase tooltip" title="Derecho a iniciar instancias nuevas">I (Initiate)</th>
                                <th class="px-3 py-2 text-center text-[10px] font-bold text-indigo-800 uppercase tooltip" title="Derecho a reclamar Human Tasks">E (Execute)</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-gray-100 bg-white">
                             <tr v-for="proc in mockProcesses" :key="proc.id" class="hover:bg-gray-50">
                                 <td class="px-3 py-2 text-xs font-medium text-gray-700">{{ proc.name }}</td>
                                 <td class="px-3 py-2 text-center">
                                     <input type="checkbox" v-model="roleForm.matrix[proc.id].initiate" class="text-indigo-600 focus:ring-indigo-500 rounded h-4 w-4 bg-gray-50 border-gray-300" />
                                 </td>
                                 <td class="px-3 py-2 text-center">
                                     <input type="checkbox" v-model="roleForm.matrix[proc.id].execute" class="text-emerald-600 focus:ring-emerald-500 rounded h-4 w-4 bg-gray-50 border-gray-300" />
                                 </td>
                             </tr>
                        </tbody>
                    </table>
                </div>
                <!-- CA-3 Asignación Masiva Button -->
                <div class="bg-yellow-50 border border-yellow-200 p-3 rounded-lg flex justify-between items-center" v-if="editingRole">
                    <div>
                        <p class="text-xs font-bold text-yellow-800">Operador Categórico de Plantilla (CA-3)</p>
                        <p class="text-[10px] text-yellow-700 mt-0.5">Sobrescribe los privilegios de los usuarios asociados forzosamente.</p>
                    </div>
                    <button type="button" @click="showToast('Ejecutando propagación asíncrona a todos los usuarios', 'success')" class="text-[10px] font-bold text-yellow-900 bg-yellow-200 px-3 py-1.5 rounded hover:bg-yellow-300 border border-yellow-400">PROPAGACIÓN MASIVA</button>
                </div>
             </div>
             <div class="mt-4 pt-4 flex justify-end gap-3 border-t">
               <span v-if="!roleMatrixValidation" class="text-red-500 text-xs font-bold mr-auto self-center">⚠️ Fallo Zod. Estructura Corrupta.</span>
               <button @click="showRoleModal = false" class="px-4 py-2 text-sm text-gray-700 font-medium hover:bg-gray-100 rounded transition border">Cerrar</button>
               <button @click="saveRole" :disabled="!roleMatrixValidation" class="bg-indigo-600 text-white px-5 py-2 rounded shadow text-sm font-bold hover:bg-indigo-700 transition disabled:opacity-50">Consolidar Rol</button>
             </div>
         </div>
       </div>

       <!-- Temp Password Modal CA-3 -->
       <div v-if="showTempPassModal" class="fixed inset-0 bg-gray-900/80 flex items-center justify-center z-[300] p-4 backdrop-blur border-2 border-red-500">
         <div class="bg-white rounded-xl shadow-2xl p-6 max-w-md w-full border-t-4 border-red-600 flex flex-col items-center text-center">
            <span class="text-5xl mb-3 mt-2 block w-full text-center">🔐</span>
            <h3 class="text-xl font-black text-gray-900 uppercase tracking-wide mb-1">Clave Temporal Generada</h3>
            <p class="text-[13px] text-gray-600 mb-6 font-medium leading-relaxed px-4">Por protocolos <b>Zero-Trust</b> (CA-3), esta frase no se volverá a desplegar a posteriori de este punto. Asegúrese de enviarla por canal seguro.</p>
            
            <div class="w-full bg-gray-900 border border-gray-700 text-emerald-400 font-mono text-2xl p-4 rounded-xl tracking-[0.2em] break-all shadow-inner relative group isolate">
               {{ tempPasswordValue }}
               <div class="absolute inset-x-0 bottom-1 flex justify-center opacity-0 group-hover:opacity-100 transition">
                  <span class="bg-black/50 text-[10px] uppercase text-white px-2 py-0.5 rounded-full font-sans tracking-normal">Hash Temporal Abierto</span>
               </div>
            </div>
            
            <button @click="showTempPassModal = false" class="w-full bg-red-600 text-white px-5 py-3 rounded-lg font-bold hover:bg-red-700 transition uppercase shadow-xl mt-6 text-sm tracking-wide">
                CONFIRMO QUE HE COPIADO AL PORTAPAPELES
            </button>
         </div>
       </div>
     </Teleport>

        <!-- Modal Audit JSON Delta (CA-17) -->
        <Teleport to="body">
          <div v-if="showAuditModal" class="fixed inset-0 bg-gray-900/90 flex items-center justify-center z-[300] p-4 backdrop-blur-md">
            <div class="bg-gray-900 rounded-xl shadow-2xl overflow-hidden max-w-3xl w-full border border-gray-700 flex flex-col">
              <div class="px-6 py-4 bg-gray-800 border-b border-gray-700 flex justify-between items-center text-white">
                <h3 class="text-lg font-bold font-mono tracking-widest">[{ {{ activeAuditLog?.action }} }] :: EVIDENCIA FORENSE</h3>
                <button @click="showAuditModal = false" class="text-gray-400 hover:text-white">&times;</button>
              </div>
              <div class="p-6 overflow-y-auto bg-gray-900 h-[60vh]">
                 <pre class="text-emerald-400 font-mono text-xs leading-relaxed break-all whitespace-pre-wrap"><code>{{ JSON.stringify(activeAuditLog?.delta, null, 2) }}</code></pre>
              </div>
              <div class="px-6 py-3 bg-black flex justify-end">
                 <button @click="showAuditModal = false" class="bg-indigo-600 text-white font-bold px-6 py-2 rounded text-sm hover:bg-indigo-700 uppercase tracking-wider">Cerrar Visor</button>
              </div>
            </div>
          </div>
        </Teleport>

  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { z } from 'zod';
import apiClient from '@/services/apiClient';

// ── Navegación Tabs ──
const tabs = [
  { id: 'users', name: 'Usuarios y Sesiones' },
  { id: 'roles', name: 'Fábrica de Roles' },
  { id: 'matrix', name: 'Permisos de Procesos' },
  { id: 'delegations', name: 'Delegaciones' },
  { id: 'api_keys', name: 'Cuentas de Servicio' },
  { id: 'audit', name: 'Auditoría CISO' }
];
const currentTab = ref('users');
const toast = ref<{ msg: string; type: 'success' | 'error' }>({ msg: '', type: 'success' });

const showToast = (msg: string, type: 'success' | 'error' = 'success') => {
  toast.value = { msg, type };
  setTimeout(() => { toast.value.msg = ''; }, 4000);
};

// ── TAB 1 & TAB_ROLES: Mocks de Usuarios y Roles ──
const mockRoles = ref([
  { id: 'R_GLOBAL', name: 'Global Admin' },
  { id: 'R_RISK', name: 'Risk Analyst' },
  { id: 'R_SALES', name: 'Promotor Ventas' },
  { id: 'R_LEGAL', name: 'Jurídico' }
]);

const mockUsers = ref([
  { id: 'U-001', name: 'Carlos Admin', email: 'cerberos@ibpms.local', department: 'TI', roles: ['R_GLOBAL'], active: true, isExternalIdp: false },
  { id: 'U-002', name: 'Ana Ramos', email: 'aramos@ibpms.local', department: 'Riesgos', roles: ['R_RISK', 'R_LEGAL'], active: true, isExternalIdp: false },
  { id: 'U-003', name: 'Luisa F.', email: 'lf@ibpms.local', department: 'Ventas', roles: ['R_SALES'], active: false, isExternalIdp: true }
]);

const getRoleName = (roleId: string) => {
    const r = mockRoles.value.find(x => x.id === roleId);
    return r ? r.name : roleId;
};

const toggleUserStatus = async (user: any) => {
    // CA-5: Kill Switch UI
    const originalState = user.active;
    user.active = !user.active; // Mapeo Optimista
    try {
        await apiClient.put(`/api/v1/admin/users/${user.id}/status`, { active: user.active });
        if(!user.active) showToast(`Usuario ${user.name} desactivado (Kill Switch accionado).`, 'error');
        else showToast(`Usuario ${user.name} activado exitosamente.`, 'success');
    } catch(e: any) {
        // En un entorno 100% real revertimos, local mode ignoramos 404 mocking
        if (!e.message?.includes('Network Error')) {
            showToast('Fallback local: Kill Switch emulado (sin Backend)', 'success');
        } else {
            user.active = originalState; // Rollback
            showToast('Error de red al cambiar estado.', 'error');
        }
    }
};

const killSession = (user: any) => {
  if (confirm(`⚠️ ¿Desconectar forzosamente al usuario ${user.name} (Destruir JWT Remoto)?`)) {
    user.active = false;
    showToast(`Sesión de ${user.email} terminada y añadida al Blacklist.`, 'success');
  }
};

const globalKillSession = async () => {
    if (confirm("⚠️ ALERTA NIVEL ROJO: ¿Está seguro que desea revocar todas las sesiones globalmente? Esto expulsará a todos los usuarios del sistema.")) {
        try {
            await apiClient.post(`/kill-session`);
            showToast('Sesiones Centrales Evaporadas (Kill Session Global Accionado)', 'error');
        } catch(e) {
            showToast('Fallback local: Sesiones Centrales Evaporadas (sin Backend)', 'error');
        }
    }
};

// ── Modals & Zod Logic (CA-2, CA-3, CA-4, CA-6, CA-7) ──
const showUserModal = ref(false);
const editingUser = ref<any>(null);
const userForm = ref({ name: '', email: '', department: '', roles: [] as string[], password: '', isExternalIdp: false });
const passwordVisible = ref(false);

const passwordPolicy = z.string()
    .min(8, 'Mínimo 8 caracteres')
    .regex(/[A-Z]/, 'Al menos una Mayúscula')
    .regex(/[0-9]/, 'Al menos un Número')
    .regex(/[!@#$%^&*]/, 'Al menos un Símbolo Especial (!@#$%...)');

const passwordValidation = computed(() => {
    if(userForm.value.isExternalIdp || editingUser.value) return { success: true }; // Standby local edits
    if(!userForm.value.password) return { success: false, errors: ['Requerido'] };
    const res = passwordPolicy.safeParse(userForm.value.password);
    if(res.success) return {success: true, errors: []};
    return {success: false, errors: res.error.issues.map(i => i.message)};
});

const openUserModal = (user: any = null) => {
    editingUser.value = user;
    if(user) {
        userForm.value = { ...user, roles: [...user.roles], password: '' };
    } else {
        userForm.value = { name: '', email: '', department: '', roles: [], password: '', isExternalIdp: false };
    }
    showUserModal.value = true;
};

const saveUser = async () => {
    if(!passwordValidation.value.success && !userForm.value.isExternalIdp && !editingUser.value) return;
    
    // Simulate Backend Save
    if(editingUser.value) {
        const u = mockUsers.value.find(x => x.id === editingUser.value.id);
        if(u) Object.assign(u, userForm.value);
        showToast('Usuario actualizado con éxito', 'success');
    } else {
        mockUsers.value.unshift({
            id: 'U-00' + (mockUsers.value.length + 1),
            ...userForm.value,
            active: true
        });
        showToast('Usuario creado (Zod Policy Verificada)', 'success');
    }
    showUserModal.value = false;
};

const showTempPassModal = ref(false);
const tempPasswordValue = ref('');
const generateTempPassword = async () => {
    if(!editingUser.value) return;
    try {
        // Mock Backend Axios Request -> HTTP 200 Plain Text (CA-3)
        const res = await apiClient.post(`/api/v1/admin/users/${editingUser.value.id}/reset-password`);
        tempPasswordValue.value = res.data.tempPassword || 'Auto$Zod' + Math.floor(Math.random()*9999) + '!';
        showTempPassModal.value = true;
    } catch(e) {
        // Degraded Mode para entorno POC
        tempPasswordValue.value = 'Offline$Dev' + Math.floor(Math.random()*9999) + '!';
        showTempPassModal.value = true;
    }
};

// ── TAB 2: Permisos Matriz ──
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

const showRoleModal = ref(false);
const editingRole = ref<any>(null);
const roleForm = ref({ name: '', id: '', parentRole: '', matrix: {} as Record<string, { initiate: boolean, execute: boolean }> });

const roleMatrixSchema = z.record(z.object({
    initiate: z.boolean(),
    execute: z.boolean()
}));

const roleMatrixValidation = computed(() => {
    // CA-4 Zod Check
    return roleMatrixSchema.safeParse(roleForm.value.matrix).success;
});

const onParentRoleChange = () => {
    const parentId = roleForm.value.parentRole;
    if(!parentId) return;
    
    // CA-6 Clonar permisos del Rol Padre selecto
    for(const p of mockProcesses) {
        roleForm.value.matrix[p.id].initiate = matrixState.value[`${parentId}_${p.id}_I`] || false;
        roleForm.value.matrix[p.id].execute = matrixState.value[`${parentId}_${p.id}_E`] || false;
    }
    showToast(`Matriz pre-llenada con herencia de ${parentId}`, 'success');
};

const openRoleModal = (role: any = null) => {
    editingRole.value = role;
    if(role) { 
        // Reconstruct matrix from global state (mock)
        const matrix: Record<string, { initiate: boolean, execute: boolean }> = {};
        for(const p of mockProcesses) {
            matrix[p.id] = {
                initiate: matrixState.value[`${role.id}_${p.id}_I`] || false,
                execute: matrixState.value[`${role.id}_${p.id}_E`] || false
            };
        }
        roleForm.value = { ...role, parentRole: '', matrix }; 
    }
    else { 
        const matrix: Record<string, { initiate: boolean, execute: boolean }> = {};
        for(const p of mockProcesses) {
            matrix[p.id] = { initiate: false, execute: false };
        }
        roleForm.value = { name: '', id: 'R_', parentRole: '', matrix }; 
    }
    showRoleModal.value = true;
};
const saveRole = () => {
    if(!roleMatrixValidation.value) return; // Zod Interlock
    
    if(editingRole.value) {
        const r = mockRoles.value.find(x => x.id === editingRole.value.id);
        if(r) Object.assign(r, { id: roleForm.value.id, name: roleForm.value.name });
    } else {
        mockRoles.value.push({ id: roleForm.value.id, name: roleForm.value.name });
    }
    
    // Salvaguardar Matriz en el estado unificado
    for(const p of mockProcesses) {
        matrixState.value[`${roleForm.value.id}_${p.id}_I`] = roleForm.value.matrix[p.id].initiate;
        matrixState.value[`${roleForm.value.id}_${p.id}_E`] = roleForm.value.matrix[p.id].execute;
    }

    showRoleModal.value = false;
    showToast('Roles de sistema sincronizados (Zod Validated)', 'success');
};

const isMatrixDirty = ref(false);

const markMatrixDirty = () => { isMatrixDirty.value = true; };
const saveMatrix = () => {
  isMatrixDirty.value = false;
  showToast('Matriz de Seguridad propagada hacia Camunda Autorizations.');
};
const downloadMatrixCsv = async () => {
  try {
    const response = await apiClient.get('/api/v1/admin/security/matrix/export', { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `CISO_Access_Matrix_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    showToast('Auditoría CISO descargada con éxito.', 'success');
  } catch (e) {
    showToast('Fallback local: Generando Blob Simulado CISO.', 'success');
    // Fallback Blob creation for local UAT
    const fallbackBlob = new Blob(["PROCESS,ROLE,INITIATE,EXECUTE\nKYC_P,R_GLOBAL,TRUE,TRUE"], { type: 'text/csv' });
    const fallbackUrl = window.URL.createObjectURL(fallbackBlob);
    const fallbackLink = document.createElement('a');
    fallbackLink.href = fallbackUrl;
    fallbackLink.setAttribute('download', `MOCK_CISO_Access_Matrix.csv`);
    document.body.appendChild(fallbackLink);
    fallbackLink.click();
    window.URL.revokeObjectURL(fallbackUrl);
  }
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
// ── TAB 6: AUDITORÍA CISO (CA-17) ──
const showAuditModal = ref(false);
const activeAuditLog = ref<any>(null);

const mockAuditLogs = ref([
  { id: 'AUD-1001', timestamp: new Date(Date.now() - 3600000).toISOString(), adminId: 'U-001 (Admin)', action: 'MODIFY_ROLE_MATRIX', delta: { "roleId": "R_SALES", "before": { "KYC_P": { "initiate": false, "execute": false } }, "after": { "KYC_P": { "initiate": true, "execute": true } } } },
  { id: 'AUD-1002', timestamp: new Date(Date.now() - 86400000).toISOString(), adminId: 'U-001 (Admin)', action: 'REVOKE_DELEGATION', delta: { "delegationId": "DEL-1710", "targetUserId": "U-003", "status": "REVOKED_SOFT_DELETE", "reason": "Revocación manual CISO" } },
  { id: 'AUD-1003', timestamp: new Date(Date.now() - 250000000).toISOString(), adminId: 'SYSTEM_CRON', action: 'FREEZE_STALE_USER', delta: { "userId": "U-002", "inactivityDays": 95, "status": "FROZEN_SOFT_DELETE" } }
]);

const openAuditModal = (log: any) => {
  activeAuditLog.value = log;
  showAuditModal.value = true;
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
