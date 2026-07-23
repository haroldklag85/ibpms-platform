## 2026-05-30T05:21:50Z
You are the Worker. Your working directory is c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\.agents\worker_remediation_1.
Your task is to fix the MapStruct/Lombok dependency ordering and add the required traceability headers to resolve the Forensic Audit's failures.

### MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A Forensic Auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

### Tasks:
1. Fix annotation processors configuration in `backend/ibpms-core/pom.xml`:
   - Under `<annotationProcessorPaths>` in the `maven-compiler-plugin`, declare `lombok` before `mapstruct-processor`.
   - Also, add the `lombok-mapstruct-binding` dependency path to ensure correct mapping code generation.
   - Example structure under `<annotationProcessorPaths>`:
     ```xml
     <path>
         <groupId>org.projectlombok</groupId>
         <artifactId>lombok</artifactId>
         <version>1.18.30</version>
     </path>
     <path>
         <groupId>org.mapstruct</groupId>
         <artifactId>mapstruct-processor</artifactId>
         <version>1.5.5.Final</version>
     </path>
     <path>
         <groupId>org.projectlombok</groupId>
         <artifactId>lombok-mapstruct-binding</artifactId>
         <version>0.2.0</version>
     </path>
     ```

2. Add the traceability comment `// @Traceability: US-003 - ADR-001` on the very first line (line 1) of the following 14 files:
   - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/in/UpdateFeatureToggleUseCase.java`
   - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/FeatureTogglePort.java`
   - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/FormDesignPort.java`
   - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/GenericProcessDefinitionPort.java`
   - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/GenericTaskPort.java`
   - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/ImpersonationPort.java`
   - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/MenuTopologyPort.java`
   - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/ProcessEnginePort.java`
   - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/RoleHierarchyPort.java`
   - `backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/out/TaskQueryPort.java`
   - `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/FormEventStoreEntity.java`
   - `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/TaskDraftJpaEntity.java`
   - `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/adapter/BpmnAuditJpaAdapterTest.java` (ensure it starts with `// @Traceability: US-003 - ADR-001` as line 1; replace the current `// @Traceability: US-005, CA-63` line)
   - `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/adapter/DataMappingJpaAdapterTest.java` (ensure it starts with `// @Traceability: US-003 - ADR-001` as line 1; replace the current `// @Traceability: US-005, CA-63` line)

3. Verify:
   - Run `mvn clean compile` in `backend/ibpms-core` to verify that compilation succeeds.
   - Inspect the generated mappers in `target/generated-sources/...` to ensure that mapping implementations are generated correctly with properties being mapped (instead of being empty).
   - Run `mvn test` in `backend/ibpms-core` to ensure all tests (including `TaskDraftIntegrationTest`) pass.

4. Write a detailed `changes.md` and `handoff.md` inside your working directory.
5. Report back when done.

## 2026-05-30T05:44:02Z
**Context**: Checking status of remediation task.
**Content**: Please provide a status update on compilation and test run. If it is completed, please write the handoff reports and report back.
**Action**: Reply with your current status.
