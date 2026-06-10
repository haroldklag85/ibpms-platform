# Project Plan: US-005 Process Version Tag Auto-Suggestion Homologated to v0

## Architecture
- **Frontend Component**: `BpmnDesigner.vue` handles the BPMN modeler interface and holds the process version auto-suggestion logic.
- **Unit Test**: `BpmnDesigner.spec.ts` exercises the BpmnDesigner component and asserts correct behavior including version tag auto-suggestion.

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | Exploration | Search and analyze the exact locations in BpmnDesigner.vue and BpmnDesigner.spec.ts | None | DONE |
| 2 | Implementation & Local Verification | Modify BpmnDesigner.vue and BpmnDesigner.spec.ts, run unit tests, and build the frontend | Milestone 1 | DONE |
| 3 | Quality Review | Review code quality, strict typing, and adherence to clean code rules | Milestone 2 | DONE |
| 4 | Integrity Audit | Run Forensic Auditor to ensure no cheating, hardcoded test logic, or bad practices | Milestone 3 | DONE |
| 5 | Gate & Synthesis | Confirm passing of tests, build, and audit, and report back | Milestone 4 | DONE |
| 6 | Remediation - Fix Backend JUnit and Frontend Vitest failures | Fix BpmnDeployContractTest regression and flaky frontend tests, commit changes | Milestone 5 | DONE |
| 7 | Commit & Final Victory Verification | Confirm all tests pass, changes committed, and re-audit | Milestone 6 | DONE |

## Interface Contracts
### Modeler Suggestion Logic
- Inputs: `currentVersion` (number) or absence of version tag
- Outputs: Suggestions for version tag: '0.0.0' (v0) when draft/lacking, instead of '1.0.0'.
