# Analysis Report — US-005, CA-5 Frontend Modeler Investigation

## Executive Summary
This report maps out the current architecture of `BpmnDesigner.vue` and `BpmnDesigner.spec.ts` in relation to **US-005, CA-5 (Glosario de Variables de Negocio & Editor de Regla de Nomenclatura)**. It details how the modeler manages BPMN XML, identifies data sources for variables, maps the existing test suite, and presents a complete, concrete implementation strategy with code snippets and diff mappings for subsequent implementation by the developer.

---

## 1. Modeler Properties Panel & XML Lifecycle Analysis

### 1.1 Properties Panel Structure (Nomenclature Rule)
The properties panel is defined as a Vue `<aside>` component (lines 144–466 of `BpmnDesigner.vue`). It renders context-sensitive controls based on the selected element.
* **Global Process Properties (Root Element):** Rendered when `!selectedElement.id` evaluates to `true` (lines 154–198).
* **Nomenclature Rule Input Position:** Inside the Global Process Properties section (lines 166–174), styled as a fuchsia container (`bg-fuchsia-50`). It is bound to `processNomenclature` via `v-model` and triggers `updateProcessProperty('ReglaNomenclatura', processNomenclature)` on `@change`.

### 1.2 BPMN XML Loading, Parsing, Modifying, and Saving
* **Loading & Parsing:** The diagram initializes with `emptyBpmn` in `onMounted` (line 1500) using `modelerInstance.importXML()`. For catalog items, `loadProcess(p)` (lines 2044–2070) fetches the process XML via `integrationStore.get('/api/v1/design/processes/{key}/xml')` and imports it.
* **Modifying:**
  * Global attributes (like SLA) are saved using Camunda extension attributes on the root process (e.g., `camunda:dueDate`).
  * Process properties use `updateProcessProperty(name, value)` (lines 2257–2291) which accesses the root canvas element, ensures `extensionElements` exists, gets or creates `camunda:Properties`, and updates/appends a `camunda:Property` with the given `name` and `value`.
  * Task-level attributes use `syncElementProperties(key, value)` (lines 2303–2313) to write properties like `name` or `camunda:formKey` via `modeling.updateProperties()`.
* **Saving:** Every 30 seconds, a watch on `timeStore.currentTick` triggers `saveDraft()` if changes are pending. This calls `modelerInstance.saveXML({ format: true })` and sends it to `/api/v1/design/processes/{id}/draft`.

---

## 2. Variable Sourcing & Data Fetching Mapping

Currently, variables are derived from three main sources:
1. **Linked Forms (Form Catalog):** Loaded via `fetchForms()` (lines 1259–1279), which populates `availableForms` from the backend endpoint `/api/v1/forms/active?processKey=...`. Each user task specifies a linked form via `camunda:formKey`. The visual schemas of these forms (containing fields and variable names) are stored in `ibpms_form_definitions` (accessible via `/api/v1/forms/{technicalName}/versions/1`).
2. **Webhooks / Connectors:** Service tasks specify a `camunda:delegateExpression` which delegates to integration connectors. Connector output variables and mapper variables are loaded from the task's data mapping payload (`integrationStore.getProcessVariables`).
3. **Session Context:** Exposed via `useAuthStore()` (from `@/stores/authStore`). The authenticated user is stored in the `user` reactive ref, containing `username` and `roles`. Global session properties like `session.user_name` and `session.email` are system variables that are always available for nomenclature construction.

---

## 3. Test Suite Analysis (`BpmnDesigner.spec.ts`)

