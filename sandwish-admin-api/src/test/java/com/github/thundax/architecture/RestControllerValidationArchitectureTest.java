package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.LayerArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class RestControllerValidationArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldUseSpringMethodValidationForApiRequests() {
        JavaClasses classes = importPackages("com.github.thundax");

        LayerArchitectureRuleSupport.restControllersShouldNotDependOnValidator().check(classes);
    }
}
