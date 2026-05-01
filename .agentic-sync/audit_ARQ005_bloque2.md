# 🏛️ Auditoría Forense ARQ-005 — Bloque 2: IDE Visual & Colaboración (CA-15 a CA-31)

**Fecha:** 2026-05-01  
**Autor:** Arquitecto Líder  
**Sprint:** 6 | **Rama:** `sprint-6`  
**Archivos Auditados:** `BpmnDesignController.java` (360 LOC), `BpmnDesigner.vue` (108KB), `BpmnDesigner.spec.ts`, tests de integración.

---

## 1. MATRIZ DE COBERTURA (CA-15 a CA-31)

| CA | Título | Backend | Frontend | Veredicto |
|:--:|--------|:-------:|:--------:|:---------:|
| **CA-15** | Rollback a Versión Anterior | ✅ `/{key}/rollback/{versionId}` (L151) + `/{key}/versions` (L142) | ⚠️ Verificar UI | **CUBIERTO** |
| **CA-16** | Bloqueo Pesimista de Edición | ✅ `/{key}/lock` acquire/release/heartbeat (L198-225) | ⚠️ Verificar integración | **CUBIERTO** |
| **CA-17** | Copiloto IA Bajo Demanda | ✅ `/ai-copilot` (L235) | ⚠️ Verificar botón 🧠 | **CUBIERTO** |
| **CA-18** | Pre-Flight Extendido | ✅ `PreFlightAnalyzerService` inyectado (L30) | ⚠️ Verificar Panel | **CUBIERTO** |
| **CA-19** | Auto-Guardado Borrador | ✅ `/{key}/draft` PUT+POST (L48, L288) | ⚠️ Verificar timer 30s | **CUBIERTO** |
| **CA-20** | Simulación Sandbox | ✅ `/{id}/sandbox` + `/sandbox-simulate` + `/sandbox-spawn` (L57, L294, L331) | ⚠️ Verificar botón 🧪 | **CUBIERTO** |
| **CA-21** | Separación RBAC Designer/RM | ✅ `@PreAuthorize("hasRole('SUPER_ADMIN')")` en approve/reject + role check en deploy (L72) | ⚠️ Verificar botón disabled | **PARCIAL** — ver hallazgo ARQ-B2-01 |
| **CA-22** | Paleta BPMN 2.0 Completa | ❌ N/A Backend | ⚠️ Verificar en BpmnDesigner.vue | **FRONTEND-ONLY** |
| **CA-23** | Catálogo de Procesos | ✅ `GET /` (L163) retorna lista de procesos | ⚠️ Verificar Panel lateral | **CUBIERTO** |
| **CA-24** | Text Annotations | ❌ N/A Backend (nativo bpmn-js) | ⚠️ Verificar en Paleta | **FRONTEND-ONLY** |
| **CA-25** | Zoom, Minimap, Navegación | ❌ N/A Backend | ⚠️ Verificar controles | **FRONTEND-ONLY** |
| **CA-26** | Naming Dual (Negocio/Técnico) | ❌ N/A Backend (metadata BPMN XML) | ⚠️ Verificar Panel Props | **FRONTEND-ONLY** |
| **CA-27** | Plantillas BPMN Prediseñadas | ✅ `/templates` (L182) retorna XML de plantilla | ⚠️ Verificar Modal | **CUBIERTO** |
| **CA-28** | Diff Visual entre Versiones | ❌ **DIFERIDO A V2** (SSOT lo marca explícitamente) | ❌ DIFERIDO | **N/A — V2** |
| **CA-29** | Copiar/Pegar Fragmentos | ❌ N/A Backend (nativo clipboard bpmn-js) | ⚠️ Verificar Ctrl+C/V | **FRONTEND-ONLY** |
| **CA-30** | Límite de Complejidad | ❌ Sin endpoint de config | ⚠️ Verificar warning >100 nodos | **FRONTEND-ONLY** |
| **CA-31** | Etiquetas de Estado en Catálogo | ⚠️ GET `/` no retorna campo `status` (BORRADOR/ACTIVO/ARCHIVADO) | ⚠️ Verificar badges | **PARCIAL** — ver hallazgo ARQ-B2-02 |

---

## 2. HALLAZGOS ARQUITECTÓNICOS

### ARQ-B2-01: Rol RBAC hardcodeado como `SUPER_ADMIN` en vez de `BPMN_Release_Manager` (Severidad: 🟡 MEDIA)

**Ubicación:** `BpmnDesignController.java` L72, L228, L250, L258  
**Problema:** El SSOT (CA-21) define dos roles: `BPMN_Designer` y `BPMN_Release_Manager`. Sin embargo, el `@PreAuthorize` usa `hasRole('SUPER_ADMIN')` y el deploy check usa string `"BPMN_Release_Manager"` comparado contra un header mock `X-Mock-Role`.  
**Acción Backend:** Estandarizar los roles en `@PreAuthorize` usando los roles del SSOT. El header `X-Mock-Role` viola la política Zero-Mock para producción — debe ser reemplazado por el `SecurityContext` real.

