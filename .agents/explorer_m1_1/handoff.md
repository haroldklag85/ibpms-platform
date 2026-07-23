# Handoff Report: US-007 Hexagonal Refactoring Compliance

## 1. Observation

- **Forbidden Dependencies in Use Case**:
  `DmnGovernanceUseCase.java` (located at `backend/ibpms-core/src/main/java/com/ibpms/poc/application/usecase/dmn/DmnGovernanceUseCase.java`) contains direct dependencies on infrastructure JPA entities and repositories:
  - Line 3: `import com.ibpms.poc.infrastructure.jpa.entity.dmn.DmnModelEntity;`
  - Line 4: `import com.ibpms.poc.infrastructure.jpa.repository.dmn.DmnModelRepository;`
  - Line 24: `private final DmnModelRepository dmnRepository;`
  - Line 36: `public DmnModelEntity updateDmnContent(...)`

- **Architectural Policy**:
  `adr-001-hexagonal-architecture.md` (located at `docs/architecture/adr-001-hexagonal-architecture.md`) states:
  - Line 21: `"1. Dominio Aislado (Entities & Domain Services): El código dentro de la capa domain no debe tener absolutamente ninguna dependencia tecnológica. No puede importar @Entity de JPA..."`
  - Line 22: `"2. Inversión de Dependencias (Puertos): La capa de aplicación application/ports definirá "Puertos de Entrada" e "Interfaces de Puertos de Salida" para hablar con bases de datos..."`

- **Current Compliance Test**:
  `DmnArchitectureComplianceTest.java` (located at `backend/ibpms-core/src/test/java/com/ibpms/poc/application/usecase/dmn/DmnArchitectureComplianceTest.java`) scans only lines starting with `"import "` and checks for `"com.ibpms.poc.infrastructure.jpa"` or `"jakarta.persistence"`. It can be bypassed by declaring fully qualified classes inline or using other JPA libraries.

- **Verbatim Red Phase Test Failure**:
  Executing the test using the repository's local Maven installation:
  ```powershell
  cd C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend
  ..\maven\apache-maven-3.9.6\bin\mvn.cmd test -pl ibpms-core -Dtest=DmnArchitectureComplianceTest
  ```
  Produces the following output:
  ```text
  [INFO] Running com.ibpms.poc.application.usecase.dmn.DmnArchitectureComplianceTest
  [ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.145 s <<< FAILURE! -- in com.ibpms.poc.application.usecase.dmn.DmnArchitectureComplianceTest
  [ERROR] com.ibpms.poc.application.usecase.dmn.DmnArchitectureComplianceTest.testHexagonalCompliance -- Time elapsed: 0.071 s <<< FAILURE!
  org.opentest4j.AssertionFailedError: Architectural violation (ADR-001): DmnGovernanceUseCase imports infrastructure/JPA class: import com.ibpms.poc.infrastructure.jpa.entity.dmn.DmnModelEntity;
  ```

---

## 2. Logic Chain

1. Per **ADR-001**, the application/domain layers must be isolated from technology/infrastructure layers (specifically JPA databases/repositories).
2. `DmnGovernanceUseCase.java` violates this policy by referencing and importing `DmnModelEntity` and `DmnModelRepository` directly.
3. The current implementation of `DmnArchitectureComplianceTest.java` fails (TDD red phase) because it flags the imports.
4. However, the current check is vulnerable to bypasses because it only scans statements starting with `"import "`. Any inline usage of fully qualified names (e.g., `com.ibpms.poc.infrastructure.jpa.entity.dmn.DmnModelEntity`) or usage of other JPA namespaces (e.g. `org.springframework.data.jpa`) would not trigger a failure.
5. Therefore, `DmnArchitectureComplianceTest.java` should be refactored to perform a robust scan across all non-commented code lines, asserting the absence of the forbidden strings.

---

## 3. Caveats

- **Spring Annotations**: The use case utilizes `@Service` and `@Transactional` (Spring Framework core/transaction annotations). Per instructions, these are permitted container annotations and not considered direct persistence/JPA annotations (unlike `jakarta.persistence.*` or `org.springframework.data.jpa.*`).
- **Static Analysis Depth**: The compliance test uses standard text parsing rather than full bytecode scanning (like ArchUnit). Given the simple, lightweight layout, text scanning is sufficient as long as comment parsing is handled correctly to avoid false positives in documentation.

---

## 4. Conclusion

We recommend refactoring `DmnArchitectureComplianceTest.java` to make it strictly assert that `DmnGovernanceUseCase.java` contains NO imports or usage of:
- `DmnModelEntity`
- `DmnModelRepository`
- Any persistence/JPA packages (`jakarta.persistence`, `javax.persistence`, `org.springframework.data.jpa`).

### Proposed Changes to `DmnArchitectureComplianceTest.java`

Replace the implementation of `DmnArchitectureComplianceTest.java` with:

```java
package com.ibpms.poc.application.usecase.dmn;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class DmnArchitectureComplianceTest {

    @Test
    public void testHexagonalCompliance() throws IOException {
        Path path = Paths.get("src/main/java/com/ibpms/poc/application/usecase/dmn/DmnGovernanceUseCase.java");
        if (!Files.exists(path)) {
            path = Paths.get("ibpms-core/src/main/java/com/ibpms/poc/application/usecase/dmn/DmnGovernanceUseCase.java");
        }
        
        if (!Files.exists(path)) {
            fail("Source file DmnGovernanceUseCase.java not found.");
        }

        List<String> lines = Files.readAllLines(path);
        int lineNum = 0;
        for (String line : lines) {
            lineNum++;
            String trimmed = line.trim();
            
            // Ignore comment lines to avoid false positives in documentation/tracing tags
            if (trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")) {
                continue;
            }

            // 1. Assert no imports/usage of DmnModelEntity
            if (trimmed.contains("DmnModelEntity")) {
                fail("Architectural violation (ADR-001) at line " + lineNum + 
                     ": DmnGovernanceUseCase references forbidden infrastructure entity 'DmnModelEntity'.");
            }

            // 2. Assert no imports/usage of DmnModelRepository
            if (trimmed.contains("DmnModelRepository")) {
                fail("Architectural violation (ADR-001) at line " + lineNum + 
                     ": DmnGovernanceUseCase references forbidden infrastructure repository 'DmnModelRepository'.");
            }

            // 3. Assert no persistence/JPA annotations or packages
            if (trimmed.contains("jakarta.persistence") || 
                trimmed.contains("javax.persistence") || 
                trimmed.contains("org.springframework.data.jpa")) {
                fail("Architectural violation (ADR-001) at line " + lineNum + 
                     ": DmnGovernanceUseCase contains forbidden persistence/JPA framework package: " + trimmed);
            }
        }
    }
}
```

---

## 5. Verification Method

To run the test suite and observe the TDD red phase:

1. Open PowerShell and navigate to the backend directory:
   ```powershell
   cd C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend
   ```
2. Execute the compliance test:
   ```powershell
   ..\maven\apache-maven-3.9.6\bin\mvn.cmd test -pl ibpms-core -Dtest=DmnArchitectureComplianceTest
   ```
3. Verify that the build fails with a `BUILD FAILURE` and lists an assertion error pointing to `DmnArchitectureComplianceTest.java`.
