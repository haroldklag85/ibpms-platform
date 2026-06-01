// @Traceability: US-003 - ADR-001
package com.ibpms.poc.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class HexagonalArchitectureArchUnitTest {

    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ibpms.poc");

    @Test
    public void domainModelShouldBePureJavaPojo() {
        ArchRule rule = noClasses().that().resideInAPackage("..domain.model..")
                // Excluimos eventos legados de Kanban usando regex en el FQN
                .and().haveNameNotMatching(".*\\.KanbanTaskStatusChangedEvent$")
                .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
                .orShould().dependOnClassesThat().resideInAPackage("org.hibernate..")
                .orShould().dependOnClassesThat().resideInAPackage("org.springframework..")
                .as("Domain models must be pure POJOs and must not depend on JPA, Hibernate, or Spring Framework");

        rule.check(importedClasses);
    }

    @Test
    public void domainPortsShouldNotDependOnSpringData() {
        ArchRule rule = noClasses().that().resideInAPackage("..domain.port..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework.data..")
                .as("Domain ports must not depend on Spring Data framework classes (e.g. Page, Pageable)");

        rule.check(importedClasses);
    }

    @Test
    public void noPluralAdaptersOrPortsPackagesAreAllowed() {
        ArchRule rule1 = noClasses().should().resideInAPackage("..adapters..")
                .as("All adapter packages must be named in singular 'adapter', not plural 'adapters'");
        ArchRule rule2 = noClasses().should().resideInAPackage("..ports..")
                .as("All port packages must be named in singular 'port', not plural 'ports'");

        rule1.check(importedClasses);
        rule2.check(importedClasses);
    }

    @Test
    public void restControllersShouldOnlyResideInInfrastructureWeb() {
        ArchRule rule = classes().that().haveSimpleNameEndingWith("Controller")
                .and().haveSimpleNameNotEndingWith("Test")
                .and().haveSimpleNameNotEndingWith("Tests")
                .and().haveSimpleNameNotEndingWith("IntegrationTest")
                // Exclusión de controladores legados/temporales de otras historias por regex en FQN
                .and().haveNameNotMatching(".*\\.AuthDebugController$")
                .and().haveNameNotMatching(".*\\.FormCompletionController$")
                .and().haveNameNotMatching(".*\\.PromptLibraryController$")
                .and().haveNameNotMatching(".*\\.ProjectExecutionController$")
                .and().haveNameNotMatching(".*\\.TaskCompletionController$")
                .and().haveNameNotMatching(".*\\.GenericFormController$")
                .should().resideInAPackage("..infrastructure.web..")
                .as("All Web REST Controllers must reside in appropriate infrastructure web packages");

        rule.check(importedClasses);
    }
}
