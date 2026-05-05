package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.ApiAnnotationArchitectureRuleSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class ApiAnnotationArchitectureTest {

    @Test
    public void shouldDeclareRestControllerApiAnnotations() throws IOException {
        Path sourceRoot = projectRoot().resolve("sandwish-front-api/src/main/java/com/github/thundax/modules");

        ApiAnnotationArchitectureRuleSupport.assertRestControllersDeclareRequestMapping(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertRestControllersDeclareApi(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertMappedMethodsDeclareApiOperation(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertMappedMethodsDeclareSingleHttpMapping(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertApiOperationDeclaresAccessAnnotation(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertRequestBodyRequestParametersDeclareValid(sourceRoot);
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
