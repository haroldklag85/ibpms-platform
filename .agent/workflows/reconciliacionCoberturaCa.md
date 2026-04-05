---
description: Workflow para reconciliar la Matriz de Cobertura de implementación contra el SSOT, los handoffs y el historial de Git, detectando discrepancias entre lo planeado, lo delegado y lo realmente construido.
---

# Reconciliación de Cobertura por Criterio de Aceptación

> **Versión:** 1.0 | **Última Actualización:** 2026-04-05
> **Responsable:** Agente Arquitecto Líder
> **Dependencias:** `.agentic-sync/coverage_matrix.md`, `v1_user_stories.md`, `.agentic-sync/handoff_*.md`

## Contexto

La Matriz de Cobertura (`.agentic-sync/coverage_matrix.md`) es mantenida por los agentes de desarrollo y el Arquitecto. Pero puede desincronizarse. Este workflow cruza 4 fuentes independientes para detectar discrepancias.

## Triggers

- Al finalizar un Sprint o iteración de auditoría.
- Cuando el Humano sospeche que un CA fue marcado como ✅ sin evidencia real.
- Antes de ejecutar un merge a `main` (auditoría pre-merge).

## Flujo de Reconciliación (4 Fases)

### FASE 1: Inventario del Universo de CAs (Fuente: SSOT)

1. Usa `grep_search` con `Query: "Scenario:"` e `Includes: ["v1_user_stories.md"]` para obtener la lista completa de CAs definidos para la US objetivo.
2. Cuenta el total de CAs. Este es el "universo" (100%).
3. Identifica cuáles tienen el marcador `[REMEDIACIÓN]` (son CAs nacidos de auditorías).

### FASE 2: Inventario de Delegaciones (Fuente: Handoffs)

1. Usa `grep_search` con `Query: "US-{XXX}"` sobre `.agentic-sync/` para encontrar todos los handoffs que referencian la US.
2. Lee cada handoff para extraer qué rango de CAs fue delegado y a qué rol (Backend/Frontend).
3. Registra en un mapa: `{CA-N: {delegado_backend: true, delegado_frontend: true}}`.

### FASE 3: Inventario de Commits Reales (Fuente: Git)

1. Ejecuta en terminal:
   ```bash
   git log --all --oneline --grep="US-{XXX}" --grep="CA-" --all-match
   ```
2. Parsea los mensajes de commit para extraer qué CAs tienen código real commiteado.
3. Registra en el mapa: `{CA-N: {commit_existe: true, rama: "sprint-X/...", autor: "agente"}}`.

### FASE 4: Detección de Discrepancias

Cruza las 3 fuentes y genera un reporte con las siguientes categorías:

| Categoría | Significado | Acción |
|-----------|-------------|--------|
| ✅ **Consistente** | CA en SSOT + delegado en handoff + commit real + marcado ✅ en matriz | Ninguna. Todo alineado. |
| ⚠️ **Falso Positivo** | CA marcado ✅ en matriz PERO sin commit real en git | Investigar. ¿Se perdió el commit? ¿El agente mintió? Degradar a ⏳. |
| ⚠️ **Commit Huérfano** | Commit real referenciando un CA que NO existe en el SSOT | Investigar. ¿Es un CA obsoleto renumerado? ¿Alucinación del agente? |
| ⚠️ **Delegación Sin Ejecución** | Handoff creado pero sin commit correspondiente | El agente recibió la tarea pero no la completó. Escalar al Humano. |
| ❌ **Sin Cobertura** | CA existe en SSOT pero no tiene handoff, ni commit, ni entrada en matriz | Trabajo pendiente. Priorizar en siguiente Sprint. |
| 🚫 **Excluido Correctamente** | CA marcado como V2+ o fuera de alcance | Verificar que realmente esté fuera del scope de MoSCoW. |

### Formato del Reporte de Reconciliación

```markdown
# 🔍 Reporte de Reconciliación — US-{XXX}
> **Fecha:** YYYY-MM-DD | **Ejecutado por:** Arquitecto Líder

## Resumen
| Métrica | Valor |
|---------|-------|
| Total CAs en SSOT | XX |
| Consistentes (✅) | XX (XX%) |
| Falsos Positivos (⚠️) | XX |
| Commits Huérfanos (⚠️) | XX |
| Sin Cobertura (❌) | XX |
| Excluidos V2 (🚫) | XX |

## Detalle de Discrepancias
| CA | SSOT | Handoff | Commit | Matriz | Veredicto |
|----|------|---------|--------|--------|-----------|
| CA-43 | ✅ | ✅ Back+Front | ✅ abc1234 | ✅ | Consistente |
| CA-87 | ✅ | ❌ | ❌ | ❌ | Sin Cobertura |
| CA-12 | ✅ | ✅ Back | ✅ def5678 | ❌ | Matriz desactualizada |
```

## Validaciones Finales

- [ ] Todo CA con commit tiene entrada en la Matriz.
- [ ] Todo CA marcado ✅ en la Matriz tiene al menos 1 commit verificable.
- [ ] Los CAs `[REMEDIACIÓN]` tienen trazabilidad al ticket REM de origen.
- [ ] No hay commits que referencien CAs inexistentes (renumerados o fantasma).

## Anti-Patrones

1. **NO confiar solo en la Matriz.** La Matriz es declarativa. Git es la verdad empírica.
2. **NO ejecutar este workflow en medio de un Sprint activo** donde los agentes aún están committeando. Esperar al cierre del bloque de trabajo.
3. **NO marcar CAs como ✅ "porque el handoff existe".** Un handoff es una delegación, no una implementación.
