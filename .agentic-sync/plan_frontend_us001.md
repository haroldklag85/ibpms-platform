# Plan de Acción: Frontend Remediation (US-001)

## Arquitectura y Componentes a Intervenir
- `src/views/Workdesk.vue`
- `src/stores/useWorkdeskStore.ts`

## Detalles de Implementación

### 1. Búsqueda Híbrida Reactiva (Gap CA-2)
- Inyectar `<input type="search">` en la barra superior de `Workdesk.vue`.
- Crear función `debouncedSearch` (500ms) usando timeout manual o vue/use.
- Modificar el store `useWorkdeskStore.ts` extendiendo `fetchGlobalInbox(params: { search?: string, delegated?: boolean })` para aceptar la query.

### 2. Re-Factor de Data Grid Universal (Gap CA-3)
- Reemplazar iteradores de UI basados en Tarjetas (flex col) en `Workdesk.vue` por un tag `<table class="min-w-full divide-y divide-gray-200">`.
- Desplegar estrictamente el Header con las 5 columnas requeridas: Nombre (íncl. íconos ⚡/📋), SLA, Estado, Avance, Recurso Asignado.
- Ajustar el renderizado del listado de tareas inyectándolas en el `<tbody>` guardando el Tailwind coporativo.

### 3. Toggle de Delegación de Bandejas (Gap CA-4)
- Configurar un Switch UI o Select "Viendo: Mis Tareas | Tareas de mi Equipo" en la cabecera.
- Mapearlo a una variable de estado y accionar una recarga reactiva de `fetchGlobalInbox` pasando la bandera de delegación.

### 4. SLA Ticking Engine "Vivo" (Gap CA-5)
- Declarar `const currentTick = ref(Date.now())` en `Workdesk.vue`.
- Iniciar un intervalo global en `onMounted`: `timer = setInterval(() => currentTick.value = Date.now(), 60000)`.
- Limpiar el Timer obligatoriamente en `onUnmounted`.
- Modificar la firma de evaluación del SLA visual inyectando `currentTick.value` en las dependencias para refrescar Vue.

### 5. Botón Anti Cherry-Picking (Gap CA-8)
- Adicionar una directiva oculta bajo flag estático `const FEATURE_FORCE_QUEUE = false;`.
- Añadir el respectivo dispatch asilado al presionar "Atender Siguiente Tarea".

## QA / Criterios de Aprobación
Al verificar el código y las correctas validaciones locales vía Linter (`npm run build`), se anunciará al ecosistema para convocar al especialista de QA (Test E2E de Grids).
