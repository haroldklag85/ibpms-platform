<template>
  <div class="h-full flex flex-col bg-background p-6">
    <header class="mb-6 flex items-center justify-between">
      <h1 class="text-2xl font-bold text-text">Bandeja de Casos (Docketing)</h1>
      <nav class="flex space-x-4">
        <router-link to="/designer" class="text-primary hover:underline">Diseñador BPMN</router-link>
        <router-link to="/dashboard" class="text-primary hover:underline">Dashboard BAM</router-link>
      </nav>
    </header>

    <div class="flex-1 overflow-auto rounded-lg border border-border bg-surface p-4 shadow">
      <!-- Tabla/Kanban de casos -->
      <table class="w-full text-left text-sm text-text">
        <thead class="bg-gray-50 uppercase text-text-muted border-b border-border">
          <tr>
            <th class="px-6 py-3 font-medium">Radicado</th>
            <th class="px-6 py-3 font-medium">Proceso</th>
            <th class="px-6 py-3 font-medium">Estado / SLA</th>
            <th class="px-6 py-3 font-medium">Acción</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-border">
          <tr class="hover:bg-gray-50 transition-colors">
            <td class="px-6 py-4 font-semibold text-primary">RAD-2024-001</td>
            <td class="px-6 py-4">Revisión de Demanda Laboral</td>
            <td class="px-6 py-4"><span class="rounded-full bg-warning/20 px-2 py-1 text-xs font-bold text-warning">Vence Hoy</span></td>
            <td class="px-6 py-4 text-primary cursor-pointer">Ver Expediente</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
// Lógica base de Inbox (Recuperada para V1 SPA)
</script>
