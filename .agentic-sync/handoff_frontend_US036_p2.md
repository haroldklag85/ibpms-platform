# Handoff Frontend - Iteración 37 (US-036: CA-6 a CA-10)

## Propósito
Construir los componentes visuales de Autogestión (Delegación) y la gestión de Cuentas de Servicio (API Keys), respetando las normativas de inmutabilidad (Botones de Soft Delete).

## Criterios de Aceptación Cubiertos (Frontend)
* **CA-6 (Herencia Visual):** En la Pantalla 14, al diseñar un Rol, permitir cruzarlo con un "Rol Padre" mediante un Dropdown `[Heredar de...]`.
* **CA-7 (UI Soft-Delete):** Invalidar el concepto de "Eliminar Usuario" de las mentes. Cambiar cualquier ícono de basura por un ícono de "Congelar/Desactivar".
* **CA-8 (Ciudadano Interno):** Transparente para la UI, el sistema tratará a un SSO user recién llegado como un `[Ciudadano_Interno]` con bandeja de lectura restringida.
* **CA-9 (Panel de Autogestión / Delegación):** Diseñar un componente accesible para usuarios estándar donde elijan a un "Compañero Suplente", abran un *DatePicker* con `Fecha Inicio` y `Fecha Fin`, y autoricen la clonación temporal de sus accesos.
* **CA-10 (Gestión de Robots M2M):** Una pestaña en la Pantalla 14 exclusiva para Cuentas de Servicio. Botón `[Generar Nueva API Key]`. Mostrar un alert (One-Time-View) obligando al Admin a copiar el Hash generado, ya que por protección no será visible después.

## Tareas Vue (Prioridad 2 - Suspendido hasta Fase 1)
1. Construir el Widget de Delegación Temporal (DatePicker condicional) en el Perfil de Usuario.
2. Añadir la Tab de Robots M2M.
3. Actualizar la vista de Creación de Roles para soportar Herencia.
