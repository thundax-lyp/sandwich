package com.github.thundax.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import javax.servlet.http.HttpServlet;
import org.junit.Test;

public class ServletBoundaryArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldKeepBusinessModulesFreeOfServletEndpoints() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        noClasses()
                .that()
                .resideInAPackage("..modules..")
                .should()
                .beAssignableTo(HttpServlet.class)
                .check(classes);
    }

    @Test
    public void shouldKeepBusinessModulesOutOfServletPackages() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        noClasses().should().resideInAPackage("..servlet..").check(classes);
    }
}
