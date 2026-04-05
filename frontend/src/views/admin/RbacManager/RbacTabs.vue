<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 flex flex-col overflow-hidden">
    
    <!-- Cabecera de Pestañas -->
    <div class="border-b border-gray-200 bg-gray-50 flex items-center px-4 space-x-6">
      <button 
        @click="activeTab = 'global'" 
        class="py-3 text-sm font-medium border-b-2 transition-colors duration-150 flex items-center gap-2"
        :class="activeTab === 'global' ? 'border-indigo-600 text-indigo-700' : 'border-transparent text-gray-500 hover:text-gray-700'"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg>
        Roles Globales de TI (Sistemas)
        <span class="bg-gray-100 text-gray-600 py-0.5 px-2 rounded-full text-xs border border-gray-200">{{ globalCount }}</span>
      </button>

      <button 
        @click="activeTab = 'process'" 
        class="py-3 text-sm font-medium border-b-2 transition-colors duration-150 flex items-center gap-2"
        :class="activeTab === 'process' ? 'border-amber-500 text-amber-700' : 'border-transparent text-gray-500 hover:text-gray-700'"
      >
        <svg class="w-4 h-4 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
        Roles Autogenerados (BPMN / Motores)
        <span class="bg-emerald-50 text-emerald-700 py-0.5 px-2 rounded-full text-xs border border-emerald-200">{{ processCount }}</span>
      </button>

      <button 
        @click="activeTab = 'service-accounts'" 
        class="py-3 text-sm font-medium border-b-2 transition-colors duration-150 flex items-center gap-2"
        :class="activeTab === 'service-accounts' ? 'border-purple-600 text-purple-700' : 'border-transparent text-gray-500 hover:text-gray-700'"
      >
        <svg class="w-4 h-4 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"></path></svg>
        Service Accounts (API Keys)
      </button>
    </div>

    <!-- Contenedor Dinámico -->
    <div class="flex-1 overflow-auto p-4 bg-gray-50/30">
      
      <!-- Listado Global -->
      <GlobalRolesTable v-if="activeTab === 'global'" />

      <!-- Listado de Procesos -->
      <ProcessRolesTable v-if="activeTab === 'process'" />

      <!-- Listado de API Keys -->
      <ServiceAccountsTable v-if="activeTab === 'service-accounts'" />

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRbacStore } from '@/stores/rbacStore'
import GlobalRolesTable from './GlobalRolesTable.vue'
import ProcessRolesTable from './ProcessRolesTable.vue'
import ServiceAccountsTable from './ServiceAccountsTable.vue'

const store = useRbacStore()
const activeTab = ref('global')

const globalCount = computed(() => store.globalRoles.length)
const processCount = computed(() => store.processRoles.length)
</script>
