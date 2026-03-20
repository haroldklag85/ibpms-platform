# Handoff Frontend - Iteración 38 (US-036: CA-11 a CA-15)

## Propósito
Exponer visualmente el botón rojo de exterminio de sesión, preparar la interfaz de configuración para encender formularios anónimos y diseñar la vista "Desnuda" sin navegación de SPA para dichos ciudadanos.

## Criterios de Aceptación Cubiertos (Frontend)
* **CA-11 y CA-12 (Reglas Pasivas):** No programar componentes de Doble Factor en Vue. Ignorar el borrado de campos por RBAC (se hará en el Pro-Code builder a futuro).
* **CA-13 (Roles Dinámicos):** Transparente para Frontend. El backend enviará la Data unificada.
* **CA-14 (Botón Táctico de Exorcismo):** En la Ficha de Pantalla 14 del Administrador, añadir un Botón Rojo Peligroso `[Revocar Todo y Matar Sesión]`. Al invocarlo, disparará Axios contra `/kill-session`. 
* **CA-15 (Bypass Anónimo de URLs Públicas):** 
   1. En la configuración de un Proceso (Admin), inyectar un Switch: `[Permitir Trámite Público]`. Al activarlo, mostrar el Link anónimo genérico al Admin.
   2. Crear una Ruta Huérfana en Vue Router (ej: `/public/start/{processKey}`) que carezca de envolvente global (Sin Menú Lateral, sin Header con Logout) y por supuesto, que ignore los `Navigation Guards` de Autenticación, permitiendo carga directa en la Web abierta.

## Tareas Vue (Prioridad 2 - Suspendido hasta Fase 1)
1. Inyectar botón Rojo y consumir API de Exorcismo.
2. Desacoplar Vue Router creando la zona "Huérfana" `/public/`.
3. Inyectar el Switch de visibilidad `isPublic` en el Setup de Procesos.
