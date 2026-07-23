---
description: >
  Workflow de auditoría automatizada que detecta drift (desalineación) entre la estructura
  del repositorio de requerimientos (docs/requirements/) y los artefactos de gobernanza
  del enjambre (.cursorrules, .agents/skills/, .agent/workflows/). Genera un plan de
  acción correctiva clasificado por prioridad.
version: 1.0.0
triggers:
  - "Después de cualquier reestructuración del repositorio de requerimientos (nueva Épica, renombrado, migración)."
  - "Al inicio de cada Sprint como validación de integridad preventiva."
  - "Cuando un agente reporte errores de 'archivo no encontrado' o referencias rotas."
  - "Cuando el Humano sospeche que los artefactos de gobernanza están desincronizados."
applies_to:
  - Arquitecto Líder
---

# 🔍 Workflow: Reconciliación de Gobernanza vs Estructura de Requerimientos

> **Versión:** 1.0 | **Fecha de creación:** 2026-04-14
> **Responsable:** Agente Arquitecto Líder
> **Entregable:** `.agentic-sync/governance_reconciliation_report_YYYY-MM-DD.md`

## Contexto

Los artefactos de gobernanza del enjambre (`.cursorrules`, skills y workflows) contienen referencias
a archivos del repositorio de requerimientos (`docs/requirements/`). Cuando la estructura de
requerimientos cambia (nuevas Épicas, migraciones, renombrados), estas referencias pueden quedar
obsoletas, causando que los agentes sigan rutas incorrectas, lean archivos deprecados o fallen
silenciosamente.

Este workflow automatiza la detección de ese drift y genera un plan de acción correctiva priorizado.

## Roles autorizados

- **Ejecutor:** Agente Arquitecto Líder (único).
- **Validador:** Humano (aprueba las correcciones antes de aplicarlas).
- **Excluidos:** Todos los demás agentes — este workflow es una meta-auditoría de la infraestructura de gobernanza.

---

## Flujo de Trabajo (4 Fases Estrictas)

### FASE 1: Inventario de la Estructura de Requerimientos Actual

**Objetivo:** Obtener el estado real del repositorio de requerimientos.

**Pasos:**

1. Ejecutar `list_dir` sobre `docs/requirements/` para obtener el árbol de primer nivel:
   ```
   Archivos esperados: v1_user_stories_index.md, v1_user_stories_registry.json,
   v1_moscow_scope_validation.md, functional_requirements.md, non_functional_requirements.md
   ```

2. Ejecutar `list_dir` sobre `docs/requirements/epics/` para obtener todos los archivos de Épica:
   ```
   Archivos esperados: epic_A_*.md, epic_B_*.md, epic_C_*.md, ...
   ```

3. Leer `v1_user_stories_registry.json` y extraer la lista de archivos referenciados en el campo `file`.

4. **Validación cruzada:**
   - ¿Cada archivo en el campo `file` del registry realmente existe en disco? → Si no, marcar como `REGISTRY_DESINCRONIZADO`.
   - ¿Hay archivos en `epics/` que no aparecen en el registry? → Si sí, marcar como `ARCHIVO_HUÉRFANO`.

5. Generar el inventario consolidado:
   ```markdown
   ## Inventario de Estructura de Requerimientos
   | Archivo | Existe en disco | Existe en Registry | Estado |
   |---------|:---:|:---:|--------|
   | epics/epic_A_motor_core.md | ✅ | ✅ | OK |
   | epics/epic_F_nuevo.md | ✅ | ❌ | HUÉRFANO |
   ```

---

### FASE 2: Escaneo de Referencias en Artefactos de Gobernanza

**Objetivo:** Extraer todas las referencias a `docs/requirements/` que existen en rules, skills y workflows.

**Ejecutar los siguientes comandos PowerShell:**

