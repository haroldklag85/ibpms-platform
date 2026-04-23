# 🚨 Reporte de Auditoría: Violaciones Zero-Mock Gate
**Para:** Arquitecto Líder / CISO
**Fecha:** 2026-04-23
**Contexto del Incidente:** El pre-commit hook `anti-mock-scanner.js` ha bloqueado la integración del Sprint 6 hacia el entorno UAT. La política estricta prohíbe datos "mockeados" quemados (`hardcoded`) dentro del directorio `/src/views/` y `/src/components/`, requiriendo que cualquier simulación de datos ocurra estrictamente en la capa de testing (`/src/tests/`) o se consuman endpoints reales del Backend.

## 📊 Inventario de Deuda Técnica (10 Archivos)

> [!WARNING]
> La compilación E2E para la Jornada 4 está bloqueada hasta que estos componentes sean refactorizados para conectarse a sus respectivos `Stores` reales (Pinia) o se inyecten de forma dinámica desde el servidor.

### 1. Módulo Modeler (Diseñadores de Procesos y Formularios)
- **`src/views/admin/Modeler/BpmnDesigner.vue`**
  - **Falla:** `const mockRole = ref(...)` (Línea 734)
  - **Contexto:** Se utilizaba para simular el rol activo del usuario en UI (`BPMN_Release_Manager`) y gobernar la visibilidad de los paneles CA-21/CA-66. Debería consumirse desde el `authStore`.
- **`src/views/admin/Modeler/FormDesigner.vue`**
  - **Falla:** `const mockContext =` (Línea 859)
  - **Contexto:** Variable inyectando datos estáticos al lienzo del formulario para simular la pre-visualización sin un motor de variables dinámicas detrás.
- **`src/views/admin/Modeler/InstancesManager.vue`**
  - **Falla:** `const mockedInstances =` (Línea 105)
  - **Contexto:** Lista de instancias en vuelo quemada en duro para propósitos de renderizado en UI (posiblemente heredada de handoffs tempranos).

### 2. Módulo de Seguridad y RBAC
- **`src/views/admin/RbacManager/GlobalRolesTable.vue`**
  - **Falla:** `const mockUserIds = ['00000000-0000-...', ...]` (Línea 262)
  - **Contexto:** Un comentario de un agente anterior dictamina: `// IDs demo para el demostrador (handoff autoriza payload mock)`. Esto indica que la deuda técnica fue explícitamente autorizada de manera temporal en un sprint pasado, pero ahora rompe la política Zero-Mock para la certificación.

### 3. Service Delivery y Workdesk
- **`src/views/admin/ServiceDelivery/IntakeManual.vue`**
  - **Falla:** `const mockEmails =` (Línea 229)
- **`src/views/public/PublicIntake.vue`**
  - **Falla:** `const mockSubmit =` (Línea 44)
  - **Contexto:** Simula la ejecución 200 OK del backend en el formulario público.
- **`src/views/Workdesk.vue`**
  - **Falla:** `const mockOpenTask =` (Línea 658)
- **`src/views/WorkdeskMockup.vue`**
  - **Falla:** `const mockConnectionState =` (Línea 526) y `const mockOpenTask =` (Línea 693)
  - **Contexto:** Este componente parece ser un prototipo residual completo. Debería evaluarse su eliminación inmediata si el `Workdesk.vue` real ya está operativo.

### 4. Configuración y Componentes Compartidos
- **`src/views/admin/SettingsView.vue`**
  - **Falla:** `const mockUsers =` (Línea 118)
- **`src/components/agile/AssigneeMultiSelect.vue`**
  - **Falla:** `const mockDirectory =` (Línea 72)
  - **Contexto:** Lista local de usuarios del Active Directory simulada para el componente de multi-asignación. Debe reemplazarse por una llamada al `userStore` o Endpoint de Directorio Real.

---

## 🛠️ Opciones de Solución para el Arquitecto

> [!TIP]
> Se recomiendan las siguientes estrategias para solventar la deuda de manera definitiva y permitir el commit:

1. **Refactorización de la Capa de Estado:** Trasladar la obtención de datos (ej. roles y usuarios) hacia los stores globales reales (`useAuthStore`, `useRbacStore`, `useMenuStore`).
2. **Purga de Prototipos Residuales:** Eliminar los archivos puente temporales como `WorkdeskMockup.vue` y limpiar la lógica de "Fallback Local" de los diseñadores BPMN (que silencian errores de red usando variables estáticas).
3. **Factories de Testing a su Capa Correcta:** Trasladar los arrays quemados de UUIDs y registros hacia la carpeta `/frontend/src/tests/` (por ejemplo, en un archivo `factories.ts`), asegurando que en UAT/Producción el frontend obligue a consumir el Backend.
