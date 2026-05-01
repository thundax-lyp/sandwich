package com.github.thundax.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;
import org.springframework.stereotype.Component;

public class InterfaceAssemblerArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldKeepInterfaceAssemblersOutOfSpringComponents() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        noClasses()
                .that()
                .haveSimpleNameEndingWith("InterfaceAssembler")
                .should()
                .beAnnotatedWith(Component.class)
                .check(classes);
    }

    @Test
    public void shouldKeepInterfaceAssemblersFreeOfServiceDependencies() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        noClasses()
                .that()
                .haveSimpleNameEndingWith("InterfaceAssembler")
                .should()
                .dependOnClassesThat()
                .haveSimpleNameEndingWith("Service")
                .check(classes);
    }
}
