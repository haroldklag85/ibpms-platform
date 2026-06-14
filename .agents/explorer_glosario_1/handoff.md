# Handoff Report — Modeler Variable Glossary & Nomenclature Investigation (CA-5)

This report details the investigation of US-005, CA-5 in `BpmnDesigner.vue` and `BpmnDesigner.spec.ts`.

---

## 1. Observation

* **Target File Path:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`
* **Nomenclature Input Container:** Located in `BpmnDesigner.vue` (lines 166–174)
  ```html
  <div class="p-3 bg-fuchsia-50 dark:bg-fuchsia-900/20 border border-fuchsia-200 rounded">
     <label class="block text-xs font-bold text-fuchsia-800 dark:text-fuchsia-300 mb-1 flex items-center justify-between">
       <span>🎟 Regla de Nomenclatura (CA-5)</span>
       <AppTooltip :content="isNomenclatureSyntaxError ? '⚠️ Error de sintaxis: llaves sin cerrar' : bpmnTooltips.NOMENCLATURE" :isError="isNomenclatureSyntaxError" />
     </label>
     <input type="text" v-model="processNomenclature" @change="updateProcessProperty('ReglaNomenclatura', processNomenclature)" :class="{'border-red-500 ring-1 ring-red-500 bg-red-50': isNomenclatureSyntaxError}" class="w-full text-xs border-fuchsia-300 dark:border-fuchsia-600 dark:bg-gray-700 dark:text-white rounded focus:ring-fuchsia-500 focus:border-fuchsia-500 p-2 border transition" placeholder="Ej: OC-{Solicitante}" />
     <p class="text-[10px] text-fuchsia-600 dark:text-fuchsia-400 mt-1 leading-tight">Obligatorio. Define la máscara para instanciar tickets. Se inyecta al nodo raíz del XML.</p>
  </div>
  ```
* **Process Variable Storage:** Written in `updateProcessProperty()` (lines 2257–2291). Global properties are added inside `camunda:Properties` under `bpmn:ExtensionElements` of the canvas root process element.
* **Variable Sources & API Endpoints:**
  * Linked forms: Loaded via `fetchForms()` (line 1259) using `/api/v1/forms/active?processKey=...`.
  * Form fields: Single form fields can be fetched from `/api/v1/forms/{technicalName}/versions/1` using the `formFields` list.
  * Connectors & webhooks: Loaded from `externalTopics` and `connectorMappings` mapping schemas.
  * Session context: Auth store user state `user = ref<{ username: string, roles: string[] } | null>(null)` at `stores/authStore.ts` (line 9).
* **Test Suite File Path:** `frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts`
* **Test Suite Structure:** Standard Vitest test suite `describe('BpmnDesigner.vue', ...)` (lines 13–814) with test cases validating SLA limits, call activities, and DMN bindings.

---

## 2. Logic Chain

1. **Glosario de Variables de Negocio State & Persistence:** Since process-level settings are written as custom key-value attributes inside the BPMN root element extension properties, manual variable declarations (`declaredVariables`) must be serialized (e.g. as a JSON string under the custom key `GlosarioVariables`) and persisted using `updateProcessProperty('GlosarioVariables', JSON.stringify(declaredVariables.value))`.
2. **XML Rehydration Gaps:** The current implementation contains `processNomenclature = ref('')` and `globalSla = ref(72)` but lacks parser hooks on `import.done`. Therefore, a `syncStateFromBpmnRoot()` method must be invoked inside the `import.done` listener to parse the root element's `camunda:Property` collections and populate `processNomenclature`, `declaredVariables`, and `globalSla`.
3. **Dynamic Variable Merging:** To build a complete variable catalog for the autocomplete popover, the frontend must traverse the diagram's User Tasks, extract all unique `camunda:formKey` properties, fetch their visual form field mappings from `/api/v1/forms/{formKey}/versions/1`, cache them in a reactive dictionary (`formFieldsCache`), and compute the union alongside session properties (`session.user_name`, `session.email`), glossary declarations (`declaredVariables`), and connector variables.
4. **Autocomplete Popover & Color-Coded Pills Preview:**
   * Overlaying color badges inside a native HTML `<input>` is not viable due to HTML rendering limitations of native input text values.
   * Instead, we construct a real-time parsed preview box immediately below the input, using regex token splits to separate plain text from `{variable}` tokens, styling the variables as color badges depending on their source (`Session`, `Glossary`, `Form`, `Connector`).
   * Autocomplete is activated when the user inputs a `{` symbol, parsing the substring between the opening brace and the current cursor to filter the list of merged variables.

---

## 3. Caveats

* **Form Fields Fetching:** This proposal assumes form definitions are active and accessible via `/api/v1/forms/{formKey}/versions/1`. If a process uses form keys that do not exist yet in the database, the network request will fail; the cache must handle this gracefully and fallback to showing no variables for that form.
* **Complex Variables:** Only flat primitive variables (String, Number, Boolean) are color-coded. Deep nested structures in connector schemas are not expanded.

---

## 4. Conclusion

The modeler is fully prepared to support CA-5. Implementing it requires:
1. Adding a `declaredVariables` array to BpmnDesigner process state.
2. Building the "Glosario de Variables de Negocio" panel inside the global process properties section.
3. Hooking `syncStateFromBpmnRoot` on `import.done` to ensure full XML-to-UI sync.
4. Adding `mergedVariables` computed property, popover filtering, and text selection replacement triggers on input fields.
5. Implementing a pill-based real-time preview component.

---

## 5. Verification Method

* **Independent Verification of Current Modeler State:** Inspect `BpmnDesigner.vue` from lines 140 to 220 and 2250 to 2300 using `view_file` to confirm the properties panel template and the modeler properties synchronization functions.
* **Testing Commands:** Run `npm run test` inside the frontend workspace (`frontend/`) to run the unit tests.
* **Unit Tests to Add:** Append the proposed `describe('CA-5: Business Glossary & Nomenclature Rule Autocomplete', ...)` unit tests to `BpmnDesigner.spec.ts` at line 814.
