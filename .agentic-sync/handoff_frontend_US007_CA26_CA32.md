---
name: "Handoff Frontend - US-007 (Modo Manual DMN) CA-26 a CA-32"
role: "Frontend"
---

# 1. Metadatos y SSOT (Single Source of Truth)
- **Iteración/Sprint:** Sprint-6
- **Rama de trabajo:** sprint-6
- **User Story:** US-007 (Generador Cognitivo de DMN)
- **Criterios de Aceptación (CAs) a desarrollar:** CA-26, CA-27, CA-28, CA-29, CA-30, CA-31, CA-32
- **Path del SSOT:** `docs/requirements/epics/epic_B_formularios_bpmn.md`
- **Flujo de Trabajo:** Backend -> Frontend -> QA

# 2. Alineación Arquitectónica y ADRs (Architecture Decision Records)
- **Validación de ADRs:**
  - `adr-002-vue3-microfrontends.md`: Se utilizarán componentes Vue 3 con Composition API y Pinia para el manejo de estado, manteniendo responsabilidades separadas.
- **Lineamientos Transversales:** Se implementará la edición manual del DMN asegurando que la UI coexista con el panel de IA (Chat NLP) sin bloqueos. Además, la protección contra inyecciones y la validación en vivo (FEEL) asegura que el DMN se envíe bien formado y seguro hacia el backend.

# 3. Rutas Exactas y Contexto Preexistente
- **Vistas y Componentes Vue:**
  - Buscar los componentes relacionados a DMN en `frontend/src/views/admin/Dmn/` o `frontend/src/components/dmn/` (Ej. `DmnEditor.vue`, `DmnCatalog.vue`, `DmnGrid.vue`).
  - Buscar el store en `frontend/src/stores/dmnStore.ts`.

# 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

**1. CA-26: Coexistencia Chat NLP y Grilla:**
Asegura que el `DmnEditor.vue` o contenedor principal renderice tanto la columna de chat como la grilla en un diseño Split-View:
```vue
<template>
  <div class="flex h-screen">
    <!-- Panel del Generador Cognitivo (Chat NLP) -->
    <div class="w-1/3 border-r bg-gray-50">
      <NlpChatPanel />
    </div>
    <!-- Grilla Visual Manual DMN -->
    <div class="w-2/3 p-4">
      <DmnGridManual :editable="true" />
    </div>
  </div>
</template>
```

**2. CA-27: Binding Zod en Columnas (Input):**
En las cabeceras de entrada, no usar `input type="text"`, sino un Dropdown que reciba el diccionario:
```vue
<template>
  <select v-model="column.variableName" class="form-select">
    <option v-for="v in zodDictionary" :key="v.id" :value="v.name">
      {{ v.name }} ({{ v.type }})
    </option>
  </select>
</template>
```

**3. CA-28: Validación FEEL en Vivo:**
En las celdas de condición (`td > input`), aplicar validación al evento `@input`:
```js
const validateFEELSyntax = (value) => {
  // Lógica ligera de FEEL: ej: '< 1000', '>= 50', '"Aprobado"'
  const feelRegex = /^(<|<=|>|>=|=|!=)?\s*("[^"]*"|\d+(\.\d+)?|true|false)$/;
  return feelRegex.test(value);
};
```
Mostrar tooltip/borde rojo si es inválido, y bloquear `isFormValid`.

**4. CA-29: Fila Catch-All Automática:**
Añadir dinámicamente la última fila inamovible (bloqueada):
```js
const catchAllRow = {
  id: 'catch-all',
  inputs: ['null'], // o vacío dependiendo de la lógica
  outputs: ['"Revisión Humana"'],
  isLocked: true // 🔒 no se puede borrar
};
// Siempre renderizar esta fila al final del tbody
```

**5. CA-31: SRE Límite 100 Filas:**
```vue
<button @click="addRow" :disabled="rows.length >= 100" class="btn">
  + Agregar Fila
</button>
<span v-if="rows.length >= 100" class="text-orange-500 text-xs">Límite SRE alcanzado (100)</span>
```

**6. CA-32: Trazabilidad Manual y Badge:**
Al darle "Publicar" luego de edición manual, se manda el flag en el `dmnStore`:
```js
async saveDmn() {
   const payload = { xmlContent: generatedXml.value, isManual: true };
   await axios.put(`/api/v1/dmn-models/${this.dmnId}`, payload);
}
```
En el `DmnCatalog.vue`, renderizar el Badge:
```vue
<span v-if="dmn.isManual" class="badge bg-yellow-100 text-yellow-800">📝 Modificada Manualmente</span>
<span v-else class="badge bg-blue-100 text-blue-800">🤖 100% IA</span>
```

# 5. Matriz de QA y Testing Atómico
Dirigido a QA para `frontend/src/components/__tests__/DmnGrid.spec.ts`:

| Test Name | CA Evaluado | Aserción Esperada |
| --- | --- | --- |
| `renders_split_view_chat_and_grid` | CA-26 | Verifica que los paneles `.chat-panel` y `.grid-panel` existan simultáneamente. |
| `validates_feel_syntax_realtime` | CA-28 | Al ingresar un valor no-FEEL ("abcd" sin comillas), la celda obtiene la clase `.border-red-500`. |
| `renders_catch_all_locked_row` | CA-29 | Existe un `tr.catch-all` y no tiene botón de eliminar. |
| `disables_add_button_at_100_rows` | CA-31 | Si la grilla tiene 100 filas, el botón `[+ Agregar]` tiene el atributo `disabled`. |

# 6. Mensaje de Despacho (Comunicación al Agente Especialista)

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_Frontend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_Frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
>
> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
> 
> "Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`."
