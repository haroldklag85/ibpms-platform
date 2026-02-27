# 🎨 Handoff Document - Sprint 8: Micro-Frontends Nativos (Single Origin V1)

**Atención: Equipo de Agentes de Desarrollo Frontend (Vue Squad)**
Este documento contiene las especificaciones inmutables (Contrato de Arquitectura) para la ejecución del Sprint 8. Su misión es refactorizar nuestra "Single Page Application" monolítica hacia un ecosistema de **Micro-Frontends (MFE)** basado en Module Federation.

---

## 🎯 Objetivo del Sprint
Desacoplar la aplicación actual (`workspace-spa`) en tres (3) aplicaciones lógicas distintas utilizando el plugin de **Vite Module Federation**:
1.  **`host-app` (Contenedor Principal):** Se encargará del Shell (Navegación, Login, Tema Base) y consumirá los componentes remotos.
2.  **`inbox-remote` (Módulo de Usuario):** Expondrá las vistas de `InboxPage.vue`, `DashboardPage.vue` y `DynamicForm.vue` (Bandeja de operaciones diarias).
3.  **`admin-remote` (Módulo de Configuración TI):** Expondrá únicamente el `ProcessDesignerPage.vue` (Diseñador BPMN/DMN).

## 🏛️ Restricción Arquitectónica (CRÍTICA)
Debido a la estricta cuota pactada de **1 Máquina Virtual de Frontend (IaaS V1)**, está absolutamente PROHIBIDO desplegar estos tres MFEs en puertos distintos en el servidor de Producción, de lo contrario tendríamos problemas de CORS infernales y saturación de NGINX.

*   **Regla "Single Origin":** Aunque lógicamente sean 3 repositorios/carpetas bajo Vite, al momento de hacer `npm run build`, la canalización (CI/CD o local) debe empaquetar los distritos `dist/` resultantes y servirlos **todos en el mismo NGINX bajo el puerto 80/443**.

## 🛠️ Stack Tecnológico Permitido
*   Vue 3 (Composition API) + TypeScript.
*   **Vite 5+**
*   Plugin recomendado: `@originjs/vite-plugin-federation`.

## 📝 Contrato de Configuración (Vite Federation)

Ejemplo Mandatorio para la exposición del Remote (e.g. `inbox-remote/vite.config.ts`):
```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import federation from '@originjs/vite-plugin-federation'

export default defineConfig({
  plugins: [
    vue(),
    federation({
      name: 'inbox_remote',
      filename: 'remoteEntry.js',
      exposes: {
        './InboxPage': './src/views/InboxPage.vue',
        './DashboardPage': './src/views/DashboardPage.vue'
      },
      shared: ['vue', 'vue-router'] // Es vital compartir estas librerías para evitar dobles descargas
    })
  ],
  build: {
    target: 'esnext' // Requisito de Module Federation
  }
})
```

## ✅ Pasos de Acción Inmediata para el Agente Frontend:

1.  Crea una nueva estructura en la ruta base: `frontend/mfe/`.
2.  Dentro de ella, crea las 3 subcarpetas: `host-app`, `inbox-remote`, `admin-remote`.
3.  Mueve el código fuente actual de `workspace-spa/src` y distribúyelo según las responsabilidades asignadas (No escribas código de cero, ¡MUEVE y reconfigura los archivos `vue` que ya creamos!).
4.  Configura el servidor proxy de desarrollo (solo local) para que en tu máquina de desarrollo el `host` corra en `3000`, `inbox` en `3001` y `admin` en `3002`.
5.  Actualiza el enrutador (`router/index.ts`) en el `host-app` para cargar las vistas mediante `import('inbox_remote/InboxPage')`.

¡Procedan a ejecutar el Sprint 8 y reporten el éxito mediante Git Commits garantizando que el Host renderiza todo!
