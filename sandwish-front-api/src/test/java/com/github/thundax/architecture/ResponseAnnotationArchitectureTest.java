package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.ApiAnnotationArchitectureRuleSupport;
import com.github.thundax.common.test.architecture.NamingArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class ResponseAnnotationArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldEnforceResponseClassAnnotations() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        ApiAnnotationArchitectureRuleSupport.responseClassAnnotationsRequired("com.github.thundax.modules")
                .check(classes);
    }

    @Test
    public void shouldKeepResponseCreationInInterfaceAssemblers() throws Exception {
        ApiAnnotationArchitectureRuleSupport.assertControllersDoNotCreateResponses(mainSourceRoot());
    }

    @Test
    public void shouldUseLayerTypeSuffixes() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        NamingArchitectureRuleSupport.assertLayerTypeNames(classes);
    }

    private static Path mainSourceRoot() {
        return projectRoot().resolve("sandwish-front-api/src/main/java/com/github/thundax/modules");
    }

    private static Path projectRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("pom.xml")) && Files.exists(current.resolve("sandwish-front-api"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate project root");
    }
}
