# 🟢 Handoff Frontend — US-025 Fase 2: App Shell RBAC + Role Selector

> **Emitido por:** Arquitecto Líder  
> **Fecha:** 2026-05-03  
> **Agente Destino:** Frontend  
> **Prioridad:** 🟡 P1  
> **Pre-requisitos:** Fase 0 ✅ + Fase 1A ✅ + Fase 1B (Backend+Infra) ✅  
> **Gate de Salida:** `npm run build` + `npm run test:unit` 100%

---

## 1. Contexto

Con Fase 1A los tests del App Shell están blindados y el interceptor ADR-014 funciona.
Con Fase 1B el backend ya delega al puerto MenuTopologyPort (menús dinámicos desde BD).
Ahora debemos completar la experiencia RBAC visual: selector de rol en el header,
sidebar que reacciona al cambio de rol, y transiciones suaves.

---

## 2. Tareas (6)

### TAREA F2.1 — Crear RoleSelectorDropdown.vue (NUEVO)
Ruta: `frontend/src/components/shell/RoleSelectorDropdown.vue`

Especificación:
- Componente `<script setup lang="ts">` que usa `useAuthStore()`
- Muestra el `authStore.activeRole` como texto del botón
- Al hacer click, abre un dropdown con la lista de `authStore.roles`
- Al seleccionar un rol diferente, llama `authStore.switchRole(roleId)`
- El dropdown debe cerrarse al seleccionar o al hacer click fuera (onClickOutside)
- Estilo glassmorphism: `backdrop-blur-md bg-white/90 border border-slate-200 rounded-lg shadow-xl`

Anatomía del componente:
```html
<div class="relative" data-testid="role-selector">
  <!-- Botón trigger -->
  <button @click="isOpen = !isOpen" class="flex items-center gap-2 px-3 py-1.5 ...">
    <span class="material-symbols-outlined text-[18px]">badge</span>
    <span class="text-sm font-medium truncate max-w-[140px]">{{ formatRole(authStore.activeRole) }}</span>
    <span class="material-symbols-outlined text-[16px]" :class="{ 'rotate-180': isOpen }">expand_more</span>
  </button>
  
  <!-- Dropdown panel -->
  <div v-if="isOpen && authStore.roles.length > 1" class="absolute top-full mt-1 right-0 w-56 ...glassmorphism...">
    <button v-for="role in authStore.roles" :key="role"
      @click="selectRole(role)"
      :class="{ 'bg-indigo-50 text-indigo-700 font-bold': role === authStore.activeRole }"
      class="w-full text-left px-3 py-2 text-sm hover:bg-slate-50 ...">
      {{ formatRole(role) }}
    </button>
  </div>
</div>
```

Lógica:
- `formatRole(role)`: elimina 'ROLE_', reemplaza '_' por ' ', capitaliza primera letra
- `selectRole(role)`: llama `authStore.switchRole(role)`, cierra dropdown, sidebar se refrescará vía el listener `role-switched` que ya existe en authStore.ts L133
- Solo renderiza si `authStore.roles.length > 1` (si tiene 1 solo rol, no muestra selector)

### TAREA F2.2 — Montar RoleSelectorDropdown en MainLayout Header
Archivo: `frontend/src/layouts/MainLayout.vue`

En el `<header>` (L116-177), insertar el RoleSelectorDropdown ENTRE el buscador (L134-139) y el density toggle (L143-166):

```html
<!-- Después de L141 (div separador) -->
<RoleSelectorDropdown v-if="authStore.roles.length > 1" />
<div class="h-6 w-px bg-slate-200 hidden md:block mx-1"></div>
```

Agregar el import en `<script setup>`:
```typescript
import RoleSelectorDropdown from '@/components/shell/RoleSelectorDropdown.vue';
```

### TAREA F2.3 — Listener de role-switched en sidebar (purge + refetch)
Archivo: `frontend/src/layouts/MainLayout.vue`

En el `<script setup>` (después de L254 `menuStore.fetchMenuLayout()`), agregar:

```typescript
import { onMounted, onUnmounted } from 'vue';

const handleRoleSwitched = async () => {
    menuStore.purgeTopology();
    await menuStore.fetchMenuLayout();
};

onMounted(() => {
    menuStore.fetchMenuLayout();
    window.addEventListener('role-switched', handleRoleSwitched);
});

onUnmounted(() => {
    window.removeEventListener('role-switched', handleRoleSwitched);
});
```

Esto reemplaza el `onMounted` actual (L252-255) que solo hace `fetchMenuLayout()`.
Ahora el sidebar se refrescará automáticamente cuando el usuario cambie de rol.

### TAREA F2.4 — Skeleton → Spinner timeout en sidebar loading
Archivo: `frontend/src/layouts/MainLayout.vue`

