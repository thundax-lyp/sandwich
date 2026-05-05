package com.github.thundax.common.test.architecture;

import com.github.thundax.common.test.architecture.fixture.path.invalid.InvalidPathController;
import com.github.thundax.common.test.architecture.fixture.path.valid.ValidPathController;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.Test;

public class PathArchitectureRuleSupportTest {

    @Test
    public void shouldAcceptControllerPathWithRequiredPrefix() {
        JavaClasses classes = new ClassFileImporter().importClasses(ValidPathController.class);

        PathArchitectureRuleSupport.controllerRequestMappingShouldStartWith(
                        ValidPathController.class.getPackage().getName(), "/sys")
                .check(classes);
    }

    @Test(expected = AssertionError.class)
    public void shouldRejectControllerPathWithoutRequiredPrefix() {
        JavaClasses classes = new ClassFileImporter().importClasses(InvalidPathController.class);

        PathArchitectureRuleSupport.controllerRequestMappingShouldStartWith(
                        InvalidPathController.class.getPackage().getName(), "/sys")
                .check(classes);
    }
}
