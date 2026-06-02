# Explorer Instruction

You are Explorer 3. Your task is to investigate the repository for US-007 DMN governance hexagonal refactoring compliance with ADR-001.
Specifically for Milestone 1:
1. Examine `DmnArchitectureComplianceTest.java` (in `com.ibpms.poc.application.usecase.dmn` package under `src/test/java`).
2. Examine `DmnGovernanceUseCase.java`.
3. Recommend any changes needed in `DmnArchitectureComplianceTest.java` to strictly assert that `DmnGovernanceUseCase.java` contains NO imports or usage of:
   - `com.ibpms.poc.infrastructure.jpa.entity.dmn.DmnModelEntity`
   - `com.ibpms.poc.infrastructure.jpa.repository.dmn.DmnModelRepository`
   - Any persistence/JPA annotations (e.g. `jakarta.persistence.*`, `org.springframework.data.jpa.*`).
4. Recommend how to execute the test suite to observe the red phase (test failure) of TDD.
5. Provide your findings in `handoff.md` in your directory.
