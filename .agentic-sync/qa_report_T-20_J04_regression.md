# 🕵️ QA REGRESSION REPORT — T-20 Journey J-04
# Suite de Certificación Playwright (Zero-Mock V2)

**Emitido por:** 🕵️ QA - PLAYWRIGHT / SRE
**Destinatario:** 🧠 ARQUITECTO LÍDER
**Fecha:** 2026-05-12T22:35:00-05:00
**Sprint:** 7 — Iteración 7.1
**Duración total:** 36.3 minutos
**Backend Profile:** `e2e` (Native Host, PID 35496, Puerto 8080)
**Exit Code:** 1 (FAILED)

---

## 📊 RESUMEN EJECUTIVO

| Métrica | Valor |
|---------|-------|
| **Total Tests Ejecutados** | 121 (88 originales + 33 retries) |
| **Tests Pasados** | 45 |
| **Tests Fallidos** | 72 (36 originales × 2 intentos) |
| **Tests Saltados (Skipped)** | 4 |
| **Tasa de Éxito** | **37.2%** (45/121) |
| **Inmutabilidad LG-04** | ✅ VERIFICADA (`git diff` vacío) |
| **Backend Post-Test** | ✅ HTTP 200 (Saludable) |

---

## ✅ TESTS PASADOS (45 tests)

### Core J-04 (Bandeja Compacta) — 12/12 ✅
| # | Test ID | Spec File | Estado |
|---|---------|-----------|--------|
| 1 | CU-J04-01 | `j04-f1-f2-bandeja-ejecucion.e2e.spec.ts` | ✅ PASSED |
| 2 | CU-J04-02 | `j04-f1-f2-bandeja-ejecucion.e2e.spec.ts` | ✅ PASSED |
| 3 | CU-J04-03 | `j04-f1-f2-bandeja-ejecucion.e2e.spec.ts` | ✅ PASSED |
| 4 | CU-J04-04 | `j04-f1-f2-bandeja-ejecucion.e2e.spec.ts` | ✅ PASSED |
| 5 | CU-J04-05 | `j04-f1-f2-bandeja-ejecucion.e2e.spec.ts` | ✅ PASSED |
| 6 | CU-J04-06 | `j04-f1-f2-bandeja-ejecucion.e2e.spec.ts` | ✅ PASSED |
| 7 | CU-J04-07 | `j04-f1-f2-bandeja-ejecucion.e2e.spec.ts` | ✅ PASSED |
| 8 | CU-J04-08 | `j04-f1-f2-bandeja-ejecucion.e2e.spec.ts` | ✅ PASSED |
| 9 | CU-J04-09 | `j04-f1-f2-bandeja-ejecucion.e2e.spec.ts` | ✅ PASSED |
| 10 | CU-J04-10 | `j04-f1-f2-bandeja-ejecucion.e2e.spec.ts` | ✅ PASSED |
| 11 | CU-J04-11 | `j04-f1-f2-bandeja-ejecucion.e2e.spec.ts` | ✅ PASSED |
| 12 | CU-J04-12 | `j04-f1-f2-bandeja-ejecucion.e2e.spec.ts` | ✅ PASSED |

### Delegación & Skipeo — 1/1 ✅
| # | Test ID | Spec File | Estado |
|---|---------|-----------|--------|
| 13 | F4/F6 Delegación | `j04-f4-f6-delegacion-skipeo.e2e.spec.ts` | ✅ PASSED |

### Kanban Board (Compacto) — 4/4 ✅
| # | Test ID | Spec File | Estado |
|---|---------|-----------|--------|
| 14 | CU-J04-29 | `j04-f7-kanban.e2e.spec.ts` | ✅ PASSED |
| 15 | CU-J04-30 | `j04-f7-kanban.e2e.spec.ts` | ✅ PASSED |
| 16 | CU-J04-31 | `j04-f7-kanban.e2e.spec.ts` | ✅ PASSED |
| 17 | CU-J04-32 | `j04-f7-kanban.e2e.spec.ts` | ✅ PASSED |