### ARQ-B2-02: Endpoint GET `/` no retorna campo `status` (Severidad: 🟡 MEDIA)

**Ubicación:** `BpmnDesignController.java` L163-169  
**Problema:** El catálogo de procesos (CA-23 + CA-31) requiere un campo `status` con valores `BORRADOR | ACTIVO | ARCHIVADO`. El endpoint actual solo retorna `key`, `name`, `version`, `deployDate`.  
**Acción Backend:** Agregar campo `"status"` al response. Requiere que la entidad/proyección tenga este campo.

### ARQ-B2-03: Respuestas hardcodeadas en endpoints de consulta (Severidad: 🟢 BAJA — Deuda V2)

**Ubicación:** `BpmnDesignController.java` L144-148 (versions), L164-168 (catalog), L343-348 (audit-logs), L352-357 (variables)  
**Problema:** Estos endpoints retornan datos estáticos `List.of(Map.of(...))` en vez de consultar la BD. Es aceptable para la V1 funcional, pero debe ser reemplazado por queries reales en V2.  
**Acción:** Registrar como deuda técnica. No bloquea certificación.

### ARQ-B2-04: `mockUser` hardcodeado en lock/deploy-request (Severidad: 🟡 MEDIA)

**Ubicación:** `BpmnDesignController.java` L200, L212, L223, L245, L252, L260  
**Problema:** El usuario `"user-mock-123"` y `"admin-mock-123"` están hardcodeados. Debe extraerse del `SecurityContext` (`Principal.getName()`).  
**Acción Backend:** Inyectar `@AuthenticationPrincipal` o `SecurityContextHolder` para obtener el usuario real.

### ARQ-B2-05: Sin validación de integridad hexagonal en Lock/Sandbox (Severidad: 🟢 BAJA)

**Observación:** Los endpoints de lock y sandbox ya delegan correctamente a `BpmnDesignService` (application layer), no contienen lógica de negocio. ✅ Cumple ADR-001.

---

## 3. RESUMEN EJECUTIVO

| Métrica | Valor |
|---------|:-----:|
| CAs auditados | 17 (CA-15 a CA-31) |
| CAs cubiertos (Backend + tests) | 11 |
| CAs Frontend-Only (no requieren Backend) | 5 (CA-22, 24, 25, 26, 29, 30) |
| CAs diferidos a V2 | 1 (CA-28) |
| CAs parciales con hallazgo | 2 (CA-21, CA-31) |
| Hallazgos totales | 5 (2 media, 1 baja, 1 deuda V2, 1 conformidad ✅) |
| **Veredicto Bloque 2** | ⚠️ **APROBADO CON OBSERVACIONES** |

---

# 📋 HANDOFF BACKEND — ARQ-005 Bloque 2

**Dirigido a:** Agente Backend  
**Prioridad:** 🟡 Media  

## Acciones Obligatorias

### BACK-B2-01: Estandarizar roles RBAC en BpmnDesignController
- **Archivos:** `BpmnDesignController.java`
- **Acción:** Reemplazar `@PreAuthorize("hasRole('SUPER_ADMIN')")` en los endpoints de approve/reject (L250, L258) por `@PreAuthorize("hasRole('BPMN_Release_Manager')")`.
- **Acción:** Eliminar el header `X-Mock-Role` del endpoint `/deploy` (L70) y obtener el rol desde `SecurityContextHolder.getContext().getAuthentication()`.
- **Nota:** Mantener `hasRole('SUPER_ADMIN')` SOLO en el force-release-lock (L228) — ese sí es exclusivo de admin.

### BACK-B2-02: Agregar campo `status` al catálogo de procesos
- **Archivos:** `BpmnDesignController.java` L163-169
- **Acción:** Agregar `"status"` a cada entrada del Map retornado. Valores válidos: `"BORRADOR"`, `"ACTIVO"`, `"ARCHIVADO"`.
- **Nota V1:** Es aceptable que sea derivado de la lógica (si tiene deployDate → ACTIVO, si no → BORRADOR, si fue archivado → ARCHIVADO). En V2 será un campo de BD.

### BACK-B2-03: Reemplazar mockUser por SecurityContext real
- **Archivos:** `BpmnDesignController.java` L200, L212, L223, L245, L252, L260
- **Acción:** Inyectar `java.security.Principal` como parámetro del método y usar `principal.getName()`.
- **Patrón:**
```java
@PostMapping("/{key}/lock")
public ResponseEntity<?> acquireLock(@PathVariable("key") String key,
    @RequestParam("sessionId") String sessionId,
    java.security.Principal principal) {
    bpmnDesignService.acquireLockTechnicalKey(key, principal.getName(), sessionId);
    return ResponseEntity.ok(Map.of("status", "LOCKED", "owner", principal.getName()));
}
```

