# Reporte de Auditoría Estricta: US-001 (CA-29)
## Contadores en Filtros Facetados del Workdesk

### 🗺️ Ruta Estructural Navegada (Top-Down)
1. `view_file: docs/requirements/v1_user_stories_index.md` y `docs/requirements/epics/epic_A_motor_core.md` (Para extraer el criterio de CA-29: Contadores por Origen y Estado incrustados en la respuesta de la grilla principal).
2. `list_dir: backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/` (Exploración jerárquica buscando los servicios del dominio de Workdesk).
3. `view_file: WorkdeskQueryService.java` (Evaluación de la inyección de repositorio y la extracción real de contadores).
4. `list_dir: frontend/src/` (Verificación del frontend).
5. `view_file: Workdesk.vue` (Auditoría visual de los Chips/Facetas en la UI).

### 🏷️ Archivos Etiquetados con Éxito (`@Traceability`)
*   `WorkdeskQueryService.java`: Inyectada la etiqueta `// @Traceability(US = "US-001", CA = {"CA-29"})` sobre el método `getFacets()`.
*   `frontend/src/views/Workdesk.vue`: Inyectada la etiqueta `<!-- @Traceability(US = "US-001", CA = {"CA-22", "CA-29"}) Filtros Facetados (Chips) -->` en el div contenedor de facetas.

### 🚨 Brechas de Implementación y Deuda Técnica Detectada
1. **Desacoplamiento Estructural no Autorizado:** El CA-29 especifica "el Backend retornará los contadores como parte del response de la grilla (CA-20), en un objeto adicional: `facets:`". Sin embargo, el backend exilió esto a un endpoint completamente separado (`/global-inbox/facets`), forzando al frontend a hacer dos peticiones HTTP concurrentes.
2. **Ausencia de Contadores por Origen:** El Criterio de Aceptación es explícito exigiendo contadores tanto por Status (`PENDING`, `OVERDUE`) como por Origen (`BPMN`, `KANBAN`, `GANTT`). La consulta del servicio en backend llama a `countByStatusPerTenant`, limitándose exclusivamente a mapear el Status y omitiendo el origen.
3. **Manejo de Caché Deficiente:** El `@Cacheable` en `getFacets` utiliza una llave hardcodeada (`facets_ + tenantId`) y carece de un `@CacheEvict` claro vinculado a la mutación de estado de tareas, lo que provocará que el frontend renderice contadores fantasma u obsoletos.

El CA-29 se marca con hallazgos formales documentados en `task.md`.
