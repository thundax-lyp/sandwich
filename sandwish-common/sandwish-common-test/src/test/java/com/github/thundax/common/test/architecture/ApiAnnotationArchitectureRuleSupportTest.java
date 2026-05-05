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
    public void shouldPassWhenApiOperationDeclaresHasPermission() throws IOException {
        Path sourceRoot = temporaryFolder.newFolder("src").toPath();
        writeController(
                sourceRoot,
                "@ApiOperation(value = \"query\", notes = \"sys:user:view\")\n"
                        + "    @HasPermission(\"sys:user:view\")\n"
                        + "    public void list() {}\n");

        ApiAnnotationArchitectureRuleSupport.assertApiOperationDeclaresAccessAnnotation(sourceRoot);
    }

    @Test(expected = AssertionError.class)
    public void shouldRejectApiOperationWithoutAccessAnnotation() throws IOException {
        Path sourceRoot = temporaryFolder.newFolder("src").toPath();
        writeController(sourceRoot, "@ApiOperation(value = \"query\")\n" + "    public void list() {}\n");

        ApiAnnotationArchitectureRuleSupport.assertApiOperationDeclaresAccessAnnotation(sourceRoot);
    }

    @Test
    public void shouldPassWhenApiOperationDeclaresPublicApi() throws IOException {
        Path sourceRoot = temporaryFolder.newFolder("src").toPath();
        writeController(
                sourceRoot, "@ApiOperation(value = \"query\")\n" + "    @PublicApi\n" + "    public void list() {}\n");

        ApiAnnotationArchitectureRuleSupport.assertApiOperationDeclaresAccessAnnotation(sourceRoot);
    }

    @Test
    public void shouldPassWhenApiOperationClassDeclaresPublicApi() throws IOException {
        Path sourceRoot = temporaryFolder.newFolder("publicClass").toPath();
        writeControllerClass(
                sourceRoot,
                "@RestController\n"
                        + "@PublicApi\n"
                        + "public class FixtureController {\n"
                        + "    @ApiOperation(value = \"query\")\n"
                        + "    public void list() {}\n"
                        + "}\n");

        ApiAnnotationArchitectureRuleSupport.assertApiOperationDeclaresAccessAnnotation(sourceRoot);
    }

    @Test
    public void shouldPassWhenRestControllerDeclaresClassRequestMapping() throws IOException {
        Path sourceRoot = temporaryFolder.newFolder("mapped").toPath();
        writeControllerClass(
                sourceRoot,
                "@RestController\n"
                        + "@RequestMapping(\"/api/sys/user\")\n"
                        + "public class FixtureController {\n"
                        + "}\n");

        ApiAnnotationArchitectureRuleSupport.assertRestControllersDeclareRequestMapping(sourceRoot);
    }

    @Test(expected = AssertionError.class)
    public void shouldRejectRestControllerWithoutClassRequestMapping() throws IOException {
        Path sourceRoot = temporaryFolder.newFolder("unmapped").toPath();
        writeControllerClass(sourceRoot, "@RestController\n" + "public class FixtureController {\n" + "}\n");

        ApiAnnotationArchitectureRuleSupport.assertRestControllersDeclareRequestMapping(sourceRoot);
    }

    @Test
    public void shouldPassWhenRestControllerDeclaresApi() throws IOException {
        Path sourceRoot = temporaryFolder.newFolder("api").toPath();
        writeControllerClass(
                sourceRoot,
                "@Api(tags = \"fixture\")\n" + "@RestController\n" + "public class FixtureController {\n" + "}\n");

        ApiAnnotationArchitectureRuleSupport.assertRestControllersDeclareApi(sourceRoot);
    }

    @Test(expected = AssertionError.class)
    public void shouldRejectRestControllerWithoutApi() throws IOException {
        Path sourceRoot = temporaryFolder.newFolder("missingApi").toPath();
        writeControllerClass(sourceRoot, "@RestController\n" + "public class FixtureController {\n" + "}\n");

        ApiAnnotationArchitectureRuleSupport.assertRestControllersDeclareApi(sourceRoot);
    }

    @Test
    public void shouldPassWhenMappedMethodDeclaresApiOperation() throws IOException {
        Path sourceRoot = temporaryFolder.newFolder("operation").toPath();
        writeControllerClass(
                sourceRoot,
                "@RestController\n"
                        + "public class FixtureController {\n"
                        + "    @RequestMapping(value = \"list\")\n"
                        + "    @ApiOperation(value = \"list\", notes = \"ignore\")\n"
                        + "    public void list() {}\n"
                        + "}\n");

        ApiAnnotationArchitectureRuleSupport.assertMappedMethodsDeclareApiOperation(sourceRoot);
    }

    @Test
    public void shouldPassWhenShortcutMappedMethodDeclaresApiOperation() throws IOException {
        Path sourceRoot = temporaryFolder.newFolder("shortcutOperation").toPath();
        writeControllerClass(
                sourceRoot,
                "@RestController\n"
                        + "public class FixtureController {\n"
                        + "    @ApiOperation(value = \"list\")\n"
                        + "    @GetMapping(value = \"list\")\n"
                        + "    public void list() {}\n"
                        + "}\n");

        ApiAnnotationArchitectureRuleSupport.assertMappedMethodsDeclareApiOperation(sourceRoot);
    }

    @Test(expected = AssertionError.class)
    public void shouldRejectMappedMethodWithoutApiOperation() throws IOException {
        Path sourceRoot = temporaryFolder.newFolder("missingOperation").toPath();
        writeControllerClass(
                sourceRoot,
                "@RestController\n"
                        + "public class FixtureController {\n"
                        + "    @RequestMapping(value = \"list\")\n"
                        + "    public void list() {}\n"
                        + "}\n");

        ApiAnnotationArchitectureRuleSupport.assertMappedMethodsDeclareApiOperation(sourceRoot);
    }

    @Test
    public void shouldPassWhenMappedMethodDeclaresSingleHttpMapping() throws IOException {
        Path sourceRoot = temporaryFolder.newFolder("singleMapping").toPath();
        writeControllerClass(
                sourceRoot,
                "@RestController\n"
                        + "public class FixtureController {\n"
                        + "    @ApiOperation(value = \"list\")\n"
                        + "    @PostMapping(value = \"list\")\n"
                        + "    public void list() {}\n"
                        + "}\n");

        ApiAnnotationArchitectureRuleSupport.assertMappedMethodsDeclareSingleHttpMapping(sourceRoot);
    }

    @Test(expected = AssertionError.class)
    public void shouldRejectMappedMethodWithMultipleHttpMappings() throws IOException {
        Path sourceRoot = temporaryFolder.newFolder("multipleMapping").toPath();
        writeControllerClass(
                sourceRoot,
                "@RestController\n"
                        + "public class FixtureController {\n"
                        + "    @ApiOperation(value = \"list\")\n"
                        + "    @PostMapping(value = \"list\")\n"
                        + "    @GetMapping(value = \"list\")\n"
                        + "    public void list() {}\n"
                        + "}\n");

        ApiAnnotationArchitectureRuleSupport.assertMappedMethodsDeclareSingleHttpMapping(sourceRoot);
    }

    private void writeController(Path sourceRoot, String methodSource) throws IOException {
        writeControllerClass(
                sourceRoot, "@RestController\npublic class FixtureController {\n    " + methodSource + "}\n");
    }

    private void writeControllerClass(Path sourceRoot, String source) throws IOException {
        Files.write(sourceRoot.resolve("FixtureController.java"), source.getBytes(StandardCharsets.UTF_8));
    }
}
