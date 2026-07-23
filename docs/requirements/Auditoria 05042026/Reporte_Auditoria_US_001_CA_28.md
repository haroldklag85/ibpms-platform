# Reporte de Auditoría Estricta: US-001 (CA-28)
## Prevención de Condición de Carrera en "Atender Siguiente"

### 🗺️ Ruta Estructural Navegada (Top-Down)
1. `view_file: docs/requirements/epics/epic_A_motor_core.md` (Lectura comprensiva del criterio de aceptación CA-28 enfocado en bloqueo pesimista en DB usando `FOR UPDATE SKIP LOCKED`).
2. `list_dir: backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/` (Exploración jerárquica de controladores donde se ubica el enrutamiento).
3. `view_file: WorkdeskAttendNextController.java` (Evaluación de la llamada al repositorio que asigna las tareas y orquesta websockets).
4. `view_file: WorkdeskProjectionRepository.java` (Verificación en la capa de persistencia de las anotaciones y queries JPQL/Nativas para confirmar el Lock en BD).

### 🏷️ Archivos Etiquetados con Éxito (`@Traceability`)
*   `WorkdeskProjectionRepository.java`: Inyectada la etiqueta estandarizada `// @Traceability(US = "US-001", CA = {"CA-16", "CA-21", "CA-28"})` sobre la query nativa de Postgres que aplica el `SKIP LOCKED`.
*   `WorkdeskAttendNextController.java`: Inyectada la etiqueta `// @Traceability(US = "US-001", CA = {"CA-28"})` sobre el endpoint `@PostMapping("/attend-next")`.

### 🚨 Brechas de Implementación y Deuda Técnica Detectada
Durante el análisis de cumplimiento de la asignación atómica se encontró un **Antipatrón Arquitectónico Crítico** que vulnera los principios de la Arquitectura Hexagonal y de Clean Architecture adoptados en el proyecto:

1. **Sangrado de Capas (Transaction/Business Bleed):** La anotación `@Transactional`, junto con las reglas de negocio, resolución de skills, mutación de asignación en base de datos (`.save()`), y emisión de eventos asíncronos vía WebSocket (Spring `SimpMessagingTemplate`), fueron programadas y acopladas directamente en el `WorkdeskAttendNextController.java` (Capa de Adaptador de Entrada/Infraestructura Web).
2. **Impacto en Rendimiento:** Al iniciar la transacción de BD dentro del Controlador HTTP, el ciclo de vida del *Connection Pool* queda secuestrado por el tiempo de vida de la petición TCP/HTTP. Si existe degradación en la red del cliente enviando los acks, el hilo de BD permanece bloqueado innecesariamente. 
3. **Imposibilidad de Reuso:** Si se deseara invocar "Atender Siguiente" desde un proceso batch, desde una CLI administrativa o desde otra saga de eventos internos (RabbitMQ), sería imposible ya que la lógica está acoplada al `ResponseEntity` y a la petición HTTP originada en el cliente web.

**Plan de Remediación:**
La lógica contenida en `attendNext` y `skipAndNext` debe refactorizarse inmediatamente, extrayéndose hacia un caso de uso de Dominio (ej. `WorkdeskRoutingService.java` en la capa `application/service`), delegando al controlador web su única función original: desempaquetar parámetros HTTP y devolver códigos de estado.

El hallazgo se encuentra formalizado y sumariado en `task.md`.
