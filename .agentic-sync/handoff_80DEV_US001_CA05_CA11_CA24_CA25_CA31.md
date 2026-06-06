# 📋 Handoff Arquitectónico — Iteración 80-DEV (US-001)
> **Fecha:** 2026-04-13 | **Arquitecto Líder:** Agente Antigravity  
> **Rama:** `sprint-3/informe_auditoriaSprint1y2`  
> **Protocolo aplicado:** `.agents/skills/architect_handoff_protocol/SKILL.md` v1.0.0

---

## 1. Metadatos y SSOT (Single Source of Truth)

| Campo | Valor |
|-------|-------|
| **Iteración** | 80-DEV |
| **Sprint** | Sprint 3 |
| **Rama Git** | `sprint-3/informe_auditoriaSprint1y2` |
| **User Story** | US-001 — Obtener Tareas Pendientes en el Workdesk |
| **Archivo SSOT** | `docs/requirements/epics/epic_A_motor_core.md` (líneas 86–351) |
| **CAs a desarrollar** | CA-05, CA-11, CA-24, CA-25, CA-31 |
| **CAs excluidos (V2)** | Interruptor `[Mute]` de CA-11 (notificaciones sonoras Push), Personalización de umbrales por Tenant de CA-24 (US-036 dependencia) |
| **Flujo de ejecución** | **Backend → Frontend → QA (Secuencial estricto)** |
| **Razón de secuencialidad** | El Backend NO requiere cambios funcionales en esta iteración (SLA es cálculo 100% Frontend), pero debe verificar que `slaExpirationDate` ya está presente en el DTO. Frontend implementa el Heartbeat Engine y los semáforos. QA certifica ambas capas. |

---

## 2. Alineación Arquitectónica y ADRs

### ADRs que rigen esta iteración

| ADR / Restricción | Impacto en 80-DEV |
|--------------------|-------------------|
| **Anti DOM-Thrashing (CA-11)** | PROHIBIDO crear `setInterval` por cada tarjeta. Se usará un ÚNICO `requestAnimationFrame` global (Global Heartbeat Store) ya existente en `timeStore.ts`. |
| **CQRS Read Model** | El campo `slaExpirationDate` ya existe en `WorkdeskProjectionEntity.java` (L20) y se retorna al Frontend en el DTO `WorkdeskGlobalItemDTO`. No se requiere migración SQL. |
| **Tenant Isolation (CA-14)** | Los umbrales de CA-24 se implementarán como constantes estáticas del Frontend (V1). La personalización por tenant se difiere a V2 (dependencia US-036). |
| **Zero-Trust SRE** | Compilación Backend vía `.agents/skills/backend_sre_compilation_audit/SKILL.md`. |
| **Zero-Trust UI** | Build Frontend vía `.agents/skills/frontend_build_audit/SKILL.md`. |

### Trazabilidad de la Solución

Esta iteración es **100% Frontend-dominante**. El Backend ya entrega `slaExpirationDate` como campo ISO 8601 en el DTO sanitizado (verificado en iteraciones 76-DEV/77-DEV). La lógica del semáforo SLA se calcula localmente usando `Date.now()` vs `slaExpirationDate`, implementando un `requestAnimationFrame` loop global. El `timeStore.ts` ya existe con `startEngine()` y `stopEngine()`, pero NO está conectado a `Workdesk.vue` — actualmente usa un `setInterval` de 60s (L476). **Esta iteración reemplaza el `setInterval` por el Heartbeat Store basado en rAF.**

---

## 3. Rutas Exactas y Contexto Preexistente

### 3.1 Backend — Archivos a verificar (SIN cambios funcionales)

| Archivo | Ruta relativa | Estado actual |
|---------|---------------|---------------|
| **WorkdeskGlobalItemDTO.java** | `backend/.../application/dto/WorkdeskGlobalItemDTO.java` | ✅ Ya contiene campo `slaExpirationDate` (ISO 8601). **No requiere cambios.** |
| **WorkdeskProjectionEntity.java** | `backend/.../infrastructure/jpa/entity/WorkdeskProjectionEntity.java` | ✅ Campo `sla_expiration_date` presente (L20). **No requiere cambios.** |
| **WorkdeskProjectionRepository.java** | `backend/.../infrastructure/jpa/repository/WorkdeskProjectionRepository.java` | ✅ Query retorna `sla_expiration_date`. **No requiere cambios.** |

