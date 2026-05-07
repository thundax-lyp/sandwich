package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.LayerArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class ServiceApiInterfaceArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldKeepHttpContractsOnControllers() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        LayerArchitectureRuleSupport.serviceApiInterfacesShouldNotExist().check(classes);
    }

    @Test
    public void shouldOnlyDeclareEntryServicesInApiModule() {
        LayerArchitectureRuleSupport.assertApiModuleSourceDeclaresOnlyEntryServices("sandwish-admin-api");
    }
}
