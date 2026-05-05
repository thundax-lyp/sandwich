package com.github.thundax.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NamingArchitectureRuleSupportTest extends AbstractArchitectureTest {

    private static final String FIXTURE_PACKAGE = "com.github.thundax.common.test.architecture.fixture";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldAcceptExistingBoundedHelperAndToolNamingFixture() {
        JavaClasses classes = importPackages(FIXTURE_PACKAGE);

        NamingArchitectureRuleSupport.assertHelperNamesBounded(classes);
        NamingArchitectureRuleSupport.assertToolPackagesOutOfArchitectureRoleNames(classes);
    }

    @Test
    public void shouldPassWhenServiceQuerySourceDeclaresNoSetter() throws IOException {
        Path sourceRoot = serviceQueryRoot("valid");
        writeQuery(sourceRoot, "public class UserQuery {\n    private String name;\n}\n");

        NamingArchitectureRuleSupport.assertServiceQueryObjectsDeclareNoSetters(sourceRoot);
    }

    @Test(expected = AssertionError.class)
    public void shouldRejectServiceQuerySourceSetters() throws IOException {
        Path sourceRoot = serviceQueryRoot("invalid");
        writeQuery(sourceRoot, "public class UserQuery {\n    public void setName(String name) {}\n}\n");

        NamingArchitectureRuleSupport.assertServiceQueryObjectsDeclareNoSetters(sourceRoot);
    }

    private Path serviceQueryRoot(String name) throws IOException {
        Path root = temporaryFolder.newFolder(name).toPath();
        Path packagePath = root.resolve("modules/sys/service/query");
        Files.createDirectories(packagePath);
        return root;
    }

    private void writeQuery(Path sourceRoot, String source) throws IOException {
        Files.write(
                sourceRoot.resolve("modules/sys/service/query/UserQuery.java"),
                source.getBytes(StandardCharsets.UTF_8));
    }
}
