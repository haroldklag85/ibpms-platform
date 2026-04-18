# Solicitud de Aprobación QA / SDET — Iteración 4 (Blindaje)

## 📌 Asunto
Dictamen del Plan de Certificación Defensiva: Flujos Negativos Vitest + Playwright + Reconciliación Final Sprint 5 (115 CAs).

## 📄 Resumen Estratégico

Arquitecto, he analizado exhaustivamente el contrato `handoff_qa_sprint5_iteracion4.md`, los workflows `cierreDeudaTecCriteriosAceptacion.md`, `router_certificacion_qa.md` y `reconciliacionCoberturaCa.md`, y he elaborado mi plan de certificación.

### Bloque A — Vitest Nivel 1 (13 tests nuevos)
- **US-002 (CA-21→CA-28):** WorkdeskTabs (switch PERSONAL/POOL), WorkdeskGrid (claim-fail rollback, claim-next 204), useSlaTrafficLight (thresholds semáforo).
- **US-029 (CA-31→CA-37):** NetworkRetryModal (non-dismissable, 3 reintentos), SessionConflictBanner (force-session emit), useDraftTtl (countdown con FakeTimers + teardown limpio), useFormStore extensión (HTTP 500 → error genérico sin stack trace).
- **US-007 (CA-21→CA-24):** DmnNlpPanel extensiones (422 XML malo, 403 HIT_POLICY_FORBIDDEN, 429 rate-limit countdown, 504 timeout).

### Bloque B — Playwright Nivel 2 (7 scripts E2E)
- Interceptores para 500, 409, 429, 504 y eventos WebSocket.
- Multi-Context para simulación de conflicto de sesión (Tab A vs Tab B).

### Bloque C — Reconciliación y Cierre
- Ejecución del workflow `reconciliacionCoberturaCa.md` sobre los 115 CAs del Sprint 5.
- Actualización de `coverage_matrix.md`.
- Generación de acta de cierre `cierre_iteracion_sprint5_iter4.md`.

## 🛡️ Directivas Adoptadas
- **Precaución Timers (Iter3):** Todo `vi.useFakeTimers()` tendrá teardown explícito en `afterEach`.
- **TDD-First:** Test RED antes del componente.
- **Router QA:** Nivel B.4 — Automatización SDET Playwright.

## 🛑 Checkpoint Táctico
Solicito autorización formal para iniciar modo EXECUTION sobre este plan de blindaje defensivo.

---
**Agente: QA Automation / SDET**
*Esperando Veredicto de la Jefatura Técnica...*
