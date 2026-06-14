# 🩺 Diagnóstico Forense: Bug-Fix User Creation 404 y Payload Mismatch
**ID:** `bug_diagnosis_user_creation_404`
**Fecha:** 2026-05-20
**Agente:** BUG-FIX LEAD

## Descripción del Bug
El usuario reporta que al intentar crear un nuevo usuario desde la pestaña "Usuarios y Sesiones" (IdentityGovernance), el sistema falla silenciosamente en la UI y la consola arroja un error 404 para la petición HTTP POST y problemas adicionales de validación.

## Análisis Forense (Quadruple Check)
1. **Síntoma / Capa Probable:** Error de Enrutamiento 404 originado en el Frontend (`apiClient`) y un error subsecuente de Mapeo de Payload (Bad Request o Excepción Interna).
2. **Archivos Sospechosos y Causas:**
   - `frontend/src/views/admin/Security/IdentityGovernance.vue`: Múltiples invocaciones a `apiClient` e `integrationStore` contenían el prefijo `/api/v1` en su URL (`/api/v1/admin/users`), cuando la instancia configurada de `apiClient` ya inyectaba este prefijo globalmente a través de `baseURL: '/api/v1'`. Esto generaba que la URL real despachada fuera `/api/v1/api/v1/admin/users`, resultando en un 404 inmediato.
   - En el método `saveUser()`, al realizar la solicitud `POST`, se inyectaba el payload clonando el modelo de UI `...userForm.value`, el cual generaba claves como `name` y `roles`.
   - `backend/.../dto/security/UserCreateRequestDTO.java`: El backend exige que el objeto enviado contenga `username` y `roleIds` explícitamente. Al enviar `name` y `roles`, el DTO en Spring Boot fallaba sus validaciones `@NotBlank` o las deserializaciones.
3. **Validación contra SSOT:** El Frontend debe adherirse de manera exacta al DTO y aprovechar la configuración global de Axios (`baseURL`) sin duplicar rutas.
   
## Plan Quirúrgico
- **Frontend (`IdentityGovernance.vue`):** 
  - Ejecutar una limpieza global (RegEx) de todas las llamadas de red (`apiClient` e `integrationStore`) removiendo las redundancias de `/api/v1/`.
  - Refactorizar el método `saveUser()` y construir un objeto JSON explícito tanto para la Inserción (`createPayload`) como la Actualización (`updatePayload`), mapeando las variables de UI a las firmas exigidas por los contratos `UserCreateRequestDTO` y `UserUpdateRequestDTO`.