The test suite leverages `mount` from `@vue/test-utils` and Vitest for assertions.
* **Mocks:** The suite mocks `useRouter`, `useRoute`, `useAuthStore`, `useTimeStore`, `useConnectionStore`, and `useIntegrationStore`. The `bpmn-js` Modeler is mocked to supply the necessary command structures (`modeling`, `canvas`, `elementRegistry`).
* **Test Context:** Mounts `BpmnDesigner.vue` inside a standard `describe('BpmnDesigner.vue')` wrapper.
* **Integration Strategy for Tests:** CA-5 unit tests should be added as a dedicated `describe('CA-5: Business Glossary & Nomenclature Rule Autocomplete')` block inside `BpmnDesigner.spec.ts` (specifically at the end of the file, around line 814). These tests will verify:
  1. Global process state correctly initializes and parses glossary variables from BPMN XML extensions.
  2. Manual variables can be added/removed, updating both UI state and the XML.
  3. Dynamic variable merging successfully compiles glossary, form, connector, and session context variables.
  4. The nomenclature autocomplete popover displays when a `{` character is typed and hides when selecting a variable or on blur.
  5. Validates that syntax errors (mismatched curly braces) show error indicators.

---

## 4. Proposed Implementation Strategy

Below is the concrete design and implementation sketch for the six required components.

### 4.1 Business Variables Glossary State & UI Panel
Add a collapsible section "Glosario de Variables de Negocio" inside the root process properties view.
* **Reactive State:**
  ```typescript
  const declaredVariables = ref<{ name: string, type: 'String' | 'Number' | 'Boolean' }[]>([]);
  const newVarName = ref('');
  const newVarType = ref<'String' | 'Number' | 'Boolean'>('String');
  ```

### 4.2 Manual Variable Declaration & XML Persistence
* **Adding Variables:** Create an `addDeclaredVariable()` method. It validates that the variable name consists of alphanumeric characters/underscores and does not exist in the list, then pushes it.
* **XML Persistence:**
  ```typescript
  const updateDeclaredVariablesInXml = () => {
    updateProcessProperty('GlosarioVariables', JSON.stringify(declaredVariables.value));
  };
  ```
  Whenever variables are added or deleted, call `updateDeclaredVariablesInXml()`.
* **XML Reading/Rehydration:** On `import.done`, extract this property from the XML.
  ```typescript
  const syncStateFromBpmnRoot = () => {
    if (!modelerInstance) return;
    try {
      const canvas = modelerInstance.get('canvas');
      const rootElement = canvas.getRootElement();
      const bo = rootElement.businessObject;
      const extensionElements = bo.get('extensionElements');
      if (extensionElements) {
        const camundaProperties = extensionElements.values?.find((e: any) => e.$type === 'camunda:Properties');
        if (camundaProperties) {
          // Parse Glossary Variables
          const glossaryProp = camundaProperties.values?.find((p: any) => p.name === 'GlosarioVariables');
          if (glossaryProp && glossaryProp.value) {
            declaredVariables.value = JSON.parse(glossaryProp.value);
          } else {
            declaredVariables.value = [];
          }
          // Parse Nomenclature Rule
          const nomenclatureProp = camundaProperties.values?.find((p: any) => p.name === 'ReglaNomenclatura');
          processNomenclature.value = nomenclatureProp ? nomenclatureProp.value : '';
        }
      }
      // Parse Global SLA
      const globalSlaAttr = bo.get('camunda:dueDate');
      if (globalSlaAttr) {
        const match = globalSlaAttr.match(/^P(\d+)H$/);
        globalSla.value = match ? parseInt(match[1]) : 72;
      }
    } catch (e) {
      console.error('Error syncing state from BPMN Root', e);
    }
  };
  ```