```powershell
# 2.1 — Escanear .cursorrules
Select-String -Path ".cursorrules" -Pattern "docs/requirements|v1_user_stories|v1_moscow|functional_requirements|non_functional_requirements" |
  Select-Object LineNumber, Line

# 2.2 — Escanear todos los Skills
Get-ChildItem -Path ".agents\skills" -Recurse -Include "*.md" |
  ForEach-Object { Select-String -Path $_.FullName -Pattern "docs/requirements|v1_user_stories|v1_moscow|functional_requirements|non_functional_requirements" } |
  Select-Object @{N='File';E={Split-Path $_.Path -Leaf}}, LineNumber, Line

# 2.3 — Escanear todos los Workflows
Get-ChildItem -Path ".agent\workflows" -Recurse -Include "*.md" |
  ForEach-Object { Select-String -Path $_.FullName -Pattern "docs/requirements|v1_user_stories|v1_moscow|functional_requirements|non_functional_requirements" } |
  Select-Object @{N='File';E={Split-Path $_.Path -Leaf}}, LineNumber, Line
```

**Para cada resultado, extraer:**
- Archivo de gobernanza donde se encontró la referencia.
- Número de línea.
- Path referenciado (ej. `v1_user_stories.md` (deprecado), `epics/epic_A_motor_core.md`).
- Tipo de referencia (lectura, escritura, dependencia, prohibición).

**Filtro importante:** Las líneas que contengan `PROHIBIDO` o `deprecado` son referencias de advertencia intencionales y deben clasificarse como `ADVERTENCIA_INTENCIONAL`, no como referencias activas.

---

### FASE 3: Detección de Discrepancias

**Objetivo:** Cruzar el inventario de la Fase 1 con las referencias de la Fase 2 y clasificar cada hallazgo.

**Tabla de clasificación:**

| Código | Categoría | Prioridad | Significado | Acción Correctiva |
|--------|-----------|:---------:|-------------|-------------------|
| `REF-OK` | ✅ Alineado | — | Referencia apunta a un archivo que existe en la estructura actual | Ninguna |
| `REF-ROTA` | 🔴 Referencia Rota | **CRÍTICA** | Referencia apunta a un archivo que NO existe o fue eliminado | Corregir referencia inmediatamente |
| `REF-DEPREC` | 🔴 Referencia a Monolito | **CRÍTICA** | Referencia usa `v1_user_stories.md` (monolito deprecado) sin ser una advertencia intencional | Migrar al nuevo protocolo (índice → épica) |
| `REF-OBSOLETA` | 🟡 Patrón Obsoleto | **MEDIA** | Referencia usa `grep_search` sobre archivos `.md` de requerimientos | Migrar a PowerShell `Select-String` |
| `ADV-OK` | ✅ Advertencia Intencional | — | Línea contiene `PROHIBIDO`/`deprecado` junto a `v1_user_stories.md` | Ninguna (es intencional) |
| `HUERFANO` | ⚠️ Archivo Huérfano | **BAJA** | Archivo existe en `epics/` pero ningún artefacto de gobernanza lo referencia | Verificar si es pendiente de integración |
| `REG-DESINC` | ⚠️ Registry Desincronizado | **MEDIA** | El registry JSON no refleja el estado actual de `epics/` | Ejecutar workflow `sincronizacionRegistryJson.md` |

**Reglas de clasificación:**
- Si una línea contiene `v1_user_stories.md` Y NO contiene `PROHIBIDO|deprecado|DEPRECADO` → `REF-DEPREC` (CRÍTICA).
- Si una línea contiene `grep_search` Y el contexto indica búsqueda sobre `docs/requirements/` → `REF-OBSOLETA` (MEDIA).
- Si una línea referencia un path en `epics/` que existe en disco → `REF-OK`.
- Si una línea referencia un path que NO existe en disco → `REF-ROTA` (CRÍTICA).

---

### FASE 4: Generación del Plan de Acción Correctiva

**Objetivo:** Producir un reporte Markdown ejecutable con todas las discrepancias y sus correcciones.

**Formato del reporte:**

