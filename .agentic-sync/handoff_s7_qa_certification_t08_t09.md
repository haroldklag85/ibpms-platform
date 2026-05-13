# 🧠→🕵️ Handoff: Arquitecto Líder → QA
# T-08 y T-09: Certificación E2E de Refactorización Frontend

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🕵️ QA
**Fecha:** 2026-05-12T11:34:28-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🔴 Alta
**Dependencia:** T-08 y T-09 (Handoff Frontend) DEBEN estar completados

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (Leyes Globales)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/qa_e2e_validation_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes
cat docs/architecture/adr_010_zero_mock_testing.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación @Traceability o comentario `// @Traceability: US-XXX, CA-XX`. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

El Frontend ha eliminado deudas técnicas importantes (SLA basado en setInterval y Kanban con arrays mockeados). Estas piezas son críticas para la experiencia del usuario y cualquier regresión bloqueará la plataforma.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Integridad E2E | `frontend/src/tests/views/kanban/KanbanView.spec.ts` | Las pruebas unitarias/E2E previas podrían fallar tras eliminar los arrays estáticos, ya que ahora esperan interactuar con interceptores de Axios reales. |
| Cobertura SLA | `frontend/src/tests/components/common/UniversalSlaTimer.spec.ts` | Se debe verificar empíricamente que el SLA reacciona al `timeStore` y que Vitest aprueba el mockeo del reloj central. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Actualizar Specs de Vitest (Kanban)

**Archivo:** `frontend/src/tests/views/kanban/KanbanView.spec.ts`

Inyectar interceptores HTTP o mocking de Axios a nivel de Vitest para simular respuestas desde `/api/v1/kanban`.

```typescript
import { vi } from 'vitest';
import apiClient from '@/services/apiClient';
// @Traceability: US-008, CA-12

vi.mock('@/services/apiClient');

describe('KanbanView.vue Integración', () => {
  it('Debe obtener tareas del backend al montar', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce({ data: [] });
    // Validar el renderizado sin arrays quemados
  });
});
```

### Paso 2: Certificar "Green Build" Global

**Archivo:** `task.md` (y reportes de QA)

Garantizar que todas las pruebas pasen y documentar el certificado en el archivo de seguimiento `task.md` cambiando de `Pendiente` a `✅ CERTIFICADO`.

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Pruebas de SLA Exitosas | `cd frontend && npx vitest run UniversalSlaTimer.spec.ts` → PASS |
| 2 | Pruebas de Kanban Exitosas | `cd frontend && npx vitest run KanbanView.spec.ts` → PASS |
| 3 | Trazabilidad E2E | `grep "@Traceability: US-008" frontend/src/tests/views/kanban/KanbanView.spec.ts` → Retorna coincidencias |
| 4 | Test Suite Green Build | `cd frontend && npm run test:unit` → PASS |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Modificar `KanbanView.spec.ts` para adaptarse a la API REST.
2. Ejecutar suite unitaria: `cd frontend && npm run test:unit`
3. Actualizar `task.md` indicando `✅ CERTIFICADO` para T-08 y T-09.
4. Commit: `git add . && git commit -m "test(qa): certificar refactor de SLA y Zero-Mock Kanban (T-08, T-09)" && git push`

---

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 🕵️ QA.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/qa_e2e_validation_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat .agents/skills/zero_mock_enforcement/SKILL.md
5. cat docs/architecture/adr_010_zero_mock_testing.md
6. cat .agentic-sync/handoffs/handoff_s7_qa_certification_t08_t09.md

TU MISIÓN:

1. Ejecutar el Paso 1 de las Instrucciones Quirúrgicas del handoff (Actualizar Specs Vitest de Kanban).
2. Garantizar que todos los Tests de Componentes (Workdesk, UniversalSlaTimer, KanbanView) pasen correctamente tras los cambios.
3. Actualizar `task.md` a ✅ CERTIFICADO para las tareas T-08 y T-09.
4. Commit: `git add . && git commit -m "test(qa): certificar refactor de SLA y Zero-Mock Kanban (T-08, T-09)" && git push`

REGLAS INQUEBRANTABLES:
- DEBES incluir "// @Traceability: US-008, CA-12" en los specs de pruebas de Kanban (LEY GLOBAL 3).
- PROHIBIDO saltarse pruebas con `it.skip` para lograr el PASS artificialmente.
- DEBES reportar cualquier falla al Arquitecto Líder si los componentes fallan de forma crítica.
```
