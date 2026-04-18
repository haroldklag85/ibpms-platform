<template>
  <div class="skeleton-container bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 border border-gray-100 dark:border-gray-700">
    <div class="animate-pulse flex space-x-4">
      <div class="rounded-full bg-slate-200 h-10 w-10"></div>
      <div class="flex-1 space-y-6 py-1">
        <div class="h-2 bg-slate-200 rounded"></div>
        <div class="space-y-3">
          <div class="grid grid-cols-3 gap-4">
            <div class="h-2 bg-slate-200 rounded col-span-2"></div>
            <div class="h-2 bg-slate-200 rounded col-span-1"></div>
          </div>
          <div class="h-2 bg-slate-200 rounded"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// CA-15: Skeleton loader visual representation without logic.
</script>

<style scoped>
.skeleton-container {
  min-height: 120px;
}
</style>
