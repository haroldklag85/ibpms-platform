/**
 * Playwright Component Testing - Bootstrap
 * 
 * Aquí se importan los estilos globales y plugins necesarios
 * para que los componentes Vue se rendericen como en la app real.
 */
import '../src/assets/base.css';
import { beforeMount } from '@playwright/experimental-ct-vue/hooks';
import { createPinia } from 'pinia';

beforeMount(async ({ app }) => {
  const pinia = createPinia();
  app.use(pinia);
});
