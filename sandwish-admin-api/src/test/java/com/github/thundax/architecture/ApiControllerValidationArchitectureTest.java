package com.github.thundax.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import javax.validation.Validator;
import org.junit.Test;
import org.springframework.web.bind.annotation.RestController;

public class ApiControllerValidationArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldUseSpringMethodValidationForApiRequests() {
        JavaClasses classes = importPackages("com.github.thundax");

        noClasses()
                .that()
                .resideInAPackage("..controller..")
                .and()
                .areAnnotatedWith(RestController.class)
                .should()
                .dependOnClassesThat()
                .areAssignableTo(Validator.class)
                .check(classes);
    }
}