### 3.2 Frontend — Archivos a modificar

| Archivo | Ruta relativa | Estado actual |
|---------|---------------|---------------|
| **timeStore.ts** | `frontend/src/stores/timeStore.ts` | ✅ Existe (31 líneas). Ya tiene `requestAnimationFrame` loop con `currentTick` ref y `startEngine()`/`stopEngine()`. **Falta:** listener de `visibilitychange` (CA-25), throttle de actualización para evitar renders excesivos. |
| **Workdesk.vue** | `frontend/src/views/Workdesk.vue` | ✅ Existe (558 líneas). **Problemas actuales:** (1) Usa `setInterval(60000)` en L476 en vez de rAF del timeStore (CA-11); (2) `getSlaStatus()` usa umbrales hardcoded de 24 horas en vez de porcentajes (CA-24); (3) No tiene recálculo en `visibilitychange` (CA-25); (4) No tiene auto-refresco pasivo (CA-31). |
| **useWorkdeskStore.ts** | `frontend/src/stores/useWorkdeskStore.ts` | ✅ Existe (222 líneas). **No requiere cambios directos** para esta iteración. Ya tiene `fetchGlobalInbox()`. |

### 3.3 QA — Archivos a modificar/crear

| Archivo | Ruta relativa | Estado actual |
|---------|---------------|---------------|
| **WorkdeskRepositoryTest.java** | `backend/.../test/.../repository/WorkdeskRepositoryTest.java` | ✅ Existe (361 líneas, 9 tests). Extender con test de `slaExpirationDate` presente en la query nativa. |
| **useWorkdeskStore.spec.ts** | `frontend/src/tests/useWorkdeskStore.spec.ts` | ✅ Existe (131 líneas, 5 tests de 79-DEV). Extender con tests del Heartbeat, umbrales y auto-refresco. |

---

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

### 4.1 Frontend — Heartbeat Store mejorado (CA-05, CA-11, CA-25): `timeStore.ts` [MODIFICAR]

**Archivo actual** (31 líneas con rAF básico) debe reemplazarse por:

```typescript
import { defineStore } from 'pinia';
import { ref, readonly } from 'vue';

/**
 * CA-05: SLA Ticking Engine Vivo (requestAnimationFrame global)
 * CA-11: Anti DOM-Thrashing — un solo heartbeat, TODAS las tarjetas heredan pasivamente.
 * CA-25: Recálculo inmediato al volver de pestaña inactiva (visibilitychange).
 */
export const useTimeStore = defineStore('timeStore', () => {
    const currentTick = ref(Date.now());
    let animationFrameId: number | null = null;
    let isActive = false;
    let lastUpdateTime = 0;

    // CA-11: Throttle de actualización — re-calcular cada 1 segundo, no cada frame
    const TICK_INTERVAL_MS = 1000;

    const tick = () => {
        const now = Date.now();
        // Solo actualizar el ref reactivo cada 1s para evitar DOM-thrashing
        if (now - lastUpdateTime >= TICK_INTERVAL_MS) {
            currentTick.value = now;
            lastUpdateTime = now;
        }
        if (isActive) {
            animationFrameId = requestAnimationFrame(tick);
        }
    };

    const startEngine = () => {
        if (isActive) return;
        isActive = true;
        lastUpdateTime = Date.now();
        currentTick.value = Date.now();
        animationFrameId = requestAnimationFrame(tick);

        // CA-25: Listener de visibilitychange para recálculo inmediato
        document.addEventListener('visibilitychange', _onVisibilityChange);
    };

    const stopEngine = () => {
        isActive = false;
        if (animationFrameId !== null) {
            cancelAnimationFrame(animationFrameId);
            animationFrameId = null;
        }
        document.removeEventListener('visibilitychange', _onVisibilityChange);
    };

    // CA-25: Al volver de tab inactiva, recálculo INMEDIATO
    const _onVisibilityChange = () => {
        if (document.visibilityState === 'visible') {
            currentTick.value = Date.now();
            lastUpdateTime = Date.now();
        }
    };

    // CA-25: Exponer el tiempo de inactividad al Workdesk para CA-31
    const getInactivityMs = (): number => {
        return Date.now() - lastUpdateTime;
    };

    return {
        currentTick: readonly(currentTick),
        startEngine,
        stopEngine,
        getInactivityMs
    };
});
```

