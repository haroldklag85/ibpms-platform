# Handoff Backend - Iteración 44 (US-038: CA-1 a CA-5)

## Propósito
Integrar el motor central de Identidad Híbrida (Microsoft EntraID / Keycloak) blindando a la plataforma iBPMS (V1) contra colapsos de red, saturación de Tokens JWT (Bloating) y perfiles corporativos incompletos mediante Aprovisionamiento Just-In-Time (JIT).

## Criterios de Aceptación Cubiertos (Backend)
* **CA-1 (Redis Fail-Open):** Interceptor JWT. Debe consultar Redis (Lista Negra de Tokens). Si Redis arroja un *Timeout* o *Connection Refused*, el interceptor debe aplicar una postura **Fail-Open**: confiar exclusivamente en la firma criptográfica (RSA/HMAC) y expiración del JWT, permitiendo el paso del usuario para no asfixiar la operatividad, emitiendo un Log de severidad crítica.
* **CA-2 (Anti-Token Bloat):** Al parsear un JWT externo (o en el mapeo OAuth2), extraer la lista de roles (`groups` o `roles`) y aplicar un `.filter(rol -> rol.startsWith("ibpms_rol_"))`, descartando el resto (Ej: `VPN_Users`) para evitar saturar el Payload y prevenir errores HTTP 431.
* **CA-3 (JIT Provisioning & Guardrail):** Endpoint de Login/Sync `POST /api/v1/auth/sync`. Recibe el JWT, busca al usuario en BD local. Si no existe, lo crea (`[Ciudadano_Interno]`). Si al Token original le faltan *Claims Mínimos Vitales* configurables, el backend debe retornar un HTTP especial (Ej: 428 Precondition Required o 460 Incomplete Profile) informándole al Frontend qué datos faltan.
* **CA-4 (Break-Glass Protocol):** Endpoint aislado `POST /api/v1/auth/emergency-login` que solo opere bajo validadores de IP (`HttpServletRequest.getRemoteAddr()`) para permitir acceso a la cuenta hiper-administradora en caso de Apocalipsis EntraID.
* **CA-5 (RBAC Aditivo):** La lógica de autorización de Spring Security (`hasAnyAuthority`) debe consolidar la sumatoria de todos los roles (Allow-Overrides), no aplicar bloqueos por intersecciones nulas de roles contradictorios.

## Tareas Java (Prioridad 1 - Tarea Bloqueante)
1. Codificar el Filtro JWT de Spring Security (Tolerancia a fallas Redis).
2. Crear el servicio de Sincronización JIT (Endpoint de Login).
3. Asegurar los filtros Anti-Bloat en el mapeo de Roles del Token.
