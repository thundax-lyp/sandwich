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

    private static final String[] HTTP_MAPPING_ANNOTATIONS = {
        "@RequestMapping", "@GetMapping", "@PostMapping", "@PutMapping", "@DeleteMapping", "@PatchMapping"
    };

    private static final Pattern REST_CONTROLLER_CLASS_PATTERN = Pattern.compile(
            "((?:@[A-Za-z0-9_.]+(?:\\([^)]*\\))?\\s+)*)public\\s+class\\s+([A-Za-z0-9_]+Controller)\\b");
    private static final Pattern PUBLIC_METHOD_DECLARATION_PATTERN =
            Pattern.compile("public\\s+[^{;]+\\s+([A-Za-z0-9_]+)\\s*\\(");

    private ApiAnnotationArchitectureRuleSupport() {}

    public static ArchRule requestClassAnnotationsRequired(String basePackage) {
        return ModelAnnotationArchitectureRuleSupport.requestClassAnnotationsRequired(basePackage);
    }

    public static ArchRule responseClassAnnotationsRequired(String basePackage) {
        return ModelAnnotationArchitectureRuleSupport.responseClassAnnotationsRequired(basePackage);
    }

    public static void assertApiOperationDeclaresAccessAnnotation(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectAccessAnnotationViolations(root, path, violations));
        }

        assertTrue("API methods must declare @HasPermission or @PublicApi: " + violations, violations.isEmpty());
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

    public static void assertRestControllersDeclareApi(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectApiClassViolations(root, path, violations));
        }

        assertTrue("REST controllers must declare @Api: " + violations, violations.isEmpty());
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

    public static void assertMappedMethodsDeclareSingleHttpMapping(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path -> collectSingleHttpMappingViolations(root, path, violations));
        }

        assertTrue(
                "Mapped controller methods must declare exactly one HTTP mapping: " + violations, violations.isEmpty());
    }

    private static void collectAccessAnnotationViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        String classAnnotations = restControllerClassAnnotations(content);
        if (classAnnotations.length() == 0) {
            return;
        }
        boolean accessAnnotatedClass =
                classAnnotations.contains("@PublicApi") || classAnnotations.contains("@HasPermission");
        Matcher matcher = PUBLIC_METHOD_DECLARATION_PATTERN.matcher(content);
        int previousMethodEnd = restControllerClassEnd(content);
        while (matcher.find()) {
            String annotations = content.substring(previousMethodEnd, matcher.start());
            String methodName = matcher.group(1);
            if (annotations.contains("@ApiOperation")
                    && !accessAnnotatedClass
                    && !annotations.contains("@PublicApi")
                    && !annotations.contains("@HasPermission")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " method=" + methodName);
            }
            previousMethodEnd = matcher.end();
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

    private static void collectApiClassViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        Matcher matcher = REST_CONTROLLER_CLASS_PATTERN.matcher(content);
        while (matcher.find()) {
            String annotations = matcher.group(1);
            String className = matcher.group(2);
            if (annotations.contains("@RestController") && !annotations.contains("@Api")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " class=" + className);
            }
        }
    }

    private static void collectApiOperationViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        if (restControllerClassAnnotations(content).length() == 0) {
            return;
        }
        Matcher matcher = PUBLIC_METHOD_DECLARATION_PATTERN.matcher(content);
        int previousMethodEnd = restControllerClassEnd(content);
        while (matcher.find()) {
            String annotations = content.substring(previousMethodEnd, matcher.start());
            String methodName = matcher.group(1);
            if (httpMappingCount(annotations) > 0 && !annotations.contains("@ApiOperation")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " method=" + methodName);
            }
            previousMethodEnd = matcher.end();
        }
    }

    private static void collectSingleHttpMappingViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        if (restControllerClassAnnotations(content).length() == 0) {
            return;
        }
        Matcher matcher = PUBLIC_METHOD_DECLARATION_PATTERN.matcher(content);
        int previousMethodEnd = restControllerClassEnd(content);
        while (matcher.find()) {
            String annotations = content.substring(previousMethodEnd, matcher.start());
            String methodName = matcher.group(1);
            int mappingCount = httpMappingCount(annotations);
            if (mappingCount > 1) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " method=" + methodName);
            }
            previousMethodEnd = matcher.end();
        }
    }

    private static int httpMappingCount(String annotations) {
        int count = 0;
        for (String annotation : HTTP_MAPPING_ANNOTATIONS) {
            if (annotations.contains(annotation)) {
                count++;
            }
        }
        return count;
    }

    private static String restControllerClassAnnotations(String content) {
        Matcher matcher = REST_CONTROLLER_CLASS_PATTERN.matcher(content);
        if (matcher.find() && matcher.group(1).contains("@RestController")) {
            return matcher.group(1);
        }
        return "";
    }

    private static int restControllerClassEnd(String content) {
        Matcher matcher = REST_CONTROLLER_CLASS_PATTERN.matcher(content);
        if (matcher.find() && matcher.group(1).contains("@RestController")) {
            return matcher.end();
        }
        return 0;
    }
}