En el spinner de loading del sidebar (L33-36), agregar un timer de 5s que cambia a spinner
y 15s que muestra error:

Agregar al `<script setup>`:
```typescript
const loadingPhase = ref<'skeleton' | 'spinner' | 'timeout'>('skeleton');
let loadingTimer: ReturnType<typeof setTimeout> | null = null;

watch(() => menuStore.isLoading, (loading) => {
    if (loading) {
        loadingPhase.value = 'skeleton';
        loadingTimer = setTimeout(() => {
            loadingPhase.value = 'spinner';
            loadingTimer = setTimeout(() => {
                loadingPhase.value = 'timeout';
            }, 10000); // 10s más = 15s total
        }, 5000);
    } else {
        loadingPhase.value = 'skeleton';
        if (loadingTimer) clearTimeout(loadingTimer);
    }
});
```

Modificar el template del spinner (L33-36):
```html
<div v-if="menuStore.isLoading" class="flex flex-col items-center justify-center p-4 gap-2">
    <!-- Skeleton (0-5s) -->
    <template v-if="loadingPhase === 'skeleton'">
        <div v-for="i in 4" :key="i" class="h-8 bg-slate-800 rounded-lg animate-pulse w-full"></div>
    </template>
    <!-- Spinner (5-15s) -->
    <template v-else-if="loadingPhase === 'spinner'">
        <span class="material-symbols-outlined animate-spin text-slate-500">sync</span>
        <p v-if="!isSidebarCollapsed" class="text-[10px] text-slate-500">Cargando módulos...</p>
    </template>
    <!-- Timeout (>15s) -->
    <template v-else>
        <span class="material-symbols-outlined text-red-400">error</span>
        <p v-if="!isSidebarCollapsed" class="text-[10px] text-red-400">Error al cargar menú</p>
        <button v-if="!isSidebarCollapsed" @click="menuStore.purgeTopology(); menuStore.fetchMenuLayout()"
            class="text-[10px] text-indigo-400 underline">Reintentar</button>
    </template>
</div>
```

### TAREA F2.5 — Router Transition fade 300ms
Archivo: `frontend/src/layouts/MainLayout.vue`

Reemplazar el router-view actual (L180-186):
```html
<div class="flex-1 overflow-auto bg-transparent relative">
    <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
            <keep-alive include="Workdesk">
                <component :is="Component" />
            </keep-alive>
        </transition>
    </router-view>
</div>
```

Agregar CSS en `<style scoped>` (al final):
```css
.fade-enter-active, .fade-leave-active {
    transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
    opacity: 0;
}
```

### TAREA F2.6 — Crear RoleSelectorDropdown.spec.ts (TEST)
Ruta: `frontend/src/tests/components/shell/RoleSelectorDropdown.spec.ts`

3 escenarios mínimos:
1. Con 1 solo rol → NO renderiza el selector (v-if)
2. Con 2+ roles → Renderiza botón con activeRole formateado, click abre dropdown
3. Seleccionar un rol diferente → llama authStore.switchRole(roleId), dropdown se cierra

---

## 3. Archivos

| Archivo | Acción | Líneas afectadas |
|---------|:------:|:----------------:|
| `components/shell/RoleSelectorDropdown.vue` | CREAR | ~80 líneas |
| `layouts/MainLayout.vue` | MODIFICAR | L141 (insertar selector), L180-186 (transition), L252-255 (listener), L33-36 (skeleton) |
| `tests/components/shell/RoleSelectorDropdown.spec.ts` | CREAR | ~50 líneas |

## 4. Límites — NO TOCAR

- ❌ NO crear ImpersonationBanner.vue (eso es Fase 3B)
- ❌ NO instalar vue-i18n (eso es Fase 3B)  
- ❌ NO crear FatalToast.vue ni SessionLockModal.vue (eso es Fase 3A)
- ❌ NO modificar authStore.ts (switchRole ya existe en L132-134)
- ❌ NO modificar useMenuStore.ts (purgeTopology ya existe)
- ❌ NO tocar código backend ni Liquibase

## 5. Gate

```bash
cd frontend
npm run build     # Compilación sin errores
npm run test:unit  # 100% verde (incluye nuevo RoleSelectorDropdown.spec.ts)
```

Verificación visual (manual):
1. Login como usuario multi-rol → Header muestra dropdown con roles
2. Seleccionar otro rol → Sidebar se refresca con módulos del nuevo rol
3. Navegar entre vistas → Transición fade suave visible
4. Sidebar loading lento → Skeleton (0-5s) → Spinner (5-15s) → Error + Reintentar

## 6. Próximo Handoff

→ `.agentic-sync/handoff_frontend_US025_fase3A.md` (UX Avanzado: FatalToast, SessionLock, A11y)
Espera aprobación del Arquitecto.
