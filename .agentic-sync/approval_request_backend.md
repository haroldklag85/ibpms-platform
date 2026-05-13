# Solicitud de Aprobación Arquitectónica: Backend US-038 (CA-01 al CA-05)

He analizado los requerimientos y el estado actual del código y propongo el siguiente plan de implementación para abordar la Tolerancia a Fallos, Aprovisionamiento JIT y el protocolo Break-Glass.

## Resumen del Plan Propuesto

1. **Tolerancia a Fallos (CA-01):**
   - Modificaré el bloque catch en `JwtAuthFilter` donde se consulta al `jwtBlacklistService`. Si hay un error (timeout de Redis), aplicaré el protocolo Fail-Open Degradado: si la petición es `GET` u `OPTIONS`, se permitirá continuar. Si es cualquier otro método que mute estado, se cortará la cadena con un HTTP 503 ("Sistema degradado").

2. **Aprovisionamiento JIT (CA-03):**
   - Crearé la excepción `PreconditionRequiredException` mapeada a HTTP 428.
   - Modificaré `EntraIdSyncService.provisionUser` para validar claims obligatorios y lanzar dicha excepción detallando los campos faltantes, la cual será interceptada por un `@ControllerAdvice`.

3. **Protocolo Break-Glass (CA-04):**
   - Crearé `EmergencyLoginController` en la ruta `/api/v1/auth/emergency-login` que, dado un secreto de contingencia, emitirá un token JWT de corta duración con privilegios para actuar si EntraID se encuentra fuera de servicio.
   - Habilitaré la ruta en `SecurityConfig`.

4. **Anti-Token Bloat y RBAC Aditivo (CA-02, CA-05):**
   - La lógica de filtrar roles por `ibpms_rol_` ya existe en `JwtAuthFilter`. Para CA-05, implementaré pruebas en `JwtAuthFilterTest` para validar y demostrar explícitamente la suma correcta de `GrantedAuthority` cuando el token contenga múltiples roles.

### Preguntas Abiertas
- ¿Desea el equipo de Arquitectura que el Endpoint de Break-Glass se ubique en un controlador dedicado `EmergencyLoginController` (como propongo) o que se integre en algún otro controlador existente?

Quedo a la espera de la aprobación formal para transicionar a la fase de **EXECUTION** y aplicar TDD estricto.
