# 🚀 HANDOFF REMEDIACIÓN BACKEND — US-017 (CQRS & Event Sourcing)

**De:** Arquitecto Líder
**Para:** Agente Backend
**Prioridad:** CRÍTICA (Bloqueante de Arranque Spring Boot)
**Rama:** `sprint-6`

---

## 🎯 Objetivo de la Remediación
El motor CQRS y la bóveda inmutable han sido desarrollados satisfactoriamente, pero la arquitectura actual sufre de un conflicto fatal de enrutamiento que provocará un colapso del contenedor Spring Boot (`Ambiguous handler methods mapped`). Adicionalmente, existen fugas de trazabilidad en el Event Store durante los Auto-Claims y omisiones de validación RBAC.

Tu misión es aplicar los 4 parches arquitectónicos detallados a continuación respetando la directriz **Zero-Mock** y la pureza del modelo Hexagonal.

---

## 🛠️ Plan de Trabajo Prescriptivo (Remediación)

### 1. [ARQ-017-01] Resolución de Colisión de Endpoints
Actualmente `FormCompletionController` y `WorkboxTaskController` están mapeados a `POST /api/v1/workbox/tasks/{id}/complete`.
Adicionalmente, `DraftController` contiene endpoints dummy (ficticios).
* **Acción 1:** Elimina la clase `DraftController.java` (basura residual).
* **Acción 2:** Modifica `FormCompletionController.java`. Cambia su ruta base a `@RequestMapping("/api/v1/workbox/bpmn-tasks")` u otra variante que garantice el no-conflicto, preservando el llamado a `formCompletionService.completeTask`.
* **Alternativa recomendada:** Como el CQRS asume todo, fusiona las peticiones de completitud en un solo orquestador o simplemente usa `/api/v1/workbox/bpmn-tasks/{taskId}/complete` para diferenciarlo del Workdesk Ágil.

### 2. [ARQ-017-02] Seguridad: Validación RBAC en Auto-Claim (CA-13)
En `com.ibpms.poc.application.service.AutoClaimService` (línea ~48):
* **Acción:** Reemplaza el comentario `// Simulación: Si hay grupos asignados...` con una validación real.
* Extrae los roles/grupos del usuario actual (puedes usar `SecurityContextHolder.getContext().getAuthentication().getAuthorities()`).
* Crúzalos con la lista obtenida de `taskService.getIdentityLinksForTask(taskId)`.
* Si no hay intersección (el usuario no pertenece al `candidateGroup` o carece de permisos de administrador), lanza una `IllegalStateException("HTTP 403 - FORBIDDEN")`.

### 3. [ARQ-017-03] Trazabilidad: Inyección de Evento `TASK_AUTO_CLAIMED` (CA-04)
El CQRS exige inmutabilidad forense absoluta.
* **Acción:** En `AutoClaimService.java`, inyecta el `FormEventRepository`.
* Justo después de ejecutar `taskService.claim(taskId, userId)`, construye un `FormEvent` con tipo `EventType.TASK_AUTO_CLAIMED` (Asegúrate de que este Enum exista).
* Guarda el evento en la bóveda: `formEventRepository.save(autoClaimEvent);`

### 4. [ARQ-017-04] Resiliencia: Retry Saga Compensatorio (CA-10)
* **Acción:** En `CamundaCompletionAdapter.java` o directamente sobre el llamado en `FormCompletionService.java`, asegúrate de incluir la anotación de resiliencia de Spring Retry:
  `@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))`
* Asegúrate de que `@EnableRetry` esté activo en la configuración principal.

---

## 🚦 Criterios de Aceptación (Definición de Terminado)
1. Ejecutar `mvn clean compile` sin errores.
2. Levantar la aplicación localmente (`SpringApplication.run`) sin que explote por `Ambiguous handler methods mapped`.
3. Verificación de que el `AutoClaimService` graba su evento en base de datos.
4. Generar reporte `approval_request_backend.md` documentando los cambios.

**Instrucción Final:** Ejecuta estos pasos de forma estricta. No uses Mocks bajo ninguna circunstancia. Reporta cuando hayas concluido.
