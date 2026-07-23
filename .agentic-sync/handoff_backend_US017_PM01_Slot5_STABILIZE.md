# 🔧 Handoff Backend — US-017 STABILIZE (Sprint PM-01, Slot 5)

> **Fecha**: 2026-06-09  
> **Sprint**: PM-01 | **Slot**: 5 | **Cadena**: 2 — Core Workdesk  
> **US**: US-017 — Ejecución y Persistencia Inmutable de Formularios (CQRS & Event Sourcing)  
> **Branch de trabajo**: `sprint-8/pm-01/us-017-stabilize` (crear desde `devDavid`)  
> **Rol**: Desarrollador Backend  
> **Prerequisitos**: US-002 (~92%), US-008 (Slot 4 ejecutado) — ya integradas en `devDavid`  

---

## Pre-Handoff Checklist — US-017

| # | Verificación | Estado | Evidencia |
|---|---|---|---|
| 1 | US en sprint actual (Roadmap PM-IA) | ✅ | Sprint PM-01, Cadena 2 — `01_ROADMAP_Y_METODOLOGIA.md` línea 311 |
| 2 | Endpoints definidos en API_CONTRACTS.md | ✅ | Sección 5.10 — Contratos CQRS agregados 2026-06-09 |
| 3 | Prerrequisitos completados | ⚠️ | US-001: ✅, US-002: ~92% (core funcional), US-008: Slot 4 ejecutado |
| 4 | Matriz de cobertura actualizada | ❌ | Tiene 3 conflictos activos — SU RESOLUCIÓN ES PARTE DE ESTE HANDOFF |

---

## 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA

El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
**PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

---

## ⚠️ POLÍTICA ANTIAMNESIA — RE-ENTRENAMIENTO OBLIGATORIO

Antes de escribir UNA SOLA línea de código, DEBES re-entrenar tu contexto leyendo:
1. **Arquitectura Core:** `docs/architecture/arquitecturar.md`
2. **Negocio US-017:** `docs/requirements/epics/epic_A_motor_core.md` (líneas 1009-1288)
3. **Gobernanza PM-IA:** `docs/sprints/gobernanza_pm/GUIA_ARQUITECTO_LIDER.md`
4. **Contratos API:** `docs/sprints/gobernanza_pm/API_CONTRACTS.md` (sección 5.10)

---

## 1. CONTEXTO Y OBJETIVO

US-017 es la **historia gemela de backend** que gobierna la persistencia CQRS/Event Sourcing para la completitud de tareas. Tiene **26 CAs** (CA-01 a CA-26) de los cuales:
- **CA-01 a CA-18**: Backend puro (CQRS, Event Store, Drafts, Auto-Claim, Rate-Limiting, Archivado)
- **CA-19 a CA-26**: Frontend UX (Toast de conexión — NO es tu responsabilidad)

**PROBLEMA**: US-017 tiene conflictos de merge en la coverage matrix y violaciones arquitectónicas detectadas en la auditoría del Arquitecto Líder. El avance real es incierto (~50% a ~95%).

**TU MISIÓN**: Estabilizar el backend de US-017, resolver conflictos, corregir violaciones, y verificar que TODOS los 18 CAs backend compilan y funcionan correctamente sin romper US-002 (Claim) ni US-008 (Kanban).

---

## 2. TAREAS ORDENADAS POR PRIORIDAD

### TAREA 1 (P0): Crear Branch y Resolver Conflictos

```bash
# Crear branch de estabilización desde devDavid
git checkout devDavid
git pull origin devDavid
git checkout -b sprint-8/pm-01/us-017-stabilize
```

**Resolver conflictos en `.agentic-sync/coverage_matrix.md`:**

Hay **3 zonas de conflicto** entre `HEAD` y `origin/DevDavid`:

| Zona | Líneas | Resolución |
|------|--------|------------|
| 1 (Resumen Ejecutivo) | 35-41 | Adoptar versión HEAD (tiene Sprint 6.2 data) |
| 2 (US-017 Section) | 514-580 | **ADOPTAR VERSIÓN HEAD** (~95%, 26 CAs) como base. Esta refleja el trabajo real de los sprints anteriores |
| 3 (Apéndice Brechas) | 870-877 | Adoptar versión HEAD (más completa) |

**Criterio de resolución**: La versión HEAD representa el estado más actualizado con datos de Sprint 6.1 y 6.2. La versión DevDavid tiene una evaluación de CAs diferente que fue creada ANTES de los sprints de estabilización.

Eliminar también las secciones duplicadas al final del archivo (líneas 898-914 duplican 881-897).

---

### TAREA 2 (P0): Corregir Violación Hexagonal en FormSubmissionUseCase.java

**Archivo**: `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/FormSubmissionUseCase.java`

**PROBLEMA**: Este UseCase importa directamente clases de la capa de infraestructura JPA:
- `infrastructure.jpa.entity.FormEventEntity`
- `infrastructure.jpa.repository.FormEventRepository`

**Esto viola el ADR-001 (Arquitectura Hexagonal)**: La capa `application/` NUNCA debe importar de `infrastructure/`.

**CORRECCIÓN REQUERIDA**:
1. Reemplazar imports de `infrastructure.jpa.entity.FormEventEntity` por `domain.model.FormEvent`
2. Reemplazar imports de `infrastructure.jpa.repository.FormEventRepository` por `domain.port.FormEventRepository`
3. Usar los métodos del puerto de dominio en lugar de las operaciones JPA directas
4. El mapper entre domain y entity debe vivir en la capa de `infrastructure/persistence/` (adapter)

**REFERENCIA**: Otros servicios como `AutoClaimService.java` y `RejectionLogService.java` ya usan correctamente el domain port — úsalos como modelo.

---

### TAREA 3 (P1): Verificar Migraciones Liquibase

Existen **múltiples migraciones** que crean/modifican la tabla del Event Store:

| Archivo | Acción | Tabla |
|---------|--------|-------|
| `db/changelog/changes/016-create-cqrs-event-store.sql` | CREATE | `ibpms_form_event_store` (nombre antiguo) |
| `db/changelog/sprint3/001_create_form_event_store.sql` | CREATE | `form_event_store` (nombre actual) |
| `db/changelog/40-us029-fix-form-event-store.sql` | DROP old + CREATE new | `form_event_store` |
| `db/changelog/sprint3/002_create_task_drafts.sql` | CREATE | `task_drafts` |
| `db/changelog/39-us029-form-execution-schema.sql` | ALTER | Adds `visible_fields` JSONB |

**VERIFICACIÓN REQUERIDA**:
1. Verificar el orden de ejecución en `db/changelog/db.changelog-master.yaml` — ¿están en el orden correcto?
2. Verificar que la migración 016 (nombre antiguo `ibpms_form_event_store`) está correctamente supersedida por la 40 (que hace DROP + CREATE con nombre `form_event_store`)
3. Si hay conflictos de naming, corregir para que solo exista UNA tabla `form_event_store`
4. Ejecutar `mvn liquibase:validate` para confirmar que las migraciones son consistentes

---

### TAREA 4 (P1): Verificar los 18 CAs Backend

Para cada CA, verifica que el código existe, compila, y es funcionalmente correcto:

| CA | Qué Verificar | Archivo(s) Clave |
|----|---------------|-------------------|
| CA-01 | CQRS Event Sourcing: `FORM_SUBMITTED_EVENT` se graba en `form_event_store` | `FormCompletionService.java` |
| CA-02 | Solo DTO minificado va a Camunda (NO payload completo) | `FormCompletionService.java` |
| CA-03 | Rollback Saga si Camunda falla | `FormCompletionService.java` |
| CA-04 | Auto-Claim para tareas de grupo sin assignee | `AutoClaimService.java` |
| CA-05 | `rejectionLogs` inyectado en `/form-context` | `RejectionLogService.java`, `FormBffCoreService.java` |
| CA-06 | Schema Event Store con 3 event types | `FormEventEntity.java`, DDL |
| CA-07 | Endpoints GET/PUT/DELETE draft | `TaskDraftApiController.java`, `TaskDraftService.java` |
| CA-08 | Cross-reference con US-029 documentada | Documentación solamente |
| CA-09 | `FORM_DRAFT_SAVED` NO está en Event Store | `EventType.java` |
| CA-10 | Rollback genera `FORM_SUBMIT_ROLLED_BACK` (no DELETE) | `FormCompletionService.java` |
| CA-11 | `rejectionLogs` tiene 5 campos obligatorios | `RejectionLogService.java` |
| CA-12 | Cifrado at-rest habilitado | Configuración PostgreSQL |
| CA-13 | Validación `candidateGroup` antes de Auto-Claim | `AutoClaimService.java` |
| CA-14 | Rate-limiting 6/min en endpoints de draft | Configuración / Filter |
| CA-15 | Response incluye `eventReference` (12 chars legibles) | `FormCompletionService.java` |
| CA-16 | DELETE draft dentro del flujo de submit | `FormCompletionService.java` |
| CA-17 | SLA 5s normal / 17s con reintentos | Timeout config |
| CA-18 | Cron de archivado anual | `EventStoreArchivalScheduler.java` |

---

### TAREA 5 (P1): Verificar Integración con US-002 y US-008

**US-017 depende de US-002 (Claim):**
- El Auto-Claim (CA-04) usa `taskService.claim()` de Camunda
- Verificar que `AutoClaimService.java` es compatible con los cambios del Slot 1 de US-002
- Verificar que `TaskClaimApiController.java` (US-002) no conflictua con los controllers de US-017

**US-017 coexiste con US-008 (Kanban):**
- Las tareas Kanban también pueden completarse vía `/complete`
- Verificar que `FormCompletionService.java` maneja tanto tareas BPMN como Kanban
- Verificar que `KanbanTaskEntity` (US-008 Slot 4) no conflictua con `FormEventEntity`

---

### TAREA 6 (P2): Actualizar Coverage Matrix

Tras completar las tareas anteriores, actualizar `.agentic-sync/coverage_matrix.md`:
- Sección US-017 con el estado real verificado de cada CA
- Incluir commit hash de la resolución
- Fecha de verificación: la actual

---

## 3. CONTRATOS API (Referencia)

Los contratos para US-017 están definidos en `docs/sprints/gobernanza_pm/API_CONTRACTS.md`, sección **5.10 CQRS / Task Completion**:

| Endpoint | Método | CA |
|----------|--------|----|
| `/api/v1/workbox/tasks/{taskId}/complete` | POST | CA-01 a CA-04, CA-10, CA-15-17 |
| `/api/v1/workbox/tasks/{taskId}/draft` | GET | CA-07 |
| `/api/v1/workbox/tasks/{taskId}/draft` | PUT | CA-07, CA-14 |
| `/api/v1/workbox/tasks/{taskId}/draft` | DELETE | CA-07, CA-16 |

---

## 4. ALINEACIÓN ARQUITECTÓNICA

| ADR | Impacto | Acción |
|-----|---------|--------|
| ADR-001 (Hexagonal) | 🔴 Violación en `FormSubmissionUseCase.java` | CORRECCIÓN OBLIGATORIA (Tarea 2) |
| ADR-009 (PostgreSQL) | ✅ Event Store usa JSONB en PostgreSQL | Verificar migraciones |
| ADR-010 (Testing) | ⚠️ Tests existen pero algunos skipped | Verificar que compilan |
| ADR-011 (Local CQRS) | ✅ Implementado correctamente | OK |

---

## ⚠️ IMPORTANTE

Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes (Workdesk, Kanban, Claim) será motivo de rechazo inmediato.

---

## 📋 Compilación obligatoria

Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

---

## INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN

1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_BACKEND.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_BACKEND.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve).
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
