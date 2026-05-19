package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.LayerArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class ServletRegistrationArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldNotRegisterServletEndpoints() {
        JavaClasses classes = importPackages("com.github.thundax.configure", "com.github.thundax.modules");

        LayerArchitectureRuleSupport.servletRegistrationBeanShouldNotBeUsed().check(classes);
    }
}
