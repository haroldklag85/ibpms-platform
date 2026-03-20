# Handoff Frontend - Iteración 44 (US-038: CA-1 a CA-5)

## Propósito
Responder en tiempo real a las excepciones de control de Identidad dictadas por el Backend, forzando la recolección manual de datos faltantes (JIT Guardrail) y disponiendo de un acceso de emergencia.

## Criterios de Aceptación Cubiertos (Frontend)
* **CA-3 (Modal JIT - Guardrail de Claims Mínimos):** 
    - En el ciclo del SSO Callback, si la redirección o el endpoint `/api/v1/auth/sync` retorna el error específico de "Perfil Incompleto" (Ej: HTTP 428 o Body con `missingClaims: ['Sucursal_ID']`), el router NO dejará pasar al usuario al Workdesk.
    - Se renderizará un Modal Bloqueante Obligatorio: `[Completar Perfil Local]`. El usuario llenará solo los campos solicitados por la BD, enviará de vuelta un PUT al Backend y entonces sí recibirá su token de sesión válido.
* **CA-4 (Break-Glass Login):** 
    - Diseñar una ruta oculta (Ej: `/admin/breakglass` o `?emergency=true` en Login).
    - Expondrá un formulario explícito Usuario/Contraseña Local (evadiendo el botón corporativo de Azure AD).
    - Este formulario consumirá ciegamente el endpoint `POST /api/v1/auth/emergency-login`.
    - *(Nota: La rotación pasiva post-incidente se delegará a validaciones estándar en pantalla de configuración).*

## Tareas Vue (Prioridad 2 - Suspendido hasta Fase 1)
1. Interceptar HTTP Responses del Login.
2. Inyectar el Componente Global de `CompleteProfileModal.vue`.
3. Renderizar la pantalla secreta de Break-Glass.
