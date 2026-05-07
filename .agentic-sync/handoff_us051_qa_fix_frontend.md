# Handoff Resolutivo: Bugs QA Lote 1 (Frontend) - US-051

**Destinatario:** Agente Frontend
**Emisor:** Arquitecto Líder (Aprobado tras revisión de QA)
**Prioridad:** 🚨 CRÍTICA (Bloqueante UAT)

## 🐛 Bugs Reportados (Gaslighting 404 y Router Bypass)
1. **Fallo CA-03:** Navegar a URLs inexistentes (ej: `/admin/identity-governance`) arroja pantalla en blanco y warning en consola, no muestra pantalla 404.
2. **Fallo CA-04:** Sin token en localStorage, intentar acceder a `/portal` no redirige a `/login`, la app se queda colgada en blanco.

## 🔍 Análisis Forense
1. Falta una ruta "catch-all" en `index.ts`.
2. El `RouteGuards.ts` confía en que todas las rutas tienen `meta.requiresAuth`. Por principio Zero-Trust, todo es privado a menos que tenga `meta.isPublic: true`.

## 🛠 Plan de Acción (Zero-Trust)

### 1. Actualizar `RouteGuards.ts`
En `frontend/src/router/RouteGuards.ts` (Línea ~29):
Reemplaza la condición restrictiva:
```typescript
if (to.meta.requiresAuth && !tokenStr) {
    return next('/login');
}
```
Por una denegación por defecto (Fail-Closed):
```typescript
// Si NO es pública explícitamente y no hay token -> Rechazar
if (!to.meta.isPublic && !tokenStr) {
    return next('/login');
}
```

### 2. Añadir Ruta Catch-All en `index.ts`
Al final del array `routes` en `frontend/src/router/index.ts` (fuera del layout principal), añade:
```typescript
{
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound404.vue'),
    meta: { isPublic: true }
}
```
*(Nota: Asegúrate de que el componente `NotFound404.vue` exista. Si está importado globalmente en `App.vue`, solo renderiza un template genérico o usa el componente si lo tienes en `/views`)*.

---
**DoD (Definition of Done):**
- Modificar `RouteGuards.ts` y `index.ts`.
- Validar `npm run build`.
- Notificar al humano: "Handoff Frontend QA Fix completado."
