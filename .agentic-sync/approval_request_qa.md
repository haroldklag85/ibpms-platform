# 🔍 Solicitud de Aprobación QA — Sprint 6.1 (UAT Certification)

> **De:** Agente SDET / UAT Automation Engineer
> **Para:** Arquitecto Líder
> **Fecha:** 2026-04-19
> **Sprint:** 6 — Iteración 6.1

## 📋 Resumen del Plan Propuesto (Zero-Trust UAT)

En acato a sus directivas en `handoff_s6_qa.md` y la pirámide de testing (ADR-010), he generado mi plan de implementación para certificar la Iteración 6.1 de manera completamente empírica usando el Backend vivo (vía Docker) y prohibiendo interceptiones simuladas (`page.route`).

### Fases de Ejecución:
1. **Lote 1 (B1 y B2):** Revisión de Healthchecks (Camunda, RabbitMQ, DB, Backend) y aprovisionamiento de Fixtures E2E (Usuarios Alpha/Beta en `e2e-data.ts`).
2. **Lote 2 (B3 - P0 Certs):** Tests exclusivos backend (`request` de Playwright). Validación de IDOR en BpmnCopilotController (CU-JSEC-02) y rechazo en Webhook Legacy 410 (CU-JSEC-17).
3. **Lote 3 (B4 - Smoke J-04):** Flujo de operador MVP. Transita `Login -> Workdesk -> Claim -> Form -> Submit -> Clear`. Tolerancias de tiempos amplias.
4. **Lote 4 (B5 - Componentes Core):** Constatación visual contra la base de datos real del dropdown E2E DMN (B-20) y tablero Kanban sin *mocks*.
5. **Lote 5 (Gobernanza):** Atualización rigurosa de `.agentic-sync/coverage_matrix.md` declarando `✅` (Exitoso) o `❌` (Bug); excluyendo US-017 con `⏭️ SKIP`. Terminando en el acta de cierre `cierre_iteracion_s6_1.md`.

## 🛑 Permiso de Ejecución

Arquitecto, el plan de certificación está preparado. Confirme que la estrategia es acorde al mandato, y respóndame formalmente otorgándome el cambio a **modo EXECUTION**.

---

# 🏛️ VEREDICTO DEL ARQUITECTO LÍDER — Sprint 6.1 QA

**Fecha:** 2026-04-19  
**Emisor:** Arquitecto Líder SW  
**Documento Evaluado:** `implementation_plan.md` (Agente QA — conversación `181d4691`)  
**Handoff de Referencia:** `.agentic-sync/handoff_s6_qa.md`

---

## ✅ VEREDICTO: APROBADO CON 4 OBSERVACIONES OBLIGATORIAS

El plan demuestra comprensión correcta de la estrategia multi-lote, respeta el principio Zero-Trust (sin `page.route`), y alinea los 5 lotes con el handoff. Sin embargo, existen desviaciones menores que DEBEN corregirse durante la fase EXECUTION.

---

### Auditoría de Cobertura (Handoff vs Plan)

| Bloque | Handoff | Plan del Agente | Veredicto |
|:------:|---------|-----------------|:---------:|
| **B1** Verificar Docker Compose | Healthchecks: PostgreSQL, Redis, Camunda, RabbitMQ, Backend actuator | ✅ Mencionados correctamente | ✅ PASS |
| **B2** Fixtures E2E | Crear `e2e-data.ts` con TENANTS, USERS, API constants | ✅ Mencionado explícitamente | ✅ PASS |
| **B3** Specs P0 IDOR + Webhook | 2 archivos spec: `idor-copilot.e2e.spec.ts`, `webhook-legacy.e2e.spec.ts` | ✅ Ambos archivos nombrados y CU-UAT referenciados | ✅ PASS |
| **B4** Smoke J-04 | `smoke-j04-operario.e2e.spec.ts` — Login→Claim→Form→Submit→RYOW | ✅ Flujo completo descrito con timeouts extendidos | ✅ PASS |
| **B5** B-20 + Kanban | `b20-dmn-dropdown.e2e.spec.ts` + `kanban-board.e2e.spec.ts` (nuevo — no estaba en handoff) | ✅ + ⚠️ VER OBS-1 |
| **Gobernanza** | Reconciliación coverage_matrix + cierre formal | ✅ Lote 5 cubre ambos | ✅ PASS |

---

### Observaciones Obligatorias (DEBEN cumplirse en EXECUTION)

#### OBS-1: Spec Kanban — Nomenclatura y alineación ⚠️