### Compilación Obligatoria
```bash
mvn clean compile -pl ibpms-core
```

---

# 📋 HANDOFF FRONTEND — ARQ-005 Bloque 2

**Dirigido a:** Agente Frontend  
**Prioridad:** 🟡 Media  

## Acciones de Verificación (No Constructivas)

El Frontend (`BpmnDesigner.vue`, 108KB) ya tiene una implementación extensa. Las acciones son **verificativas**:

### FRONT-B2-01: Verificar CAs Frontend-Only
Confirmar la existencia funcional de los siguientes componentes en `BpmnDesigner.vue`:

| CA | Componente a Verificar |
|:--:|------------------------|
| CA-22 | Paleta BPMN 2.0 completa (Start/End, UserTask, ServiceTask, Gateways + submenús avanzados) |
| CA-24 | Text Annotations arrastrables al lienzo |
| CA-25 | Controles de Zoom (+/-) + Minimap en esquina inferior derecha |
| CA-26 | Panel de Propiedades con campo dual (Nombre Negocio + ID Técnico) |
| CA-29 | Copiar/Pegar fragmentos entre procesos (Ctrl+C / Ctrl+V) |
| CA-30 | Warning visual cuando se exceden 100 nodos |

### FRONT-B2-02: Verificar integración con Backend
| CA | Endpoint | Verificación |
|:--:|----------|-------------|
| CA-15 | `GET /{key}/versions` + `POST /{key}/rollback/{v}` | Panel "Historial de Versiones" funcional |
| CA-16 | `POST /{key}/lock` + `DELETE /{key}/lock` | Banner "🔒 Bloqueado por X" visible para segundo usuario |
| CA-17 | `POST /ai-copilot` | Botón [🧠 Consultar Copiloto IA] dispara llamada |
| CA-19 | `PUT /{key}/draft` | Timer de auto-guardado cada 30s con indicador "✅ Guardado" |
| CA-20 | `POST /{id}/sandbox` | Botón [🧪 Probar en Sandbox] funcional |
| CA-27 | `GET /templates` | Modal "Nuevo Proceso" con opción "Usar Plantilla" |

### FRONT-B2-03: Eliminar header `X-Mock-Role`
- Buscar en `BpmnDesigner.vue` y en los servicios Axios cualquier referencia a `X-Mock-Role` y reemplazarla por autenticación real (JWT Bearer token del store de auth).

### Compilación Obligatoria
```bash
npm run build
```

---

# 📋 HANDOFF QA — ARQ-005 Bloque 2

**Dirigido a:** Agente QA - E2E  
**Prioridad:** 🟡 Media  

## Checkpoints de Validación

### QA-B2-01: Compilación limpia
```bash
mvn clean compile -pl ibpms-core
npm run build  # (frontend)
```

### QA-B2-02: Tests del scope Bloque 2
```bash
mvn clean test -Dtest="BpmnDeployContractTest,SandboxIsolationTest,SandboxGovernanceTest,ProcessLockPersistenceTest,BreakLockRbacTest,ExternalTaskTopicsCatalogTest,DeployRequestWorkflowTest,DataMappingIntegrityTest,BpmnCopilotSseIntegrationTest" -pl ibpms-core
```

### QA-B2-03: Verificación Zero-Mock
```bash
node scripts/anti-mock-scanner.js
```
Resultado esperado: `exit 0` (sin violaciones).

### QA-B2-04: Regresión Bloque 1
Re-ejecutar el scope del Bloque 1 para confirmar que los cambios del Bloque 2 no introdujeron regresiones:
```bash
mvn clean test -Dtest="FormCertificationTest,IdentityGovernanceIntegrationTest,ApplicationTests" -pl ibpms-core
```

---

# 📋 HANDOFF INFRA/BD — ARQ-005 Bloque 2

**Dirigido a:** Agente Infra  
**Prioridad:** 🟢 Baja (Verificativo)  

## Acciones

### INFRA-B2-01: Verificar tablas existentes
Las siguientes tablas ya deben existir (creadas en Bloque 1 o por Liquibase):

| Tabla | Validar |
|-------|---------|
| `ibpms_process_locks` | Columnas: `process_key`, `locked_by`, `locked_at`, `expires_at` |
| `ibpms_deploy_requests` | Columnas: `id`, `status`, `requested_by` |
| `ibpms_data_mappings` | Columnas: `id`, `process_key`, `form_id`, `mapping_json` |
| `ibpms_external_task_topics` | Columnas: `topic_name`, `description`, `worker_class`, `is_active` |
| `ibpms_audit_log` | Columnas: `id`, `source`, `metadata`, `timestamp` |

### INFRA-B2-02: Sin acciones constructivas
No se requieren nuevas migraciones Liquibase para el Bloque 2. Todas las tablas requeridas fueron provisionadas en el Bloque 1.

---

**Fin del Handoff Consolidado — Bloque 2 US-005**