### 4.2 Frontend — Workdesk.vue: Semáforo SLA con Umbrales (CA-24) y Auto-Refresco (CA-31)

**A. Reemplazar `getSlaStatus()` L433-441 por cálculo basado en porcentaje:**

```typescript
// CA-24: Umbrales deterministas basados en % del tiempo restante
// Estos son los DEFAULT del sistema V1. La personalización por tenant se difiere a V2.
const SLA_THRESHOLDS = {
    GREEN_ABOVE: 0.50,   // > 50% restante → Verde
    YELLOW_ABOVE: 0.15,  // > 15% restante → Amarillo
    // < 15% → Rojo
    // 0% → Vencida
};

const getSlaStatus = (isoString?: string): 'OK' | 'WARNING' | 'EXPIRED' | 'CRITICAL' => {
    if (!isoString) return 'OK'; // Sin SLA = no hay presión

    const deadline = new Date(isoString).getTime();
    const now = timeStore.currentTick; // CA-05/CA-11: Reactivo vía Heartbeat Store
    const diff = deadline - now;

    if (diff <= 0) return 'EXPIRED'; // ⚫ Vencida (0%)

    // CA-24: Necesitamos el "total del SLA" para calcular el porcentaje.
    // Como el DTO no incluye sla_start_date, usamos heurística:
    // si quedan < 15% del plazo estimado → Rojo, etc.
    // Aproximación V1: usar 48hrs como ventana base de referencia
    const totalSlaWindow = 48 * 60 * 60 * 1000; // 48h ventana base V1
    const percentRemaining = Math.min(diff / totalSlaWindow, 1.0);

    if (percentRemaining > SLA_THRESHOLDS.GREEN_ABOVE) return 'OK';        // 🟢
    if (percentRemaining > SLA_THRESHOLDS.YELLOW_ABOVE) return 'WARNING';   // 🟡
    return 'CRITICAL'; // 🔴
};
```

**B. Actualizar `getSlaPillClass()` L443-448 para incluir iconografía CA-11:**

```typescript
const getSlaPillClass = (isoString?: string) => {
    const st = getSlaStatus(isoString);
    if (st === 'EXPIRED') return 'bg-gray-200 text-gray-700 border-gray-300';         // ⚫
    if (st === 'CRITICAL') return 'bg-red-50 text-red-700 border-red-200/60';         // 🔴
    if (st === 'WARNING') return 'bg-yellow-50 text-yellow-700 border-yellow-200/60'; // 🟡
    return 'bg-emerald-50 text-emerald-700 border-emerald-200/60';                    // 🟢
};

// CA-11: Iconografía accesible para daltónicos (SVG inline)
const getSlaIcon = (isoString?: string): string => {
    const st = getSlaStatus(isoString);
    if (st === 'EXPIRED') return '⚫';  // Vencida
    if (st === 'CRITICAL') return '⚡'; // Rojo (Urgente)
    if (st === 'WARNING') return '⏳';  // Amarillo (Por vencer)
    return '✔️';                         // Verde (Al día)
};
```

**C. Reemplazar `setInterval` del `onMounted` L476-478 por Heartbeat Store + CA-31:**

```typescript
import { useTimeStore } from '@/stores/timeStore';

const timeStore = useTimeStore();

// CA-31: Umbral de inactividad para auto-refresco (5 minutos)
const INACTIVITY_THRESHOLD_MS = 5 * 60 * 1000;
let visibilityCleanup: (() => void) | null = null;

onMounted(() => {
    loadData();

    // CA-05/CA-11: Arrancar Heartbeat Store en vez de setInterval
    timeStore.startEngine();

    // CA-31: Listener de visibilitychange para auto-refresco pasivo
    const onVisibilityReturn = async () => {
        if (document.visibilityState === 'visible') {
            // CA-25: El timeStore ya recalcula `currentTick` inmediatamente

            // CA-31: Si inactividad > 5 min → refresco silencioso de datos
            if (timeStore.getInactivityMs() > INACTIVITY_THRESHOLD_MS) {
                await loadData(); // Refresco silencioso, mantiene filtros
            }
        }
    };
    document.addEventListener('visibilitychange', onVisibilityReturn);
    visibilityCleanup = () => document.removeEventListener('visibilitychange', onVisibilityReturn);

    // CA-6: Iniciar conexión WebSocket (Ghost Deletion)
    store.initWebSocket();
});

onUnmounted(() => {
    // CA-11: Detener Heartbeat Engine
    timeStore.stopEngine();

    // CA-31: Limpiar listener
    if (visibilityCleanup) visibilityCleanup();

    if (searchTimeout) clearTimeout(searchTimeout);
    store.disconnectWebSocket();
});
```

