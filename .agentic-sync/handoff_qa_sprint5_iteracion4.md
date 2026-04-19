# Handoff QA — Sprint 5, Iteración 4 (Blindaje: QA Defensivo y Flujos Negativos)

> **Fecha:** 2026-04-18 | **Iteración:** Sprint5-Iter4 | **Arquitecto:** Líder
> **US Objetivo:** US-002 (CA-21 al CA-28), US-029 (CA-31 al CA-37), US-007 (CA-21 al CA-24)
> **Rama de trabajo:** `main`

---

## 1. Contexto Estratégico

La Iteración 4 es la **fase de certificación final** del Sprint 5. Tu misión como QA Lead no es solo validar que los flujos negativos funcionan — es **probar que el sistema NO se rompe** cuando todo sale mal. Esta iteración cierra los 115 CAs del Sprint 5 y debe generar la reconciliación final de cobertura.

**Prerequisito:** Backend Y Frontend de la Iteración 4 deben estar pusheados antes de iniciar esta fase.

---

## 2. Estrategia de Certificación (Pirámide ADR-010)

| Nivel | Herramienta | Responsabilidad QA Iter4 |
|-------|-------------|--------------------------|
| **Nivel 1** (Unitario) | Vitest + `createTestingPinia()` | Validar renderizado de componentes de error (modales, banners, countdown) |
| **Nivel 2** (E2E) | Playwright + Interceptores | Simular fallas de red, timeouts, y colisiones de sesión |
| **Nivel 3** (Integración) | Playwright Multi-Context | Flujos concurrentes negativos (force-unclaim, sesión duplicada) |

---

## 3. Alcance de Certificación (CAs QA Iter4)

### 3.1 Nivel 1: Tests Unitarios Vitest (Aislamiento de Componentes de Error)

#### A. US-002 — Resiliencia Visual del Workdesk (CA-21 al CA-28)

- **Archivo:** `tests/components/workdesk/WorkdeskTabs.spec.ts`
  - `CA-22:` Montar `WorkdeskTabs.vue` con mock de store. Verificar que al hacer click en tab "Mi Bandeja", el store mute `activeView` a `PERSONAL` y se dispare un re-fetch. Verificar que tab "Cola Equipo" muta a `POOL`.
  
- **Archivo:** `tests/components/workdesk/WorkdeskGrid.spec.ts` (extender)
  - `CA-21:` Simular `claimTask()` que falla (mock reject). Verificar que la tarea reaparece en la lista local (rollback visual) y que se muestra Toast de error.
  - `CA-28:` Simular click en botón "Atender Siguiente". Mock de `POST claim-next` retornando 204. Verificar que se muestra Toast "No hay tareas disponibles" y NO se navega.

- **Archivo:** `tests/composables/useSlaTrafficLight.spec.ts`
  - `CA-24/CA-25:` Verificar que el composable computa correctamente los colores basado en los umbrales del config. Inyectar thresholds `{ green: 4, yellow: 8 }` y una tarea con 5 horas. Debe ser `sla-yellow`.

#### B. US-029 — Componentes de Resiliencia en Formularios (CA-31 al CA-37)

- **Archivo:** `tests/components/shared/NetworkRetryModal.spec.ts`
  - `CA-31:` Montar con prop `isVisible: true`. Verificar que NO se puede cerrar con Escape ni click-outside (non-dismissable). Verificar botones "Reintentar" y "Cancelar" presentes.
  - `CA-32:` Simular 3 clicks en "Reintentar". Verificar que el texto cambia: "Intento 2 de 3", "Intento 3 de 3". Tras el 3°, verificar mensaje "Contacte a soporte" con el idempotencyKey.

- **Archivo:** `tests/components/shared/SessionConflictBanner.spec.ts`
  - `CA-35:` Montar con prop `conflictDetected: true`. Verificar banner visible con texto "Tienes otra pestaña editando esta tarea". Verificar botón "Forzar Edición Aquí" emite evento `force-session`.

- **Archivo:** `tests/composables/useDraftTtl.spec.ts`
  - `CA-36:` Con `vi.useFakeTimers()`, simular polling que retorna `ttl: 250`. Verificar que banner amarillo aparece con countdown. Avanzar timers hasta que `ttl <= 0`. Verificar que modal rojo bloqueante aparece.
  - **PRECAUCIÓN (Directiva Arquitecto Iter3):** Garantizar `afterEach` con `vi.runOnlyPendingTimers()` + `vi.useRealTimers()` + reinicio de Pinia para evitar side-effects.

- **Archivo:** `tests/stores/useFormStore.spec.ts` (extender)
  - `CA-37:` Invocar `submitForm()` con mock Axios que retorna HTTP 500. Verificar que el error expuesto al componente es genérico ("Error interno") y NO contiene stack trace.

#### C. US-007 — Validación Defensiva DMN (CA-21 al CA-24)

