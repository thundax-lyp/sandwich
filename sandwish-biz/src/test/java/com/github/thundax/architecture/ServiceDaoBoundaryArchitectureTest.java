package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.LayerArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class ServiceDaoBoundaryArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldKeepServiceBoundaryTypesCleanExceptKnownDirtyTypes() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        LayerArchitectureRuleSupport.assertServiceBoundaryTypesClean(classes);
    }

    @Test
    public void shouldKeepDaoBoundaryTypesClean() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        LayerArchitectureRuleSupport.assertDaoBoundaryTypesClean(classes);
    }

    @Test
    public void shouldNotIntroduceEmptyServiceBaseTypes() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        LayerArchitectureRuleSupport.assertNoEmptyServiceBaseTypes(classes);
    }

    @Test
    public void shouldNotDeclareAuthServicesInBusinessModule() {
        LayerArchitectureRuleSupport.assertBusinessModuleSourceDoesNotDeclareAuthServices();
    }

    @Test
    public void shouldRejectLayerPublicApiMethodsUsedOnlyByTests() {
        LayerArchitectureRuleSupport.assertLayerPublicApiMethodsAreNotTestOnly();
    }
}
