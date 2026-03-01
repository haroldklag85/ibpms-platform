<template>
  <div class="h-full flex flex-col pt-2 bg-gray-50">
    <!-- Header Admin -->
    <div class="flex justify-between items-center mb-6 px-6">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">Consola de Administración</h2>
        <p class="text-gray-500 text-sm mt-1">Gestión centralizada de permisos e integraciones.</p>
      </div>
    </div>

    <div class="flex-1 flex px-6 pb-6 space-x-6 overflow-hidden">
      <!-- Sidebar Settings Vertical -->
      <div class="w-64 bg-white border border-gray-200 rounded-lg shadow-sm h-fit">
        <ul class="flex flex-col py-2">
          <li>
            <button @click="currentTab = 'profile'" :class="[currentTab === 'profile' ? 'bg-blue-50 text-ibpms-brand font-bold border-r-4 border-ibpms-brand' : 'text-gray-600 hover:bg-gray-50']" class="w-full text-left px-5 py-3 transition">
              👤 Perfil V1
            </button>
          </li>
          <li>
            <button @click="currentTab = 'webhooks'" :class="[currentTab === 'webhooks' ? 'bg-blue-50 text-ibpms-brand font-bold border-r-4 border-ibpms-brand' : 'text-gray-600 hover:bg-gray-50']" class="w-full text-left px-5 py-3 transition">
              🔗 Integraciones (Webhooks)
            </button>
          </li>
          <li>
            <button @click="currentTab = 'users'" :class="[currentTab === 'users' ? 'bg-blue-50 text-ibpms-brand font-bold border-r-4 border-ibpms-brand' : 'text-gray-600 hover:bg-gray-50']" class="w-full text-left px-5 py-3 transition">
              👥 Gestión RBAC
            </button>
          </li>
        </ul>
      </div>

      <!-- Área Dinámica de Settings -->
      <div class="flex-1 overflow-y-auto bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        
        <div v-if="currentTab === 'webhooks'" class="animate-fade-in">
          <div class="mb-5">
            <h3 class="text-lg font-bold text-gray-800 border-b pb-2 mb-3">API & Webhooks</h3>
            <p class="text-sm text-gray-600">Añade URLs externas para ser notificadas (Outbound) o expón Endpoints seguros al internet para disparar tareas internamente (Inbound).</p>
          </div>

          <IntegrationsList :items="webhooks" @add-new="isSlideOpen = true" />
        </div>

        <div v-else-if="currentTab === 'users'" class="animate-fade-in h-full">
           <RbacManager :users="mockUsers" @edit-roles="openRoleModal" />
        </div>

        <div v-else class="flex flex-col items-center justify-center h-full text-gray-400">
           <span class="text-4xl mb-4">🚧</span>
           <p>El Módulo "{{ currentTab }}" entrará en el próximo Sprint.</p>
        </div>

      </div>
    </div>

    <!-- Slide-Over Panel (Nuevo Webhook Form) -->
    <div v-if="isSlideOpen" class="fixed inset-0 bg-black/40 z-50 flex justify-end transition-opacity">
      <div class="w-full max-w-md bg-white h-full shadow-2xl animate-slide-in flex flex-col">
        <div class="p-6 border-b flex justify-between items-center bg-gray-50">
          <h2 class="text-lg font-bold text-gray-800">Nueva Integración</h2>
          <button @click="isSlideOpen = false" class="text-gray-500 hover:text-red-500 font-bold text-xl">&times;</button>
        </div>
        
        <!-- Formulario (Simulado) -->
        <div class="p-6 flex-1 overflow-y-auto space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Dirección del Enrutamiento</label>
            <select v-model="draftType" class="w-full px-3 py-2 border rounded shadow-sm focus:ring-ibpms focus:border-ibpms">
              <option value="INBOUND">Inbound (Escuchar de Afuera)</option>
              <option value="OUTBOUND">Outbound (Notificar hacia Afuera)</option>
            </select>
          </div>

          <div>
             <label class="block text-sm font-medium text-gray-700 mb-1">Nombre Descriptivo</label>
             <input type="text" placeholder="Ej: AWS Lambda Notifier" class="w-full px-3 py-2 border rounded shadow-sm">
          </div>

          <div v-if="draftType === 'OUTBOUND'">
             <label class="block text-sm font-medium text-gray-700 mb-1">Target URL (Webhook Externo)</label>
             <input type="url" placeholder="https://api.empresa.com/webhook" class="w-full font-mono text-sm px-3 py-2 border rounded shadow-sm">
          </div>

          <div v-if="draftType === 'INBOUND'">
             <div class="bg-purple-50 p-3 rounded border border-purple-100 mt-2">
               <p class="text-xs text-purple-800 font-medium mb-1">Secreto Generado Automáticamente:</p>
               <p class="font-mono text-xs break-all text-gray-600 bg-white border p-1 rounded">sk_test_8923hjf9823h8f2y3r823f</p>
               <p class="text-xs text-gray-500 mt-2">Este token deberá ser inyectado en la cabecera `X-iBPMS-Token` por el emisor.</p>
             </div>
          </div>
        </div>

        <div class="p-4 border-t bg-gray-50 flex justify-end space-x-3">
          <button @click="isSlideOpen = false" class="px-4 py-2 border bg-white rounded text-sm hover:bg-gray-100">Cancelar</button>
          <button @click="saveWebhook" class="px-4 py-2 bg-ibpms-brand text-white text-sm font-bold rounded shadow-sm hover:bg-blue-600">
            Registrar Webhook
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import IntegrationsList from '@/components/admin/IntegrationsList.vue';
import RbacManager from '@/components/admin/RbacManager.vue';
import type { WebhookConfig, WebhookDirection } from '@/types/Integration';
import type { UserProfile } from '@/types/Security';

