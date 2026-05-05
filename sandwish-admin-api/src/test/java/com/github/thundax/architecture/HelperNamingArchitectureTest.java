package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.NamingArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class HelperNamingArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldKeepHelperNamesBounded() {
        JavaClasses classes = importPackages("com.github.thundax.common");

        NamingArchitectureRuleSupport.assertHelperNamesBounded(classes);
    }

    @Test
    public void shouldKeepToolPackagesOutOfArchitectureRoleNames() {
        JavaClasses classes = importPackages("com.github.thundax.common");

        NamingArchitectureRuleSupport.assertToolPackagesOutOfArchitectureRoleNames(classes);
    }
}
