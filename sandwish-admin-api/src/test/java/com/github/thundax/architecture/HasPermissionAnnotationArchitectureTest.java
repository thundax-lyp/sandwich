package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.ApiAnnotationArchitectureRuleSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class HasPermissionAnnotationArchitectureTest {

    @Test
    public void shouldDeclareAccessAnnotationWhenApiOperationExists() throws IOException {
        Path sourceRoot = projectRoot().resolve("sandwish-admin-api/src/main/java/com/github/thundax/modules");

        ApiAnnotationArchitectureRuleSupport.assertApiOperationDeclaresAccessAnnotation(sourceRoot);
    }

    @Test
    public void shouldDeclareRestControllerApiAnnotations() throws IOException {
        Path sourceRoot = projectRoot().resolve("sandwish-admin-api/src/main/java/com/github/thundax/modules");

        ApiAnnotationArchitectureRuleSupport.assertRestControllersDeclareRequestMapping(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertRestControllerRequestMappingsUseApiResourcePath(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertRestControllersDeclareApi(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertApiTagsDoNotUseNumericPrefix(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertMappedMethodsDeclareApiOperation(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertMappedMethodsDeclareSingleHttpMapping(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertMappedMethodsUsePostOrGetMapping(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertJsonRequestMethodsUsePostMapping(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertGetMappingMethodsReturnVoid(sourceRoot);
        ApiAnnotationArchitectureRuleSupport.assertRequestBodyRequestParametersDeclareValid(sourceRoot);
    }

    private static Path projectRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("pom.xml")) && Files.exists(current.resolve("sandwish-admin-api"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate project root");
    }
}
