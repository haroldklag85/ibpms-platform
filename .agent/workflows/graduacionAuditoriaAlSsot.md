---
description: Protocolo obligatorio para graduar hallazgos de auditoría resueltos al SSOT (v1_user_stories.md), eliminando la Amnesia Institucional y cerrando el ciclo de trazabilidad inversa.
---

# Protocolo de Graduación de Hallazgos al SSOT (Anti-Amnesia Institucional)

> **Versión:** 1.0 | **Última Actualización:** 2026-04-05
> **Ley Padre:** LEY GLOBAL 3 — Directriz SSOT (`.cursorrules`)
> **Principio:** Si no existe en `v1_user_stories.md`, no existe para ningún agente ni auditor.

## Contexto

Cuando una auditoría detecta un GAP y se crea un artefacto satélite de remediación (Ej: `us003_gap_remediation_brief.md`), los hallazgos resueltos quedan atrapados fuera del SSOT. La próxima auditoría redescubre los mismos GAPs porque `v1_user_stories.md` nunca fue actualizado. Este workflow rompe ese ciclo infinito.

## Triggers (Cuándo ejecutar)

- Después de que un agente complete las remediaciones de un `*_gap_remediation_brief.md`.
- Al finalizar un Sprint donde hubo auditorías correctivas con hallazgos resueltos.
- Cuando el Humano detecte que hallazgos viejos están siendo redescubiertos.
- Cuando existan artefactos satélite (`*_functional_analysis.md`, `*_remediation_brief.md`, `audit_*_report.md`) con GAPs marcados como RESUELTOS que no sean CAs formales en el SSOT.

## Roles autorizados

- **Ejecutor principal:** Agente Product Owner o Agente Arquitecto Líder.
- **Validador:** Humano (aprueba la inyección antes de persistir).
- **Excluidos:** Backend, Frontend, QA — estos agentes no modifican el SSOT directamente.

---

## Flujo de Trabajo (5 Fases Estrictas)

### FASE 1: Inventario de Hallazgos Resueltos

**Objetivo:** Identificar todos los GAPs resueltos que aún viven fuera del SSOT.

1. Usa `grep_search` con `Query: "RESUELTO"` o `"IMPLEMENTADO"` o `"CERRADO"` sobre `docs/requirements/` para localizar artefactos satélite con hallazgos resueltos.
2. Lee cada artefacto satélite encontrado usando `view_file` con paginación (`StartLine`/`EndLine`).
3. Extrae **solo** los GAPs con estado RESUELTO/IMPLEMENTADO. Ignora los pendientes.
4. Genera una lista consolidada:

```markdown
| # | GAP Original | Artefacto Satélite | Ticket | US Destino |
|---|-------------|-------------------|--------|-----------|
| 1 | Falta cifrado PII en LocalStorage | us003_gap_remediation_brief.md | REM-003-01 | US-003 |
| 2 | AutoClaim para tareas Group-Level | us003_gap_remediation_brief.md | REM-003-05 | US-003 |
```

### FASE 2: Redacción de CAs Formales (Formato Gherkin)

**Objetivo:** Convertir cada GAP resuelto en un Criterio de Aceptación formal listo para inyección.

Por cada GAP, redacta un `Scenario` Gherkin con este formato obligatorio:

```gherkin
# ==============================================================================
# [LETRA]. REMEDIACIONES POST-AUDITORÍA (Sprint Remediation Brief YYYY-MM-DD)
# Origen: us{X}_gap_remediation_brief.md — Tickets REM-{X}-01 a REM-{X}-NN
# ==============================================================================

Scenario: [Descripción clara de la decisión técnica tomada] (CA-[N]) [REMEDIACIÓN]
  Given [contexto técnico preciso — NO la pregunta abierta original]
  When [condición que activa este comportamiento]
  Then [la solución implementada como expectativa verificable]
  And [detalles arquitectónicos complementarios]
```

