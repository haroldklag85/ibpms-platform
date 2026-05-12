---
name: Handoff Quality Standard
description: |
  Estándar de calidad obligatorio para la redacción de documentos de Handoff inter-agente.
  Garantiza consistencia estructural, trazabilidad, y eliminación de ambigüedad en toda
  delegación técnica dentro del Enjambre de IA. Complementa el skill
  `architect_handoff_protocol` (contenido arquitectónico) con un enfoque en FORMATO y CALIDAD.
version: 1.0.0
applies_to:
  - Arquitecto Líder (emisor principal)
  - Cualquier agente que escale o delegue trabajo a otro agente
triggers:
  - "Crea el handoff"
  - "Genera handoff"
  - "Prepara delegación"
  - "Despacha a [agente]"
  - "Escala al Arquitecto"
---

# 📐 SKILL: Handoff Quality Standard (HQS)

## 📌 Propósito

Este skill estandariza la **estructura, formato y calidad** de todo documento de Handoff
generado en el proyecto. Su objetivo es que cualquier agente receptor pueda ejecutar las
instrucciones sin preguntar, sin asumir, y sin desviarse del alcance.

> **Jerarquía:** Este SKILL se subordina a `.cursorrules` (Leyes Globales). Si alguna
> instrucción aquí contradice una Ley Global, prevalece `.cursorrules`.

---

## 🏛️ Principios Rectores

1. **Determinismo:** Un handoff debe producir el MISMO resultado sin importar quién lo ejecute.
2. **Autosuficiencia:** El agente receptor NO debe necesitar buscar contexto adicional fuera
   del handoff y las lecturas obligatorias que este referencia.
3. **Trazabilidad:** Cada instrucción debe rastrearse a una US/CA del SSOT.
4. **Verificabilidad:** Cada tarea debe tener un criterio de aceptación medible y binario (PASS/FAIL).

---

## ✅ Estructura Obligatoria (7 Secciones)

Todo Handoff DEBE contener estas 7 secciones en este orden exacto.
Omitir cualquier sección invalida el handoff.

### Sección 1: Encabezado y Metadatos

```markdown
# 🧠→[emoji] Handoff: [Emisor] → [Receptor]
# [Código de Tarea]: [Título descriptivo]

**Emitido por:** [Collar de identificación del emisor, ej. 🧠 ARQUITECTO LÍDER]
**Destinatario:** [Collar del receptor, ej. ⚙️ BACKEND - JAVA]
**Fecha:** [ISO 8601 con timezone, ej. 2026-05-11T22:07:00-05:00]
**Sprint:** [Número] — [Iteración]
**Prioridad:** [🔴 Alta | 🟡 Media | 🟢 Baja]
**Dependencia:** [Tareas que deben completarse antes, o "Ninguna"]
```

**Reglas del encabezado:**
- El emoji del receptor debe coincidir con su Collar de Identidad (LEY GLOBAL 1).
- La flecha `→` indica dirección de delegación.
- Si hay dependencia, indicar el código de tarea exacto (ej. "T-04 Backend debe estar completado").

---

### Sección 2: Lecturas Obligatorias

```markdown
## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

\```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/[skill_del_agente]/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes
cat docs/architecture/[adr_relevante].md
\```
```

**Reglas de las lecturas:**
- `.cursorrules` SIEMPRE es la primera lectura. Sin excepción.
- El skill principal del agente receptor SIEMPRE es la segunda lectura.
- Incluir al menos 1 ADR relevante a la tarea.
- Cada lectura debe tener un comentario `# N.` numerado explicando QUÉ contiene.

**Cierre obligatorio de sección:**
```markdown
> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `// @Traceability: US-XXX, CA-XX`.
> Esto es INNEGOCIABLE.
```

---

### Sección 3: Diagnóstico del Arquitecto

```markdown
## 🔬 Diagnóstico del Arquitecto

[Descripción textual del problema detectado durante la auditoría forense]

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| [Nombre del hallazgo] | [Archivo:línea] | [Descripción técnica precisa] |
```

**Reglas del diagnóstico:**
- DEBE incluir una tabla con hallazgos forenses.
- Cada hallazgo DEBE referenciar archivo y número de línea.
- PROHIBIDO usar lenguaje vago como "hay problemas de arquitectura".
  Usar: "El archivo X importa Y en la línea Z, violando ADR-001 §3".
- Si se detectaron componentes reutilizables existentes, listarlos en una tabla separada.

---

### Sección 4: Instrucciones Quirúrgicas

```markdown
## 🎯 Instrucciones Quirúrgicas

