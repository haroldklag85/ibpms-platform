---
author: Lead Architect
target: Frontend Developer Agent
epic: US-001 Hybrid Workdesk
status: OPEN
---

# Hand-off Técnico: Frontend Remediation (US-001)

Este documento contiene las especificaciones técnicas (Engineering Grade) para cerrar los Gaps encontrados en la UI del Workdesk Híbrido. Se requiere un enfoque **Strict Vertical Slice** (Store -> UI). No des por terminada esta tarea sin que el código compile y el linter no arroje errores (`npm run build`).

### Misiones y Criterios de Aceptación (A Desarrollar)

#### 1. Búsqueda Híbrida Reactiva (Gap CA-2)
- **Modificar:** `src/views/Workdesk.vue` y `src/stores/useWorkdeskStore.ts`
- **Comportamiento Esperado:**
  - Agregar un `<input type="search">` en la cabecera.
  - Implementar un debouncer (ejs. 500ms) para evitar saturar el backend.
  - El Store debe actualizar su llamada `fetchGlobalInbox` para incluir el parámetro `search=X`.

#### 2. Re-Factor de Data Grid Universal (Gap CA-3)
- **Modificar:** `src/views/Workdesk.vue`
- **Comportamiento Esperado:**
  - Reemplazar las actuales "Cards" (`<div ... flex flex-col sm:flex-row>`) por una **Tabla `<table class="min-w-full divide-y divide-gray-200">`**.
  - Garantizar **estrictamente 5 columnas**: `[Nombre (Incluye Iconos ⚡/📋), SLA, Estado, Avance, Recurso Asignado]`.
  - Debe retener los colores de Tailwind corporativo.

#### 3. Toggle de Delegación de Bandejas (Gap CA-4)
- **Modificar:** `Workdesk.vue`
- **Comportamiento Esperado:**
  - Insertar un Dropdown o Switch en la cabecera que diga "Viendo: Mis Tareas | Tareas de mi Equipo".
  - Al cambiar, debe invocar al Store pasando la orden de alterar el contexto (`fetchGlobalInbox` con propiedad `delegatedToId`).

#### 4. SLA Ticking Engine "Vivo" (Gap CA-5)
- **Modificar:** `Workdesk.vue`
- **Comportamiento Esperado:**
  - Actualmente, `getSlaRelativeTime(Date)` se calcula estáticamente al cargar la tarjeta.
  - Implementar un ref `currentTick` global inicializado con `Date.now()`.
  - Crear un `setInterval` en un hook `onMounted` que actualice `currentTick` cada 60.000ms (1 minuto). Obliga a usar `onUnmounted` para purgar el timer de memoria.
  - Volver reactivas las macros `getSlaRelativeTime` inyectando `currentTick.value` para forzar a Vue a re-pintar de Verde a Amarillo a Rojo dinámicamente sin F5.
  
#### 5. Opcional (Botón forzoso Anti Cherry-Picking) (Gap CA-8)
- Crear un botón gigante "Atender Siguiente Tarea" oculto bajo una constante de Feature Toggle `FEATURE_FORCE_QUEUE=false`.

*Al confirmar la terminación, debes correr los linters y notificar al Agente de QA para que monte los tests end-to-end de los grids.*
