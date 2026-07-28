# 🧠→🎨 Handoff: 🧠 ARQUITECTO LÍDER → 🎨 FRONTEND - VUE
# T-01: Modeler Bugfix - Inmutabilidad de ID Técnico en Procesos Existentes

**Emitido por:** [🧠 ARQUITECTO LÍDER]
**Destinatario:** [🎨 FRONTEND - VUE]
**Fecha:** 2026-06-10T19:35:00-05:00
**Sprint:** 6 — Iteración 1
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (Leyes Globales)
cat .cursorrules

# 2. Skill principal de Frontend Build Audit
cat .agents/skills/frontend_build_audit/SKILL.md

# 3. Skills transversales y de disciplina técnica requeridos
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md
cat .agents/skills/yudhi_architecture_compliance/SKILL.md

# 4. Handoff de la tarea actual
cat .agentic-sync/handoff_US005_bug_guardado.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación @Traceability o comentario `// @Traceability: US-005, CA-15`. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

Al cargar un proceso existente mediante `loadProcess` (con URL `?processId=solitud-tc`), se asigna de manera síncrona `currentProcessName.value = p.name` ("solicitud TC"). Debido a que los watchers en Vue 3 son asíncronos y se encolan para ejecutarse en el post-flush queue, el watcher de `currentProcessName` se ejecuta *después* de que `loadProcess` haya terminado su tick de asignaciones (el cual incluyó `processId.value = p.key`).
El watcher calcula el slug `'solicitud-tc'` (con "c") y pisa el valor `'solitud-tc'` (sin "c"). Esto produce un error HTTP 400/500 al guardar borradores ya que el backend no encuentra el ID técnico modificado.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Watcher de Autogeneración Incondicional | `BpmnDesigner.vue:3244-3248` | El watcher genera el slug a partir del nombre y pisa `processId.value` sin validar si el proceso ya fue cargado o si ya tiene versiones previas. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Introducir variable reactiva `isNewProcess`

**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`

Declarar la variable reactiva en la sección de definición de variables reactivas del proceso:

```typescript
// @Traceability: US-005, CA-15
const isNewProcess = ref(true);
```

### Paso 2: Configurar `isNewProcess` en la creación de nuevos procesos

**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`

En la función `createNewProcess()`, establecer `isNewProcess.value = true`:

```typescript
const createNewProcess = () => {
  // @Traceability: US-005, CA-15
  isNewProcess.value = true;
  currentProcessName.value = newProcessName.value;
  processPattern.value = newProcessPattern.value;
  processStatus.value = 'BORRADOR';
  // ...
```

### Paso 3: Configurar `isNewProcess` en la carga de procesos existentes

**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`

En la función `loadProcess()`, establecer `isNewProcess.value = false` antes de cambiar el nombre o el ID del proceso:

```typescript
// @Traceability: US-005, CA-40
const loadProcess = async (p: any) => {
  // @Traceability: US-005, CA-15
  isNewProcess.value = false;
  showWelcomeModal.value = false;
  showCatalog.value = false;
  try {
    currentProcessName.value = p.name;
    processStatus.value = p.status;
    processId.value = p.key;
    // ...
```

### Paso 4: Condicionar el watcher de `currentProcessName`

**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`

Modificar el watcher para evaluar la bandera `isNewProcess`:

```typescript
// ── Auto-slug processId from name ────────────────────────────
// @Traceability: US-005, CA-15
watch(currentProcessName, (name) => {
  if (name && isNewProcess.value) {
    processId.value = name.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, '');
  }
});
```

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Pruebas de regresión TDD en Vitest pasan en verde | Ejecutar `wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts` -> 80 passed |
| 2 | Compilación exitosa de Frontend | Ejecutar `wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend npm run build` -> exit code 0 |
| 3 | Trazabilidad de código inyectado | `grep -rn "@Traceability: US-005, CA-15" src/views/admin/Modeler/BpmnDesigner.vue` devuelve referencias en `isNewProcess`, `createNewProcess`, `loadProcess` y `watch`. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Aplicar cambios a `frontend/src/views/admin/Modeler/BpmnDesigner.vue` siguiendo las instrucciones quirúrgicas.
2. Ejecutar la validación de tests unitarios:
   `wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts`
3. Ejecutar el build de producción para validar la integridad tipográfica de TypeScript:
   `wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend npm run build`
4. Consolidar los cambios:
   `git add . && git commit -m "fix(modeler): fix technical ID mutation bug on process load" && git push origin sprint-6`

---

## 📋 Instrucciones para Copiar y Pegar (Prompt de Subagente)

```
Asume el rol de 🎨 FRONTEND - VUE.

Para esta tarea es OBLIGATORIO utilizar y cumplir con la disciplina de los siguientes skills de desarrollo:
- addyosmani_planning (planificación rigurosa)
- addyosmani_sre_discipline (estrategia y validación estricta de supervivencia)
- addyosmani_code_review (revisión de código e integridad del diff)
- yudhi_architecture_compliance (cumplimiento de estándares de arquitectura)
- yudhi_database_migrations (buenas prácticas de base de datos)

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/frontend_build_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat .agents/skills/yudhi_architecture_compliance/SKILL.md
5. cat .agentic-sync/handoff_US005_bug_guardado.md

TU MISIÓN:
1. Modificar BpmnDesigner.vue para inyectar la bandera reactiva `isNewProcess` y condicionalizar el watcher de `currentProcessName` para que no sobrescriba el ID técnico al cargar un proceso existente.
2. Validar que las pruebas unitarias pasen a verde ejecutando:
   wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts
3. Ejecutar el build de frontend para certificar la compilación sin advertencias o roturas de tipos:
   wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend npm run build
4. Consolidar los cambios con un commit convencional a la rama de sprint actual y realizar el push correspondiente.

REGLAS INQUEBRANTABLES:
- Prohibido el uso de git stash bajo la Ley Global 2.
- Mantener la trazabilidad agregando el comentario // @Traceability: US-005, CA-15.
```
