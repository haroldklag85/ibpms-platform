# Project Context: US-005 Process Version Tag Auto-Suggestion

## Technical Stack
- Frontend: Vue 3 with Vite/TS.
- Testing: Vitest.

## Key Goals
- When process version tag auto-suggestion runs: if the process is a draft (currentVersion is 0) or lacks a version tag, suggest "0.0.0" instead of "1.0.0".
- Assertions in `BpmnDesigner.spec.ts` must align with the suggestion behavior.
- Clean code enforcement, strict typing (no any).
- Build check: `npm run build` must compile cleanly.
