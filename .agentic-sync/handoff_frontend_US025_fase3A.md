# 🟣 Handoff Frontend — US-025 Fase 3A: UX Avanzado

> **Emitido por:** Arquitecto Líder  
> **Fecha:** 2026-05-03  
> **Agente Destino:** Frontend  
> **Prioridad:** 🟢 P2  
> **Rama Git:** sprint-6  
> **Pre-requisito:** Fase 2 completada  
> **Gate de Salida:** `npm run test:unit` 100% pass + `npm run build` limpio

---

## 1. Contexto

Los 7 GAPs funcionales restantes de US-025 son features UX premium que no tienen código: Toast Fatal imborrable, Soft-Undo pattern, Empty States ilustrados, Focus Ring A11y, Lazy Loading, Session Lock Modal, y Degradación Responsiva.

---

## 2. Alineación Arquitectónica

| ADR | Impacto |
|-----|---------|
| ADR-002 (Vue 3) | Composition API, composables reutilizables |
| ADR-010 (Testing) | Todo componente nuevo requiere tests |
| ADR-014 (Error Observability) | FatalToast consume el interceptor 5xx remediado en Fase 1A |

---

## 3. Tareas

### Tarea 3.1 — Toast Fatal Nivel 0 (CA-21)

**Crear:** `frontend/src/components/common/FatalToast.vue`

**Especificación:**
- Escucha `global-error-dispatch` con `type: 'SERVER_ERROR'`
- Renderiza toast ROJO permanente (sin botón cerrar, sin auto-dismiss)
- Muestra: `"Error crítico (Trace: {traceId}). Contacte soporte."`
- Posición: top-center, z-index máximo, por encima de modales
- El toast persiste hasta que el usuario recarga la página

**Test:** `FatalToast.spec.ts` — 2 escenarios: aparece con error 500, no aparece con error 502

---

### Tarea 3.2 — Soft-Undo Pattern (CA-14)

**Crear:** `frontend/src/composables/useSoftUndo.ts`

**Especificación:**
```typescript
export function useSoftUndo(actionFn: () => Promise<void>, undoFn: () => void, delayMs = 5000) {
  // 1. Muestra toast con countdown "Acción ejecutada. Deshacer (5s)"
  // 2. Si el usuario hace clic en "Deshacer" → invoca undoFn()
  // 3. Si pasan 5s sin undo → invoca actionFn() (la acción destructiva real)
  // 4. Si el usuario cierra el tab → sendBeacon con flag de undo-abort
}
```

**Integrar en:** `WorkdeskGrid.vue` para acciones de eliminar/desasignar tarea

**Test:** `useSoftUndo.spec.ts` — 3 escenarios: execute after timeout, undo cancels, beacon on close

---

### Tarea 3.3 — Empty States Ilustrados (CA-12)

**Crear:** `frontend/src/components/common/EmptyState.vue`

**Props:**
```typescript
interface Props {
  variant: 'no-tasks' | 'no-access' | 'error' | 'no-results';
  title?: string;
  description?: string;
  ctaLabel?: string;
  ctaAction?: () => void;
}
```

**UX:** Ilustración SVG inline (no imagen externa). Usar gradientes suaves. CTA button con hover animation.

**Integrar en:** `Workdesk.vue` (reemplazar el empty-state actual de texto plano)

**Test:** `EmptyState.spec.ts` — 2 escenarios: renderiza con cada variant, CTA emite evento

---

### Tarea 3.4 — Focus Ring A11y (CA-23)

**Archivo:** `frontend/src/assets/index.css` (o global styles)

**CSS global a agregar:**
```css
/* US-025 CA-23: A11y Focus Ring */
*:focus-visible {
  outline: 2px solid var(--v-theme-primary, #1976d2);
  outline-offset: 2px;
  border-radius: 4px;
}

*:focus:not(:focus-visible) {
  outline: none;
}
```

**Verificación:** Navegar con Tab por toda la aplicación. Cada elemento interactivo (botón, input, link, select) debe mostrar ring azul visible.

---

### Tarea 3.5 — Lazy Loading con IntersectionObserver (CA-28)