### 4.3 Dynamic Variable Merging
Compile all available variables using a reactive computed property.
* **Cache for Form Fields:**
  ```typescript
  const formFieldsCache = ref<Record<string, { name: string, type: string }[]>>({});
  
  // Watcher to scan the diagram for formKeys and trigger fetching
  const scanAndFetchFormFields = async () => {
    if (!modelerInstance) return;
    const elementRegistry = modelerInstance.get('elementRegistry');
    const userTasks = elementRegistry.filter((e: any) => e.type === 'bpmn:UserTask');
    const formKeys = [...new Set(userTasks.map((t: any) => t.businessObject.get('camunda:formKey')).filter(Boolean))];
    
    for (const key of formKeys) {
      if (!formFieldsCache.value[key]) {
        try {
          const { data } = await integrationStore.get(`/api/v1/forms/${key}/versions/1`);
          if (data && data.formFields) {
            formFieldsCache.value[key] = data.formFields.map((f: any) => ({
              name: f.camundaVariable || f.id,
              type: f.type === 'number' ? 'Number' : f.type === 'checkbox' ? 'Boolean' : 'String'
            }));
          }
        } catch (e) {
          console.warn(`Could not load fields for form key: ${key}`);
        }
      }
    }
  };
  
  // Trigger scan when selection changes or commandStack modifies
  modelerInstance.on('commandStack.changed', scanAndFetchFormFields);
  ```
* **Computed Merged Variables:**
  ```typescript
  const mergedVariables = computed(() => {
    const list: { name: string, type: string, source: 'Glossary' | 'Form' | 'Connector' | 'Session' }[] = [];
    
    // 1. Session Context (System variables)
    list.push({ name: 'session.user_name', type: 'String', source: 'Session' });
    list.push({ name: 'session.email', type: 'String', source: 'Session' });
    
    // 2. Glossary/Declared variables
    declaredVariables.value.forEach(v => {
      list.push({ name: v.name, type: v.type, source: 'Glossary' });
    });
    
    // 3. Form variables from cache
    Object.values(formFieldsCache.value).forEach(fields => {
      fields.forEach(f => {
        if (!list.some(l => l.name === f.name)) {
          list.push({ name: f.name, type: f.type, source: 'Form' });
        }
      });
    });
    
    // 4. Connector variables
    processVariables.value.forEach(v => {
      if (!list.some(l => l.name === v.name)) {
        list.push({ name: v.name, type: v.type || 'String', source: 'Connector' });
      }
    });
    
    return list;
  });
  ```

### 4.4 Autocomplete Popover for Nomenclature Rule Input
* **UI Controls & State:**
  ```typescript
  const showAutocompletePopover = ref(false);
  const autocompleteSearchQuery = ref('');
  const nomenclatureInputRef = ref<HTMLInputElement | null>(null);
  
  const filteredAutocompleteVariables = computed(() => {
    const query = autocompleteSearchQuery.value.toLowerCase();
    return mergedVariables.value.filter(v => v.name.toLowerCase().includes(query));
  });
  ```
* **Autocomplete Event Listeners:**
  ```typescript
  const handleNomenclatureInput = (event: Event) => {
    const value = (event.target as HTMLInputElement).value;
    const cursor = (event.target as HTMLInputElement).selectionStart || 0;
    
    // Detect open brace '{' before cursor position
    const textBeforeCursor = value.substring(0, cursor);
    const lastBraceIndex = textBeforeCursor.lastIndexOf('{');
    const lastCloseBraceIndex = textBeforeCursor.lastIndexOf('}');
    
    if (lastBraceIndex > lastCloseBraceIndex) {
      showAutocompletePopover.value = true;
      autocompleteSearchQuery.value = textBeforeCursor.substring(lastBraceIndex + 1);
    } else {
      showAutocompletePopover.value = false;
    }
  };
  
  const selectVariable = (varName: string) => {
    if (!nomenclatureInputRef.value) return;
    const input = nomenclatureInputRef.value;
    const value = input.value;
    const cursor = input.selectionStart || 0;
    const textBeforeCursor = value.substring(0, cursor);
    const textAfterCursor = value.substring(cursor);
    const lastBraceIndex = textBeforeCursor.lastIndexOf('{');
    
    // Replace typed search text with actual bracketed variable name
    const newValue = value.substring(0, lastBraceIndex) + `{${varName}}` + textAfterCursor;
    processNomenclature.value = newValue;
    updateProcessProperty('ReglaNomenclatura', newValue);
    
    // Hide autocomplete and refocus input
    showAutocompletePopover.value = false;
    nextTick(() => {
      input.focus();
      const newCursorPos = lastBraceIndex + varName.length + 2;
      input.setSelectionRange(newCursorPos, newCursorPos);
    });
  };
  ```

