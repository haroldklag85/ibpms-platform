# Handoff Frontend - Iteración 36 (US-036: CA-1 a CA-5)

## Propósito
Dotar a la Pantalla 14 (Gestor de Identidades) de la Matriz Visual de Permisos y condicionar el enrutamiento de la plataforma en base a las reglas de Autorización RBAC.

## Criterios de Aceptación Cubiertos (Frontend)
* **CA-1 (Hibridación):** Visualizar el origen de los roles en la grilla (Badge "Local" vs "EntraID").
* **CA-2 (Super Admin Access):** Implementar Navigation Guards (`vue-router`). Solo si `authStore.roles.includes('ROLE_SUPER_ADMIN')` permitir el renderizado de la Pantalla 14 de Gestión de Roles. Ocultar el link del Side Menu para el resto.
* **CA-3 (Roles Plantilla):** Interfaz para ensamblar "Roles Plantilla" seleccionando permisos, y un botón de "Asignación Masiva" cruzando Grilla de Usuarios vs Rol.
* **CA-4 (Matriz Iniciador/Ejecutor):** Dentro de la configuración de un Rol, pintar una Tabla Matricial cruzando `[Lista de Procesos BPMN]` vs Checkboxes de `[Puede_Iniciar]` y `[Puede_Ejecutar]`. Las validaciones asimétricas de Zod deben garantizar su consistencia antes del POST.
* **CA-5 (Privacidad Visual):** Adaptar el Workdesk (Pantalla 5). El Frontend NO debe traer la data completa y ocultarla (Inseguro). Debe constreñirse a consumir la API de colas privadas del Backend y renderizar estrictamente lo devuelto.

## Tareas Vue (Prioridad 2 - Suspendido hasta Fase 1)
1. Construir el panel Matricial de Permisos (Checkboxes por Proceso).
2. Codificar las defensas de Enrutador (Vue Router) aislando Pantalla 14.
3. Asegurar que el estado Pinia interprete el JWT para condicionar la UI al vuelo.