- **Archivo:** `tests/components/dmn/DmnNlpPanel.spec.ts` (extender)
  - `CA-21:` Inyectar en `useDmnStore` estado `generationError: { status: 422, detail: 'XML mal formado' }`. Verificar que el panel muestra bloque rojo con el detalle. Verificar que el canvas XML NO renderiza nada.
  - `CA-22:` Inyectar error `{ status: 403, type: 'HIT_POLICY_FORBIDDEN', policy: 'COLLECT' }`. Verificar dialog con botón "Regenerar con política UNIQUE".
  - `CA-23:` Inyectar error `{ status: 429, retryAfter: 30 }`. Verificar countdown visual. Mock `setInterval` y verificar que el botón "Simular" tiene `disabled` hasta countdown = 0.
  - `CA-24:` Inyectar error `{ status: 504 }`. Verificar mensaje de timeout + botones "Reintentar" y "Usar template por defecto".

---

### 3.2 Nivel 2: E2E Playwright (Interceptores de Falla)

#### A. US-002 — Rollback Visual y Force-Unclaim E2E

- **Archivo:** `e2e/us002-claim-rollback.spec.ts`
  - Interceptar `POST /api/v1/workbox/tasks/*/claim` con `status: 500`. Verificar que la tarea reaparece en el pool visual tras el rollback optimistic.

- **Archivo:** `e2e/us002-force-unclaim.spec.ts`
  - Interceptar evento WebSocket `TASK_FORCE_UNCLAIMED`. Verificar que aparece Toast "Un supervisor ha reasignado tu tarea" y que el formulario se cierra automáticamente redirigiendo al workdesk.

- **Archivo:** `e2e/us002-claim-next.spec.ts`
  - Interceptar `POST /api/v1/workbox/tasks/claim-next` con `status: 200` + body con tarea. Verificar navegación automática a `/task-viewer/{id}`. Luego con `status: 204`, verificar Toast "No hay tareas".

#### B. US-029 — Resiliencia de Submit E2E

- **Archivo:** `e2e/us029-submit-timeout.spec.ts`
  - Interceptar `POST /api/v1/workbox/tasks/*/complete` con delay de 35s y `status: 504`. Verificar que `NetworkRetryModal` aparece. Click "Reintentar". Interceptor retorna 200. Verificar Toast de éxito.

- **Archivo:** `e2e/us029-session-conflict.spec.ts`
  - Abrir dos contextos de navegador (Tab A y Tab B) con mismo usuario. Tab A hace `PUT /draft` exitoso. Tab B intenta `PUT /draft` → interceptar con `status: 409 SESSION_CONFLICT`. Verificar banner de conflicto en Tab B.

- **Archivo:** `e2e/us029-draft-expiration.spec.ts`
  - Interceptar `GET /api/v1/workbox/tasks/*/draft-ttl` con `{ ttl: 10 }` (10 segundos). Verificar banner amarillo. Esperar. Siguiente poll retorna `status: 410`. Verificar modal rojo bloqueante.

#### C. US-007 — Rate Limiting E2E

- **Archivo:** `e2e/us007-rate-limit.spec.ts`
  - Interceptar `POST /api/v1/dmn/simulate` con `status: 429` y `Retry-After: 15`. Verificar countdown visual. Verificar que el botón está deshabilitado durante el countdown.

---

### 3.3 Nivel 3: Reconciliación Final de Cobertura (Cierre Sprint 5)

Al finalizar TODA la certificación, ejecuta obligatoriamente:

1. **Workflow** `.agent/workflows/reconciliacionCoberturaCa.md` — Cruza los 115 CAs del Sprint 5 contra Git, Handoffs, SSOT y Matriz.
2. **Actualización** `.agentic-sync/coverage_matrix.md` — Marca los CAs certificados como ✅ con el commit hash del test.
3. **Reporte Final** — Genera un artefacto `cierre_iteracion_sprint5_iter4.md` en `.agentic-sync/` con el acta de cierre según la plantilla de la Fase 6 del workflow `cierreDeudaTecCriteriosAceptacion.md`.

---

## 4. Reglas de Gobernanza Mandatorias

- **Quality Gate Pre-Push:** `npm run test:unit` (100% green) + `npm run build` (0 errores) + `npx playwright test` (100% green o justificación documentada para timeouts contra Backend ausente).
- **Aplica** el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.
- **Reconciliación:** Al finalizar, ejecuta internamente el workflow `.agent/workflows/reconciliacionCoberturaCa.md`.
- **Router QA:** Usa `.agent/workflows/router_certificacion_qa.md` — esta iteración es Nivel B.4 (Automatización SDET Playwright).
- **Cierre Deuda:** Todo CA implementado debe seguir la trazabilidad exigida por `.agent/workflows/cierreDeudaTecCriteriosAceptacion.md` (Fase 5: Coverage Matrix).
- **PRECAUCIÓN Timers:** Herencia de directiva Iter3 — TODO test que use `vi.useFakeTimers()` debe tener teardown explícito en `afterEach`.

---

## 5. NFR/QA Strategy

- **Vitest:** Cobertura de componentes de error al 100% (mount + assertions DOM).
- **Playwright:** Interceptores REST para simular cada código HTTP negativo (409, 422, 429, 500, 504).
- **Playwright Multi-Context:** Para sesión duplicada y force-unclaim concurrente.
- **Métrica objetivo:** 0 CAs en estado "Sin Cobertura" al finalizar la reconciliación.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
