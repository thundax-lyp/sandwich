package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.SortableArchitectureRuleSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.Test;

public class SortableArchitectureTest {

    @Test
    public void shouldKeepSortableDomainsWithinContract() throws Exception {
        Path sourceRoot = mainSourceRoot();

        SortableArchitectureRuleSupport.assertSortableEntitiesAreAllowed(
                sourceRoot,
                Arrays.asList("Dict", "Role", "Department", "Menu", "StoredObject", "Member", "Submission"));
        SortableArchitectureRuleSupport.assertSortCommandsAreAllowed(
                sourceRoot,
                Arrays.asList(
                        "DictSortCommand",
                        "RoleSortCommand",
                        "StorageSortCommand",
                        "MemberSortCommand",
                        "SubmissionSortCommand"));
        SortableArchitectureRuleSupport.assertSortCommandsUseOrderedIdsOnly(sourceRoot);
        SortableArchitectureRuleSupport.assertSortMethodsAreTransactional(sourceRoot);
    }

    @Test
    public void shouldKeepFlatSortSchemaPriorityUnique() throws Exception {
        SortableArchitectureRuleSupport.assertFlatSortSchemasDeclarePriorityUnique(
                projectRoot().resolve("db/schema"),
                Arrays.asList("sys_dict", "sys_role", "assist_storage", "member_member", "submission_submission"));
    }

    private static Path mainSourceRoot() {
        return projectRoot().resolve("sandwish-biz/src/main/java/com/github/thundax/modules");
    }

    private static Path projectRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("pom.xml")) && Files.exists(current.resolve("sandwish-biz"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate project root");
    }
}