### Paso N: [Título del paso]

**Archivo:** `[ruta/relativa/al/archivo]`

[Descripción de lo que se debe hacer]

\```[lenguaje]
// Snippet prescriptivo — NO es pseudocódigo, es código ejecutable
\```
```

**Reglas de las instrucciones:**
- Cada paso DEBE indicar el archivo exacto a crear o modificar.
- Los snippets DEBEN ser código ejecutable, NO pseudocódigo ni descripciones abstractas.
- PROHIBIDO decir "agrega la lógica necesaria". Dictar la firma del método, los tipos,
  y la estructura de control mínima.
- Numerar los pasos secuencialmente (Paso 1, Paso 2...).
- Si un paso tiene alternativas, presentarlas como "Opción A / Opción B" con pros y contras.

---

### Sección 5: Criterios de Aceptación (DoD)

```markdown
## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | [Criterio medible y binario] | [Cómo verificarlo: comando, query, inspección] |
```

**Reglas de los criterios:**
- DEBE haber al menos 3 criterios por handoff.
- Cada criterio DEBE ser **binario** (PASS o FAIL, no "parcialmente completo").
- La columna "Evidencia" DEBE especificar el comando o acción exacta para verificar
  (ej. `grep "import.*infrastructure" Controller.java` → 0 resultados).
- SIEMPRE incluir como criterio final: Build/Compilación exitosa + Commit en rama.

---

### Sección 6: Secuencia de Ejecución

```markdown
## 🚦 SECUENCIA DE EJECUCIÓN

1. [Paso 1 con comando exacto]
2. [Paso 2 con comando exacto]
...
N. Commit: `git add . && git commit -m "[tipo]([alcance]): [descripción]" && git push`
```

**Reglas de la secuencia:**
- Los comandos de compilación/build DEBEN referenciar el SKILL del agente,
  NO inventar comandos ad-hoc.
- El último paso SIEMPRE es el commit con mensaje convencional
  (`feat`, `refactor`, `fix`, `test`, `chore`).
- PROHIBIDO incluir `git stash` (LEY GLOBAL 2).
- PROHIBIDO incluir `cd` como paso suelto — usar rutas relativas en los comandos.

---

### Sección 7: Instrucciones de Copiar y Pegar (Prompt del Agente)

```markdown
## 📋 Instrucciones para Copiar y Pegar

\```
Asume el rol de [Collar de Identidad del agente].

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/[skill_principal]/SKILL.md
3. cat .agents/skills/[skill_transversal]/SKILL.md
4. cat docs/architecture/[adr_relevante].md
5. cat .agentic-sync/[nombre_del_handoff].md

TU MISIÓN:

1. [Tarea 1 con instrucciones claras]
2. [Tarea 2 con instrucciones claras]
3. Build/Compile: [comando exacto]
4. Commit: [comando git exacto]

REGLAS INQUEBRANTABLES:
- [Regla 1 específica al contexto]
- [Regla 2 específica al contexto]
- [Regla N]
\```
```

**Reglas del prompt:**
- DEBE comenzar con "Asume el rol de [Collar]".
- DEBE listar las lecturas obligatorias como paso previo con `cat`.
- DEBE incluir el handoff mismo como última lectura (`cat .agentic-sync/[handoff].md`).
- La sección "REGLAS INQUEBRANTABLES" debe contener al menos 3 reglas contextuales.
- PROHIBIDO usar lenguaje condicional ("si puedes", "intenta", "sería bueno").
  Usar lenguaje imperativo ("DEBES", "PROHIBIDO", "OBLIGATORIO").

---

## 🚫 Anti-Patrones (Errores Prohibidos en Handoffs)

