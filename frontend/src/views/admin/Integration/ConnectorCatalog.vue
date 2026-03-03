<template>
  <div class="h-full w-full bg-gray-50 flex flex-col pt-4 px-6 gap-6 overflow-y-auto">
    
    <!-- ═══════ Header ═══════ -->
    <header class="flex flex-col md:flex-row justify-between items-start md:items-center shrink-0 gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 flex items-center gap-2">
          🔌 Catálogo de Integraciones (Integration Hub)
        </h1>
        <p class="text-sm text-gray-500 mt-1">Busca, reutiliza o construye conectores hacia el APIM Corporativo y sistemas legados (CA-9).</p>
      </div>
      <div class="flex gap-3">
        <div class="relative">
          <input type="text" v-model="searchQuery" placeholder="Buscar conector..." class="w-64 pl-10 pr-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm focus:ring-indigo-500 focus:border-indigo-500" />
          <svg class="w-4 h-4 text-gray-400 absolute left-3 top-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
        </div>
        <router-link to="/admin/integration/builder" class="bg-indigo-600 text-white px-4 py-2 rounded-md shadow text-sm font-medium hover:bg-indigo-700 transition flex items-center gap-2">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
          Nuevo Conector
        </router-link>
      </div>
    </header>

    <!-- ═══════ Architecture Notice ═══════ -->
    <div class="bg-blue-50 border-l-4 border-blue-500 p-4 rounded-r-md">
      <div class="flex items-start">
        <svg class="w-5 h-5 text-blue-600 mt-0.5 mr-3 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        <div>
          <h3 class="text-sm font-bold text-blue-800">Arquitectura Blueprint V1</h3>
          <p class="text-xs text-blue-700 mt-1 leading-relaxed">
            iBPMS asume separación de deberes: Protocolos SOAP (Legados) y volumetría masiva deben resolverse en el APIM Externo / RabbitMQ. Este Hub es para orquestar la comunicación API REST directa (vía JSON) hacia esos buses, consumiendo PGP para payload encryption (CA-23).
          </p>
        </div>
      </div>
    </div>

    <!-- ═══════ App Store Grid ═══════ -->
    <section class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 pb-8">
      <div v-for="conn in filteredConnectors" :key="conn.id" class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 hover:shadow-md hover:border-indigo-300 transition flex flex-col">
        
        <div class="flex justify-between items-start mb-4">
          <div class="w-12 h-12 rounded-lg flex items-center justify-center text-2xl shadow-inner" :class="conn.bgColor">
            {{ conn.icon }}
          </div>
          <div class="flex flex-col items-end gap-1">
            <span class="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full" :class="conn.status === 'ACTIVO' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'">
              {{ conn.status }}
            </span>
            <span class="text-[10px] font-mono text-gray-400 border border-gray-200 px-1.5 rounded">{{ conn.protocol }}</span>
          </div>
        </div>

        <h3 class="text-lg font-bold text-gray-900">{{ conn.name }}</h3>
        <p class="text-xs text-gray-500 mt-2 flex-1 line-clamp-2">{{ conn.description }}</p>

        <div class="mt-4 pt-4 border-t border-gray-100 flex flex-wrap gap-2 text-[10px] text-gray-500">
          <span v-if="conn.tags.includes('PGP')" class="bg-purple-50 text-purple-700 border border-purple-200 px-2 py-0.5 rounded flex items-center gap-1">🔐 PGP Encrypted</span>
          <span v-if="conn.tags.includes('APIM')" class="bg-blue-50 text-blue-700 border border-blue-200 px-2 py-0.5 rounded flex items-center gap-1">🌐 APIM Proxy</span>
          <span class="bg-gray-100 px-2 py-0.5 rounded">{{ conn.authType }}</span>
        </div>

        <!-- Acciones Hover -->
        <div class="mt-4 flex gap-2">
          <button class="flex-1 bg-gray-50 hover:bg-gray-100 text-gray-700 text-xs font-semibold py-1.5 rounded border border-gray-200 transition">
            Test Ping
          </button>
          <router-link :to="`/admin/integration/builder?id=${conn.id}`" class="flex-1 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 text-xs font-semibold py-1.5 rounded border border-indigo-200 transition text-center">
            Configurar
          </router-link>
        </div>
      </div>
    </section>

  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

const searchQuery = ref('');

interface Connector {
  id: string;
  name: string;
  description: string;
  icon: string;
  bgColor: string;
  status: 'ACTIVO' | 'BORRADOR';
  protocol: 'REST' | 'SOAP-over-REST';
  authType: 'OAuth 2.0' | 'API Key' | 'Basic' | 'HMAC';
  tags: string[];
}

const connectors = ref<Connector[]>([
  {
    id: 'conn_01',
    name: 'SharePoint MS V1',
    description: 'Gestor Documental corporativo. Sube binarios a las librerías del cliente.',
    icon: '📁',
    bgColor: 'bg-blue-50 text-blue-600',
    status: 'ACTIVO',
    protocol: 'REST',
    authType: 'OAuth 2.0',
    tags: ['O365', 'Binary']
  },
  {
    id: 'conn_02',
    name: 'Oracle NetSuite',
    description: 'Bypass a ERP mediante APIM para creación de órdenes de compra.',
    icon: '💰',
    bgColor: 'bg-red-50 text-red-600',
    status: 'ACTIVO',
    protocol: 'SOAP-over-REST',
    authType: 'Basic',
    tags: ['APIM', 'ERP']
  },
  {
    id: 'conn_03',
    name: 'Core Bancario Mainframe',
    description: 'Orquestación de transacciones usando RabbitMQ para absorber picos (CA-23). Enrutado por APIM.',
    icon: '🏦',
    bgColor: 'bg-teal-50 text-teal-600',
    status: 'ACTIVO',
    protocol: 'REST',
    authType: 'HMAC',
    tags: ['APIM', 'PGP', 'RabbitMQ']
  },
  {
    id: 'conn_04',
    name: 'Exchange Mail Sender',
    description: 'Despacho de correos transaccionales desde Camunda Service Tasks.',
    icon: '📧',
    bgColor: 'bg-blue-50 text-blue-600',
    status: 'BORRADOR',
    protocol: 'REST',
    authType: 'OAuth 2.0',
    tags: ['O365']
  }
]);

const filteredConnectors = computed(() => {
  if (!searchQuery.value) return connectors.value;
  return connectors.value.filter(c => 
    c.name.toLowerCase().includes(searchQuery.value.toLowerCase()) || 
    c.description.toLowerCase().includes(searchQuery.value.toLowerCase())
  );
});
</script>