### 4.5 Color-Coded Pills Preview Rendering
Since a standard HTML `<input>` cannot render colored children internally, we render a highly visual, read-only preview row immediately beneath the input container.
* **State Parsing:**
  ```typescript
  const nomenclatureParts = computed(() => {
    const val = processNomenclature.value || '';
    const parts = val.split(/(\{.*?\})/g);
    
    return parts.map(part => {
      const isVariable = part.startsWith('{') && part.endsWith('}');
      if (!isVariable) {
        return { text: part, isVariable: false };
      }
      
      const varName = part.substring(1, part.length - 1);
      const foundVar = mergedVariables.value.find(v => v.name === varName);
      
      let badgeClass = 'bg-red-50 text-red-700 border-red-200 dark:bg-red-950/20 dark:text-red-400 dark:border-red-800'; // Unresolved
      if (foundVar) {
        if (foundVar.source === 'Session') badgeClass = 'bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-950/20 dark:text-blue-400 dark:border-blue-800';
        else if (foundVar.source === 'Form') badgeClass = 'bg-green-50 text-green-700 border-green-200 dark:bg-green-950/20 dark:text-green-400 dark:border-green-800';
        else if (foundVar.source === 'Connector') badgeClass = 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/20 dark:text-amber-400 dark:border-amber-800';
        else if (foundVar.source === 'Glossary') badgeClass = 'bg-purple-50 text-purple-700 border-purple-200 dark:bg-purple-950/20 dark:text-purple-400 dark:border-purple-800';
      }
      
      return {
        text: part,
        isVariable: true,
        badgeClass,
        type: foundVar ? foundVar.type : 'Desconocido'
      };
    });
  });
  ```
* **HTML template block for Pills Preview:**
  ```html
  <div v-if="processNomenclature" class="mt-2 flex flex-wrap items-center gap-1 text-[11px] border border-gray-100 dark:border-gray-800 rounded p-2 bg-gray-50/50 dark:bg-gray-900/50">
    <span class="text-gray-400 font-medium mr-1">Regla procesada:</span>
    <template v-for="(part, idx) in nomenclatureParts" :key="idx">
      <span v-if="part.isVariable" :class="part.badgeClass" class="inline-flex items-center gap-1 px-1.5 py-0.5 rounded border font-mono font-semibold" :title="`Tipo: ${part.type}`">
        {{ part.text }}
      </span>
      <span v-else class="text-gray-700 dark:text-gray-300 font-mono">{{ part.text }}</span>
    </template>
  </div>
  ```

### 4.6 Dummies-tone Explanatory Tooltip
Add `NOMENCLATURE_DUMMY` to the `bpmnTooltips` registry inside `BpmnDesigner.vue`:
```typescript
NOMENCLATURE_DUMMY: '🎟 ¿Qué es esto? Es una plantilla para nombrar las solicitudes automáticamente. \n\n' +
                    '1. Escribe texto fijo como "OC-".\n' +
                    '2. Abre una llave "{" para ver qué variables puedes meter (como {session.user_name} o campos de tus formularios).\n' +
                    '3. Ejemplo: "OC-{solicitante_nombre}" creará tickets que se vean como "OC-Carlos".'
```
We use this tooltip text inside the label structure.
```html
<AppTooltip :content="isNomenclatureSyntaxError ? '⚠️ Error de sintaxis: llaves sin cerrar' : bpmnTooltips.NOMENCLATURE_DUMMY" :isError="isNomenclatureSyntaxError" />
```

---

## 5. Draft Unit Test Proposal (`BpmnDesigner.spec.ts`)

Here is the structured test code to be added inside `BpmnDesigner.spec.ts` for full verification of CA-5:

```typescript
    describe('CA-5: Business Glossary & Nomenclature Rule Autocomplete', () => {
        let wrapper: any;

        beforeEach(async () => {
            wrapper = mount(BpmnDesigner, {
                global: {
                    plugins: [createTestingPinia({ stubActions: false })],
                    stubs: {
                        AppTooltip: { template: '<span class="mock-tooltip"><slot /></span>' }
                    }
                }
            });
            // Mock diagram load
            await wrapper.vm.onMounted;
        });

        afterEach(() => {
            wrapper.unmount();
        });

        it('should correctly initialize with empty glossary and no nomenclature rule', () => {
            expect(wrapper.vm.declaredVariables).toEqual([]);
            expect(wrapper.vm.processNomenclature).toBe('');
        });

        it('should allow adding manual variables to the glossary and persist them to XML', async () => {
            wrapper.vm.newVarName = 'montoAprobado';
            wrapper.vm.newVarType = 'Number';
            await wrapper.vm.addDeclaredVariable();

            expect(wrapper.vm.declaredVariables).toContainEqual({ name: 'montoAprobado', type: 'Number' });
            
            // Verify XML persistence call
            const rootElement = wrapper.vm.modelerInstance.get('canvas').getRootElement();
            const extensionElements = rootElement.businessObject.get('extensionElements');
            const camundaProperties = extensionElements.values.find((e: any) => e.$type === 'camunda:Properties');
            const glossaryProp = camundaProperties.values.find((p: any) => p.name === 'GlosarioVariables');
            
            expect(glossaryProp).toBeDefined();
            expect(JSON.parse(glossaryProp.value)).toContainEqual({ name: 'montoAprobado', type: 'Number' });
        });

        it('should validate manual variable inputs and prevent duplicate keys', async () => {
            wrapper.vm.declaredVariables = [{ name: 'dupVar', type: 'String' }];
            wrapper.vm.newVarName = 'dupVar';
            wrapper.vm.newVarType = 'Boolean';
            
            const spyToast = vi.spyOn(wrapper.vm, 'showToast');
            await wrapper.vm.addDeclaredVariable();

            // Should reject duplicates
            expect(wrapper.vm.declaredVariables.length).toBe(1);
            expect(spyToast).toHaveBeenCalledWith('La variable dupVar ya está declarada', 'error');
        });

        it('should dynamically merge variables from different sources', () => {
            wrapper.vm.declaredVariables = [{ name: 'varManual', type: 'Number' }];
            wrapper.vm.processVariables = [{ name: 'varConnector', type: 'String' }];
            wrapper.vm.formFieldsCache = {
                'formKey_1': [{ name: 'varForm', type: 'String' }]
            };

            const merged = wrapper.vm.mergedVariables;
            
            // Check session context variables
            expect(merged).toContainEqual({ name: 'session.user_name', type: 'String', source: 'Session' });
            expect(merged).toContainEqual({ name: 'session.email', type: 'String', source: 'Session' });
            
            // Check sources
            expect(merged).toContainEqual({ name: 'varManual', type: 'Number', source: 'Glossary' });
            expect(merged).toContainEqual({ name: 'varConnector', type: 'String', source: 'Connector' });
            expect(merged).toContainEqual({ name: 'varForm', type: 'String', source: 'Form' });
        });

        it('should toggle the autocomplete popover on nomenclature input', async () => {
            const input = wrapper.find('input[placeholder="Ej: OC-{Solicitante}"]');
            await input.setValue('OC-');
            expect(wrapper.vm.showAutocompletePopover).toBe(false);

            await input.setValue('OC-{');
            // Trigger input event with cursor position right after '{'
            const nativeInput = input.element as HTMLInputElement;
            nativeInput.setSelectionRange(4, 4);
            await input.trigger('input');
            
            expect(wrapper.vm.showAutocompletePopover).toBe(true);
        });
    });
```