### Observabilidad & Seguridad — 6/6 ✅
| # | Test ID | Spec File | Estado |
|---|---------|-----------|--------|
| 18 | CU-J04-38 (Inactividad) | `j04-f8-f12-negativos.e2e.spec.ts` | ✅ PASSED |
| 19 | CU-J04-40 (CQRS F11) | `j04-f8-f12-negativos.e2e.spec.ts` | ✅ PASSED |
| 20 | CU-J04-41 (History) | `j04-f8-f12-negativos.e2e.spec.ts` | ✅ PASSED |
| 21 | CU-J04-42 (Audit Trail) | `j04-f8-f12-negativos.e2e.spec.ts` | ✅ PASSED |
| 22 | NEG-04 (IDOR Delegación) | `j04-f8-f12-negativos.e2e.spec.ts` | ✅ PASSED |
| 23 | NEG-07 (Sin Rol Admin) | `j04-f8-f12-negativos.e2e.spec.ts` | ✅ PASSED |

### Negativos — 3/3 ✅
| # | Test ID | Spec File | Estado |
|---|---------|-----------|--------|
| 24 | NEG-02 (Timeout) | `j04-f8-f12-negativos.e2e.spec.ts` | ✅ PASSED |
| 25 | NEG-03 (Upload >50MB) | `j04-f8-f12-negativos.e2e.spec.ts` | ✅ PASSED |
| 26 | NEG-06 (Kanban bloqueo) | `j04-f8-f12-negativos.e2e.spec.ts` | ✅ PASSED |

### Diagnóstico & IDOR — 5/5 ✅
| # | Test ID | Spec File | Estado |
|---|---------|-----------|--------|
| 27 | Console Errors | `dump-errors.spec.ts` | ✅ PASSED |
| 28 | Dump HTML | `dump-html.spec.ts` | ✅ PASSED |
| 29 | URL Check | `dump-url.spec.ts` | ✅ PASSED |
| 30 | CU-JSEC-02 (IDOR Cross-Tenant) | `idor-copilot.e2e.spec.ts` | ✅ PASSED |
| 31 | CU-JSEC-02b (Own Session) | `idor-copilot.e2e.spec.ts` | ✅ PASSED |

### CQRS Event Sourcing — 2/2 ✅
| # | Test ID | Spec File | Estado |
|---|---------|-----------|--------|
| 32 | CU-01 (Auto-Claim+Submit) | `us017-cqrs-event-sourcing.spec.ts` | ✅ PASSED |
| 33 | CU-02 (Offline Toast) | `us017-cqrs-toast.spec.ts` | ✅ PASSED |

### CQRS Phase 8-11 — 1/1 ✅
| # | Test ID | Spec File | Estado |
|---|---------|-----------|--------|
| 34 | CU-J04-40 (CQRS Persist) | `phase8-11-degradation-cqrs.spec.ts` | ✅ PASSED |

### Webhook Intake (US-004) — 4/4 ✅
| # | Test ID | Spec File | Estado |
|---|---------|-----------|--------|
| 35 | CU-WH-01 (410 GONE) | `us004-webhook-intake-pipeline.e2e.spec.ts` | ✅ PASSED |
| 36 | CU-WH-02 (410 sin ClientState) | `us004-webhook-intake-pipeline.e2e.spec.ts` | ✅ PASSED |
| 37 | CU-WH-04 (Auto-responder) | `us004-webhook-intake-pipeline.e2e.spec.ts` | ✅ PASSED |
| 38 | CU-WH-NEG-01 (XSS) | `us004-webhook-intake-pipeline.e2e.spec.ts` | ✅ PASSED |
| 39 | CU-WH-NEG-02 (Empty) | `us004-webhook-intake-pipeline.e2e.spec.ts` | ✅ PASSED |

