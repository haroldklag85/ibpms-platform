# Architectural Compliance Analysis Handoff: Hexagonal Refactoring (US-007)

## Executive Summary
This report analyzes the compliance of `DmnGovernanceUseCase.java` with the Hexagonal Architecture rules defined in `ADR-001`. It identifies architectural leaks in the current use case, recommends changes to strengthen assertions in `DmnArchitectureComplianceTest.java`, and outlines execution procedures for TDD.

---

## 1. Observation

During our investigation, we observed the following files and directories in the repository:

### ADR-001 Definition
In `ibpms-platform/docs/architecture/adr-001-hexagonal-architecture.md`:
* **Rule 1 (Isolated Domain):**
  > "El código dentro de la capa `domain` no debe tener absolutamente ninguna dependencia tecnológica. No puede importar `@Entity` de JPA, ni bibliotecas externas..." (Lines 21-22)
* **Rule 2 (Dependency Inversion):**
  > "La capa de aplicación `application/ports` definirá "Puertos de Entrada" e "Interfaces de Puertos de Salida" para hablar con bases de datos, APIs de red, o el propio motor BPM." (Lines 22-23)
* **Rule 3 (Disposable Adapters):**
  > "El código que hable con bases de datos (JPA / PostgreSQL) o los motores de proceso (Zeebe / Flowable) residirá en la periferia `infrastructure/adapters` implementando los puertos." (Lines 23-24)

### Current Architecture Leaks in `DmnGovernanceUseCase.java`
In `ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/application/usecase/dmn/DmnGovernanceUseCase.java`:
- Directly imports database JPA entities and repositories (infrastructure layer) on lines 3-4:
  ```java
  3: import com.ibpms.poc.infrastructure.jpa.entity.dmn.DmnModelEntity;
  4: import com.ibpms.poc.infrastructure.jpa.repository.dmn.DmnModelRepository;
  ```
- Directly declares a repository dependency on line 24:
  ```java
  24:     private final DmnModelRepository dmnRepository;
  ```
- Uses JPA Entity types as parameter/return types and locally in methods (lines 36, 37, 72, 73, 83, 84, 105):
  ```java
  36:     public DmnModelEntity updateDmnContent(String dmnId, String newXml, String invokerTenantId, boolean isManual) {
  37:         DmnModelEntity dmn = dmnRepository.findById(dmnId)
  ...
  73:         return dmnRepository.save(dmn);
  ```

### Limitations of `DmnArchitectureComplianceTest.java`
In `ibpms-platform/backend/ibpms-core/src/test/java/com/ibpms/poc/application/usecase/dmn/DmnArchitectureComplianceTest.java`:
- Lines 28-32 only check lines starting with `import `:
  ```java
  28:             if (trimmed.startsWith("import ")) {
  29:                 if (trimmed.contains("com.ibpms.poc.infrastructure.jpa") || trimmed.contains("jakarta.persistence")) {
  30:                     fail("Architectural violation (ADR-001): DmnGovernanceUseCase imports infrastructure/JPA class: " + trimmed);
  31:                 }
  32:             }
  ```
- **Loophole 1:** Bypassed by inline class references (e.g. `com.ibpms.poc.infrastructure.jpa.entity.dmn.DmnModelEntity dmn = ...`).
- **Loophole 2:** Fails to check for other forbidden namespaces like `org.springframework.data.jpa.*` (such as JPA repositories or annotations) or `javax.persistence.*`.
- **Loophole 3:** Fails to verify usage/references within the class body, only inspecting the `import` statements.

---

## 2. Logic Chain

1. **ADR-001 Compliance Mandate:** ADR-001 states that domain and application services (such as use cases) must be isolated from technology and infrastructure classes (such as JPA database entities/repositories).
2. **Current Leaks:** `DmnGovernanceUseCase.java` directly imports and references `DmnModelEntity` and `DmnModelRepository`. It belongs to the `application.usecase.dmn` package, violating ADR-001 by coupling with infrastructure types instead of abstracting persistence behind a port interface.
3. **Inadequacy of Compliance Test:** The current test `DmnArchitectureComplianceTest.java` only scans lines starting with `"import "`. It will detect imports of `com.ibpms.poc.infrastructure.jpa`, but it can be easily bypassed by using fully qualified inline class names (e.g., declaring `com.ibpms...DmnModelEntity` directly in parameters or methods). It also fails to test for other persistence packages.
4. **Resolution via Strict Checking:** By reading the file as a single String, stripping comments (to prevent false failures on descriptive javadoc), and using strict substring/regex checks on the class body, the test will catch any presence of `DmnModelEntity`, `DmnModelRepository`, `jakarta.persistence`, `javax.persistence`, and `org.springframework.data.jpa`.
5. **TDD Observation:** Running the newly updated test suite will produce a test failure (red phase of TDD), as the current `DmnGovernanceUseCase.java` is heavily dependent on these forbidden elements.

