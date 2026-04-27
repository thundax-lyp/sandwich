package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.ModelAnnotationArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class ResponseAnnotationArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldEnforceResponseClassAnnotations() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        ModelAnnotationArchitectureRuleSupport.responseClassAnnotationsRequired("com.github.thundax.modules")
                .check(classes);
    }
}
