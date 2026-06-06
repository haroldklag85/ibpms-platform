# Worker Instructions - US-005, CA-5 Glosario de Datos Unificado (Propuesta 2)

You are the `teamwork_preview_worker` subagent. Your task is to implement the "Glosario de Datos Unificado (Propuesta 2)" in `BpmnDesigner.vue` and write the corresponding unit tests in `BpmnDesigner.spec.ts` under the CA-5 scope.

## Traceability Requirement
- You MUST insert the following comment as a traceability line at the top of any modified code files (i.e. `BpmnDesigner.vue` and `BpmnDesigner.spec.ts`):
  `// @Traceability: US-005, CA-5`

## Tasks

### 1. Business Variables Glossary State & UI Panel in `BpmnDesigner.vue`
- Add a new collapsible card section titled "Glosario de Variables de Negocio" in the process properties sidebar of `BpmnDesigner.vue`. It should be visible only when no element is selected (root process properties, alongside the Nomenclature Rule and SLA inputs).
- Declare a reactive state variable `declaredVariables` as a ref array of objects: `{ name: string, type: 'String' | 'Number' | 'Boolean' }[]`.
- Inside the card, render a list of declared variables, with a button to delete/remove each variable.
- Include a form to add a new variable:
  - Input field for name/key (validated for alphanumeric/underscores, not empty, no duplicates).
  - Dropdown select for type (`String`, `Number`, `Boolean`).
  - Action button to add the variable. If the name is invalid or a duplicate, show a Toast error (e.g. `showToast('La variable ' + name + ' ya está declarada', 'error')`).

### 2. Variable Persistence in BPMN XML Extension elements
- Use `updateProcessProperty('GlosarioVariables', JSON.stringify(declaredVariables.value))` to persist the glossary list into the XML under the `GlosarioVariables` extension property. Call this whenever a manual variable is added or removed.
- Hook into the BPMN modeler's `import.done` event. When a diagram imports successfully, parse the `GlosarioVariables` property from the process extension elements to populate `declaredVariables`. (Ensure to handle missing or corrupt properties gracefully, defaulting to an empty array).
- Also ensure that `processNomenclature` and `globalSla` are rehydrated from the XML root extension elements upon import.

### 3. Dynamic Variable Merging
- Build a reactive computed property `mergedVariables` that returns a list of unified variables from the following sources:
  1. **Sesión/Sistema (Auto):** e.g., `{session.user_name}`, `{session.email}` (always present).
  2. **Formularios Activos:** Extract unique `camunda:formKey` properties by scanning the diagram's User Tasks and Start Event. Fetch form fields from `/api/v1/forms/{formKey}/versions/1` using the `integrationStore`. Cache the results in a reactive dictionary `formFieldsCache` to avoid repeated fetches. Include form variables as `{name: field.camundaVariable || field.id, type: ..., source: 'Form'}`.
  3. **Integraciones/Webhooks:** Merge connector/webhook output variables and active process variables (`processVariables`).
  4. **Manual Glossary Variables:** Merge manually declared variables from `declaredVariables` as `source: 'Glossary'`.
- Deduplicate variables by name in the merged list.

### 4. Autocomplete Pill Editor for Nomenclature Rule Input
- Replace the simple text input for `processNomenclature` with an autocomplete pill editor:
  - When typing a curly brace `{` in the text field, show an autocomplete popover/list of the merged variables filtered by the text after `{`.
  - When a variable is selected from the suggestions, replace the brace search query with `{glosario.<variable_key>}` (or `{session.user_name}` / `{session.email}` for system context) in the input value and update the XML. Refocus the input and set the cursor after the inserted variable.
  - Render color-coded pills/chips immediately below the input container using regex token splits to separate plain text from `{variable}` tokens. Style these badges based on their source (e.g. Blue for Session, Green for Form, Purple for Glossary, Amber for Connector/Webhook, Red with error styling for unresolved/unknown variables).

### 5. Dummies-Tone Tooltip
- Add a tooltip button `❓` next to the "Regla de Nomenclatura (CA-5)" label.
- The tooltip content must be styled beautifully and written in an extremely friendly "dummies-tone" to explain:
  1. What a nomenclature rule is.
  2. How the `{var}` placeholders get automatically replaced at runtime when starting a case.
  3. Simple examples (e.g., explaining that `TICKET-{session.user_name}` resolves to `TICKET-Carlos`).
  - E.g.: `'🎟 ¿Qué es esto? Es una plantilla para nombrar las solicitudes automáticamente. \n\n1. Escribe texto fijo como "OC-".\n2. Abre una llave "{" para ver qué variables puedes meter (como {session.user_name} o campos de tus formularios).\n3. Ejemplo: "OC-{solicitante_nombre}" creará tickets que se vean como "OC-Carlos".'`

### 6. Component Unit Tests in `BpmnDesigner.spec.ts`
- Append a new test block `describe('CA-5: Business Glossary & Nomenclature Rule Autocomplete', () => { ... })` at the end of `BpmnDesigner.spec.ts`.
- Write unit tests that mock the necessary context and assert:
  - The "Glosario de Variables de Negocio" panel is rendered and allows adding manual variables.
  - Adding/removing variables updates both the local state and the XML.
  - Duplicate keys are rejected and trigger a Toast error.
  - Variables from different sources are merged dynamically.
  - Autocomplete popover opens on typing `{` and inserts the chosen variable.
  - Tooltip contains the expected friendly content.

## Verification
- You MUST execute `npx vitest run` in the frontend directory (`frontend/`) and ensure 100% of tests pass.
- You MUST execute `npm run build` in the frontend directory (`frontend/`) and ensure that the production compilation finishes successfully with zero warnings/errors.
- Include the exact commands run and output results in your handoff report.

### MANDATORY INTEGRITY WARNING
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A Forensic Auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.