---

## 3. Caveats

- **Spring Stereotypes:** The Use Case utilizes Spring Framework annotations (`@Service`, `@Transactional`, `@Value`, `@org.springframework.web.client.RestTemplate`). Under pure Hexagonal Architecture, some architectures isolate use cases completely from Spring annotations (making them plain POJOs and using `@Configuration` classes to instantiate them). However, in this project's convention, `@Service` is allowed at the application layer, but direct database dependency (JPA/persistence) is strictly prohibited.
- **Transactions:** `@Transactional` is used in the use case. If the use case is fully decoupled from JPA, the transaction management will need to be handled either by a persistence port wrapper or left as a Spring transaction aspect, which is acceptable since Spring handles the actual transaction integration in the infrastructure layer.

---

## 4. Conclusion & Recommendations

### Proposed Architectural Compliance Test
We recommend replacing the test implementation in `DmnArchitectureComplianceTest.java` with the proposed content (saved as `proposed_DmnArchitectureComplianceTest.java` in the agent folder).

#### Summary of Test Enhancements:
1. **Comment Stripping:** Strips block and line comments to avoid false-positives when classes are mentioned in documentation comments.
2. **Strict Text Containment Search:** Checks if the cleaned file contains class names (`DmnModelEntity`, `DmnModelRepository`) or packages (`jakarta.persistence`, `javax.persistence`, `org.springframework.data.jpa`) anywhere in the code.
3. **Multi-Location Search Fallback:** Checks paths like `src/...` and `ibpms-core/src/...` to ensure robustness regardless of build runner directories.

### TDD Execution Command
To observe the red phase (test failure) of TDD, run the following command in PowerShell/Terminal:
```bash
cd ibpms-platform/backend/ibpms-core
mvn test -Dtest=DmnArchitectureComplianceTest
```
Since this test is a pure unit test and does not load the Spring Context or boot the database, it runs fast, offline, and does not require Docker or other running services.

### Hexagonal Refactoring Plan for the Implementer
To make `DmnGovernanceUseCase` compliant and pass the test:
1. **Create Domain Entity:** Create a technology-agnostic `DmnModel` in the domain layer (`com.ibpms.poc.domain.model` or similar) containing fields like `id`, `xmlContent`, `status`, `name`, `createdAt`, `updatedAt`, `authorJwtHash`, `tenantId`, `chatHistoryJson`, and `isManual`.
2. **Create Port Interface:** Create an output port `DmnModelPort` (e.g. in `com.ibpms.poc.domain.port` or application ports package):
   ```java
   public interface DmnModelPort {
       Optional<DmnModel> findById(String id);
       DmnModel save(DmnModel dmn);
       void delete(DmnModel dmn);
       List<DmnModel> findByTenantId(String tenantId);
   }
   ```
3. **Implement Adapter:** Implement the port in the infrastructure layer (e.g., `com.ibpms.poc.infrastructure.jpa.adapter.DmnModelPersistenceAdapter`) which is injected with `DmnModelRepository`, performs mapping between `DmnModel` domain objects and `DmnModelEntity` database structures, and implements `DmnModelPort`.
4. **Refactor Use Case:** Inject `DmnModelPort` into `DmnGovernanceUseCase` instead of `DmnModelRepository`, change references of `DmnModelEntity` to `DmnModel`, and remove the forbidden imports.

---

## 5. Verification Method

### How to Verify the Findings
1. **Apply the patch or proposed test file:**
   Apply `DmnArchitectureComplianceTest.patch` or replace `DmnArchitectureComplianceTest.java` with `proposed_DmnArchitectureComplianceTest.java`.
2. **Run the TDD failure command:**
   Execute `mvn test -Dtest=DmnArchitectureComplianceTest` under `ibpms-platform/backend/ibpms-core`.
3. **Expected result:**
   The test MUST fail, reporting:
   `Architectural violation (ADR-001): DmnGovernanceUseCase contains forbidden import or usage of: DmnModelEntity` (or similar for `DmnModelRepository`).
