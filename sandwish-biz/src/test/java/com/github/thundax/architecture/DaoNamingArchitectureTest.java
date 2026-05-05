package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.NamingArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class DaoNamingArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldUseDaoPortMethodShape() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        NamingArchitectureRuleSupport.assertDaoInterfaceMethodNames(classes);
    }
}
