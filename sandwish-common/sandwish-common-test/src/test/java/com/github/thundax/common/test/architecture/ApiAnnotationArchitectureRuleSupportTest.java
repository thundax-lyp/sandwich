package com.github.thundax.common.test.architecture;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ApiAnnotationArchitectureRuleSupportTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldPassWhenApiOperationNotesDeclareHasPermission() throws IOException {
        Path sourceRoot = temporaryFolder.newFolder("src").toPath();
        writeController(
                sourceRoot,
                "@ApiOperation(value = \"query\", notes = \"sys:user:view\")\n"
                        + "    @HasPermission(\"sys:user:view\")\n"
                        + "    public void list() {}\n");

        ApiAnnotationArchitectureRuleSupport.assertApiOperationNotesDeclareHasPermission(sourceRoot);
    }

    @Test(expected = AssertionError.class)
    public void shouldRejectApiOperationNotesWithoutHasPermission() throws IOException {
        Path sourceRoot = temporaryFolder.newFolder("src").toPath();
        writeController(
                sourceRoot,
                "@ApiOperation(value = \"query\", notes = \"sys:user:view\")\n" + "    public void list() {}\n");

        ApiAnnotationArchitectureRuleSupport.assertApiOperationNotesDeclareHasPermission(sourceRoot);
    }

    @Test
    public void shouldIgnoreApiOperationIgnoreNotes() throws IOException {
        Path sourceRoot = temporaryFolder.newFolder("src").toPath();
        writeController(
                sourceRoot, "@ApiOperation(value = \"query\", notes = \"ignore\")\n" + "    public void list() {}\n");

        ApiAnnotationArchitectureRuleSupport.assertApiOperationNotesDeclareHasPermission(sourceRoot);
    }

    private void writeController(Path sourceRoot, String methodSource) throws IOException {
        Files.write(
                sourceRoot.resolve("FixtureController.java"),
                ("public class FixtureController {\n    " + methodSource + "}\n").getBytes(StandardCharsets.UTF_8));
    }
}
