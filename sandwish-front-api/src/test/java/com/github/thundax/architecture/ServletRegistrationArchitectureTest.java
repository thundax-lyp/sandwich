package com.github.thundax.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

public class ServletRegistrationArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldNotRegisterServletEndpoints() {
        JavaClasses classes = importPackages("com.github.thundax.autoconfigure", "com.github.thundax.modules");

        noClasses()
                .should()
                .dependOnClassesThat()
                .areAssignableTo(ServletRegistrationBean.class)
                .check(classes);
    }
}
