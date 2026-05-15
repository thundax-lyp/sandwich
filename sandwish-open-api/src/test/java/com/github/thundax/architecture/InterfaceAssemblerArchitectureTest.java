package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.LayerArchitectureRuleSupport;
import com.github.thundax.common.test.architecture.NamingArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class InterfaceAssemblerArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldKeepInterfaceAssemblersOutOfSpringComponents() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        LayerArchitectureRuleSupport.interfaceAssemblersShouldNotBeSpringComponents()
                .check(classes);
    }

    @Test
    public void shouldKeepInterfaceAssemblersFreeOfServiceDependencies() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        LayerArchitectureRuleSupport.interfaceAssemblersShouldNotDependOnServices()
                .check(classes);
    }

    @Test
    public void shouldKeepInterfaceAssemblerMethodsStatic() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        NamingArchitectureRuleSupport.assertInterfaceAssemblerPublicMethodsStatic(classes);
    }

    @Test
    public void shouldKeepEntityIdConversionOutOfInterfaceAssemblers() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        NamingArchitectureRuleSupport.assertInterfaceAssemblersDoNotWrapEntityIdConversion(classes);
    }
}
