package com.github.thundax.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class ModelAnnotationArchitectureRuleSupportTest extends AbstractArchitectureTest {

    private static final String FIXTURE_PACKAGE =
            "com.github.thundax.common.test.architecture.fixture";

    @Test
    public void shouldPassWhenRequestClassAnnotationsMatchRequiredSet() {
        JavaClasses classes = importPackages(FIXTURE_PACKAGE + ".request.valid");

        ModelAnnotationArchitectureRuleSupport.requestClassAnnotationsRequired(FIXTURE_PACKAGE)
                .check(classes);
    }

    @Test(expected = AssertionError.class)
    public void shouldRejectRequestClassWithMissingAnnotations() {
        JavaClasses classes = importPackages(FIXTURE_PACKAGE + ".request.missing");

        ModelAnnotationArchitectureRuleSupport.requestClassAnnotationsRequired(FIXTURE_PACKAGE)
                .check(classes);
    }

    @Test(expected = AssertionError.class)
    public void shouldRejectRequestClassWithExtraAnnotations() {
        JavaClasses classes = importPackages(FIXTURE_PACKAGE + ".request.extra");

        ModelAnnotationArchitectureRuleSupport.requestClassAnnotationsRequired(FIXTURE_PACKAGE)
                .check(classes);
    }
}
