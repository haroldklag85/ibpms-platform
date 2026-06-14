# Handoff de Remediación Integrada: US-038 (Federación de Identidad y Fail-Open) - Bloque 1

**Fecha/Hora:** 2026-04-18
**Contexto:** Auditoría Técnica y Forense iniciada para la US-038 por el Agente Arquitecto Líder.
**Alcance:** Remediación de las brechas funcionales de los Criterios de Aceptación CA-01 al CA-06.

---

## 🛑 Hallazgos y GAPs Detectados (Auditoría Forense - Bloque 1)

Tras evaluar el estado de la US-038, certifico como "All-Green" los criterios **CA-02 (Filtro Mochila Pesada instalado en JwtAuthFilter)** y **CA-05 (Resolución Aditiva de Permisos soportada nativamente por Spring Security)**.

Sin embargo, existen 4 GAPs ineludibles que la cuadrilla debe resolver inmediatamente:

1. **GAP de Seguridad (CA-01 - Fail-Open Policy Degradado):** El `JwtAuthFilter` tiene un bloque `catch` que atrapa la caída de Redis (Lista Negra), pero permite TODAS las peticiones. La política dicta operar en degradación segura: si la validación de Lista Negra falla, el sistema debe revisar el Método HTTP. Si es `GET`, permite pasar (Fail-Open); si es `POST/PUT/DELETE`, debe inyectar un error `403 Forbidden` abortando mutaciones (Fail-Closed) para prevenir intrusiones en sombra.
2. **GAP Funcional (CA-03 - Guardrail JIT de Claims Mínimos):** El aprovisionamiento JIT no valida "Claims Mínimos Vitales". El Backend debe inspeccionar el JWT en búsqueda de metadatos básicos (Ej: `custom:sucursal_id`). Si el perfil corporativo está incompleto (faltan campos vitales), debe detener el request y notificar al frontend (HTTP 403 / 409 con código de error específico `INCOMPLETE_PROFILE`) para que renderice un modal "Completar Perfil Local".
3. **GAP de Ciberseguridad (CA-04 - Protocolo Break-Glass):** Falta el Login de Emergencia Local (Backdoor autorizado). Requiere un endpoint `@PostMapping("/api/v1/auth/break-glass")` exclusivo para la red interna (IP Whitelist o validación de Header). Si se usa, debe generar un JWT local y disparar Alerta Roja al log. 
4. **GAP Arquitectónico (CA-06 - SoD, Juez y Parte):** No existe prevención para la segregación de funciones. El Backend debe poseer un interceptor transversal (ej. `@Aspect`) o chequeo en los endpoints de completitud de tarea (`/complete`) que valide que el `CurrentUserID` != `TaskCreatorID` si la tarea actual es de un proceso sujeto a restricciones. Si infringe, lanza Excepción y escribe en el "Tablero de Anomalías".

---

## 🛠️ Cuadrilla de Desarrollo: Directivas de Remediación

### Para el Agente Backend (Experto Data & Security)
1. **Filtro Degradado (CA-01):** En `JwtAuthFilter.java`, dentro del `catch` de validación de Redis, extraiga el `request.getMethod()`. Si es distinto de "GET" o "OPTIONS", interrumpa el `FilterChain` con `response.sendError(403, "Modo Seguro Degradado: Mutaciones prohibidas sin Blacklist Activa")`.
2. **Aprovisionamiento Guardrail (CA-03):** En el ciclo JIT, inspeccione el JWT. Si falta un set de claims requeridos (aloyelos por configuración o checkee un dummy claim), envíe al cliente un status `409 Conflict` o devolviendo un flag, y no inyecte el Principal en el `SecurityContextHolder`.
3. **Emergency Auth (CA-04):** Construir `BreakGlassController` que autentique user/pass tradicionales directamente contra la tabla `ibpms_users` (sin EntraID). Aplique validación primitiva (por ahora, imprimir LOG crítico que diga `[EMERGENCY BREAK-GLASS INITIATED]`).
4. **Filtro Juez y Parte (CA-06):** Cree un AOP `@Aspect` sobre el método que complete tareas de Camunda (ej. `completeTask()`), obteniendo el originador de la instancia de Camunda y comparándolo con el usuario autenticado. Lanza `SecurityViolationException` si coinciden.

### Para el Agente Frontend (Experto UI)
1. **Modal de Completitud (CA-03):** Crear un componente `IncompleteProfileModal.vue` que se levante si la API retorna el error/código de guardrail (JIT incompleto). Captura el Form de datos y lo envía al BE para subsanar el perfil.
2. **Alerta Degradada Visual (CA-01):** Si cualquier request de mutación recibe 403 por Degradación, mostrar un Toast: "El Sistema opera en Modo de Mitigación (Solo Lectura). No se registrarán sus cambios."

### Para el Agente QA (Testing E2E)
1. **Simulación de Caída (CA-01):** Escriba test que inyecte un Mock de Redis que lance `RedisConnectionFailureException` y compruebe que los `GET` pasan pero los `POST` fallan.
2. **Aserciones Juez (CA-06):** Provea un JUnit que valide que iniciar un caso con un Id e intentar aprobar la tarea posterior con ese mismo Id resulta en una infracción `403`.

---

**Protocolo de Uso:** Procedan de inmediato y confirmen las implementaciones en el canal base para proceder a la segunda etapa.
