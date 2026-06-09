# Scope: Frontend M2 (US-004 Triage Items)

## Architecture
- Frontend Vue application using Pinia for state management.
- Pinia store: `useIntakeTriageStore.ts`
- View: `IntakeTriageView.vue` (Dumb Component)
- Traceability metadata: `@Traceability: US-004, CA-6, CA-8`
- TailwindCSS for styling.
- Router config for the view.

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | Frontend M2 | Store, View, Router, Traceability | none | DONE |

## Interface Contracts
- Store must expose actions to fetch list of triage items, and process a triage item.
- View must not make direct HTTP requests (must use Store).
