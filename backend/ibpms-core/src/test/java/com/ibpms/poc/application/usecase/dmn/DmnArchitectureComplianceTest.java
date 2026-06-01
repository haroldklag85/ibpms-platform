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
        System.out.println("--- DEBUG ARCHITECTURE TEST ---");
        System.out.println("Resolved path: " + path.toAbsolutePath());
        System.out.println("File length: " + content.length());
        
        // Remove block comments (/* ... */) and line comments (// ...)
        // safely ignoring comments to avoid false positives on comments
        String cleanContent = content
            .replaceAll("/\\*(?s).*?\\*/", "")
            .replaceAll("//.*", "")
            .replaceAll("DmnModelRepositoryPort", "DmnModelPort");
        System.out.println("Cleaned content length: " + cleanContent.length());
        for (String tok : new String[]{"DmnModelEntity", "DmnModelRepository", "jakarta.persistence", "javax.persistence", "org.springframework.data.jpa"}) {
            if (cleanContent.contains(tok)) {
                int idx = cleanContent.indexOf(tok);
                int start = Math.max(0, idx - 50);
                int end = Math.min(cleanContent.length(), idx + 50);
                System.out.println("Matched token: " + tok + " at index " + idx + ". Context: " + cleanContent.substring(start, end));
            }
        }


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
