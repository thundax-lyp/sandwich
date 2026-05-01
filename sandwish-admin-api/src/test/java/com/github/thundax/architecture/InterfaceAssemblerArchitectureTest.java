package com.github.thundax.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.util.ArrayList;
import java.util.List;
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

    @Test
    public void shouldKeepInterfaceAssemblerMethodsStatic() {
        JavaClasses classes = importPackages("com.github.thundax.modules");
        List<String> violations = new ArrayList<>();

        for (JavaClass javaClass : classes) {
            if (!javaClass.getSimpleName().endsWith("InterfaceAssembler")) {
                continue;
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (method.getModifiers().contains(JavaModifier.PUBLIC)
                        && !method.getModifiers().contains(JavaModifier.STATIC)) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue("InterfaceAssembler public methods must be static: " + violations, violations.isEmpty());
    }
}
