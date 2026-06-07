# Aprobación de Plan de Implementación FRONTEND - US-030 (Monitoreo BPMN)

## 🎯 Contexto
Implementación de la interfaz de "Monitoreo" (BAM) para administradores, que visualizará el estado de salud del motor BPMN, listando instancias de procesos (activas/completadas) e incidentes (errores técnicos) consultando los endpoints reales en `/api/v1/bpm/telemetry/*`.

## 🧩 Plan de Acción

### 1. Capa de Servicios y Store (Pinia + Axios)
*   **Servicio API (`frontend/src/services/telemetryService.ts`)**: Implementación estricta de las llamadas a `/api/v1/bpm/telemetry/instances` y `/api/v1/bpm/telemetry/incidents`.
*   **Store (`frontend/src/stores/useTelemetryStore.ts`)**: Gestión del estado global de las métricas (`instances`, `incidents`, `isLoading`, `error`). Implementación de Graceful Degradation capturando errores HTTP 404/500 y prohibiendo rotundamente interceptores como `mockAdapter.ts`.

### 2. Capa Visual (Componentes Vue 3 + Tailwind CSS)
*   **Vista Principal (`frontend/src/views/monitoring/BpmDashboard.vue`)**: Layout centralizado para alojar las métricas y componentes del BAM, asegurando una estética profesional, responsiva y ordenada con Tailwind.
*   **Tabla de Instancias (`frontend/src/components/monitoring/InstanceTable.vue`)**: Tabla para listar los procesos, integrando formato amigable de fechas y filtrado visual por su estado.
*   **Panel de Incidentes (`frontend/src/components/monitoring/IncidentPanel.vue`)**: Interfaz para visualizar el registro de errores y métricas anómalas.

### 3. Rutas y Navegación
*   Agregar la ruta `/monitoring` en `frontend/src/router/index.ts` para exponer el dashboard a los usuarios con rol de administrador.

### 4. Flujo de Certificación SRE (Zero-Trust)
*   Al concluir, se ejecutará obligatoriamente `npm run build` en el directorio frontend.
*   No se entregará el código hasta garantizar un "Build Successful" libre de errores de TypeScript, Plantillas o Imports.
*   Finalmente, actualización de `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md`.