const currentTab = ref('users'); // Empezar probando el Tab de Usuarios
const isSlideOpen = ref(false);
const draftType = ref<WebhookDirection>('OUTBOUND');

// Mock Data Users
const mockUsers = ref<UserProfile[]>([
  { userId: "c.romero", fullName: "Carlos Romero", email: "c.romero@empresa.com", department: "Operaciones", assignedRoles: ["RECLAMADOR", "BASICO"], isActive: true },
  { userId: "a.luna", fullName: "Ana Luna", email: "a.luna@empresa.com", department: "Sistemas (TI)", assignedRoles: ["ADMIN_PLATAFORMA", "SUPERVISOR"], isActive: true },
  { userId: "e.perez", fullName: "Elena Pérez", email: "e.perez@empresa.com", department: "Finanzas", assignedRoles: ["APROBADOR_PAGOS"], isActive: false }
]);

const openRoleModal = (user: UserProfile) => {
  alert(`Desplegando modal de asignación de roles para: ${user.fullName}`);
};

// Mock Data (Listado traído desde `IntegrationService.ts` a futuro)
const webhooks = ref<WebhookConfig[]>([
  {
    id: "wh_01",
    name: "Zapier Slack Notifier",
    direction: "OUTBOUND",
    targetUrl: "https://hooks.zapier.com/hooks/catch/123/abc",
    triggerEvent: "Task_Completed",
    status: "ACTIVE",
    secretToken: "****"
  },
  {
    id: "wh_02",
    name: "ERP Facturación Gatillo",
    direction: "INBOUND",
    processDefinitionKey: "Process_Facturacion_01",
    status: "ACTIVE",
    secretToken: "sk_live_v23b4k2j4bk2j4bk2j34"
  }
]);

const saveWebhook = () => {
  // Simulando llamada backend
  alert("Webhook guardado exitosamente. En un entorno real se crearía el Ingress en APIM y BBDD.");
  isSlideOpen.value = false;
};
</script>

<style scoped>
.animate-slide-in {
  animation: slideIn 0.3s ease-out forwards;
}
.animate-fade-in {
  animation: fadeIn 0.3s ease-out forwards;
}

@keyframes slideIn {
  from { transform: translateX(100%); }
  to { transform: translateX(0); }
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
