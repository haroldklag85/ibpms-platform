# 🧠→🎨 Handoff: Arquitecto → Frontend Vue
# T-24-FRONTEND: Testabilidad J-02 y Estabilización Pasiva

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🎨 FRONTEND - VUE
**Fecha:** 2026-05-13T17:55:00-05:00
**Sprint:** 7 — Iteración 7.2
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (Leyes Globales)
cat .cursorrules

# 2. Skill principal del agente receptor
cat ibpms-platform/.agents/skills/frontend_build_audit/SKILL.md

# 3. Skills transversales aplicables
cat ibpms-platform/.agents/skills/zero_mock_enforcement/SKILL.md
cat ibpms-platform/.agents/skills/clean_code_standards/SKILL.md

# 4. ADRs relevantes
cat ibpms-platform/docs/architecture/adr_006_vue3_lowcode_engine.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación `@Traceability` o comentario `// @Traceability: Testabilidad J-02 (T-24)`. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

La arquitectura J-02 está ensamblada con JPA/Axios, pero carece de anclajes de prueba deterministas (`data-testid`) en el DOM para la interacción E2E. Además, pueden quedar remanentes fantasma de Pinia Mocks que deben limpiarse para cumplir ADR-010.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Carencia de localizadores E2E | Componentes BPMN/DMN | Faltan atributos `data-testid` canónicos. |
| Mocks Remanentes | Stores Pinia J-02 | Posible existencia de Mocks en memoria a ser depurados. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Inyección de Localizadores y Depuración Zero-Mock

**Archivo:** `frontend/src/views/modeler/` (Archivos `.vue` de BPMN y DMN) y correspondientes `stores/`.

Debes inyectar atributos `data-testid` para asegurar el soporte a QA y eliminar cualquier mock estático:

```vue
<!-- Snippet prescriptivo — Ejemplo en un template Vue -->
<template>
  <div class="bpmn-canvas-container" data-testid="bpmn-canvas-wrapper">
    <!-- @Traceability: Testabilidad J-02 (T-24) -->
    <button data-testid="btn-save-draft" @click="saveDraft">Guardar Borrador</button>
    <button data-testid="btn-deploy" @click="deployProcess">Desplegar</button>
  </div>
</template>
```

### Paso 2: Estabilización Pasiva (Fixing)
El objetivo secundario en este ciclo es actuar reactivamente: resolver posibles bugs UI/Network que sean reportados por los scripts Playwright del Agente QA.

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Inyección `data-testid` | `grep -r "data-testid" frontend/src/views/modeler/` arroja cobertura total en botones y canvas. |
| 2 | Limpieza de Mocks | Ningún archivo de J-02 contiene `setTimeout` artificiales o fixtures locales estáticas. |
| 3 | Trazabilidad Inyectada | Comentarios `@Traceability: Testabilidad J-02 (T-24)` presentes en los cambios. |
| 4 | Build Limpio | Ejecución exitosa de `npm run build` sin errores de Typescript o Vite. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Modificar archivos `.vue` inyectando `data-testid`.
2. Purgar remanentes en los Stores correspondientes.
3. Esperar reportes de QA y aplicar fix (si aplica).
4. Build: `npm run build`
5. Commit: `git add . && git commit -m "refactor(ui): testabilidad y localizadores E2E J-02" && git push`

---

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 🎨 FRONTEND - VUE.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat ibpms-platform/.cursorrules
2. cat ibpms-platform/.agents/skills/frontend_build_audit/SKILL.md
3. cat ibpms-platform/.agents/skills/zero_mock_enforcement/SKILL.md
4. cat ibpms-platform/.agents/skills/clean_code_standards/SKILL.md
5. cat ibpms-platform/docs/architecture/adr_006_vue3_lowcode_engine.md
6. cat ibpms-platform/.agentic-sync/T-24_Frontend_Testability.md

TU MISIÓN:

1. Inyectar localizadores (`data-testid`) canónicos en los componentes BPMN y DMN.
2. Limpiar cualquier dependencia fantasma de Pinia Mocks.
3. Estar en estado de Estabilización Pasiva para arreglar bugs levantados por QA.
4. Build/Compile: `cd frontend && npm run build`
5. Commit: `git add . && git commit -m "refactor(ui): testabilidad y localizadores E2E J-02" && git push`

REGLAS INQUEBRANTABLES:
- OBLIGATORIO inyectar `// @Traceability: Testabilidad J-02 (T-24)`.
- DEBES asegurar la compilación limpia del entorno (`npm run build`).
```
