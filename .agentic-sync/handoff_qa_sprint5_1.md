# Handoff QA — Sprint 5.1 (Cierre de Deuda Técnica: Certificación y Reconciliación)

> **Fecha:** 2026-04-18 | **Sprint:** 5.1 (Remediación) | **Arquitecto:** Líder
> **US Objetivo:** US-002 (CA-1, CA-5, CA-6, CA-8, CA-9, CA-11), US-007 (CA-2→CA-6, CA-13→18), US-029 (CA-2→CA-7)
> **Rama de trabajo:** `sprint-5/iteracion4`

---

## 1. Contexto Estratégico

Este es el cierre definitivo del Sprint 5. El Backend ha remediado las vulnerabilidades IDOR/JWT/BD y el Frontend ha añadido modo read-only, sanitización XSS y feedback Zod campo-a-campo. Tu misión es **certificar que la deuda está efectivamente cerrada** y actualizar la Coverage Matrix a los niveles objetivo: US-002 ~50%, US-007 ~75%, US-029 ~80%.

**Prerequisito:** Backend Y Frontend del Sprint 5.1 pusheados en `sprint-5/iteracion4`.

---

## 2. Estrategia de Certificación (Pirámide ADR-010)

| Nivel | Herramienta | Responsabilidad QA Sprint 5.1 |
|-------|-------------|-------------------------------|
| **Nivel 1** (Unitario) | Vitest + `createTestingPinia()` | Componentes nuevos: `TaskPreviewModal`, `ClaimAuditTrail`, XSS sanitization |
| **Nivel 2** (E2E) | Playwright + Interceptores | Flujos corregidos: claim con JWT real, force-unclaim, audit-trail, BFF real |
| **Nivel 3** (Integración) | Vitest + JUnit | Saga compensation, Owner Check 403, TTL cleanup |

---

## 3. Alcance de Certificación

### 3.1 Nivel 1: Tests Unitarios Vitest

#### A. US-002 — Verificación UI Remediada

- **Archivo:** `tests/components/workdesk/TaskPreviewModal.spec.ts`
  - `CA-5:` Montar modal con mock de preview data. Verificar campos read-only (disabled). Verificar botón "Reclamar" invoca `claimTask()`. Verificar botón "Cerrar" oculta el modal.

- **Archivo:** `tests/components/workdesk/ClaimAuditTrail.spec.ts`
  - `CA-9:` Montar con array de eventos mock `[{action: 'CLAIM', actor: 'user1'}, {action: 'FORCE_UNCLAIM', actor: 'admin'}]`. Verificar renderizado de timeline con fechas y acciones.

- **Archivo:** `tests/components/workdesk/WorkdeskGrid.spec.ts` (extender)
  - `CA-7:` Simular click "Liberar tarea". Verificar dialog de confirmación. Click "Cancelar" → tarea NO liberada. Click "Liberar" → `unclaimTask()` invocado.

#### B. US-007 — Verificación XSS

- **Archivo:** `tests/components/dmn/DmnIntelligence.spec.ts`
  - `CA-4:` Inyectar XML con payload XSS (`<script>alert('xss')</script>`, `onerror="..."`, `javascript:`). Verificar que el HTML renderizado NO contiene ningún tag peligroso.

#### C. US-029 — Verificación Feedback Zod

- **Archivo:** `tests/stores/useFormStore.spec.ts` (extender)
  - `CA-2:` Mock de Axios que retorna HTTP 400 con body `{ errors: [{ field: 'email', message: 'Formato inválido' }] }`. Verificar que `validationErrors.email === 'Formato inválido'`.

---

### 3.2 Nivel 2: E2E Playwright

#### A. US-002 — Flujos de Seguridad Corregidos

- **Archivo:** `e2e/us002-claim-jwt.spec.ts`
  - `CA-1:` Interceptar `POST /tasks/{id}/claim` con respuesta exitosa que incluya `assignee: 'real_user_jwt'` (no `e2e_user`). Verificar que la UI muestra el usuario real.

