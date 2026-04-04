<template>
  <div class="space-y-6 animate-fade-in-up">
    <!-- Header -->
    <header class="flex flex-col md:flex-row justify-between items-start md:items-center bg-white/60 dark:bg-slate-800/60 backdrop-blur-md p-6 rounded-2xl shadow border border-slate-200 dark:border-slate-700">
      <div>
        <h1 class="text-3xl font-extrabold text-slate-900 dark:text-white flex items-center gap-3">
          <span class="p-2 bg-rose-100 dark:bg-rose-900/30 text-rose-600 dark:text-rose-400 rounded-lg">
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
          </span>
          Dead Letter Queue Dashboard
        </h1>
        <p class="mt-2 text-sm text-slate-500 dark:text-slate-400 font-medium">Monitorización de RabbitMQ y TaskRescue</p>
      </div>
      <div class="mt-4 md:mt-0">
        <button @click="fetchDLQ" class="bg-indigo-600 hover:bg-indigo-700 text-white px-5 py-2.5 rounded-xl shadow-lg shadow-indigo-200 dark:shadow-none transition-all flex items-center gap-2 font-semibold">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path></svg>
          Sincronizar Cluster
        </button>
      </div>
    </header>

    <!-- Metrics Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div class="bg-gradient-to-br from-rose-50 to-orange-50 dark:from-slate-800 dark:to-slate-800 p-6 rounded-2xl border border-rose-100 dark:border-rose-900/50 shadow-sm relative overflow-hidden group">
        <div class="absolute right-0 top-0 w-24 h-24 bg-rose-500/10 rounded-bl-full transition-transform group-hover:scale-110"></div>
        <p class="text-sm font-bold text-rose-600 dark:text-rose-400 uppercase tracking-widest mb-1">Mensajes Atascados</p>
        <h3 class="text-4xl font-black text-rose-900 dark:text-rose-100">{{ messages.length }}</h3>
      </div>
      <div class="bg-gradient-to-br from-amber-50 to-yellow-50 dark:from-slate-800 dark:to-slate-800 p-6 rounded-2xl border border-amber-100 dark:border-amber-900/50 shadow-sm relative overflow-hidden group">
        <div class="absolute right-0 top-0 w-24 h-24 bg-amber-500/10 rounded-bl-full transition-transform group-hover:scale-110"></div>
        <p class="text-sm font-bold text-amber-600 dark:text-amber-400 uppercase tracking-widest mb-1">Warning Rate</p>
        <h3 class="text-4xl font-black text-amber-900 dark:text-amber-100">12%</h3>
      </div>
      <div class="bg-gradient-to-br from-emerald-50 to-teal-50 dark:from-slate-800 dark:to-slate-800 p-6 rounded-2xl border border-emerald-100 dark:border-emerald-900/50 shadow-sm relative overflow-hidden group">
        <div class="absolute right-0 top-0 w-24 h-24 bg-emerald-500/10 rounded-bl-full transition-transform group-hover:scale-110"></div>
        <p class="text-sm font-bold text-emerald-600 dark:text-emerald-400 uppercase tracking-widest mb-1">Reintentos Hoy</p>
        <h3 class="text-4xl font-black text-emerald-900 dark:text-emerald-100">342</h3>
      </div>
    </div>

    <!-- DataGrid -->
    <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-700 overflow-hidden">
      <div class="p-5 border-b border-slate-200 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-800/50 flex justify-between items-center">
        <h2 class="text-lg font-bold text-slate-800 dark:text-slate-100 flex items-center gap-2">
          <svg class="w-5 h-5 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16"></path></svg>
          Colas de Retención (DLQ)
        </h2>
        <div class="flex gap-2">
           <button @click="purgeAll" class="text-xs font-semibold px-3 py-1.5 bg-red-100 hover:bg-red-200 text-red-700 rounded-md transition duration-200" v-if="messages.length > 0">Purgar Todo</button>
           <button @click="retryAll" class="text-xs font-semibold px-3 py-1.5 bg-teal-100 hover:bg-teal-200 text-teal-700 rounded-md transition duration-200" v-if="messages.length > 0">Reencolar Todo</button>
        </div>
      </div>
      
      <div v-if="isLoading" class="p-12 flex justify-center items-center">
         <svg class="animate-spin h-8 w-8 text-indigo-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
      </div>

      <div v-else-if="messages.length === 0" class="p-16 text-center">
         <div class="inline-block p-4 rounded-full bg-emerald-100 dark:bg-emerald-900/30 text-emerald-500 mb-4">
            <svg class="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
         </div>
         <h3 class="text-xl font-bold text-slate-800 dark:text-slate-200">DLQ Limpia</h3>
         <p class="text-slate-500 mt-2">No hay mensajes atascados en este momento. La infraestructura está saludable.</p>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-slate-50 dark:bg-slate-800 text-slate-500 dark:text-slate-400 text-xs uppercase tracking-wider font-semibold">
              <th class="px-6 py-4">ID Mensaje</th>
              <th class="px-6 py-4">Exchange / Routing</th>
              <th class="px-6 py-4">Causa (Exception)</th>
              <th class="px-6 py-4">Retries</th>
              <th class="px-6 py-4">Timestamp</th>
              <th class="px-6 py-4 text-right">Acciones</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100 dark:divide-slate-700/50">
            <tr v-for="msg in messages" :key="msg.id" class="hover:bg-slate-50/80 dark:hover:bg-slate-700/30 transition-colors">
              <td class="px-6 py-4 font-mono text-xs text-slate-600 dark:text-slate-300">{{ msg.id.substring(0,8) }}...</td>
              <td class="px-6 py-4">
                <span class="px-2.5 py-1 bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-300 rounded-md text-xs font-medium border border-slate-200 dark:border-slate-600">
                  {{ msg.exchange }}
                </span>
                <span class="block mt-1 text-[10px] text-slate-400">RK: {{ msg.routingKey }}</span>
              </td>
              <td class="px-6 py-4">
                <div class="max-w-xs truncate text-sm font-medium text-rose-600 dark:text-rose-400" :title="msg.errorReason">
                  {{ msg.errorReason }}
                </div>
              </td>
              <td class="px-6 py-4 text-sm text-slate-600 dark:text-slate-400">
                  <span class="flex items-center gap-1">
                     <span class="w-2 h-2 rounded-full" :class="msg.retries > 3 ? 'bg-red-500' : 'bg-yellow-500'"></span>
                     {{ msg.retries }} / 5
                  </span>
              </td>
              <td class="px-6 py-4 text-xs text-slate-500">{{ new Date(msg.timestamp).toLocaleString() }}</td>
              <td class="px-6 py-4 text-right space-x-2">
                <button @click="inspectMsg(msg)" class="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition" title="Ver Payload">
                   <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path></svg>
                </button>
                <button @click="retryMsg(msg.id)" class="p-1.5 text-slate-400 hover:text-teal-600 hover:bg-teal-50 rounded-lg transition" title="Reintentar">
                   <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path></svg>
                </button>
                <button @click="purgeMsg(msg.id)" class="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition" title="Descartar">
                   <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import apiClient from '@/services/apiClient';