### Kill-Switch (US-036/038) — 3/3 ✅
| # | Test ID | Spec File | Estado |
|---|---------|-----------|--------|
| 40 | CU-KS-02 (Post-revocación 401) | `us036-kill-switch-break-glass.e2e.spec.ts` | ✅ PASSED |
| 41 | CU-KS-NEG-02 (Sin Auth 401) | `us036-kill-switch-break-glass.e2e.spec.ts` | ✅ PASSED |
| 42 | CU-KS-NEG-03 (XSS Fuzzing) | `us036-kill-switch-break-glass.e2e.spec.ts` | ✅ PASSED |

---

## ❌ TESTS FALLIDOS — MAPA DE DAÑOS (36 tests originales, 72 con retries)

### 🔴 CLUSTER 1: DMN Dropdown (B-20) — 1 test
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| `b20-dmn-dropdown.e2e.spec.ts` — Seleccionar BusinessRuleTask muestra dropdown DMN | TimeoutError: Elemento no renderizado | UI/Selector |

### 🔴 CLUSTER 2: Multi-Instance Browser (J-04 F3) — 1 test
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| `j04-f3-multi-instance.e2e.spec.ts` — Multi-Browser claim and execution | Context/Browser concurrency failure | Infraestructura |

### 🔴 CLUSTER 3: Degradación BPMN (F8) — 3 tests
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| CU-J04-35 — Degradación Camunda → banner amber + CQRS OFFLINE | TimeoutError: banner amber no aparece | UI/Intercepción |
| CU-J04-36 — Kanban sigue operando durante degradación Camunda | TimeoutError: Kanban no responde | UI/Intercepción |
| CU-J04-37 — Reiniciar Camunda → banner desaparece → CQRS ONLINE | TimeoutError: banner no desaparece | UI/Intercepción |

### 🔴 CLUSTER 4: Director Firma (F9-F10) — 1 test
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| CU-J04-39 — Director: reclama y completa Firma Final | TimeoutError: Flujo Director no completado | Datos/Seed |

### 🔴 CLUSTER 5: Negativos — 2 tests
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| NEG-01 — Formulario vacío → Zod client bloquea | TimeoutError: Formulario no cargado | UI/Nav |
| NEG-05 — Skipeo sin motivo → botón disabled | TimeoutError: Componente no renderizado | UI/Nav |

### 🔴 CLUSTER 6: Kanban Real Backend (OBS-1) — 1 test
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| `kanban-board.e2e.spec.ts` — Operario interactúa con Kanban real | Element not found / API error | Backend/Seed |

### 🔴 CLUSTER 7: Phase 1 Workdesk (Legacy Specs) — 4 tests
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| CU-J04-01 — Analista N1 accede al Workdesk | TimeoutError: DataGrid no cargado | UI/Routing |
| CU-J04-02 — Panel de métricas y CQRS state | TimeoutError: Panel métricas no visible | UI/Routing |
| CU-J04-03/04 — Semáforo SLA Vivo + Ordenamiento | TimeoutError: SLA timer no renderizado | UI/Routing |
| CU-J04-05 — Filtros facetados y debounce | TimeoutError: Facets no cargados | UI/Routing |

### 🔴 CLUSTER 8: Phase 8-11 Degradation — 1 test
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| CU-J04-35/37 — Degradación BPMN (Intercepción 503) | Intercepción Playwright no capturada | UI/Intercepción |

### 🔴 CLUSTER 9: Smoke Operario — 1 test
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| CU-J04-01→06 — Happy Path Login→Submit→Desaparición | TimeoutError: Flujo E2E no completado | Full-Stack |

