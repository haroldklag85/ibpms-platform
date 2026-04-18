# Handoff Backend — Sprint 5, Iteración 3 (Interfaz Client-Side: Soporte API)

> **Fecha:** 2026-04-18 | **Iteración:** Sprint5-Iter3 | **Arquitecto:** Líder
> **US Objetivo:** US-002 (CA-11 al CA-15), US-029 (CA-21 al CA-30), US-007 (CA-16 al CA-20), US-025 (CA-09 al CA-26)

---

## 1. Contexto Estratégico

La Iteración 3 es **predominantemente Frontend**, pero el Backend debe proveer los contratos REST y WebSocket finales que el Frontend consumirá en vivo. Tu rol en esta iteración es **estabilizar y endurecer los endpoints** que ya existen como cascarones Mock, transformándolos en puertos funcionales conectados a la capa de Aplicación certificada en las Iteraciones 1 y 2.

---

## 2. Alineación Arquitectónica

| ADR | Impacto |
|-----|---------|
| ADR-001 (Hexagonal) | Los Controllers siguen siendo Dumb Adapters. Toda lógica nueva va en `application/service/`. |
| ADR-003 (Camunda 7) | El endpoint `/claim` debe invocar el `TaskService` interno de Camunda para sincronizar el assignee. |
| ADR-010 (Pirámide Testing) | Tests de integración `@WebMvcTest` para cada endpoint nuevo. |
| ADR-011 (CQRS Local) | Las consultas de lectura (GET /tasks, GET /sla-log) deben usar proyecciones o DTOs de lectura separados. |

---

## 3. Alcance Técnico (CAs Backend Iter3)

### 3.1 US-002 — Soporte WebSocket para Reclamación en Tiempo Real (CA-11 al CA-15)
- **CA-11/CA-12:** Implementar emisor de evento `TASK_CLAIMED` vía WebSocket (STOMP sobre SockJS) cuando `AgileTaskService.claimTask()` se ejecute exitosamente.
- **CA-13:** El mensaje WebSocket debe incluir: `{ taskId, claimedBy, timestamp }`.
- **CA-14:** Endpoint `GET /api/v1/workbox/tasks?status=AVAILABLE` debe filtrar tareas ya reclamadas.
- **CA-15:** Endpoint `POST /api/v1/workbox/tasks/{id}/unclaim` — liberar tarea y emitir `TASK_UNCLAIMED`.

### 3.2 US-029 — Endpoints de Borrador y Submit Seguro (CA-21 al CA-30)
- **CA-21/CA-22:** `PUT /api/v1/workbox/tasks/{id}/draft` — Persistir borrador con debounce server-side (ignorar si payload idéntico al último guardado).
- **CA-23/CA-24:** `POST /api/v1/workbox/tasks/{id}/complete` — Validación Zod server-side del schema del formulario antes de delegar a Camunda `taskService.complete()`.
- **CA-25:** Respuesta estructurada RFC 7807 para errores de validación con detalle por campo.
- **CA-26 al CA-30:** Manejo de estados transicionales (DRAFT → SUBMITTED → COMPLETED). Rechazo de submit si estado != DRAFT o CLAIMED.

### 3.3 US-007 — Endpoint DMN Generador Funcional (CA-16 al CA-20)
- **CA-16:** Reemplazar el `MockNlpDmnAdapter` por un adaptador que invoque el `AiDmnGeneratorPort` real (si credenciales disponibles) o mantenga el fallback Mock con flag `@ConditionalOnProperty("ibpms.ai.mock-enabled")`.
- **CA-17/CA-18:** Endpoint `POST /api/v1/dmn/generate` debe retornar `{ dmnXml, confidence, explanation }`.
- **CA-19/CA-20:** Validación de entrada: rechazar prompts vacíos (400), prompts > 2000 chars (413).

### 3.4 US-025 — BFF de Roles Dinámicos (CA-09 al CA-14)
- **CA-09/CA-10:** Endpoint `GET /api/v1/auth/effective-roles` que consume el CTE recursivo (Iter2) y retorna la lista plana de roles efectivos con sus permisos.
- **CA-11 al CA-14:** Endpoint `GET /api/v1/dashboard/cards?role={roleId}` que retorna las Cards/Widgets visibles para el rol activo.

---

## 4. Reglas de Gobernanza Mandatorias

- **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.
- **TDD:** Aplica estrictamente `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor).
- **Clean Code:** Aplica `.agents/skills/clean_code_standards/SKILL.md`.
- **Reconciliación:** Al finalizar, ejecuta internamente el workflow `.agent/workflows/reconciliacionCoberturaCa.md` para verificar que cada CA tiene commit asociado.
- **Router QA:** Tu código será evaluado bajo `.agent/workflows/router_certificacion_qa.md` — prepárate para auditoría estática (Nivel B.1).
- **Cierre Deuda:** Todo CA implementado debe seguir la trazabilidad exigida por `.agent/workflows/cierreDeudaTecCriteriosAceptacion.md` (Fase 5: Coverage Matrix).

---

## 5. NFR/QA Strategy

Pruebas de integración `@WebMvcTest` para cada Controller nuevo/modificado. Tests de WebSocket con `StompSession` mockeado. El Quality Gate exige `mvn clean package` en BUILD SUCCESS antes de declarar cierre.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
