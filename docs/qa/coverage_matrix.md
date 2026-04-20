# Sprint 6.2 UAT/E2E Coverage Matrix

**Estado General de la Cobertura del MVP (44 User Stories / Scenarios Base J-04)**
- Casos Ejecutados en la Iteración 6.2: 48 (incluidos legacy)
- Tasa de Aprobación Empírica actual: 5/48 (10%)
- Métrica requerida para J-04 Veredicto: >= 30/44 PASS (Pendiente)

## Matriz de Trazabilidad US ↔ CA ↔ Spec E2E

| User Story | Criterio de Aceptación (CA) | Spec File | Test Name | Resultado | Evidencia |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **US-041 (Copilot)** | CU-JSEC-02 / 02b | idor-copilot.e2e.spec.ts | Tenant Alpha NO destruye sesión Beta | ✅ PASS | Lote 1 (Post-Fix) |
| **US-017 (Webhook)** | CU-JSEC-17 / 17b | webhook-legacy.e2e.spec.ts | POST retorna 410 Gone | ✅ PASS | Lote 2 |
| **US-B20 (DMN)** | B-20 (Dropdown) | b20-dmn-dropdown.e2e.spec.ts | BusinessRuleTask -> Dropdown DMN | ❌ FAIL | Timeout Canvas |
| **US-027 (Kanban)** | OBS-1 / B5 | kanban-board.e2e.spec.ts | Interacción Kanban Real | ✅ PASS | Lote 3 (Timeout Resuelto) |
| **US-J04 (Workdesk)** | CU-J04-01→06 | smoke-j04-operario.e2e.spec.ts | Workdesk MVP Happy Path | ❌ FAIL | Timeout /workdesk |
| **US-J04 (Workdesk)** | F1-F2 | j04-f1-f2-bandeja-ejecucion.e2e.spec.ts | Workdesk Inbox / Ejecución | ❌ FAIL/SKIP | Timeout redirect |
| **US-J04 (Multi)** | F3 | j04-f3-multi-instance.e2e.spec.ts | Ghost Deletion 2 Browsers | ❌ FAIL/SKIP | Redirección UI |
| **US-J04 (Delegation)** | F4-F6 | j04-f4-f6-delegacion-skipeo.e2e.spec.ts | Skipeo y Delegación | ❌ FAIL/SKIP | Redirección UI |
| **US-J04 (Kanban)** | F7 | j04-f7-kanban.e2e.spec.ts | Kanban D&D e2e suite | ❌ FAIL/SKIP | Timeout redirect |
| **US-J04 (Negativos)** | F8-F12 | j04-f8-f12-negativos.e2e.spec.ts | Inactividad, Timeout, 50MB, IDOR | ❌ FAIL/SKIP | Mocks missing UI |

*(Nota: Aunque los specs han sido programados exhaustivamente, las aserciones de Playwright no encuentran los atributos data-testid mapeados en los componentes de Vue o hay cuellos de botella en la redirección. La UI debe alinearse.)*
