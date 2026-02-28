<template>
  <div>
    <h2 class="text-2xl font-bold text-gray-800 mb-6">Portal de Servicios (App Launcher)</h2>
    
    <div class="bg-blue-50 border-l-4 border-ibpms-brand p-4 mb-8">
      <div class="flex">
        <div class="flex-shrink-0">
          <span class="text-xl">👋</span>
        </div>
        <div class="ml-3">
          <p class="text-sm text-blue-700">
            Buenos días. ¿Qué necesitas iniciar hoy? Selecciona un proceso de tu catálogo.
          </p>
        </div>
      </div>
    </div>

    <!-- Módulo de Recursos Humanos -->
    <div class="mb-8">
      <h3 class="text-lg font-semibold text-gray-700 mb-4 border-b pb-2">Recursos Humanos</h3>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        
        <!-- Tarjeta de Proceso -->
        <div class="bg-white rounded-lg shadow-sm border p-6 hover:shadow-md transition">
          <div class="flex items-center mb-4">
            <span class="text-3xl mr-3">🏖️</span>
            <h4 class="text-lg font-bold text-ibpms">Vacaciones</h4>
          </div>
          <p class="text-gray-600 text-sm mb-6">Solicitar días de descanso legales. (Requiere aprobación de jefe directo).</p>
          <button class="w-full bg-gray-100 hover:bg-gray-200 text-ibpms font-medium py-2 px-4 rounded transition">
            🚀 Iniciar Proceso
          </button>
        </div>

        <!-- Tarjeta de Proceso -->
        <div class="bg-white rounded-lg shadow-sm border p-6 hover:shadow-md transition">
          <div class="flex items-center mb-4">
            <span class="text-3xl mr-3">🤝</span>
            <h4 class="text-lg font-bold text-ibpms">Onboarding</h4>
          </div>
          <p class="text-gray-600 text-sm mb-6">Ingreso de un nuevo empleado a la compañía. Activa dotación e IT.</p>
          <button class="w-full bg-gray-100 hover:bg-gray-200 text-ibpms font-medium py-2 px-4 rounded transition">
            🚀 Iniciar Proceso
          </button>
        </div>

      </div>
    </div>

    <!-- Módulo de Compras -->
    <div>
      <h3 class="text-lg font-semibold text-gray-700 mb-4 border-b pb-2">Compras y Legal</h3>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        
        <!-- Tarjeta de Proceso -->
        <div class="bg-white rounded-lg shadow-sm border p-6 hover:shadow-md transition">
          <div class="flex items-center mb-4">
            <span class="text-3xl mr-3">🛒</span>
            <h4 class="text-lg font-bold text-ibpms">Req. de Compra</h4>
          </div>
          <p class="text-gray-600 text-sm mb-6">Compras menores a $5000 USD. Genera orden directamente hacia el ERP.</p>
          <button class="w-full bg-gray-100 hover:bg-gray-200 text-ibpms font-medium py-2 px-4 rounded transition">
            🚀 Iniciar Proceso
          </button>
        </div>

      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
// Esta pantalla en el futuro listará ProcessDefinitions desde el backend
</script>