**Crear:** `frontend/src/composables/useLazyLoad.ts`

```typescript
import { ref, onMounted, onUnmounted, type Ref } from 'vue';

export function useLazyLoad(targetRef: Ref<HTMLElement | null>, options?: IntersectionObserverInit) {
  const isVisible = ref(false);
  let observer: IntersectionObserver | null = null;

  onMounted(() => {
    if (!targetRef.value) return;
    observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        isVisible.value = true;
        observer?.disconnect();
      }
    }, { threshold: 0.1, ...options });
    observer.observe(targetRef.value);
  });

  onUnmounted(() => observer?.disconnect());
  return { isVisible };
}
```

**Uso en componentes pesados (Dashboard BAM, gráficos):**
```vue
<template>
  <div ref="sectionRef">
    <HeavyChart v-if="isVisible" />
    <SkeletonChart v-else />
  </div>
</template>
```

**Test:** `useLazyLoad.spec.ts` — 2 escenarios: no visible initially, visible after intersection

---

### Tarea 3.6 — Soft-Lock Sesión Glassmorphism (CA-27)

**Crear:** `frontend/src/components/common/SessionLockModal.vue`

**Especificación:**
- Se activa cuando `authStore.isTokenExpired()` === true (o cuando interceptor recibe 401)
- Modal fullscreen con backdrop glassmorphism (`backdrop-filter: blur(10px)`)
- Muestra avatar del usuario + nombre + input de contraseña
- "Desbloquear" → Invoca `POST /api/v1/auth/login` con credenciales → `authStore.hydrateAuth()`
- "Cerrar Sesión" → `authStore.logout()` + redirect `/login`
- El modal BLOQUEA toda interacción con la app (no puede hacer clic detrás)

**Integrar en:** `App.vue` como componente global que escucha estado de token

**Test:** `SessionLockModal.spec.ts` — 3 escenarios: renderiza cuando token expira, unlock exitoso, logout path

---

### Tarea 3.7 — Degradación Responsiva (CA-8)

**Archivo:** `frontend/src/layouts/MainLayout.vue`

**Implementación:**
```typescript
const isMobile = ref(window.innerWidth < 768);
const onResize = () => { isMobile.value = window.innerWidth < 768; };
onMounted(() => window.addEventListener('resize', onResize));
onUnmounted(() => window.removeEventListener('resize', onResize));
```

**Comportamiento:**
- En mobile (`< 768px`): Sidebar colapsado por defecto. Módulos de admin/modelado ocultos con `v-if="!isMobile"`
- En tablet (`768-1024px`): Sidebar colapsado, módulos visibles
- En desktop (`> 1024px`): Sidebar expandido, todos los módulos visibles

---

## 4. Criterios de Aceptación del Gate

- [ ] `FatalToast.vue` se renderiza para errores 500 y es imborrable
- [ ] `useSoftUndo` funcional con countdown y sendBeacon
- [ ] `EmptyState.vue` reutilizable con 4 variants
- [ ] Focus ring visible en TODA la app vía Tab navigation
- [ ] `useLazyLoad` composable funcional con IntersectionObserver
- [ ] `SessionLockModal.vue` aparece cuando token expira
- [ ] Mobile viewport oculta módulos admin del sidebar
- [ ] Todos los tests de componentes/composables nuevos pasando
- [ ] `npm run test:unit` 100% verde
- [ ] `npm run build` sin errores

## 5. Exclusiones

- NO implementar i18n (CA-24) — Diferido a V2
- NO implementar DOM Virtualization (CA-22) — Diferido a V2
- NO implementar Impersonación (CA-9/CA-31) — Diferido a V2

## 6. Archivos Impactados

| Archivo | Acción |
|---------|--------|
| `components/common/FatalToast.vue` | Crear |
| `composables/useSoftUndo.ts` | Crear |
| `components/common/EmptyState.vue` | Crear |
| `assets/index.css` | Modificar (focus ring) |
| `composables/useLazyLoad.ts` | Crear |
| `components/common/SessionLockModal.vue` | Crear |
| `layouts/MainLayout.vue` | Modificar (responsive) |
| + sus respectivos `.spec.ts` | Crear |
