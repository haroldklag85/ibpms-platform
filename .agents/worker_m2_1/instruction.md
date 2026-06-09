# Worker Instruction - Milestones 2-6: Complete DMN Governance Refactoring

You are Worker 2. Your task is to refactor the DMN governance module of US-007 to comply with ADR-001 (Hexagonal Architecture / DDD).

## Requirements

### Step 1. Create Domain Models & Ports
1. Create `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/model/DmnModel.java` as a pure POJO (no JPA, Hibernate, or Spring persistence annotations). It must contain:
   - Fields: `id` (String), `xmlContent` (String), `status` (String), `name` (String), `createdAt` (LocalDateTime), `updatedAt` (LocalDateTime), `authorJwtHash` (String), `tenantId` (String), `chatHistoryJson` (String), `isManual` (Boolean).
   - Standard getters and setters.
   - Traceability header: `// @Traceability: US-007 - ADR-001` on the first line.
2. Create `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/port/DmnModelRepositoryPort.java` as a domain port interface. It must contain the following methods:
   - `Optional<DmnModel> findById(String id)`
   - `DmnModel save(DmnModel dmnModel)`
   - `void delete(DmnModel dmnModel)`
   - `List<DmnModel> findByTenantId(String tenantId)`
   - `List<DmnModel> findByStatusAndUpdatedAtBefore(String status, LocalDateTime cutoff)`
   - Traceability header: `// @Traceability: US-007 - ADR-001` on the first line.

### Step 2. Create Infrastructure Entities, Mappers & Adapters
1. Rename `DmnModelEntity.java` to `DmnModelJpaEntity.java` under `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/dmn/`.
   - Update class name and constructor to `DmnModelJpaEntity`.
   - Make sure all JPA/persistence annotations and table mappings are kept.
   - Traceability header: `// @Traceability: US-007 - ADR-001` on the first line.
2. Create `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/mapper/DmnModelMapper.java` using MapStruct. It should define mapping bidirectionally between `DmnModel` and `DmnModelJpaEntity` with component model `spring`:
   ```java
   // @Traceability: US-007 - ADR-001
   package com.ibpms.poc.infrastructure.jpa.mapper;

   import com.ibpms.poc.domain.model.DmnModel;
   import com.ibpms.poc.infrastructure.jpa.entity.dmn.DmnModelJpaEntity;
   import org.mapstruct.Mapper;

   @Mapper(componentModel = "spring")
   public interface DmnModelMapper {
       DmnModel toDomain(DmnModelJpaEntity entity);
       DmnModelJpaEntity toEntity(DmnModel domain);
   }
   ```
3. Update `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/repository/dmn/DmnModelRepository.java` to extend `JpaRepository<DmnModelJpaEntity, String>`. Update return/parameter types from `DmnModelEntity` to `DmnModelJpaEntity`.
   - Traceability header: `// @Traceability: US-007 - ADR-001` on the first line.
4. Create `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapter/DmnModelJpaAdapter.java` implementing `DmnModelRepositoryPort`. It should delegate to `DmnModelRepository` and use `DmnModelMapper` for conversion between the domain and database entities.
   - Traceability header: `// @Traceability: US-007 - ADR-001` on the first line.

### Step 3. Refactor Use Cases and Controllers
1. Refactor `backend/ibpms-core/src/main/java/com/ibpms/poc/application/usecase/dmn/DmnGovernanceUseCase.java`:
   - Inject `DmnModelRepositoryPort` instead of `DmnModelRepository`.
   - Change occurrences of `DmnModelEntity` to `DmnModel`.
   - Remove imports of `DmnModelEntity` and `DmnModelRepository` (infrastructure/JPA classes).
   - Traceability header: `// @Traceability: US-007 - ADR-001` on the first line.
2. Update `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/dmn/DmnGovernanceController.java` or any other controllers/classes referencing the old `DmnModelEntity` to use the appropriate layers/entities.
   - Traceability header: `// @Traceability: US-007 - ADR-001` on the first line.

### Step 4. Compile and Run Verification Tests
1. Navigate to `backend` directory.
2. Compile and test using the local maven executable:
   `cmd.exe /c "..\maven\apache-maven-3.9.6\bin\mvn.cmd clean test-compile -pl ibpms-core"`
3. Fix any compilation errors.
4. Run the architectural compliance test to verify it now passes (Green Phase):
   `cmd.exe /c "..\maven\apache-maven-3.9.6\bin\mvn.cmd test -Dtest=DmnArchitectureComplianceTest -pl ibpms-core"`
5. Run the full test suite in `ibpms-core` to verify all tests pass:
   `cmd.exe /c "..\maven\apache-maven-3.9.6\bin\mvn.cmd test -pl ibpms-core"`
6. Output your handoff report to `C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\.agents\worker_m2_1\handoff.md` showing compilation status, compliance test results, and test suite execution summary.
7. Call `send_message` to the parent with the results.

### MANDATORY INTEGRITY WARNING
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A Forensic Auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.
