# 📋 Solicitud de Revisión — Agente Frontend

> **Fecha**: 2026-07-01T21:05:00-05:00  
> **Agente**: Frontend Developer  
> **Rama**: `DevDavid`  
> **Bugs**: BUG-J02-004 (P2) + BUG-J02-005 (P3)  
> **Handoff**: `.agentic-sync/handoff_frontend_BUG01-JORNEY_BUG-J02-004_005.md`

---

## Resumen del Plan

Se realizarán **7 cambios quirúrgicos** en un único archivo: `frontend/src/views/admin/Modeler/BpmnDesigner.vue`.

### BUG-J02-004 — Filtro Visual Simple/Maestro
1. **Nueva ref** `formTypeFilter` de tipo `'ALL' | 'SIMPLE' | 'MAESTRO'` (default: `'ALL'`).
2. **Modificar `filteredForms` computed**: Combinar el filtro existente por `processPattern` (Filtro 1) con el nuevo filtro visual del usuario (Filtro 2). Se preserva 100% la lógica existente.
3. **Insertar toggle de 3 botones** (Todos / 🟢 Simple / 🔵 Maestro) en 2 lugares:
   - Sección UserTask (antes del `<select>` FormKey, línea 529)
   - Sección StartEvent (antes del `<select>` FormKey, línea 597)
4. **Exponer** `formTypeFilter` en `defineExpose`.

### BUG-J02-005 — CSS Dropdown FormKey
5. **Reemplazar clases CSS** del `<select>` FormKey en UserTask (línea 529).
6. **Reemplazar clases CSS** del `<select>` FormKey en StartEvent (línea 597).

Mejoras CSS aplicadas:
- `rounded-lg`, `p-2.5`, `bg-white`, `text-gray-900`, `shadow-sm`
- `focus:ring-2 focus:ring-indigo-500`, `transition-colors`, `hover:border-indigo-400`
- `appearance-none`, `cursor-pointer`

## Impacto de Regresión
- **BAJO**: Cambios puramente aditivos y cosméticos.
- La lógica de `processPattern` se preserva intacta.
- No se modifican endpoints, servicios ni lógica de negocio.

## Verificación Planificada
- `npm run build` para validar compilación.
- Verificación visual de los toggles y estilos CSS.

---

**Solicito aprobación formal del Arquitecto Líder para proceder a modo EXECUTION.**
