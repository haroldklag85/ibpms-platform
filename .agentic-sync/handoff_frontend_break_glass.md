# Handoff: Implementación UI Break-Glass / Kill-Switch (US-036 / US-038)

**Destinatario:** [🎨 FRONTEND - VUE]
**Emitido por:** [🧠 ARQUITECTO LÍDER]
**Fecha:** 2026-05-11
**Contexto:** Cierre final de auditoría del Sprint J-04.

## 🎯 Objetivo Principal (Misión)
El Backend ya implementó exitosamente el servicio `JwtBlacklistService` conectado a Redis para la revocación masiva de sesiones. Sin embargo, la interfaz gráfica (UI) aún no tiene forma de disparar esta acción. 
**Tu misión es integrar un botón "Kill-Switch / Exorcización" en la vista de Seguridad/Administración (por ejemplo, en el componente de gestión de sesiones o usuarios) que consuma el controlador real del backend.**

## 🛠️ Especificaciones Técnicas

### 1. Endpoint a Consumir
- **Ruta:** `POST /api/v1/admin/users/{userId}/revoke-session`
- **Descripción:** Revoca todas las sesiones y tokens JWT activos de un usuario específico, inyectándolos en la Blacklist global de Redis.
- **Autorización:** Requiere rol `ROLE_ADMIN_IT` o `ROLE_SUPER_ADMIN` (class-level `@PreAuthorize` en `SessionRevocationController.java`).
- **⚠️ IMPORTANTE:** La ruta corregida fue validada forense por el Arquitecto Líder contra el código fuente real (línea 15 y 32 de `SessionRevocationController.java`). NO usar la ruta antigua `/admin/auth/revoke/`.

### 2. Diseño del Componente y UX
- El botón de acción ("Revocar Sesiones", "Exorcizar" o "Kill-Switch") debe ser **visible solo** para usuarios con rol `ROLE_ADMIN_IT` o `ROLE_SUPER_ADMIN`.
- **Alerta de Interacción (Zero-Trust UI):** Según las políticas del proyecto, *QUEDA ESTRICTAMENTE PROHIBIDO* usar `alert()` o `confirm()` nativos. Debes crear/utilizar un **Modal de Vue 3** para la confirmación de esta acción destructiva.
- **Manejo de Errores (Fail-Fast):** Implementa bloques `try/catch` reales usando Axios. Muestra notificaciones (Toasts) apropiadas para casos de éxito o error (ej: Error HTTP 403, 500).

---

## 🛑 REGLAS DE GOBERNANZA Y SKILLS (OBLIGATORIAS)

### 0. Skills de Ejecución (OBLIGATORIO LEER ANTES DE CODIFICAR)
Para asegurar que tu ejecución se adhiera a las directrices de la plataforma, **debes leer y aplicar estrictamente** los siguientes skills:
- **`cat .agents/skills/frontend_build_audit/SKILL.md`** (Validación de Compilación Frontend)
- **`cat .agents/skills/zero_mock_enforcement/SKILL.md`** (Prohibición de Mocks de Red)

### 1. LEY GLOBAL 3: Trazabilidad Inversa / Anti-Amnesia Institucional (.cursorrules)
Es obligatorio que documentes el código. Debes inyectar etiquetas de trazabilidad en los componentes Vue modificados o creados.
- **Ejemplo de Etiqueta (Vue):** 
  ```html
  <!-- @Traceability: US-036, US-038 - CA-21, CA-25 -->
  ```
- **SSOT (Single Source of Truth):** Al finalizar tu implementación, asegúrate de marcar esta tarea como resuelta en `task.md` y/o en la documentación de historias de usuario correspondiente (SSOT) garantizando la *Trazabilidad Inversa*.

### 2. Zero-Mock & Real Database Enforcement (ADR-010)
**¡ESTÁ ESTRICTAMENTE PROHIBIDO SIMULAR RESPUESTAS (MOCKS)!** 
Tu componente debe consumir el endpoint real de la API a través de Axios. No uses arrays estáticos ni interceptores de red para "falsear" la respuesta. Si el backend falla, la UI debe manejar el error y mostrarlo, aplicando *Graceful Degradation*.

### 3. Zero-Trust Compilation & SRE Immunity (LEY GLOBAL 2)
No puedes entregar este trabajo simplemente escribiendo código. Al finalizar, debes validar tu código:
- **Ejecuta localmente:** `npm run build` (o su equivalente Vite `npm run type-check`) para asegurar que el proyecto compila correctamente ("Green Build").
- **Prohibido usar `git stash`:** Todo tu trabajo terminado debe enviarse con `git add .`, seguido de `git commit -m "feat(security): implementa boton kill-switch US-036"` y un posterior `git push` a la rama correspondiente.

---
## 📋 Tareas Puntuales para Frontend
1. Identificar/Crear el espacio UI en la sección de Administración (ej: `/admin/security/identity` o la vista de listado de usuarios).
2. Construir la función de invocación al API (`POST /api/v1/admin/auth/revoke/{userId}`).
3. Construir el **Modal de Confirmación** (sin `window.confirm()`).
4. Añadir la trazabilidad `@Traceability`.
5. Ejecutar la compilación para confirmar ausencia de errores.
6. Actualizar SSOT (`task.md` / `sprint_plan_s7.md` si corresponde) documentando el cierre del hallazgo.
7. Cometer y Empujar el código.

¡Confío en tu precisión! Recuerda empezar tu respuesta asumiendo el rol y confirmando la recepción de este handoff.
