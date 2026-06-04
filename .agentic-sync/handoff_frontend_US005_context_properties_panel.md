# 🧠→🎨 Handoff: Arquitecto Líder → Frontend Vue 3
# US-005-FRONTEND: Panel de Propiedades Contextual en Modeler BPMN (Opción 1)

**Emitido por:** 🧠 ARQUITECTO LÍDER / COPILOTO IA
**Destinatario:** 🎨 FRONTEND - VUE 3
**Fecha:** 2026-06-01T19:30:00-05:00
**Sprint:** V1 — Certificación Zero-Mock
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/frontend_build_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. Handoff actual (Este documento)
cat .agentic-sync/handoff_frontend_US005_context_properties_panel.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar comentarios de trazabilidad:
> `// @Traceability: US-005, CA-77 (Panel de Propiedades Contextual en Modeler)`.
> Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

Actualmente, el panel derecho `CAMUNDA PROPERTIES` en el Diseñador BPMN renderiza **todos** los componentes de configuración simultáneamente en el panel lateral, independientemente de qué elemento esté seleccionado en el lienzo:
- Muestra `FormKey (User Task)`
- Muestra `Conector API (Service Task)`
- Muestra `Escalamiento & Ping-Pong`
- Muestra `Regla de Nomenclatura`

Esto genera una severa fricción de UX/UI (sobrecarga visual) y puede inducir a errores lógicos en ejecución, ya que el motor de Camunda asocia formularios y conectores a nivel de nodos de tareas (`UserTask`, `ServiceTask`), no de forma global a nivel de proceso.

### Comportamiento Esperado (Propuesta 1 - Context-Aware Panel):
1. **Sin selección (o seleccionando el fondo del lienzo):**
   - Mostrar únicamente las propiedades globales del proceso: *Nombre de Negocio, ID Técnico, Regla de Nomenclatura (CA-5), SLA Global y Patrón de Proceso*.
   - **Ocultar** FormKey, Conector API, SLA de Tarea, y Escalamiento & Ping-Pong.
2. **Al seleccionar una User Task (`bpmn:UserTask`):**
   - Mostrar: *Nombre de la Tarea, ID de Tarea, FormKey, SLA de la Tarea, y Escalamiento & Ping-Pong*.
   - Ocultar: Propiedades globales del proceso y el Conector API.
3. **Al seleccionar una Service Task (`bpmn:ServiceTask`):**
   - Mostrar: *Nombre de la Tarea, ID de Tarea, Conector API (con su grilla de mapeo de datos) y External Topic*.
   - Ocultar: Propiedades globales, FormKey y Escalamiento & Ping-Pong.
4. **Al seleccionar cualquier otro elemento (Gateways, EndEvents, SequenceFlows, etc.):**
   - Mostrar un banner indicativo que diga: *"No hay propiedades de Camunda editables para este símbolo"* o colapsar el panel mostrando únicamente las propiedades globales del proceso (conforme a la decisión de diseño acordada).

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Modificación del Panel en el Template de Vue
**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`

Envuelve condicionalmente las secciones del panel de propiedades derecho (`<aside>`) utilizando la propiedad reactiva `selectedElement` (específicamente evaluando `selectedElement.type`).

1. Las secciones globales del proceso (Nombre de negocio, ID Técnico, Regla de Nomenclatura, SLA Global, Patrón de Proceso, etc.) deben renderizarse cuando no haya ningún elemento seleccionado (`!selectedElement.id` o `selectedElement.type === ''`), o cuando el linter colapse el panel al fondo.
2. El selector de `FormKey` y el acordeón de `Escalamiento & Ping-Pong` deben estar condicionados mediante `v-if="selectedElement.type === 'bpmn:UserTask'"`.
3. El selector de `Conector API (Service Task)` (incluyendo su tabla `DataMapperGrid`) y el bloque `External Topic` deben estar condicionados mediante `v-if="selectedElement.type === 'bpmn:ServiceTask'"`.
4. Añadir un contenedor informativo con estilo Tailwind para cuando se seleccionen elementos sin propiedades (ej. `bpmn:ExclusiveGateway`, `bpmn:EndEvent`, `bpmn:SequenceFlow`):
   ```html
   <!-- @Traceability: US-005, CA-77 Panel de Propiedades Contextual -->
   <div v-if="selectedElement.id && !['bpmn:UserTask', 'bpmn:ServiceTask', 'bpmn:BusinessRuleTask', 'bpmn:CallActivity'].includes(selectedElement.type)" class="p-4 bg-gray-50 border border-gray-200 rounded text-xs text-gray-500 text-center">
     ℹ️ No hay propiedades de Camunda editables para este elemento.
   </div>
   ```

### Paso 2: Creación de Unit Tests en Vitest
**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts`

Escribe pruebas unitarias utilizando `@vue/test-utils` para garantizar la renderización contextual de los inputs según el tipo de elemento asignado en `selectedElement`.

1. **Test 1:** Verificar que si `selectedElement.id` está vacío, el dropdown de `FormKey` y `Conector API` no se renderizan en el DOM.
2. **Test 3:** Verificar que si `selectedElement.type` es `'bpmn:UserTask'`, los elementos de `FormKey` y `Escalamiento` son visibles, pero el de `Conector API` no lo es.
3. **Test 4:** Verificar que si `selectedElement.type` es `'bpmn:ServiceTask'`, el conector de API y el mapeo de variables son visibles en pantalla, mientras que `FormKey` queda oculto.
4. **Test 5:** Verificar que si se selecciona una compuerta (`bpmn:ExclusiveGateway`), se dibuja el banner de aviso de "No hay propiedades editables".

---

## ✅ Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Los inputs de tareas no se muestran a nivel global si no hay selección | Inspección de `BpmnDesigner.vue` y ejecución de tests |
| 2 | Al seleccionar una User Task, solo se expone su formulario y escalamiento | Pruebas unitarias en `BpmnDesigner.spec.ts` aprobadas |
| 3 | Al seleccionar una Service Task, solo se expone el conector API | Pruebas unitarias en `BpmnDesigner.spec.ts` aprobadas |
| 4 | El build de producción compila correctamente | `npm run build` finalizado con éxito |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Posicionarse en la rama de desarrollo correspondiente.
2. Realizar los cambios quirúrgicos en [BpmnDesigner.vue](file:///c:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.vue).
3. Añadir los tests en [BpmnDesigner.spec.ts](file:///c:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts).
4. Correr la suite de pruebas locales: `npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts`.
5. Ejecutar `npm run build` para asegurar compatibilidad de tipos TypeScript.
6. Commit y Push.

---

## 📋 Instrucciones para Copiar y Pegar en el Chat de Desarrollo

```text
Asume el rol de 🎨 FRONTEND - VUE 3.

ANTES DE EMPEZAR, lee obligatoriamente:
1. cat .cursorrules
2. cat .agents/skills/frontend_build_audit/SKILL.md
3. cat .agentic-sync/handoff_frontend_US005_context_properties_panel.md

TU MISIÓN:
1. Modificar BpmnDesigner.vue para implementar el comportamiento contextual del panel de propiedades (Ocultar/mostrar FormKey, Conector API y Escalamiento según el tipo de selectedElement).
2. Modificar BpmnDesigner.spec.ts para agregar las pruebas de validación de visualización contextual de elementos.
3. Ejecutar los tests con vitest y asegurar que compila con npm run build.
4. Realizar commit atómico documentando con @Traceability.
```