- **Archivo:** `e2e/us002-force-unclaim-supervisor.spec.ts`
  - `CA-8:` Interceptar `POST /tasks/{id}/force-unclaim` con respuesta 200. Verificar Toast "Tarea reasignada". Luego con usuario sin `ROLE_SUPERVISOR` → interceptar con 403. Verificar mensaje de acceso denegado.

- **Archivo:** `e2e/us002-audit-trail.spec.ts`
  - `CA-9:` Interceptar `GET /tasks/{id}/audit-trail` con array de eventos. Verificar que el timeline se renderiza con el orden correcto (más reciente primero).

- **Archivo:** `e2e/us002-preview-readonly.spec.ts`
  - `CA-5:` Interceptar `GET /tasks/{id}/preview`. Verificar que el modal muestra datos. Verificar que los campos son read-only (no editables).

#### B. US-007 — IDOR Fix Verificado

- **Archivo:** `e2e/us007-tenant-isolation.spec.ts`
  - `CA-6:` Crear dos contextos de navegador simulando dos tenants diferentes. Tenant A intenta acceder a un DMN de Tenant B → interceptar con 403 Forbidden. Verificar que la UI muestra "Acceso denegado".

#### C. US-029 — BFF Real y Validación

- **Archivo:** `e2e/us029-zod-field-errors.spec.ts`
  - `CA-2:` Interceptar `POST /tasks/{id}/complete` con HTTP 400 y `errors[]`. Verificar que los mensajes inline aparecen debajo de los campos correspondientes.

- **Archivo:** `e2e/us029-saga-compensation.spec.ts`
  - `CA-4:` Interceptar complete con HTTP 200 primero, luego simular rollback (Camunda falla). Verificar que la UI muestra mensaje de error indicando compensación activa.

- **Archivo:** `e2e/us029-owner-check.spec.ts`
  - `CA-6:` Interceptar `POST /tasks/{id}/complete` con HTTP 403 `"No eres el dueño de esta tarea"`. Verificar que la UI muestra el mensaje y NO limpia el formulario.

---

### 3.3 Reconciliación Final y Cierre

Al finalizar TODA la certificación:

1. **Ejecutar** `.agent/workflows/reconciliacionCoberturaCa.md` — Cruzar las 4 fuentes (SSOT, Handoffs, Git, Matriz) para los 21 CAs del Sprint 5.1.
2. **Actualizar** `.agentic-sync/coverage_matrix.md`:
   - US-002: objetivo ~50%
   - US-007: objetivo ~75%
   - US-029: objetivo ~80%
3. **Generar** acta de cierre `.agentic-sync/cierre_sprint_5_1.md` según plantilla Fase 6 del workflow `cierreDeudaTecCriteriosAceptacion.md`.

---

## 4. Reglas de Gobernanza Mandatorias

- **Quality Gate Pre-Push:** `npm run test:unit` (100% green) + `npm run build` (0 errores) + `npx playwright test` (100% green o justificación documentada).
- **Aplica** el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para la Ley de Correspondencia Gherkin.
- **Reconciliación:** Ejecutar `.agent/workflows/reconciliacionCoberturaCa.md` al cierre.
- **Router QA:** `.agent/workflows/router_certificacion_qa.md` — Nivel B.4 (Automatización SDET Playwright).
- **Cierre Deuda:** `.agent/workflows/cierreDeudaTecCriteriosAceptacion.md` (Fase 5 + Fase 6).
- **PRECAUCIÓN Timers:** Todo `vi.useFakeTimers()` debe tener teardown explícito en `afterEach`.

---

## 5. NFR/QA Strategy

- **Vitest:** Test por cada componente nuevo + extensiones de tests existentes.
- **Playwright:** Interceptores para todos los flujos corregidos (JWT, IDOR, force-unclaim, Zod errors).
- **Multi-Context Playwright:** Para verificación de tenant isolation (CA-6 US-007).
- **Métrica objetivo:** 0 CAs P0/P1 en estado "Sin Cobertura" al finalizar la reconciliación.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `sprint-5/iteracion4`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
