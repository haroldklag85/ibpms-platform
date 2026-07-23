# Handoff QA — Certificación de Brecha de Integración (Tasks & BulkAssign)

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | Deuda Técnica de Integración (Iteración 5 / sprint-6) |
| **Rama Git** | `sprint-6` |
| **Deuda a Certificar** | Cierre de Gap de Integración en Frontend (`TaskService.ts`) |
| **Dependencia** | Ejecutar DESPUÉS de que Frontend notifique su finalización |

---

## 2. Puntos de Certificación (Checkpoints)

Verifica que el Agente Frontend corrigió las rutas y agregó la funcionalidad faltante.

| ID | Checkpoint | Método de Verificación | Resultado Esperado |
|----|-----------|----------------------|-------------------|
| **QA-INT-01** | Erradicación de `/tareas` | `grep "/tareas" frontend/src/services/TaskService.ts` | **SIN RESULTADOS** (0 matches) |
| **QA-INT-02** | Rutas corregidas a `/tasks` | `grep "/tasks" frontend/src/services/TaskService.ts` | Al menos 5 matches |
| **QA-INT-03** | Método bulkAssign | `grep "bulkAssign" frontend/src/services/TaskService.ts` | Implementación presente apuntando a `/agile/projects/` |
| **QA-INT-04** | Typescript OK | `cd frontend && npm run type-check` (o equivalente) | Pass sin errores de TS |

---

## 3. Reporte de Certificación (Template)

```markdown
## Reporte de Certificación QA — Integration Gap Tasks

| ID | Checkpoint | Estado | Evidencia |
|----|-----------|--------|-----------|
| QA-INT-01 | Erradicación /tareas | ✅/❌ | grep vacío |
| QA-INT-02 | Rutas corregidas | ✅/❌ | X matches encontrados |
| QA-INT-03 | Método bulkAssign | ✅/❌ | Presente y correcto |
| QA-INT-04 | Typescript Build | ✅/❌ | PASS |

**Veredicto:** PASS / FAIL
```
