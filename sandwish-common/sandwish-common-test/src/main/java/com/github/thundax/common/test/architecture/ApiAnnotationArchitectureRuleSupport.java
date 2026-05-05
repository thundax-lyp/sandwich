package com.github.thundax.common.test.architecture;

import static org.junit.Assert.assertTrue;

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

    private ApiAnnotationArchitectureRuleSupport() {}

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
}
