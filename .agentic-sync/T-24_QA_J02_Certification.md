# 🧠→🕵️ Handoff: ARQUITECTO LÍDER → QA E2E
# T-24-QA: Certificación E2E Exhaustiva J-02 — 57 Escenarios UAT

**Emitido por:** 🧠 ARQUITECTO LÍDER (Antigravity)
**Destinatario:** 🕵️ QA - E2E
**Fecha:** 2026-05-14T03:34:00-05:00
**Sprint:** 6 — Iteración 7.2
**Prioridad:** 🔴 Alta
**Dependencia:** T-24-FE (Frontend debe inyectar `data-testid` canónicos) | T-24-DB (Infra debe ejecutar seeds)

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3, 4)
cat .cursorrules

# 2. Skill principal del agente QA
cat .agents/skills/qa_e2e_validation_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. Auditoría de brechas (tu mapa de trabajo)
cat .agentic-sync/T-24_UAT_Gap_Analysis.md

# 5. SSOT de Casos de Uso (los 57 escenarios)
cat docs/uat/casos_uso_uat_j02.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> `// @Traceability: Certificación E2E J-02 (T-24)`. Esto es INNEGOCIABLE.

> ⚠️ **LEY GLOBAL 4 — Inmutabilidad de Regresión:** PROHIBIDO modificar aserciones de tests existentes para que pasen. Se arregla el código, NUNCA el test.

---

## 🔬 Diagnóstico del Arquitecto

La auditoría forense v2 sobre `T-24_UAT_Gap_Analysis.md` reveló una cobertura efectiva del **~12%** con **0 escenarios completamente cubiertos** de los 57 definidos en el SSOT UAT.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| `route.fulfill()` — Mockea bandeja completa | `us002-workbox-kanban.spec.ts:L9-35` | Viola ADR-010 Zero-Mock. Toda la data es ficticia |
| `route.fulfill()` — Mockea bulk-claim | `us002-workbox-kanban.spec.ts:L74-76` | Respuesta 200 inventada sin backend |
| `route.fulfill()` — Mockea unclaim | `us002-workbox-kanban.spec.ts:L116-118` | Respuesta 200 inventada sin backend |
| `route.fulfill()` — Mockea `/auth/me` | `us001-workdesk-delegation.spec.ts:L55-59` | Inyecta delegados inexistentes |
| `localStorage.setItem()` — DMN draft | `us007-dmn-preflight.spec.ts:L17-21` | Elude UI de creación DMN |
| `removeAttribute('disabled')` — Bypass Pre-Flight | `us005-bpmn-modeler-persistence.e2e.spec.ts:L59` | Fuerza botón deploy sin validación |
| `catch(() => null)` — Silencia errores | `us005-bpmn-modeler-persistence.e2e.spec.ts:L76` | Oculta fallos de deploy |

---

## 🎯 Instrucciones Quirúrgicas

### REGLA MAESTRA: 1 ARCHIVO .ts POR CADA CASO DE USO

Cada CU del UAT debe tener su propio archivo `.spec.ts` en `frontend/e2e/certification/j02/`. Nomenclatura estricta:

