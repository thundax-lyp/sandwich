package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.NamingArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class DaoNamingArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldUseDaoImplSuffix() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        NamingArchitectureRuleSupport.assertDaoTypeNamesUseDaoSuffix(classes);
    }

    @Test
    public void shouldUseLayerTypeSuffixes() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        NamingArchitectureRuleSupport.assertLayerTypeNames(classes);
    }
}
