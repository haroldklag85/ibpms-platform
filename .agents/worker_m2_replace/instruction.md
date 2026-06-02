# Replacement Worker Instruction - Refactoring Verification & Fixes

You are Worker 3. You are replacing Worker 2 who became unresponsive during the verification phase of the DMN Governance refactoring. Most of the code files have already been created or modified by Worker 2, but they need compilation check, potential fixes, and test runs.

## Requirements

### Step 1. Verify Existing Refactored Files
Check the following files to ensure they conform to the architectural requirements (Hexagonal/DDD) and contain the traceability comment `// @Traceability: US-007 - ADR-001` on their first line:
1. `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/model/DmnModel.java` (pure POJO, no JPA annotations)
2. `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/port/DmnModelRepositoryPort.java`
3. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/dmn/DmnModelJpaEntity.java` (JPA Entity, renamed from DmnModelEntity.java)
4. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/mapper/DmnModelMapper.java` (MapStruct mapper)
5. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapter/DmnModelJpaAdapter.java` (implements DmnModelRepositoryPort)
6. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/repository/dmn/DmnModelRepository.java` (extends JpaRepository<DmnModelJpaEntity, String>)
7. `backend/ibpms-core/src/main/java/com/ibpms/poc/application/usecase/dmn/DmnGovernanceUseCase.java` (uses port, no JPA imports)
8. `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/dmn/DmnGovernanceController.java` (uses usecase/port/dto as appropriate)
9. Schedulers/Jobs like `DmnDraftCleanupScheduler.java` and `DmnGarbageCollectorJob.java` that might use the repository or entity.

### Step 2. Build and Fix Compilation Errors
1. Navigate to the `backend` directory.
2. Run compilation using the project's maven executable:
   `cmd.exe /c "..\maven\apache-maven-3.9.6\bin\mvn.cmd clean test-compile -pl ibpms-core"`
3. If there are compilation errors (e.g., in other tests, controllers, or configuration files that referenced `DmnModelEntity` or `DmnModelRepository` directly), resolve them. Make sure to add `// @Traceability: US-007 - ADR-001` to the first line of any files you modify.

### Step 3. Run and Verify Test Suite
1. Run the specific architectural compliance test:
   `cmd.exe /c "..\maven\apache-maven-3.9.6\bin\mvn.cmd test -Dtest=DmnArchitectureComplianceTest -pl ibpms-core"`
   Verify that this test now passes (Green Phase).
2. Run all tests in `ibpms-core` to verify no regressions:
   `cmd.exe /c "..\maven\apache-maven-3.9.6\bin\mvn.cmd test -pl ibpms-core"`
3. Report any failures and fix them if they were caused by the DMN governance refactoring.

### Step 4. Document and Report
1. Write your handoff report to `C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\.agents\worker_m2_replace\handoff.md`. Include:
   - Verification status of the files.
   - Compilation and test execution output snippets.
   - Exact commands used.
2. Send a message to the orchestrator (conversation ID: 0012de79-7a57-425a-84c8-8ab3b0c0cb71) with a summary of your findings and verification results.

### MANDATORY INTEGRITY WARNING
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A Forensic Auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.
