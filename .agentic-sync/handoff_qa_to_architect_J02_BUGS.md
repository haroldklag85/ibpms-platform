# Handoff QA → Arquitecto Líder: Bugs Bloqueantes J-02 Sprint PM-01

## Metadata
- **Origen:** Certificación UAT Manual — Journey J-02 (BPMN + Forms)
- **Sprint:** PM-01 (Fase de Estabilización)
- **Agente QA:** Antigravity
- **Tester Humano:** Harold
- **Fecha:** 2026-06-24
- **Prioridad:** 🔴 P0 — BLOQUEANTE
- **Estado de Certificación:** PAUSADA — Misiones 2 y 3 bloqueadas

---

## Bug Crítico: BUG-J02-003 — Mock en Catálogo de Formularios

### Síntoma Reportado por el Tester
Harold reportó que el "Gestor de Formularios" (`/admin/modeler/forms`) muestra 3 formularios que **NO corresponden** a los formularios reales almacenados en PostgreSQL. Los formularios reales de la BD no aparecen en el catálogo. No se evidenció petición HTTP GET al backend en DevTools.

### Diagnóstico Confirmado por Análisis Forense del Código

| Aspecto | Estado |
|---------|--------|
| Frontend hace HTTP call real? | ✅ SÍ — `GET /api/v1/forms` |
| Frontend tiene datos hardcodeados? | ✅ NO — completamente limpio |
| Frontend tiene fallback a mock? | ✅ NO — si la API falla, muestra error |
| Backend sirve datos reales de la BD? | ❌ **NO — sirve datos hardcodeados en memoria** |
| Ubicación del mock | `FormDirectoryService.java` líneas **13-17** |
| Nombre de la variable | `mockDirectory` (literalmente "mock") |

### Ruta del Código (Call Stack Completo)

```
FormList.vue (onMounted, L194)
  → fetchForms() (L147-158)
    → integrationStore.get('/forms') (L152)
      → apiClient.get('/api/v1/forms') 
        → FormDirectoryController.java (L26-30)
          → FormDirectoryService.searchForms(search) (L19-29)
            → Retorna mockDirectory (L13-17) ← 🚨 AQUÍ ESTÁ EL MOCK
```

### Archivos Afectados

| Archivo | Líneas | Acción Requerida |
|---------|--------|------------------|
| `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/form/FormDirectoryService.java` | L12-28 | **REFACTORIZAR:** Eliminar `mockDirectory` y reemplazar con consulta real a PostgreSQL vía repository JPA |
| `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/api/form/FormDirectoryController.java` | L15-31 | Revisar — puede necesitar ajuste si se cambia el servicio |

### Pista Crítica para la Corrección
Ya existe un endpoint que SÍ funciona con datos reales de la BD:

| Endpoint | Controller | Service | ¿Consulta BD? |
|----------|-----------|---------|----------------|
| `GET /api/v1/forms` (usado por catálogo) | `FormDirectoryController` | `FormDirectoryService` | ❌ NO — usa `mockDirectory` |
| `GET /api/v1/forms/active` (usado por BPMN dropdown) | `FormCatalogController` | `FormDesignService.listarCatalogo()` | ✅ SÍ — consulta real |

**Opción de corrección sugerida:** `FormDirectoryService.searchForms()` debería usar la misma lógica que `FormDesignService.listarCatalogo()` (inyectar el repository JPA y ejecutar query real contra la tabla `form_definitions` o equivalente).

### Comentario del Desarrollador Original
En `FormDirectoryService.java:12`:
```java
// Estructura en memoria según requerimiento de Misión (Evasión de BD compleja para acelerar Boot)
```
> Este comentario confirma que fue una decisión deliberada de atajo, no un error involuntario.

---

## Bugs Secundarios (No Bloqueantes — P3)

### BUG-J02-001: Ruta `/admin/modeler/` retorna 404
- **Archivo:** `frontend/src/router/index.ts` (L70-98, L216-220)
- **Causa:** Las 5 rutas del modeler son siblings planas, no hay ruta padre `/admin/modeler`
- **Fix sugerido:** Agregar redirect `{ path: 'admin/modeler', redirect: '/admin/modeler/bpmn' }` en el router

### BUG-J02-002: Link roto en BpmnDesigner para Call Activities
- **Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue:4197`
- **Causa:** `window.open('/admin/modeler?processId=...')` apunta a ruta inexistente
- **Fix sugerido:** Cambiar a `window.open('/admin/modeler/bpmn?processId=...')`

---

## Impacto en la Certificación

| Misión | Estado | Impacto del Bug |
|--------|--------|-----------------|
| M0 — Infraestructura | ✅ PASS | No afectada |
| M1 — Login/Navegación | ⚠️ PASS con observaciones | BUG-001, BUG-002 (no bloqueantes) |
| **M2 — Catálogo Forms** | **🔴 FAIL** | **BUG-003 (P0 Mock) — BLOQUEANTE** |
| **M3 — Crear Formulario** | **🟠 BLOQUEADA** | Depende de M2 |
| M4 — BPMN Canvas | ⏳ Pendiente | Puede continuar (no depende de M2) |
| M5 — Dropdown FormKey | ⏳ Pendiente | Usa `/api/v1/forms/active` (endpoint REAL) — puede continuar |
| M6 — Persistencia | ⏳ Pendiente | Puede continuar parcialmente |
| M7 — RBAC | ⏳ Pendiente | Puede continuar |

---

## Instrucciones para el Arquitecto

1. **Corregir BUG-J02-003** (P0): Eliminar el mock de `FormDirectoryService.java` y conectar a la BD real
2. **Corregir BUG-J02-001** (P3): Agregar redirect para `/admin/modeler/`
3. **Corregir BUG-J02-002** (P3): Arreglar URL en `BpmnDesigner.vue:4197`
4. **Después de corregir:** Harold y el Agente QA retomarán la certificación desde Misión 2

---

## Decisión Pendiente para Harold

Mientras el Arquitecto corrige BUG-J02-003, la certificación puede **continuar parcialmente** con las Misiones 4-6 (BPMN) ya que el dropdown FormKey usa el endpoint `/api/v1/forms/active` que SÍ consulta la BD real. Las Misiones 2-3 quedan BLOQUEADAS hasta el fix.