### 🔴 CLUSTER 10: Hexagonal Compliance (US-001) — 6 tests
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| CU-HEX-01 — POST /workdesk/attend-next → 200 | HTTP 404 — Endpoint no existe | Backend/API |
| CU-HEX-02 — POST /workdesk/attend-next/skip → 200 | HTTP 404 — Endpoint no existe | Backend/API |
| CU-HEX-03 — GET /feature-toggles/FORCE_ROUTING → 200 | HTTP 404 — Endpoint no existe | Backend/API |
| CU-HEX-04 — PUT /feature-toggles con SUPER_ADMIN → 200 | HTTP 404 — Endpoint no existe | Backend/API |
| CU-HEX-05 — PUT sin ROLE_SUPER_ADMIN → 403 | HTTP 404 — Endpoint no existe | Backend/API |
| CU-HEX-07 — Delegante no autorizado → Toast | TimeoutError: Toast no renderizado | UI/Backend |

### 🔴 CLUSTER 11: Workdesk Delegation (US-001) — 2 tests
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| CA-08 — Feature Toggle Administrativo | TimeoutError: UI no responde | UI/Backend |
| CA-04 — IDOR Protection Delegación | TimeoutError: Flujo delegación no completado | UI/Backend |

### 🔴 CLUSTER 12: Workbox Kanban (US-002) — 3 tests
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| CA-22 Tabs — Interaccionalidad pestañas | TimeoutError: Tabs no renderizados | UI/Routing |
| CA-02 Bulk Claim — Reclamación masiva | TimeoutError: Bulk claim timeout | UI/Backend |
| CA-04 Unclaim — Liberar tarea con Motivo | TimeoutError: Modal unclaim no aparece | UI/Backend |

### 🔴 CLUSTER 13: FormDesigner Persistence (US-003) — 2 tests
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| QA-003-01 — Create and persist dynamic schemas | TimeoutError: Schema no persistido | Backend/API |
| QA-003-02 — GC Purge stale drafts | TimeoutError: Purga no ejecutada | Backend/API |

### 🔴 CLUSTER 14: Webhook Intake (US-004) — 1 test
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| CU-WH-03 — POST /intake/webhook con HMAC → 202 | HTTP != 202 — HMAC validation failure | Backend/Crypto |

### 🔴 CLUSTER 15: DMN Preflight (US-007) — 1 test
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| CA-14 — Simulación DMN Pre-Flight | TimeoutError: DMN execution failure | Backend/Camunda |

### 🔴 CLUSTER 16: Kanban Zero-Mock (US-008) — 2 tests
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| CU-KB-01 — Kanban columnas reales backend | Columns not rendered from API | Backend/Seed |
| CU-KB-02 — Tarjeta en TODO de datos reales | No cards from seed-e2e.sql | Backend/Seed |

### 🔴 CLUSTER 17: Connection Toast (US-017) — 1 test
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| Connection Toast reacciona a eventos de red | Toast no detectado | UI/Event |

### 🔴 CLUSTER 18: Kill-Switch (US-036) — 3 tests
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| CU-KS-01 — SUPER_ADMIN revoca sesión → 200 + Redis | HTTP != 200 — Endpoint issue | Backend/Redis |
| CU-KS-03 — Idempotencia: revocar 2x no causa error | Idempotency check failure | Backend/Redis |
| CU-KS-NEG-01 — OPERARIO revoca → 403 | HTTP != 403 — RBAC miss | Backend/Security |

### 🔴 CLUSTER 19: Draft Recovery / Panic / Whitelist (US-039) — 3 tests
| Test | Error Probable | Categoría |
|------|---------------|-----------|
| QA-039-12 — Amber banner recovery | Banner not shown | UI/LocalStorage |
| QA-039-05 — Panic validation <20 chars | Validation not triggered | UI/Zod |
| QA-039-06 — API whitelist variables | Endpoint issue | Backend/API |

---

## 🔬 AUDITORÍA DE EXCEPCIONES SILENCIOSAS DEL BACKEND

