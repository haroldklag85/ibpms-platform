# Handoff Técnico: Sprint 5 - Iteración 1 (Frontend & QA Context)

## 📌 Metadatos del Handoff
- **Agente Destino:** Desarrollador Frontend (Vue.js/Pinia) y Agente QA.
- **Autor:** Arquitecto Líder (Antigravity)
- **Riesgo:** Medio (Alineación de Contratos y preparativos de pruebas).

## 🎯 Objetivo de la Iteración (Frontend & QA Parallelization)
Mientras el Backend asegura los bloqueos de persistencia y parchea el motor Camunda, los agentes Frontend y QA tienen prohibido quedarse inactivos. Deben estructurar las bases de pruebas (Testcontainers) y los esqueletos del estado local del navegador (Pinia/API Client) para no ser un cuello de botella en la Iteración 3.

## 🛠️ Acciones Tácticas Requeridas - FRONTEND

### 1. Mapeo de Contratos API (US-002, US-029 y US-007)
**Contexto:** El Backend está construyendo endpoints restrictivos.
- **Acción:** Definir en `apiClient.ts` los stubs para:
  - `POST /api/v1/workbox/tasks/{id}/claim`
  - `POST /api/v1/workbox/tasks/{id}/complete`
  - `PUT /api/v1/workbox/tasks/{id}/draft`
  - `POST /api/v1/dmn/generate` (Endpoint IA)
- **Mock First:** Habilitar MSW (Mock Service Worker) o mocks de Axios para simular respuestas 403 (Implicit Locking) de modo que se puedan testear comportamientos de UI paralelamente.

### 2. Estructura de Stores (Pinia)
**Contexto:** Preparación para US-025 y US-002.
- **Acción:** Asegurar que `authStore.ts` esté inyectando `ActiveRole` y que `agileStore.ts` tenga las mutaciones para manejar la desaparición instantánea de tareas (Preparación WebSockets).

## 🛠️ Acciones Tácticas Requeridas - QA / AUTOMATION

### 1. Reforzar el Ecosistema Playwright
**Contexto:** Evitar los falsos positivos cuando entre la UI.
- **Acción:** Establecer las pre-condiciones en la Suite E2E de Playwright para inyectar *Test Users* concurrentes (Ej. Operador A y Operador B) en navegadores Incógnitos separados, preparando el terreno para probar la concurrencia del US-002 CA-11.

### 2. Saneamiento de Vitest
**Contexto:** Las pruebas fallidas del Sprint anterior por "Pinia no inicializado".
- **Acción:** Aplicar `createTestingPinia()` globalmente en los setups del Frontend para que la próxima iteración pueda probar los componentes Vue aisladamente.

---

## 🛑 Condición para Cierre de Iteración
El Agente Frontend debe poder correr `npm run test:unit` sin colapsos de Store. El Agente QA debe certificar que los archivos base de E2E están creados y lógicamente listos para apuntar a localhost en cuanto el desarrollo UI (Iteración 3) comience.