```markdown
# 📋 Reporte de Reconciliación de Gobernanza vs Estructura de Requerimientos
> **Fecha:** YYYY-MM-DD | **Ejecutor:** Arquitecto Líder
> **Estado:** PENDIENTE DE APROBACIÓN

## Resumen Ejecutivo
| Categoría | Total | Estado |
|-----------|:-----:|--------|
| 🔴 CRÍTICA (REF-ROTA + REF-DEPREC) | X | ❌ Requiere corrección inmediata |
| 🟡 MEDIA (REF-OBSOLETA + REG-DESINC) | X | ⚠️ Requiere corrección planificada |
| ⚠️ BAJA (HUERFANO) | X | ℹ️ Requiere verificación |
| ✅ OK (REF-OK + ADV-OK) | X | ✅ Sin acción |

## Discrepancias Detectadas (Ordenadas por Prioridad)

### 🔴 CRÍTICAS
| # | Archivo de Gobernanza | Línea | Referencia Actual | Corrección Propuesta |
|---|----------------------|:-----:|-------------------|---------------------|
| 1 | workflow_X.md | L15 | `v1_user_stories.md` (deprecado) | Cambiar a `v1_user_stories_index.md` → `epics/epic_X.md` |

### 🟡 MEDIA
| # | Archivo de Gobernanza | Línea | Referencia Actual | Corrección Propuesta |
|---|----------------------|:-----:|-------------------|---------------------|
| 1 | workflow_Y.md | L25 | `grep_search` sobre `.md` | Cambiar a PowerShell `Select-String` |

### ⚠️ BAJA
| # | Archivo Huérfano | Ubicación | Verificación |
|---|-----------------|-----------|--------------|
| 1 | epic_F_nuevo.md | epics/ | ¿Pendiente de integración? |

## Próximos Pasos
1. Presentar este reporte al Humano para aprobación.
2. Si aprobado, ejecutar las correcciones en orden de prioridad (🔴 → 🟡 → ⚠️).
3. Re-ejecutar este workflow como self-test para confirmar 0 discrepancias.
```

**Depositar el reporte en:** `.agentic-sync/governance_reconciliation_report_YYYY-MM-DD.md`

---

## Validaciones de Calidad

Antes de dar el workflow por completado:

- [ ] Se escanearon los 3 scopes: `.cursorrules`, `.agents/skills/`, `.agent/workflows/`.
- [ ] Se cruzó el inventario de archivos reales con las referencias encontradas.
- [ ] Cada discrepancia tiene una corrección propuesta específica (archivo, línea, cambio).
- [ ] Las advertencias intencionales (`PROHIBIDO`/`deprecado`) fueron correctamente filtradas.
- [ ] El reporte fue depositado en `.agentic-sync/`.
- [ ] El Humano aprobó las correcciones antes de su aplicación.

---

## Self-Test (Verificación Post-Corrección)

Después de aplicar las correcciones aprobadas, **re-ejecutar este mismo workflow** como validación final.
El resultado esperado es:

| Categoría | Total Esperado |
|-----------|:--------------:|
| 🔴 CRÍTICA | **0** |
| 🟡 MEDIA | **0** |
| ⚠️ BAJA | **0** (o justificados por el Humano) |
| ✅ OK | **100%** |

Si el self-test detecta discrepancias residuales, el ciclo de corrección NO se considera cerrado.

---

## Anti-Patrones

1. **NO ejecutar correcciones sin aprobación humana.** Este workflow detecta y propone; el Humano decide.
2. **NO ignorar las advertencias intencionales.** Las líneas con `PROHIBIDO`/`deprecado` son guardrails deliberados.
3. **NO confiar en `grep_search` para este workflow.** Usar exclusivamente PowerShell `Select-String` (ver Regla 0 de `grep_search_governance/SKILL.md`).
4. **NO ejecutar parcialmente.** Las 4 fases son secuenciales y obligatorias. Saltarse la Fase 1 invalida las conclusiones.

---

## Restricciones

- Este workflow es ejecutable ÚNICAMENTE por el Agente Arquitecto Líder.
- No se deben aplicar correcciones a `.cursorrules` sin aprobación explícita del Humano (es la Constitución del enjambre).
- Los cambios correctivos deben commitearse a la rama de sprint activa, nunca directamente a `main`.