| Hallazgo | Severidad | Detalle |
|----------|-----------|---------|
| `MismatchedInputException` (Camunda External Task Client) | ⚪ INFO | Ruido ambiental esperado — el External Task Client intenta fetch-and-lock workers que no existen en el entorno E2E. **No es bloqueante.** |
| `HttpRequestMethodNotSupportedException` | ⚪ INFO | Algunos tests envían métodos HTTP no soportados intencionalmente (pruebas negativas). Controlado. |
| `NullPointerException` | ✅ NINGUNO | No se detectaron NPEs en los logs del backend durante toda la ejecución de 36.3 minutos. |
| `OutOfMemoryError` | ✅ NINGUNO | Backend estable en memoria durante toda la ejecución. |
| `StackOverflowError` | ✅ NINGUNO | No detectado. |
| **Backend Health Post-Test** | ✅ HTTP 200 | Backend sigue operativo tras la batería completa de 121 test runs. |

---

## 📋 CLASIFICACIÓN DE DAÑOS POR CAUSA RAÍZ

| Causa Raíz | Tests Afectados | Prioridad |
|------------|:---------------:|-----------|
| **Endpoints no implementados** (`attend-next`, `feature-toggles`, `kill-switch`) | ~11 | 🔴 P0 — Backend |
| **UI Timeouts** (Componentes no renderizados tras navegación) | ~12 | 🔴 P0 — Frontend |
| **Degradación BPMN / Intercepción 503** (Simulación Camunda-down) | ~4 | 🟡 P1 — Infraestructura |
| **Seed E2E incompleto** (Datos faltantes para Kanban, Director, DMN) | ~5 | 🟡 P1 — Datos |
| **HMAC / Crypto validation** (Webhook intake) | ~1 | 🟡 P1 — Backend |
| **Concurrencia Multi-Browser** | ~1 | 🟢 P2 — Infraestructura |
| **LocalStorage/Zod frontend** (Draft recovery, panic validation) | ~3 | 🟢 P2 — Frontend |

---

## 🛡️ CUMPLIMIENTO DE LEYES GLOBALES

| Ley | Cumplimiento | Evidencia |
|-----|:------------:|-----------|
| **LG-0 (RAG-First)** | ✅ | 6 archivos de governance leídos antes de ejecutar |
| **LG-1 (Identidad)** | ✅ | Collar [🕵️ QA - E2E] aplicado |
| **LG-2 (Zero-Trust Compilation)** | ✅ | Backend nativo vivo + Playwright real ejecutado |
| **LG-3 (Trazabilidad)** | ✅ | Reporte persistido en `.agentic-sync/` |
| **LG-4 (Inmutabilidad Regresión)** | ✅ | `git diff -- frontend/e2e/certification/` = VACÍO |

---

## 🎯 RECOMENDACIONES PARA EL ARQUITECTO LÍDER

1. **P0 — Backend API Endpoints:** Los tests de `us001-hexagonal-compliance` fallan sistemáticamente por HTTP 404. Los endpoints `/workdesk/attend-next`, `/workdesk/feature-toggles/*` y `/session/revoke` no existen. Requiere implementación o re-ruteo.

2. **P0 — UI Routing/Rendering:** Los `phase1-workdesk.spec.ts` y los `smoke-j04-operario.spec.ts` fallan por timeout en carga de DataGrid. Posible causa: los selectores CSS/ARIA apuntan a una estructura DOM que cambió post-remediación, o hay un problema de precarga de datos.

3. **P1 — Seed E2E:** Los tests de Kanban Zero-Mock (`us008-kanban-zeromock`) no encuentran tarjetas en la columna TODO. Verificar que `seed-e2e.sql` tiene procesos Camunda activos con tareas en estado `CREATED`.

4. **P1 — Degradación BPMN (F8):** Los 3 tests de degradación son timeout puro. Verificar si la lógica de intercepción Playwright (`route.fulfill`) sigue siendo compatible con la estructura actual de las llamadas Camunda REST.

5. **P2 — Kill-Switch Redis:** `CU-KS-01` falla. Verificar que existe el endpoint `/api/admin/session/revoke` y que Redis está configurado para session management en perfil `e2e`.

---

*Fin del reporte. Generado automáticamente por el agente QA SRE.*
*Ningún archivo de test fue modificado durante esta ejecución.*