El handoff prescribía un único spec B5 con 2 tests (DMN dropdown + Kanban). El agente lo descompuso en 2 archivos separados:
- `b20-dmn-dropdown.e2e.spec.ts` ← ✅ correcto
- `kanban-board.e2e.spec.ts` ← ⚠️ nuevo, no prescrito en el handoff

> **Instrucción:** Acepto la descomposición en 2 archivos (es más limpio). Sin embargo, el spec de Kanban **DEBE verificar**:
> 1. Que `KanbanView` carga datos desde la API real (`/api/v1/kanban/board`) — NO mocks hardcodeados.
> 2. Que al mover una tarjeta a "BLOCKED", aparece el modal con textarea de mínimo 10 caracteres.
> 3. Que las tarjetas en estado "DONE" son de solo lectura (botones deshabilitados).
> Si alguna de estas verificaciones NO es posible porque el Backend no expone el endpoint Kanban aún, documentarlo como `❌ BUG — Backend: Endpoint /api/v1/kanban/board no implementado` en el reporte de cierre.

#### OBS-2: Secuencia de Ejecución — Backend y Frontend DEBEN haber terminado ⚠️

El plan NO menciona explícitamente la **pre-condición secuencial**: Backend Y Frontend deben haber hecho `git push` ANTES de que QA inicie. 

> **Instrucción:** Antes de ejecutar CUALQUIER test, asegúrate de:
> 1. `git pull origin sprint-6/uat-certification` — traer TODO lo que Backend y Frontend pushearon.
> 2. `npm install` (en `frontend/`) — actualizar dependencias si se añadieron nuevas.
> 3. `npm run build` — verificar que el Frontend compila sin errores antes de abrir Playwright.
> Solo entonces inicia los lotes de pruebas.

#### OBS-3: Comando de ejecución Playwright — Config correcto ⚠️

El plan indica: `npx playwright test --project=chromium` dirigido a la carpeta `certification`.

> **Instrucción:** Usa el config E2E creado por Frontend, NO el default:
> ```bash
> npx playwright test --config=playwright.e2e.config.ts
> ```
> El `playwright.e2e.config.ts` (creado por Frontend como B3) apunta a `testDir: './e2e/certification'` y define el proyecto `e2e-certification` con dependencia en `e2e-auth-setup`. NO uses `--project=chromium` ya que el config E2E tiene sus propios projects configurados.

#### OBS-4: Reporte de Evidencia — Screenshots/Video obligatorios ⚠️

El plan no menciona la adjunción de evidencia visual exigida por `qa_e2e_validation_audit/SKILL.md` §3.

> **Instrucción:** Todo reporte de lote DEBE incluir:
> - Resumen de tests (passed/failed/skipped) en texto.
> - Screenshots de tests fallidos (automáticos de Playwright en `test-results/`).
> - Reporte HTML en `playwright-report/index.html`. 
> - Si hay errores de consola JS, capturarlos y adjuntarlos.

---

### Decisiones Arquitectónicas Confirmadas

1. **Playwright `request` context para B3 (P0 IDOR):** ✅ Correcto. Los tests P0 son REST puro (sin browser). Usar `request.post()` / `request.delete()` directamente contra el backend es la estrategia correcta para certificación de APIs.

2. **Timeouts extendidos para B4 (Smoke J-04):** ✅ Correcto. El flujo contra backend real + Camunda + PostgreSQL necesita tolerancias amplias. Los 90s del handoff son razonables.

3. **Exclusión US-017 como `⏭️ SKIP`:** ✅ Correcto. US-017 (CQRS/Event Store) no fue desarrollada. Documentar como deuda V2.

---

### Instrucciones de Ejecución Post-Aprobación

1. `git pull origin sprint-6/uat-certification` — traer cambios de Backend + Frontend.
2. `cd frontend && npm install && npm run build` — compilar Frontend actualizado.
3. Verificar Docker E2E: `docker compose -f docker-compose.e2e.yml up -d` → healthchecks verdes.
4. Ejecutar por lotes secuenciales (Lote 1 → Lote 5). NO ejecutar lotes en paralelo.
5. `npx playwright test --config=playwright.e2e.config.ts` para cada lote.
6. Capturar evidencia: screenshots, video, reporte HTML.
7. Reconciliación: actualizar `coverage_matrix.md` con columna `E2E Real S6`.
8. Cierre: generar `cierre_iteracion_s6_1.md` según workflow Fase 5+6.
9. `git add . && git commit -m "qa: certificación E2E iteración 6.1" && git push`.

---

**ESTADO: ✅ APROBADO — Procede a modo EXECUTION aplicando las 4 observaciones. Recuerda: NO inicies hasta que Backend y Frontend hayan terminado y pusheado.**
