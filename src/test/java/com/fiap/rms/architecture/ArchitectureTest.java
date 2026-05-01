package com.fiap.rms.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void importClasses() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.fiap.rms");
    }

    @Test
    void rule1_domainMustNotDependOnFrameworksOrOuterLayers() {
        noClasses().that().resideInAPackage("com.fiap.rms.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta..",
                        "javax..",
                        "org.hibernate..",
                        "org.projectlombok..",
                        "com.fiap.rms.application..",
                        "com.fiap.rms.infrastructure.."
                )
                .because("the domain layer is the core of the application and must be pure Java " +
                        "with zero framework, outer-layer, or application-layer dependencies")
                .check(importedClasses);
    }

    @Test
    void rule2_applicationMustDependOnlyOnDomainAndJdk() {
        noClasses().that().resideInAPackage("com.fiap.rms.application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta..",
                        "javax..",
                        "org.hibernate..",
                        "org.projectlombok..",
                        "com.fiap.rms.infrastructure..",
                        "com.fiap.rms.shared.."
                )
                .because("the application layer may only depend on the domain layer and the JDK; " +
                        "it must remain framework-agnostic and unaware of infrastructure concerns")
                .check(importedClasses);
    }

    @Test
    void rule3_domainAndApplicationMustNotDependOnInfrastructure() {
        noClasses().that().resideInAnyPackage(
                        "com.fiap.rms.domain..",
                        "com.fiap.rms.application.."
                )
                .should().dependOnClassesThat().resideInAPackage("com.fiap.rms.infrastructure..")
                .because("dependency arrows must always point inward: infrastructure depends on inner layers, " +
                        "never the reverse — violating this would couple business logic to delivery mechanisms")
                .check(importedClasses);
    }

    @Test
    void rule4_sharedMustNotDependOnAnyLayer() {
        noClasses().that().resideInAPackage("com.fiap.rms.shared..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.fiap.rms.domain..",
                        "com.fiap.rms.application..",
                        "com.fiap.rms.infrastructure.."
                )
                .because("shared utilities must remain layer-agnostic so that all layers can import them " +
                        "without creating dependency cycles")
                .check(importedClasses);
    }

    @Test
    void rule5_webControllersMustNotDependOnOutputAdapters() {
        noClasses().that().resideInAPackage("com.fiap.rms.infrastructure.adapter.in.web..")
                .should().dependOnClassesThat().resideInAPackage("com.fiap.rms.infrastructure.adapter.out..")
                .because("controllers must reach the outside world only via use case ports, " +
                        "never by directly wiring to output adapters — this would bypass the application layer")
                .check(importedClasses);
    }

    @Test
    void rule6_useCaseTypesMustBeInterfacesInPortIn() {
        classes().that().haveSimpleNameEndingWith("UseCase")
                .should().beInterfaces()
                .andShould().resideInAPackage("com.fiap.rms.application.port.in")
                .because("UseCase types define use case contracts; they must be interfaces located in " +
                        "application.port.in so that use case implementations can be swapped without touching callers")
                .allowEmptyShould(true)
                .check(importedClasses);
    }

    @Test
    void rule7_portTypesMustBeInterfacesInPortInOrPortOut() {
        classes().that().haveSimpleNameEndingWith("Port")
                .should().beInterfaces()
                .andShould().resideInAnyPackage(
                        "com.fiap.rms.application.port.in",
                        "com.fiap.rms.application.port.out"
                )
                .because("Port types define contracts between layers; they must be interfaces in " +
                        "application.port.in (driving) or application.port.out (driven)")
                .allowEmptyShould(true)
                .check(importedClasses);
    }

    @Test
    void rule8_adapterTypesMustLiveUnderInfrastructureAdapter() {
        classes().that().haveSimpleNameEndingWith("Adapter")
                .should().resideInAPackage("com.fiap.rms.infrastructure.adapter..")
                .because("Adapter implementations bridge the outside world to the application and must " +
                        "live under infrastructure.adapter to keep them isolated from business logic")
                .allowEmptyShould(true)
                .check(importedClasses);
    }
}
