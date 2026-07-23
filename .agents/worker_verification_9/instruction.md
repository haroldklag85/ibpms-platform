# Replacement Worker Instruction - Verification of DMN Governance and Integration Tests

You are a Verification Worker (Worker 9), replacing a previous worker that stalled. Your task is to complete the verification of the DMN Governance Hexagonal refactoring and resolve any remaining integration test failures.

## Context
We have refactored the DMN governance module of US-007 to comply with ADR-001 (Hexagonal Architecture / DDD).
The domain layer is purified, but some integration tests of `ibpms-core` might still be failing or need verification.

## Tasks

### 1. Recreate clean Database Containers
Run the following commands using the PowerShell terminal from `C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform` (or command line) to stop the existing e2e containers, wipe volumes, and restart them clean:
- `docker compose -f docker-compose.e2e.yml down -v`
- `docker compose -f docker-compose.e2e.yml up -d`
- Wait for a few seconds to ensure they are healthy. Check with `docker ps`.

### 2. Run DmnArchitectureComplianceTest
Verify that the compliance test passes:
- `..\maven\apache-maven-3.9.6\bin\mvn.cmd test -Dtest=DmnArchitectureComplianceTest -pl ibpms-core`

### 3. Run and Fix Integration Tests
Run all tests in `ibpms-core`:
- `..\maven\apache-maven-3.9.6\bin\mvn.cmd test -pl ibpms-core`

If there are integration tests failing due to PostgreSQL connection or credential issues (e.g. trying to connect to 5433 with wrong credentials):
- Identify the failing test classes.
- Refactor them to extend `AbstractIntegrationTest` (or define proper `@DynamicPropertySource` or property overrides).
- Note: A script `refactor_tests.py` exists in `.agents/worker_verification_8/refactor_tests.py` that was used to modify `KanbanStateTransitionIT.java`, `OrphanedTaskCleanupIntegrationTest.java`, and `TimeTrackingIT.java`. Review what has been done and complete any remaining refactorings.
- IMPORTANT: If you modify any Java source or test file, you MUST add `// @Traceability: US-007 - ADR-001` on line 1 of the file.

### 4. Verify Integrity and Traceability
- Ensure every file you modify has `// @Traceability: US-007 - ADR-001` on line 1.
- Ensure that the domain model `DmnModel.java` has no JPA/Hibernate annotations.

### 5. Document and Report
- Write your handoff report to `C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\.agents\worker_verification_9\handoff.md`.
- Include the exact maven build/test output commands and results in your handoff.
- Send a message to the orchestrator (conversation ID: 0012de79-7a57-425a-84c8-8ab3b0c0cb71) when you are done.

### MANDATORY INTEGRITY WARNING
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A Forensic Auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.
