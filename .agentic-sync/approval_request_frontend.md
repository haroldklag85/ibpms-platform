# Solicitud de Revisión Arquitectónica — BUG-TRANSITION-BLANK
**Fecha:** 2026-06-17T16:13:00-05:00  
**Solicitante:** Agente Frontend (DevDavid)  
**Severidad:** CRÍTICA (pantalla blanca en navegación SPA)

---

## Resumen Ejecutivo

Se solicita aprobación para aplicar un **fix quirúrgico** en `MainLayout.vue` que corrige la pantalla blanca al navegar entre vistas SPA.

## Causa Raíz Confirmada

En `frontend/src/layouts/MainLayout.vue`, líneas 255-256, existen **2 comentarios HTML** (`<!-- @Traceability... -->`) dentro del tag `<transition name="fade" mode="out-in">`. En Vue 3, los comentarios son parseados como VNodes, generando un fragmento con múltiples nodos raíz. Esto corrompe la máquina de estados `out-in`: el componente saliente nunca termina de animar y el entrante nunca se renderiza.

## Cambio Propuesto

**Mover** los 2 comentarios `@Traceability` desde **dentro** del `<transition>` hacia **fuera** del `<router-view>` (antes de este), donde no interfieren con el renderizado.

- **Archivos afectados:** 1 (`MainLayout.vue`)
- **Líneas modificadas:** 6 (reubicación de 2 comentarios)
- **Impacto en funcionalidades adyacentes:** CERO (solo se mueven comentarios HTML)
- **Riesgo de regresión:** MÍNIMO

## Criterios de Aceptación

| ID | Criterio | Método de Validación |
|----|----------|---------------------|
| CA-BUG-1 | Navegación SPA sin pantalla blanca | Navegación manual + build exitoso |
| CA-BUG-2 | Comentarios eliminados del `<transition>` | Inspección visual del código |

## Solicitud Formal

Arquitecto Líder, solicito su veredicto formal (✅ APROBADO / ❌ RECHAZADO) para proceder con la implementación de este fix quirúrgico.
