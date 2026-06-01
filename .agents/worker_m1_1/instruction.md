# Worker Instruction - Milestone 1: Implement Failing Architecture Test (TDD Phase 1)

You are Worker 1. Your task is to implement the updated architectural compliance test and run it to verify that it fails.

## Requirements
1. Modify `backend/ibpms-core/src/test/java/com/ibpms/poc/application/usecase/dmn/DmnArchitectureComplianceTest.java` with the following content:
```java
// @Traceability: US-007 - ADR-001
package com.ibpms.poc.application.usecase.dmn;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Architectural compliance test asserting that DmnGovernanceUseCase does not violate ADR-001.
 * It ensures DmnGovernanceUseCase does not contain any imports or usage of JPA/persistence entities,
 * repositories, or annotations (e.g. DmnModelEntity, DmnModelRepository, jakarta.persistence.*, org.springframework.data.jpa.*).
 */
public class DmnArchitectureComplianceTest {

    @Test
    public void testHexagonalCompliance() throws IOException {
        Path path = Paths.get("src/main/java/com/ibpms/poc/application/usecase/dmn/DmnGovernanceUseCase.java");
        if (!Files.exists(path)) {
            path = Paths.get("ibpms-core/src/main/java/com/ibpms/poc/application/usecase/dmn/DmnGovernanceUseCase.java");
        }
        if (!Files.exists(path)) {
            path = Paths.get("backend/ibpms-core/src/main/java/com/ibpms/poc/application/usecase/dmn/DmnGovernanceUseCase.java");
        }
        
        if (!Files.exists(path)) {
            fail("Source file DmnGovernanceUseCase.java not found.");
        }

        String content = Files.readString(path);
        
        // Remove block comments (/* ... */) and line comments (// ...)
        // safely ignoring comments to avoid false positives on comments
        String cleanContent = content
            .replaceAll("/\\*(?s).*?\\*/", "")
            .replaceAll("//.*", "");

        // Forbidden classes and packages representing persistence or infrastructure leak (ADR-001)
        String[] forbiddenTokens = {
            "DmnModelEntity",
            "DmnModelRepository",
            "jakarta.persistence",
            "javax.persistence",
            "org.springframework.data.jpa"
        };

        for (String token : forbiddenTokens) {
            if (cleanContent.contains(token)) {
                fail("Architectural violation (ADR-001): DmnGovernanceUseCase contains forbidden import or usage of: " + token);
            }
        }
    }
}
```

2. Run the command to execute only this test:
   `mvn test -Dtest=DmnArchitectureComplianceTest -pl ibpms-core` inside the `backend` directory (or appropriate directory where `pom.xml` is located).
   Note: The tool `run_command` should be run in `C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend`.
3. Confirm that the test fails (Red Phase) as expected due to the imports of `DmnModelEntity` and `DmnModelRepository` in `DmnGovernanceUseCase.java`.
4. Output your handoff report to `C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\.agents\worker_m1_1\handoff.md` with:
   - Command run and output
   - The test failure stack trace/message
   - Confirmation of TDD red phase
5. Call `send_message` to the parent with the results.

### MANDATORY INTEGRITY WARNING
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A Forensic Auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.