```
frontend/e2e/certification/j02/
├── f1-formularios/
│   ├── cu-j02-01-iform-maestro-auditoria.spec.ts
│   ├── cu-j02-02-formularios-simples.spec.ts
│   ├── cu-j02-03-iform-maestro-evaluacion.spec.ts
│   └── cu-j02-04-validacion-zod.spec.ts
├── f2-dmn-bpmn/
│   ├── cu-j02-05-crear-tabla-dmn.spec.ts
│   ├── cu-j02-06-importar-bpmn.spec.ts
│   ├── cu-j02-07-vincular-formkey.spec.ts
│   ├── cu-j02-08-vincular-decisionref.spec.ts
│   └── cu-j02-09-exportar-bpmn.spec.ts
├── f3-deploy/
│   ├── cu-j02-10-preflight.spec.ts
│   └── cu-j02-11-deploy-rm.spec.ts
├── f5-generico/
│   └── cu-j02-k01-kanban-generico.spec.ts
├── f6-observabilidad/
│   ├── cu-j02-obs-01-dashboard-bam.spec.ts
│   ├── cu-j02-obs-02-historial-motor.spec.ts
│   └── cu-j02-obs-03-audit-log-modeler.spec.ts
├── f7a-workdesk/
│   ├── cu-j02-w01-paginacion.spec.ts
│   ├── cu-j02-w02-busqueda.spec.ts
│   ├── cu-j02-w03-filtros-facetados.spec.ts
│   ├── cu-j02-w04-semaforo-sla.spec.ts
│   ├── cu-j02-w05-recalculo-inactividad.spec.ts
│   ├── cu-j02-w06-consolidacion-grilla.spec.ts
│   ├── cu-j02-w07-keepalive.spec.ts
│   ├── cu-j02-w08-websocket.spec.ts
│   ├── cu-j02-w09-delegacion.spec.ts
│   └── cu-j02-w10-attend-next.spec.ts
├── f7b-claim/
│   ├── cu-j02-c01-reclamo-individual.spec.ts
│   ├── cu-j02-c02-concurrencia.spec.ts
│   ├── cu-j02-c03-bulk-claim.spec.ts
│   ├── cu-j02-c04-exploracion-readonly.spec.ts
│   ├── cu-j02-c05-liberacion-amnesia.spec.ts
│   ├── cu-j02-c06-despojo-forzoso.spec.ts
│   ├── cu-j02-c07-trazabilidad-popup.spec.ts
│   └── cu-j02-c08-separacion-cola-bandeja.spec.ts
├── f7c-kanban/
│   ├── cu-j02-a01-crear-tablero.spec.ts
│   ├── cu-j02-a02-crud-tarjetas.spec.ts
│   ├── cu-j02-a03-drag-drop-ws.spec.ts
│   ├── cu-j02-a04-blocked-modal.spec.ts
│   ├── cu-j02-a05-time-tracking.spec.ts
│   ├── cu-j02-a06-inmutabilidad-done.spec.ts
│   └── cu-j02-a07-formulario-generico.spec.ts
└── negativos/
    ├── cu-j02-neg-01-form-sin-campos.spec.ts
    ├── cu-j02-neg-02-datos-invalidos.spec.ts
    ├── cu-j02-neg-03-deploy-sin-formkey.spec.ts
    ├── cu-j02-neg-04-designer-sin-rol.spec.ts
    ├── cu-j02-neg-05-decisionref-huerfano.spec.ts
    ├── cu-j02-neg-06-obs-invalidas.spec.ts
    ├── cu-j02-neg-07-director-rechaza.spec.ts
    ├── cu-j02-neg-08-hard-limit-paginacion.spec.ts
    ├── cu-j02-neg-09-idor-delegacion.spec.ts
    ├── cu-j02-neg-10-rate-limiting.spec.ts
    ├── cu-j02-neg-11-dto-sanitizacion.spec.ts
    ├── cu-j02-neg-12-cross-team-despojo.spec.ts
    ├── cu-j02-neg-13-optimistic-rollback.spec.ts
    ├── cu-j02-neg-14-exceder-columnas.spec.ts
    ├── cu-j02-neg-15-editar-done.spec.ts
    ├── cu-j02-neg-16-doble-asignacion.spec.ts
    └── cu-j02-neg-17-borrar-timelog.spec.ts
```

### Plantilla Obligatoria por Archivo

Cada archivo `.spec.ts` DEBE seguir esta estructura:

```typescript
import { test, expect } from '@playwright/test';
// @Traceability: Certificación E2E J-02 (T-24) — CU-J02-XXX

/**
 * CU-J02-XXX: [Título del Caso de Uso]
 * @US: US-XXX
 * @CAs: CA-XX, CA-YY
 * @Fase: [N]
 * @SSOT: docs/uat/casos_uso_uat_j02.md (líneas XXX-YYY)
 * @ZeroMock: true — PROHIBIDO route.fulfill(), localStorage mock
 */
test.describe('CU-J02-XXX: [Título]', () => {
  test.use({ storageState: 'e2e/playwright/.auth/[rol].json' });

  test('Paso N: [Descripción del paso UAT]', async ({ page }) => {
    // Aserción contra backend REAL
  });
});
```

### Escenarios NO APLICA (Fase 4)

Los siguientes 8 CUs están marcados como 🔵 NO APLICA porque requieren motor Camunda con proceso desplegado activo. **Créalos como `.spec.ts` con `test.skip()` documentado:**

- CU-J02-F1-01 a F1-06, F2-01, F3-01, F4-01

