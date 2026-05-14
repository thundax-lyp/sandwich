package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.ApiSurfaceArchitectureRuleSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.Test;

public class ApiSurfaceArchitectureTest {

    @Test
    public void shouldKeepApiModelsAndSortEndpointsWithinSurfaceContract() throws Exception {
        Path sourceRoot = mainSourceRoot();

        ApiSurfaceArchitectureRuleSupport.assertApiModelsDoNotExposePriority(sourceRoot);
        ApiSurfaceArchitectureRuleSupport.assertSortRequestsUseOrderedIdsOnly(sourceRoot);
        ApiSurfaceArchitectureRuleSupport.assertSortRequestsAreAllowed(sourceRoot, Collections.<String>emptyList());
        ApiSurfaceArchitectureRuleSupport.assertSortEndpointsAreAllowed(sourceRoot, Collections.<String>emptyList());
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
