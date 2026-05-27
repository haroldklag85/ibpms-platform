# Implementation Plan — Shared Clipboard (US-005, CA-29)

This plan outlines the steps for implementing the shared clipboard between different BPMN modeler instances using `localStorage`.

## 1. Objectives
- Decorate `clipboard.get` and `clipboard.set` methods of the `bpmn-js` clipboard service inside `BpmnDesigner.vue`.
- Save copies securely into `localStorage` under `bpmn_shared_clipboard` removing circular references (e.g., `$parent`, `parent`).
- Expose the clipboard via `getModelerClipboard()` to make it testable.
- Ensure the Vitest test suite runs successfully.
- Build the frontend production bundle to ensure no compile-time errors.
- Commit and push changes to branch `sprint-6` (no git stash).

## 2. Implementation Steps

### Step 1: Decorate the Modeler's Clipboard
Inside `BpmnDesigner.vue` on the `onMounted` hook:
1. Retrieve the `clipboard` service:
   ```typescript
   const clipboard = modelerInstance.get('clipboard');
   ```
2. Decorate `clipboard.set(data)`:
   - Call the original set method.
   - Use a custom replacer to serialize `data` to JSON safely (removing circular references `$parent` and `parent`).
   - Store the serialized JSON in `localStorage` under `bpmn_shared_clipboard`.
3. Decorate `clipboard.get()`:
   - Try to retrieve `bpmn_shared_clipboard` from `localStorage`.
   - Parse it and return the data.
   - If not found or fails, fallback to the original get method.

### Step 2: Expose the Clipboard for Testability
At the end of the script tag in `BpmnDesigner.vue`, expose a method `getModelerClipboard` returning the modeler's clipboard instance (or null if not initialized):
```typescript
const getModelerClipboard = () => {
  return modelerInstance ? modelerInstance.get('clipboard') : null;
};
```
If using `<script setup>`, ensure it is exposed (e.g., using `defineExpose({ ..., getModelerClipboard })` or since all properties in setup are scoped, let's verify if `defineExpose` is needed).
Currently, in `BpmnDesigner.vue`, there is no `defineExpose`. Let's define `defineExpose` at the very end of the `<script setup>` block:
```typescript
defineExpose({
  // other methods/refs if needed, but mainly:
  getModelerClipboard,
  saveDraft,
  preFlightStatus,
  onDiagramEdit,
  processPattern,
  filteredForms,
  availableConnectors,
  showToast,
  toast,
  zoomIn,
  zoomOut,
  zoomFit
});
```
Wait! Let's check what variables are accessed by the wrapper in `BpmnDesigner.spec.ts`:
Looking at `BpmnDesigner.spec.ts`:
- `wrapper.vm.saveDraft()` (line 101)
- `wrapper.vm.preFlightStatus` (line 115)
- `wrapper.vm.onDiagramEdit()` (line 116)
- `wrapper.vm.processPattern` (line 128)
- `wrapper.vm.filteredForms` (line 130)
- `wrapper.vm.availableConnectors` (line 146)
- `wrapper.vm.showToast(...)` (line 169)
- `wrapper.vm.toast` (line 173)
So yes, exposing all these properties in `defineExpose` ensures they remain accessible to Vitest tests when the component is mounted!
Wait, in Vue 3 `<script setup>`, properties are **not** exposed to the outside (like unit tests using `wrapper.vm`) by default, unless they are explicitly exposed via `defineExpose`. Wait, let's check if there is an existing `defineExpose` or if Vue is configured in a way that doesn't need it. There was no `defineExpose` in our search, so we will add it.

### Step 3: Add Traceability Comments
Add the comment:
`// @Traceability: US-005, CA-29 Copiar y Pegar Fragmentos entre Procesos`
next to the decorated functions in `BpmnDesigner.vue`.

## 3. Verification Plan
1. Run local tests:
   `npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts` in `frontend/`.
2. Check that all tests pass, including the new ones for CA-29.
3. Build the project:
   `npm run build` in `frontend/`.
4. Perform git commit and push to `sprint-6`.