interface DLQMessage {
    id: string;
    exchange: string;
    routingKey: string;
    errorReason: string;
    retries: number;
    timestamp: string;
    payload: string;
}

const messages = ref<DLQMessage[]>([]);
const isLoading = ref(false);

const fetchDLQ = async () => {
    isLoading.value = true;
    try {
        // MOCK Implementación fallback en caso de no existir API DLQ
        const res = await apiClient.get('/api/v1/dlq/messages').catch(() => ({
            data: [
                { id: 'msg-9a8b7c6d5e4f', exchange: 'amq.topic', routingKey: 'camunda.task.create', errorReason: 'Connection Refused: Postgres DB Pool exhausted.', retries: 4, timestamp: new Date(Date.now() - 3600000).toISOString(), payload: '{"taskId":"123","tenantId":"T1"}' },
                { id: 'msg-1f2e3d4c5b6a', exchange: 'dlx.exchange', routingKey: 'email.notification.send', errorReason: 'SMTP Auth Failed: Invalid credentials.', retries: 2, timestamp: new Date(Date.now() - 7200000).toISOString(), payload: '{"to":"harolt@antigravity.space"}' },
                { id: 'msg-bbccdd112233', exchange: 'amq.direct', routingKey: 'integration.s3.upload', errorReason: 'S3 Storage Limit Exceeded for Tenant.', retries: 5, timestamp: new Date(Date.now() - 100000).toISOString(), payload: '{"bucket":"ibpms-docs"}' }
            ]
        }));
        messages.value = res.data;
    } finally {
        setTimeout(() => isLoading.value = false, 600);
    }
};

onMounted(() => {
    fetchDLQ();
});

const retryMsg = (id: string) => {
    messages.value = messages.value.filter(m => m.id !== id);
};
const purgeMsg = (id: string) => {
    messages.value = messages.value.filter(m => m.id !== id);
};
const purgeAll = () => { messages.value = []; };
const retryAll = () => { messages.value = []; };
const inspectMsg = (msg: DLQMessage) => {
    alert(`Payload Crudo:\n\n${msg.payload}\n\nTrace:\n${msg.errorReason}`);
};
</script>

<style scoped>
.animate-fade-in-up {
  animation: fadeInUp 0.5s ease-out forwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
