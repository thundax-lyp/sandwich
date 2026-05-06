package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.NamingArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class ServiceNamingArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldUseServiceMethodShape() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        NamingArchitectureRuleSupport.assertServiceInterfaceMethodNames(classes);
    }

    @Test
    public void shouldReturnCreatedEntityIdFromServiceAddMethods() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        NamingArchitectureRuleSupport.assertServiceAddMethodsReturnEntityId(classes);
    }

    @Test
    public void shouldPlaceServiceQueryObjectsUnderServiceQueryPackage() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        NamingArchitectureRuleSupport.assertServiceQueryObjectsUnderServiceQueryPackage(classes);
    }

    @Test
    public void shouldNotDeclareSettersInServiceQueryObjects() throws IOException {
        Path root = repositoryRoot();
        Path sourceRoot = root.resolve("sandwish-biz").resolve("src/main/java/com/github/thundax/modules");

        NamingArchitectureRuleSupport.assertServiceQueryObjectsDeclareNoSetters(sourceRoot);
    }

    @Test
    public void shouldDeclareOnlyRequiredAnnotationsInServiceQueryObjects() throws IOException {
        Path root = repositoryRoot();
        Path sourceRoot = root.resolve("sandwish-biz").resolve("src/main/java/com/github/thundax/modules");

        NamingArchitectureRuleSupport.assertServiceQueryObjectsDeclareOnlyRequiredAnnotations(sourceRoot);
    }

    private Path repositoryRoot() {
        Path current = Paths.get("").toAbsolutePath();
        if (Files.exists(current.resolve("TODO.md"))) {
            return current;
        }
        Path parent = current.getParent();
        while (parent != null) {
            if (Files.exists(parent.resolve("TODO.md"))) {
                return parent;
            }
            parent = parent.getParent();
        }
        throw new IllegalStateException("Can not find repository root");
    }
}