**D. Actualizar la Col 2 (SLA) del template para incluir icono accesible CA-11 (L222-227):**

```html
<!-- Col 2: SLA Semáforo Vivo con Iconografía Accesible (CA-11) -->
<td class="px-4 py-3">
  <span :class="['px-2 py-1 rounded text-[10px] font-bold uppercase tracking-wider border flex items-center gap-1 w-fit', getSlaPillClass(task.slaExpirationDate)]">
    <span class="text-xs">{{ getSlaIcon(task.slaExpirationDate) }}</span>
    {{ getSlaRelativeTime(task.slaExpirationDate) }}
  </span>
</td>
```

**E. Eliminar las líneas del `setInterval` viejo (L430-431, L476-478, L487):**
- Eliminar: `const currentTick = ref(Date.now());`
- Eliminar: `let timerId: ReturnType<typeof setInterval> | null = null;`
- Eliminar: `timerId = setInterval(..., 60000);`
- Eliminar: `if(timerId) clearInterval(timerId);`
- Reemplazar todas las referencias a `currentTick.value` por `timeStore.currentTick`

---

## 5. Matriz de QA y Testing Atómico

### Tests del Repository Data Layer (JUnit / @SpringBootTest)

**Archivo destino:** `backend/.../test/.../repository/WorkdeskRepositoryTest.java` (extender)

| # | Test Name | CA Evaluado | Aserción Esperada |
|---|-----------|-------------|-------------------|
| 10 | `testSlaExpirationDate_PresentInQuery` | CA-05/CA-24 | Insertar tarea con `slaExpirationDate` futuro, verificar que el campo NO es null en el resultado de `findWorkdeskTasks()`. |
| 11 | `testSlaExpirationDate_NullHandling` | CA-05 | Insertar tarea SIN `slaExpirationDate`. Verificar que el campo es null en el resultado (el Frontend renderizará "Sin SLA"). |

### Tests del Frontend Store/Component (Vitest)

**Archivo destino:** `frontend/src/tests/useWorkdeskStore.spec.ts` (extender)

| # | Test Name | CA Evaluado | Aserción Esperada |
|---|-----------|-------------|-------------------|
| 15 | `heartbeat_uses_rAF_not_setInterval` | CA-11 | Verificar que `timeStore.startEngine()` invoca `requestAnimationFrame` y NO `setInterval`. |
| 16 | `sla_thresholds_green_above_50_percent` | CA-24 | Con `slaExpirationDate` = now + 36h (75% de 48h), `getSlaStatus()` retorna `'OK'`. |
| 17 | `sla_thresholds_yellow_between_15_50` | CA-24 | Con `slaExpirationDate` = now + 12h (25% de 48h), `getSlaStatus()` retorna `'WARNING'`. |
| 18 | `sla_thresholds_red_below_15` | CA-24 | Con `slaExpirationDate` = now + 4h (8.3% de 48h), `getSlaStatus()` retorna `'CRITICAL'`. |
| 19 | `sla_thresholds_expired_past_deadline` | CA-24 | Con `slaExpirationDate` = now - 1h, `getSlaStatus()` retorna `'EXPIRED'`. |
| 20 | `visibilitychange_recalculates_tick` | CA-25 | Simular `document.dispatchEvent(new Event('visibilitychange'))` con `visibilityState = 'visible'`. Verificar que `currentTick` se actualiza inmediatamente. |
| 21 | `auto_refresh_after_5min_inactivity` | CA-31 | Simular inactividad > 5 min. Disparar `visibilitychange`. Verificar que `fetchGlobalInbox` se invoca con los filtros actuales. |

---

## 6. Mensajes de Despacho (Copy & Paste para el Humano)

### 🔄 Orden de ejecución: SECUENCIAL ESTRICTO
1. **Primero:** Backend (verificación + test de perimetraje)
2. **Segundo:** Frontend (Heartbeat Engine + Semáforos + Auto-refresco)
3. **Tercero:** QA (certificación cruzada Backend + Frontend)