**Reglas de redacción:**
- El Scenario debe contener la **decisión técnica tomada**, NO el GAP original ni la pregunta abierta.
- Incluir el marcador `[REMEDIACIÓN]` en el título para distinguirlo de CAs originales.
- La letra de la sección (A, B, C...) debe ser la siguiente disponible en el bloque Feature de la US.
- Si un GAP resuelto **corrige** un CA existente (en lugar de agregar uno nuevo), el CA original debe ser REESCRITO, no duplicado.

### FASE 3: Inyección Paginada en el SSOT

**Objetivo:** Insertar los CAs nuevos en `v1_user_stories.md` sin leer el archivo entero.

**REGLA DE VIDA O MUERTE:** El archivo `v1_user_stories.md` es gigante. Tienes **PROHIBIDO** abrirlo sin paginación.

1. Usa `grep_search` con `MatchPerLine: true` y `Query: "US-{XXX}"` para localizar la línea exacta donde empieza la User Story destino.
2. Usa `grep_search` con `Query: "Trazabilidad UX"` para encontrar el cierre del bloque Gherkin más cercano.
3. Usa `view_file` con `StartLine` y `EndLine` (máximo 150 líneas) para ver el bloque completo.
4. Inyecta los nuevos CAs **antes del cierre** del bloque ` ``` ` del Feature Gherkin, después de la última sección existente.
5. Usa `replace_file_content` para hacer la inyección quirúrgica.

### FASE 4: Renumeración Secuencial de CAs

**Objetivo:** Garantizar que los CAs de la US queden secuencialmente numerados (CA-1..CA-N) sin huecos ni duplicados.

Ejecuta el workflow `/renumeracionCriteriosAceptacionUs.md` sobre la US que fue modificada.

### FASE 5: Deprecación o Archivado del Artefacto Satélite

**Objetivo:** Cerrar el ciclo documental del artefacto usado como fuente.

Presenta al Humano dos opciones:
- **Opción A (Stub de Redirección):** Reemplazar el contenido del satélite con un stub indicando: `> HALLAZGOS GRADUADOS AL SSOT: Los GAPs de este documento fueron inyectados como CAs formales en v1_user_stories.md (US-XXX, CA-N a CA-M). Este archivo es histórico y de solo lectura.`
- **Opción B (Archivo Histórico):** Mantener el satélite intacto pero moverlo a una subcarpeta `docs/requirements/archive/` para sacarlo del scope activo del RAG.

Esperar la decisión del Humano antes de aplicar.

---

## Validaciones de Calidad

Antes de dar el workflow por completado, verifica mentalmente:

- [ ] Cada GAP resuelto tiene un CA formal en `v1_user_stories.md`.
- [ ] Los CAs nuevos están en formato Gherkin estándar (Given/When/Then).
- [ ] Los CAs nuevos tienen el marcador `[REMEDIACIÓN]` y referencia al ticket de origen.
- [ ] La numeración secuencial es correcta (sin huecos ni duplicados).
- [ ] El artefacto satélite fue deprecado o archivado según la decisión del Humano.
- [ ] Ningún CA de remediación duplica un CA original — lo reemplaza o complementa.

---

## Anti-Patrones (Lo que NO hacer)

1. **NO duplicar CAs.** Si un GAP corrige un CA existente, REESCRIBE el CA original. No crees un CA-87 que diga lo mismo que el CA-3 pero "mejor".
2. **NO inyectar GAPs pendientes.** Solo los RESUELTOS se gradúan. Los pendientes siguen viviendo en el satélite.
3. **NO leer el SSOT entero.** Usa siempre `grep_search` + `view_file` paginado. Violar esto colapsa la ventana de contexto.
4. **NO eliminar artefactos satélite sin autorización.** Siempre dejar la decisión de deprecación al Humano.

---

## Restricciones

- Este workflow NO es ejecutable por agentes Backend, Frontend o QA. Solo PO o Arquitecto.
- El Humano debe validar los CAs redactados antes de la inyección (FASE 3).
- No se deben graduar decisiones "en proceso" o "por definir" — solo decisiones técnicas finales.
