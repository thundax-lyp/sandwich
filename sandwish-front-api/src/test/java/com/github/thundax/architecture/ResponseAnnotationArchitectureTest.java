package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.ApiAnnotationArchitectureRuleSupport;
import com.github.thundax.common.test.architecture.NamingArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class ResponseAnnotationArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldEnforceResponseClassAnnotations() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        ApiAnnotationArchitectureRuleSupport.responseClassAnnotationsRequired("com.github.thundax.modules")
                .check(classes);
    }

    @Test
    public void shouldUseLayerTypeSuffixes() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        NamingArchitectureRuleSupport.assertLayerTypeNames(classes);
    }
}
