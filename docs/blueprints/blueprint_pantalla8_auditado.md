# Project Template Builder (Epic 8) - QA & Schema Refactoring Plan

## User Review Required
> [!WARNING]
> The current underlying database architecture uses a `Dual Schema Data Architecture` (`phases_json` JSON column) instead of full relational entity structures.
> To fulfill the **"5 tablas jerárquicas ON DELETE CASCADE"** instruction mathematically, I will execute a Liquibase DDL migration destroying the JSON column and constructing proper PostgreSQL Foreign Keys.

## Proposed Changes

### Database Schema (Liquibase / DDL)
We will introduce a new migration script `13-refactor-project-template-hierarchy.sql` introducing:
- `ibpms_pt_phase` (Foreign Key -> `ibpms_project_template`)
- `ibpms_pt_milestone` (Foreign Key -> `ibpms_pt_phase`)
- `ibpms_pt_task` (Foreign Key -> `ibpms_pt_milestone`)
- `ibpms_pt_dependency` (Composite Foreign Keys -> `ibpms_pt_task` source and target)
> All Foreign Keys will strictly enforce `ON DELETE CASCADE`.

### Backend Entities (Hibernate / JPA)
Refactor `ProjectTemplateEntity` to replace the `String phasesJson` column with `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` mappings pointing to the 4 nested Entities. Provide topological sort algorithms during the `POST /api/v1/templates` endpoint to ensure DAG validity (preventing cycles) before triggering repository saves.

### Frontend Store (Vitest Assurance)
Implement logic in the Vue Store / View limiting the "Publish" button. 
- The `isPublishable` computed property MUST return `false` if `rawTasks` (flattened tasks) contain any task with a missing `form_key`.
- Tests will assert the `Publish` element is `disabled`.

## Verification Plan

### Automated Tests
*   `TemplateHierarchyIntegrityTest.java` (JUnit): Inject a cyclic payload via REST where `Task A` depends on `Task B`, and `Task B` depends on `Task A`. Assert that the topological validation layer intercepts this and returns HTTP 400.
*   `TemplateBuilder.spec.ts` (Vitest): Mount `TemplateBuilder.vue`. Dynamically insert 2 pseudo-tasks, modify only 1 with a `form_key`. Assert the `[ PUBLICAR PLANTILLA ]` button strictly retains its `disabled` attribute.

### Sign-off
Upon Exit Code 0 from both the Backend Maven test suite and Frontend Vitest suite, document the evidence in `.agentic-sync/epic8_qa_report.md` as requested.
