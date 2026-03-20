# Handoff Frontend - Iteración 35 (US-048: CA-1 a CA-7)

## Propósito
Construir la Interfaz de Administración "Pantalla 14" (Internal IdP) para gestionar Usuarios y Roles. Debe acatar políticas visuales de seguridad y mutar su estructura dependiendo del origen del usuario (Local vs EntraID).

## Criterios de Aceptación Cubiertos (Frontend)
* **CA-1 (Pantalla 14):** Crear la vista administrativa con Grillas (DataTables) para listar Usuarios y Roles. Modal o Página lateral para Crear/Editar.
* **CA-2 (Fuerza de Contraseña):** El formulario de creación de usuario local debe validar en tiempo real (Zod/VeeValidate) la regla: 8 chars, 1 Mayúscula, 1 Número, 1 Símbolo. Botón [Guardar] bloqueado si no cumple.
* **CA-3 (Clave Temporal):** Añadir botón de emergencia `[Generar Clave Temporal]` en la vista de edición. Al recibir el Response HTTP (200 OK) con la clave plana temporal, renderizarla en un Modal amigable (Alert/Dialog) e instruir al Admin que la copie porque no se volverá a mostrar.
* **CA-4 & CA-6 (Fábrica de Roles):** Interfaz para nombrar Roles y un componente Multi-Select visual (Dropdown con Tags) en la creación de usuario para asociar múltiples sombreros simultáneamente.
* **CA-5 (Kill Switch UI):** Toggle switch visual `[Activo / Inactivo]` en la grilla principal o panel de detalle. Al pulsarlo en Inactivo, detona la API destructiva del Backend.
* **CA-7 (Mutación Híbrida EntraID):** Si el registro del usuario viene marcado como externo (`isExternalIdp: true`), ocultar estructuralmente (con `v-if`) los campos "Contraseña", "Cambiar Clave" y el botón de Clave Temporal.

## Tareas Vue (Prioridad 2)
1. Diseñar el `IdentityManager.vue` con sub-tabs para `[Usuarios]` y `[Roles]`.
2. Integrar Axios con las nuevas APIs de Backend (`/api/v1/admin/users`, `/api/v1/admin/roles`).
3. Aplicar validación estricta Zod en el esquema de registro.
