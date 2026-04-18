# Handoff Backend — Sprint 5.1 (Cierre de Deuda Técnica: Seguridad y Persistencia)

> **Fecha:** 2026-04-18 | **Sprint:** 5.1 (Remediación) | **Arquitecto:** Líder
> **US Objetivo:** US-002 (CA-1, CA-5, CA-6, CA-8, CA-9, CA-11), US-007 (CA-2, CA-3, CA-5, CA-6, CA-13→18), US-029 (CA-2, CA-5)
> **Rama de trabajo:** `sprint-5/iteracion4`

---

## 1. Contexto Estratégico

Este mini-sprint es una **operación quirúrgica de remediación**. La auditoría del Sprint 5 reveló brechas críticas documentadas en `coverage_matrix.md`:
- **Seguridad:** IDOR activo en DMN (tenantId hardcodeado), JWT no inyectado en claim (assignee hardcodeado `"e2e_user"`), PII enviada sin seudonimizar al LLM.
- **Persistencia:** BD comentada en claim, BFF con mock repository, caché no segmentada por tenant.

Tu misión es **remediar sin construir funcionalidades nuevas**. Solo arreglas lo roto.

---

## 2. Alineación Arquitectónica

| ADR | Impacto |
|-----|---------|
| ADR-001 (Hexagonal) | Las correcciones de seguridad (JWT injection, tenant isolation) van en `application/service/`, NO en Controllers. |
| ADR-003 (Camunda 7) | El force-unclaim debe sincronizar con Camunda `taskService.setAssignee(null)`. |
| ADR-009 (PostgreSQL) | Nuevas tablas (`claim_audit_log`) via Liquibase changeset. Activar `SKIP LOCKED` ya implementado. |
| ADR-010 (Pirámide) | Tests `@WebMvcTest` por cada endpoint nuevo. `@DataJpaTest` para auditoría y SKIP LOCKED. |

---

## 3. Alcance Técnico

### 3.1 US-002 — Remediación del Claim (6 CAs)

- **CA-1 (P0 — Seguridad):** En `WorkboxTaskController` (o equivalente), reemplazar `"e2e_user"` por `SecurityContextHolder.getContext().getAuthentication().getName()`. Descomentar la llamada a `taskRepository.assignTask()` para que el assignee se persista en PostgreSQL. Verificar que tras un reinicio del servidor, la tarea sigue asignada.

- **CA-11 (P0 — Concurrencia):** En el servicio de claim, reemplazar la lógica Redis SETNX por la invocación a `AgileTaskRepositoryJpa.findByIdForUpdate()` que ya implementaste en la Iter1 con `@Lock(PESSIMISTIC_WRITE)` y `SKIP LOCKED`. Redis puede mantenerse como caché de lectura, pero la **fuente de verdad del lock debe ser PostgreSQL**.

- **CA-5 (P1):** Crear endpoint `GET /api/v1/workbox/tasks/{id}/preview` que retorne los datos de la tarea SIN adquirir lock. Usar una proyección DTO de solo lectura. El Frontend mostrará un modal "Vista previa" antes de reclamar.

- **CA-6 (P1):** Parametrizar `AutoClaimService` con `@ConfigurationProperties("ibpms.claim.ghost-timeout")` con valores por defecto (30 minutos). Añadir soporte para override por tenant si existe la config en BD.

- **CA-8 (P1):** Crear endpoint `POST /api/v1/workbox/tasks/{id}/force-unclaim`. Validar que el usuario autenticado tenga `ROLE_SUPERVISOR` o `ROLE_ADMIN`. Registrar el despojo en la tabla de auditoría (CA-9). Emitir evento WebSocket `TASK_FORCE_UNCLAIMED`.

- **CA-9 (P1):** Crear tabla `claim_audit_log` via Liquibase:
  ```sql
  CREATE TABLE claim_audit_log (
    id BIGSERIAL PRIMARY KEY, task_id VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL, -- CLAIM, UNCLAIM, FORCE_UNCLAIM
    actor_user_id VARCHAR(255) NOT NULL, reason TEXT,
    created_at TIMESTAMP DEFAULT NOW()
  );
  ```
  Crear endpoint `GET /api/v1/workbox/tasks/{id}/audit-trail` que retorne el historial ordenado DESC.