---

### 1️⃣ Para el Agente Backend:

```text
Actúa como Agente Backend.
Iniciamos la Iteración 80-DEV de la US-001 en la rama `sprint-3/informe_auditoriaSprint1y2`.

OBJETIVO: Verificar que la infraestructura de datos soporta los cálculos de SLA del Frontend y agregar tests de perimetraje.

ALCANCE (CAs):
- CA-05: Verificar que `slaExpirationDate` está presente en el DTO de respuesta de la grilla.
- CA-24: Verificar que tareas sin SLA retornan `null` (no un valor por defecto).
- NO HAY CAMBIOS FUNCIONALES EN BACKEND para esta iteración. Solo verificación y testing.

CONTEXTO PREEXISTENTE (lee estos archivos ANTES de codificar):
- `backend/.../application/dto/WorkdeskGlobalItemDTO.java` — Campo `slaExpirationDate` ya presente.
- `backend/.../infrastructure/jpa/entity/WorkdeskProjectionEntity.java` — Campo `sla_expiration_date` (L20).
- `backend/.../test/.../repository/WorkdeskRepositoryTest.java` — Ya tiene 9 tests. DEBES extender.

TAREAS PRESCRIPTIVAS:
1. VERIFICAR que `WorkdeskGlobalItemDTO.java` tiene campo `slaExpirationDate` de tipo `LocalDateTime` o `String` ISO 8601.
2. AGREGAR TEST 10 `testSlaExpirationDate_PresentInQuery`: Insertar tarea con SLA futuro, verificar campo no-null en resultado.
3. AGREGAR TEST 11 `testSlaExpirationDate_NullHandling`: Insertar tarea sin SLA, verificar campo null en resultado.

Los snippets exactos están en `.agentic-sync/handoff_80DEV_US001_CA05_CA11_CA24_CA25_CA31.md` sección 5.

Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en .agents/skills/backend_sre_compilation_audit/SKILL.md (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.
Actualiza `.agentic-sync/coverage_matrix.md` marcando CA-05 en columna Back como ✅.
Haz commit y push en la rama `sprint-3/informe_auditoriaSprint1y2`.
```

---

### 2️⃣ Para el Agente Frontend (solo tras commit del Backend):

```text
Actúa como Agente Frontend.
Continuamos la Iteración 80-DEV de la US-001 en la rama `sprint-3/informe_auditoriaSprint1y2`.

OBJETIVO: Implementar el SLA Ticking Engine vivo, semáforos con umbrales, recálculo en tab inactiva y auto-refresco pasivo.

ALCANCE (CAs):
- CA-05: Reemplazar `setInterval(60000)` por `requestAnimationFrame` global via `timeStore.ts`. Los semáforos SLA se actualizan en tiempo real sin F5.
- CA-11: Anti DOM-Thrashing — UN solo heartbeat global. Iconografía accesible para daltónicos (⚡🔴, ⏳🟡, ✔️🟢). Throttle de 1 segundo entre ticks.
- CA-24: Umbrales de color basados en % restante: >50% = Verde, 15-50% = Amarillo, <15% = Rojo, 0% = Vencida(Gris). Constantes estáticas V1.
- CA-25: Listener `visibilitychange` en el `timeStore`. Al volver de tab inactiva, recálculo INMEDIATO de `currentTick`.
- CA-31: Si inactividad > 5 minutos, refresco silencioso de datos vía `fetchGlobalInbox()` SIN destruir filtros activos.
- Exclusiones V2: Interruptor [Mute] de CA-11 (requiere infraestructura Push), Personalización de umbrales por Tenant de CA-24 (US-036).

CONTEXTO PREEXISTENTE (lee estos archivos ANTES de codificar):
- `frontend/src/stores/timeStore.ts` — Ya tiene rAF básico (31 líneas). DEBES extender con visibilitychange y throttle.
- `frontend/src/views/Workdesk.vue` — 558 líneas. Usa `setInterval(60000)` en L476. `getSlaStatus()` en L433. DEBES reemplazar ambos.
- `frontend/src/stores/useWorkdeskStore.ts` — NO requiere cambios.

TAREAS PRESCRIPTIVAS:
1. MODIFICAR `timeStore.ts`: Agregar throttle de 1s al loop rAF, listener `visibilitychange` que recalcula `currentTick` inmediato, y método `getInactivityMs()`.
2. MODIFICAR `Workdesk.vue`:
   a. Importar `useTimeStore` y eliminar `currentTick` ref local (L430) y `timerId` (L431).
   b. REEMPLAZAR `getSlaStatus()` (L433-441) con cálculo basado en umbrales porcentuales (CA-24).
   c. AGREGAR función `getSlaIcon()` que retorne iconos accesibles (⚡, ⏳, ✔️) para daltónicos (CA-11).
   d. REEMPLAZAR `getSlaPillClass()` (L443-448) para incluir estado `CRITICAL` (rojo) y `EXPIRED` (gris/negro).
   e. Actualizar la columna SLA del template (L222-227) para mostrar icono inline junto al texto.
   f. REEMPLAZAR el `setInterval` del `onMounted` (L476-478) por `timeStore.startEngine()`.
   g. AGREGAR listener `visibilitychange` en `onMounted` para CA-31 (auto-refresco silencioso tras 5 min de inactividad).
   h. REEMPLAZAR `onUnmounted` para llamar `timeStore.stopEngine()` y limpiar el listener de visibilitychange.
   i. Cambiar todas las refs a `currentTick.value` por `timeStore.currentTick`.

Los snippets exactos están en `.agentic-sync/handoff_80DEV_US001_CA05_CA11_CA24_CA25_CA31.md` sección 4.1 a 4.5.

Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en .agents/skills/frontend_build_audit/SKILL.md.
Actualiza `.agentic-sync/coverage_matrix.md` marcando CA-05, CA-11, CA-24, CA-25, CA-31 en columna Front como ✅.
Haz commit y push en la rama `sprint-3/informe_auditoriaSprint1y2`.
```