| # | Anti-Patrón | Por qué es peligroso | Corrección |
|---|-------------|---------------------|------------|
| AP-01 | Handoff sin lecturas obligatorias | El agente ignora las Leyes Globales y los skills, produciendo código no-gobernado | Siempre incluir Sección 2 |
| AP-02 | Handoff sin criterios de aceptación | No hay forma de verificar si el trabajo está "Done". El agente declara éxito sin evidencia | Siempre incluir Sección 5 con criterios binarios |
| AP-03 | Handoff que sugiere `git stash` | Viola LEY GLOBAL 2. El stash es volátil, no auditable y no transferible | Usar `git commit` + `git push` |
| AP-04 | Instrucciones abstractas: "mejora la arquitectura" | El agente alucina una solución que puede violar ADRs o crear regresiones | Dictar snippets prescriptivos con archivos y líneas exactas |
| AP-05 | Handoff sin `@Traceability` | Viola LEY GLOBAL 3. El hallazgo se redescubrirá como nuevo en la próxima auditoría | Incluir recordatorio de trazabilidad en Sección 2 y en snippets |
| AP-06 | Handoff con comandos de compilación ad-hoc | El agente puede usar flags incorrectos o rutas erróneas, produciendo falsos positivos de build | Referenciar el SKILL de compilación del agente por ruta |
| AP-07 | Omitir dependencias entre handoffs | El agente Frontend intenta consumir un endpoint que Backend aún no ha creado | Declarar dependencias en el encabezado (Sección 1) |
| AP-08 | Handoff sin diagnóstico forense | El agente no entiende POR QUÉ debe hacer el cambio, solo el QUÉ. Riesgo de solución superficial | Incluir tabla de hallazgos con archivo:línea y violación específica |
| AP-09 | Usar `alert()` o `confirm()` en instrucciones Frontend | Viola .cursorrules §5 (Zero-Trust UI). Las alertas nativas son interceptables | Instruir uso de Modales Vue 3 |
| AP-10 | Handoff sin prompt de copiar y pegar | El humano debe reinterpretar el handoff para crear el prompt, introduciendo ambigüedad | Siempre incluir Sección 7 con bloque de texto listo |

---

## 📏 Checklist de Validación Pre-Despacho

Antes de entregar un handoff al humano, el emisor DEBE verificar:

- [ ] ¿Tiene las 7 secciones obligatorias?
- [ ] ¿La Sección 2 incluye `.cursorrules` como primera lectura?
- [ ] ¿La Sección 3 tiene tabla de hallazgos con archivo:línea?
- [ ] ¿La Sección 4 tiene snippets ejecutables (no pseudocódigo)?
- [ ] ¿La Sección 5 tiene al menos 3 criterios binarios?
- [ ] ¿La Sección 6 termina con `git commit` + `git push`?
- [ ] ¿La Sección 7 tiene el prompt listo para copiar?
- [ ] ¿Se declararon las dependencias de otros handoffs?
- [ ] ¿Se incluyen anotaciones `@Traceability` en los snippets?
- [ ] ¿No se usaron anti-patrones de la tabla AP-01 a AP-10?

---

## 🔄 Relación con Otros Skills

| Skill | Relación |
|-------|----------|
| `architect_handoff_protocol/SKILL.md` | Complementario. Ese skill define el CONTENIDO arquitectónico. Este define el FORMATO y CALIDAD |
| `backend_sre_compilation_audit/SKILL.md` | Referenciado. Los handoffs de Backend DEBEN apuntar a este skill para compilación |
| `frontend_build_audit/SKILL.md` | Referenciado. Los handoffs de Frontend DEBEN apuntar a este skill para build |
| `qa_e2e_validation_audit/SKILL.md` | Referenciado. Los handoffs de QA DEBEN apuntar a este skill para certificación |
| `clean_code_standards/SKILL.md` | Referenciado. TODOS los handoffs DEBEN incluirlo como lectura transversal |
| `zero_mock_enforcement/SKILL.md` | Referenciado. TODOS los handoffs DEBEN incluirlo como lectura transversal |

---

## 🎯 Gatillo de Ejecución

Este SKILL se activa automáticamente cuando:
1. El **Arquitecto Líder** recibe instrucción de crear handoffs, delegaciones o despachos.
2. **Cualquier agente** necesita escalar un hallazgo o bloqueante a otro agente
   (ej. QA→Arquitecto, Backend→Frontend).
3. El humano solicita "genera handoff", "despacha al agente X", o "crea instrucciones para [rol]".

Al activarse, el agente emisor DEBE:
1. Leer este SKILL completo.
2. Leer `architect_handoff_protocol/SKILL.md` (contenido arquitectónico).
3. Generar el handoff cumpliendo las 7 secciones y la checklist de validación.