```typescript
// @Traceability: Certificación E2E J-02 (T-24) — CU-J02-F1-01
test.describe('CU-J02-F1-01: Iniciar caso siniestro', () => {
  test.skip(true, '🔵 NO APLICA — Requiere Motor Camunda activo con proceso desplegado');
  test('Placeholder para Sprint 7+', async () => {});
});
```

---

## ✅ Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | 49 archivos `.spec.ts` creados (57 CUs - 8 NO APLICA = 49 activos) | `find frontend/e2e/certification/j02 -name "*.spec.ts" \| wc -l` → 49+ |
| 2 | 0 instancias de `route.fulfill()` en archivos nuevos | `grep -r "route.fulfill" frontend/e2e/certification/j02/` → 0 resultados |
| 3 | 0 instancias de `localStorage.setItem` como mock de estado | `grep -r "localStorage.setItem" frontend/e2e/certification/j02/` → 0 resultados |
| 4 | 0 instancias de `removeAttribute('disabled')` | `grep -r "removeAttribute" frontend/e2e/certification/j02/` → 0 resultados |
| 5 | Cada archivo contiene `// @Traceability: Certificación E2E J-02 (T-24)` | `grep -rL "@Traceability" frontend/e2e/certification/j02/` → 0 resultados |
| 6 | `npx playwright test --grep "CU-J02" --reporter=list` ejecuta sin errores de sintaxis | Exit code 0 o tests skipped (no errores TS) |
| 7 | Commit en `sprint-6`: `test(j02): add 49 E2E specs for UAT certification` | `git log -1 --oneline` |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Leer `.cursorrules` + Skills listados en Sección 2.
2. Leer `T-24_UAT_Gap_Analysis.md` para entender las 7 violaciones actuales.
3. Leer `docs/uat/casos_uso_uat_j02.md` completo (1427 líneas, 57 CUs).
4. Crear estructura de carpetas `frontend/e2e/certification/j02/`.
5. Para cada CU: abrir el UAT, localizar los pasos, y crear el `.spec.ts` con aserciones reales.
6. Verificar Zero-Mock: `grep -r "route.fulfill\|localStorage.setItem\|removeAttribute" frontend/e2e/certification/j02/` → 0.
7. Compilar: `npx playwright test --grep "CU-J02" --reporter=list` (validar sintaxis).
8. Commit: `git add frontend/e2e/certification/j02/ && git commit -m "test(j02): add 49 E2E specs for UAT J-02 certification // @Traceability: T-24" && git push origin sprint-6`.

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de [🕵️ QA - E2E].

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/qa_e2e_validation_audit/SKILL.md
3. cat .agents/skills/zero_mock_enforcement/SKILL.md
4. cat .agents/skills/clean_code_standards/SKILL.md
5. cat .agentic-sync/T-24_UAT_Gap_Analysis.md
6. cat docs/uat/casos_uso_uat_j02.md
7. cat .agentic-sync/T-24_QA_J02_Certification.md

TU MISIÓN:

1. Crear 49 archivos .spec.ts (1 por CU) en frontend/e2e/certification/j02/ organizados por fase.
2. Cada test DEBE interactuar con la UI real y asertar contra el backend Zero-Mock.
3. Los 8 CUs de Fase 4 (F1-01 a F4-01) van con test.skip("🔵 NO APLICA").
4. Cada archivo DEBE tener el header @Traceability y @SSOT con líneas del UAT.
5. Ejecutar: npx playwright test --grep "CU-J02" --reporter=list
6. Commit: git add . && git commit -m "test(j02): add 49 E2E specs for UAT J-02 certification" && git push origin sprint-6

REGLAS INQUEBRANTABLES:
- PROHIBIDO usar route.fulfill() — Zero-Mock V2 (ADR-010).
- PROHIBIDO usar localStorage.setItem() como atajo de estado.
- PROHIBIDO usar removeAttribute('disabled') o expect.soft() como silenciador.
- PROHIBIDO modificar tests existentes (LEY GLOBAL 4).
- OBLIGATORIO documentar con // @Traceability en CADA archivo.
- Fixtures XML BPMN/DMN son PERMITIDOS vía __modelerInstance.importXML() para Fase 2.
- Panel de Propiedades: OBLIGATORIO asertar formKey/decisionRef desde el sidebar UI.
```

---

> // @Traceability: Certificación E2E J-02 (T-24)
