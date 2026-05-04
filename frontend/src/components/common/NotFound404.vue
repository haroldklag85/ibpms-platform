<template>
  <div class="h-full w-full flex flex-col items-center justify-center bg-gray-50 text-gray-800 p-6 min-h-screen">
    <div class="text-center max-w-md">
      <div class="text-[120px] leading-none mb-4 font-black text-gray-200">404</div>
      <h1 class="text-2xl font-bold mb-2">{{ t('errors.notFound') }}</h1>
      <p class="text-sm text-gray-500 mb-8 font-medium">
        {{ t('errors.notFoundDesc') }}
      </p>
      <button @click="$router.push('/')" class="bg-indigo-600 text-white font-bold py-3 px-6 rounded-lg hover:bg-indigo-700 shadow-sm transition">
         {{ t('sidebar.home') }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
// CA-3: Security by Obscurity Fallback Screen
</script>
