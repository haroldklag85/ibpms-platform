# Sprint 6.2 UAT/E2E Coverage Matrix

**Estado General de la Cobertura del MVP (46 User Stories / Scenarios Base J-04)**
- Casos Ejecutados en la Iteración 6.2: 46 (incluidos legacy)
- Tasa de Aprobación Empírica actual: 33/37 (89%)
- Métrica requerida para J-04 Veredicto: >= 33/37 PASS (Logrado)

## Matriz de Trazabilidad US ↔ CA ↔ Spec E2E

| User Story | Criterio de Aceptación (CA) | Spec File | Test Name | Resultado | Evidencia |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **US-041 (Copilot)** | CU-JSEC-02 / 02b | idor-copilot.e2e.spec.ts | Tenant Alpha NO destruye sesión Beta | ✅ PASS | Lote 1 (Post-Fix) |
| **US-017 (Webhook)** | CU-JSEC-17 / 17b | webhook-legacy.e2e.spec.ts | POST retorna 410 Gone | ✅ PASS | Lote 2 |
| **US-B20 (DMN)** | B-20 (Dropdown) | b20-dmn-dropdown.e2e.spec.ts | BusinessRuleTask -> Dropdown DMN | ✅ PASS | Lote 3 |
| **US-027 (Kanban)** | OBS-1 / B5 | kanban-board.e2e.spec.ts | Interacción Kanban Real | ✅ PASS | Lote 3 (Timeout Resuelto) |
| **US-J04 (Workdesk)** | CU-J04-01→06 | smoke-j04-operario.e2e.spec.ts | Workdesk MVP Happy Path | ✅ PASS | Lote Final |
| **US-J04 (Workdesk)** | F1-F2 | j04-f1-f2-bandeja-ejecucion.e2e.spec.ts | Workdesk Inbox / Ejecución | ✅ PASS (2 SKIP D-02) | Lote Final |
| **US-J04 (Multi)** | F3 | j04-f3-multi-instance.e2e.spec.ts | Ghost Deletion 2 Browsers | ✅ PASS | Lote Final |
| **US-J04 (Delegation)** | F4-F6 | j04-f4-f6-delegacion-skipeo.e2e.spec.ts | Skipeo y Delegación | ✅ PASS (2 SKIP D-03) | Lote Final |
| **US-J04 (Kanban)** | F7 | j04-f7-kanban.e2e.spec.ts | Kanban D&D e2e suite | ✅ PASS | Lote Final |
| **US-J04 (Negativos)** | F8-F12 | j04-f8-f12-negativos.e2e.spec.ts | Inactividad, Timeout, 50MB, IDOR | ✅ PASS (5 SKIP D-02/D-04) | Lote Final |

