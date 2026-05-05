package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.LayerArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class ServletBoundaryArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldKeepBusinessModulesFreeOfServletEndpoints() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        LayerArchitectureRuleSupport.businessModulesShouldNotDeclareServletEndpoints()
                .check(classes);
    }

    @Test
    public void shouldKeepBusinessModulesOutOfServletPackages() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        LayerArchitectureRuleSupport.businessModulesShouldNotUseServletPackages()
                .check(classes);
    }
}
