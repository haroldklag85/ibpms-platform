# Handoff INFRA/BD — US-039 | DT-039-02: Validación de Dependencias y Configuración de Caché

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | Cierre Deuda Técnica — Iteración 3 |
| **Sprint** | 6 |
| **Rama Git** | `sprint-6` |
| **User Story** | US-039 — Formulario Genérico Base |
| **Deuda Técnica** | DT-039-02 — Caffeine cache provider ausente |
| **SSOT** | `docs/requirements/v1_user_stories_index.md` → `docs/requirements/epics/epic_B_formularios_bpmn.md` |
| **Flujo de Trabajo** | **INFRA/BD (pre-validación)** → Backend → QA |

---

## 2. Alineación Arquitectónica y ADRs

### ADRs Aplicables
- **ADR-009 (PostgreSQL + pgvector):** La caché L1 (Caffeine) NO reemplaza la BD. Solo actúa como buffer in-process para evitar queries repetitivas a la tabla `ibpms_roles`.
- **ADR-001 (Hexagonal):** La configuración de caché es infraestructura pura. No se introduce ninguna tabla nueva ni migración Liquibase para este cambio.

### Contexto del Problema
El equipo Backend agregará la librería Caffeine al `pom.xml` y configurará `application.yml`. El equipo INFRA/BD debe verificar que:
1. La infraestructura Docker soporta la nueva dependencia sin conflictos.
2. No hay impacto en los contenedores existentes (`docker-compose.yml`).
3. La tabla `ibpms_roles` (fuente de datos para la caché) está correctamente poblada con los seeds VIP.

---

## 3. Rutas Exactas y Contexto Preexistente

### Archivos a verificar (NO modificar)

| Archivo | Propósito de Verificación |
|---------|--------------------------|
| `docker-compose.yml` | Confirmar que no se requiere servicio de caché externo (Redis) para este cambio |
| `docker-compose.e2e.yml` | Confirmar que los tests E2E no dependen de Redis cache |
| `backend/ibpms-core/src/main/resources/db/changelog/sprint6/21-us039-generic-form-schema.sql` | Verificar que el seed de roles VIP existe |

### Estado actual de la tabla `ibpms_roles`
La migración `21-us039-generic-form-schema.sql` contiene:
```sql
-- L9: ALTER TABLE ibpms_roles ADD COLUMN IF NOT EXISTS is_vip_restricted BOOLEAN DEFAULT FALSE;
-- L13-18: UPDATE seeds para ALTA_DIRECCION, APROBADOR_FINANCIERO, SELLO_LEGAL
```

---

## 4. Checklist de Validación INFRA/BD

| # | Verificación | Cómo Validar | Resultado Esperado |
|---|-------------|-------------|-------------------|
| 1 | Caffeine es in-process (no requiere servicio externo) | Confirmar que `docker-compose.yml` NO necesita nuevo servicio `cache` | ✅ Sin cambios en docker-compose |
| 2 | Seeds de roles VIP en BD | Ejecutar: `SELECT name, is_vip_restricted FROM ibpms_roles WHERE is_vip_restricted = true;` | 3 filas: `ALTA_DIRECCION`, `APROBADOR_FINANCIERO`, `SELLO_LEGAL` |
| 3 | No hay conflicto con Redis | Buscar `spring.data.redis` o `spring.redis` en `application.yml` | Si existe Redis, validar que `spring.cache.type: caffeine` NO entre en conflicto. Si Redis se usa para sesiones/otro, Caffeine para caché de dominio es compatible. |
| 4 | La migración Liquibase es idempotente | Verificar `IF NOT EXISTS` en el ALTER de `is_vip_restricted` | `ADD COLUMN IF NOT EXISTS` presente |

---

## 5. Escenario de Riesgo

> [!WARNING]
> **Riesgo:** Si el proyecto ya usa Redis como `CacheManager` para algún otro cache, agregar `spring.cache.type: caffeine` podría sobrescribir el provider global. En ese caso, se necesita un `CacheManager` compuesto (Caffeine para `vipRoles`, Redis para lo demás).
>
> **Mitigación:** Verificar si existe `spring.data.redis` o alguna clase `RedisCacheConfig` antes de aplicar. Si existe, notificar al Arquitecto Líder para diseñar un `CompositeCacheManager`.

---

## 6. Mensaje de Despacho

> **Instrucciones para el Agente INFRA/BD:**
>
> Lee este documento completo. Tu tarea es de **pre-validación**, NO de implementación.
>
> 1. Ejecuta los 4 checkpoints de la sección 4.
> 2. Si el checkpoint #3 detecta Redis existente, **DETENTE** e informa al Arquitecto Líder antes de que Backend proceda.
> 3. Si todos los checkpoints son ✅, confirma al equipo Backend que puede proceder con su handoff.
> 4. **NO modificar** ningún archivo de infraestructura ni base de datos.
>
> **Rama:** `sprint-6`. PROHIBIDO trabajar en `main`.
