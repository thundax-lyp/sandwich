package com.github.thundax.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class ServiceApiInterfaceArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldKeepHttpContractsOnControllers() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        noClasses()
                .that()
                .resideInAPackage("..api..")
                .should()
                .haveSimpleNameEndingWith("ServiceApi")
                .check(classes);
    }
}
