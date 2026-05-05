package com.github.thundax.common.test.architecture;

import static org.junit.Assert.assertTrue;

import com.tngtech.archunit.lang.ArchRule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ApiAnnotationArchitectureRuleSupport {

    private static final Pattern API_OPERATION_PATTERN =
            Pattern.compile("@ApiOperation\\([^)]*notes\\s*=\\s*\"([^\"]*)\"[^)]*\\)([\\s\\S]*?)public\\s+");
    private static final Pattern REST_CONTROLLER_CLASS_PATTERN = Pattern.compile(
            "((?:@[A-Za-z0-9_.]+(?:\\([^)]*\\))?\\s+)*)public\\s+class\\s+([A-Za-z0-9_]+Controller)\\b");
    private static final Pattern MAPPED_METHOD_PATTERN =
            Pattern.compile("@RequestMapping\\([^)]*\\)([\\s\\S]*?)public\\s+[^{;]+\\s+([A-Za-z0-9_]+)\\s*\\(");

    private ApiAnnotationArchitectureRuleSupport() {}

    public static ArchRule requestClassAnnotationsRequired(String basePackage) {
        return ModelAnnotationArchitectureRuleSupport.requestClassAnnotationsRequired(basePackage);
    }

    public static ArchRule responseClassAnnotationsRequired(String basePackage) {
        return ModelAnnotationArchitectureRuleSupport.responseClassAnnotationsRequired(basePackage);
    }

    public static void assertApiOperationNotesDeclareHasPermission(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectHasPermissionViolations(root, path, violations));
        }

        assertTrue(
                "API methods with non-empty/non-ignore notes must declare @HasPermission: " + violations,
                violations.isEmpty());
    }

    public static void assertRestControllersDeclareRequestMapping(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectRequestMappingViolations(root, path, violations));
        }

        assertTrue("REST controllers must declare class-level @RequestMapping: " + violations, violations.isEmpty());
    }

    public static void assertMappedMethodsDeclareApiOperation(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectApiOperationViolations(root, path, violations));
        }

        assertTrue("Mapped controller methods must declare @ApiOperation: " + violations, violations.isEmpty());
    }

    private static void collectHasPermissionViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        Matcher matcher = API_OPERATION_PATTERN.matcher(content);
        while (matcher.find()) {
            String notes = matcher.group(1).trim();
            String annotations = matcher.group(2);
            if (notes.length() > 0 && !"ignore".equals(notes) && !annotations.contains("@HasPermission")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " notes=" + notes);
            }
        }
    }

    private static void collectRequestMappingViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        Matcher matcher = REST_CONTROLLER_CLASS_PATTERN.matcher(content);
        while (matcher.find()) {
            String annotations = matcher.group(1);
            String className = matcher.group(2);
            if (annotations.contains("@RestController") && !annotations.contains("@RequestMapping")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " class=" + className);
            }
        }
    }

    private static void collectApiOperationViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        Matcher matcher = MAPPED_METHOD_PATTERN.matcher(content);
        while (matcher.find()) {
            String annotations = matcher.group(1);
            String methodName = matcher.group(2);
            if (!annotations.contains("@ApiOperation")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " method=" + methodName);
            }
        }
    }
}
