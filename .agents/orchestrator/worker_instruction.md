# Worker Instruction - Verification of DMN Governance and Integration Tests

You are a Verification Worker. Your task is to verify the DMN Governance Hexagonal refactoring and resolve integration test failures.

## Context
We have refactored the DMN governance module of US-007 to comply with ADR-001 (Hexagonal Architecture / DDD).
The domain layer is purified, but the integration tests of `ibpms-core` are failing to run successfully.
Specifically:
1. Some tests fail with database connection/authentication errors because they do not extend `AbstractIntegrationTest` and try to connect using default properties (`ibpms_user`/`ibpms_password` on port 5433).
2. The database container might have dirty state (`liquibase.exception.DatabaseException: ERROR: relation "ibpms_case" already exists`).

## Tasks

### 1. Recreate clean Database Containers
Run the following commands using the PowerShell terminal from `C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform` (or command line) to stop the existing e2e containers, wipe volumes, and restart them clean:
- `docker compose -f docker-compose.e2e.yml down -v`
- `docker compose -f docker-compose.e2e.yml up -d`
- Wait for a few seconds to ensure they are healthy. You can check with `docker ps`.

### 2. Run DmnArchitectureComplianceTest
Verify that the compliance test passes:
- `..\maven\apache-maven-3.9.6\bin\mvn.cmd test -Dtest=DmnArchitectureComplianceTest -pl ibpms-core`

### 3. Run and Fix Integration Tests
Run all tests in `ibpms-core`:
- `..\maven\apache-maven-3.9.6\bin\mvn.cmd test -pl ibpms-core`
If there are failures because of connection/authentication issues (e.g. `WorkdeskQueryControllerDelegationTest` etc.):
- Identify the test classes that fail with `PSQLException: FATAL: password authentication failed for user "ibpms_user"` or similar.
- For each such test class:
  - If it is an integration test using `@SpringBootTest` but not inheriting from `AbstractIntegrationTest`, modify it to inherit from `AbstractIntegrationTest` (or define proper `@DynamicPropertySource` or property overrides).
  - IMPORTANT: If you modify any Java source or test file, you MUST add `// @Traceability: US-007 - ADR-001` on line 1 of the file.
- If there are other compilation/runtime failures related to the refactored DMN governance classes, fix them.

### 4. Verify Integrity and Traceability
- Ensure every file you modify has `// @Traceability: US-007 - ADR-001` on line 1.
- Ensure that the domain model `DmnModel.java` has no JPA/Hibernate annotations.

### 5. Document and Report
- Write your handoff report to `C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\.agents\worker_verification_8\handoff.md`.
- Send a message to the orchestrator (conversation ID: 0012de79-7a57-425a-84c8-8ab3b0c0cb71) when you are done.

### MANDATORY INTEGRITY WARNING
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A Forensic Auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.
