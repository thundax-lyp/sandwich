package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.ApiAnnotationArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class RequestAnnotationArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldEnforceRequestClassAnnotations() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        ApiAnnotationArchitectureRuleSupport.requestClassAnnotationsRequired("com.github.thundax.modules")
                .check(classes);
    }
}
