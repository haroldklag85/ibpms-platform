# Sprint 6.1 UAT/E2E Coverage Matrix

**Estado General de la Cobertura del MVP (18 User Stories Base)**
- Casos Ejecutados en este lote: 7
- Tasa de Aprobación Empírica actual: 57% (4/7)
- Cobertura E2E Mapeada: ~38% (7 de 18 Casos Core).

## Matriz de Trazabilidad US ↔ CA ↔ Spec E2E

| User Story | Criterio de Aceptación (CA) | Spec File | Test Name | Resultado | Evidencia |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **US-041 (Copilot)** | CU-JSEC-02 | idor-copilot.e2e.spec.ts | Tenant Alpha NO destruye sesión Beta | ✅ PASS | Lote 1 (HTML Rep) |
| **US-041 (Copilot)** | CU-JSEC-02b | idor-copilot.e2e.spec.ts | Tenant Alpha destruye su sesión | ✅ PASS | Lote 1 (HTML Rep) |
| **US-017 (Webhook)** | CU-JSEC-17 | webhook-legacy.e2e.spec.ts | POST retorna 410 Gone | ✅ PASS | Lote 2 (HTML Rep) |
| **US-017 (Webhook)** | CU-JSEC-17b | webhook-legacy.e2e.spec.ts | POST sin state retorna 410 Gone | ✅ PASS | Lote 2 (HTML Rep) |
| **US-B20 (DMN)** | B-20 (Dropdown) | b20-dmn-dropdown.e2e.spec.ts | Seleccionar BusinessRuleTask muestra dropdown DMN | ❌ FAIL | Timeout/Selector |
| **US-027 (Kanban)** | OBS-1 / B5 | kanban-board.e2e.spec.ts | Operario interactua con Kanban real | ❌ FAIL | Timeout/Selector |
| **US-J04 (Workdesk)** | CU-J04-01→06 | smoke-j04-operario.e2e.spec.ts | Login → Workdesk → Claim... → Desaparición | ❌ FAIL | Timeout/Selector |
| **US-043 (SLA Engine)** | PENDIENTE | - | - | ⏳ PENDIENTE | - |
| **US-001 (Portal)** | PENDIENTE | - | - | ⏳ PENDIENTE | - |
| **US-002 (BPMN UI)** | PENDIENTE | - | - | ⏳ PENDIENTE | - |
| **US-003 (Mocks)** | PENDIENTE | - | - | ⏳ PENDIENTE | - |
| **US-033 (Azure DB)** | PENDIENTE | - | - | ⏳ PENDIENTE | - |
| **US-XXX (General)** | PENDIENTE (x6) | - | - | ⏳ PENDIENTE | - |

*(Nota: Las US PENDIENTES requieren el andamiaje de Spec Files durante el Sprint 6.2/V2).*
