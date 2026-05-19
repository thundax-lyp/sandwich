package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.NamingArchitectureRuleSupport;
import com.github.thundax.common.test.architecture.PathArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class ConfigurationAnnotationArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldUseConfigurationAnnotationNamingRules() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        NamingArchitectureRuleSupport.assertConfigurationClassNames(classes);
    }

    @Test
    public void shouldPlaceConfigurationAnnotatedClassesUnderConfigurePackage() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        PathArchitectureRuleSupport.assertConfigurationClassPlacement(classes);
    }
}