### 3.2 US-007 — Remediación IDOR y Seguridad DMN (9 CAs)

- **CA-6 (P0 — IDOR):** En `DmnGovernanceController`, reemplazar TODO `tenantId` hardcodeado por `SecurityContextHolder.getContext().getAuthentication()` → extraer el claim `tenant_id` del JWT. Aplicar filtro `WHERE tenant_id = :tenantId` en TODAS las queries del repositorio DMN. **Esta es la vulnerabilidad más crítica del sistema.**

- **CA-2 (P0 — Caché):** Segmentar la caché DMN por `tenantId`. La key debe ser `dmn:{tenantId}:{promptHash}` en lugar de `dmn:{promptHash}`.

- **CA-5 (P1 — PII):** Implementar un `PiiSanitizer` que reemplace emails, nombres y teléfonos del prompt antes de enviarlo al LLM. Usar regex estándar. Almacenar el mapping para reconstruir el DMN con datos reales post-generación.

- **CA-3 (P1):** Crear `DmnDraftCleanupScheduler` con `@Scheduled(cron = "0 0 3 * * *")` que borre borradores DMN con `updated_at < NOW() - INTERVAL '30 days'`.

- **CA-13 (P1):** Implementar persistencia dual: guardar borrador DMN en BD Y en Redis. Al leer, priorizar Redis (velocidad), fallback a BD (resiliencia).

- **CA-14 (P1):** Asegurar que `POST /api/v1/dmn/simulate` sea funcional (no solo mock). Si el adaptador IA no está disponible, usar el motor Camunda DMN embebido para ejecutar la tabla contra inputs de prueba.

- **CA-15 (P1):** Cuando se actualice un DMN (`PUT /api/v1/dmn/{id}`), invalidar la entrada de caché Redis correspondiente (`DEL dmn:{tenantId}:{id}`).

- **CA-16→18 (P1):** Crear endpoints: `GET /api/v1/dmn/catalog` (listado paginado por tenant), `GET /api/v1/dmn/{id}` (detalle), con annotations `@Operation` de OpenAPI.

### 3.3 US-029 — Remediación BFF y Validación (2 CAs)

- **CA-5 (P0 — BFF Real):** En `FormBffCoreService`, reemplazar `mockEventSourcingRepository` por el repositorio JPA real (`FormEventRepositoryJpa`). Verificar que el prefill del formulario carga datos reales de la BD.

- **CA-2 (P0 — Zod campo-a-campo):** En el `GlobalExceptionHandler`, cuando captures una `ConstraintViolationException` o error de validación Zod, retornar RFC 7807 con un array `errors` donde cada entrada tenga `{ field, message, rejectedValue }`.

---

## 4. Reglas de Gobernanza Mandatorias

- **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.
- **TDD:** Aplica estrictamente `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor).
- **Clean Code:** Aplica `.agents/skills/clean_code_standards/SKILL.md`.
- **Reconciliación:** Al finalizar, ejecuta internamente el workflow `.agent/workflows/reconciliacionCoberturaCa.md` para verificar que cada CA tiene commit asociado.
- **Router QA:** Tu código será evaluado bajo `.agent/workflows/router_certificacion_qa.md`.
- **Cierre Deuda:** Todo CA implementado debe seguir la trazabilidad exigida por `.agent/workflows/cierreDeudaTecCriteriosAceptacion.md` (Fase 5: Coverage Matrix).

---

## 5. NFR/QA Strategy

- Tests `@WebMvcTest` para CADA endpoint nuevo/corregido.
- Tests `@DataJpaTest` para la tabla `claim_audit_log` y la activación de SKIP LOCKED.
- Test de integración que demuestre que el `assignee` sobrevive a un reinicio del servidor.
- Quality Gate: `mvn clean package` BUILD SUCCESS.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `sprint-5/iteracion4`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
