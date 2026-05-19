package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.SortableArchitectureRuleSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class SortablePersistenceArchitectureTest {

    @Test
    public void shouldKeepNormalUpdatesAwayFromPriority() throws Exception {
        SortableArchitectureRuleSupport.assertSortableDaoNormalUpdateDoesNotSetPriority(mainSourceRoot());
    }

    private static Path mainSourceRoot() {
        return projectRoot().resolve("sandwish-infra/src/main/java/com/github/thundax/modules");
    }

    private static Path projectRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("pom.xml")) && Files.exists(current.resolve("sandwish-infra"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate project root");
    }
}
