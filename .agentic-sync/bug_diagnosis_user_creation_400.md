# 🩺 Diagnóstico Forense: Bug-Fix User Creation 400 Bad Request
**ID:** `bug_diagnosis_user_creation_400`
**Fecha:** 2026-05-20
**Agente:** BUG-FIX LEAD

## Descripción del Bug
Tras solventar el error 404 inicial, el usuario reporta que al intentar crear un nuevo usuario con la contraseña `Root#Temp4Sys`, la consola arroja un error **400 Bad Request** y falla la creación.

## Análisis Forense (Quadruple Check)
1. **Síntoma / Capa Probable:** Falla de Validación (`@Valid`) en el Backend con enmascaramiento de error en el Frontend.
2. **Causas Raíz:**
   - **Discrepancia en la Política de Contraseñas (Regex):** 
     - La validación Zod en el Frontend (en `IdentityGovernance.vue`) utilizaba el patrón `/[!@#$%^&*]/` para caracteres especiales.
     - El Backend (en `UserCreateRequestDTO` y `UserUpdateRequestDTO`) utilizaba la validación `@Pattern` con el patrón `[@$!%*?&]`.
     - *Consecuencia:* Al utilizar el carácter `#` (ej. `Root#Temp4Sys`), la UI lo permitía (Zod verde), pero el Backend lo rechazaba y lanzaba `MethodArgumentNotValidException`, que se traduce en un HTTP 400 sin stacktrace en consola.
   - **Enmascaramiento de Error en Frontend:**
     - El interceptor `showToast` intentaba leer el mensaje mediante `e.response?.data?.message`.
     - `GlobalExceptionHandler` en el backend usa el estándar `ProblemDetail` de Spring, por lo que el texto real del error se aloja en `e.response.data.detail`. Esto ocasionaba que en UI solo se viera el genérico "Error guardando usuario: AxiosError...".
   
## Plan Quirúrgico Ejecutado
- **Backend (UserCreateRequestDTO y UserUpdateRequestDTO):** Se agregaron los símbolos `#` y `^` a la expresión regular de validación de contraseñas.
- **Frontend (IdentityGovernance.vue):** 
  - Se agregó el símbolo `?` a la expresión regular de Zod para completa simetría con el Backend.
  - Se modificó la lectura de la respuesta de error para extraer `e.response?.data?.detail`, exponiendo el motivo real del fallo.