---

### 3️⃣ Para el Agente QA (solo tras commit del Frontend):

```text
Actúa como Agente QA Especialista.
Cerramos la Iteración 80-DEV de la US-001 en la rama `sprint-3/informe_auditoriaSprint1y2`.

OBJETIVO: Certificar el Heartbeat Engine, los umbrales de semáforo SLA y el auto-refresco pasivo.

NFR / QA STRATEGY: Pruebas unitarias al Repository Data Layer + Vitest para la lógica de semáforos.

CONTEXTO PREEXISTENTE:
- `backend/.../test/.../repository/WorkdeskRepositoryTest.java` — Ya tiene 11 tests (Backend ya agregó TEST 10 y 11 en su paso). VERIFICA que existen.
- `frontend/src/tests/useWorkdeskStore.spec.ts` — Ya tiene 5 tests de 79-DEV. DEBES extender con 7 tests adicionales.

TAREAS PRESCRIPTIVAS (Frontend / Vitest):
1. EXTENDER `frontend/src/tests/useWorkdeskStore.spec.ts` con los siguientes tests:
   - Test 15: `heartbeat_uses_rAF_not_setInterval` (CA-11) — Verificar que `timeStore.startEngine()` registra `requestAnimationFrame`.
   - Test 16: `sla_thresholds_green_above_50_percent` (CA-24) — SLA en 75% restante → `'OK'`.
   - Test 17: `sla_thresholds_yellow_between_15_50` (CA-24) — SLA en 25% restante → `'WARNING'`.
   - Test 18: `sla_thresholds_red_below_15` (CA-24) — SLA en 8.3% restante → `'CRITICAL'`.
   - Test 19: `sla_thresholds_expired_past_deadline` (CA-24) — SLA en pasado → `'EXPIRED'`.
   - Test 20: `visibilitychange_recalculates_tick` (CA-25) — Simular visibilitychange → currentTick se actualiza.
   - Test 21: `auto_refresh_after_5min_inactivity` (CA-31) — Simular inactividad > 5 min, disparo de fetchGlobalInbox.

La matriz completa de test-a-CA está en `.agentic-sync/handoff_80DEV_US001_CA05_CA11_CA24_CA25_CA31.md` sección 5.

Audita `.agentic-sync/coverage_matrix.md` y verifica que CA-05, CA-11, CA-24, CA-25 y CA-31 estén marcados ✅ en Back, Front y QA.
Haz commit cerrando la iteración en la rama `sprint-3/informe_auditoriaSprint1y2`.
```
