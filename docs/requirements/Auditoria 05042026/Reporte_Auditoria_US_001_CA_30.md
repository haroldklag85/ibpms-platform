# Reporte de Auditoría Estricta: US-001 (CA-30)
## Rate Limiting para la Grilla del Workdesk

### 🗺️ Ruta Estructural Navegada (Top-Down)
1. `view_file: docs/requirements/v1_user_stories_index.md` (Para localizar la Épica A de la US-001).
2. `view_file: docs/requirements/epics/epic_A_motor_core.md` (Para extraer semánticamente el CA-30: Límite de 60 rq/min por usuario autenticado con mensaje específico de 429).
3. `list_dir: backend/` -> `backend/ibpms-core/src/main/java/` -> `com/ibpms/poc/` (Exploración jerárquica del proyecto Java).
4. `list_dir: com/ibpms/poc/infrastructure/web/` (Exploración de la capa Hexagonal de adaptadores de entrada).
5. `view_file: WorkdeskQueryController.java` (Para auditar la implementación del endpoint `GET /api/v1/workdesk/tasks` o `/global-inbox` que expone la grilla).

### 🏷️ Archivos Etiquetados con Éxito (`@Traceability`)
*   `WorkdeskQueryController.java`: Se corrigió e inyectó el formato estandarizado `// @Traceability(US = "US-001", CA = {"CA-30"})` sobre la declaración inicial de Bucket4j en el constructor y antes de la validación transaccional (`tryConsume`).

### 🚨 Brechas de Implementación y Deuda Técnica Arquitectónica
La auditoría estructurada reveló desviaciones **Críticas** de arquitectura de cara al cumplimiento del **CA-30**:

1. **Violación de Aislamiento de Rate Limit (Falso Positivo DoS):** El criterio de aceptación exige explícitamente "máximo 60 peticiones por minuto **por usuario autenticado**". Sin embargo, el objeto `Bucket` de la librería Bucket4j fue instanciado como variable de clase global en el Constructor del Controlador (`@RestController`). Al ser un singleton por defecto en Spring, este `bucket` aglomera el conteo globalmente. Si el Cliente A consume 40 tokens y el Cliente B consume 25, el Cliente B sufrirá un bloqueo 429 inmediato, paralizando la aplicación para todos tras cruzar los 60 combinados. El estado no está aislado mediante una estructura `ConcurrentHashMap` de tokens por `userId` ni delegado a Redis.
2. **Carencia de Contrato de Respuesta Exigido:** El escenario dicta que al superarse el límite, el backend retornará `HTTP 429 Too Many Requests` **con el mensaje: "Has realizado demasiadas consultas. Espera unos segundos antes de intentarlo de nuevo."**. El código en la línea 56 hace `return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();`, despachando el HTTP Code desnudo, sin el body explícito requerido, lo que imposibilita la renderización estandarizada del modal de advertencia al operario.

Ambas violaciones han sido consolidadas formalmente en `task.md`.
