# Informe de Estado y Avance Real Proyecto IBPMS
**Fecha de Auditoría:** 09-06-2026
**Auditor:** PM-IA
**Enfoque:** Auditoría técnica quirúrgica de infraestructura, backend, frontend y matriz de estado. QA excluido.

---

## 1. ESTADO GLOBAL DE DESARROLLO (SSOT: coverage_matrix.md)

De un total de **56 Historias de Usuario (US)** mapeadas, el estado real es:

*   ✅ **COMPLETADAS (12 US - 21%):**
    *   US-000, US-001, US-003, US-005, US-028, US-029, US-034, US-038, US-039, US-043, US-048, US-051
*   🔨 **EN CONSTRUCCIÓN (7 US):**
    *   US-002 (~92%), US-004 (~71%), US-007 (~94%), US-017 (~95%), US-025 (~11%), US-027 (~65%), US-030 (~85%)
*   🔶 **EN PROGRESO (1 US):**
    *   US-036 (Marcada ~40%, pero Back+Front están al 100% funcional)
*   🔨 **SCAFFOLDING (5 US):**
    *   US-008 (~10%), US-011, US-021, US-035, US-045
*   ❌ **PENDIENTES (31 US - 55%):**
    *   No iniciadas.

**Hallazgos Críticos en Matriz:**
1.  **Falsos Positivos Detectados:** US-008 fue declarada "Operativa" previamente, pero solo tiene un 10% de scaffolding con datos mockeados.
2.  **Conflictos de Consistencia:** Contradicciones en los encabezados (ej. US-001 dice "EN CONSTRUCCIÓN 86%" pero sus CA suman 30/30=100%).
3.  **Conflictos Git:** Múltiples conflictos (OBS-1 en US-005 CA-68 Entity/DDL mismatch, duplicación en `TaskClaimApiController`).

---

## 2. INFRAESTRUCTURA Y BASE DE DATOS (Auditoría de Entorno)

La auditoría forense de los archivos de configuración y Docker revela problemas de estabilidad estructural:

### 2.1 Conflictos de Puertos
*   `docker-compose.yml` (servicio `ibpms-postgres`) y `docker-compose.e2e.yml` (servicio `postgres-e2e`) **ambos intentan exponer el puerto host 5433**. No pueden ejecutarse simultáneamente.

### 2.2 Riesgo de Gestión Dual de Esquema (CRÍTICO)
*   **Liquibase** está activado y gestiona 59 migraciones en `db.changelog-master.yaml`.
*   Sin embargo, en `application.yml`, la propiedad `spring.jpa.hibernate.ddl-auto: update` está activa tanto en el perfil `default` como en `uat`.
*   **Consecuencia:** Hibernate intentará auto-crear/alterar tablas sobre las que Liquibase ya gestiona, generando una colisión inminente de esquemas.

### 2.3 Estado de Tablas Críticas
*   ✅ **`form_event_store` (CQRS):** Existe. Creada por `sprint3/001_create_form_event_store.sql`. Tiene triggers de inmutabilidad (`trg_prevent_event_update`, `trg_prevent_event_delete`).
*   ✅ **`task_drafts`:** Existe. Creada por `sprint3/002_create_task_drafts.sql`.

### 2.4 Residuos Legacy y Semillas
*   Existen archivos SQL con sintaxis MySQL incompatibles con Postgres en `infra/db/migrations/`.
*   La semilla de desarrollo (`seed-dev.sql`) tiene el modo de inicialización en `never`, por lo que **no se carga automáticamente** a pesar de estar configurada.
*   `seed-e2e.sql` contiene comandos DDL (`DROP TABLE`/`CREATE TABLE`) que compiten con el estado de Liquibase.

---

## 3. AUDITORÍA DE FRONTEND (Vue 3 / TypeScript)

### 3.1 Violaciones de Arquitectura (Bypass de API Client)
El proyecto cuenta con un cliente centralizado (`apiClient.ts`) con 52 métodos mapeados que manejan autenticación y reintentos. Sin embargo, **4 archivos violan esta regla importando `axios` directamente**:
*   `AgileBacklogList.vue`
*   `agileStore.ts`
*   `useIntakeTriageStore.ts`
*   `AgileHub.vue`
*   Adicionalmente, hay llamadas nativas `fetch` directas en `apiClient.ts` y `useIntegrationStore.ts` para la ruta `/api/v1/ai/copilot/session`.

### 3.2 Presencia de Mocks en Producción (Deuda Técnica Crítica)
Se identificaron componentes en producción que aún dependen de datos quemados (mocks) en lugar de conexiones reales:
*   `FormRenderer.vue`: Datos quemados en opciones de autocompletado (`mock1`, `mock2`) y fallback a `'mock-task'`.
*   `DmnGridManual.vue`: Fila mockeada `mock-initial-1`.
*   `TaskViewerModal.vue`: Textos visibles en UI como `"Click para Mock"`.
*   `IdentityGovernance.vue`: Depende fuertemente de mocks locales para el dashboard.

### 3.3 Problemas de Escabilidad (Archivos Masivos)
Existen 3 archivos monolíticos que superan los límites de mantenibilidad razonable y deben ser descompuestos:
1.  `BpmnDesigner.vue` (126 KB, ~3000 líneas)
2.  `FormDesigner.vue` (115 KB, ~2800 líneas)
3.  `IdentityGovernance.vue` (90 KB, ~2000 líneas)

### 3.4 Dependencias y Módulos
*   **Moment.js** está presente en dependencias de producción, a pesar de estar obsoleto (se recomienda migrar a `dayjs`).
*   `vue-virtual-scroller` está anclado a una versión Beta.
*   `rbacStore.js` está escrito en JavaScript puro, rompiendo el estándar TypeScript del resto de stores.

---

## 4. AUDITORÍA DE BACKEND (Java 17 / Spring Boot)

*Nota: La ejecución de la búsqueda exhaustiva de rutas `@GetMapping`/`@PostMapping` no se finalizó debido a la masividad de archivos, pero el análisis de controladores confirma:*

*   Más de **65 controladores REST** mapeados (`@RestController`) distribuidos entre `com.ibpms.core` y `com.ibpms.poc`.
*   Las rutas mapeadas en el Frontend (`apiClient.ts`) apuntan hacia los controladores de backend, pero la falta de un contrato único en el pasado permitió discrepancias. Las rutas ahora están documentadas en `API_CONTRACTS.md` (con 19 endpoints principales identificados, de los cuales solo 4 están 100% verificados).

---

## CONCLUSIÓN TÉCNICA
El proyecto no está en un 80% como algunas métricas de los agentes sugerían, sino en un **21% de cierre real verificado**. 
El caos de integración es producto de tres factores que ahora están bajo control del PM-IA:
1.  **Falsos positivos (Mocks):** Agentes Frontend construyendo UI con datos falsos sin conectar al Backend.
2.  **Choque de configuración:** Infraestructura (Hibernate vs Liquibase) pisándose entre sí.
3.  **Falta de contratos:** Frontend saltándose los clientes oficiales y apuntando a rutas inexactas.

El **Sprint PM-01** (Fase de Estabilización) está diseñado exactamente para resolver estas anomalías antes de permitir nuevo desarrollo de features.
